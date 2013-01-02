package org.yogpstop.qp;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemSilktouch extends Item {

	public ItemSilktouch(int par1) {
		super(par1);
		this.iconIndex = 5;
		this.setItemName("SilkTouchModule");
		this.setCreativeTab(CreativeTabs.tabRedstone);
	}
	
	@Override
	public String getTextureFile(){
		return "/org/yogpstop/qp/blocks.png";
	}

}
