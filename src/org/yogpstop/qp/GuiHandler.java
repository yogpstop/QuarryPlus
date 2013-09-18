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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.yogpstop.qp.client.GuiInfMJSrc;
import org.yogpstop.qp.client.GuiMover;
import org.yogpstop.qp.client.GuiList;

import static org.yogpstop.qp.QuarryPlus.*;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
		case guiIdMover:
			return new GuiMover(player, world, x, y, z);
		case guiIdFList:
			return new GuiList((byte) 0, (TileBasic) world.getBlockTileEntity(x, y, z));
		case guiIdSList:
			return new GuiList((byte) 1, (TileBasic) world.getBlockTileEntity(x, y, z));
		case guiIdInfMJSrc:
			return new GuiInfMJSrc(x, y, z, world);
		}

		return null;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
		case guiIdMover:
			return new ContainerMover(player, world, x, y, z, null);
		}
		return null;
	}
}
