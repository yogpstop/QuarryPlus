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

import static org.yogpstop.qp.PacketHandler.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.LaserKind;
import static buildcraft.BuildCraftFactory.frameBlock;
import buildcraft.core.Box;
import buildcraft.core.proxy.CoreProxy;
import static buildcraft.core.utils.Utils.addToRandomPipeAround;
import static buildcraft.core.utils.Utils.addToRandomInventoryAround;
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
import net.minecraftforge.common.ForgeDirection;

public class TileQuarry extends TileBasic {
	private int targetX, targetY, targetZ;

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
			if (this.heads != null) {
				this.heads.setHead(this.headPosX, this.headPosY, this.headPosZ);
				this.heads.updatePosition();
				try {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(bos);
					dos.writeInt(this.xCoord);
					dos.writeInt(this.yCoord);
					dos.writeInt(this.zCoord);
					dos.writeByte(StC_HEAD_POS);
					dos.writeDouble(this.headPosX);
					dos.writeDouble(this.headPosY);
					dos.writeDouble(this.headPosZ);
					PacketDispatcher.sendPacketToAllAround(this.xCoord, this.yCoord, this.zCoord, 256, this.worldObj.provider.dimensionId,
							composeTilePacket(bos));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (!done) break;
			this.now = BREAKBLOCK;
		case NOTNEEDBREAK:
		case BREAKBLOCK:
			if (S_breakBlock()) while (!S_checkTarget())
				S_setNextTarget();
			break;
		}
		List<ItemStack> todelete = new LinkedList<ItemStack>();
		for (ItemStack is : this.cacheItems) {
			int added = addToRandomInventoryAround(this.worldObj, this.xCoord, this.yCoord, this.zCoord, is);
			is.stackSize -= added;
			if (is.stackSize > 0) {
				added = addToRandomPipeAround(this.worldObj, this.xCoord, this.yCoord, this.zCoord, ForgeDirection.UNKNOWN, is);
				is.stackSize -= added;
			}
			if (is.stackSize <= 0) todelete.add(is);
		}
		this.cacheItems.removeAll(todelete);
	}

	private boolean S_checkTarget() {
		if (this.targetY > this.box.yMax) this.targetY = this.box.yMax;
		int bid = this.worldObj.getBlockId(this.targetX, this.targetY, this.targetZ);
		switch (this.now) {
		case BREAKBLOCK:
		case MOVEHEAD:
			if (this.targetY < 1) {
				G_destroy();
				sendNowPacket(this, this.now);
				return true;
			}
			if (bid == 0 || bid == Block.bedrock.blockID) return false;
			if (this.pump == ForgeDirection.UNKNOWN && this.worldObj.getBlockMaterial(this.targetX, this.targetY, this.targetZ).isLiquid()) return false;
			return true;
		case NOTNEEDBREAK:
			if (this.targetY < this.box.yMin) {
				this.now = MAKEFRAME;
				G_renew_powerConfigure();
				this.targetX = this.box.xMin;
				this.targetY = this.box.yMax;
				this.targetZ = this.box.zMin;
				this.addX = this.addZ = this.digged = true;
				this.changeZ = false;
				sendNowPacket(this, this.now);
				return S_checkTarget();
			}
			if (bid == 0 || bid == Block.bedrock.blockID) return false;
			if (this.pump == ForgeDirection.UNKNOWN && this.worldObj.getBlockMaterial(this.targetX, this.targetY, this.targetZ).isLiquid()) return false;
			if (bid == frameBlock.blockID && this.worldObj.getBlockMetadata(this.targetX, this.targetY, this.targetZ) == 0) {
				byte flag = 0;
				if (this.targetX == this.box.xMin || this.targetX == this.box.xMax) flag++;
				if (this.targetY == this.box.yMin || this.targetY == this.box.yMax) flag++;
				if (this.targetZ == this.box.zMin || this.targetZ == this.box.zMax) flag++;
				if (flag > 1) return false;
			}
			return true;
		case MAKEFRAME:
			if (this.targetY < this.box.yMin) {
				this.now = MOVEHEAD;
				G_renew_powerConfigure();
				this.targetX = this.box.xMin + 1;
				this.targetY = this.box.yMin;
				this.targetZ = this.box.zMin + 1;
				this.addX = this.addZ = this.digged = true;
				this.changeZ = false;
				this.worldObj.spawnEntityInWorld(new EntityMechanicalArm(this.worldObj, this.box.xMin + 0.75D, this.box.yMax, this.box.zMin + 0.75D, this.box
						.sizeX() - 1.5D, this.box.sizeZ() - 1.5D, this));
				this.heads.setHead(this.headPosX, this.headPosY, this.headPosZ);
				this.heads.updatePosition();
				sendNowPacket(this, this.now);
				return S_checkTarget();
			}
			if (bid == Block.bedrock.blockID) return false;
			if (this.worldObj.getBlockMaterial(this.targetX, this.targetY, this.targetZ).isSolid()
					&& (bid != frameBlock.blockID || this.worldObj.getBlockMetadata(this.targetX, this.targetY, this.targetZ) != 0)) {
				this.now = NOTNEEDBREAK;
				G_renew_powerConfigure();
				this.targetX = this.box.xMin;
				this.targetZ = this.box.zMin;
				this.targetY = this.box.yMax;
				this.addX = this.addZ = this.digged = true;
				this.changeZ = false;
				sendNowPacket(this, this.now);
				return S_checkTarget();
			}
			byte flag = 0;
			if (this.targetX == this.box.xMin || this.targetX == this.box.xMax) flag++;
			if (this.targetY == this.box.yMin || this.targetY == this.box.yMax) flag++;
			if (this.targetZ == this.box.zMin || this.targetZ == this.box.zMax) flag++;
			if (flag > 1) {
				if (bid == frameBlock.blockID && this.worldObj.getBlockMetadata(this.targetX, this.targetY, this.targetZ) == 0) return false;
				return true;
			}
			return false;
		}
		System.err.println("yogpstop: Unknown status");
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
			if (this.targetX < this.box.xMin || this.box.xMax < this.targetX) {
				this.addX = !this.addX;
				this.changeZ = true;
				this.targetX = Math.max(this.box.xMin, Math.min(this.box.xMax, this.targetX));
			}
			if (this.targetZ < this.box.zMin || this.box.zMax < this.targetZ) {
				this.addZ = !this.addZ;
				this.changeZ = false;
				this.targetZ = Math.max(this.box.zMin, Math.min(this.box.zMax, this.targetZ));
			}
			if (this.box.xMin == this.targetX && this.box.zMin == this.targetZ) {
				if (this.digged) this.digged = false;
				else this.targetY--;
			}
		} else {
			if (this.addX) this.targetX++;
			else this.targetX--;
			int out = this.now == NOTNEEDBREAK ? 0 : 1;
			if (this.targetX < this.box.xMin + out || this.box.xMax - out < this.targetX) {
				this.addX = !this.addX;
				this.targetX = Math.max(this.box.xMin + out, Math.min(this.targetX, this.box.xMax - out));
				if (this.addZ) this.targetZ++;
				else this.targetZ--;
				if (this.targetZ < this.box.zMin + out || this.box.zMax - out < this.targetZ) {
					this.addZ = !this.addZ;
					this.targetZ = Math.max(this.box.zMin + out, Math.min(this.targetZ, this.box.zMax - out));
					if (this.digged) this.digged = false;
					else {
						this.targetY--;
						double aa = S_getDistance(this.box.xMin + 1, this.targetY, this.box.zMin + out);
						double ad = S_getDistance(this.box.xMin + 1, this.targetY, this.box.zMax - out);
						double da = S_getDistance(this.box.xMax - 1, this.targetY, this.box.zMin + out);
						double dd = S_getDistance(this.box.xMax - 1, this.targetY, this.box.zMax - out);
						double res = Math.min(aa, Math.min(ad, Math.min(da, dd)));
						if (res == aa) {
							this.addX = true;
							this.addZ = true;
							this.targetX = this.box.xMin + out;
							this.targetZ = this.box.zMin + out;
						} else if (res == ad) {
							this.addX = true;
							this.addZ = false;
							this.targetX = this.box.xMin + out;
							this.targetZ = this.box.zMax - out;
						} else if (res == da) {
							this.addX = false;
							this.addZ = true;
							this.targetX = this.box.xMax - out;
							this.targetZ = this.box.zMin + out;
						} else if (res == dd) {
							this.addX = false;
							this.addZ = false;
							this.targetX = this.box.xMax - out;
							this.targetZ = this.box.zMax - out;
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
		if (!PowerManager.useEnergyF(this.pp, this.unbreaking)) return false;
		this.worldObj.setBlock(this.targetX, this.targetY, this.targetZ, frameBlock.blockID);
		S_setNextTarget();
		return true;
	}

	private boolean S_breakBlock() {
		this.digged = true;
		if (S_breakBlock(this.targetX, this.targetY, this.targetZ, PowerManager.BreakType.Quarry)) {
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
				CoreProxy.proxy.removeEntity(entity);
				this.cacheItems.add(drop);
			}
		}
	}

	private void S_createBox() {
		if (this.box.isInitialized()) return;
		if (!S_checkIAreaProvider(this.xCoord - 1, this.yCoord, this.zCoord)) if (!S_checkIAreaProvider(this.xCoord + 1, this.yCoord, this.zCoord)) if (!S_checkIAreaProvider(
				this.xCoord, this.yCoord, this.zCoord - 1)) if (!S_checkIAreaProvider(this.xCoord, this.yCoord, this.zCoord + 1)) if (!S_checkIAreaProvider(
				this.xCoord, this.yCoord - 1, this.zCoord)) if (!S_checkIAreaProvider(this.xCoord, this.yCoord + 1, this.zCoord)) {
			int xMin = 0, zMin = 0;
			ForgeDirection o = ForgeDirection.values()[this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord)].getOpposite();
			switch (o) {
			case EAST:
				xMin = this.xCoord + 1;
				zMin = this.zCoord - 5;
				break;
			case WEST:
				xMin = this.xCoord - 11;
				zMin = this.zCoord - 5;
				break;
			case SOUTH:
				xMin = this.xCoord - 5;
				zMin = this.zCoord + 1;
				break;
			case NORTH:
			default:
				xMin = this.xCoord - 5;
				zMin = this.zCoord - 11;
				break;
			}
			this.box.initialize(xMin, this.yCoord, zMin, xMin + 10, this.yCoord + 4, zMin + 10);
		}
	}

	private boolean S_checkIAreaProvider(int x, int y, int z) {
		TileEntity te = this.worldObj.getBlockTileEntity(x, y, z);
		if (te instanceof IAreaProvider) {
			this.box.initialize(((IAreaProvider) te));
			this.box.reorder();
			if (this.box.contains(this.xCoord, this.yCoord, this.zCoord)) {
				this.box.reset();
				return false;
			}
			if (this.box.sizeX() < 3 || this.box.sizeZ() < 3) {
				this.box.reset();
				return false;
			}
			if (this.box.sizeY() <= 1) this.box.yMax = this.box.yMin + 3;
			this.iap = (IAreaProvider) te;
			return true;
		}
		return false;
	}

	private void S_setFirstPos() {
		this.targetX = this.box.xMin;
		this.targetZ = this.box.zMin;
		this.targetY = this.box.yMax;
		this.headPosX = this.box.centerX();
		this.headPosZ = this.box.centerZ();
		this.headPosY = this.box.yMax - 1;
	}

	private void S_destroyFrames() {
		if (!this.box.isInitialized()) return;
		int xn = this.box.xMin;
		int xx = this.box.xMax;
		int yn = this.box.yMin;
		int yx = this.box.yMax;
		int zn = this.box.zMin;
		int zx = this.box.zMax;
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
		if (this.worldObj.getBlockId(x, y, z) == frameBlock.blockID) {
			this.worldObj.setBlockMetadataWithNotify(x, y, z, 1, 3);
		}
	}

	private boolean S_moveHead() {
		double x = this.targetX - this.headPosX;
		double y = this.targetY + 1 - this.headPosY;
		double z = this.targetZ - this.headPosZ;
		double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
		double blocks = PowerManager.useEnergyH(this.pp, distance, this.unbreaking);

		if (blocks * 2 > distance) {
			this.headPosX = this.targetX;
			this.headPosY = this.targetY + 1;
			this.headPosZ = this.targetZ;
			return true;
		}
		if (blocks > 0.1) {
			this.headPosX += x * blocks / distance;
			this.headPosY += y * blocks / distance;
			this.headPosZ += z * blocks / distance;
		}
		return false;
	}

	byte G_getNow() {
		return this.now;
	}

	@Override
	protected void G_destroy() {
		this.now = NONE;
		G_renew_powerConfigure();
		if (this.heads != null) {
			this.heads.setDead();
			this.heads = null;
		}
		this.box.deleteLasers();
		if (!this.worldObj.isRemote) {
			S_destroyFrames();
			sendNowPacket(this, this.now);
		}
		ForgeChunkManager.releaseTicket(this.chunkTicket);
	}

	@Override
	public void G_reinit() {
		if (!this.box.isInitialized() && !this.worldObj.isRemote) S_createBox();
		this.now = NOTNEEDBREAK;
		G_renew_powerConfigure();
		G_initEntities();
		if (!this.worldObj.isRemote) {
			S_setFirstPos();
			PacketDispatcher.sendPacketToAllAround(this.xCoord, this.yCoord, this.zCoord, 256, this.worldObj.provider.dimensionId,
					PacketHandler.getPacketFromNBT(this));
			sendNowPacket(this, this.now);
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

	void setArm(EntityMechanicalArm ema) {
		this.heads = ema;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!this.initialized) {
			G_initEntities();
			G_renew_powerConfigure();
			this.initialized = true;
		}
		if (!this.worldObj.isRemote) S_updateEntity();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttc) {
		super.readFromNBT(nbttc);
		this.box.initialize(nbttc);
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
		this.box.writeToNBT(nbttc);
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

	static final byte NONE = 0;
	static final byte NOTNEEDBREAK = 1;
	static final byte MAKEFRAME = 2;
	static final byte MOVEHEAD = 4;
	static final byte BREAKBLOCK = 5;

	private double headPosX, headPosY, headPosZ;
	final Box box = new Box();
	private EntityMechanicalArm heads;
	private boolean initialized = true;
	private byte now = NONE;

	@Override
	protected void C_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		super.C_recievePacket(pattern, data, ep);
		switch (pattern) {
		case StC_NOW:
			this.now = data.readByte();
			G_renew_powerConfigure();
			this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
			G_initEntities();
			break;
		case StC_HEAD_POS:
			this.headPosX = data.readDouble();
			this.headPosY = data.readDouble();
			this.headPosZ = data.readDouble();
			if (this.heads != null) this.heads.setHead(this.headPosX, this.headPosY, this.headPosZ);
			break;
		}
	}

	private void G_initEntities() {
		this.box.deleteLasers();
		switch (this.now) {
		case NOTNEEDBREAK:
		case MAKEFRAME:
			this.box.createLasers(this.worldObj, LaserKind.Stripes);
			break;
		case MOVEHEAD:
		case BREAKBLOCK:
			if (this.heads == null) this.worldObj.spawnEntityInWorld(new EntityMechanicalArm(this.worldObj, this.box.xMin + 0.75D, this.box.yMax,
					this.box.zMin + 0.75D, this.box.sizeX() - 1.5D, this.box.sizeZ() - 1.5D, this));
			break;
		}

		if (this.heads != null) {
			if (this.now != BREAKBLOCK && this.now != MOVEHEAD) {
				this.heads.setDead();
				this.heads = null;
			} else {
				this.heads.setHead(this.headPosX, this.headPosY, this.headPosZ);
				this.heads.updatePosition();
			}
		}
	}

	@Override
	public boolean isActive() {
		return G_getNow() != NONE;
	}

	@Override
	protected void G_renew_powerConfigure() {
		TileEntity te = this.worldObj.getBlockTileEntity(this.xCoord + this.pump.offsetX, this.yCoord + this.pump.offsetY, this.zCoord + this.pump.offsetZ);
		byte pmp = 0;
		if (te instanceof TilePump) pmp = ((TilePump) te).unbreaking;
		else this.pump = ForgeDirection.UNKNOWN;
		if (this.now == NONE) PowerManager.configure0(this.pp);
		else if (this.now == MAKEFRAME) PowerManager.configureF(this.pp, this.efficiency, this.unbreaking, pmp);
		else PowerManager.configureB(this.pp, this.efficiency, this.unbreaking, pmp);
	}
}
