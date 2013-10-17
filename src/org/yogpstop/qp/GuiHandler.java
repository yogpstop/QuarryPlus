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
import org.yogpstop.qp.client.GuiP_List;
import org.yogpstop.qp.client.GuiQ_List;
import org.yogpstop.qp.client.GuiPlacer;

import static org.yogpstop.qp.QuarryPlus.*;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
		case guiIdMover:
			return new GuiMover(player, world, x, y, z);
		case guiIdFList:
			return new GuiQ_List((byte) 0, (TileBasic) world.getBlockTileEntity(x, y, z));
		case guiIdSList:
			return new GuiQ_List((byte) 1, (TileBasic) world.getBlockTileEntity(x, y, z));
		case guiIdInfMJSrc:
			return new GuiInfMJSrc((TileInfMJSrc) world.getBlockTileEntity(x, y, z));
		case guiIdPlacer:
			return new GuiPlacer(player.inventory, (TilePlacer) world.getBlockTileEntity(x, y, z));
		case guiIdPump:
		case guiIdPump + 1:
		case guiIdPump + 2:
		case guiIdPump + 3:
		case guiIdPump + 4:
		case guiIdPump + 5:
			return new GuiP_List((byte) (ID - guiIdPump), (TilePump) world.getBlockTileEntity(x, y, z));
		}

		return null;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
		case guiIdMover:
			return new ContainerMover(player, world, x, y, z, null);
		case guiIdPlacer:
			return new ContainerPlacer(player.inventory, (TilePlacer) world.getBlockTileEntity(x, y, z));
		}
		return null;
	}
}
