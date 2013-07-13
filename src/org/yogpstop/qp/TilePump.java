package org.yogpstop.qp;

import java.util.HashMap;

import com.google.common.io.ByteArrayDataInput;

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
import net.minecraftforge.liquids.LiquidStack;

public class TilePump extends APacketTile implements ITankContainer {
	private static final int Y_SIZE = 256;
	private static final int CHUNK_SCALE = 16;
	private static final int RANGE = 4;

	private final HashMap<Integer, Integer> liquids = new HashMap<Integer, Integer>();
	private ForgeDirection connectTo = ForgeDirection.UNKNOWN;
	private boolean initialized = false;
	private boolean[][][] blocks;
	private int[][][] bidl;
	private int[][][] metal;
	private int cXoffset, cYoffset, cZoffset, currentHeight = Integer.MIN_VALUE;
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
		return this.currentHeight >= 0;
	}

	private void searchLiquid(int x, int y, int z, int range) {
		int chunk_side = (1 + range * 2);
		int block_side = chunk_side * CHUNK_SCALE;
		this.cx = x;
		this.cy = y;
		this.cz = z;
		this.cXoffset = (x >> 4) - range;
		this.cYoffset = y >> 4;
		this.cZoffset = (z >> 4) - range;
		this.currentHeight = Y_SIZE - (this.cYoffset << 4) - 1;
		if (this.blocks == null ? true : this.blocks.length != Y_SIZE - (this.cYoffset << 4)) {
			this.blocks = new boolean[Y_SIZE - (this.cYoffset << 4)][block_side][block_side];
			this.bidl = new int[Y_SIZE - (this.cYoffset << 4)][block_side][block_side];
			this.metal = new int[Y_SIZE - (this.cYoffset << 4)][block_side][block_side];
		}
		ExtendedBlockStorage[] ebsa;
		ExtendedBlockStorage ebs;
		int kx, ky, kz, bx, by, bz, bid;
		Block bb;
		for (kx = 0; kx < chunk_side; kx++) {
			for (kz = 0; kz < chunk_side; kz++) {
				ebsa = this.worldObj.getChunkFromChunkCoords(kx + this.cXoffset, kz + this.cZoffset).getBlockStorageArray();
				for (ky = 0; ky < (Y_SIZE >> 4) - this.cYoffset; ky++) {
					ebs = ebsa[ky + this.cYoffset];
					if (ebs == null) continue;
					for (by = 0; by < CHUNK_SCALE; by++) {
						for (bx = 0; bx < CHUNK_SCALE; bx++) {
							for (bz = 0; bz < CHUNK_SCALE; bz++) {
								bid = ebs.getExtBlockID(bx, by, bz);
								bb = Block.blocksList[bid];
								this.bidl[by | (ky << 4)][bx | (kx << 4)][bz | (kz << 4)] = bid;
								this.metal[by | (ky << 4)][bx | (kx << 4)][bz | (kz << 4)] = ebs.getExtBlockMetadata(bx, by, bz);
								if (bb instanceof ILiquid || (bb != null ? bb.blockMaterial.isLiquid() : false)) {
									this.blocks[by | (ky << 4)][bx | (kx << 4)][bz | (kz << 4)] = true;
								} else {
									this.blocks[by | (ky << 4)][bx | (kx << 4)][bz | (kz << 4)] = false;
								}

							}
						}
					}
				}
			}
		}
	}

	boolean removeLiquids(IPowerProvider pp, int x, int y, int z) {
		if (!this.worldObj.getBlockMaterial(x, y, z).isLiquid()) return true;
		sendNowPacket();
		if (this.cx != x || this.cy != y || this.cz != z || this.currentHeight < 0) searchLiquid(x, y, z, RANGE);
		int block_count = 0;
		int chunk_side = (1 + RANGE * 2);
		int block_side = CHUNK_SCALE * chunk_side;
		Block bb;
		int bx, bz, meta, bid;
		for (; block_count == 0; this.currentHeight--) {
			if (this.currentHeight < 0) return false;
			for (bx = 0; bx < block_side; bx++) {
				for (bz = 0; bz < block_side; bz++) {
					if (this.blocks[this.currentHeight][bx][bz]) {
						bid = this.bidl[this.currentHeight][bx][bz];
						bb = Block.blocksList[bid];
						meta = this.metal[this.currentHeight][bx][bz];
						if ((bb instanceof ILiquid && ((ILiquid) bb).stillLiquidId() == bid && ((ILiquid) bb).stillLiquidMeta() == meta)
								|| bb instanceof BlockStationary) {
							block_count++;
						}
					}
				}
			}
		}
		this.currentHeight++;
		float p = (float) (block_count * BP / Math.pow(CE, this.efficiency));
		if (pp.useEnergy(p, p, true) == p) {
			for (bx = 0; bx < block_side; bx++) {
				for (bz = 0; bz < block_side; bz++) {
					if (this.blocks[this.currentHeight][bx][bz]) {
						bid = this.bidl[this.currentHeight][bx][bz];
						bb = Block.blocksList[bid];
						meta = this.metal[this.currentHeight][bx][bz];
						if ((bb instanceof ILiquid && ((ILiquid) bb).stillLiquidId() == bid && ((ILiquid) bb).stillLiquidMeta() == meta)
								|| bb instanceof BlockStationary) {
							if (!this.liquids.containsKey(bid)) this.liquids.put(bid, 0);
							this.liquids.put(bid, this.liquids.get(bid) + LiquidContainerRegistry.BUCKET_VOLUME);
						}
					}
				}
			}
			for (bx = 0; bx < block_side; bx++) {
				for (bz = 0; bz < block_side; bz++) {
					if (this.blocks[this.currentHeight][bx][bz]) {
						bid = this.bidl[this.currentHeight][bx][bz];
						bb = Block.blocksList[bid];
						if ((bb != null ? bb.blockMaterial.isLiquid() : false) || bb instanceof ILiquid) {
							this.worldObj.setBlockToAir(bx + (this.cXoffset << 4), this.currentHeight + (this.cYoffset << 4), bz + (this.cZoffset << 4));
						}
					}
				}
			}
			this.currentHeight--;
		}
		sendNowPacket();
		return this.currentHeight < 0;
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
		this.connectTo = ForgeDirection.values()[nbttc.getByte("connectTo")];
		this.prev = (byte) (this.connectTo.ordinal() | (working() ? 0x80 : 0));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttc) {
		super.writeToNBT(nbttc);
		nbttc.setByte("efficiency", this.efficiency);
		nbttc.setByte("connectTo", (byte) this.connectTo.ordinal());
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
