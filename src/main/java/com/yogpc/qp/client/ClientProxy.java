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

package com.yogpc.qp.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;

import com.yogpc.qp.CommonProxy;
import com.yogpc.qp.EntityMechanicalArm;
import com.yogpc.qp.TileLaser;
import com.yogpc.qp.TileQuarry;
import com.yogpc.qp.TileRefinery;

import buildcraft.core.render.RenderVoid;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	@Override
	public void registerTextures() {
		RenderingRegistry.registerEntityRenderingHandler(EntityMechanicalArm.class, new RenderVoid());
		ClientRegistry.bindTileEntitySpecialRenderer(TileRefinery.class, RenderRefinery.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry.class, new RenderQuarry());
		ClientRegistry.bindTileEntitySpecialRenderer(TileLaser.class, new RenderLaser());
		RenderingRegistry.registerBlockHandler(RenderRefinery.INSTANCE);
	}

	@Override
	public void removeEntity(Entity e) {
		e.worldObj.removeEntity(e);
		if (e.worldObj.isRemote) ((WorldClient) e.worldObj).removeEntityFromWorld(e.getEntityId());
	}

	@Override
	public World getClientWorld() {
		return Minecraft.getMinecraft().theWorld;
	}

	@Override
	public EntityPlayer getPacketPlayer(INetHandler inh) {
		if (inh instanceof NetHandlerPlayServer) return ((NetHandlerPlayServer) inh).playerEntity;
		return Minecraft.getMinecraft().thePlayer;
	}
}
