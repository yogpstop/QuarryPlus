package com.yogpc.qp;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

import com.yogpc.mc_lib.APacketTile;

public abstract class APowerTile extends APacketTile implements IPowerReceptor {
  private final PowerHandler pp = new PowerHandler(this, Type.MACHINE);
  private double stored, recv_max, stored_max;

  @Override
  public void updateEntity() {
    super.updateEntity();
    double tick_received = 0;
    double remain_receive;
    remain_receive =
        Math.min(this.recv_max - tick_received, this.stored_max - this.stored - tick_received);
    tick_received += this.pp.useEnergy(0, remain_receive, true);
    this.stored += tick_received;
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
    this.stored = nbttc.getDouble("storedEnergy");
    this.stored_max = nbttc.getDouble("MAX_stored");
    this.recv_max = nbttc.getDouble("MAX_receive");
    this.pp.configure(0, this.recv_max, 0, this.stored_max);
  }

  @Override
  public void writeToNBT(final NBTTagCompound nbttc) {
    super.writeToNBT(nbttc);
    nbttc.setDouble("storedEnergy", this.stored);
    nbttc.setDouble("MAX_stored", this.stored_max);
    nbttc.setDouble("MAX_receive", this.recv_max);
  }

  public final double useEnergy(final double min, final double max, final boolean real) {
    double res = 0;
    if (this.stored >= min)
      if (this.stored <= max) {
        res = this.stored;
        if (real)
          this.stored = 0;
      } else {
        res = max;
        if (real)
          this.stored -= max;
      }
    return res;
  }

  public final double getStoredEnergy() {
    return this.stored;
  }

  public final double getMaxStored() {
    return this.stored_max;
  }

  public final void configure(final double max, final double maxstored) {
    this.recv_max = max;
    this.stored_max = maxstored;
    this.pp.configure(0, this.recv_max, 0, this.stored_max);
  }
}
