package com.yogpc.qp.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.tile.TileWorkbench;

public class BlockWorkbench extends ADismCBlock {
  private final Random random = new Random();

  public BlockWorkbench() {
    super(Material.iron);
    setHardness(3F);
    setCreativeTab(QuarryPlusI.ct);
    setBlockName("WorkbenchPlus");
    setBlockTextureName("yogpstop_qp:workbench");
  }

  @Override
  public void breakBlock(final World w, final int x, final int y, final int z, final Block b,
      final int m) {
    final TileWorkbench t = (TileWorkbench) w.getTileEntity(x, y, z);
    if (t != null) {
      for (int i = 0; i < 27; i++) {
        final ItemStack is = t.inv[i];
        if (is != null) {
          final float f = this.random.nextFloat() * 0.8F + 0.1F;
          final float f1 = this.random.nextFloat() * 0.8F + 0.1F;
          final float f2 = this.random.nextFloat() * 0.8F + 0.1F;
          while (is.stackSize > 0) {
            int k1 = this.random.nextInt(21) + 10;
            if (k1 > is.stackSize)
              k1 = is.stackSize;
            is.stackSize -= k1;
            final EntityItem e =
                new EntityItem(w, x + f, y + f1, z + f2, new ItemStack(is.getItem(), k1,
                    is.getItemDamage()));
            if (is.hasTagCompound())
              e.getEntityItem().setTagCompound((NBTTagCompound) is.getTagCompound().copy());
            final float f3 = 0.05F;
            e.motionX = (float) this.random.nextGaussian() * f3;
            e.motionY = (float) this.random.nextGaussian() * f3 + 0.2F;
            e.motionZ = (float) this.random.nextGaussian() * f3;
            w.spawnEntityInWorld(e);
          }
        }
      }
      w.func_147453_f(x, y, z, b);
    }
    super.breakBlock(w, x, y, z, b, m);
  }

  @Override
  public TileEntity createNewTileEntity(final World p_149915_1_, final int p_149915_2_) {
    return new TileWorkbench();
  }

  @Override
  public boolean onBlockActivated(final World w, final int x, final int y, final int z,
      final EntityPlayer e, final int par6, final float par7, final float par8, final float par9) {
    if (!w.isRemote)
      e.openGui(QuarryPlus.I, QuarryPlusI.guiIdWorkbench, w, x, y, z);
    return true;
  }
}
