package org.yogpstop.qp;

import java.util.List;

import static buildcraft.core.CreativeTabBuildCraft.tabBuildCraft;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemBase extends Item {

	public ItemBase(int par1) {
		super(par1);
		this.setItemName("ModuleBase");
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setCreativeTab(tabBuildCraft);
	}

	@Override
	public String getTextureFile() {
		return "/org/yogpstop/qp/blocks.png";
	}

	@Override
	public String getItemNameIS(ItemStack is) {
		switch (is.getItemDamage()) {
		case 1:
			return "item.SilkTouchModule";
		case 2:
			return "item.FortuneModule";
		case 3:
			return "item.EfficiencyModule";
		default:
			return "item.BaseModule";
		}
	}

	@Override
	public int getIconFromDamage(int meta) {
		switch (meta) {
		case 1:
			return 5;
		case 2:
			return 6;
		case 3:
			return 7;
		default:
			return 8;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs,
			List par3List) {
		par3List.add(new ItemStack(par1, 1, 0));
		par3List.add(new ItemStack(par1, 1, 1));
		par3List.add(new ItemStack(par1, 1, 2));
		par3List.add(new ItemStack(par1, 1, 3));
	}

}
