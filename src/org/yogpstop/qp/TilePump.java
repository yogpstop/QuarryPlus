package org.yogpstop.qp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import buildcraft.BuildCraftFactory;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.core.Box;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidHandler;

public class TilePump extends APacketTile implements IFluidHandler, IPowerReceptor {
	private ForgeDirection connectTo = ForgeDirection.UNKNOWN;
	private boolean initialized = false;

	private byte prev = (byte) ForgeDirection.UNKNOWN.ordinal();

	protected byte unbreaking;
	protected byte fortune;
	protected boolean silktouch;
	protected byte efficiency;//No advantage

	TileBasic G_connected() {
		TileEntity te = this.worldObj.getBlockTileEntity(this.xCoord + this.connectTo.offsetX, this.yCoord + this.connectTo.offsetY, this.zCoord
				+ this.connectTo.offsetZ);
		if (te instanceof TileBasic) return (TileBasic) te;
		this.connectTo = ForgeDirection.UNKNOWN;
		S_sendNowPacket();
		return null;
	}

	boolean G_working() {
		return this.currentHeight >= this.cy;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttc) {
		super.readFromNBT(nbttc);
		this.silktouch = nbttc.getBoolean("silktouch");
		this.fortune = nbttc.getByte("fortune");
		this.efficiency = nbttc.getByte("efficiency");
		this.unbreaking = nbttc.getByte("unbreaking");
		this.connectTo = ForgeDirection.values()[nbttc.getByte("connectTo")];
		if (nbttc.getTag("mapping0") instanceof NBTTagString) {
			this.mapping[0] = nbttc.getString("mapping0");
			this.mapping[1] = nbttc.getString("mapping1");
			this.mapping[2] = nbttc.getString("mapping2");
			this.mapping[3] = nbttc.getString("mapping3");
			this.mapping[4] = nbttc.getString("mapping4");
			this.mapping[5] = nbttc.getString("mapping5");
		}
		this.range = nbttc.getByte("range");
		this.quarryRange = nbttc.getBoolean("quarryRange");
		this.prev = (byte) (this.connectTo.ordinal() | (G_working() ? 0x80 : 0));
		if (this.silktouch) {
			this.liquids.clear();
			NBTTagList nbttl = nbttc.getTagList("liquids");
			for (int i = 0; i < nbttl.tagCount(); i++) {
				this.liquids.add(FluidStack.loadFluidStackFromNBT((NBTTagCompound) nbttl.tagAt(i)));
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttc) {
		super.writeToNBT(nbttc);
		nbttc.setBoolean("silktouch", this.silktouch);
		nbttc.setByte("fortune", this.fortune);
		nbttc.setByte("efficiency", this.efficiency);
		nbttc.setByte("unbreaking", this.unbreaking);
		nbttc.setByte("connectTo", (byte) this.connectTo.ordinal());
		nbttc.setString("mapping0", this.mapping[0] == null ? "null" : this.mapping[0]);
		nbttc.setString("mapping1", this.mapping[1] == null ? "null" : this.mapping[1]);
		nbttc.setString("mapping2", this.mapping[2] == null ? "null" : this.mapping[2]);
		nbttc.setString("mapping3", this.mapping[3] == null ? "null" : this.mapping[3]);
		nbttc.setString("mapping4", this.mapping[4] == null ? "null" : this.mapping[4]);
		nbttc.setString("mapping5", this.mapping[5] == null ? "null" : this.mapping[5]);
		nbttc.setByte("range", this.range);
		nbttc.setBoolean("quarryRange", this.quarryRange);
		if (this.silktouch) {
			NBTTagList nbttl = new NBTTagList();
			for (FluidStack l : this.liquids)
				nbttl.appendTag(l.writeToNBT(new NBTTagCompound()));
			nbttc.setTag("liquids", nbttl);
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		int pX, pY, pZ;
		TileEntity te;
		FluidStack fs;
		for (ForgeDirection fd : ForgeDirection.VALID_DIRECTIONS) {
			pZ = this.liquids.indexOf(FluidRegistry.getFluidStack(this.mapping[fd.ordinal()], 0));
			if (pZ == -1) continue;
			fs = this.liquids.get(pZ);
			te = this.worldObj.getBlockTileEntity(this.xCoord + this.connectTo.offsetX,this.yCoord + this.connectTo.offsetY,this.zCoord + this.connectTo.offsetZ);
			if (te instanceof IFluidHandler && ((IFluidHandler) te).canFill(fd.getOpposite(), fs.getFluid())) fs.amount -= ((IFluidHandler) te).fill(
					fd.getOpposite(), fs, true);
		}
		if (this.worldObj.isRemote || this.initialized) return;
		pX = this.xCoord + this.connectTo.offsetX;
		pY = this.yCoord + this.connectTo.offsetY;
		pZ = this.zCoord + this.connectTo.offsetZ;
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
		if (this.silktouch) is.addEnchantment(Enchantment.enchantmentsList[33], 1);
		if (this.unbreaking > 0) is.addEnchantment(Enchantment.enchantmentsList[34], this.unbreaking);
		if (this.fortune > 0) is.addEnchantment(Enchantment.enchantmentsList[35], this.fortune);
	}

	public List<String> C_getEnchantments() {
		ArrayList<String> als = new ArrayList<String>();
		if (this.efficiency > 0) als.add(Enchantment.enchantmentsList[32].getTranslatedName(this.efficiency));
		if (this.silktouch) als.add(Enchantment.enchantmentsList[33].getTranslatedName(1));
		if (this.unbreaking > 0) als.add(Enchantment.enchantmentsList[34].getTranslatedName(this.unbreaking));
		if (this.fortune > 0) als.add(Enchantment.enchantmentsList[35].getTranslatedName(this.fortune));
		return als;
	}

	void G_init(NBTTagList nbttl) {
		if (nbttl != null) for (int i = 0; i < nbttl.tagCount(); i++) {
			short id = ((NBTTagCompound) nbttl.tagAt(i)).getShort("id");
			short lvl = ((NBTTagCompound) nbttl.tagAt(i)).getShort("lvl");
			if (id == 32) this.efficiency = (byte) lvl;
			if (id == 33) this.silktouch = true;
			if (id == 34) this.unbreaking = (byte) lvl;
			if (id == 35) this.fortune = (byte) lvl;
		}
		G_reinit();
	}

	void G_reinit() {
		if (this.worldObj.isRemote) return;
		TileEntity te;
		for (ForgeDirection fd : ForgeDirection.VALID_DIRECTIONS) {
			te = this.worldObj.getBlockTileEntity(this.xCoord + this.connectTo.offsetX, this.yCoord + this.connectTo.offsetY, this.zCoord
					+ this.connectTo.offsetZ);
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
	private byte range = 0;
	private boolean quarryRange = true;

	private int block_side_x, block_side_z;

	private static final int ARRAY_MAX = 0x80000;
	private static final int[] xb = new int[ARRAY_MAX];
	private static final int[] yb = new int[ARRAY_MAX];
	private static final int[] zb = new int[ARRAY_MAX];
	private static int cp = 0, cg = 0;
	private int count;

	private Box S_getBox() {
		TileBasic tb = G_connected();
		if (tb instanceof TileQuarry) return ((TileQuarry) tb).box;
		return null;
	}

	void S_changeRange(EntityPlayer ep) {
		if (this.range >= this.fortune * 2) {
			if (G_connected() instanceof TileQuarry) this.quarryRange = true;
			this.range = 0;
		} else if (this.quarryRange) {
			this.quarryRange = false;
		} else this.range++;
		PacketDispatcher.sendPacketToPlayer(
				new Packet3Chat(ChatMessageComponent.func_111066_d(StatCollector.translateToLocalFormatted("chat.pump_rtoggle", this.quarryRange ? "quarry"
						: Integer.toString(this.range * 2 + 1)))), (Player) ep);
		this.count = Integer.MAX_VALUE - 1;
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
		int chunk_side_x, chunk_side_z;
		this.cx = x;
		this.cy = y;
		this.cz = z;
		this.yOffset = y & 0xFFFFFFF0;
		this.currentHeight = Y_SIZE - 1;
		Box b = S_getBox();
		if (b != null && b.isInitialized()) {
			chunk_side_x = 1 + (b.xMax >> 4) - (b.xMin >> 4);
			chunk_side_z = 1 + (b.zMax >> 4) - (b.zMin >> 4);
			this.xOffset = b.xMin & 0xFFFFFFF0;
			this.zOffset = b.zMin & 0xFFFFFFF0;
			int x_add = ((this.range * 2) + 1) - chunk_side_x;
			if (x_add > 0) {
				chunk_side_x += x_add;
				this.xOffset -= ((x_add & 0xFFFFFFFE) << 3) + (((x_add % 2) != 0 && (b.centerX() % 0x10) <= 8) ? 0x10 : 0);
			}
			int z_add = ((this.range * 2) + 1) - chunk_side_z;
			if (z_add > 0) {
				chunk_side_z += z_add;
				this.zOffset -= ((z_add & 0xFFFFFFFE) << 3) + (((z_add % 2) != 0 && (b.centerZ() % 0x10) <= 8) ? 0x10 : 0);
			}
		} else {
			this.quarryRange = false;
			chunk_side_x = chunk_side_z = (1 + this.range * 2);
			this.xOffset = ((x >> 4) - this.range) << 4;
			this.zOffset = ((z >> 4) - this.range) << 4;

		}
		if (!this.quarryRange) b = null;
		this.block_side_x = chunk_side_x * CHUNK_SCALE;
		this.block_side_z = chunk_side_z * CHUNK_SCALE;
		this.blocks = new byte[Y_SIZE - this.yOffset][this.block_side_x][this.block_side_z];
		this.ebses = new ExtendedBlockStorage[chunk_side_x][chunk_side_z][];
		int kx, kz;
		for (kx = 0; kx < chunk_side_x; kx++) {
			for (kz = 0; kz < chunk_side_z; kz++) {
				this.ebses[kx][kz] = this.worldObj.getChunkFromChunkCoords(kx + (this.xOffset >> 4), kz + (this.zOffset >> 4)).getBlockStorageArray();
			}
		}
		S_put(x - this.xOffset, y, z - this.zOffset);
		Block b_c;
		ExtendedBlockStorage ebs_c;
		while (cp != cg) {
			ebs_c = this.ebses[xb[cg] >> 4][zb[cg] >> 4][yb[cg] >> 4];
			if (ebs_c != null) {
				b_c = Block.blocksList[ebs_c.getExtBlockID(xb[cg] & 0xF, yb[cg] & 0xF, zb[cg] & 0xF)];
				if (this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] == 0 && isLiquid(b_c)) {
					this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x3F;

					if ((b != null ? b.xMin & 0xF : 0) < xb[cg]) S_put(xb[cg] - 1, yb[cg], zb[cg]);
					else this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;

					if (xb[cg] < (b != null ? b.xMax - this.xOffset : this.block_side_x - 1)) S_put(xb[cg] + 1, yb[cg], zb[cg]);
					else this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;

					if ((b != null ? b.zMin & 0xF : 0) < zb[cg]) S_put(xb[cg], yb[cg], zb[cg] - 1);
					else this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;

					if (zb[cg] < (b != null ? b.zMax - this.zOffset : this.block_side_z - 1)) S_put(xb[cg], yb[cg], zb[cg] + 1);
					else this.blocks[yb[cg] - this.yOffset][xb[cg]][zb[cg]] = 0x7F;

					if (yb[cg] + 1 < Y_SIZE) S_put(xb[cg], yb[cg] + 1, zb[cg]);
				}
			}
			cg++;
			if (cg == ARRAY_MAX) cg = 0;
		}
	}

	boolean S_removeLiquids(PowerHandler tbpp, int x, int y, int z) {
		if (!this.worldObj.getBlockMaterial(x, y, z).isLiquid()) return true;
		S_sendNowPacket();
		this.count++;
		if (this.cx != x || this.cy != y || this.cz != z || this.currentHeight < this.cy || this.count > 200) S_searchLiquid(x, y, z);
		int block_count = 0;
		int frame_count = 0;
		Block bb;
		int bx, bz, meta, bid;
		FluidStack fs = null;
		for (; block_count == 0; this.currentHeight--) {
			if (this.currentHeight < this.cy) return false;
			for (bx = 0; bx < this.block_side_x; bx++) {
				for (bz = 0; bz < this.block_side_z; bz++) {
					if (this.blocks[this.currentHeight - this.yOffset][bx][bz] != 0) {
						if ((this.blocks[this.currentHeight - this.yOffset][bx][bz] & 0x40) != 0) {
							frame_count++;
						}
						bid = this.ebses[bx >> 4][bz >> 4][this.currentHeight >> 4].getExtBlockID(bx & 0xF, this.currentHeight & 0xF, bz & 0xF);
						bb = Block.blocksList[bid];
						if (isLiquid(bb)) {
							block_count++;
						}
					}
				}
			}
		}
		this.currentHeight++;
		float p = (float) (block_count * 10D / (this.unbreaking + 1) + frame_count * 25D / (this.unbreaking + 1));
		float used = tbpp.useEnergy(p, p, false);
		if (used == p) {
			used = tbpp.useEnergy(p, p, true);
			for (bx = 0; bx < this.block_side_x; bx++) {
				for (bz = 0; bz < this.block_side_z; bz++) {
					if (this.blocks[this.currentHeight - this.yOffset][bx][bz] != 0) {
						bid = this.ebses[bx >> 4][bz >> 4][this.currentHeight >> 4].getExtBlockID(bx & 0xF, this.currentHeight & 0xF, bz & 0xF);
						bb = Block.blocksList[bid];
						meta = this.ebses[bx >> 4][bz >> 4][this.currentHeight >> 4].getExtBlockMetadata(bx & 0xF, this.currentHeight & 0xF, bz & 0xF);
						if (isLiquid(bb)) {
							if (bb instanceof IFluidBlock
									&& ((IFluidBlock) bb).canDrain(this.worldObj, bx + this.xOffset, this.currentHeight, bz + this.zOffset)) {
								fs = ((IFluidBlock) bb).drain(this.worldObj, bx + this.xOffset, this.currentHeight, bz + this.zOffset, true);
							} else if ((bid == Block.waterStill.blockID || bid == Block.waterMoving.blockID) && meta == 0) {
								this.worldObj.setBlockToAir(bx + this.xOffset, this.currentHeight, bz + this.zOffset);
								fs = new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME);
							} else if ((bid == Block.lavaStill.blockID || bid == Block.lavaMoving.blockID) && meta == 0) {
								this.worldObj.setBlockToAir(bx + this.xOffset, this.currentHeight, bz + this.zOffset);
								fs = new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME);
							}
							if (fs != null) {
								int index = this.liquids.indexOf(fs);
								if (index != -1) this.liquids.get(index).amount += fs.amount;
								else this.liquids.add(fs);
								fs = null;
							} else this.worldObj.setBlockToAir(bx + this.xOffset, this.currentHeight, bz + this.zOffset);
							if ((this.blocks[this.currentHeight - this.yOffset][bx][bz] & 0x40) != 0) this.worldObj.setBlock(bx + this.xOffset,
									this.currentHeight, bz + this.zOffset, BuildCraftFactory.frameBlock.blockID);
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

	private final LinkedList<FluidStack> liquids = new LinkedList<FluidStack>();
	private final String[] mapping = new String[ForgeDirection.VALID_DIRECTIONS.length];

	public String[] C_getNames() {
		String[] ret = new String[this.mapping.length];
		int index;
		for (int i = 0; i < ret.length; i++) {
			index = this.liquids.indexOf(FluidRegistry.getFluidStack(this.mapping[i], 0));
			ret[i] = StatCollector.translateToLocalFormatted("chat.pumpitem", fdToString(ForgeDirection.getOrientation(i)), this.mapping[i], index == -1 ? 0
					: this.liquids.get(index).amount);
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
		if (this.liquids.isEmpty()) return this.mapping[side] = null;
		int index = this.liquids.indexOf(FluidRegistry.getFluidStack(this.mapping[side], 0)) + 1;
		if (index == this.liquids.size()) index = 0;
		return this.mapping[side] = FluidRegistry.getFluidName(this.liquids.get(index));
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection fd, FluidStack resource, boolean doDrain) {
		return drain(fd, resource == null ? 0 : resource.amount, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return false;
	}

	private FluidStack getFluidStack(ForgeDirection fd) {
		if (fd.ordinal() < 0 || fd.ordinal() >= this.mapping.length) return getFluidStack(ForgeDirection.UP);
		int index = this.liquids.indexOf(FluidRegistry.getFluidStack(this.mapping[fd.ordinal()], 0));
		if (index != -1) return this.liquids.get(index);
		return null;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection fd) {
		FluidStack fs = getFluidStack(fd);
		if (fs == null) return null;
		return new FluidTankInfo[] { new FluidTankInfo(fs, Integer.MAX_VALUE) };
	}

	@Override
	public FluidStack drain(ForgeDirection fd, int maxDrain, boolean doDrain) {
		FluidStack fs = getFluidStack(fd);
		if (fs == null) return null;
		FluidStack ret = fs.copy();
		ret.amount = Math.min(fs.amount, maxDrain);
		if (doDrain) fs.amount -= ret.amount;
		if (fs.amount <= 0) this.liquids.remove(fs);
		if (ret.amount <= 0) return null;
		return ret;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final boolean isLiquid(Block b) {
		return b == null ? false : (b instanceof IFluidBlock || b instanceof BlockFluid || b.blockMaterial.isLiquid());
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		TileBasic tb = G_connected();
		return tb == null ? null : tb.getPowerReceiver(side);
	}

	@Override
	public void doWork(PowerHandler workProvider) {}

	@Override
	public World getWorld() {
		return this.worldObj;
	}
}
