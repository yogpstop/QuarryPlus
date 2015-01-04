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

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import com.yogpc.mc_lib.IPacketContainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerMover extends Container implements IPacketContainer {
  public IInventory craftMatrix = new InventoryBasic("Matrix", false, 2) {
    @Override
    public void markDirty() {
      super.markDirty();
      detectAndSendChanges();
    }
  };
  private final World worldObj;
  private final int posX, posY, posZ;

  public ContainerMover(final IInventory player, final World w, final int x, final int y,
      final int z) {
    this.worldObj = w;
    this.posX = x;
    this.posY = y;
    this.posZ = z;
    int row;
    int col;
    for (col = 0; col < 2; ++col)
      addSlotToContainer(new SlotMover(this.craftMatrix, col, 8 + col * 144, 35));
    for (row = 0; row < 3; ++row)
      for (col = 0; col < 9; ++col)
        addSlotToContainer(new Slot(player, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
    for (col = 0; col < 9; ++col)
      addSlotToContainer(new Slot(player, col, 8 + col * 18, 142));
  }

  @Override
  public void onContainerClosed(final EntityPlayer ep) {
    super.onContainerClosed(ep);
    if (this.worldObj.isRemote)
      return;
    for (int var2 = 0; var2 < 2; ++var2) {
      final ItemStack var3 = this.craftMatrix.getStackInSlotOnClosing(var2);
      if (var3 != null)
        ep.dropPlayerItemWithRandomChoice(var3, false);
    }
  }

  @Override
  public boolean canInteractWith(final EntityPlayer var1) {
    return this.worldObj.getBlock(this.posX, this.posY, this.posZ) != QuarryPlus.blockMover ? false
        : var1.getDistanceSq(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D) <= 64.0D;
  }

  public int avail = -1;

  @Override
  public void detectAndSendChanges() {
    super.detectAndSendChanges();
    final int n = checkInventory();
    if (this.avail != n) {
      this.avail = n;
      for (int j = 0; j < this.crafters.size(); ++j)
        ((ICrafting) this.crafters.get(j)).sendProgressBarUpdate(this, 0, this.avail);
    }
  }

  @Override
  public void addCraftingToCrafters(final ICrafting p) {
    super.addCraftingToCrafters(p);
    p.sendProgressBarUpdate(this, 0, checkInventory());
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void updateProgressBar(final int i, final int j) {
    this.avail = j;
  }

  @Override
  public ItemStack transferStackInSlot(final EntityPlayer ep, final int i) {
    ItemStack src = null;
    final Slot slot = (Slot) this.inventorySlots.get(i);
    if (slot != null && slot.getHasStack()) {
      final ItemStack remain = slot.getStack();
      src = remain.copy();
      if (i < 2) {
        if (!mergeItemStack(remain, 2, 38, true))
          return null;
      } else {
        Slot toslot;
        final ItemStack put = remain.copy();
        put.stackSize = 1;
        boolean changed = false;
        toslot = (Slot) this.inventorySlots.get(0);
        if (!changed && toslot.isItemValid(remain) && toslot.getStack() == null) {
          toslot.putStack(put);
          remain.stackSize--;
          changed = true;
        }
        toslot = (Slot) this.inventorySlots.get(1);
        if (!changed && toslot.isItemValid(remain) && toslot.getStack() == null) {
          toslot.putStack(put);
          remain.stackSize--;
          changed = true;
        }
        if (!changed)
          return null;
      }
      if (remain.stackSize == 0)
        slot.putStack(null);
      else
        slot.onSlotChanged();
      if (remain.stackSize == src.stackSize)
        return null;
      slot.onPickupFromSlot(ep, remain);
    }
    return src;
  }

  private void moveEnchant() {
    if (!checkTo(this.avail))
      return;
    ItemStack is;
    NBTTagList list;
    is = this.craftMatrix.getStackInSlot(0);
    list = is.getEnchantmentTagList();
    if (list == null)
      return;
    boolean done = false;
    for (int i = 0; i < list.tagCount(); i++) {
      short lvl = list.getCompoundTagAt(i).getShort("lvl");
      if (lvl > 0 && list.getCompoundTagAt(i).getShort("id") == this.avail) {
        if (lvl > 1)
          list.getCompoundTagAt(i).setShort("lvl", --lvl);
        else {
          {
            final NBTTagList nlist = new NBTTagList();
            for (int j = 0; j < list.tagCount(); j++)
              if (list.getCompoundTagAt(j).getShort("id") != this.avail)
                nlist.appendTag(list.getCompoundTagAt(j));
            list = nlist;
          }
          is.getTagCompound().removeTag("ench");
          if (list.tagCount() > 0)
            is.getTagCompound().setTag("ench", list);
          if (is.getTagCompound().hasNoTags())
            is.setTagCompound(null);
        }
        done = true;
        break;
      }
    }
    if (!done)
      return;
    done = false;
    is = this.craftMatrix.getStackInSlot(1);
    list = is.getEnchantmentTagList();
    if (list == null || list.tagCount() == 0) {
      if (!is.hasTagCompound())
        is.setTagCompound(new NBTTagCompound());
      final NBTTagCompound nbttc = is.getTagCompound();
      list = new NBTTagList();
      nbttc.setTag("ench", list);
    } else
      for (int i = 0; i < list.tagCount(); i++) {
        final short id = list.getCompoundTagAt(i).getShort("id");
        short lvl = list.getCompoundTagAt(i).getShort("lvl");
        if (lvl < Enchantment.enchantmentsList[id].getMaxLevel() && id == this.avail) {
          list.getCompoundTagAt(i).setShort("lvl", ++lvl);
          done = true;
          break;
        }
      }
    if (!done) {
      final NBTTagCompound ench = new NBTTagCompound();
      ench.setShort("id", (short) this.avail);
      ench.setShort("lvl", (short) 1);
      list.appendTag(ench);
    }
  }

  private int checkInventory() {
    final ItemStack pickaxeIs = this.craftMatrix.getStackInSlot(0);
    if (this.craftMatrix.getStackInSlot(1) == null || pickaxeIs == null)
      return -1;
    final NBTTagList pickaxeE = pickaxeIs.getEnchantmentTagList();
    if (pickaxeE == null)
      return -1;
    for (int i = 0; i < pickaxeE.tagCount(); i++) {
      final short id = pickaxeE.getCompoundTagAt(i).getShort("id");
      if (checkTo(id) && pickaxeE.getCompoundTagAt(i).getShort("lvl") > 0
          && (id == this.avail || this.avail < 0))
        return id;
    }
    return -1;
  }

  private int next(final int add) {
    final ItemStack pickaxeIs = this.craftMatrix.getStackInSlot(0);
    if (this.craftMatrix.getStackInSlot(1) == null || pickaxeIs == null)
      return -1;
    final NBTTagList pickaxeE = pickaxeIs.getEnchantmentTagList();
    if (pickaxeE == null)
      return -1;
    int first = -1;
    boolean next = false;
    for (int i = add > 0 ? 0 : pickaxeE.tagCount() - 1; 0 <= i && i < pickaxeE.tagCount(); i += add) {
      final short id = pickaxeE.getCompoundTagAt(i).getShort("id");
      if (!checkTo(id) || pickaxeE.getCompoundTagAt(i).getShort("lvl") <= 0)
        continue;
      if (this.avail < 0 || next)
        return id;
      if (first < 0)
        first = id;
      if (id == this.avail)
        next = true;
    }
    return first;
  }

  private boolean checkTo(final int id) {
    final ItemStack target = this.craftMatrix.getStackInSlot(1);
    if (target == null || !(target.getItem() instanceof IEnchantableItem)
        || !((IEnchantableItem) target.getItem()).canMove(target, id))
      return false;
    final NBTTagList quarryE = target.getEnchantmentTagList();
    if (quarryE == null)
      return true;
    for (int i = 0; i < quarryE.tagCount(); i++)
      if (id == quarryE.getCompoundTagAt(i).getShort("id")) {
        if (Enchantment.enchantmentsList[id].getMaxLevel() > quarryE.getCompoundTagAt(i).getShort(
            "lvl"))
          return true;
        return false;
      }
    return true;
  }

  @Override
  public void receivePacket(final byte[] ba) {
    if (ba[0] == 2)
      moveEnchant();
    else {
      this.avail = next(ba[0] - 2);
      for (int j = 0; j < this.crafters.size(); ++j)
        ((ICrafting) this.crafters.get(j)).sendProgressBarUpdate(this, 0, this.avail);
    }
    detectAndSendChanges();
  }
}
