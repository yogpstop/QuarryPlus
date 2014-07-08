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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;

public abstract class APacketTile extends TileEntity {
	abstract void S_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep);

	abstract void C_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep);

	@Override
	public final Packet getDescriptionPacket() {
		ByteBuf buf = Unpooled.buffer();
		buf.writeByte(0);
		PacketHandler.getPacketFromNBT(this).writeData(buf);
		return new FMLProxyPacket(buf, "QuarryPlus");
	}
}