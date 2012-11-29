package org.yogpstop.qp;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import net.minecraft.src.World;

public class ContainerQuarry extends Container {
	private IInventory inventoryQuarry;
	private EntityPlayer player;
	private IInventory playerInventory;
	private World world;
	private int xCoord;
	private int yCoord;
	private int zCoord;

	public ContainerQuarry(EntityPlayer player, World world, int x, int y, int z) {
		this.player = player;
		this.playerInventory = player.inventory;
		this.inventoryQuarry = (TileQuarry) world.getBlockTileEntity(x, y, z);
		this.world = world;
		this.xCoord = x;
		this.yCoord = y;
		this.zCoord = z;

		for (int rows = 0; rows < 5; ++rows) {
			addSlotToContainer(new SlotQuarry(inventoryQuarry, rows,
					32 + rows * 24, 35));
		}

		for (int rows = 0; rows < 2; ++rows) {
			for (int columns = 0; columns < 4; ++columns) {

				addSlotToContainer(new SlotQuarry(inventoryQuarry, columns
						+ rows * 4 + 5, 8 + rows * 144, 8 + columns * 18));
			}
		}

		for (int rows = 0; rows < 3; ++rows) {
			for (int slotIndex = 0; slotIndex < 9; ++slotIndex) {
				addSlotToContainer(new SlotQuarry(playerInventory, slotIndex
						+ rows * 9 + 9, 8 + slotIndex * 18, 84 + rows * 18));
			}
		}

		for (int slotIndex = 0; slotIndex < 9; ++slotIndex) {
			addSlotToContainer(new SlotQuarry(playerInventory, slotIndex,
					8 + slotIndex * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityPlayer) {
		return this.world.getBlockId(this.xCoord, this.yCoord, this.zCoord) != QuarryPlus.blockQuarry.blockID ? false
				: entityPlayer.getDistanceSq((double) this.xCoord + 0.5D,
						(double) this.yCoord + 0.5D,
						(double) this.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
		ItemStack var2 = null;
		Slot var3 = (Slot) this.inventorySlots.get(slotIndex);

		if (var3 != null && var3.getHasStack()) {
			ItemStack var4 = var3.getStack();
			var2 = var4.copy();

			if (slotIndex < 9) {
				if (!this.mergeItemStack(var4, 9, 45, true)) {
					return null;
				}
			} else if (!this.mergeItemStack(var4, 0, 9, false)) {
				return null;
			}

			if (var4.stackSize == 0) {
				var3.putStack((ItemStack) null);
			} else {
				var3.onSlotChanged();
			}

			if (var4.stackSize == var2.stackSize) {
				return null;
			}

			var3.onPickupFromSlot(player, var4);
		}

		return var2;
	}

}