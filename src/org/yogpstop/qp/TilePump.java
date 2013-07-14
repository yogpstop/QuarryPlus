package org.yogpstop.qp;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.google.common.io.ByteArrayDataInput;

import buildcraft.BuildCraftFactory;
import buildcraft.api.power.IPowerProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStationary;
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
		this.mapping = nbttc.getIntArray("mapping");
		if (this.mapping.length == 0) this.mapping = new int[ForgeDirection.VALID_DIRECTIONS.length];
		this.prev = (byte) (this.connectTo.ordinal() | (working() ? 0x80 : 0));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttc) {
		super.writeToNBT(nbttc);
		nbttc.setByte("efficiency", this.efficiency);
		nbttc.setByte("connectTo", (byte) this.connectTo.ordinal());
		nbttc.setIntArray("mapping", this.mapping);
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
	void recievePacketOnServer(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
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
	void recievePacketOnClient(byte pattern, ByteArrayDataInput data) {
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
				this.mapping[i] = data.readInt();
			this.liquids.clear();
			{
				int length = data.readInt();
				for (int i = 0; i < length; i++)
					this.liquids.put(data.readInt(), data.readInt());
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

	private Block b_c;
	private ExtendedBlockStorage ebs_c;
	private int block_side;

	private static final int ARRAY_MAX = 0x1FFFF;
	private static final int[] xb = new int[ARRAY_MAX];
	private static final int[] yb = new int[ARRAY_MAX];
	private static final int[] zb = new int[ARRAY_MAX];
	private static int currentPut = 0, currentGet = 0;
	private int count;

	private static void put(int x, int y, int z) {
		xb[currentPut] = x;
		yb[currentPut] = y;
		zb[currentPut] = z;
		currentPut++;
		if (currentPut == ARRAY_MAX) currentPut = 0;
	}

	private void setTargetRepeating() {
		while (currentPut != currentGet) {
			this.ebs_c = this.ebses[xb[currentGet] >> 4][zb[currentGet] >> 4][yb[currentGet] >> 4];
			if (this.ebs_c == null) return;
			this.b_c = Block.blocksList[this.ebs_c.getExtBlockID(xb[currentGet] & 0xF, yb[currentGet] & 0xF, zb[currentGet] & 0xF)];
			if (this.blocks[yb[currentGet] - this.yOffset][xb[currentGet]][zb[currentGet]] == 0
					&& (this.b_c instanceof ILiquid || (this.b_c != null ? this.b_c.blockMaterial.isLiquid() : false))) {
				this.blocks[yb[currentGet] - this.yOffset][xb[currentGet]][zb[currentGet]] = 0x3F;
				if (0 < xb[currentGet]) put(xb[currentGet] - 1, yb[currentGet], zb[currentGet]);
				else this.blocks[yb[currentGet] - this.yOffset][xb[currentGet]][zb[currentGet]] = 0x7F;
				if (xb[currentGet] < this.block_side - 1) put(xb[currentGet] + 1, yb[currentGet], zb[currentGet]);
				else this.blocks[yb[currentGet] - this.yOffset][xb[currentGet]][zb[currentGet]] = 0x7F;
				if (0 < zb[currentGet]) put(xb[currentGet], yb[currentGet], zb[currentGet] - 1);
				else this.blocks[yb[currentGet] - this.yOffset][xb[currentGet]][zb[currentGet]] = 0x7F;
				if (zb[currentGet] < this.block_side - 1) put(xb[currentGet], yb[currentGet], zb[currentGet] + 1);
				else this.blocks[yb[currentGet] - this.yOffset][xb[currentGet]][zb[currentGet]] = 0x7F;
				if (yb[currentGet] + 1 < Y_SIZE) put(xb[currentGet], yb[currentGet] + 1, zb[currentGet]);
			}
			currentGet++;
			if (currentGet == ARRAY_MAX) currentGet = 0;
		}
		currentPut = 0;
		currentGet = 0;
	}

	private void searchLiquid(int x, int y, int z, int rg) {
		this.count = 0;
		int chunk_side = (1 + rg * 2);
		this.block_side = chunk_side * CHUNK_SCALE;
		this.cx = x;
		this.cy = y;
		this.cz = z;
		this.xOffset = ((x >> 4) - rg) << 4;
		this.yOffset = (y >> 4) << 4;
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
		setTargetRepeating();
	}

	boolean removeLiquids(IPowerProvider pp, int x, int y, int z) {
		if (!this.worldObj.getBlockMaterial(x, y, z).isLiquid()) return true;
		sendNowPacket();
		this.count++;
		if (this.cx != x || this.cy != y || this.cz != z || this.currentHeight < this.cy || this.count > 100) searchLiquid(x, y, z, RANGE);
		int block_count = 0;
		int frame_count = 0;
		Block bb;
		int bx, bz, meta, bid;
		Map<Integer, Integer> cacheLiquids = new HashMap<Integer, Integer>();
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
						if ((bb != null ? bb.blockMaterial.isLiquid() : false) || bb instanceof ILiquid) {
							block_count++;
						}
						if ((bb instanceof ILiquid && ((ILiquid) bb).stillLiquidId() == bid && ((ILiquid) bb).stillLiquidMeta() == meta)
								|| bb instanceof BlockStationary) {
							if (!cacheLiquids.containsKey(bid)) cacheLiquids.put(bid, 0);
							cacheLiquids.put(bid, cacheLiquids.get(bid) + LiquidContainerRegistry.BUCKET_VOLUME);
						}
					}
				}
			}
		}
		this.currentHeight++;
		float p = (float) (block_count * BP_R / Math.pow(CE_R, this.efficiency) + frame_count * BP_F / Math.pow(CE_F, this.efficiency));
		if (pp.useEnergy(p, p, true) == p) {
			for (Integer key : cacheLiquids.keySet()) {
				if (!this.liquids.containsKey(key)) this.liquids.put(key, 0);
				this.liquids.put(key, this.liquids.get(key) + cacheLiquids.get(key));
			}
			for (bx = 0; bx < this.block_side; bx++) {
				for (bz = 0; bz < this.block_side; bz++) {
					if (this.blocks[this.currentHeight - this.yOffset][bx][bz] != 0) {
						bid = this.ebses[bx >> 4][bz >> 4][this.currentHeight >> 4].getExtBlockID(bx & 0xF, this.currentHeight & 0xF, bz & 0xF);
						bb = Block.blocksList[bid];
						if ((bb != null ? bb.blockMaterial.isLiquid() : false) || bb instanceof ILiquid) {
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

	private final NavigableMap<Integer, Integer> liquids = new TreeMap<Integer, Integer>();
	private int[] mapping = new int[ForgeDirection.VALID_DIRECTIONS.length];

	public String[] getNames() {
		String[] ret = new String[this.mapping.length];
		for (int i = 0; i < ret.length; i++) {
			StringBuilder c = new StringBuilder();
			c.append(LiquidDictionary.findLiquidName(new LiquidStack(this.mapping[i], 0)));
			c.append(" ");
			c.append(this.liquids.get(this.mapping[i]));
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
		if (fd.ordinal() < 0 || fd.ordinal() > this.mapping.length || !this.liquids.containsKey(this.mapping[fd.ordinal()])) return null;
		return new InfVolatLiquidTank(this.mapping[fd.ordinal()], this.liquids);
	}
}
