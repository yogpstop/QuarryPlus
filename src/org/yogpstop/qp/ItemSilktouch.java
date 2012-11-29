package org.yogpstop.qp;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Item;

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
