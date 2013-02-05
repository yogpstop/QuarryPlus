package org.yogpstop.qp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import static cpw.mods.fml.common.network.PacketDispatcher.sendPacketToAllPlayers;

import static buildcraft.BuildCraftFactory.frameBlock;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.LaserKind;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.core.Box;
import buildcraft.core.proxy.CoreProxy;
import static buildcraft.core.utils.Utils.addToRandomInventory;
import static buildcraft.core.utils.Utils.addToRandomPipeEntry;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class TileQuarry extends TileEntity implements IPowerReceptor,
		IPipeConnection {
	private final Box box = new Box();
	private final int[] target = new int[3];
	private final double[] headPos = new double[3];

	private EntityMechanicalArm heads;

	private byte fortune;
	private boolean silktouch;
	private byte efficiency;

	public boolean removeLava, removeWater, removeLiquid, buildAdvFrame;

	private ArrayList<ItemStack> cacheItems = new ArrayList<ItemStack>();
	private ArrayList<int[]> cacheFrame = new ArrayList<int[]>();
	private ArrayList<int[]> cacheNonNeeded = new ArrayList<int[]>();
	private ArrayList<int[]> cacheFills = new ArrayList<int[]>();

	public ArrayList<Integer> fortuneList = new ArrayList<Integer>();
	public ArrayList<Integer> silktouchList = new ArrayList<Integer>();

	private boolean initialized = true;

	private PROGRESS now = PROGRESS.NONE;

	private IPowerProvider pp;

	private enum PROGRESS {
		NONE((byte) 0), NOTNEEDBREAK((byte) 1), MAKEFRAME((byte) 2), FILL(
				(byte) 3), MOVEHEAD((byte) 4), BREAKBLOCK((byte) 5);
		PROGRESS(final byte arg) {
			byteValue = arg;
		}

		public byte getByteValue() {
			return byteValue;
		}

		public static PROGRESS valueOf(final byte arg) {
			for (PROGRESS d : values()) {
				if (d.getByteValue() == arg) {
					return d;
				}
			}
			return null;
		}

		private final byte byteValue;
	}

	@Override
	public Packet getDescriptionPacket() {
		return PacketHandler.getPacket(this);
	}

	private void initFromNBT() {
		initEntities();
		if (worldObj != null)
			if (!worldObj.isRemote)
				initBlocks();
		initialized = true;
	}

	private void initBlocks() {
		switch (now) {
		case NOTNEEDBREAK:
			initNonNeededBlocks();
		case MAKEFRAME:
			initFrames();
		case FILL:
			initFills();
		default:
		}
	}

	private void initEntities() {
		box.deleteLasers();
		switch (now) {
		case NOTNEEDBREAK:
		case MAKEFRAME:
			box.createLasers(worldObj, LaserKind.Stripes);
			break;
		case MOVEHEAD:
		case BREAKBLOCK:
			if (heads == null)
				worldObj.spawnEntityInWorld(new EntityMechanicalArm(worldObj,
						box.xMin + 0.75D, box.yMax, box.zMin + 0.75D, box
								.sizeX() - 1.5D, box.sizeZ() - 1.5D, this));
			break;
		default:
		}

		if (heads != null) {
			if (now != PROGRESS.BREAKBLOCK && now != PROGRESS.MOVEHEAD) {
				heads.setDead();
				heads = null;
			} else {
				heads.setHead(headPos[0], headPos[1], headPos[2]);
				heads.updatePosition();
			}
		}
	}

	public ArrayList<String> getEnchantments() {
		ArrayList<String> als = new ArrayList<String>();
		if (silktouch)
			als.add(Enchantment.enchantmentsList[33].getTranslatedName(1));
		if (fortune > 0)
			als.add(Enchantment.enchantmentsList[35].getTranslatedName(fortune));
		if (efficiency > 0)
			als.add(Enchantment.enchantmentsList[32]
					.getTranslatedName(efficiency));
		return als;
	}

	protected void init(NBTTagList nbttl) {
		if (nbttl != null)
			for (int i = 0; i < nbttl.tagCount(); i++) {
				short id = ((NBTTagCompound) nbttl.tagAt(i)).getShort("id");
				short lvl = ((NBTTagCompound) nbttl.tagAt(i)).getShort("lvl");
				if (id == 33)
					silktouch = true;
				if (id == 35)
					fortune = (byte) lvl;
				if (id == 32)
					efficiency = (byte) lvl;
			}
		createBox();
		requestTicket();
		initPowerProvider();
		reinit();
	}

	protected void reinit() {
		setFirstPos();
		now = PROGRESS.NOTNEEDBREAK;
		if (!worldObj.isRemote)
			initBlocks();
		initEntities();
	}

	private void initPowerProvider() {
		pp = PowerFramework.currentFramework.createPowerProvider();
		pp.configure(0, 0, 100, 0, 30000);
	}

	@Override
	public void updateEntity() {
		if (!initialized)
			initFromNBT();
		if (worldObj.isRemote)
			return;
		switch (now) {
		case NOTNEEDBREAK:
			if (cacheNonNeeded.size() > 0) {
				if (breakBlock(cacheNonNeeded.get(0)))
					cacheNonNeeded.remove(0);
				break;
			}
			now = PROGRESS.MAKEFRAME;
		case MAKEFRAME:
			if (cacheFrame.size() > 0) {
				if (makeFrame(cacheFrame.get(0)))
					cacheFrame.remove(0);
				break;
			}
			if (buildAdvFrame)
				now = PROGRESS.FILL;
			else {
				now = PROGRESS.MOVEHEAD;
				worldObj.spawnEntityInWorld(new EntityMechanicalArm(worldObj,
						box.xMin + 0.75D, box.yMax, box.zMin + 0.75D, box
								.sizeX() - 1.5D, box.sizeZ() - 1.5D, this));
				heads.setHead(headPos[0], headPos[1], headPos[2]);
				heads.updatePosition();
				sendPacketToAllPlayers(PacketHandler.getPacket(this));
				for (; !checkTarget();) {
					setNextTarget();
				}
			}
			box.deleteLasers();
			sendPacketToAllPlayers(PacketHandler.getPacket(this));
		case FILL:
			if (cacheFills.size() > 0) {
				if (makeFrame(cacheFills.get(0)))
					cacheFills.remove(0);
				break;
			}
			now = PROGRESS.MOVEHEAD;
			worldObj.spawnEntityInWorld(new EntityMechanicalArm(worldObj,
					box.xMin + 0.75D, box.yMax, box.zMin + 0.75D,
					box.sizeX() - 1.5D, box.sizeZ() - 1.5D, this));
			heads.setHead(headPos[0], headPos[1], headPos[2]);
			heads.updatePosition();
			sendPacketToAllPlayers(PacketHandler.getPacket(this));
			for (; !checkTarget();) {
				setNextTarget();
			}
			break;
		case MOVEHEAD:
			boolean done = moveHead();
			heads.setHead(headPos[0], headPos[1], headPos[2]);
			heads.updatePosition();
			sendPacketToAllPlayers(PacketHandler.getPacket(this));
			if (!done)
				break;
			now = PROGRESS.BREAKBLOCK;
		case BREAKBLOCK:
			if (breakBlock(target)) {
				now = PROGRESS.MOVEHEAD;
				for (; !checkTarget();) {
					setNextTarget();
				}
			}
			break;
		default:
			break;
		}
		ArrayList<ItemStack> cache = new ArrayList<ItemStack>();
		for (ItemStack is : cacheItems) {
			ItemStack added = addToRandomInventory(is, worldObj, xCoord,
					yCoord, zCoord, ForgeDirection.UNKNOWN);
			is.stackSize -= added.stackSize;
			if (is.stackSize > 0)
				if (!addToRandomPipeEntry(this, ForgeDirection.UNKNOWN, is))
					cache.add(is);
		}
		cacheItems = cache;
	}

	private boolean checkTarget() {
		if (now != PROGRESS.MOVEHEAD)
			return false;
		int bid = worldObj.getBlockId(target[0], target[1], target[2]);
		if (bid == 0 || bid == Block.bedrock.blockID)
			return false;
		if (!removeLava
				&& (bid == Block.lavaMoving.blockID || bid == Block.lavaStill.blockID))
			return false;
		if (!removeWater
				&& (bid == Block.waterMoving.blockID || bid == Block.waterStill.blockID))
			return false;
		if (!removeLiquid
				&& worldObj.getBlockMaterial(target[0], target[1], target[2])
						.isLiquid())
			return false;
		return true;
	}

	private void destroy() {
		box.deleteLasers();
		now = PROGRESS.NONE;
		if (heads != null) {
			heads.setDead();
			heads = null;
		}
		if (!worldObj.isRemote) {
			destroyFrames();
		}
	}

	@Override
	public void invalidate() {
		ForgeChunkManager.releaseTicket(chunkTicket);
		destroy();
		if (!worldObj.isRemote
				&& worldObj.getGameRules().getGameRuleBooleanValue(
						"doTileDrops")) {
			ItemStack is = new ItemStack(QuarryPlus.itemQuarry);
			setEnchantment(is);
			float var6 = 0.7F;
			double var7 = (double) (worldObj.rand.nextFloat() * var6)
					+ (double) (1.0F - var6) * 0.5D;
			double var9 = (double) (worldObj.rand.nextFloat() * var6)
					+ (double) (1.0F - var6) * 0.5D;
			double var11 = (double) (worldObj.rand.nextFloat() * var6)
					+ (double) (1.0F - var6) * 0.5D;
			EntityItem var13 = new EntityItem(worldObj, (double) xCoord + var7,
					(double) yCoord + var9, (double) zCoord + var11, is);
			var13.delayBeforeCanPickup = 10;
			worldObj.spawnEntityInWorld(var13);
		}
		super.invalidate();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttc) {
		super.readFromNBT(nbttc);
		box.initialize(nbttc);
		silktouch = nbttc.getBoolean("silktouch");
		fortune = nbttc.getByte("fortune");
		efficiency = nbttc.getByte("efficiency");
		target[0] = nbttc.getInteger("targetX");
		target[1] = nbttc.getInteger("targetY");
		target[2] = nbttc.getInteger("targetZ");
		headPos[0] = nbttc.getDouble("headPosX");
		headPos[1] = nbttc.getDouble("headPosY");
		headPos[2] = nbttc.getDouble("headPosZ");
		removeWater = nbttc.getBoolean("removeWater");
		removeLava = nbttc.getBoolean("removeLava");
		removeLiquid = nbttc.getBoolean("removeLiquid");
		buildAdvFrame = nbttc.getBoolean("buildAdvFrame");
		addZ = nbttc.getBoolean("addZ");
		addX = nbttc.getBoolean("addX");
		PowerFramework.currentFramework.loadPowerProvider(this, nbttc);
		now = PROGRESS.valueOf(nbttc.getByte("now"));
		initialized = false;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttc) {
		super.writeToNBT(nbttc);
		box.writeToNBT(nbttc);
		nbttc.setBoolean("silktouch", silktouch);
		nbttc.setByte("fortune", fortune);
		nbttc.setByte("efficiency", efficiency);
		nbttc.setInteger("targetX", target[0]);
		nbttc.setInteger("targetY", target[1]);
		nbttc.setInteger("targetZ", target[2]);
		nbttc.setDouble("headPosX", headPos[0]);
		nbttc.setDouble("headPosY", headPos[1]);
		nbttc.setDouble("headPosZ", headPos[2]);
		nbttc.setBoolean("removeWater", removeWater);
		nbttc.setBoolean("removeLava", removeLava);
		nbttc.setBoolean("removeLiquid", removeLiquid);
		nbttc.setBoolean("buildAdvFrame", buildAdvFrame);
		nbttc.setBoolean("addZ", addZ);
		nbttc.setBoolean("addX", addX);
		PowerFramework.currentFramework.savePowerProvider(this, nbttc);
		nbttc.setByte("now", now.getByteValue());
	}

	private void setEnchantment(ItemStack is) {
		if (silktouch)
			is.addEnchantment(Enchantment.enchantmentsList[33], 1);
		if (fortune > 0)
			is.addEnchantment(Enchantment.enchantmentsList[35], fortune);
		if (efficiency > 0)
			is.addEnchantment(Enchantment.enchantmentsList[32], efficiency);
	}

	private boolean makeFrame(int[] coord) {
		float y = Math.max(-4.8F * (float) efficiency + 25F, 0F);
		if (pp.useEnergy(y, y, true) != y)
			return false;
		worldObj.setBlockWithNotify(coord[0], coord[1], coord[2],
				frameBlock.blockID);
		return true;
	}

	private boolean addX = true;
	private boolean addZ = true;
	private Ticket chunkTicket;

	private void setNextTarget() {
		if (addX)
			target[0]++;
		else
			target[0]--;
		if (target[0] == box.xMax || target[0] == box.xMin) {
			addX = !addX;
			if (addX)
				target[0]++;
			else
				target[0]--;
			if (addZ)
				target[2]++;
			else
				target[2]--;
			if (target[2] == box.zMax || target[2] == box.zMin) {
				addZ = !addZ;
				if (addZ)
					target[2]++;
				else
					target[2]--;
				target[1]--;
			}
		}
		if (target[1] < 1) {
			destroy();
			sendPacketToAllPlayers(PacketHandler.getPacket(this));
			return;
		}
	}

	private void createBox() {
		if (!checkIAreaProvider(xCoord - 1, yCoord, zCoord))
			if (!checkIAreaProvider(xCoord + 1, yCoord, zCoord))
				if (!checkIAreaProvider(xCoord, yCoord, zCoord - 1))
					if (!checkIAreaProvider(xCoord, yCoord, zCoord + 1))
						if (!checkIAreaProvider(xCoord, yCoord - 1, zCoord))
							if (!checkIAreaProvider(xCoord, yCoord + 1, zCoord)) {
								int xMin = 0, zMin = 0;
								ForgeDirection o = ForgeDirection.values()[worldObj
										.getBlockMetadata(xCoord, yCoord,
												zCoord)].getOpposite();
								switch (o) {
								case EAST:
									xMin = xCoord + 1;
									zMin = zCoord - 5;
									break;
								case WEST:
									xMin = xCoord - 11;
									zMin = zCoord - 5;
									break;
								case SOUTH:
									xMin = xCoord - 5;
									zMin = zCoord + 1;
									break;
								case NORTH:
								default:
									xMin = xCoord - 5;
									zMin = zCoord - 11;
									break;
								}
								box.initialize(xMin, yCoord, zMin, xMin + 10,
										yCoord + 4, zMin + 10);
							}
	}

	private void setFirstPos() {
		target[0] = box.xMin + 1;
		target[2] = box.zMin + 1;
		target[1] = box.yMin;
		headPos[0] = box.centerX();
		headPos[2] = box.centerZ();
		headPos[1] = box.yMax - 1;
	}

	private boolean checkIAreaProvider(int x, int y, int z) {
		if (worldObj.getBlockTileEntity(x, y, z) instanceof IAreaProvider) {
			box.initialize(((IAreaProvider) worldObj
					.getBlockTileEntity(x, y, z)));
			box.reorder();
			if (box.contains(xCoord, yCoord, zCoord)) {
				box.reset();
				return false;
			}
			if (box.sizeX() < 3 || box.sizeZ() < 3) {
				box.reset();
				return false;
			}
			if (box.sizeY() <= 1)
				box.yMax += 3 - box.sizeY();
			((IAreaProvider) worldObj.getBlockTileEntity(x, y, z))
					.removeFromWorld();
			return true;
		}
		return false;
	}

	private void initFrames() {
		cacheFrame = new ArrayList<int[]>();
		int xn = box.xMin;
		int xx = box.xMax;
		int yn = box.yMin;
		int yx = box.yMax;
		int zn = box.zMin;
		int zx = box.zMax;
		for (int x = xn + 1; x <= xx - 1; x++) {
			checkAndAddFrame(new int[] { x, yn, zn });
			checkAndAddFrame(new int[] { x, yn, zx });
			checkAndAddFrame(new int[] { x, yx, zn });
			checkAndAddFrame(new int[] { x, yx, zx });
		}
		for (int y = yn + 1; y <= yx - 1; y++) {
			checkAndAddFrame(new int[] { xn, y, zn });
			checkAndAddFrame(new int[] { xn, y, zx });
			checkAndAddFrame(new int[] { xx, y, zn });
			checkAndAddFrame(new int[] { xx, y, zx });
		}
		for (int z = zn + 1; z <= zx - 1; z++) {
			checkAndAddFrame(new int[] { xn, yn, z });
			checkAndAddFrame(new int[] { xn, yx, z });
			checkAndAddFrame(new int[] { xx, yn, z });
			checkAndAddFrame(new int[] { xx, yx, z });
		}
		checkAndAddFrame(new int[] { xn, yn, zn });
		checkAndAddFrame(new int[] { xn, yn, zx });
		checkAndAddFrame(new int[] { xn, yx, zn });
		checkAndAddFrame(new int[] { xn, yx, zx });
		checkAndAddFrame(new int[] { xx, yn, zn });
		checkAndAddFrame(new int[] { xx, yn, zx });
		checkAndAddFrame(new int[] { xx, yx, zn });
		checkAndAddFrame(new int[] { xx, yx, zx });
	}

	private void checkAndAddFrame(int[] coord) {
		if (!(worldObj.getBlockId(coord[0], coord[1], coord[2]) == frameBlock.blockID && worldObj
				.getBlockMetadata(coord[0], coord[1], coord[2]) == 0))
			cacheFrame.add(coord);
	}

	private void initFills() {
		int xn = box.xMin;
		int xx = box.xMax;
		int yn = box.yMin;
		int zn = box.zMin;
		int zx = box.zMax;
		for (int y = yn; y > 0; y--) {
			for (int x = xn; x <= xx; x++) {
				checkAndAddFill(x, y, zn);
				checkAndAddFill(x, y, zx);
			}
			for (int z = zn + 1; z < zx; z++) {
				checkAndAddFill(xn, y, z);
				checkAndAddFill(xx, y, z);
			}
		}
	}

	private void checkAndAddFill(int x, int y, int z) {
		if (!worldObj.getBlockMaterial(x, y, z).isSolid()) {
			cacheFills.add(new int[] { x, y, z });
		}
	}

	private void destroyFrames() {
		int xn = box.xMin;
		int xx = box.xMax;
		int yn = box.yMin;
		int yx = box.yMax;
		int zn = box.zMin;
		int zx = box.zMax;
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

	private void initNonNeededBlocks() {
		cacheNonNeeded = new ArrayList<int[]>();
		for (int y = box.yMax; y >= box.yMin; y--) {
			for (int x = box.xMin; x <= box.xMax; x++) {
				for (int z = box.zMin; z <= box.zMax; z++) {
					int bid = worldObj.getBlockId(x, y, z);
					if (bid != 0 && bid != Block.bedrock.blockID)
						if (bid == frameBlock.blockID
								&& worldObj.getBlockMetadata(x, y, z) == 0) {
							byte flag = 0;
							if (x == box.xMin || x == box.xMax)
								flag++;
							if (y == box.yMin || y == box.yMax)
								flag++;
							if (z == box.zMin || z == box.zMax)
								flag++;
							if (flag < 2)
								cacheNonNeeded.add(new int[] { x, y, z });
						} else
							cacheNonNeeded.add(new int[] { x, y, z });
				}
			}
		}
	}

	private void setBreakableFrame(int x, int y, int z) {
		if (worldObj.getBlockId(x, y, z) == frameBlock.blockID) {
			worldObj.setBlockMetadata(x, y, z, 1);
		}
	}

	private boolean moveHead() {
		if (efficiency >= 4) {
			headPos[0] = target[0];
			headPos[1] = target[1] + 1;
			headPos[2] = target[2];
			return true;
		}
		double distance = getRestDistance();
		float pw = (float) Math.min(2 + pp.getEnergyStored() / 500,
				(distance - 0.1F) * 200F / (efficiency * 3 + 1));
		float used = pp.useEnergy(pw, pw, true);
		double blocks = used * (float) (efficiency * 3 + 1) / 200F + 0.1F;

		if (blocks * 2 > distance) {
			headPos[0] = target[0];
			headPos[1] = target[1] + 1;
			headPos[2] = target[2];
			return true;
		}
		if (used > 0) {
			headPos[0] += Math.cos(Math.atan2(target[2] - headPos[2], target[0]
					- headPos[0]))
					* blocks;
			headPos[1] += Math.sin(Math.atan2(target[1] + 1 - headPos[1],
					target[0] - headPos[0])) * blocks;
			headPos[2] += Math.sin(Math.atan2(target[2] - headPos[2], target[0]
					- headPos[0]))
					* blocks;
		}
		return false;
	}

	private double getRestDistance() {
		return Math.sqrt(Math.pow(target[0] - headPos[0], 2)
				+ Math.pow(target[1] + 1 - headPos[1], 2)
				+ Math.pow(target[2] - headPos[2], 2));
	}

	private boolean breakBlock(int[] coord) {
		float pw = Math.max(
				(-7.8F * (float) efficiency + 40F)
						* blockHardness(coord[0], coord[1], coord[2]), 0F);
		if (pp.useEnergy(pw, pw, true) != pw)
			return false;
		cacheItems.addAll(getDroppedItems(coord[0], coord[1], coord[2]));
		worldObj.playAuxSFXAtEntity(
				null,
				2001,
				coord[0],
				coord[1],
				coord[2],
				worldObj.getBlockId(coord[0], coord[1], coord[2])
						+ (worldObj.getBlockMetadata(coord[0], coord[1],
								coord[2]) << 12));
		worldObj.setBlockWithNotify(coord[0], coord[1], coord[2], 0);
		checkDropItem(coord);
		return true;
	}

	@SuppressWarnings("rawtypes")
	private void checkDropItem(int[] coord) {
		AxisAlignedBB axis = AxisAlignedBB.getBoundingBox(coord[0] - 4,
				coord[1] - 4, coord[2] - 4, coord[0] + 6, coord[1] + 6,
				coord[2] + 6);
		List result = worldObj.getEntitiesWithinAABB(EntityItem.class, axis);
		for (int ii = 0; ii < result.size(); ii++) {
			if (result.get(ii) instanceof EntityItem) {
				EntityItem entity = (EntityItem) result.get(ii);
				if (entity.isDead)
					continue;
				ItemStack drop = entity.getEntityItem();
				if (drop.stackSize <= 0)
					continue;
				CoreProxy.proxy.removeEntity(entity);
				cacheItems.add(drop);
			}
		}
	}

	private float blockHardness(int x, int y, int z) {
		Block b = Block.blocksList[worldObj.getBlockId(x, y, z)];
		if (b != null) {
			if (worldObj.getBlockMaterial(x, y, z).isLiquid())
				return 0;
			return b.getBlockHardness(worldObj, x, y, z);
		}
		return (float) 0;
	}

	private ArrayList<ItemStack> getDroppedItems(int x, int y, int z) {
		Block b = Block.blocksList[worldObj.getBlockId(x, y, z)];
		int meta = worldObj.getBlockMetadata(x, y, z);
		if (b == null)
			return new ArrayList<ItemStack>();
		if (b.canSilkHarvest(worldObj, null, x, y, z, meta)
				&& silktouch
				&& (silktouchList.contains(b.blockID) || silktouchList.size() == 0)) {
			ArrayList<ItemStack> al = new ArrayList<ItemStack>();
			al.add(new ItemStack(b, 1, meta));
			return al;
		}
		return b.getBlockDropped(worldObj, x, y, z, meta, ((fortuneList
				.contains(b.blockID) || fortuneList.size() == 0) ? fortune : 0));
	}

	@Override
	public void setPowerProvider(IPowerProvider provider) {
		pp = provider;

	}

	@Override
	public IPowerProvider getPowerProvider() {
		return pp;
	}

	@Override
	public void doWork() {
	}

	@Override
	public int powerRequest() {
		return (int) Math.ceil(Math.min(getPowerProvider()
				.getMaxEnergyReceived(), getPowerProvider()
				.getMaxEnergyStored() - getPowerProvider().getEnergyStored()));
	}

	protected void setArm(EntityMechanicalArm ema) {
		heads = ema;
	}

	protected void forceChunkLoading(Ticket ticket) {
		if (chunkTicket == null) {
			chunkTicket = ticket;
		}

		Set<ChunkCoordIntPair> chunks = Sets.newHashSet();
		ChunkCoordIntPair quarryChunk = new ChunkCoordIntPair(xCoord >> 4,
				zCoord >> 4);
		chunks.add(quarryChunk);
		ForgeChunkManager.forceChunk(ticket, quarryChunk);

		for (int chunkX = box.xMin >> 4; chunkX <= box.xMax >> 4; chunkX++) {
			for (int chunkZ = box.zMin >> 4; chunkZ <= box.zMax >> 4; chunkZ++) {
				ChunkCoordIntPair chunk = new ChunkCoordIntPair(chunkX, chunkZ);
				ForgeChunkManager.forceChunk(ticket, chunk);
				chunks.add(chunk);
			}
		}
		sendPacketToAllPlayers(PacketHandler.getPacket(this));
	}

	private void requestTicket() {
		if (chunkTicket != null)
			return;
		chunkTicket = ForgeChunkManager.requestTicket(QuarryPlus.instance,
				worldObj, Type.NORMAL);
		if (chunkTicket == null)
			return;
		chunkTicket.getModData().setInteger("quarryX", xCoord);
		chunkTicket.getModData().setInteger("quarryY", yCoord);
		chunkTicket.getModData().setInteger("quarryZ", zCoord);
		forceChunkLoading(chunkTicket);
	}

	@Override
	public boolean isPipeConnected(ForgeDirection with) {
		return true;
	}
}
