/*
 * Copyright (C) 2012,2013 yogpstop
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the
 * GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.yogpstop.qp;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class ItemTool extends Item implements IEnchantableItem {
	Icon ile, ils;

	public ItemTool(int par1) {
		super(par1);
		setMaxStackSize(1);
		setHasSubtypes(true);
		this.setMaxDamage(0);
		setCreativeTab(QuarryPlus.ct);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int par1) {
		switch (par1) {
		case 1:
			return this.ile;
		case 2:
			return this.ils;
		}
		return this.itemIcon;
	}

	@Override
	public boolean isBookEnchantable(ItemStack itemstack1, ItemStack itemstack2) {
		return false;
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer ep, World w, int x, int y, int z, int side, float par8, float par9, float par10) {
		if (is.getItemDamage() == 1) {
			boolean s = false, f = false;
			NBTTagList nbttl = is.getEnchantmentTagList();
			if (nbttl != null) for (int i = 0; i < nbttl.tagCount(); i++) {
				short id = ((NBTTagCompound) nbttl.tagAt(i)).getShort("id");
				if (id == 33) s = true;
				if (id == 35) f = true;
			}
			if (w.getBlockTileEntity(x, y, z) instanceof TileBasic && s != f) {
				if (!w.isRemote) ((TileBasic) w.getBlockTileEntity(x, y, z)).sendOpenGUI(ep, f ? PacketHandler.StC_OPENGUI_FORTUNE
						: PacketHandler.StC_OPENGUI_SILKTOUCH);
				return true;
			}
		}
		return false;
	}

	@Override
	public String getUnlocalizedName(ItemStack is) {
		switch (is.getItemDamage()) {
		case 1:
			return "item.listEditor";
		case 2:
			return "item.liquidSelector";
		}
		return "item.statusChecker";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) {
		par3List.add(new ItemStack(par1, 1, 0));
		par3List.add(new ItemStack(par1, 1, 1));
		par3List.add(new ItemStack(par1, 1, 2));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister ir) {
		this.itemIcon = ir.registerIcon("yogpstop_qp:statusChecker");
		this.ile = ir.registerIcon("yogpstop_qp:listEditor");
		this.ils = ir.registerIcon("yogpstop_qp:liquidSelector");
	}

	@Override
	public boolean canMove(ItemStack is, int id, int meta) {
		if (meta != 1) return false;
		if (is.getEnchantmentTagList() != null) return false;
		return id == 33 || id == 35 || id == -1;
	}
}
