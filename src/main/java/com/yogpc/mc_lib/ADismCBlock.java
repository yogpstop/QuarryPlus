package com.yogpc.mc_lib;

import java.util.ArrayList;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cofh.api.block.IDismantleable;
import cpw.mods.fml.common.Optional;

@Optional.Interface(iface = "cofh.api.block.IDismantleable", modid = "CoFHAPI|block")
public abstract class ADismCBlock extends BlockContainer implements IDismantleable {
  protected ADismCBlock(final Material m) {
    super(m);
  }

  @Override
  public boolean canDismantle(final EntityPlayer arg0, final World arg1, final int arg2,
      final int arg3, final int arg4) {
    return true;
  }

  @Override
  public ArrayList<ItemStack> dismantleBlock(final EntityPlayer e, final World w, final int x,
      final int y, final int z, final boolean toinv) {
    final ArrayList<ItemStack> ret = getDrops(w, x, y, z, w.getBlockMetadata(x, y, z), 0);
    w.setBlockToAir(x, y, z);
    if (!toinv)
      for (final ItemStack is : ret) {
        final float f = 0.7F;
        final double d0 = w.rand.nextFloat() * f + (1.0F - f) * 0.5D;
        final double d1 = w.rand.nextFloat() * f + (1.0F - f) * 0.5D;
        final double d2 = w.rand.nextFloat() * f + (1.0F - f) * 0.5D;
        final EntityItem entityitem = new EntityItem(w, x + d0, y + d1, z + d2, is);
        entityitem.delayBeforeCanPickup = 10;
        w.spawnEntityInWorld(entityitem);
      }
    return ret;
  }
}
