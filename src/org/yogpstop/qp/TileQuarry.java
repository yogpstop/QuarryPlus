package org.yogpstop.qp;

import java.util.ArrayList;

import buildcraft.BuildCraftFactory;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.core.Box;
import buildcraft.core.utils.Utils;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TileQuarry extends TileEntity implements IInventory,
		IPowerReceptor {
	private final Box box = new Box();
	private final int[] target = new int[3];
	private final ItemStack[] inv = new ItemStack[getSizeInventory()];

	private byte fortune;
	private boolean silktouch;
	private byte efficiency;

	private ArrayList<ItemStack> cacheItems = new ArrayList<ItemStack>();
	private ArrayList<int[]> cacheFrame = new ArrayList<int[]>();
	private ArrayList<int[]> cacheNonNeeded = new ArrayList<int[]>();

	private boolean initialized = true;

	private PROGRESS now = PROGRESS.NONE;

	private IPowerProvider pp;

	enum PROGRESS {
		NONE((byte) 0), NOTNEEDBREAK((byte) 1), MAKEFRAME((byte) 2), MOVEHEAD(
				(byte) 3), BREAKBLOCK((byte) 4);
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

	public void init() {
		pp = PowerFramework.currentFramework.createPowerProvider();
		pp.configure(0, 0, 100, 0, 30000);
		createBox();
		initFrames();
		initNonNeededBlocks();
		now = PROGRESS.NOTNEEDBREAK;
	}

	public void initFromNBT() {
		switch (now) {
		case NOTNEEDBREAK:
			initNonNeededBlocks();
		case MAKEFRAME:
			initFrames();
			break;
		default:
		}
		initialized = true;
	}

	@Override
	public void updateEntity() {
		if (worldObj.isRemote)
			return;
		if (!initialized)
			initFromNBT();
		switch (now) {
		case NOTNEEDBREAK:
			if (cacheNonNeeded.size() > 0) {
				if (breakBlock(cacheNonNeeded.get(0)))
					cacheNonNeeded.remove(0);
			} else {
				now = PROGRESS.MAKEFRAME;
				makeNextFrame();
			}
			break;
		case MAKEFRAME:
			if (cacheFrame.size() > 0) {
				makeNextFrame();
			} else {
				now = PROGRESS.BREAKBLOCK;
				if (breakBlock(target))
					setNextTarget();
			}
			break;
		case MOVEHEAD:
			break;
		case BREAKBLOCK:
			if (breakBlock(target))
				setNextTarget();
			break;
		default:
			break;
		}
		ArrayList<ItemStack> cache = new ArrayList<ItemStack>();
		for (ItemStack is : cacheItems) {
			ItemStack added = Utils.addToRandomInventory(is, worldObj, xCoord,
					yCoord, zCoord, ForgeDirection.UNKNOWN);
			is.stackSize -= added.stackSize;
			if (is.stackSize > 0)
				if (!Utils.addToRandomPipeEntry(this, ForgeDirection.UNKNOWN,
						is))
					cache.add(is);
		}
		cacheItems = cache;
	}

	@Override
	public void onInventoryChanged() {
		silktouch = false;
		efficiency = 0;
		fortune = 0;
		for (ItemStack is : inv) {
			if (is == null)
				continue;
			if (is.getItem() instanceof ItemBase) {
				switch (is.getItemDamage()) {
				case 1:
					silktouch = true;
					break;
				case 2:
					fortune += is.stackSize;
					break;
				case 3:
					efficiency += is.stackSize;
					break;
				default:
				}
			}
		}
	}

	public void destroyQuarry() {
		destoryFrames();
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
		PowerFramework.currentFramework.loadPowerProvider(this, nbttc);
		NBTTagList items = nbttc.getTagList("Items");
		for (byte i = 0; i < items.tagCount(); i++) {
			NBTTagCompound item = (NBTTagCompound) items.tagAt(i);
			inv[item.getByte("Slot")] = ItemStack.loadItemStackFromNBT(item);
		}
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
		PowerFramework.currentFramework.savePowerProvider(this, nbttc);
		NBTTagList items = new NBTTagList();
		for (byte i = 0; i < getSizeInventory(); i++) {
			ItemStack is = inv[i];
			if (is != null) {
				NBTTagCompound item = new NBTTagCompound();
				is.writeToNBT(item);
				item.setByte("Slot", i);
				items.appendTag(item);
			}
		}
		nbttc.setTag("Items", items);
		nbttc.setByte("now", now.getByteValue());
	}

	private void makeNextFrame() {
		float y = -6F * (float) efficiency + 25F;
		if (pp.useEnergy(y, y, true) != y)
			return;
		int[] coord = cacheFrame.get(0);
		cacheFrame.remove(0);
		worldObj.setBlockWithNotify(coord[0], coord[1], coord[2],
				BuildCraftFactory.frameBlock.blockID);
	}

	private void setNextTarget() {
		target[0]++;
		if (target[0] == box.xMax) {
			target[0] = box.xMin + 1;
			target[2]++;
			if (target[2] == box.zMax) {
				target[2] = box.zMin + 1;
				target[1]--;
			}
		}
		if (target[1] < 1) {
			now = PROGRESS.NONE;
			destoryFrames();
			return;
		}
		int bid = worldObj.getBlockId(target[0], target[1], target[2]);
		if (bid == 0 || bid == Block.bedrock.blockID)
			setNextTarget();
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
		target[0] = box.xMin + 1;
		target[2] = box.zMin + 1;
		target[1] = box.yMin;
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
		for (int x = xn; x <= xx; x++) {
			checkAndAddFrame(new int[] { x, yn, zn });
			checkAndAddFrame(new int[] { x, yn, zx });
			checkAndAddFrame(new int[] { x, yx, zn });
			checkAndAddFrame(new int[] { x, yx, zx });
		}
		for (int y = yn; y <= yx; y++) {
			checkAndAddFrame(new int[] { xn, y, zn });
			checkAndAddFrame(new int[] { xn, y, zx });
			checkAndAddFrame(new int[] { xx, y, zn });
			checkAndAddFrame(new int[] { xx, y, zx });
		}
		for (int z = zn; z <= zx; z++) {
			checkAndAddFrame(new int[] { xn, yn, z });
			checkAndAddFrame(new int[] { xn, yx, z });
			checkAndAddFrame(new int[] { xx, yn, z });
			checkAndAddFrame(new int[] { xx, yx, z });
		}
	}

	private void checkAndAddFrame(int[] coord) {
		if (!(worldObj.getBlockId(coord[0], coord[1], coord[2]) == BuildCraftFactory.frameBlock.blockID && worldObj
				.getBlockMetadata(coord[0], coord[1], coord[2]) == 0))
			cacheFrame.add(coord);
	}

	private void destoryFrames() {
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
		for (int x = box.xMin; x <= box.xMax; x++) {
			for (int y = box.yMin; y <= box.yMax; y++) {
				for (int z = box.zMin; z <= box.zMax; z++) {
					int bid = worldObj.getBlockId(x, y, z);
					if (bid != 0 && bid != Block.bedrock.blockID)
						cacheNonNeeded.add(new int[] { x, y, z });
				}
			}
		}
	}

	private void setBreakableFrame(int x, int y, int z) {
		if (worldObj.getBlockId(x, y, z) == BuildCraftFactory.frameBlock.blockID) {
			worldObj.setBlockMetadata(x, y, z, 1);
		}
	}

	private boolean breakBlock(int[] coord) {
		float pw = (-9.75F * (float) efficiency + 40F)
				* blockHardness(coord[0], coord[1], coord[2]);
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
		return true;
	}

	private float blockHardness(int x, int y, int z) {
		Block b = Block.blocksList[worldObj.getBlockId(x, y, z)];
		if (b != null)
			return b.getBlockHardness(worldObj, x, y, z);
		return (float) 0;
	}

	private ArrayList<ItemStack> getDroppedItems(int x, int y, int z) {
		Block b = Block.blocksList[worldObj.getBlockId(x, y, z)];
		if (b == null)
			return new ArrayList<ItemStack>();
		if (b.canSilkHarvest(worldObj, null, x, y, z,
				worldObj.getBlockMetadata(x, y, z))
				&& silktouch) {
			ArrayList<ItemStack> al = new ArrayList<ItemStack>();
			al.add(new ItemStack(b, 1, worldObj.getBlockMetadata(x, y, z)));
			return al;
		}
		return b.getBlockDropped(worldObj, x, y, z,
				worldObj.getBlockMetadata(x, y, z), fortune);
	}

	@Override
	public int getSizeInventory() {
		return 8;
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		return inv[var1];
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		ItemStack c = inv[var1];
		inv[var1] = null;
		return c;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return inv[var1];
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		inv[var1] = var2;
	}

	@Override
	public String getInvName() {
		return "QuarryPlus";
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		if (worldObj == null) {
			return true;
		}
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this) {
			return false;
		}
		return var1.getDistanceSq((double) xCoord + 0.5D,
				(double) yCoord + 0.5D, (double) zCoord + 0.5D) <= 64D;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
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
}
