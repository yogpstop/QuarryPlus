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

package org.yogpstop.qp;

import org.yogpstop.qp.client.GuiInfMJSrc;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TileInfMJSrc extends APacketTile {
	public float power = 10;
	public int interval = 1;
	private int cInterval = 1;
	public boolean active = true;

	@Override
	public void updateEntity() {
		if (!this.active) return;
		if (--this.cInterval > 0) return;
		TileEntity te;
		for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
			te = this.worldObj.getBlockTileEntity(this.xCoord + d.offsetX, this.yCoord + d.offsetY, this.zCoord + d.offsetZ);
			if (te instanceof IPowerReceptor) {
				PowerReceiver pr = ((IPowerReceptor) te).getPowerReceiver(d.getOpposite());
				if (pr != null) pr.receiveEnergy(Type.ENGINE, this.power, d.getOpposite());
			}
		}
		this.cInterval = this.interval;
	}

	@Override
	void S_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		switch (pattern) {
		case PacketHandler.infmjsrc:
			this.power = data.readFloat();
			this.interval = data.readInt();
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.makeInfMJSrcPacket(this.xCoord, this.yCoord, this.zCoord, this.power, this.interval));
			break;
		case PacketHandler.infmjsrca:
			this.active = data.readBoolean();
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.makeInfMJSrcAPacket(this.xCoord, this.yCoord, this.zCoord, this.active));
		}
	}

	@Override
	void C_recievePacket(byte pattern, ByteArrayDataInput data) {
		switch (pattern) {
		case PacketHandler.infmjsrc:
			this.power = data.readFloat();
			this.interval = data.readInt();
			if (Minecraft.getMinecraft().currentScreen instanceof GuiInfMJSrc) {
				GuiInfMJSrc gims = (GuiInfMJSrc) Minecraft.getMinecraft().currentScreen;
				if (gims.x == this.xCoord && gims.y == this.yCoord && gims.z == this.zCoord) Minecraft.getMinecraft().thePlayer.openGui(QuarryPlus.instance,
						QuarryPlus.guiIdInfMJSrc, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			}
			break;
		case PacketHandler.infmjsrca:
			this.active = data.readBoolean();
			if (Minecraft.getMinecraft().currentScreen instanceof GuiInfMJSrc) {
				GuiInfMJSrc gims = (GuiInfMJSrc) Minecraft.getMinecraft().currentScreen;
				if (gims.x == this.xCoord && gims.y == this.yCoord && gims.z == this.zCoord) Minecraft.getMinecraft().thePlayer.openGui(QuarryPlus.instance,
						QuarryPlus.guiIdInfMJSrc, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttc) {
		super.readFromNBT(nbttc);
		this.power = nbttc.getFloat("power");
		this.interval = nbttc.getInteger("interval");
		this.active = nbttc.getBoolean("active");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttc) {
		super.writeToNBT(nbttc);
		nbttc.setFloat("power", this.power);
		nbttc.setInteger("interval", this.interval);
		nbttc.setBoolean("active", this.active);
	}

}
