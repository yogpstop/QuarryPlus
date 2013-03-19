package org.yogpstop.qp;

import java.util.List;

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
            TileQuarry tq = (TileQuarry) world.getBlockTileEntity(quarryX, quarryY, quarryZ);
            tq.forceChunkLoading(ticket);
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
            if (blId == QuarryPlus.blockQuarry.blockID) {
                validTickets.add(ticket);
            }
        }
        return validTickets;
    }

}