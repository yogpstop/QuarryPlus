package org.yogpstop.qp;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Item;

public class ItemFortune extends Item {

	public ItemFortune(int par1) {
		super(par1);
		this.iconIndex = 6;
		this.setItemName("FortuneModule");
		this.setCreativeTab(CreativeTabs.tabRedstone);
	}
	
	@Override
	public String getTextureFile(){
		return "/org/yogpstop/qp/blocks.png";
	}

}
