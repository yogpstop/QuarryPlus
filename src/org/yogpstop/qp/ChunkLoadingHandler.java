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

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import com.google.common.collect.Lists;

public class ChunkLoadingHandler implements ForgeChunkManager.OrderedLoadingCallback {
	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world) {
		for (Ticket ticket : tickets) {
			int quarryX = ticket.getModData().getInteger("quarryX");
			int quarryY = ticket.getModData().getInteger("quarryY");
			int quarryZ = ticket.getModData().getInteger("quarryZ");
			TileEntity te = world.getBlockTileEntity(quarryX, quarryY, quarryZ);
			if (te instanceof TileQuarry) ((TileQuarry) te).forceChunkLoading(ticket);
			else if (te instanceof TileMarker) ((TileMarker) te).forceChunkLoading(ticket);
		}
	}

	@Override
	public List<Ticket> ticketsLoaded(List<Ticket> tickets, World world, int maxTicketCount) {
		List<Ticket> validTickets = Lists.newArrayList();
		for (Ticket ticket : tickets) {
			int quarryX = ticket.getModData().getInteger("quarryX");
			int quarryY = ticket.getModData().getInteger("quarryY");
			int quarryZ = ticket.getModData().getInteger("quarryZ");

			int blId = world.getBlockId(quarryX, quarryY, quarryZ);
			if (blId == QuarryPlus.blockQuarry.blockID || blId == QuarryPlus.blockMarker.blockID) {
				validTickets.add(ticket);
			}
		}
		return validTickets;
	}

}