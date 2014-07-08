package com.yogpc.qp;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CreativeTabQuarryPlus extends CreativeTabs {

	public CreativeTabQuarryPlus() {
		super("QuarryPlus");
	}

	@Override
	public Item getTabIconItem() {
		return Item.getItemFromBlock(QuarryPlus.blockQuarry);
	}

}
