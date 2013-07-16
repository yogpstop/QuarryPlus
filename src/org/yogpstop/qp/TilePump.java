package org.yogpstop.qp;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.yogpstop.Inline;

import com.google.common.io.ByteArrayDataInput;

import buildcraft.BuildCraftFactory;
import buildcraft.api.power.IPowerProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlowing;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquid;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;

public class TilePump extends APacketTile implements ITankContainer {
	private ForgeDirection connectTo = ForgeDirection.UNKNOWN;
	private boolean initialized = false;

	private byte prev = (byte) ForgeDirection.UNKNOWN.ordinal();

	static double CE_R;
	static double BP_R;
	static double CE_F;
	static double BP_F;

	protected byte efficiency;

	boolean connected() {
		int pX = this.xCoord;
		int pY = this.yCoord;
		int pZ = this.zCoord;
		switch (this.connectTo) {
		case UP:
			pY++;
			break;
		case DOWN:
			pY--;
			break;
		case SOUTH:
			pZ++;
			break;
		case NORTH:
			pZ--;
			break;
		case EAST:
			pX++;
			break;
		case WEST:
			pX--;
			break;
		default:
		}
		TileEntity te = this.worldObj.getBlockTileEntity(pX, pY, pZ);
		if (te instanceof TileBasic) return true;
		this.connectTo = ForgeDirection.UNKNOWN;
		sendNowPacket();
		return false;
	}

	boolean working() {
		return this.currentHeight >= this.cy;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttc) {
		super.readFromNBT(nbttc);
		this.efficiency = nbttc.getByte("efficiency");
		this.connectTo = ForgeDirection.values()[nbttc.getByte("connectTo")];
		this.mapping[0] = nbttc.getLong("mapping0");
		this.mapping[1] = nbttc.getLong("mapping1");
		this.mapping[2] = nbttc.getLong("mapping2");
		this.mapping[3] = nbttc.getLong("mapping3");
		this.mapping[4] = nbttc.getLong("mapping4");
		this.mapping[5] = nbttc.getLong("mapping5");
		this.prev = (byte) (this.connectTo.ordinal() | (working() ? 0x80 : 0));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttc) {
		super.writeToNBT(nbttc);
		nbttc.setByte("efficiency", this.efficiency);
		nbttc.setByte("connectTo", (byte) this.connectTo.ordinal());
		nbttc.setLong("mapping0", this.mapping[0]);
		nbttc.setLong("mapping1", this.mapping[1]);
		nbttc.setLong("mapping2", this.mapping[2]);
		nbttc.setLong("mapping3", this.mapping[3]);
		nbttc.setLong("mapping4", this.mapping[4]);
		nbttc.setLong("mapping5", this.mapping[5]);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!this.worldObj.isRemote && !this.initialized) {
			int pX, pY, pZ;
			TileEntity te;

			pX = this.xCoord;
			pY = this.yCoord;
			pZ = this.zCoord;
			switch (this.connectTo) {
			case UP:
				pY++;
				break;
			case DOWN:
				pY--;
				break;
			case SOUTH:
				pZ++;
				break;
			case NORTH:
				pZ--;
				break;
			case EAST:
				pX++;
				break;
			case WEST:
				pX--;
				break;
			default:
			}
			te = this.worldObj.getBlockTileEntity(pX, pY, pZ);
			if (te instanceof TileBasic && ((TileBasic) te).connect(this.connectTo.getOpposite())) {
				sendNowPacket();
				this.initialized = true;
			} else if (this.worldObj.isAirBlock(pX, pY, pZ) || this.connectTo == ForgeDirection.UNKNOWN) {
				this.connectTo = ForgeDirection.UNKNOWN;
				sendNowPacket();
				this.initialized = true;
			}
		}
	}

	void setEnchantment(ItemStack is) {
		if (this.efficiency > 0) is.addEnchantment(Enchantment.enchantmentsList[32], this.efficiency);
	}

	void init(NBTTagList nbttl) {
		if (nbttl != null) for (int i = 0; i < nbttl.tagCount(); i++) {
			short id = ((NBTTagCompound) nbttl.tagAt(i)).getShort("id");
			short lvl = ((NBTTagCompound) nbttl.tagAt(i)).getShort("lvl");
			if (id == 32) this.efficiency = (byte) lvl;
		}
		reinit();
	}

	void reinit() {
		int pX, pY, pZ;
		TileEntity te;
		for (ForgeDirection fd : ForgeDirection.VALID_DIRECTIONS) {
			pX = this.xCoord;
			pY = this.yCoord;
			pZ = this.zCoord;
			switch (fd) {
			case UP:
				pY++;
				break;
			case DOWN:
				pY--;
				break;
			case SOUTH:
				pZ++;
				break;
			case NORTH:
				pZ--;
				break;
			case EAST:
				pX++;
				break;
			case WEST:
				pX--;
				break;
			default:
			}
			te = this.worldObj.getBlockTileEntity(pX, pY, pZ);
			if (te instanceof TileBasic && ((TileBasic) te).connect(fd.getOpposite())) {
				this.connectTo = fd;
				sendNowPacket();
				return;
			}
		}
		this.connectTo = ForgeDirection.UNKNOWN;
		sendNowPacket();
		return;
	}

	private void sendNowPacket() {
		byte c = (byte) (this.connectTo.ordinal() | (working() ? 0x80 : 0));
		if (c != this.prev) {
			this.prev = c;
			PacketHandler.sendNowPacket(this, c);
		}
	}

	@Override
	void S_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		switch (pattern) {
		case PacketHandler.toggleLiquid_0:
		case PacketHandler.toggleLiquid_0 + 1:
		case PacketHandler.toggleLiquid_0 + 2:
		case PacketHandler.toggleLiquid_0 + 3:
		case PacketHandler.toggleLiquid_0 + 4:
		case PacketHandler.toggleLiquid_0 + 5:
			if (this.liquids.containsKey(this.mapping[pattern - PacketHandler.toggleLiquid_0])
					&& this.liquids.higherKey(this.mapping[pattern - PacketHandler.toggleLiquid_0]) != null) this.mapping[pattern
					- PacketHandler.toggleLiquid_0] = this.liquids.higherKey(this.mapping[pattern - PacketHandler.toggleLiquid_0]);
			else if (!this.liquids.isEmpty()) this.mapping[pattern - PacketHandler.toggleLiquid_0] = this.liquids.firstKey();
			else this.mapping[pattern - PacketHandler.toggleLiquid_0] = 0;
		case PacketHandler.Liquid_l:
			PacketHandler.sendPacketToPlayer(this, ep, PacketHandler.Liquid_l, this.mapping, this.liquids);
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdPump, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		}
	}

	@Override
	void C_recievePacket(byte pattern, ByteArrayDataInput data) {
		switch (pattern) {
		case PacketHandler.packetNow:
			byte flag = data.readByte();
			if ((flag & 0x80) != 0) this.cy = this.currentHeight = -1;
			else this.currentHeight = Integer.MIN_VALUE;
			this.connectTo = ForgeDirection.getOrientation(flag & 0x7F);
			this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
			break;
		case PacketHandler.Liquid_l:
			if (this.mapping.length != data.readInt()) break;
			for (int i = 0; i < this.mapping.length; i++)
				this.mapping[i] = data.readLong();
			this.liquids.clear();
			{
				int length = data.readInt();
				long dat;
				for (int i = 0; i < length; i++) {
					dat = data.readLong();
					this.liquids.put(dat, new InfVolatLiquidTank(new LiquidStack((int) (dat & 0xFFFFFFFF), data.readInt(), (int) (dat >> 32))));
				}
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final int Y_SIZE = 256;
	private static final int CHUNK_SCALE = 16;
	private static final int RANGE = 4;

	private byte[][][] blocks;
	private ExtendedBlockStorage[][][] ebses;
	private int xOffset, yOffset, zOffset, currentHeight = Integer.MIN_VALUE;
	private int cx, cy = -1, cz;

	private int block_side;

	private static final int ARRAY_MAX = 0x1FFFF;
	private static final int[] xb = new int[ARRAY_MAX];
	private static final int[] yb = new int[ARRAY_MAX];
	private static final int[] zb = new int[ARRAY_MAX];
	private static int cp = 0, cg = 0;
	private int count;

	private static void put(int x, int y, int z) {
		xb[cp] = x;
		yb[cp] = y;
		zb[cp] = z;
		cp++;
		if (cp == ARRAY_MAX) cp = 0;
	}

	private void searchLiquid(int x, int y, int z, int rg) {
		this.count = cp = cg = 0;
		int chunk_side = (1 + rg * 2);
		this.block_side = chunk_side * CHUNK_SCALE;
		this.cx = x;
		this.cy = y;
		this.cz = z;
		this.xOffset = ((x >> 4) - rg) << 4;
		this.yOffset = y & 0xFFFFFFF0;
		this.zOffset = ((z >> 4) - rg) << 4;
		this.currentHeight = Y_SIZE - 1;
		this.blocks = new byte[Y_SIZE - this.yOffset][this.block_side][this.block_side];
		this.ebses = new ExtendedBlockStorage[chunk_side][chunk_side][];
		int kx, kz;
		for (kx = 0; kx < chunk_side; kx++) {
			for (kz = 0; kz < chunk_side; kz++) {
				this.ebses[kx][kz] = this.worldObj.getChunkFromChunkCoords(kx + (this.xOffset >> 4), kz + (this.zOffset >> 4)).getBlockStorageArray();
			}
		}
		put(x - this.xOffset, y, z - this.zOffset);
		Block b_c;
		ExtendedBlockStorage ebs_c;
		while (cp != cg) {
			ebs_c = this.ebses[xb[cg] >> 4][zb[cg] >> 4][yb[cg] >> 4];
			if (ebs_c == null) return;
			b_c = Block.blocksList[ebs_c.getExtBlockID(xb[cg] & 0xF, yb[cg] & 0xF, zb[cg] & 0xF)];
			if (this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] == 0 && Inline.isLiquid(b_c)) {
				this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x3F;
				if (0 < xb[cg]) put(xb[cg] - 1, yb[cg], zb[cg]);
				else this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;
				if (xb[cg] < this.block_side - 1) put(xb[cg] + 1, yb[cg], zb[cg]);
				else this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;
				if (0 < zb[cg]) put(xb[cg], yb[cg], zb[cg] - 1);
				else this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;
				if (zb[cg] < this.block_side - 1) put(xb[cg], yb[cg], zb[cg] + 1);
				else this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;
				if (yb[cg] + 1 < Y_SIZE) put(xb[cg], yb[cg] + 1, zb[cg]);
			}
			cg++;
			if (cg == ARRAY_MAX) cg = 0;
		}
	}

	boolean removeLiquids(IPowerProvider pp, int x, int y, int z) {
		if (!this.worldObj.getBlockMaterial(x, y, z).isLiquid()) return true;
		sendNowPacket();
		this.count++;
		if (this.cx != x || this.cy != y || this.cz != z || this.currentHeight < this.cy || this.count > 200) searchLiquid(x, y, z, RANGE);
		int block_count = 0;
		int frame_count = 0;
		Block bb;
		int bx, bz, meta, bid;
		Map<Long, Integer> cacheLiquids = new HashMap<Long, Integer>();
		for (; block_count == 0; this.currentHeight--) {
			if (this.currentHeight < this.cy) return false;
			for (bx = 0; bx < this.block_side; bx++) {
				for (bz = 0; bz < this.block_side; bz++) {
					if (this.blocks[this.currentHeight - this.yOffset][bx][bz] != 0) {
						if ((this.blocks[this.currentHeight - this.yOffset][bx][bz] & 0x40) != 0) {
							frame_count++;
						}
						bid = this.ebses[bx >> 4][bz >> 4][this.currentHeight >> 4].getExtBlockID(bx & 0xF, this.currentHeight & 0xF, bz & 0xF);
						bb = Block.blocksList[bid];
						meta = this.ebses[bx >> 4][bz >> 4][this.currentHeight >> 4].getExtBlockMetadata(bx & 0xF, this.currentHeight & 0xF, bz & 0xF);
						if (Inline.isLiquid(bb)) {
							block_count++;
							if (bb instanceof ILiquid && ((ILiquid) bb).stillLiquidMeta() == meta) {
								long key = ((ILiquid) bb).stillLiquidId() | (meta << 32);
								if (!cacheLiquids.containsKey(key)) cacheLiquids.put(key, 0);
								cacheLiquids.put(key, cacheLiquids.get(key) + LiquidContainerRegistry.BUCKET_VOLUME);
							} else if (meta == 0) {
								if (bb instanceof BlockFlowing) bid--;
								if (!cacheLiquids.containsKey((long) bid)) cacheLiquids.put((long) bid, 0);
								cacheLiquids.put((long) bid, cacheLiquids.get((long) bid) + LiquidContainerRegistry.BUCKET_VOLUME);
							}
						}
					}
				}
			}
		}
		this.currentHeight++;
		float p = (float) (block_count * BP_R / Math.pow(CE_R, this.efficiency) + frame_count * BP_F / Math.pow(CE_F, this.efficiency));
		if (pp.useEnergy(p, p, true) == p) {
			for (Long key : cacheLiquids.keySet()) {
				if (!this.liquids.containsKey(key)) this.liquids.put(key, new InfVolatLiquidTank(
						new LiquidStack((int) (key & 0xFFFFFFFF), 0, (int) (key >> 32))));
				this.liquids.get(key).ls.amount += cacheLiquids.get(key);
			}
			for (bx = 0; bx < this.block_side; bx++) {
				for (bz = 0; bz < this.block_side; bz++) {
					if (this.blocks[this.currentHeight - this.yOffset][bx][bz] != 0) {
						bid = this.ebses[bx >> 4][bz >> 4][this.currentHeight >> 4].getExtBlockID(bx & 0xF, this.currentHeight & 0xF, bz & 0xF);
						bb = Block.blocksList[bid];
						if (Inline.isLiquid(bb)) {
							if ((this.blocks[this.currentHeight - this.yOffset][bx][bz] & 0x40) != 0) this.worldObj.setBlock(bx + this.xOffset,
									this.currentHeight, bz + this.zOffset, BuildCraftFactory.frameBlock.blockID);
							else this.worldObj.setBlockToAir(bx + this.xOffset, this.currentHeight, bz + this.zOffset);
						}
					}
				}
			}
			this.currentHeight--;
		}
		sendNowPacket();
		return this.currentHeight < this.cy;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private final NavigableMap<Long, InfVolatLiquidTank> liquids = new TreeMap<Long, InfVolatLiquidTank>();
	private final long[] mapping = new long[ForgeDirection.VALID_DIRECTIONS.length];

	public String[] getNames() {
		String[] ret = new String[this.mapping.length];
		for (int i = 0; i < ret.length; i++) {
			StringBuilder c = new StringBuilder();
			c.append(LiquidDictionary.findLiquidName(this.liquids.containsKey(this.mapping[i]) ? this.liquids.get(this.mapping[i]).ls : null));
			c.append(" ");
			c.append(this.liquids.containsKey(this.mapping[i]) ? this.liquids.get(this.mapping[i]).ls.amount : null);
			ret[i] = c.toString();
		}
		return ret;
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		ILiquidTank lt = getTank(from, null);
		return lt == null ? null : lt.drain(maxDrain, doDrain);
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection fd) {
		return new ILiquidTank[] { getTank(fd, null) };
	}

	@Override
	public ILiquidTank getTank(ForgeDirection fd, LiquidStack type) {
		if (fd.ordinal() < 0 || fd.ordinal() > this.mapping.length) return null;
		return this.liquids.get(this.mapping[fd.ordinal()]);
	}
}
