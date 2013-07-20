package org.yogpstop.qp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import net.minecraft.util.StatCollector;
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

	boolean C_connected() {
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
		S_sendNowPacket();
		return false;
	}

	boolean G_working() {
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
		this.range = nbttc.getByte("range");
		this.prev = (byte) (this.connectTo.ordinal() | (G_working() ? 0x80 : 0));
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
		nbttc.setByte("range", this.range);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (this.worldObj.isRemote || this.initialized) return;
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
		if (te instanceof TileBasic && ((TileBasic) te).S_connect(this.connectTo.getOpposite())) {
			S_sendNowPacket();
			this.initialized = true;
		} else if (this.worldObj.isAirBlock(pX, pY, pZ) || this.connectTo == ForgeDirection.UNKNOWN) {
			this.connectTo = ForgeDirection.UNKNOWN;
			S_sendNowPacket();
			this.initialized = true;
		}
	}

	void S_setEnchantment(ItemStack is) {
		if (this.efficiency > 0) is.addEnchantment(Enchantment.enchantmentsList[32], this.efficiency);
	}

	public List<String> C_getEnchantments() {
		ArrayList<String> als = new ArrayList<String>();
		if (this.efficiency > 0) als.add(Enchantment.enchantmentsList[32].getTranslatedName(this.efficiency));
		return als;
	}

	void G_init(NBTTagList nbttl) {
		if (nbttl != null) for (int i = 0; i < nbttl.tagCount(); i++) {
			short id = ((NBTTagCompound) nbttl.tagAt(i)).getShort("id");
			short lvl = ((NBTTagCompound) nbttl.tagAt(i)).getShort("lvl");
			if (id == 32) this.efficiency = (byte) lvl;
		}
		G_reinit();
	}

	void G_reinit() {
		if (this.worldObj.isRemote) return;
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
			if (te instanceof TileBasic && ((TileBasic) te).S_connect(fd.getOpposite())) {
				this.connectTo = fd;
				S_sendNowPacket();
				return;
			}
		}
		this.connectTo = ForgeDirection.UNKNOWN;
		S_sendNowPacket();
		return;
	}

	private void S_sendNowPacket() {
		byte c = (byte) (this.connectTo.ordinal() | (G_working() ? 0x80 : 0));
		if (c != this.prev) {
			this.prev = c;
			PacketHandler.sendNowPacket(this, c);
		}
	}

	@Override
	void S_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {}

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
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final int Y_SIZE = 256;
	private static final int CHUNK_SCALE = 16;

	private byte[][][] blocks;
	private ExtendedBlockStorage[][][] ebses;
	private int xOffset, yOffset, zOffset, currentHeight = Integer.MIN_VALUE;
	private int cx, cy = -1, cz;
	private byte range = 4;

	private int block_side;

	private static final int ARRAY_MAX = 0x1FFFF;
	private static final int[] xb = new int[ARRAY_MAX];
	private static final int[] yb = new int[ARRAY_MAX];
	private static final int[] zb = new int[ARRAY_MAX];
	private static int cp = 0, cg = 0;
	private int count;

	void changeRange(EntityPlayer ep) {
		if (this.range >= 4) this.range = 0;
		else this.range++;
		ep.sendChatToPlayer(StatCollector.translateToLocalFormatted("chat.pump_rtoggle", this.range * 2 + 1));
	}

	private static void S_put(int x, int y, int z) {
		xb[cp] = x;
		yb[cp] = y;
		zb[cp] = z;
		cp++;
		if (cp == ARRAY_MAX) cp = 0;
	}

	private void S_searchLiquid(int x, int y, int z) {
		this.count = cp = cg = 0;
		int chunk_side = (1 + this.range * 2);
		this.block_side = chunk_side * CHUNK_SCALE;
		this.cx = x;
		this.cy = y;
		this.cz = z;
		this.xOffset = ((x >> 4) - this.range) << 4;
		this.yOffset = y & 0xFFFFFFF0;
		this.zOffset = ((z >> 4) - this.range) << 4;
		this.currentHeight = Y_SIZE - 1;
		this.blocks = new byte[Y_SIZE - this.yOffset][this.block_side][this.block_side];
		this.ebses = new ExtendedBlockStorage[chunk_side][chunk_side][];
		int kx, kz;
		for (kx = 0; kx < chunk_side; kx++) {
			for (kz = 0; kz < chunk_side; kz++) {
				this.ebses[kx][kz] = this.worldObj.getChunkFromChunkCoords(kx + (this.xOffset >> 4), kz + (this.zOffset >> 4)).getBlockStorageArray();
			}
		}
		S_put(x - this.xOffset, y, z - this.zOffset);
		Block b_c;
		ExtendedBlockStorage ebs_c;
		while (cp != cg) {
			ebs_c = this.ebses[xb[cg] >> 4][zb[cg] >> 4][yb[cg] >> 4];
			if (ebs_c == null) return;
			b_c = Block.blocksList[ebs_c.getExtBlockID(xb[cg] & 0xF, yb[cg] & 0xF, zb[cg] & 0xF)];
			if (this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] == 0 && Inline.isLiquid(b_c)) {
				this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x3F;
				if (0 < xb[cg]) S_put(xb[cg] - 1, yb[cg], zb[cg]);
				else this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;
				if (xb[cg] < this.block_side - 1) S_put(xb[cg] + 1, yb[cg], zb[cg]);
				else this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;
				if (0 < zb[cg]) S_put(xb[cg], yb[cg], zb[cg] - 1);
				else this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;
				if (zb[cg] < this.block_side - 1) S_put(xb[cg], yb[cg], zb[cg] + 1);
				else this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;
				if (yb[cg] + 1 < Y_SIZE) S_put(xb[cg], yb[cg] + 1, zb[cg]);
			}
			cg++;
			if (cg == ARRAY_MAX) cg = 0;
		}
	}

	boolean S_removeLiquids(IPowerProvider pp, int x, int y, int z) {
		if (!this.worldObj.getBlockMaterial(x, y, z).isLiquid()) return true;
		S_sendNowPacket();
		this.count++;
		if (this.cx != x || this.cy != y || this.cz != z || this.currentHeight < this.cy || this.count > 200) S_searchLiquid(x, y, z);
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
		S_sendNowPacket();
		return this.currentHeight < this.cy;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private final NavigableMap<Long, InfVolatLiquidTank> liquids = new TreeMap<Long, InfVolatLiquidTank>();
	private final long[] mapping = new long[ForgeDirection.VALID_DIRECTIONS.length];

	public String[] C_getNames() {
		String[] ret = new String[this.mapping.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = StatCollector.translateToLocalFormatted("chat.pumpitem", fdToString(ForgeDirection.getOrientation(i)),
					LiquidDictionary.findLiquidName(this.liquids.containsKey(this.mapping[i]) ? this.liquids.get(this.mapping[i]).ls : null),
					this.liquids.containsKey(this.mapping[i]) ? this.liquids.get(this.mapping[i]).ls.amount : 0);
		}
		return ret;
	}

	static String fdToString(ForgeDirection fd) {
		switch (fd) {
		case UP:
			return StatCollector.translateToLocal("up");
		case DOWN:
			return StatCollector.translateToLocal("down");
		case EAST:
			return StatCollector.translateToLocal("east");
		case WEST:
			return StatCollector.translateToLocal("west");
		case NORTH:
			return StatCollector.translateToLocal("north");
		case SOUTH:
			return StatCollector.translateToLocal("south");
		default:
			return StatCollector.translateToLocal("unknown_direction");
		}
	}

	String incl(int side) {
		if (this.liquids.containsKey(this.mapping[side]) && this.liquids.higherKey(this.mapping[side]) != null) this.mapping[side] = this.liquids
				.higherKey(this.mapping[side]);
		else if (!this.liquids.isEmpty()) this.mapping[side] = this.liquids.firstKey();
		else this.mapping[side] = 0;
		return LiquidDictionary.findLiquidName(this.liquids.containsKey(this.mapping[side]) ? this.liquids.get(this.mapping[side]).ls : null);
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
