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

import com.yogpc.qp.CommonProxy;
import com.yogpc.qp.EntityLaser;
import com.yogpc.qp.TileLaser;
import com.yogpc.qp.TileQuarry;
import com.yogpc.qp.TileRefinery;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	@Override
	public void registerTextures() {
		RenderingRegistry.registerEntityRenderingHandler(EntityLaser.class, RenderEntityLaser.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileRefinery.class, RenderRefinery.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry.class, RenderQuarry.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileLaser.class, RenderLaser.INSTANCE);
		RenderingRegistry.registerBlockHandler(RenderRefinery.INSTANCE);
		RenderingRegistry.registerBlockHandler(RenderLaserBlock.INSTANCE);
		RenderingRegistry.registerBlockHandler(RenderMarker.INSTANCE);
		RenderingRegistry.registerBlockHandler(RenderFrame.INSTANCE);
	}
}
