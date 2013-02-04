package org.yogpstop.qp;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import static buildcraft.core.CreativeTabBuildCraft.tabBuildCraft;

public class ItemQuarry extends Item {

	public ItemQuarry(int par1) {
		super(par1);
		setMaxStackSize(1);
		setCreativeTab(tabBuildCraft);
		setTextureFile("/org/yogpstop/qp/blocks.png");
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer ep, World w, int x,
			int y, int z, int direction, float i, float j, float k) {
		if (w.isRemote)
			return true;
		switch (direction) {
		case 0:
			y--;
			break;
		case 1:
			y++;
			break;
		case 2:
			z--;
			break;
		case 3:
			z++;
			break;
		case 4:
			x--;
			break;
		case 5:
			x++;
			break;
		}
		if (ep.canPlayerEdit(x, y, z, direction, is) && w.isAirBlock(x, y, z)) {
			if (w.setBlockWithNotify(x, y, z, QuarryPlus.blockQuarry.blockID)) {
				QuarryPlus.blockQuarry.onBlockPlacedBy(w, x, y, z, ep);
				((TileQuarry) w.getBlockTileEntity(x, y, z)).init(is
						.getEnchantmentTagList());
				--is.stackSize;
			}
			return true;
		}
		return false;
	}
}
