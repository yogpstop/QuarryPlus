package com.yogpc.mc_lib;

import ic2.api.energy.tile.IEnergySink;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cofh.api.energy.IEnergyHandler;

public abstract class APowerTile extends APacketTile implements IPowerReceptor, IEnergyHandler,
    IEnergySink {
  private final PowerHandler pp = new PowerHandler(this, Type.MACHINE);
  private double all, maxGot, max, got;

  @Override
  public void updateEntity() {
    super.updateEntity();
    final double rem = Math.min(this.maxGot - this.got, this.max - this.all - this.got);
    this.got += this.pp.useEnergy(0, rem, true);
    this.all += this.got;
    this.got = 0;
  }

  @Override
  public final PowerReceiver getPowerReceiver(final ForgeDirection side) {
    return this.pp.getPowerReceiver();
  }

  @Override
  public final void doWork(final PowerHandler workProvider) {}

  @Override
  public final World getWorld() {
    return this.worldObj;
  }

  @Override
  public void readFromNBT(final NBTTagCompound nbttc) {
    super.readFromNBT(nbttc);
    this.all = nbttc.getDouble("storedEnergy");
    this.max = nbttc.getDouble("MAX_stored");
    this.maxGot = nbttc.getDouble("MAX_receive");
    this.pp.configure(0, this.maxGot, 0, this.max);
  }

  @Override
  public void writeToNBT(final NBTTagCompound nbttc) {
    super.writeToNBT(nbttc);
    nbttc.setDouble("storedEnergy", this.all);
    nbttc.setDouble("MAX_stored", this.max);
    nbttc.setDouble("MAX_receive", this.maxGot);
  }

  public final double useEnergy(final double n, final double x, final boolean real) {
    double res = 0;
    if (this.all >= n)
      if (this.all <= x) {
        res = this.all;
        if (real)
          this.all = 0;
      } else {
        res = x;
        if (real)
          this.all -= x;
      }
    return res;
  }

  private final double getEnergy(final double a) {
    final double ret =
        Math.min(Math.min(this.maxGot - this.got, this.max - this.all - this.got), a);
    this.got += ret;
    return ret;
  }

  public final double getStoredEnergy() {
    return this.all;
  }

  public final double getMaxStored() {
    return this.max;
  }

  public final void configure(final double x, final double maxstored) {
    this.maxGot = x;
    this.max = maxstored;
    this.pp.configure(0, this.maxGot, 0, this.max);
  }

  @Override
  public int getEnergyStored(final ForgeDirection d) {
    return (int) (this.all * 10);
  }

  @Override
  public int getMaxEnergyStored(final ForgeDirection d) {
    return (int) (this.max * 10);
  }

  @Override
  public int receiveEnergy(final ForgeDirection d, final int am, final boolean sim) {
    return (int) getEnergy((double) am / 10) * 10;
  }

  @Override
  public double getDemandedEnergy() {
    return Math.min(this.maxGot - this.got, this.max - this.all - this.got) * 2.5;
  }

  @Override
  public double injectEnergy(final ForgeDirection d, final double am, final double v) {
    return getEnergy(am / 2.5) * 2.5;
  }

  @Override
  public int getSinkTier() {
    return 3;
  }

  @Override
  public int extractEnergy(final ForgeDirection d, final int am, final boolean sim) {
    return 0;
  }

  @Override
  public boolean canConnectEnergy(final ForgeDirection d) {
    return true;
  }

  @Override
  public boolean acceptsEnergyFrom(final TileEntity te, final ForgeDirection d) {
    return true;
  }
}
