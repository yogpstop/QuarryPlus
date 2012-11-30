package org.yogpstop.qp;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Item;

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
