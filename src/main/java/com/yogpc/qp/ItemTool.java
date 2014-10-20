/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp;

import java.util.List;

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

import com.yogpc.qp.QuarryPlus.BlockData;

import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemTool extends Item implements IEnchantableItem {
  IIcon ile, ils;

  public ItemTool() {
    super();
    setMaxStackSize(1);
    setHasSubtypes(true);
    setMaxDamage(0);
    setCreativeTab(QuarryPlus.ct);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IIcon getIconFromDamage(final int par1) {
    switch (par1) {
      case 1:
        return this.ile;
      case 2:
        return this.ils;
    }
    return this.itemIcon;
  }

  @Override
  public boolean isBookEnchantable(final ItemStack itemstack1, final ItemStack itemstack2) {
    return false;
  }

  @Override
  public boolean onItemUse(final ItemStack is, final EntityPlayer ep, final World w, final int x,
      final int y, final int z, final int side, final float par8, final float par9,
      final float par10) {
    if (is.getItemDamage() == 1) {
      boolean s = false, f = false;
      final NBTTagList nbttl = is.getEnchantmentTagList();
      if (nbttl != null)
        for (int i = 0; i < nbttl.tagCount(); i++) {
          final short id = nbttl.getCompoundTagAt(i).getShort("id");
          if (id == 33)
            s = true;
          if (id == 35)
            f = true;
        }
      NBTTagCompound c = is.getTagCompound();
      final Block b = w.getBlock(x, y, z);
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
        final TileBasic tb = (TileBasic) w.getTileEntity(x, y, z);
        if (c != null && bd != null) {
          if (!w.isRemote)
            (f ? tb.fortuneList : tb.silktouchList).add(bd);
          c.removeTag("Bname");
          c.removeTag("Bmeta");
        } else if (!w.isRemote)
          ep.openGui(QuarryPlus.instance, f ? QuarryPlus.guiIdFList : QuarryPlus.guiIdSList, w, x,
              y, z);
        return true;
      }
      if (b != null && !b.isAir(w, x, y, z)) {
        if (c == null) {
          c = new NBTTagCompound();
          is.setTagCompound(c);
        }
        final String name = GameData.getBlockRegistry().getNameForObject(b);
        final int meta = w.getBlockMetadata(x, y, z);
        if (c.hasKey("Bname") && name.equals(c.getString("Bname")) && meta == c.getInteger("Bmeta"))
          c.setInteger("Bmeta", OreDictionary.WILDCARD_VALUE);
        else {
          c.setString("Bname", name);
          c.setInteger("Bmeta", meta);
        }
      }

    }
    return false;
  }

  @Override
  public String getUnlocalizedName(final ItemStack is) {
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
  public void addInformation(final ItemStack is, final EntityPlayer ep, final List l,
      final boolean b) {
    final NBTTagCompound c = is.getTagCompound();
    if (c != null && c.hasKey("Bname")) {
      l.add(c.getString("Bname"));
      final int meta = c.getInteger("Bmeta");
      if (meta != OreDictionary.WILDCARD_VALUE)
        l.add(Integer.toString(meta));
    }
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubItems(final Item i, final CreativeTabs par2CreativeTabs, final List par3List) {
    par3List.add(new ItemStack(i, 1, 0));
    par3List.add(new ItemStack(i, 1, 1));
    par3List.add(new ItemStack(i, 1, 2));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerIcons(final IIconRegister ir) {
    this.itemIcon = ir.registerIcon("yogpstop_qp:statusChecker");
    this.ile = ir.registerIcon("yogpstop_qp:listEditor");
    this.ils = ir.registerIcon("yogpstop_qp:liquidSelector");
  }

  @Override
  public boolean canMove(final ItemStack is, final int id, final int meta) {
    if (meta != 1)
      return false;
    final NBTTagList l = is.getEnchantmentTagList();
    if (l != null && l.tagCount() != 0)
      return false;
    return id == 33 || id == 35 || id == -1;
  }
}
