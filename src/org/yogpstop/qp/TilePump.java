package org.yogpstop.qp;

import java.util.HashMap;

import com.google.common.io.ByteArrayDataInput;

import buildcraft.api.power.IPowerProvider;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquid;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;

public class TilePump extends APacketTile implements ITankContainer {
	private static final int Y_SIZE = 256;
	private static final int CHUNK_SCALE = 16;
	private static final int RANGE = 4;

	private boolean[][][] blocks;
	private int Xoffset, Zoffset;
	private int currentHeight = Integer.MIN_VALUE;
	private final HashMap<Integer, Integer> liquids = new HashMap<Integer, Integer>();
	private ForgeDirection connectTo = ForgeDirection.UNKNOWN;
	private int cx, cy = -1, cz;
	private byte prev = (byte) ForgeDirection.UNKNOWN.ordinal();

	public static double CE;
	public static double BP;

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

	private void searchLiquid(int x, int y, int z, int range) {
		this.currentHeight = 255;
		this.cx = x;
		this.cy = y;
		this.cz = z;
		int blocks_l = CHUNK_SCALE * (1 + range * 2);
		int[][][] list = new int[Y_SIZE][blocks_l][blocks_l];
		this.blocks = new boolean[Y_SIZE][blocks_l][blocks_l];
		this.Xoffset = ((x >> 4) - range) << 4;
		this.Zoffset = ((z >> 4) - range) << 4;
		int bid, tx, ty, tz;
		int depth = 1;
		boolean checked = false;
		list[y][x - this.Xoffset][z - this.Zoffset] = depth;
		this.blocks[y][x - this.Xoffset][z - this.Zoffset] = true;
		while (true) {
			depth++;
			for (ty = 0; ty < Y_SIZE; ty++) {
				for (tx = 0; tx < blocks_l; tx++) {
					for (tz = 0; tz < blocks_l; tz++) {
						if (list[ty][tx][tz] == depth - 1) {
							bid = this.worldObj.getBlockId(tx + this.Xoffset, ty, tz + this.Zoffset);
							if (Block.blocksList[bid] instanceof ILiquid || bid == Block.waterStill.blockID || bid == Block.lavaStill.blockID
									|| bid == Block.waterMoving.blockID || bid == Block.lavaMoving.blockID) {
								if (tx > 0 && list[ty][tx - 1][tz] == 0) list[ty][tx - 1][tz] = depth;
								if (tx + 1 < blocks_l && list[ty][tx + 1][tz] == 0) list[ty][tx + 1][tz] = depth;
								if (tz > 0 && list[ty][tx][tz - 1] == 0) list[ty][tx][tz - 1] = depth;
								if (tz + 1 < blocks_l && list[ty][tx][tz + 1] == 0) list[ty][tx][tz + 1] = depth;
								if (ty > 0 && list[ty - 1][tx][tz] == 0) list[ty - 1][tx][tz] = depth;
								if (ty + 1 < Y_SIZE && list[ty + 1][tx][tz] == 0) list[ty + 1][tx][tz] = depth;
								if (ty >= y) this.blocks[ty][tx][tz] = true;
								checked = true;
							}
						}
					}
				}
			}
			if (!checked) break;
			checked = false;
		}
	}

	boolean removeLiquids(IPowerProvider pp, int x, int y, int z) {
		if (!this.worldObj.getBlockMaterial(x, y, z).isLiquid()) return true;
		sendNowPacket();
		if (this.cx != x || this.cy != y || this.cz != z || this.currentHeight < 0) searchLiquid(x, y, z, RANGE);
		int tx, tz, block_count = 0;
		int blocks_l = CHUNK_SCALE * (1 + RANGE * 2);
		for (; block_count == 0; this.currentHeight--) {
			if (this.currentHeight < 0) return false;
			for (tx = 0; tx < blocks_l; tx++) {
				for (tz = 0; tz < blocks_l; tz++) {
					if (this.blocks[this.currentHeight][tx][tz]) {
						block_count++;
					}
				}
			}
		}
		this.currentHeight++;
		float p = (float) (block_count * BP / Math.pow(CE, this.efficiency));
		if (pp.useEnergy(p, p, true) == p) {
			int bid;
			Block bb;
			for (tx = 0; tx < blocks_l; tx++) {
				for (tz = 0; tz < blocks_l; tz++) {
					if (this.blocks[this.currentHeight][tx][tz]) {
						bid = this.worldObj.getBlockId(tx + this.Xoffset, this.currentHeight, tz + this.Zoffset);
						bb = Block.blocksList[bid];
						if ((bb instanceof ILiquid && ((ILiquid) bb).stillLiquidId() == bid && ((ILiquid) bb).stillLiquidMeta() == this.worldObj
								.getBlockMetadata(tx + this.Xoffset, this.currentHeight, tz + this.Zoffset))
								|| bid == Block.waterStill.blockID
								|| bid == Block.lavaStill.blockID) {
							if (!this.liquids.containsKey(bid)) this.liquids.put(bid, 0);
							this.liquids.put(bid, this.liquids.get(bid) + LiquidContainerRegistry.BUCKET_VOLUME);
						}
					}
				}
			}
			for (tx = 0; tx < blocks_l; tx++) {
				for (tz = 0; tz < blocks_l; tz++) {
					if (this.blocks[this.currentHeight][tx][tz]) {
						if (this.worldObj.getBlockMaterial(tx + this.Xoffset, this.currentHeight, tz + this.Zoffset).isLiquid()) {
							this.worldObj.setBlockToAir(tx + this.Xoffset, this.currentHeight, tz + this.Zoffset);
						}
						this.blocks[this.currentHeight][tx][tz] = false;
					}
				}
			}
			this.currentHeight--;
		}
		sendNowPacket();
		return this.currentHeight < y;
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
		return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction) {
		return null;
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) {
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttc) {
		super.readFromNBT(nbttc);
		this.efficiency = nbttc.getByte("efficiency");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttc) {
		super.writeToNBT(nbttc);
		nbttc.setByte("efficiency", this.efficiency);
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
	void recievePacketOnServer(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {}

	@Override
	void recievePacketOnClient(byte pattern, ByteArrayDataInput data) {
		switch (pattern) {
		case PacketHandler.packetNow:
			byte flag = data.readByte();
			if ((flag & 0x80) != 0) this.cy = this.currentHeight = -1;
			else this.currentHeight = Integer.MIN_VALUE;
			this.connectTo = ForgeDirection.getOrientation(flag & 0x7F);
			this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
	}

}
