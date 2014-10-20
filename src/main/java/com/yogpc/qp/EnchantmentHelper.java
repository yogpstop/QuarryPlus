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

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public final class EnchantmentHelper {
  static final void init(final IEnchantableTile te, final NBTTagList nbttl) {
    byte efficiency = 0, unbreaking = 0, fortune = 0;
    boolean silktouch = false;
    if (nbttl != null)
      for (int i = 0; i < nbttl.tagCount(); i++) {
        final short id = nbttl.getCompoundTagAt(i).getShort("id");
        final short lvl = nbttl.getCompoundTagAt(i).getShort("lvl");
        if (id == 32)
          efficiency = (byte) lvl;
        if (id == 33)
          silktouch = true;
        if (id == 34)
          unbreaking = (byte) lvl;
        if (id == 35)
          fortune = (byte) lvl;
      }
    te.set(efficiency, fortune, unbreaking, silktouch);
    te.G_reinit();
  }

  private static IChatComponent getEnchChat(final int id, final int l) {
    return new ChatComponentTranslation("chat.indent", new ChatComponentTranslation(
        Enchantment.enchantmentsList[id].getName()), new ChatComponentTranslation(
        "enchnatment.level." + Integer.toString(l)));
  }

  public static Collection<IChatComponent> getEnchantmentsChat(final IEnchantableTile te) {
    final ArrayList<IChatComponent> als = new ArrayList<IChatComponent>();
    if (te.getEfficiency() <= 0 && !te.getSilktouch() && te.getUnbreaking() <= 0
        && te.getFortune() <= 0)
      als.add(new ChatComponentTranslation("chat.plusenchantno"));
    else
      als.add(new ChatComponentTranslation("chat.plusenchant"));
    if (te.getEfficiency() > 0)
      als.add(getEnchChat(32, te.getEfficiency()));
    if (te.getSilktouch())
      als.add(getEnchChat(33, 1));
    if (te.getUnbreaking() > 0)
      als.add(getEnchChat(34, te.getUnbreaking()));
    if (te.getFortune() > 0)
      als.add(getEnchChat(35, te.getFortune()));
    return als;
  }

  static void enchantmentToIS(final IEnchantableTile te, final ItemStack is) {
    if (te.getEfficiency() > 0)
      is.addEnchantment(Enchantment.enchantmentsList[32], te.getEfficiency());
    if (te.getSilktouch())
      is.addEnchantment(Enchantment.enchantmentsList[33], 1);
    if (te.getUnbreaking() > 0)
      is.addEnchantment(Enchantment.enchantmentsList[34], te.getUnbreaking());
    if (te.getFortune() > 0)
      is.addEnchantment(Enchantment.enchantmentsList[35], te.getFortune());
  }
}
