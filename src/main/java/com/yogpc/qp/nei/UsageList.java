package com.yogpc.qp.nei;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

public class UsageList {
  static Map<ItemStack, String[]> getAll() {
    return new HashMap<ItemStack, String[]>();
  }

  static String[] getFromItemStack(final ItemStack is) {
    return new String[0];
  }
}
