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

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMarker extends BlockContainer {

  public BlockMarker() {
    super(Material.circuits);
    setLightLevel(0.5F);
    setCreativeTab(QuarryPlus.ct);
    setBlockName("MarkerPlus");
  }

  @Override
  public int getRenderType() {
    return QuarryPlus.markerRenderID;
  }

  @Override
  public void setBlockBoundsBasedOnState(final IBlockAccess world, final int x, final int y,
      final int z) {
    final float w = 0.15F;
    final float h = 0.65F;
    final ForgeDirection dir = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));
    switch (dir) {
      case DOWN:
        setBlockBounds(0.5F - w, 1F - h, 0.5F - w, 0.5F + w, 1F, 0.5F + w);
        break;
      case UP:
        setBlockBounds(0.5F - w, 0F, 0.5F - w, 0.5F + w, h, 0.5F + w);
        break;
      case SOUTH:
        setBlockBounds(0.5F - w, 0.5F - w, 0F, 0.5F + w, 0.5F + w, h);
        break;
      case NORTH:
        setBlockBounds(0.5F - w, 0.5F - w, 1 - h, 0.5F + w, 0.5F + w, 1);
        break;
      case EAST:
        setBlockBounds(0F, 0.5F - w, 0.5F - w, h, 0.5F + w, 0.5F + w);
        break;
      default:
        setBlockBounds(1 - h, 0.5F - w, 0.5F - w, 1F, 0.5F + w, 0.5F + w);
        break;
    }
  }

  @Override
  public AxisAlignedBB getCollisionBoundingBoxFromPool(final World world, final int i, final int j,
      final int k) {
    return null;
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  @Override
  public boolean renderAsNormalBlock() {
    return false;
  }

  @Override
  public boolean canPlaceBlockOnSide(final World world, final int x, final int y, final int z,
      final int side) {
    final ForgeDirection dir = ForgeDirection.getOrientation(side);
    return world.isSideSolid(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, dir.getOpposite());
  }

  @Override
  public int onBlockPlaced(final World world, final int x, final int y, final int z,
      final int side, final float par6, final float par7, final float par8, final int meta) {
    return side;
  }

  @Override
  public void onBlockAdded(final World world, final int x, final int y, final int z) {
    super.onBlockAdded(world, x, y, z);
    dropTorchIfCantStay(world, x, y, z);
  }

  private void dropTorchIfCantStay(final World w, final int x, final int y, final int z) {
    final int m = w.getBlockMetadata(x, y, z);
    if (!canPlaceBlockOnSide(w, x, y, z, m)) {
      dropBlockAsItem(w, x, y, z, m, 0);
      w.setBlockToAir(x, y, z);
    }
  }

  @Override
  public TileEntity createNewTileEntity(final World w, final int m) {
    return new TileMarker();
  }

  @Override
  public void onNeighborBlockChange(final World w, final int x, final int y, final int z,
      final Block b) {
    ((TileMarker) w.getTileEntity(x, y, z)).G_updateSignal();
    dropTorchIfCantStay(w, x, y, z);
  }

  @Override
  public boolean onBlockActivated(final World world, final int x, final int y, final int z,
      final EntityPlayer ep, final int par6, final float par7, final float par8, final float par9) {
    if (!world.isRemote) {
      final Item equipped =
          ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
      if (equipped instanceof ItemTool && ep.getCurrentEquippedItem().getItemDamage() == 0) {
        final TileMarker.Link l = ((TileMarker) world.getTileEntity(x, y, z)).link;
        if (l == null)
          return true;
        ep.addChatMessage(new ChatComponentTranslation("chat.markerarea"));
        final StringBuilder sb = new StringBuilder();
        sb.append("x:").append(l.xn).append(" y:").append(l.yn).append(" z:").append(l.zn)
            .append(" - ");
        sb.append("x:").append(l.xx).append(" y:").append(l.yx).append(" z:").append(l.zx);
        ep.addChatMessage(new ChatComponentText(sb.toString()));// NP coord info
        return true;
      }
      ((TileMarker) world.getTileEntity(x, y, z)).S_tryConnection();
    }
    return true;
  }

  @Override
  public void onPostBlockPlaced(final World world, final int x, final int y, final int z,
      final int meta) {
    ((TileMarker) world.getTileEntity(x, y, z)).requestTicket();
    super.onPostBlockPlaced(world, x, y, z, meta);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerBlockIcons(final IIconRegister par1IconRegister) {
    this.blockIcon = par1IconRegister.registerIcon("yogpstop_qp:marker");
  }
}
