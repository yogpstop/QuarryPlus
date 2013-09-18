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

/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package org.yogpstop.qp;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.core.EntityBlock;
import buildcraft.factory.FactoryProxy;

public class EntityMechanicalArm extends Entity {
	EntityBlock xArm, yArm, zArm, head;

	boolean inProgressionXZ = false;
	boolean inProgressionY = false;

	protected TileQuarry parent;

	private double armSizeX;
	private double armSizeZ;
	private double xRoot;
	private double yRoot;
	private double zRoot;

	private int headX, headY, headZ;

	public EntityMechanicalArm(World world) {
		super(world);
		makeParts(world);
	}

	public EntityMechanicalArm(World world, double i, double j, double k, double width, double height, TileQuarry parent) {
		this(world);
		setPositionAndRotation(parent.xCoord, parent.yCoord, parent.zCoord, 0, 0);
		this.xRoot = i;
		this.yRoot = j;
		this.zRoot = k;
		this.motionX = 0.0;
		this.motionY = 0.0;
		this.motionZ = 0.0;
		setArmSize(width, height);
		setHead(i, j - 2, k);

		this.noClip = true;

		this.parent = parent;
		parent.setArm(this);
		updatePosition();
	}

	void setHead(double x, double y, double z) {
		this.headX = (int) (x * 32D);
		this.headY = (int) (y * 32D);
		this.headZ = (int) (z * 32D);
	}

	private void setArmSize(double x, double z) {
		this.armSizeX = x;
		this.xArm.iSize = x;
		this.armSizeZ = z;
		this.zArm.kSize = z;
		updatePosition();
	}

	private void makeParts(World world) {
		this.xArm = FactoryProxy.proxy.newDrill(world, 0, 0, 0, 1, 0.5, 0.5);
		this.yArm = FactoryProxy.proxy.newDrill(world, 0, 0, 0, 0.5, 1, 0.5);
		this.zArm = FactoryProxy.proxy.newDrill(world, 0, 0, 0, 0.5, 0.5, 1);

		this.head = FactoryProxy.proxy.newDrillHead(world, 0, 0, 0, 0.2, 1, 0.2);
		this.head.shadowSize = 1.0F;

		world.spawnEntityInWorld(this.xArm);
		world.spawnEntityInWorld(this.yArm);
		world.spawnEntityInWorld(this.zArm);
		world.spawnEntityInWorld(this.head);
	}

	@Override
	protected void entityInit() {}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		this.xRoot = nbttagcompound.getDouble("xRoot");
		this.yRoot = nbttagcompound.getDouble("yRoot");
		this.zRoot = nbttagcompound.getDouble("zRoot");
		this.armSizeX = nbttagcompound.getDouble("armSizeX");
		this.armSizeZ = nbttagcompound.getDouble("armSizeZ");
		setArmSize(this.armSizeX, this.armSizeZ);
		updatePosition();
	}

	private void findAndJoinQuarry() {
		TileEntity te = this.worldObj.getBlockTileEntity((int) this.posX, (int) this.posY, (int) this.posZ);
		if (te != null && te instanceof TileQuarry) {
			this.parent = (TileQuarry) te;
			this.parent.setArm(this);
		} else {
			setDead();
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setDouble("xRoot", this.xRoot);
		nbttagcompound.setDouble("yRoot", this.yRoot);
		nbttagcompound.setDouble("zRoot", this.zRoot);
		nbttagcompound.setDouble("armSizeX", this.armSizeX);
		nbttagcompound.setDouble("armSizeZ", this.armSizeZ);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		updatePosition();
		if (this.parent == null) {
			findAndJoinQuarry();
		}

		if (this.parent == null) {
			setDead();
			return;
		}
	}

	public void updatePosition() {
		double[] head1 = getHead();
		this.xArm.setPosition(this.xRoot, this.yRoot, head1[2] + 0.25);
		this.yArm.jSize = this.yRoot - head1[1] - 1;
		this.yArm.setPosition(head1[0] + 0.25, head1[1] + 1, head1[2] + 0.25);
		this.zArm.setPosition(head1[0] + 0.25, this.yRoot, this.zRoot);
		this.head.setPosition(head1[0] + 0.4, head1[1], head1[2] + 0.4);
	}

	@Override
	public void setDead() {
		if (this.worldObj != null && this.worldObj.isRemote) {
			this.xArm.setDead();
			this.yArm.setDead();
			this.zArm.setDead();
			this.head.setDead();
		}
		super.setDead();
	}

	private double[] getHead() {
		return new double[] { this.headX / 32D, this.headY / 32D, this.headZ / 32D };
	}
}
