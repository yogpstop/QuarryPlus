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

package com.yogpc.qp.tile;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.inventory.IInventoryConnection;
import cpw.mods.fml.common.Optional;

@Optional.Interface(iface = "cofh.api.inventory.IInventoryConnection", modid = "CoFHAPI|inventory")
public class TileBreaker extends TileEntity implements IEnchantableTile, IInventory,
    IInventoryConnection {
  public boolean silktouch = false;
  public byte fortune = 0;

  @Override
  public void readFromNBT(final NBTTagCompound nbttc) {
    super.readFromNBT(nbttc);
    this.silktouch = nbttc.getBoolean("silktouch");
    this.fortune = nbttc.getByte("fortune");
  }

  @Override
  public void writeToNBT(final NBTTagCompound nbttc) {
    super.writeToNBT(nbttc);
    nbttc.setBoolean("silktouch", this.silktouch);
    nbttc.setByte("fortune", this.fortune);
  }

  @Override
  public Map<Integer, Byte> get() {
    final Map<Integer, Byte> ret = new HashMap<Integer, Byte>();
    if (this.fortune > 0)
      ret.put(Integer.valueOf(Enchantment.fortune.effectId), Byte.valueOf(this.fortune));
    if (this.silktouch)
      ret.put(Integer.valueOf(Enchantment.silkTouch.effectId), Byte.valueOf((byte) 1));
    return ret;
  }

  @Override
  public void set(final int id, final byte val) {
    if (id == Enchantment.fortune.effectId)
      this.fortune = val;
    else if (id == Enchantment.silkTouch.effectId && val > 0)
      this.silktouch = true;
  }

  @Override
  public void G_reinit() {}

  @Override
  public int getSizeInventory() {
    return 1;
  }

  @Override
  public ItemStack getStackInSlot(final int i) {// NOTE better way?
    return new ItemStack(Blocks.cobblestone, 0);
  }

  @Override
  public ItemStack decrStackSize(final int i, final int a) {
    return null;
  }

  @Override
  public ItemStack getStackInSlotOnClosing(final int i) {
    return null;
  }

  @Override
  public void setInventorySlotContents(final int p_70299_1_, final ItemStack p_70299_2_) {}

  @Override
  public String getInventoryName() {
    return "tile.BreakerPlus.name";
  }

  @Override
  public boolean hasCustomInventoryName() {
    return false;
  }

  @Override
  public int getInventoryStackLimit() {
    return 0;
  }

  @Override
  public void markDirty() {}

  @Override
  public boolean isUseableByPlayer(final EntityPlayer p_70300_1_) {
    return false;
  }

  @Override
  public void openInventory() {}

  @Override
  public void closeInventory() {}

  @Override
  public boolean isItemValidForSlot(final int p_94041_1_, final ItemStack p_94041_2_) {
    return false;
  }

  @Override
  @Optional.Method(modid = "CoFHAPI|inventory")
  public ConnectionType canConnectInventory(final ForgeDirection arg0) {
    return ConnectionType.FORCE;
  }
}
