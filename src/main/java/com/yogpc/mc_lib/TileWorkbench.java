package com.yogpc.mc_lib;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
  protected void S_recievePacket(final byte id, final byte[] data, final EntityPlayer ep) {}

  @Override
  protected void C_recievePacket(final byte id, final byte[] data, final EntityPlayer ep) {}

}
