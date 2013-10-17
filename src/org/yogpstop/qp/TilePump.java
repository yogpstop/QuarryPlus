/*
 * Copyright (C) 2012,2013 yogpstop
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the
 * GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.yogpstop.qp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Collection;
import java.util.LinkedList;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import static buildcraft.BuildCraftFactory.frameBlock;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.core.Box;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
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

public class TilePump extends APacketTile implements IFluidHandler, IPowerReceptor, IEnchantableTile {
	private ForgeDirection connectTo = ForgeDirection.UNKNOWN;
	private boolean initialized = false;

	private byte prev = (byte) ForgeDirection.UNKNOWN.ordinal();

	protected byte unbreaking;
	protected byte fortune;
	protected boolean silktouch;

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
		this.unbreaking = nbttc.getByte("unbreaking");
		this.connectTo = ForgeDirection.values()[nbttc.getByte("connectTo")];
		if (nbttc.getTag("mapping0") instanceof NBTTagList) for (int i = 0; i < this.mapping.length; i++)
			readStringCollection(nbttc.getTagList(String.format("mapping%d", i)), this.mapping[i]);
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

	private static void readStringCollection(NBTTagList nbttl, Collection<String> target) {
		target.clear();
		for (int i = 0; i < nbttl.tagCount(); i++)
			target.add(((NBTTagString) nbttl.tagAt(i)).data);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttc) {
		super.writeToNBT(nbttc);
		nbttc.setBoolean("silktouch", this.silktouch);
		nbttc.setByte("fortune", this.fortune);
		nbttc.setByte("unbreaking", this.unbreaking);
		nbttc.setByte("connectTo", (byte) this.connectTo.ordinal());
		for (int i = 0; i < this.mapping.length; i++)
			nbttc.setTag(String.format("mapping%d", i), writeStringCollection(this.mapping[i]));
		nbttc.setByte("range", this.range);
		nbttc.setBoolean("quarryRange", this.quarryRange);
		if (this.silktouch) {
			NBTTagList nbttl = new NBTTagList();
			for (FluidStack l : this.liquids)
				nbttl.appendTag(l.writeToNBT(new NBTTagCompound()));
			nbttc.setTag("liquids", nbttl);
		}
	}

	private static NBTTagList writeStringCollection(Collection<String> target) {
		NBTTagList nbttl = new NBTTagList();
		for (String l : target)
			nbttl.appendTag(new NBTTagString("", l));
		return nbttl;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		int pX, pY, pZ;
		TileEntity te;
		FluidStack fs;
		for (ForgeDirection fd : ForgeDirection.VALID_DIRECTIONS) {
			te = this.worldObj.getBlockTileEntity(this.xCoord + fd.offsetX, this.yCoord + fd.offsetY, this.zCoord + fd.offsetZ);
			if (te instanceof IFluidHandler) {
				for (String s : this.mapping[fd.ordinal()]) {
					pZ = this.liquids.indexOf(FluidRegistry.getFluidStack(s, 0));
					if (pZ == -1) continue;
					fs = this.liquids.get(pZ);
					fs.amount -= ((IFluidHandler) te).fill(fd.getOpposite(), fs, true);
					break;
				}
			}
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

	@Override
	public void G_reinit() {
		if (this.worldObj.isRemote) return;
		TileEntity te;
		for (ForgeDirection fd : ForgeDirection.VALID_DIRECTIONS) {
			te = this.worldObj.getBlockTileEntity(this.xCoord + fd.offsetX, this.yCoord + fd.offsetY, this.zCoord + fd.offsetZ);
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
	void S_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		byte target;
		int pos;
		String buf;
		switch (pattern) {
		case PacketHandler.CtS_ADD_MAPPING:// BLjava.lang.String;
			target = data.readByte();
			this.mapping[target].add(data.readUTF());
			S_OpenGUI(target, ep);
			break;
		case PacketHandler.CtS_REMOVE_MAPPING:// BLjava.lang.String;
			target = data.readByte();
			this.mapping[target].remove(data.readUTF());
			S_OpenGUI(target, ep);
			break;
		case PacketHandler.CtS_UP_MAPPING:// BLjava.lang.String;
			target = data.readByte();
			pos = this.mapping[target].indexOf(data.readUTF());
			if (pos > 0) {
				buf = this.mapping[target].get(pos);
				this.mapping[target].remove(pos);
				this.mapping[target].add(pos - 1, buf);
			}
			S_OpenGUI(target, ep);
			break;
		case PacketHandler.CtS_DOWN_MAPPING:// BLjava.lang.String;
			target = data.readByte();
			pos = this.mapping[target].indexOf(data.readUTF());
			if (pos >= 0 && pos + 1 < this.mapping[target].size()) {
				buf = this.mapping[target].get(pos);
				this.mapping[target].remove(pos);
				this.mapping[target].add(pos + 1, buf);
			}
			S_OpenGUI(target, ep);
			break;
		case PacketHandler.CtS_TOP_MAPPING:// BLjava.lang.String;
			target = data.readByte();
			pos = this.mapping[target].indexOf(data.readUTF());
			if (pos >= 0) {
				buf = this.mapping[target].get(pos);
				this.mapping[target].remove(pos);
				this.mapping[target].addFirst(buf);
			}
			S_OpenGUI(target, ep);
			break;
		case PacketHandler.CtS_BOTTOM_MAPPING:// BLjava.lang.String;
			target = data.readByte();
			pos = this.mapping[target].indexOf(data.readUTF());
			if (pos >= 0) {
				buf = this.mapping[target].get(pos);
				this.mapping[target].remove(pos);
				this.mapping[target].addLast(buf);
			}
			S_OpenGUI(target, ep);
			break;
		case PacketHandler.CtS_RENEW_DIRECTION:
			S_OpenGUI(data.readByte(), ep);
			break;
		case PacketHandler.CtS_COPY_MAPPING:
			byte from = data.readByte();
			target = data.readByte();
			this.mapping[target].clear();
			this.mapping[target].addAll(this.mapping[from]);
			S_OpenGUI(target, ep);
			break;
		}
	}

	@Override
	void C_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		switch (pattern) {
		case PacketHandler.StC_NOW:// B
			byte flag = data.readByte();
			if ((flag & 0x80) != 0) this.cy = this.currentHeight = -1;
			else this.currentHeight = Integer.MIN_VALUE;
			this.connectTo = ForgeDirection.getOrientation(flag & 0x7F);
			this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
			break;
		case PacketHandler.StC_OPENGUI_MAPPING:// BI[Ljava.lang.String;
			byte target = data.readByte();
			int len = data.readInt();
			this.mapping[target].clear();
			for (int i = 0; i < len; i++)
				this.mapping[target].add(data.readUTF());
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdPump + target, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		}
	}

	void S_OpenGUI(int d, EntityPlayer ep) {// BI[Ljava.lang.String;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeInt(this.xCoord);
			dos.writeInt(this.yCoord);
			dos.writeInt(this.zCoord);
			dos.writeByte(PacketHandler.StC_OPENGUI_MAPPING);
			dos.writeByte(d);
			dos.writeInt(this.mapping[d].size());
			for (String s : this.mapping[d])
				dos.writeUTF(s);
			PacketDispatcher.sendPacketToPlayer(PacketHandler.composeTilePacket(bos), (Player) ep);
		} catch (Exception e) {
			e.printStackTrace();
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
		if (this.range >= (this.fortune + 1) * 2) {
			if (G_connected() instanceof TileQuarry) this.quarryRange = true;
			this.range = 0;
		} else if (this.quarryRange) {
			this.quarryRange = false;
		} else this.range++;
		PacketDispatcher.sendPacketToPlayer(
				new Packet3Chat(ChatMessageComponent.createFromText(StatCollector.translateToLocalFormatted("chat.pump_rtoggle", this.quarryRange ? "quarry"
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
		if (PowerManager.useEnergyP(tbpp, this.unbreaking, block_count, frame_count)) {
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
									this.currentHeight, bz + this.zOffset, frameBlock.blockID);
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
	public final LinkedList<String>[] mapping = new LinkedList[ForgeDirection.VALID_DIRECTIONS.length];

	{
		for (int i = 0; i < this.mapping.length; i++)
			this.mapping[i] = new LinkedList<String>();
	}

	public String[] C_getNames() {
		String[] ret = new String[this.liquids.size() + 1];
		if (this.liquids.size() > 0) {
			ret[0] = StatCollector.translateToLocal("chat.pumpcontain");
			for (int i = 0; i < this.liquids.size(); i++) {
				ret[i + 1] = new StringBuilder().append("    ").append(this.liquids.get(i).getFluid().getLocalizedName()).append(": ")
						.append(this.liquids.get(i).amount).append("mB").toString();
			}
		} else {
			ret[0] = StatCollector.translateToLocal("chat.pumpcontainno");
		}
		return ret;
	}

	public static String fdToString(ForgeDirection fd) {
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

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection fd, FluidStack resource, boolean doDrain) {
		if (resource == null) return null;
		int index = this.liquids.indexOf(resource);
		if (index == -1) return null;
		FluidStack fs = this.liquids.get(index);
		if (fs == null) return null;
		FluidStack ret = fs.copy();
		ret.amount = Math.min(fs.amount, resource.amount);
		if (doDrain) fs.amount -= ret.amount;
		if (fs.amount <= 0) this.liquids.remove(fs);
		if (ret.amount <= 0) return null;
		return ret;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection fd) {
		if (fd.ordinal() < 0 || fd.ordinal() >= this.mapping.length) return getTankInfo(ForgeDirection.UP);
		LinkedList<FluidTankInfo> ret = new LinkedList<FluidTankInfo>();
		if (this.mapping[fd.ordinal()].size() <= 0) {
			if (this.liquids.size() <= 0) {
				for (Integer i : FluidRegistry.getRegisteredFluidIDs().values())
					ret.add(new FluidTankInfo(new FluidStack(i, 0), Integer.MAX_VALUE));
			} else {
				for (FluidStack fs : this.liquids)
					ret.add(new FluidTankInfo(fs, Integer.MAX_VALUE));
			}
		} else {
			int index;
			FluidStack fs;
			for (String s : this.mapping[fd.ordinal()]) {
				fs = FluidRegistry.getFluidStack(s, 0);
				if (fs == null) continue;
				index = this.liquids.indexOf(fs);
				if (index != -1) ret.add(new FluidTankInfo(this.liquids.get(index), Integer.MAX_VALUE));
				else ret.add(new FluidTankInfo(fs, Integer.MAX_VALUE));
			}
		}
		return ret.toArray(new FluidTankInfo[ret.size()]);
	}

	@Override
	public FluidStack drain(ForgeDirection fd, int maxDrain, boolean doDrain) {
		if (fd.ordinal() < 0 || fd.ordinal() >= this.mapping.length) return drain(ForgeDirection.UP, maxDrain, doDrain);
		if (this.mapping[fd.ordinal()].size() <= 0) {
			if (this.liquids.size() <= 0) return null;
			return drain(fd, this.liquids.getFirst(), doDrain);
		}
		int index;
		FluidStack fs;
		for (String s : this.mapping[fd.ordinal()]) {
			fs = FluidRegistry.getFluidStack(s, maxDrain);
			if (fs == null) continue;
			index = this.liquids.indexOf(fs);
			if (index == -1) continue;
			return drain(fd, this.liquids.get(index), doDrain);
		}
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final boolean isLiquid(Block b) {
		return b == null ? false
				: (b instanceof IFluidBlock || b == Block.waterStill || b == Block.waterMoving || b == Block.lavaStill || b == Block.lavaMoving);
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

	@Override
	public byte getEfficiency() {
		return 0;
	}

	@Override
	public byte getFortune() {
		return this.fortune;
	}

	@Override
	public byte getUnbreaking() {
		return this.unbreaking;
	}

	@Override
	public boolean getSilktouch() {
		return this.silktouch;
	}

	@Override
	public void set(byte pefficiency, byte pfortune, byte punbreaking, boolean psilktouch) {
		this.fortune = pfortune;
		this.unbreaking = punbreaking;
		this.silktouch = psilktouch;
	}
}
