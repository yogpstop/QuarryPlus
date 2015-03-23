package com.yogpc.qp.tile;

import com.yogpc.qp.compat.IC2Helper;

import ic2.api.energy.tile.IEnergySink;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.ModAPIManager;
import cpw.mods.fml.common.Optional;

@Optional.InterfaceList(value = {
    @Optional.Interface(iface = "cofh.api.energy.IEnergyHandler", modid = "CoFHAPI|energy"),
    @Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2API")})
public abstract class APowerTile extends APacketTile implements IEnergyHandler, IEnergySink {
  private double all, maxGot, max, got;
  private boolean ic2ok = false;

  @Override
  public void updateEntity() {
    super.updateEntity();
    if (ModAPIManager.INSTANCE.hasAPI("IC2API") && !this.ic2ok && !this.worldObj.isRemote) {
      try {
        IC2Helper.ic2load(this);
      } catch (final Exception e) {
      }
      this.ic2ok = true;
    }
    this.all += this.got;
    this.got = 0;
  }

  @Override
  public void invalidate() {
    super.invalidate();
    onChunkUnload();
  }

  @Override
  public void onChunkUnload() {
    super.onChunkUnload();
    if (this.ic2ok && !this.worldObj.isRemote) {
      try {
        IC2Helper.ic2unload(this);
      } catch (final Exception e) {
      }
      this.ic2ok = false;
    }
  }

  @Override
  public void readFromNBT(final NBTTagCompound nbttc) {
    super.readFromNBT(nbttc);
    this.all = nbttc.getDouble("storedEnergy");
    this.max = nbttc.getDouble("MAX_stored");
    this.maxGot = nbttc.getDouble("MAX_receive");
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

  private final double getEnergy(final double a, final boolean r) {
    final double ret =
        Math.min(Math.min(this.maxGot - this.got, this.max - this.all - this.got), a);
    if (r)
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
  }

  @Override
  public final int getEnergyStored(final ForgeDirection d) {
    return (int) (this.all * 10);
  }

  @Override
  public final int getMaxEnergyStored(final ForgeDirection d) {
    return (int) (this.max * 10);
  }

  @Override
  public final int receiveEnergy(final ForgeDirection d, final int am, final boolean sim) {
    return (int) getEnergy((double) am / 10, !sim) * 10;
  }

  @Override
  public final double getDemandedEnergy() {
    return Math.min(this.maxGot - this.got, this.max - this.all - this.got) * 2.5;
  }

  @Override
  public final double injectEnergy(final ForgeDirection d, final double am, final double v) {
    return am - getEnergy(am / 2.5, true) * 2.5;
  }

  @Override
  public final int getSinkTier() {
    return 3;
  }

  @Override
  public final int extractEnergy(final ForgeDirection d, final int am, final boolean sim) {
    return 0;
  }

  @Override
  public final boolean canConnectEnergy(final ForgeDirection d) {
    return true;
  }

  @Override
  public final boolean acceptsEnergyFrom(final TileEntity te, final ForgeDirection d) {
    return true;
  }
}
