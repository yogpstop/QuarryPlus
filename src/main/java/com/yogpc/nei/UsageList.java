package com.yogpc.nei;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.yogpc.mc_lib.YogpstopLib;
import com.yogpc.qp.QuarryPlus;

public class UsageList {
  static String search(final Item i, final int meta) {
    if (i == Item.getItemFromBlock(QuarryPlus.blockBreaker))
      return "yog.usage.blockBreaker";
    if (i == Item.getItemFromBlock(QuarryPlus.blockFrame))
      return "yog.usage.blockFrame";
    if (i == Item.getItemFromBlock(QuarryPlus.blockInfMJSrc))
      return "yog.usage.blockInfMJSrc";
    if (i == Item.getItemFromBlock(QuarryPlus.blockLaser))
      return "yog.usage.blockLaser";
    if (i == Item.getItemFromBlock(QuarryPlus.blockMarker))
      return "yog.usage.blockMarker";
    if (i == Item.getItemFromBlock(QuarryPlus.blockMiningWell))
      return "yog.usage.blockMiningWell";
    if (i == Item.getItemFromBlock(QuarryPlus.blockMover))
      return "yog.usage.blockMover";
    if (i == Item.getItemFromBlock(QuarryPlus.blockPlacer))
      return "yog.usage.blockPlacer";
    if (i == Item.getItemFromBlock(QuarryPlus.blockPlainPipe))
      return "yog.usage.blockPlainPipe";
    if (i == Item.getItemFromBlock(QuarryPlus.blockPump))
      return "yog.usage.blockPump";
    if (i == Item.getItemFromBlock(QuarryPlus.blockQuarry))
      return "yog.usage.blockQuarry";
    if (i == Item.getItemFromBlock(QuarryPlus.blockRefinery))
      return "yog.usage.blockRefinery";
    if (i == QuarryPlus.itemTool && meta == 0)
      return "yog.usage.statusChecker";
    if (i == QuarryPlus.itemTool && meta == 1)
      return "yog.usage.listEditor";
    if (i == QuarryPlus.itemTool && meta == 2)
      return "yog.usage.liquidSelector";
    if (i == YogpstopLib.magicmirror && meta == 0)
      return "yog.usage.magicMirror";
    if (i == YogpstopLib.magicmirror && meta == 1)
      return "yog.usage.dimensionMirror";
    if (i == YogpstopLib.armor)
      return "yog.usage.elecArmor";
    if (i == Item.getItemFromBlock(YogpstopLib.controller))
      return "yog.usage.spawnerController";
    if (i == Item.getItemFromBlock(YogpstopLib.workbench))
      return "yog.usage.blockWorkbench";
    return null;
  }

  static Map<ItemStack, String[]> getAll() {
    return new HashMap<ItemStack, String[]>();
  }

  static String[] getFromItemStack(final ItemStack is) {
    return new String[0];
  }
}
