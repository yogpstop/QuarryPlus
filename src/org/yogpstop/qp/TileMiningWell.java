package org.yogpstop.qp;

import static org.yogpstop.qp.PacketHandler.*;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.ForgeDirection;

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
	protected void recievePacketOnClient(byte pattern, ByteArrayDataInput data) {
		super.recievePacketOnClient(pattern, data);
		switch (pattern) {
		case packetNow:
			this.working = data.readBoolean();
			this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
			break;
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (this.worldObj.isRemote) return;
		int depth = this.yCoord - 1;
		while (!checkTarget(depth)) {
			if (this.working) this.worldObj.setBlock(this.xCoord, depth, this.zCoord, plainPipeBlock.blockID);
			depth--;
		}
		if (this.working) breakBlock(depth);
		ArrayList<ItemStack> cache = new ArrayList<ItemStack>();
		for (ItemStack is : this.cacheItems) {
			ItemStack added = addToRandomInventory(is);
			is.stackSize -= added.stackSize;
			if (is.stackSize > 0) if (!addToRandomPipeEntry(this, ForgeDirection.UNKNOWN, is)) cache.add(is);
		}
		this.cacheItems = cache;
	}

	private boolean checkTarget(int depth) {
		if (depth < 1) {
			destroy();
			return true;
		}
		int bid = this.worldObj.getBlockId(this.xCoord, depth, this.zCoord);
		if (bid == 0 || bid == Block.bedrock.blockID || bid == plainPipeBlock.blockID) return false;
		if (this.pump == ForgeDirection.UNKNOWN && this.worldObj.getBlockMaterial(this.xCoord, depth, this.zCoord).isLiquid()) return false;
		if (!this.working) {
			this.working = true;
			sendNowPacket(this, (byte) 1);
		}
		return true;
	}

	private boolean breakBlock(int depth) {
		return breakBlock(this.xCoord, depth, this.zCoord, BP, CE, CS, CF);
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
		sendNowPacket(this, (byte) 1);
	}

	@Override
	protected void destroy() {
		this.working = false;
		sendNowPacket(this, (byte) 0);
		for (int depth = this.yCoord - 1; depth > 0; depth--) {
			if (this.worldObj.getBlockId(this.xCoord, depth, this.zCoord) != plainPipeBlock.blockID) {
				break;
			}
			this.worldObj.setBlockToAir(this.xCoord, depth, this.zCoord);
		}
	}
}
