package org.yogpstop.qp;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.ForgeDirection;

import static org.yogpstop.qp.QuarryPlus.data;

import static buildcraft.BuildCraftFactory.plainPipeBlock;
import static buildcraft.core.utils.Utils.addToRandomPipeEntry;

public class TileMiningWell extends TileBasic {

	private boolean working;

	public static double CE;
	public static double BP;
	public static double CF;
	public static double CS;

	boolean isWorking() {
		return this.working;
	}

	@Override
	public void updateEntity() {
		if (this.worldObj.isRemote) return;
		int depth = this.yCoord - 1;
		while (!checkTarget(depth)) {
			this.worldObj.setBlock(this.xCoord, depth, this.zCoord, plainPipeBlock.blockID);
			depth--;
		}
		if (!this.working) return;
		breakBlock(depth);
		ArrayList<ItemStack> cache = new ArrayList<ItemStack>();
		for (ItemStack is : this.cacheItems) {
			ItemStack added = addToRandomInventory(is);
			is.stackSize -= added.stackSize;
			if (is.stackSize > 0) if (!addToRandomPipeEntry(this, ForgeDirection.UNKNOWN, is)) cache.add(is);
		}
		this.cacheItems = cache;
	}

	private boolean checkTarget(int depth) {
		int bid = this.worldObj.getBlockId(this.xCoord, depth, this.zCoord);
		if (depth < 1) {
			this.working = false;
			return true;
		}
		if (bid == 0 || bid == Block.bedrock.blockID || bid == plainPipeBlock.blockID) return false;
		return true;
	}

	private boolean breakBlock(int depth) {
		ArrayList<ItemStack> dropped = new ArrayList<ItemStack>();
		float pw = (float) Math.max(BP * blockHardness(depth) * addDroppedItems(dropped, depth) / Math.pow(CE, this.efficiency), 0D);
		if (this.pp.useEnergy(pw, pw, true) != pw) return false;
		this.cacheItems.addAll(dropped);
		this.worldObj.setBlock(this.xCoord, depth, this.zCoord, plainPipeBlock.blockID);
		return true;
	}

	private float blockHardness(int depth) {
		Block b = Block.blocksList[this.worldObj.getBlockId(this.xCoord, depth, this.zCoord)];
		if (b != null) {
			if (this.worldObj.getBlockMaterial(this.xCoord, depth, this.zCoord).isLiquid()) return 0;
			return b.getBlockHardness(this.worldObj, this.xCoord, depth, this.zCoord);
		}
		return 0;
	}

	private double addDroppedItems(ArrayList<ItemStack> list, int depth) {
		Block b = Block.blocksList[this.worldObj.getBlockId(this.xCoord, depth, this.zCoord)];
		int meta = this.worldObj.getBlockMetadata(this.xCoord, depth, this.zCoord);
		if (b == null) return 1;
		if (b.canSilkHarvest(this.worldObj, null, this.xCoord, depth, this.zCoord, meta) && this.silktouch
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
			list.addAll(b.getBlockDropped(this.worldObj, this.xCoord, depth, this.zCoord, meta, this.fortune));
			return Math.pow(CF, this.fortune);
		}
		list.addAll(b.getBlockDropped(this.worldObj, this.xCoord, depth, this.zCoord, meta, 0));
		return 1;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttc) {
		super.readFromNBT(nbttc);
		this.working = nbttc.getBoolean("working");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttc) {
		super.writeToNBT(nbttc);
		nbttc.setBoolean("working", this.working);
	}

	@Override
	protected void reinit() {
		this.working = true;
	}
}
