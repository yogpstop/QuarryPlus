package com.yogpc.qp.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityLaser extends Entity {
  public static final int DRILL = 0;
  public static final int DRILL_HEAD = 1;
  public static final int BLUE_LASER = 2;
  public static final int RED_LASER = 3;

  public double iSize, jSize, kSize;
  public final int texture;

  public EntityLaser(final World world, final double i, final double j, final double k,
      final double iSize, final double jSize, final double kSize, final int tex) {
    super(world);
    this.preventEntitySpawning = false;
    this.noClip = true;
    this.isImmuneToFire = true;
    this.iSize = iSize;
    this.jSize = jSize;
    this.kSize = kSize;
    setPositionAndRotation(i, j, k, 0, 0);
    this.motionX = 0.0;
    this.motionY = 0.0;
    this.motionZ = 0.0;
    this.texture = tex;
  }

  @Override
  public void setPosition(final double d, final double d1, final double d2) {
    super.setPosition(d, d1, d2);
    this.boundingBox.minX = this.posX;
    this.boundingBox.minY = this.posY;
    this.boundingBox.minZ = this.posZ;

    this.boundingBox.maxX = this.posX + this.iSize;
    this.boundingBox.maxY = this.posY + this.jSize;
    this.boundingBox.maxZ = this.posZ + this.kSize;
  }

  @Override
  public void moveEntity(final double d, final double d1, final double d2) {
    setPosition(this.posX + d, this.posY + d1, this.posZ + d2);
  }

  @Override
  protected void entityInit() {}

  @Override
  protected void readEntityFromNBT(final NBTTagCompound data) {
    this.iSize = data.getDouble("iSize");
    this.jSize = data.getDouble("jSize");
    this.kSize = data.getDouble("kSize");
  }

  @Override
  protected void writeEntityToNBT(final NBTTagCompound data) {
    data.setDouble("iSize", this.iSize);
    data.setDouble("jSize", this.jSize);
    data.setDouble("kSize", this.kSize);
  }
}
