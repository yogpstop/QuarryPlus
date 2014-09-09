package com.yogpc.qp;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFrame extends Block {
  private final boolean[] sides = new boolean[6];

  public BlockFrame() {
    super(Material.glass);
    setSides(true, true, true, true, true, true);
    setHardness(0.5F);
    setTickRandomly(true);
    setBlockTextureName("yogpstop_qp:blockFrame");
  }

  @Override
  public void updateTick(final World world, final int i, final int j, final int k,
      final Random random) {
    if (world.isRemote)
      return;
    final int meta = world.getBlockMetadata(i, j, k);
    if (meta != 0 && random.nextInt(10) > 5)
      world.setBlockToAir(i, j, k);
  }

  public void setSides(final boolean yn, final boolean yp, final boolean zn, final boolean zp,
      final boolean xn, final boolean xp) {
    this.sides[0] = yn;
    this.sides[1] = yp;
    this.sides[2] = zn;
    this.sides[3] = zp;
    this.sides[4] = xn;
    this.sides[5] = xp;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean shouldSideBeRendered(final IBlockAccess w, final int x, final int y, final int z,
      final int s) {
    return this.sides[s];
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
  public Item getItemDropped(final int i, final Random random, final int j) {
    return null;
  }

  @Override
  public int getRenderType() {
    return QuarryPlus.frameRenderID;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public AxisAlignedBB getSelectedBoundingBoxFromPool(final World world, final int i, final int j,
      final int k) {
    return getCollisionBoundingBoxFromPool(world, i, j, k);
  }

  @Override
  public AxisAlignedBB getCollisionBoundingBoxFromPool(final World world, final int i, final int j,
      final int k) {
    float xMin = 0.25F, xMax = 0.75F, yMin = 0.25F, yMax = 0.75F, zMin = 0.25F, zMax = 0.75F;
    if (world.getBlock(i - 1, j, k) == this)
      xMin = 0;
    if (world.getBlock(i + 1, j, k) == this)
      xMax = 1;
    if (world.getBlock(i, j - 1, k) == this)
      yMin = 0;
    if (world.getBlock(i, j + 1, k) == this)
      yMax = 1;
    if (world.getBlock(i, j, k - 1) == this)
      zMin = 0;
    if (world.getBlock(i, j, k + 1) == this)
      zMax = 1;
    return AxisAlignedBB.getBoundingBox(i + xMin, j + yMin, k + zMin, i + xMax, j + yMax, k + zMax);
  }

  @Override
  public MovingObjectPosition collisionRayTrace(final World world, final int i, final int j,
      final int k, final Vec3 vec3d, final Vec3 vec3d1) {
    float xMin = 0.25F, xMax = 0.75F, yMin = 0.25F, yMax = 0.75F, zMin = 0.25F, zMax = 0.75F;
    if (world.getBlock(i - 1, j, k) == this)
      xMin = 0;
    if (world.getBlock(i + 1, j, k) == this)
      xMax = 1;
    if (world.getBlock(i, j - 1, k) == this)
      yMin = 0;
    if (world.getBlock(i, j + 1, k) == this)
      yMax = 1;
    if (world.getBlock(i, j, k - 1) == this)
      zMin = 0;
    if (world.getBlock(i, j, k + 1) == this)
      zMax = 1;
    setBlockBounds(xMin, yMin, zMin, xMax, yMax, zMax);
    final MovingObjectPosition r = super.collisionRayTrace(world, i, j, k, vec3d, vec3d1);
    setBlockBounds(0, 0, 0, 1, 1, 1);
    return r;
  }

  @Override
  public void addCollisionBoxesToList(final World world, final int i, final int j, final int k,
      final AxisAlignedBB axisalignedbb, final List arraylist, final Entity par7Entity) {
    setBlockBounds(0.25F, 0.25F, 0.25F, 0.75F, 0.75F, 0.75F);
    super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);

    if (world.getBlock(i - 1, j, k) == this) {
      setBlockBounds(0, 0.25F, 0.25F, 0.75F, 0.75F, 0.75F);
      super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
    }

    if (world.getBlock(i + 1, j, k) == this) {
      setBlockBounds(0.25F, 0.25F, 0.25F, 1, 0.75F, 0.75F);
      super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
    }

    if (world.getBlock(i, j - 1, k) == this) {
      setBlockBounds(0.25F, 0, 0.25F, 0.75F, 0.75F, 0.75F);
      super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
    }

    if (world.getBlock(i, j + 1, k) == this) {
      setBlockBounds(0.25F, 0.25F, 0.25F, 0.75F, 1, 0.75F);
      super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
    }

    if (world.getBlock(i, j, k - 1) == this) {
      setBlockBounds(0.25F, 0.25F, 1, 0.75F, 0.75F, 0.75F);
      super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
    }

    if (world.getBlock(i, j, k + 1) == this) {
      setBlockBounds(0.25F, 0.25F, 0.25F, 0.75F, 0.75F, 1);
      super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
    }

    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
  }
}
