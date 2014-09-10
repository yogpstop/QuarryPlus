package com.yogpc.mc_lib;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;

public class TileWorkbench extends APowerTile {
  final List<ItemStack> buffer = new ArrayList<ItemStack>();
  private WorkbenchRecipe recipe = WorkbenchRecipe.first();

  public TileWorkbench() {
    configure(1000, Double.POSITIVE_INFINITY);
  }

  void add(final EntityPlayer ep) {
    final ItemStack out = this.recipe.add(ep, this);
    if (out != null) {
      ep.inventory.markDirty();
      if (!ep.inventory.addItemStackToInventory(out))
        ep.dropPlayerItemWithRandomChoice(out, false);
    }
  }

  void clicked(final EntityPlayer ep) {
    if (ep.isSneaking()) {
      this.recipe = this.recipe.next();
      ep.addChatMessage(new ChatComponentText(this.recipe.getOutput()));
      return;
    }
    this.recipe.sendDesc(ep, this);
  }

  @Override
  public void readFromNBT(final NBTTagCompound par1NBTTagCompound) {
    super.readFromNBT(par1NBTTagCompound);
    final NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items", 10);
    for (int i = 0; i < nbttaglist.tagCount(); ++i) {
      final NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
      this.buffer.add(ItemStack.loadItemStackFromNBT(nbttagcompound1));
    }
  }

  @Override
  public void writeToNBT(final NBTTagCompound par1NBTTagCompound) {
    super.writeToNBT(par1NBTTagCompound);
    final NBTTagList nbttaglist = new NBTTagList();
    for (final ItemStack is : this.buffer) {
      final NBTTagCompound nbttagcompound1 = new NBTTagCompound();
      is.writeToNBT(nbttagcompound1);
      nbttaglist.appendTag(nbttagcompound1);
    }
    par1NBTTagCompound.setTag("Items", nbttaglist);
  }

  @Override
  protected void S_recievePacket(final byte id, final byte[] data, final EntityPlayer ep) {}

  @Override
  protected void C_recievePacket(final byte id, final byte[] data, final EntityPlayer ep) {}

}
