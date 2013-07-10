package org.yogpstop.qp;

import static org.yogpstop.qp.QuarryPlus.data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkCoordIntPair;

import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.ForgeDirection;

public class TileQuarry extends TileBasic {

	public boolean buildAdvFrame, removeLava, removeWater, removeLiquid;

	private double headPosX, headPosY, headPosZ;
	private int targetX, targetY, targetZ;

	private final Box box = new Box();
	private EntityMechanicalArm heads;

	private boolean initialized = true;

	private byte now = NONE;

	public static double CE_BB;
	public static double BP_BB;
	public static double CE_MF;
	public static double BP_MF;
	public static double CE_MH;
	public static double BP_MH;
	public static double CF;
	public static double CS;

	public static final byte NONE = 0;
	public static final byte NOTNEEDBREAK = 1;
	public static final byte MAKEFRAME = 2;
	public static final byte FILL = 3;
	public static final byte MOVEHEAD = 4;
	public static final byte BREAKBLOCK = 5;

	public static final byte packetNow = 5;
	public static final byte packetHeadPos = 6;
	public static final byte tBuildAdvFrame = 13;
	public static final byte tRemoveWater = 10;
	public static final byte tRemoveLava = 11;
	public static final byte tRemoveLiquid = 12;

	public byte getNow() {
		return this.now;
	}

	private void sendNowPacket() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(this.xCoord);
			dos.writeInt(this.yCoord);
			dos.writeInt(this.zCoord);
			dos.writeByte(packetNow);
			dos.writeByte(this.now);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "QuarryPlusTB";
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		packet.isChunkDataPacket = true;

		PacketDispatcher.sendPacketToAllPlayers(packet);
	}

	private void sendHeadPosPacket() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(this.xCoord);
			dos.writeInt(this.yCoord);
			dos.writeInt(this.zCoord);
			dos.writeByte(packetHeadPos);
			dos.writeDouble(this.headPosX);
			dos.writeDouble(this.headPosY);
			dos.writeDouble(this.headPosZ);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "QuarryPlusTB";
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		packet.isChunkDataPacket = true;

		PacketDispatcher.sendPacketToAllPlayers(packet);
	}

	@Override
	protected void recievePacketOnServer(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		super.recievePacketOnServer(pattern, data, ep);
		switch (pattern) {
		case tBuildAdvFrame:
			this.buildAdvFrame = !this.buildAdvFrame;
			sendPacketToPlayer(ep, tBuildAdvFrame, this.buildAdvFrame);
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdContainerMiner, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		case tRemoveWater:
			this.removeWater = !this.removeWater;
			sendPacketToPlayer(ep, tRemoveWater, this.removeWater);
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdContainerMiner, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		case tRemoveLava:
			this.removeLava = !this.removeLava;
			sendPacketToPlayer(ep, tRemoveLava, this.removeLava);
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdContainerMiner, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		case tRemoveLiquid:
			this.removeLiquid = !this.removeLiquid;
			sendPacketToPlayer(ep, tRemoveLiquid, this.removeLiquid);
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdContainerMiner, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		}
	}

	@Override
	protected void recievePacketOnClient(byte pattern, ByteArrayDataInput data) {
		super.recievePacketOnClient(pattern, data);
		switch (pattern) {
		case packetNow:
			this.now = data.readByte();
			initEntities();
			break;
		case packetHeadPos:
			this.headPosX = data.readDouble();
			this.headPosY = data.readDouble();
			this.headPosZ = data.readDouble();
			if (this.heads != null) this.heads.setHead(this.headPosX, this.headPosY, this.headPosZ);
			break;
		case tBuildAdvFrame:
			this.buildAdvFrame = data.readBoolean();
			break;
		case tRemoveWater:
			this.removeWater = data.readBoolean();
			break;
		case tRemoveLava:
			this.removeLava = data.readBoolean();
			break;
		case tRemoveLiquid:
			this.removeLiquid = data.readBoolean();
			break;
		}
	}

	private void updateServerEntity() {
		switch (this.now) {
		case NOTNEEDBREAK:
			if (breakBlock()) while (!checkTarget())
				setNextTarget();
			break;
		case MAKEFRAME:
		case FILL:
			if (makeFrame()) while (!checkTarget())
				setNextTarget();
			break;
		case MOVEHEAD:
			boolean done = moveHead();
			if (this.heads != null) {
				this.heads.setHead(this.headPosX, this.headPosY, this.headPosZ);
				this.heads.updatePosition();
				sendHeadPosPacket();
			}
			if (!done) break;
			this.now = BREAKBLOCK;
		case BREAKBLOCK:
			if (breakBlock()) {
				this.now = MOVEHEAD;
				while (!checkTarget()) {
					setNextTarget();
				}
			}
			break;
		}
		ArrayList<ItemStack> cache = new ArrayList<ItemStack>();
		for (ItemStack is : this.cacheItems) {
			ItemStack added = addToRandomInventory(is);
			is.stackSize -= added.stackSize;
			if (is.stackSize > 0) if (!addToRandomPipeEntry(this, ForgeDirection.UNKNOWN, is)) cache.add(is);
		}
		this.cacheItems = cache;
	}

	private boolean checkTarget() {
		int bid = this.worldObj.getBlockId(this.targetX, this.targetY, this.targetZ);
		switch (this.now) {
		case BREAKBLOCK:
		case MOVEHEAD:
			if (this.targetY < 1) {
				destroy();
				sendNowPacket();
				return true;
			}
			if (bid == 0 || bid == Block.bedrock.blockID) return false;
			if (!this.removeLava && (bid == Block.lavaMoving.blockID || bid == Block.lavaStill.blockID)) return false;
			if (!this.removeWater && (bid == Block.waterMoving.blockID || bid == Block.waterStill.blockID)) return false;
			if (!this.removeLiquid && this.worldObj.getBlockMaterial(this.targetX, this.targetY, this.targetZ).isLiquid()) return false;
			return true;
		case NOTNEEDBREAK:
			if (this.targetY < this.box.yMin) {
				this.now = MAKEFRAME;
				this.targetX = this.box.xMin;
				this.targetY = this.box.yMax;
				this.targetZ = this.box.zMin;
				this.addX = this.addZ = true;
				this.digged = this.changeZ = false;
				return true;
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
				this.now = FILL;
				this.targetX = this.box.xMin;
				this.targetY = this.box.yMin;
				this.targetZ = this.box.zMin;
				this.addX = this.addZ = true;
				this.digged = this.changeZ = false;
				this.box.deleteLasers();
				sendNowPacket();
				return true;
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
		case FILL:
			if (!this.buildAdvFrame || this.targetY < 1) {
				this.now = MOVEHEAD;
				this.targetX = this.box.xMin + 1;
				this.targetY = this.box.yMin;
				this.targetZ = this.box.zMin + 1;
				this.addX = this.addZ = true;
				this.digged = this.changeZ = false;
				this.worldObj.spawnEntityInWorld(new EntityMechanicalArm(this.worldObj, this.box.xMin + 0.75D, this.box.yMax, this.box.zMin + 0.75D, this.box
						.sizeX() - 1.5D, this.box.sizeZ() - 1.5D, this));
				this.heads.setHead(this.headPosX, this.headPosY, this.headPosZ);
				this.heads.updatePosition();
				sendNowPacket();
				return true;
			}
			if (this.worldObj.getBlockMaterial(this.targetX, this.targetY, this.targetZ).isSolid()) return false;
			return true;
		}
		System.out.println("yogpstop: Unknown status");
		return true;
	}

	private boolean addX = true;
	private boolean addZ = true;
	private boolean digged = false;
	private boolean changeZ = false;

	private void setNextTarget() {
		if (this.now == MAKEFRAME || this.now == FILL) {
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
			System.out.println(String.format("%d %d %d", this.targetX, this.targetY, this.targetZ));
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
						double aa = getDistance(this.box.xMin + 1, this.targetY, this.box.zMin + (this.now == NOTNEEDBREAK ? 0 : 1));
						double ad = getDistance(this.box.xMin + 1, this.targetY, this.box.zMax - (this.now == NOTNEEDBREAK ? 0 : 1));
						double da = getDistance(this.box.xMax - 1, this.targetY, this.box.zMin + (this.now == NOTNEEDBREAK ? 0 : 1));
						double dd = getDistance(this.box.xMax - 1, this.targetY, this.box.zMax - (this.now == NOTNEEDBREAK ? 0 : 1));
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

	private double getDistance(int x, int y, int z) {
		return Math.sqrt(Math.pow(x - this.headPosX, 2) + Math.pow(y + 1 - this.headPosY, 2) + Math.pow(z - this.headPosZ, 2));
	}

	private boolean makeFrame() {
		this.digged = true;
		float power = (float) Math.max(BP_MF / Math.pow(CE_MF, this.efficiency), 0D);
		if (this.pp.useEnergy(power, power, true) != power) return false;
		this.worldObj.setBlock(this.targetX, this.targetY, this.targetZ, frameBlock.blockID);

		return true;
	}

	private boolean breakBlock() {
		this.digged = true;
		ArrayList<ItemStack> dropped = new ArrayList<ItemStack>();
		float pw = (float) Math.max(BP_BB * blockHardness() * addDroppedItems(dropped) / Math.pow(CE_BB, this.efficiency), 0D);
		if (this.pp.useEnergy(pw, pw, true) != pw) return false;
		this.cacheItems.addAll(dropped);
		this.worldObj.playAuxSFXAtEntity(null, 2001, this.targetX, this.targetY, this.targetZ,
				this.worldObj.getBlockId(this.targetX, this.targetY, this.targetZ)
						+ (this.worldObj.getBlockMetadata(this.targetX, this.targetY, this.targetZ) << 12));
		this.worldObj.setBlockToAir(this.targetX, this.targetY, this.targetZ);
		checkDropItem();
		return true;
	}

	private float blockHardness() {
		Block b = Block.blocksList[this.worldObj.getBlockId(this.targetX, this.targetY, this.targetZ)];
		if (b != null) {
			if (this.worldObj.getBlockMaterial(this.targetX, this.targetY, this.targetZ).isLiquid()) return 0;
			return b.getBlockHardness(this.worldObj, this.targetX, this.targetY, this.targetZ);
		}
		return 0;
	}

	private double addDroppedItems(ArrayList<ItemStack> list) {
		Block b = Block.blocksList[this.worldObj.getBlockId(this.targetX, this.targetY, this.targetZ)];
		int meta = this.worldObj.getBlockMetadata(this.targetX, this.targetY, this.targetZ);
		if (b == null) return 1;
		if (b.canSilkHarvest(this.worldObj, null, this.targetX, this.targetY, this.targetZ, meta) && this.silktouch
				&& (this.silktouchList.contains(data((short) b.blockID, meta)) == this.silktouchInclude)) {
			try {
				list.add(createStackedBlock(b, meta));
				return CS;
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error e) {
				e.printStackTrace();
			}
		}
		if (this.fortuneList.contains(data((short) b.blockID, meta)) == this.fortuneInclude) {
			list.addAll(b.getBlockDropped(this.worldObj, this.targetX, this.targetY, this.targetZ, meta, this.fortune));
			return Math.pow(CF, this.fortune);
		}
		list.addAll(b.getBlockDropped(this.worldObj, this.targetX, this.targetY, this.targetZ, meta, 0));
		return 1;
	}

	private void checkDropItem() {
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

	private void createBox() {
		if (!checkIAreaProvider(this.xCoord - 1, this.yCoord, this.zCoord)) if (!checkIAreaProvider(this.xCoord + 1, this.yCoord, this.zCoord)) if (!checkIAreaProvider(
				this.xCoord, this.yCoord, this.zCoord - 1)) if (!checkIAreaProvider(this.xCoord, this.yCoord, this.zCoord + 1)) if (!checkIAreaProvider(
				this.xCoord, this.yCoord - 1, this.zCoord)) if (!checkIAreaProvider(this.xCoord, this.yCoord + 1, this.zCoord)) {
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

	private boolean checkIAreaProvider(int x, int y, int z) {
		if (this.worldObj.getBlockTileEntity(x, y, z) instanceof IAreaProvider) {
			this.box.initialize(((IAreaProvider) this.worldObj.getBlockTileEntity(x, y, z)));
			this.box.reorder();
			if (this.box.contains(this.xCoord, this.yCoord, this.zCoord)) {
				this.box.reset();
				return false;
			}
			if (this.box.sizeX() < 3 || this.box.sizeZ() < 3) {
				this.box.reset();
				return false;
			}
			if (this.box.sizeY() <= 1) this.box.yMax += 3 - this.box.sizeY();
			((IAreaProvider) this.worldObj.getBlockTileEntity(x, y, z)).removeFromWorld();
			return true;
		}
		return false;
	}

	private void setFirstPos() {
		this.targetX = this.box.xMin;
		this.targetZ = this.box.zMin;
		this.targetY = this.box.yMax;
		this.headPosX = this.box.centerX();
		this.headPosZ = this.box.centerZ();
		this.headPosY = this.box.yMax - 1;
	}

	private void destroyFrames() {
		int xn = this.box.xMin;
		int xx = this.box.xMax;
		int yn = this.box.yMin;
		int yx = this.box.yMax;
		int zn = this.box.zMin;
		int zx = this.box.zMax;
		for (int x = xn; x <= xx; x++) {
			setBreakableFrame(x, yn, zn);
			setBreakableFrame(x, yn, zx);
			setBreakableFrame(x, yx, zn);
			setBreakableFrame(x, yx, zx);
		}
		for (int y = yn; y <= yx; y++) {
			setBreakableFrame(xn, y, zn);
			setBreakableFrame(xn, y, zx);
			setBreakableFrame(xx, y, zn);
			setBreakableFrame(xx, y, zx);
		}
		for (int z = zn; z <= zx; z++) {
			setBreakableFrame(xn, yn, z);
			setBreakableFrame(xn, yx, z);
			setBreakableFrame(xx, yn, z);
			setBreakableFrame(xx, yx, z);
		}
	}

	private void setBreakableFrame(int x, int y, int z) {
		if (this.worldObj.getBlockId(x, y, z) == frameBlock.blockID) {
			this.worldObj.setBlockMetadataWithNotify(x, y, z, 1, 3);
		}
	}

	private boolean moveHead() {
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

	private Ticket chunkTicket;

	private void requestTicket() {
		if (this.chunkTicket != null) return;
		this.chunkTicket = ForgeChunkManager.requestTicket(QuarryPlus.instance, this.worldObj, Type.NORMAL);
		if (this.chunkTicket == null) return;
		this.chunkTicket.getModData().setInteger("quarryX", this.xCoord);
		this.chunkTicket.getModData().setInteger("quarryY", this.yCoord);
		this.chunkTicket.getModData().setInteger("quarryZ", this.zCoord);
		forceChunkLoading(this.chunkTicket);
	}

	private void initEntities() {
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

	private void destroy() {
		this.box.deleteLasers();
		this.now = NONE;
		if (this.heads != null) {
			this.heads.setDead();
			this.heads = null;
		}
		if (!this.worldObj.isRemote) {
			destroyFrames();
		}
	}

	@Override
	protected void reinit() {
		this.now = NOTNEEDBREAK;
		if (!this.worldObj.isRemote) {
			setFirstPos();
		}
		initEntities();
		PacketDispatcher.sendPacketToAllPlayers(PacketHandler.getPacketFromNBT(this));
	}

	@Override
	void init(NBTTagList nbttl) {
		createBox();
		requestTicket();
		super.init(nbttl);
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
		if (!this.initialized) {
			initEntities();
			this.initialized = true;
		}
		if (!this.worldObj.isRemote) updateServerEntity();
	}

	@Override
	public void invalidate() {
		destroy();
		if (!this.worldObj.isRemote) {
			ForgeChunkManager.releaseTicket(this.chunkTicket);
		}
		super.invalidate();
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
		this.buildAdvFrame = nbttc.getBoolean("buildAdvFrame");
		this.removeWater = nbttc.getBoolean("removeWater");
		this.removeLava = nbttc.getBoolean("removeLava");
		this.removeLiquid = nbttc.getBoolean("removeLiquid");
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
		nbttc.setBoolean("buildAdvFrame", this.buildAdvFrame);
		nbttc.setBoolean("removeWater", this.removeWater);
		nbttc.setBoolean("removeLava", this.removeLava);
		nbttc.setBoolean("removeLiquid", this.removeLiquid);
	}
}
