package org.yogpstop.qp;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Item;

public class ItemEfficiency extends Item {

	public ItemEfficiency(int par1) {
		super(par1);
		this.iconIndex = 7;
		this.setItemName("EfficiencyModule");
		this.setCreativeTab(CreativeTabs.tabRedstone);
	}
	
	@Override
	public String getTextureFile(){
		return "/org/yogpstop/qp/blocks.png";
	}
}
