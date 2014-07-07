package com.yogpc.qp;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

public abstract class APowerTile extends APacketTile implements IPowerReceptor {
	private final PowerHandler pp = new PowerHandler(this, Type.MACHINE);

	@Override
	public final PowerReceiver getPowerReceiver(ForgeDirection side) {
		return this.pp.getPowerReceiver();
	}

	@Override
	public final void doWork(PowerHandler workProvider) {}

	@Override
	public final World getWorld() {
		return this.worldObj;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttc) {
		super.readFromNBT(nbttc);
		this.pp.readFromNBT(nbttc);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttc) {
		super.writeToNBT(nbttc);
		this.pp.writeToNBT(nbttc);
	}

	public final double useEnergy(double min, double max, boolean real) {
		return this.pp.useEnergy(min, max, real);
	}

	public final double getStoredEnergy() {
		return this.pp.getEnergyStored();
	}

	public final double getMaxStored() {
		return this.pp.getMaxEnergyStored();
	}

	public final void configure(double min, double max, double activate, double maxstored) {
		this.pp.configure(0, max, activate, maxstored);
	}
}
