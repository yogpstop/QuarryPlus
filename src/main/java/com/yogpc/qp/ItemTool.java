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

package com.yogpc.qp;

import java.util.List;

import com.yogpc.mc_lib.PacketHandler;
import com.yogpc.qp.QuarryPlus.BlockData;

import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class ItemTool extends Item implements IEnchantableItem {
	IIcon ile, ils;

	public ItemTool() {
		super();
		setMaxStackSize(1);
		setHasSubtypes(true);
		this.setMaxDamage(0);
		setCreativeTab(QuarryPlus.ct);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1) {
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
				short id = nbttl.getCompoundTagAt(i).getShort("id");
				if (id == 33) s = true;
				if (id == 35) f = true;
			}
			NBTTagCompound c = is.getTagCompound();
			Block b = w.getBlock(x, y, z);
			BlockData bd = null;
			if (c != null && c.hasKey("Bname")) {
				bd = new BlockData(c.getString("Bname"), c.getInteger("Bmeta"));
				if (b == null || b.isAir(w, x, y, z)) {
					c.removeTag("Bname");
					c.removeTag("Bmeta");
					return true;
				}
			}
			if (w.getTileEntity(x, y, z) instanceof TileBasic && s != f) {
				TileBasic tb = (TileBasic) w.getTileEntity(x, y, z);
				if (c != null && bd != null) {
					if (!w.isRemote) (f ? tb.fortuneList : tb.silktouchList).add(bd);
					c.removeTag("Bname");
					c.removeTag("Bmeta");
				} else if (!w.isRemote) tb.sendOpenGUI(ep, f ? PacketHandler.StC_OPENGUI_FORTUNE : PacketHandler.StC_OPENGUI_SILKTOUCH);
				return true;
			}
			if (b != null && !b.isAir(w, x, y, z)) {
				if (c == null) {
					c = new NBTTagCompound();
					is.setTagCompound(c);
				}
				String name = GameData.getBlockRegistry().getNameForObject(b);
				int meta = w.getBlockMetadata(x, y, z);
				if (c.hasKey("Bname") && name.equals(c.getString("Bname")) && meta == c.getInteger("Bmeta")) {
					c.setInteger("Bmeta", OreDictionary.WILDCARD_VALUE);
				} else {
					c.setString("Bname", name);
					c.setInteger("Bmeta", meta);
				}
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
	public void addInformation(ItemStack is, EntityPlayer ep, List l, boolean b) {
		NBTTagCompound c = is.getTagCompound();
		if (c != null && c.hasKey("Bname")) {
			l.add(c.getString("Bname"));
			int meta = c.getInteger("Bmeta");
			if (meta != OreDictionary.WILDCARD_VALUE) l.add(Integer.toString(meta));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item i, CreativeTabs par2CreativeTabs, List par3List) {
		par3List.add(new ItemStack(i, 1, 0));
		par3List.add(new ItemStack(i, 1, 1));
		par3List.add(new ItemStack(i, 1, 2));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir) {
		this.itemIcon = ir.registerIcon("yogpstop_qp:statusChecker");
		this.ile = ir.registerIcon("yogpstop_qp:listEditor");
		this.ils = ir.registerIcon("yogpstop_qp:liquidSelector");
	}

	@Override
	public boolean canMove(ItemStack is, int id, int meta) {
		if (meta != 1) return false;
		NBTTagList l = is.getEnchantmentTagList();
		if (l != null && l.tagCount() != 0) return false;
		return id == 33 || id == 35 || id == -1;
	}
}
