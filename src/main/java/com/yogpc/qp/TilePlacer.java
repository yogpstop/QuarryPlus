/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class TilePlacer extends TileEntity implements IInventory {
  private final ItemStack[] stack = new ItemStack[getSizeInventory()];

  @Override
  public int getSizeInventory() {
    return 9;
  }

  @Override
  public ItemStack getStackInSlot(final int pos) {
    return this.stack[pos];
  }

  @Override
  public ItemStack decrStackSize(final int slot, final int size) {
    if (this.stack[slot] != null) {
      ItemStack itemstack;

      if (this.stack[slot].stackSize <= size) {
        itemstack = this.stack[slot];
        this.stack[slot] = null;
        markDirty();
        return itemstack;
      }
      itemstack = this.stack[slot].splitStack(size);

      if (this.stack[slot].stackSize == 0)
        this.stack[slot] = null;

      markDirty();
      return itemstack;
    }
    return null;
  }

  @Override
  public ItemStack getStackInSlotOnClosing(final int pos) {
    if (this.stack[pos] != null) {
      final ItemStack itemstack = this.stack[pos];
      this.stack[pos] = null;
      return itemstack;
    }
    return null;
  }

  @Override
  public void setInventorySlotContents(final int pos, final ItemStack parstack) {
    this.stack[pos] = parstack;
    if (parstack != null && parstack.stackSize > getInventoryStackLimit())
      parstack.stackSize = getInventoryStackLimit();
    markDirty();
  }

  @Override
  public String getInventoryName() {
    return "tile.PlacerPlus.name";
  }

  @Override
  public boolean hasCustomInventoryName() {
    return false;
  }

  @Override
  public int getInventoryStackLimit() {
    return 64;
  }

  @Override
  public boolean isUseableByPlayer(final EntityPlayer ep) {
    return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : ep
        .getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
  }

  @Override
  public void openInventory() {}

  @Override
  public void closeInventory() {}

  @Override
  public boolean isItemValidForSlot(final int i, final ItemStack itemstack) {
    return true;
  }

  @Override
  public void readFromNBT(final NBTTagCompound par1NBTTagCompound) {
    super.readFromNBT(par1NBTTagCompound);
    final NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items", 10);

    for (int i = 0; i < nbttaglist.tagCount(); ++i) {
      final NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
      final int j = nbttagcompound1.getByte("Slot") & 255;

      if (j >= 0 && j < this.stack.length)
        this.stack[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
    }
  }

  @Override
  public void writeToNBT(final NBTTagCompound par1NBTTagCompound) {
    super.writeToNBT(par1NBTTagCompound);
    final NBTTagList nbttaglist = new NBTTagList();

    for (int i = 0; i < this.stack.length; ++i)
      if (this.stack[i] != null) {
        final NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        nbttagcompound1.setByte("Slot", (byte) i);
        this.stack[i].writeToNBT(nbttagcompound1);
        nbttaglist.appendTag(nbttagcompound1);
      }

    par1NBTTagCompound.setTag("Items", nbttaglist);
  }
}
