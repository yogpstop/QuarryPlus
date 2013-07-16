package org.yogpstop.qp;

import static org.yogpstop.qp.PacketHandler.*;

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
import static buildcraft.core.utils.Utils.addToRandomPipeEntry;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkCoordIntPair;

import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.ForgeDirection;

public class TileQuarry extends TileBasic {
	private int targetX, targetY, targetZ;

	static double CE_BB;
	static double BP_BB;
	static double CE_MF;
	static double BP_MF;
	static double CE_MH;
	static double BP_MH;
	static double CF;
	static double CS;

	private void S_updateEntity() {
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
				sendHeadPosPacket(this, this.headPosX, this.headPosY, this.headPosZ);
			}
			if (!done) break;
			this.now = BREAKBLOCK;
		case NOTNEEDBREAK:
		case BREAKBLOCK:
			if (S_breakBlock()) while (!S_checkTarget())
				S_setNextTarget();
			break;
		}
		List<ItemStack> cache = new LinkedList<ItemStack>();
		for (ItemStack is : this.cacheItems) {
			ItemStack added = S_addToRandomInventory(is);
			is.stackSize -= added.stackSize;
			if (is.stackSize > 0) if (!addToRandomPipeEntry(this, ForgeDirection.UNKNOWN, is)) cache.add(is);
		}
		this.cacheItems = cache;
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
				this.targetX = this.box.xMin;
				this.targetY = this.box.yMax;
				this.targetZ = this.box.zMin;
				this.addX = this.addZ = this.digged = true;
				this.changeZ = false;
				sendNowPacket(this, this.now);
				return S_checkTarget();
			}
			if (bid == 0 || bid == Block.bedrock.blockID) return false;
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
			if (this.worldObj.getBlockMaterial(this.targetX, this.targetY, this.targetZ).isSolid()
					&& (bid != frameBlock.blockID || this.worldObj.getBlockMetadata(this.targetX, this.targetY, this.targetZ) != 0)) {
				this.now = NOTNEEDBREAK;
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
				S_setNextTarget();
			}
			if (this.targetZ < this.box.zMin || this.box.zMax < this.targetZ) {
				this.addZ = !this.addZ;
				this.changeZ = false;
				this.targetZ = Math.max(this.box.zMin, Math.min(this.box.zMax, this.targetZ));
				S_setNextTarget();
			}
			if (this.box.xMin == this.targetX && this.box.zMin == this.targetZ) {
				if (this.digged) this.digged = false;
				else this.targetY--;
			}
		} else {
			if (this.addX) this.targetX++;
			else this.targetX--;
			if (this.targetX < this.box.xMin + (this.now == NOTNEEDBREAK ? 0 : 1) || this.box.xMax - (this.now == NOTNEEDBREAK ? 0 : 1) < this.targetX) {
				this.addX = !this.addX;
				this.targetX = Math.max(this.box.xMin + (this.now == NOTNEEDBREAK ? 0 : 1),
						Math.min(this.targetX, this.box.xMax - (this.now == NOTNEEDBREAK ? 0 : 1)));
				if (this.addZ) this.targetZ++;
				else this.targetZ--;
				if (this.targetZ < this.box.zMin + (this.now == NOTNEEDBREAK ? 0 : 1) || this.box.zMax - (this.now == NOTNEEDBREAK ? 0 : 1) < this.targetZ) {
					this.addZ = !this.addZ;
					this.targetZ = Math.max(this.box.zMin + (this.now == NOTNEEDBREAK ? 0 : 1),
							Math.min(this.targetZ, this.box.zMax - (this.now == NOTNEEDBREAK ? 0 : 1)));
					if (this.digged) this.digged = false;
					else {
						this.targetY--;
						double aa = S_getDistance(this.box.xMin + 1, this.targetY, this.box.zMin + (this.now == NOTNEEDBREAK ? 0 : 1));
						double ad = S_getDistance(this.box.xMin + 1, this.targetY, this.box.zMax - (this.now == NOTNEEDBREAK ? 0 : 1));
						double da = S_getDistance(this.box.xMax - 1, this.targetY, this.box.zMin + (this.now == NOTNEEDBREAK ? 0 : 1));
						double dd = S_getDistance(this.box.xMax - 1, this.targetY, this.box.zMax - (this.now == NOTNEEDBREAK ? 0 : 1));
						double res = Math.min(aa, Math.min(ad, Math.min(da, dd)));
						if (res == aa) {
							this.addX = true;
							this.addZ = true;
							this.targetX = this.box.xMin + (this.now == NOTNEEDBREAK ? 0 : 1);
							this.targetZ = this.box.zMin + (this.now == NOTNEEDBREAK ? 0 : 1);
						} else if (res == ad) {
							this.addX = true;
							this.addZ = false;
							this.targetX = this.box.xMin + (this.now == NOTNEEDBREAK ? 0 : 1);
							this.targetZ = this.box.zMax - (this.now == NOTNEEDBREAK ? 0 : 1);
						} else if (res == da) {
							this.addX = false;
							this.addZ = true;
							this.targetX = this.box.xMax - (this.now == NOTNEEDBREAK ? 0 : 1);
							this.targetZ = this.box.zMin + (this.now == NOTNEEDBREAK ? 0 : 1);
						} else if (res == dd) {
							this.addX = false;
							this.addZ = false;
							this.targetX = this.box.xMax - (this.now == NOTNEEDBREAK ? 0 : 1);
							this.targetZ = this.box.zMax - (this.now == NOTNEEDBREAK ? 0 : 1);
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
		float power = (float) Math.max(BP_MF / Math.pow(CE_MF, this.efficiency), 0D);
		if (this.pp.useEnergy(power, power, true) != power) return false;
		this.worldObj.setBlock(this.targetX, this.targetY, this.targetZ, frameBlock.blockID);
		S_setNextTarget();
		return true;
	}

	private boolean S_breakBlock() {
		this.digged = true;
		if (S_breakBlock(this.targetX, this.targetY, this.targetZ, BP_BB, CE_BB, CS, CF)) {
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
			if (te instanceof TileMarker) this.cacheItems.addAll(((TileMarker) te).removeFromWorldWithItem());
			else ((IAreaProvider) te).removeFromWorld();
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
		float pw = (float) Math.max(
				Math.min(2D + Math.pow(CE_MH, this.efficiency) * this.pp.getEnergyStored() / 500D,
						((distance - 0.1D) * BP_MH / Math.pow(CE_MH, this.efficiency))), 0D);
		float used = this.pp.useEnergy(pw, pw, true);
		double blocks = used * Math.pow(CE_MH, this.efficiency) / BP_MH + 0.1D;

		if (blocks * 2 > distance) {
			this.headPosX = this.targetX;
			this.headPosY = this.targetY + 1;
			this.headPosZ = this.targetZ;
			return true;
		}
		if (used > 0) {
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
		this.box.deleteLasers();
		this.now = NONE;
		sendNowPacket(this, this.now);
		if (this.heads != null) {
			this.heads.setDead();
			this.heads = null;
		}
		if (!this.worldObj.isRemote) {
			S_destroyFrames();
		}
		ForgeChunkManager.releaseTicket(this.chunkTicket);
	}

	@Override
	protected void G_reinit() {
		this.now = NOTNEEDBREAK;
		if (!this.worldObj.isRemote) {
			S_setFirstPos();
		}
		G_initEntities();
		PacketDispatcher.sendPacketToAllPlayers(PacketHandler.getPacketFromNBT(this));
		sendNowPacket(this, this.now);
	}

	@Override
	void G_init(NBTTagList nbttl) {
		S_createBox();
		requestTicket();
		super.G_init(nbttl);
	}

	private Ticket chunkTicket;

	private void requestTicket() {
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
		if (this.chunkTicket == null) {
			this.chunkTicket = ticket;
		}

		Set<ChunkCoordIntPair> chunks = Sets.newHashSet();
		ChunkCoordIntPair quarryChunk = new ChunkCoordIntPair(this.xCoord >> 4, this.zCoord >> 4);
		chunks.add(quarryChunk);
		ForgeChunkManager.forceChunk(ticket, quarryChunk);

		for (int chunkX = this.box.xMin >> 4; chunkX <= this.box.xMax >> 4; chunkX++) {
			for (int chunkZ = this.box.zMin >> 4; chunkZ <= this.box.zMax >> 4; chunkZ++) {
				ChunkCoordIntPair chunk = new ChunkCoordIntPair(chunkX, chunkZ);
				ForgeChunkManager.forceChunk(ticket, chunk);
				chunks.add(chunk);
			}
		}
		PacketDispatcher.sendPacketToAllPlayers(PacketHandler.getPacketFromNBT(this));
	}

	void setArm(EntityMechanicalArm ema) {
		this.heads = ema;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!this.initialized) {
			G_initEntities();
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
	static final byte WAITLIQUID = 3;
	static final byte MOVEHEAD = 4;
	static final byte BREAKBLOCK = 5;

	private double headPosX, headPosY, headPosZ;
	private final Box box = new Box();
	private EntityMechanicalArm heads;
	private boolean initialized = true;
	private byte now = NONE;

	@Override
	protected void C_recievePacket(byte pattern, ByteArrayDataInput data) {
		super.C_recievePacket(pattern, data);
		switch (pattern) {
		case packetNow:
			this.now = data.readByte();
			this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
			G_initEntities();
			break;
		case packetHeadPos:
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
	public void onChunkUnload() {
		this.box.deleteLasers();
		if (this.heads != null) {
			this.heads.setDead();
			this.heads = null;
		}
	}
}
