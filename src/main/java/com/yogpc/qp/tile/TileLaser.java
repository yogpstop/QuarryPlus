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

package com.yogpc.qp.tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import com.yogpc.qp.PacketHandler;
import com.yogpc.qp.PowerManager;
import com.yogpc.qp.YogpstopPacket;
import com.yogpc.qp.compat.ILaserTargetHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileLaser extends APowerTile implements IEnchantableTile {
  public static class Position {
    public double x, y, z;
  }

  public Position[] lasers;
  private final List<Object> laserTargets = new ArrayList<Object>();

  protected byte unbreaking;
  protected byte fortune;
  protected byte efficiency;
  protected boolean silktouch;
  private double pa;

  private long from = 38669;

  public TileLaser() {
    PowerManager.configureL(this, this.efficiency, this.unbreaking);
  }

  @Override
  public void updateEntity() {
    super.updateEntity();
    if (this.worldObj.isRemote)
      return;

    if (!isValidTable() && this.worldObj.getWorldTime() % 100 == this.from % 100)
      findTable();

    if (!isValidTable() || getStoredEnergy() == 0) {
      removeLaser();
      return;
    }

    if (!isValidLaser())
      for (int i = 0; i < this.lasers.length; i++) {
        this.lasers[i] = new Position();
        this.from = this.worldObj.getWorldTime();
      }

    if (isValidLaser() && this.worldObj.getWorldTime() % 10 == this.from % 10)
      for (int i = 0; i < this.laserTargets.size(); i++) {
        this.lasers[i].x =
            ILaserTargetHelper.getXCoord(this.laserTargets.get(i)) + 0.475
                + (this.worldObj.rand.nextFloat() - 0.5) / 5F;
        this.lasers[i].y = ILaserTargetHelper.getYCoord(this.laserTargets.get(i)) + 9F / 16F;
        this.lasers[i].z =
            ILaserTargetHelper.getZCoord(this.laserTargets.get(i)) + 0.475
                + (this.worldObj.rand.nextFloat() - 0.5) / 5F;
      }

    final double power =
        PowerManager.useEnergyL(this, this.unbreaking, this.fortune, this.silktouch,
            this.efficiency);
    for (final Object lt : this.laserTargets)
      ILaserTargetHelper.receiveLaserEnergy(lt, 10 * power / this.laserTargets.size());
    pushPower(10 * power / this.laserTargets.size());
    if (this.worldObj.getWorldTime() % 20 == 7)
      PacketHandler.sendPacketToAround(new YogpstopPacket(this),
          this.worldObj.provider.dimensionId, this.xCoord, this.yCoord, this.zCoord);
  }

  private boolean isValidLaser() {
    for (final Position laser : this.lasers)
      if (laser == null)
        return false;
    return true;
  }

  protected boolean isValidTable() {
    if (this.laserTargets.size() == 0)
      return false;
    for (final Object lt : this.laserTargets)
      if (lt == null || !ILaserTargetHelper.isValid(lt))
        return false;
    return true;
  }

  protected void findTable() {
    removeLaser();
    final int meta = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);

    int minX = this.xCoord - 5 * (this.fortune + 1);
    int minY = this.yCoord - 5 * (this.fortune + 1);
    int minZ = this.zCoord - 5 * (this.fortune + 1);
    int maxX = this.xCoord + 5 * (this.fortune + 1);
    int maxY = this.yCoord + 5 * (this.fortune + 1);
    int maxZ = this.zCoord + 5 * (this.fortune + 1);

    switch (ForgeDirection.values()[meta]) {
      case WEST:
        maxX = this.xCoord;
        break;
      case EAST:
        minX = this.xCoord;
        break;
      case DOWN:
        maxY = this.yCoord;
        break;
      case UP:
        minY = this.yCoord;
        break;
      case NORTH:
        maxZ = this.zCoord;
        break;
      default:
      case SOUTH:
        minZ = this.zCoord;
        break;
    }

    this.laserTargets.clear();

    for (int x = minX; x <= maxX; ++x)
      for (int y = minY; y <= maxY; ++y)
        for (int z = minZ; z <= maxZ; ++z) {
          final TileEntity tile = this.worldObj.getTileEntity(x, y, z);
          if (ILaserTargetHelper.isInstance(tile))
            if (ILaserTargetHelper.isValid(tile))
              this.laserTargets.add(tile);
        }
    if (this.laserTargets.isEmpty())
      return;
    if (!this.silktouch) {
      final Object laserTarget =
          this.laserTargets.get(this.worldObj.rand.nextInt(this.laserTargets.size()));
      this.laserTargets.clear();
      this.laserTargets.add(laserTarget);
    }
    this.lasers = new Position[this.laserTargets.size()];
  }

  protected void removeLaser() {
    if (this.lasers != null)
      for (int i = 0; i < this.lasers.length; i++)
        this.lasers[i] = null;
    if (!this.worldObj.isRemote)
      PacketHandler.sendPacketToAround(new YogpstopPacket(this),
          this.worldObj.provider.dimensionId, this.xCoord, this.yCoord, this.zCoord);
  }

  private final double[] tp = new double[100];
  private int pi = 0;

  private void pushPower(final double received) {
    this.pa -= this.tp[this.pi];
    this.pa += received;
    this.tp[this.pi] = received;
    this.pi++;

    if (this.pi == this.tp.length)
      this.pi = 0;
  }

  public static final ResourceLocation[] LASER_TEXTURES = new ResourceLocation[] {
      new ResourceLocation("yogpstop_qp", "textures/entities/laser_1.png"),
      new ResourceLocation("yogpstop_qp", "textures/entities/laser_2.png"),
      new ResourceLocation("yogpstop_qp", "textures/entities/laser_3.png"),
      new ResourceLocation("yogpstop_qp", "textures/entities/laser_4.png"),
      new ResourceLocation("yogpstop_qp", "textures/entities/stripes.png")};

  public ResourceLocation getTexture() {
    final double avg = this.pa / 100;

    if (avg <= 1.0)
      return LASER_TEXTURES[0];
    else if (avg <= 2.0)
      return LASER_TEXTURES[1];
    else if (avg <= 3.0)
      return LASER_TEXTURES[2];
    else
      return LASER_TEXTURES[3];
  }

  @Override
  public void readFromNBT(final NBTTagCompound nbttc) {
    super.readFromNBT(nbttc);
    this.fortune = nbttc.getByte("fortune");
    this.efficiency = nbttc.getByte("efficiency");
    this.unbreaking = nbttc.getByte("unbreaking");
    this.silktouch = nbttc.getBoolean("silktouch");
    PowerManager.configureL(this, this.efficiency, this.unbreaking);
    this.pa = nbttc.getDouble("pa");
    final NBTTagList nbttl = nbttc.getTagList("lasers", 10);
    if (this.lasers == null || this.lasers.length != nbttl.tagCount())
      this.lasers = new Position[nbttl.tagCount()];
    for (int i = 0; i < nbttl.tagCount(); i++) {
      if (this.lasers[i] == null)
        this.lasers[i] = new Position();
      final NBTTagCompound lc = nbttl.getCompoundTagAt(i);
      this.lasers[i].x = lc.getDouble("x");
      this.lasers[i].y = lc.getDouble("y");
      this.lasers[i].z = lc.getDouble("z");
    }
  }

  @Override
  public void writeToNBT(final NBTTagCompound nbttc) {
    super.writeToNBT(nbttc);
    nbttc.setByte("fortune", this.fortune);
    nbttc.setByte("efficiency", this.efficiency);
    nbttc.setByte("unbreaking", this.unbreaking);
    nbttc.setBoolean("silktouch", this.silktouch);
    nbttc.setDouble("pa", this.pa);
    final NBTTagList nbttl = new NBTTagList();
    if (this.lasers != null)
      for (final Position l : this.lasers)
        if (l != null) {
          final NBTTagCompound lc = new NBTTagCompound();
          lc.setDouble("x", l.x);
          lc.setDouble("y", l.y);
          lc.setDouble("z", l.z);
          nbttl.appendTag(lc);
        }
    nbttc.setTag("lasers", nbttl);
  }

  @Override
  public void invalidate() {
    super.invalidate();
    removeLaser();
  }

  @Override
  public Map<Integer, Byte> get() {
    final Map<Integer, Byte> ret = new HashMap<Integer, Byte>();
    if (this.efficiency > 0)
      ret.put(Integer.valueOf(Enchantment.efficiency.effectId), Byte.valueOf(this.efficiency));
    if (this.fortune > 0)
      ret.put(Integer.valueOf(Enchantment.fortune.effectId), Byte.valueOf(this.fortune));
    if (this.unbreaking > 0)
      ret.put(Integer.valueOf(Enchantment.unbreaking.effectId), Byte.valueOf(this.unbreaking));
    if (this.silktouch)
      ret.put(Integer.valueOf(Enchantment.silkTouch.effectId), Byte.valueOf((byte) 1));
    return ret;
  }

  @Override
  public void set(final int id, final byte val) {
    if (id == Enchantment.efficiency.effectId)
      this.efficiency = val;
    else if (id == Enchantment.fortune.effectId)
      this.fortune = val;
    else if (id == Enchantment.unbreaking.effectId)
      this.unbreaking = val;
    else if (id == Enchantment.silkTouch.effectId && val > 0)
      this.silktouch = true;
  }

  @Override
  public void G_reinit() {
    PowerManager.configureL(this, this.efficiency, this.unbreaking);
  }

  @Override
  public void S_recievePacket(final byte id, final byte[] data, final EntityPlayer ep) {}

  @Override
  public void C_recievePacket(final byte id, final byte[] data, final EntityPlayer ep) {}

  @Override
  @SideOnly(Side.CLIENT)
  public AxisAlignedBB getRenderBoundingBox() {
    final AxisAlignedBB aabb =
        AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1,
            this.yCoord + 1, this.zCoord + 1);
    if (this.lasers != null)
      for (final Position p : this.lasers) {
        if (p == null)
          continue;
        final double xn = p.x - 0.0625;
        final double xx = xn + 0.125;
        final double zn = p.z - 0.0625;
        final double zx = zn + 0.125;
        if (xn < aabb.minX)
          aabb.minX = xn;
        if (xx > aabb.maxX)
          aabb.maxX = xx;
        if (p.y < aabb.minY)
          aabb.minY = p.y;
        if (p.y > aabb.maxY)
          aabb.maxY = p.y;
        if (zn < aabb.minZ)
          aabb.minZ = zn;
        if (zx > aabb.maxZ)
          aabb.maxZ = zx;
      }
    return aabb;
  }
}
