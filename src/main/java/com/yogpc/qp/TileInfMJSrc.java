/*
 * Copyright (C) 2012,2013 yogpstop
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the
 * GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;

import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

import com.google.common.io.ByteArrayDataInput;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileInfMJSrc extends APacketTile {
	public float power = 10;
	public int interval = 1;
	private int cInterval = 1;

	static Method getMjBattery = null;
	static Method addEnergy = null;
	static {
		try {
			getMjBattery = Class.forName("buildcraft.api.mj.MjAPI").getMethod("getMjBattery", Object.class);
			addEnergy = Class.forName("buildcraft.api.mj.IBatteryObject").getMethod("addEnergy", double.class);
		} catch (Exception e) {}
	}

	@Override
	public void updateEntity() {
		if (this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord)
				|| this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord + 1, this.zCoord)) return;
		if (--this.cInterval > 0) return;
		TileEntity te;
		for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
			te = this.worldObj.getTileEntity(this.xCoord + d.offsetX, this.yCoord + d.offsetY, this.zCoord + d.offsetZ);
			Object o = null;
			try {
				if (getMjBattery != null) o = getMjBattery.invoke(null, te);
				if (o != null && addEnergy != null) {
					addEnergy.invoke(o, this.power);
					continue;
				}
			} catch (Exception e) {}
			if (te instanceof IPowerReceptor) {
				PowerReceiver pr = ((IPowerReceptor) te).getPowerReceiver(d.getOpposite());
				if (pr != null) pr.receiveEnergy(Type.ENGINE, this.power, d.getOpposite());
			}
		}
		this.cInterval = this.interval;
	}

	void S_openGUI(EntityPlayer ep) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeInt(this.xCoord);
			dos.writeInt(this.yCoord);
			dos.writeInt(this.zCoord);
			dos.writeByte(PacketHandler.StC_OPENGUI_INFMJSRC);
			dos.writeFloat(this.power);
			dos.writeInt(this.interval);
			PacketHandler.sendPacketToPlayer(new QuarryPlusPacket(PacketHandler.Tile, bos.toByteArray()), ep);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	void S_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		switch (pattern) {
		case PacketHandler.CtS_INFMJSRC:
			this.power = data.readFloat();
			this.interval = data.readInt();
			S_openGUI(ep);
			break;
		}
	}

	@Override
	void C_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		switch (pattern) {
		case PacketHandler.StC_OPENGUI_INFMJSRC:
			this.power = data.readFloat();
			this.interval = data.readInt();
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdInfMJSrc, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttc) {
		super.readFromNBT(nbttc);
		this.power = nbttc.getFloat("power");
		this.interval = nbttc.getInteger("interval");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttc) {
		super.writeToNBT(nbttc);
		nbttc.setFloat("power", this.power);
		nbttc.setInteger("interval", this.interval);
	}

}
