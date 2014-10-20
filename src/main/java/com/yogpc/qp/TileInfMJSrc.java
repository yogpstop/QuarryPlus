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

import java.lang.reflect.Method;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.yogpc.mc_lib.APacketTile;
import com.yogpc.mc_lib.PacketHandler;
import com.yogpc.mc_lib.ReflectionHelper;

public class TileInfMJSrc extends APacketTile {
  public float power = 10;
  public int interval = 1;
  private int cInterval = 1;

  private static final Method getMjBattery = ReflectionHelper.getMethod(
      ReflectionHelper.getClass("buildcraft.api.mj.MjAPI"), new String[] {"getMjBattery"},
      new Class<?>[] {Object.class});
  private static final Method addEnergy = ReflectionHelper.getMethod(
      ReflectionHelper.getClass("buildcraft.api.mj.IBatteryObject"), new String[] {"addEnergy"},
      new Class<?>[] {double.class});

  @Override
  public void updateEntity() {
    if (this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord)
        || this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord + 1, this.zCoord))
      return;
    if (--this.cInterval > 0)
      return;
    TileEntity te;
    for (final ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
      te =
          this.worldObj.getTileEntity(this.xCoord + d.offsetX, this.yCoord + d.offsetY, this.zCoord
              + d.offsetZ);
      Object o = null;
      if (getMjBattery != null)
        o = ReflectionHelper.invoke(getMjBattery, null, te);
      if (o != null && addEnergy != null) {
        ReflectionHelper.invoke(addEnergy, o, new Float(this.power));
        continue;
      }
      if (te instanceof IPowerReceptor) {
        final PowerReceiver pr = ((IPowerReceptor) te).getPowerReceiver(d.getOpposite());
        if (pr != null)
          pr.receiveEnergy(Type.ENGINE, this.power, d.getOpposite());
      }
    }
    this.cInterval = this.interval;
  }

  @Override
  protected void S_recievePacket(final byte id, final byte[] data, final EntityPlayer ep) {
    final ByteArrayDataInput badi = ByteStreams.newDataInput(data);
    switch (id) {
      case PacketHandler.CtS_INFMJSRC:
        this.power = badi.readFloat();
        this.interval = badi.readInt();
        break;
    }
  }

  @Override
  protected void C_recievePacket(final byte id, final byte[] data, final EntityPlayer ep) {}

  @Override
  public void readFromNBT(final NBTTagCompound nbttc) {
    super.readFromNBT(nbttc);
    this.power = nbttc.getFloat("power");
    this.interval = nbttc.getInteger("interval");
  }

  @Override
  public void writeToNBT(final NBTTagCompound nbttc) {
    super.writeToNBT(nbttc);
    nbttc.setFloat("power", this.power);
    nbttc.setInteger("interval", this.interval);
  }

}
