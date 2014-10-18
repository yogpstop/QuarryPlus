package com.yogpc.mc_lib;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.transport.IPipeTile;

public class InvUtils {
  public static final int addToIInv(final IInventory ii, final ItemStack is1,
      final ForgeDirection fd, final boolean doAdd) {
    IInventory iii;
    int[] a;
    if (ii instanceof TileEntityChest) {
      final TileEntityChest chest = (TileEntityChest) ii;
      TileEntityChest adjacent = null;
      if (chest.adjacentChestXNeg != null)
        adjacent = chest.adjacentChestXNeg;
      if (chest.adjacentChestXPos != null)
        adjacent = chest.adjacentChestXPos;
      if (chest.adjacentChestZNeg != null)
        adjacent = chest.adjacentChestZNeg;
      if (chest.adjacentChestZPos != null)
        adjacent = chest.adjacentChestZPos;
      if (adjacent != null)
        iii = new InventoryLargeChest("", ii, adjacent);
      else
        iii = ii;
    } else
      iii = ii;
    if (iii instanceof ISidedInventory)
      a = ((ISidedInventory) iii).getAccessibleSlotsFromSide(fd.ordinal());
    else {
      a = new int[iii.getSizeInventory()];
      for (int i = 0; i < a.length; i++)
        a[i] = i;
    }
    int buf, rem = is1.stackSize;
    final List<Integer> e = new ArrayList<Integer>();
    for (final int i : a) {
      if (iii instanceof ISidedInventory) {
        if (!((ISidedInventory) iii).canInsertItem(i, is1, fd.ordinal()))
          continue;
      } else if (!iii.isItemValidForSlot(i, is1))
        continue;
      final ItemStack is2 = iii.getStackInSlot(i);
      if (is2 == null) {
        e.add(new Integer(i));
        continue;
      }
      if (!is2.isItemEqual(is1))
        continue;
      if (!ItemStack.areItemStackTagsEqual(is2, is1))
        continue;
      buf =
          Math.min(iii.getInventoryStackLimit(),
              Math.min(is2.stackSize + rem, is2.getMaxStackSize()));
      if (buf > is2.stackSize) {
        rem -= buf - is2.stackSize;
        if (doAdd)
          is2.stackSize = buf;
        if (rem <= 0)
          return is1.stackSize;
      }
    }
    for (final Integer i : e) {
      buf = Math.min(iii.getInventoryStackLimit(), Math.min(rem, is1.getMaxStackSize()));
      if (buf > 0) {
        rem -= buf;
        if (doAdd) {
          final ItemStack is2 = is1.copy();
          is2.stackSize = buf;
          iii.setInventorySlotContents(i.intValue(), is2);
        }
        if (rem <= 0)
          return is1.stackSize;
      }
    }
    return is1.stackSize - rem;
  }

  public static void injectToNearTile(final World w, final int x, final int y, final int z,
      final ItemStack is) {
    final List<IPipeTile> pp = new LinkedList<IPipeTile>();
    final List<ForgeDirection> ppd = new LinkedList<ForgeDirection>();
    final List<IInventory> pi = new LinkedList<IInventory>();
    final List<ForgeDirection> pid = new LinkedList<ForgeDirection>();
    for (final ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
      final TileEntity t = w.getTileEntity(x + d.offsetX, y + d.offsetY, z + d.offsetZ);
      if (t instanceof IInventory && !(t instanceof IPowerEmitter)
          && addToIInv((IInventory) t, is, d.getOpposite(), false) > 0) {
        pi.add((IInventory) t);
        pid.add(d.getOpposite());
      }
      if (t instanceof IPipeTile) {
        final IPipeTile p = (IPipeTile) t;
        if (p.getPipeType() != IPipeTile.PipeType.ITEM || !p.isPipeConnected(d.getOpposite()))
          continue;
        pp.add(p);
        ppd.add(d.getOpposite());
      }
    }
    for (int i = 0; i < pi.size(); i++) {
      is.stackSize -= addToIInv(pi.get(i), is, pid.get(i), true);
      if (is.stackSize <= 0)
        return;
    }
    for (int i = 0; i < pp.size(); i++) {
      is.stackSize -= pp.get(i).injectItem(is, true, ppd.get(i));
      if (is.stackSize <= 0)
        return;
    }
  }
}
