package org.yogpstop.qp;

import static org.yogpstop.qp.PacketHandler.*;

import java.util.LinkedList;
import java.util.List;

import com.google.common.io.ByteArrayDataInput;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import static buildcraft.BuildCraftFactory.plainPipeBlock;
import static buildcraft.core.utils.Utils.addToRandomPipeAround;
import static buildcraft.core.utils.Utils.addToRandomInventoryAround;

public class TileMiningWell extends TileBasic {

	private boolean working;

	public static double CE;
	public static double BP;
	public static double CF;
	public static double CS;

	boolean G_isWorking() {
		return this.working;
	}

	@Override
	protected void C_recievePacket(byte pattern, ByteArrayDataInput data) {
		super.C_recievePacket(pattern, data);
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
		while (!S_checkTarget(depth)) {
			if (this.working) this.worldObj.setBlock(this.xCoord, depth, this.zCoord, plainPipeBlock.blockID);
			depth--;
		}
		if (this.working) S_breakBlock(depth);
		List<ItemStack> cache = new LinkedList<ItemStack>();
		for (ItemStack is : this.cacheItems) {
			int added = addToRandomInventoryAround(this.worldObj, this.xCoord, this.yCoord, this.zCoord, is);
			is.stackSize -= added;
			if (is.stackSize > 0) {
				added = addToRandomPipeAround(this.worldObj, this.xCoord, this.yCoord, this.zCoord, ForgeDirection.UNKNOWN, is);
				is.stackSize -= added;
				if (is.stackSize > 0) cache.add(is);
			}
		}
		this.cacheItems = cache;
	}

	private boolean S_checkTarget(int depth) {
		if (depth < 1) {
			G_destroy();
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

	private boolean S_breakBlock(int depth) {
		return S_breakBlock(this.xCoord, depth, this.zCoord, BP, CE, CS, CF);
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
	protected void G_reinit() {
		this.working = true;
		sendNowPacket(this, (byte) 1);
	}

	@Override
	protected void G_destroy() {
		if (this.worldObj.isRemote) return;
		this.working = false;
		sendNowPacket(this, (byte) 0);
		for (int depth = this.yCoord - 1; depth > 0; depth--) {
			if (this.worldObj.getBlockId(this.xCoord, depth, this.zCoord) != plainPipeBlock.blockID) {
				break;
			}
			this.worldObj.setBlockToAir(this.xCoord, depth, this.zCoord);
		}
	}

	@Override
	public boolean isActive() {
		return G_isWorking();
	}
}
