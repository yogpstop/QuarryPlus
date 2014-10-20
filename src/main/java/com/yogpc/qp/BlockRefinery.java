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

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import com.yogpc.mc_lib.InvUtils;

public class BlockRefinery extends BlockContainer {

  public BlockRefinery() {
    super(Material.iron);
    setHardness(5F);
    setCreativeTab(QuarryPlus.ct);
    setBlockName("RefineryPlus");
    setBlockTextureName("yogpstop_qp:refineryDummy");
  }

  @Override
  public TileEntity createNewTileEntity(final World w, final int m) {
    return new TileRefinery();
  }

  private final ArrayList<ItemStack> drop = new ArrayList<ItemStack>();

  @Override
  public void breakBlock(final World world, final int x, final int y, final int z, final Block b,
      final int meta) {
    this.drop.clear();
    final TileRefinery tile = (TileRefinery) world.getTileEntity(x, y, z);
    if (world.isRemote || tile == null)
      return;
    final int count = quantityDropped(meta, 0, world.rand);
    final Item it = getItemDropped(meta, world.rand, 0);
    if (it != null)
      for (int i = 0; i < count; i++) {
        final ItemStack is = new ItemStack(it, 1, damageDropped(meta));
        EnchantmentHelper.enchantmentToIS(tile, is);
        this.drop.add(is);
      }
    super.breakBlock(world, x, y, z, b, meta);
  }

  @Override
  public ArrayList<ItemStack> getDrops(final World world, final int x, final int y, final int z,
      final int metadata, final int fortune) {
    return this.drop;
  }

  @Override
  public void onBlockPlacedBy(final World w, final int x, final int y, final int z,
      final EntityLivingBase el, final ItemStack is) {
    super.onBlockPlacedBy(w, x, y, z, el, is);
    EnchantmentHelper.init((IEnchantableTile) w.getTileEntity(x, y, z), is.getEnchantmentTagList());
    final ForgeDirection[] da =
        {ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST};
    final int di = MathHelper.floor_double((el.rotationYaw + 45.0) / 90.0) & 3;
    w.setBlockMetadataWithNotify(x, y, z, da[di].ordinal(), 1);
  }

  private static void consumeItem(final EntityPlayer ep, final ItemStack stack) {
    final ItemStack container = stack.getItem().getContainerItem(stack);
    stack.stackSize--;
    if (stack.stackSize > 0)
      ep.inventory.setInventorySlotContents(ep.inventory.currentItem, stack);
    else
      ep.inventory.setInventorySlotContents(ep.inventory.currentItem, null);
    if (container != null) {
      InvUtils.addToIInv(ep.inventory, container, ForgeDirection.UNKNOWN, true);
      if (container.stackSize > 0)
        ep.dropPlayerItemWithRandomChoice(container, false);
    }
  }

  private static boolean fill(final IFluidHandler tank, final ForgeDirection side,
      final EntityPlayer player) {
    final ItemStack current = player.getCurrentEquippedItem();
    final FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(current);
    if (liquid != null) {
      final int used = tank.fill(side, liquid, true);
      if (used > 0) {
        if (!player.capabilities.isCreativeMode) {
          consumeItem(player, current);
          player.inventory.markDirty();
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean onBlockActivated(final World world, final int x, final int y, final int z,
      final EntityPlayer ep, final int side, final float par7, final float par8, final float par9) {
    final Item equipped =
        ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
    if (BuildCraftHelper.isWrench(equipped, ep, x, y, z)) {
      if (world.isRemote)
        return true;
      final int meta = world.getBlockMetadata(x, y, z);
      switch (ForgeDirection.values()[meta]) {
        case WEST:
          world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.SOUTH.ordinal(), 3);
          break;
        case EAST:
          world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.NORTH.ordinal(), 3);
          break;
        case NORTH:
          world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.WEST.ordinal(), 3);
          break;
        case SOUTH:
        default:
          world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.EAST.ordinal(), 3);
          break;
      }
      return true;
    } else if (equipped instanceof ItemTool) {
      if (ep.getCurrentEquippedItem().getItemDamage() == 0) {
        if (world.isRemote)
          return true;
        for (final IChatComponent s : EnchantmentHelper
            .getEnchantmentsChat((IEnchantableTile) world.getTileEntity(x, y, z)))
          ep.addChatMessage(s);
        return true;
      }
    } else if (!world.isRemote) {
      if (fill((TileRefinery) world.getTileEntity(x, y, z), ForgeDirection.getOrientation(side), ep))
        return true;
    } else if (FluidContainerRegistry.isContainer(ep.getCurrentEquippedItem()))
      return true;
    return false;
  }

  @Override
  public int getRenderType() {
    return QuarryPlus.refineryRenderID;
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  @Override
  public boolean renderAsNormalBlock() {
    return false;
  }
}
