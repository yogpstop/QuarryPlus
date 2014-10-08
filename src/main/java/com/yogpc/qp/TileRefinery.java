/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.api.recipes.BuildcraftRecipes;
import buildcraft.api.recipes.IRefineryRecipeManager.IRefineryRecipe;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.yogpc.mc_lib.APowerTile;
import com.yogpc.mc_lib.PacketHandler;
import com.yogpc.mc_lib.YogpstopPacket;

public class TileRefinery extends APowerTile implements IFluidHandler, IEnchantableTile {
  public FluidStack src1, src2, res;
  private int ticks;

  public float animationSpeed = 1;
  private int animationStage = 0;

  protected byte unbreaking;
  protected byte fortune;
  protected boolean silktouch;
  protected byte efficiency;

  public int buf;

  @Override
  public void G_reinit() {
    PowerManager.configureR(this, this.efficiency, this.unbreaking);
    this.buf = (int) (FluidContainerRegistry.BUCKET_VOLUME * 4 * Math.pow(1.3, this.fortune));
  }

  @Override
  public void readFromNBT(final NBTTagCompound nbttc) {
    super.readFromNBT(nbttc);
    this.silktouch = nbttc.getBoolean("silktouch");
    this.fortune = nbttc.getByte("fortune");
    this.efficiency = nbttc.getByte("efficiency");
    this.unbreaking = nbttc.getByte("unbreaking");
    this.src1 = FluidStack.loadFluidStackFromNBT(nbttc.getCompoundTag("src1"));
    this.src2 = FluidStack.loadFluidStackFromNBT(nbttc.getCompoundTag("src2"));
    this.res = FluidStack.loadFluidStackFromNBT(nbttc.getCompoundTag("res"));
    this.animationSpeed = nbttc.getFloat("animationSpeed");
    this.animationStage = nbttc.getInteger("animationStage");
    this.buf = (int) (FluidContainerRegistry.BUCKET_VOLUME * 4 * Math.pow(1.3, this.fortune));
    PowerManager.configureR(this, this.efficiency, this.unbreaking);
  }

  @Override
  public void writeToNBT(final NBTTagCompound nbttc) {
    super.writeToNBT(nbttc);
    nbttc.setBoolean("silktouch", this.silktouch);
    nbttc.setByte("fortune", this.fortune);
    nbttc.setByte("efficiency", this.efficiency);
    nbttc.setByte("unbreaking", this.unbreaking);
    if (this.src1 != null)
      nbttc.setTag("src1", this.src1.writeToNBT(new NBTTagCompound()));
    if (this.src2 != null)
      nbttc.setTag("src2", this.src2.writeToNBT(new NBTTagCompound()));
    if (this.res != null)
      nbttc.setTag("res", this.res.writeToNBT(new NBTTagCompound()));
    nbttc.setFloat("animationSpeed", this.animationSpeed);
    nbttc.setInteger("animationStage", this.animationStage);
  }

  @Override
  public void updateEntity() {
    super.updateEntity();
    if (this.worldObj.isRemote) {
      simpleAnimationIterate();
      return;
    }
    if (this.worldObj.getWorldTime() % 20 == 7)
      PacketHandler.sendPacketToAround(new YogpstopPacket(this),
          this.worldObj.provider.dimensionId, this.xCoord, this.yCoord, this.zCoord);
    this.ticks++;
    for (int i = this.efficiency + 1; i > 0; i--) {
      final IRefineryRecipe r = BuildcraftRecipes.refinery.findRefineryRecipe(this.src1, this.src2);
      if (r == null) {
        decreaseAnimation();
        this.ticks = 0;
        return;
      }
      if (this.res != null && r.getResult().amount > this.buf - this.res.amount) {
        decreaseAnimation();
        return;
      }
      if (r.getTimeRequired() > this.ticks)
        return;
      if (i == 1)
        this.ticks = 0;
      if (!PowerManager.useEnergyR(this, r.getEnergyCost(), this.unbreaking)) {
        decreaseAnimation();
        return;
      }
      increaseAnimation();
      if (r.getIngredient1().isFluidEqual(this.src1))
        this.src1.amount -= r.getIngredient1().amount;
      else
        this.src2.amount -= r.getIngredient1().amount;
      if (r.getIngredient2() != null)
        if (r.getIngredient2().isFluidEqual(this.src2))
          this.src2.amount -= r.getIngredient2().amount;
        else
          this.src1.amount -= r.getIngredient2().amount;
      if (this.src1 != null && this.src1.amount == 0)
        this.src1 = null;
      if (this.src2 != null && this.src2.amount == 0)
        this.src2 = null;
      if (this.res == null)
        this.res = r.getResult().copy();
      else
        this.res.amount += r.getResult().amount;
    }
  }

  public int getAnimationStage() {
    return this.animationStage;
  }

  private void simpleAnimationIterate() {
    if (this.animationSpeed > 1) {
      this.animationStage += this.animationSpeed;

      if (this.animationStage > 300)
        this.animationStage = 100;
    } else if (this.animationStage > 0)
      this.animationStage--;
  }

  private void sendNowPacket() {
    try {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream(bos);
      dos.writeFloat(this.animationSpeed);
      PacketHandler.sendPacketToAround(new YogpstopPacket(bos.toByteArray(), this,
          PacketHandler.StC_NOW), this.worldObj.provider.dimensionId, this.xCoord, this.yCoord,
          this.zCoord);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void increaseAnimation() {
    final float prev = this.animationSpeed;
    if (this.animationSpeed < 2)
      this.animationSpeed = 2;
    else if (this.animationSpeed <= 5)
      this.animationSpeed += 0.1;

    this.animationStage += this.animationSpeed;

    if (this.animationStage > 300)
      this.animationStage = 100;
    if (this.animationSpeed != prev)
      sendNowPacket();
  }

  private void decreaseAnimation() {
    final float prev = this.animationSpeed;
    if (this.animationSpeed >= 1) {
      this.animationSpeed -= 0.1;

      this.animationStage += this.animationSpeed;

      if (this.animationStage > 300)
        this.animationStage = 100;
    } else if (this.animationStage > 0)
      this.animationStage--;
    if (this.animationSpeed != prev)
      sendNowPacket();
  }

  @Override
  protected void S_recievePacket(final byte id, final byte[] data, final EntityPlayer ep) {

  }

  @Override
  protected void C_recievePacket(final byte id, final byte[] data, final EntityPlayer ep) {
    final ByteArrayDataInput badi = ByteStreams.newDataInput(data);
    switch (id) {
      case PacketHandler.StC_NOW:
        this.animationSpeed = badi.readFloat();
        break;
    }
  }

  @Override
  public int fill(final ForgeDirection from, final FluidStack resource, final boolean doFill) {
    if (resource.isFluidEqual(this.src1)) {
      final int ret = Math.min(this.buf - this.src1.amount, resource.amount);
      if (doFill)
        this.src1.amount += ret;
      return ret;
    } else if (resource.isFluidEqual(this.src2)) {
      final int ret = Math.min(this.buf - this.src2.amount, resource.amount);
      if (doFill)
        this.src2.amount += ret;
      return ret;
    } else if (this.src1 == null) {
      final int ret = Math.min(this.buf, resource.amount);
      if (doFill) {
        this.src1 = resource.copy();
        this.src1.amount = ret;
      }
      return ret;
    } else if (this.src2 == null) {
      final int ret = Math.min(this.buf, resource.amount);
      if (doFill) {
        this.src2 = resource.copy();
        this.src2.amount = ret;
      }
      return ret;
    }
    return 0;
  }

  @Override
  public FluidStack drain(final ForgeDirection from, final FluidStack resource,
      final boolean doDrain) {
    // if (resource == null) return null;
    if (resource.equals(this.res))
      return drain(from, resource.amount, doDrain);
    if (resource.equals(this.src1)) {
      final FluidStack ret = this.src1.copy();
      ret.amount = Math.min(resource.amount, ret.amount);
      if (doDrain) {
        this.src1.amount -= ret.amount;
        if (this.src1.amount == 0)
          this.src1 = null;
      }
      return ret;
    }
    if (resource.equals(this.src2)) {
      final FluidStack ret = this.src2.copy();
      ret.amount = Math.min(resource.amount, ret.amount);
      if (doDrain) {
        this.src2.amount -= ret.amount;
        if (this.src2.amount == 0)
          this.src2 = null;
      }
      return ret;
    }
    return null;
  }

  @Override
  public FluidStack drain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
    if (this.res == null)
      return null;
    final FluidStack ret = this.res.copy();
    ret.amount = Math.min(maxDrain, ret.amount);
    if (doDrain) {
      this.res.amount -= ret.amount;
      if (this.res.amount == 0)
        this.res = null;
    }
    return ret;
  }

  @Override
  public boolean canFill(final ForgeDirection from, final Fluid fluid) {
    return true;
  }

  @Override
  public boolean canDrain(final ForgeDirection from, final Fluid fluid) {
    return true;
  }

  @Override
  public FluidTankInfo[] getTankInfo(final ForgeDirection from) {
    return new FluidTankInfo[] {new FluidTankInfo(this.src1, this.buf),
        new FluidTankInfo(this.src2, this.buf), new FluidTankInfo(this.res, this.buf)};
  }

  @Override
  public byte getEfficiency() {
    return this.efficiency;
  }

  @Override
  public byte getFortune() {
    return this.fortune;
  }

  @Override
  public byte getUnbreaking() {
    return this.unbreaking;
  }

  @Override
  public boolean getSilktouch() {
    return this.silktouch;
  }

  @Override
  public void set(final byte pefficiency, final byte pfortune, final byte punbreaking,
      final boolean psilktouch) {
    this.efficiency = pefficiency;
    this.fortune = pfortune;
    this.unbreaking = punbreaking;
    this.silktouch = psilktouch;
  }
}
