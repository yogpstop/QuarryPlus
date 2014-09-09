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

package com.yogpc.qp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.yogpc.mc_lib.PacketHandler;
import com.yogpc.mc_lib.YogpstopLib;
import com.yogpc.mc_lib.YogpstopPacket;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.IAreaProvider;

public class TileQuarry extends TileBasic {
	private int targetX, targetY, targetZ;
	public int xMin, xMax, yMin, yMax = Integer.MIN_VALUE, zMin, zMax;

	private IAreaProvider iap = null;

	private void S_updateEntity() {
		if (this.iap != null) {
			if (this.iap instanceof TileMarker) this.cacheItems.addAll(((TileMarker) this.iap).removeFromWorldWithItem());
			else this.iap.removeFromWorld();
			this.iap = null;
		}
		switch (this.now) {
		case MAKEFRAME:
			if (S_makeFrame()) while (!S_checkTarget())
				S_setNextTarget();
			break;
		case MOVEHEAD:
			boolean done = S_moveHead();
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				dos.writeDouble(this.headPosX);
				dos.writeDouble(this.headPosY);
				dos.writeDouble(this.headPosZ);
				PacketHandler.sendPacketToAround(new YogpstopPacket(bos.toByteArray(), this, PacketHandler.StC_HEAD_POS), this.worldObj.provider.dimensionId,
						this.xCoord, this.yCoord, this.zCoord);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!done) break;
			this.now = BREAKBLOCK;
        //$FALL-THROUGH$
      case NOTNEEDBREAK:
		case BREAKBLOCK:
			if (S_breakBlock()) while (!S_checkTarget())
				S_setNextTarget();
			break;
		}
		S_pollItems();
	}

	private boolean S_checkTarget() {
		if (this.targetY > this.yMax) this.targetY = this.yMax;
		Block b = this.worldObj.getChunkProvider().loadChunk(this.targetX >> 4, this.targetZ >> 4)
				.getBlock(this.targetX & 0xF, this.targetY, this.targetZ & 0xF);
		float h = b == null ? -1 : b.getBlockHardness(this.worldObj, this.targetX, this.targetY, this.targetZ);
		switch (this.now) {
		case BREAKBLOCK:
		case MOVEHEAD:
			if (this.targetY < 1) {
				G_destroy();
				PacketHandler.sendNowPacket(this, this.now);
				return true;
			}
			if (b == null || h < 0 || b.isAir(this.worldObj, this.targetX, this.targetY, this.targetZ)) return false;
			if (this.pump == ForgeDirection.UNKNOWN && TilePump.isLiquid(b, false, null, 0, 0, 0, 0)) return false;
			return true;
		case NOTNEEDBREAK:
			if (this.targetY < this.yMin) {
				this.now = MAKEFRAME;
				G_renew_powerConfigure();
				this.targetX = this.xMin;
				this.targetY = this.yMax;
				this.targetZ = this.zMin;
				this.addX = this.addZ = this.digged = true;
				this.changeZ = false;
				PacketHandler.sendNowPacket(this, this.now);
				return S_checkTarget();
			}
			if (b == null || h < 0 || b.isAir(this.worldObj, this.targetX, this.targetY, this.targetZ)) return false;
			if (this.pump == ForgeDirection.UNKNOWN && TilePump.isLiquid(b, false, null, 0, 0, 0, 0)) return false;
			if (b == QuarryPlus.blockFrame && this.worldObj.getBlockMetadata(this.targetX, this.targetY, this.targetZ) == 0) {
				byte flag = 0;
				if (this.targetX == this.xMin || this.targetX == this.xMax) flag++;
				if (this.targetY == this.yMin || this.targetY == this.yMax) flag++;
				if (this.targetZ == this.zMin || this.targetZ == this.zMax) flag++;
				if (flag > 1) return false;
			}
			return true;
		case MAKEFRAME:
			if (this.targetY < this.yMin) {
				this.now = MOVEHEAD;
				G_renew_powerConfigure();
				this.targetX = this.xMin + 1;
				this.targetY = this.yMin;
				this.targetZ = this.zMin + 1;
				this.addX = this.addZ = this.digged = true;
				this.changeZ = false;
				PacketHandler.sendNowPacket(this, this.now);
				return S_checkTarget();
			}
			if (b != null && b.getMaterial().isSolid()
					&& !(b == QuarryPlus.blockFrame && this.worldObj.getBlockMetadata(this.targetX, this.targetY, this.targetZ) == 0)) {
				this.now = NOTNEEDBREAK;
				G_renew_powerConfigure();
				this.targetX = this.xMin;
				this.targetZ = this.zMin;
				this.targetY = this.yMax;
				this.addX = this.addZ = this.digged = true;
				this.changeZ = false;
				PacketHandler.sendNowPacket(this, this.now);
				return S_checkTarget();
			}
			byte flag = 0;
			if (this.targetX == this.xMin || this.targetX == this.xMax) flag++;
			if (this.targetY == this.yMin || this.targetY == this.yMax) flag++;
			if (this.targetZ == this.zMin || this.targetZ == this.zMax) flag++;
			if (flag > 1) {
				if (b == QuarryPlus.blockFrame && this.worldObj.getBlockMetadata(this.targetX, this.targetY, this.targetZ) == 0) return false;
				return true;
			}
			return false;
		}
		return true;
	}

	private boolean addX = true;
	private boolean addZ = true;
	private boolean digged = true;
	private boolean changeZ = false;

	private void S_setNextTarget() {
		if (this.now == MAKEFRAME) {
			if (this.changeZ) {
				if (this.addZ) this.targetZ++;
				else this.targetZ--;
			} else {
				if (this.addX) this.targetX++;
				else this.targetX--;
			}
			if (this.targetX < this.xMin || this.xMax < this.targetX) {
				this.addX = !this.addX;
				this.changeZ = true;
				this.targetX = Math.max(this.xMin, Math.min(this.xMax, this.targetX));
			}
			if (this.targetZ < this.zMin || this.zMax < this.targetZ) {
				this.addZ = !this.addZ;
				this.changeZ = false;
				this.targetZ = Math.max(this.zMin, Math.min(this.zMax, this.targetZ));
			}
			if (this.xMin == this.targetX && this.zMin == this.targetZ) {
				if (this.digged) this.digged = false;
				else this.targetY--;
			}
		} else {
			if (this.addX) this.targetX++;
			else this.targetX--;
			int out = this.now == NOTNEEDBREAK ? 0 : 1;
			if (this.targetX < this.xMin + out || this.xMax - out < this.targetX) {
				this.addX = !this.addX;
				this.targetX = Math.max(this.xMin + out, Math.min(this.targetX, this.xMax - out));
				if (this.addZ) this.targetZ++;
				else this.targetZ--;
				if (this.targetZ < this.zMin + out || this.zMax - out < this.targetZ) {
					this.addZ = !this.addZ;
					this.targetZ = Math.max(this.zMin + out, Math.min(this.targetZ, this.zMax - out));
					if (this.digged) this.digged = false;
					else {
						this.targetY--;
						double aa = S_getDistance(this.xMin + 1, this.targetY, this.zMin + out);
						double ad = S_getDistance(this.xMin + 1, this.targetY, this.zMax - out);
						double da = S_getDistance(this.xMax - 1, this.targetY, this.zMin + out);
						double dd = S_getDistance(this.xMax - 1, this.targetY, this.zMax - out);
						double res = Math.min(aa, Math.min(ad, Math.min(da, dd)));
						if (res == aa) {
							this.addX = true;
							this.addZ = true;
							this.targetX = this.xMin + out;
							this.targetZ = this.zMin + out;
						} else if (res == ad) {
							this.addX = true;
							this.addZ = false;
							this.targetX = this.xMin + out;
							this.targetZ = this.zMax - out;
						} else if (res == da) {
							this.addX = false;
							this.addZ = true;
							this.targetX = this.xMax - out;
							this.targetZ = this.zMin + out;
						} else if (res == dd) {
							this.addX = false;
							this.addZ = false;
							this.targetX = this.xMax - out;
							this.targetZ = this.zMax - out;
						}
					}
				}
			}
		}
	}

	private double S_getDistance(int x, int y, int z) {
		return Math.sqrt(Math.pow(x - this.headPosX, 2) + Math.pow(y + 1 - this.headPosY, 2) + Math.pow(z - this.headPosZ, 2));
	}

	private boolean S_makeFrame() {
		this.digged = true;
		if (!PowerManager.useEnergyF(this, this.unbreaking)) return false;
		this.worldObj.setBlock(this.targetX, this.targetY, this.targetZ, QuarryPlus.blockFrame);
		S_setNextTarget();
		return true;
	}

	private boolean S_breakBlock() {
		this.digged = true;
		if (S_breakBlock(this.targetX, this.targetY, this.targetZ)) {
			S_checkDropItem();
			if (this.now == BREAKBLOCK) this.now = MOVEHEAD;
			S_setNextTarget();
			return true;
		}
		return false;
	}

	private void S_checkDropItem() {
		AxisAlignedBB axis = AxisAlignedBB.getBoundingBox(this.targetX - 4, this.targetY - 4, this.targetZ - 4, this.targetX + 6, this.targetY + 6,
				this.targetZ + 6);
		List<?> result = this.worldObj.getEntitiesWithinAABB(EntityItem.class, axis);
		for (int ii = 0; ii < result.size(); ii++) {
			if (result.get(ii) instanceof EntityItem) {
				EntityItem entity = (EntityItem) result.get(ii);
				if (entity.isDead) continue;
				ItemStack drop = entity.getEntityItem();
				if (drop.stackSize <= 0) continue;
				YogpstopLib.proxy.removeEntity(entity);
				this.cacheItems.add(drop);
			}
		}
	}

	private void S_createBox() {
		if (this.yMax != Integer.MIN_VALUE) return;
		if (!S_checkIAreaProvider(this.xCoord - 1, this.yCoord, this.zCoord)) if (!S_checkIAreaProvider(this.xCoord + 1, this.yCoord, this.zCoord)) if (!S_checkIAreaProvider(
				this.xCoord, this.yCoord, this.zCoord - 1)) if (!S_checkIAreaProvider(this.xCoord, this.yCoord, this.zCoord + 1)) if (!S_checkIAreaProvider(
				this.xCoord, this.yCoord - 1, this.zCoord)) if (!S_checkIAreaProvider(this.xCoord, this.yCoord + 1, this.zCoord)) {
			ForgeDirection o = ForgeDirection.values()[this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord)].getOpposite();
			switch (o) {
			case EAST:
				this.xMin = this.xCoord + 1;
				this.zMin = this.zCoord - 5;
				break;
			case WEST:
				this.xMin = this.xCoord - 11;
				this.zMin = this.zCoord - 5;
				break;
			case SOUTH:
				this.xMin = this.xCoord - 5;
				this.zMin = this.zCoord + 1;
				break;
			case NORTH:
			default:
				this.xMin = this.xCoord - 5;
				this.zMin = this.zCoord - 11;
				break;
			}
			this.yMin = this.yCoord;
			this.xMax = this.xMin + 10;
			this.zMax = this.zMin + 10;
			this.yMax = this.yCoord + 4;
		}
	}

	private boolean S_checkIAreaProvider(int x, int y, int z) {
		TileEntity te = this.worldObj.getTileEntity(x, y, z);
		if (te instanceof IAreaProvider) {
			this.iap = (IAreaProvider) te;
			this.xMin = this.iap.xMin();
			this.xMax = this.iap.xMax();
			this.yMin = this.iap.yMin();
			this.zMin = this.iap.zMin();
			this.zMax = this.iap.zMax();
			this.yMax = this.iap.yMax();
			int tmp;
			if (this.xMin > this.xMax) {
				tmp = this.xMin;
				this.xMin = this.xMax;
				this.xMax = tmp;
			}
			if (this.yMin > this.yMax) {
				tmp = this.yMin;
				this.yMin = this.yMax;
				this.yMax = tmp;
			}
			if (this.zMin > this.zMax) {
				tmp = this.zMin;
				this.zMin = this.zMax;
				this.zMax = tmp;
			}
			if (this.xCoord >= this.xMin && this.xCoord <= this.xMax && this.yCoord >= this.yMin && this.yCoord <= this.yMax && this.zCoord >= this.zMin
					&& this.zCoord <= this.zMax) {
				this.yMax = Integer.MIN_VALUE;
				return false;
			}
			if (this.xMax - this.xMin < 2 || this.zMax - this.zMin < 2) {
				this.yMax = Integer.MIN_VALUE;
				return false;
			}
			if (this.yMax - this.yMin < 2) this.yMax = this.yMin + 3;
			return true;
		}
		return false;
	}

	private void S_setFirstPos() {
		this.targetX = this.xMin;
		this.targetZ = this.zMin;
		this.targetY = this.yMax;
		this.headPosX = (this.xMin + this.xMax + 1) / 2;
		this.headPosZ = (this.zMin + this.zMax + 1) / 2;
		this.headPosY = this.yMax - 1;
	}

	private void S_destroyFrames() {
		if (this.yMax == Integer.MIN_VALUE) return;
		int xn = this.xMin;
		int xx = this.xMax;
		int yn = this.yMin;
		int yx = this.yMax;
		int zn = this.zMin;
		int zx = this.zMax;
		for (int x = xn; x <= xx; x++) {
			S_setBreakableFrame(x, yn, zn);
			S_setBreakableFrame(x, yn, zx);
			S_setBreakableFrame(x, yx, zn);
			S_setBreakableFrame(x, yx, zx);
		}
		for (int y = yn; y <= yx; y++) {
			S_setBreakableFrame(xn, y, zn);
			S_setBreakableFrame(xn, y, zx);
			S_setBreakableFrame(xx, y, zn);
			S_setBreakableFrame(xx, y, zx);
		}
		for (int z = zn; z <= zx; z++) {
			S_setBreakableFrame(xn, yn, z);
			S_setBreakableFrame(xn, yx, z);
			S_setBreakableFrame(xx, yn, z);
			S_setBreakableFrame(xx, yx, z);
		}
	}

	private void S_setBreakableFrame(int x, int y, int z) {
		if (this.worldObj.getBlock(x, y, z) == QuarryPlus.blockFrame) {
			this.worldObj.setBlockMetadataWithNotify(x, y, z, 1, 3);
		}
	}

	private boolean S_moveHead() {
		double x = this.targetX - this.headPosX;
		double y = this.targetY + 1 - this.headPosY;
		double z = this.targetZ - this.headPosZ;
		double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
		double blocks = PowerManager.useEnergyH(this, distance, this.unbreaking);
		if (blocks * 2 >= distance) {
			this.headPosX = this.targetX;
			this.headPosY = this.targetY + 1;
			this.headPosZ = this.targetZ;
			return true;
		}
		if (blocks > 0) {
			this.headPosX += x * blocks / distance;
			this.headPosY += y * blocks / distance;
			this.headPosZ += z * blocks / distance;
		}
		return false;
	}

	public byte G_getNow() {
		return this.now;
	}

	@Override
	protected void G_destroy() {
		this.now = NONE;
		G_renew_powerConfigure();
		if (!this.worldObj.isRemote) {
			S_destroyFrames();
			PacketHandler.sendNowPacket(this, this.now);
		}
		ForgeChunkManager.releaseTicket(this.chunkTicket);
	}

	@Override
	public void G_reinit() {
		if (this.yMax == Integer.MIN_VALUE && !this.worldObj.isRemote) S_createBox();
		this.now = NOTNEEDBREAK;
		G_renew_powerConfigure();
		if (!this.worldObj.isRemote) {
			S_setFirstPos();
			PacketHandler.sendPacketToAround(new YogpstopPacket(this), this.worldObj.provider.dimensionId, this.xCoord, this.yCoord, this.zCoord);
		}
	}

	private Ticket chunkTicket;

	void requestTicket() {
		if (this.chunkTicket != null) return;
		this.chunkTicket = ForgeChunkManager.requestTicket(QuarryPlus.instance, this.worldObj, Type.NORMAL);
		if (this.chunkTicket == null) return;
		NBTTagCompound tag = this.chunkTicket.getModData();
		tag.setInteger("quarryX", this.xCoord);
		tag.setInteger("quarryY", this.yCoord);
		tag.setInteger("quarryZ", this.zCoord);
		forceChunkLoading(this.chunkTicket);
	}

	void forceChunkLoading(Ticket ticket) {
		if (this.chunkTicket == null) this.chunkTicket = ticket;
		Set<ChunkCoordIntPair> chunks = Sets.newHashSet();
		ChunkCoordIntPair quarryChunk = new ChunkCoordIntPair(this.xCoord >> 4, this.zCoord >> 4);
		chunks.add(quarryChunk);
		ForgeChunkManager.forceChunk(ticket, quarryChunk);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!this.initialized) {
			G_renew_powerConfigure();
			this.initialized = true;
		}
		if (!this.worldObj.isRemote) S_updateEntity();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttc) {
		super.readFromNBT(nbttc);
		this.xMin = nbttc.getInteger("xMin");
		this.xMax = nbttc.getInteger("xMax");
		this.yMin = nbttc.getInteger("yMin");
		this.zMin = nbttc.getInteger("zMin");
		this.zMax = nbttc.getInteger("zMax");
		this.yMax = nbttc.getInteger("yMax");
		this.targetX = nbttc.getInteger("targetX");
		this.targetY = nbttc.getInteger("targetY");
		this.targetZ = nbttc.getInteger("targetZ");
		this.addZ = nbttc.getBoolean("addZ");
		this.addX = nbttc.getBoolean("addX");
		this.digged = nbttc.getBoolean("digged");
		this.changeZ = nbttc.getBoolean("changeZ");
		this.now = nbttc.getByte("now");
		this.headPosX = nbttc.getDouble("headPosX");
		this.headPosY = nbttc.getDouble("headPosY");
		this.headPosZ = nbttc.getDouble("headPosZ");
		this.initialized = false;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttc) {
		super.writeToNBT(nbttc);
		nbttc.setInteger("xMin", this.xMin);
		nbttc.setInteger("xMax", this.xMax);
		nbttc.setInteger("yMin", this.yMin);
		nbttc.setInteger("yMax", this.yMax);
		nbttc.setInteger("zMin", this.zMin);
		nbttc.setInteger("zMax", this.zMax);
		nbttc.setInteger("targetX", this.targetX);
		nbttc.setInteger("targetY", this.targetY);
		nbttc.setInteger("targetZ", this.targetZ);
		nbttc.setBoolean("addZ", this.addZ);
		nbttc.setBoolean("addX", this.addX);
		nbttc.setBoolean("digged", this.digged);
		nbttc.setBoolean("changeZ", this.changeZ);
		nbttc.setByte("now", this.now);
		nbttc.setDouble("headPosX", this.headPosX);
		nbttc.setDouble("headPosY", this.headPosY);
		nbttc.setDouble("headPosZ", this.headPosZ);
	}

	public static final byte NONE = 0;
	public static final byte NOTNEEDBREAK = 1;
	public static final byte MAKEFRAME = 2;
	public static final byte MOVEHEAD = 4;
	public static final byte BREAKBLOCK = 5;

	public double headPosX, headPosY, headPosZ;
	private boolean initialized = true;
	private byte now = NONE;

	@Override
	protected void C_recievePacket(byte id, byte[] data, EntityPlayer ep) {
		super.C_recievePacket(id, data, ep);
		switch (id) {
		case PacketHandler.StC_NOW:
			this.now = data[0];
			G_renew_powerConfigure();
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			break;
		case PacketHandler.StC_HEAD_POS:
			ByteArrayDataInput badi = ByteStreams.newDataInput(data);
			this.headPosX = badi.readDouble();
			this.headPosY = badi.readDouble();
			this.headPosZ = badi.readDouble();
			break;
		}
	}

	@Override
	protected void G_renew_powerConfigure() {
		TileEntity te = this.worldObj.getTileEntity(this.xCoord + this.pump.offsetX, this.yCoord + this.pump.offsetY, this.zCoord + this.pump.offsetZ);
		byte pmp = 0;
		if (te instanceof TilePump) pmp = ((TilePump) te).unbreaking;
		else this.pump = ForgeDirection.UNKNOWN;
		if (this.now == NONE) PowerManager.configure0(this);
		else if (this.now == MAKEFRAME) PowerManager.configureF(this, this.efficiency, this.unbreaking, pmp);
		else PowerManager.configureB(this, this.efficiency, this.unbreaking, pmp);
	}
}
