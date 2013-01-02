package org.yogpstop.qp;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemBase extends Item {

	public ItemBase(int par1) {
		super(par1);
		this.iconIndex = 8;
		this.setItemName("ModuleBase");
		this.setCreativeTab(CreativeTabs.tabRedstone);
	}
	
	@Override
	public String getTextureFile(){
		return "/org/yogpstop/qp/blocks.png";
	}

}
