package org.yogpstop.qp;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ContainerQuarry extends Container {
    private World world;
    private int xCoord;
    private int yCoord;
    private int zCoord;

    public ContainerQuarry(EntityPlayer player, World world, int x, int y, int z) {
        this.world = world;
        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;

        for (int rows = 0; rows < 3; ++rows) {
            for (int slotIndex = 0; slotIndex < 9; ++slotIndex) {
                addSlotToContainer(new Slot(player.inventory, slotIndex + rows * 9 + 9, 48 + slotIndex * 18, 157 + rows * 18));
            }
        }

        for (int slotIndex = 0; slotIndex < 9; ++slotIndex) {
            addSlotToContainer(new Slot(player.inventory, slotIndex, 48 + slotIndex * 18, 215));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer) {
        return this.world.getBlockId(this.xCoord, this.yCoord, this.zCoord) != QuarryPlus.blockQuarry.blockID ? false : entityPlayer.getDistanceSq(
                this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        return null;
    }
}