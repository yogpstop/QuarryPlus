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
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public final class EnchantmentHelper {
  static final void init(final IEnchantableTile te, final NBTTagList nbttl) {
    if (nbttl != null)
      for (int i = 0; i < nbttl.tagCount(); i++)
        te.set(nbttl.getCompoundTagAt(i).getShort("id"),
            (byte) nbttl.getCompoundTagAt(i).getShort("lvl"));
    te.G_reinit();
  }

  public static Collection<IChatComponent> getEnchantmentsChat(final IEnchantableTile te) {
    final ArrayList<IChatComponent> als = new ArrayList<IChatComponent>();
    final Map<Integer, Byte> enchs = te.get();
    if (enchs.size() <= 0)
      als.add(new ChatComponentTranslation("chat.plusenchantno"));
    else
      als.add(new ChatComponentTranslation("chat.plusenchant"));
    for (final Map.Entry<Integer, Byte> e : enchs.entrySet())
      als.add(new ChatComponentTranslation("chat.indent", new ChatComponentTranslation(
          Enchantment.enchantmentsList[e.getKey().intValue()].getName()),
          new ChatComponentTranslation("enchantment.level."
              + Byte.toString(e.getValue().byteValue()))));
    return als;
  }

  static void enchantmentToIS(final IEnchantableTile te, final ItemStack is) {
    final Map<Integer, Byte> enchs = te.get();
    for (final Map.Entry<Integer, Byte> e : enchs.entrySet())
      is.addEnchantment(Enchantment.enchantmentsList[e.getKey().intValue()], e.getValue()
          .byteValue());
  }
}
