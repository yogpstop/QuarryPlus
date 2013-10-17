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

package org.yogpstop.qp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.api.recipes.RefineryRecipes;
import buildcraft.api.recipes.RefineryRecipes.Recipe;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;

public class TileRefinery extends APacketTile implements IFluidHandler, IPowerReceptor, IEnchantableTile {
	public FluidStack src1, src2, res;
	private PowerHandler pp = new PowerHandler(this, Type.MACHINE);
	private int ticks;

	public float animationSpeed = 1;
	private int animationStage = 0;

	protected byte unbreaking;
	protected byte fortune;
	protected boolean silktouch;
	protected byte efficiency;

	public int buf;

	@Override
	public void G_reinit() {
		PowerManager.configureR(this.pp, this.efficiency, this.unbreaking);
		this.buf = (int) (FluidContainerRegistry.BUCKET_VOLUME * 4 * Math.pow(1.3, this.fortune));
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttc) {
		super.readFromNBT(nbttc);
		this.silktouch = nbttc.getBoolean("silktouch");
		this.fortune = nbttc.getByte("fortune");
		this.efficiency = nbttc.getByte("efficiency");
		this.unbreaking = nbttc.getByte("unbreaking");
		this.pp.readFromNBT(nbttc);
		this.src1 = FluidStack.loadFluidStackFromNBT(nbttc.getCompoundTag("src1"));
		this.src2 = FluidStack.loadFluidStackFromNBT(nbttc.getCompoundTag("src2"));
		this.res = FluidStack.loadFluidStackFromNBT(nbttc.getCompoundTag("res"));
		this.animationSpeed = nbttc.getFloat("animationSpeed");
		this.animationStage = nbttc.getInteger("animationStage");
		this.buf = (int) (FluidContainerRegistry.BUCKET_VOLUME * 4 * Math.pow(1.3, this.fortune));
		PowerManager.configureR(this.pp, this.efficiency, this.unbreaking);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttc) {
		super.writeToNBT(nbttc);
		nbttc.setBoolean("silktouch", this.silktouch);
		nbttc.setByte("fortune", this.fortune);
		nbttc.setByte("efficiency", this.efficiency);
		nbttc.setByte("unbreaking", this.unbreaking);
		this.pp.writeToNBT(nbttc);
		if (this.src1 != null) nbttc.setCompoundTag("src1", this.src1.writeToNBT(new NBTTagCompound()));
		if (this.src2 != null) nbttc.setCompoundTag("src2", this.src2.writeToNBT(new NBTTagCompound()));
		if (this.res != null) nbttc.setCompoundTag("res", this.res.writeToNBT(new NBTTagCompound()));
		nbttc.setFloat("animationSpeed", this.animationSpeed);
		nbttc.setInteger("animationStage", this.animationStage);
	}

	@Override
	public void updateEntity() {
		if (this.worldObj.isRemote) {
			simpleAnimationIterate();
			return;
		}
		if (this.worldObj.getWorldTime() % 20 == 7) {
			PacketDispatcher.sendPacketToAllAround(this.xCoord, this.yCoord, this.zCoord, 256, this.worldObj.provider.dimensionId,
					PacketHandler.getPacketFromNBT(this));
		}
		this.ticks++;
		for (int i = this.efficiency + 1; i > 0; i--) {
			Recipe r = RefineryRecipes.findRefineryRecipe(this.src1, this.src2);
			if (r == null) {
				decreaseAnimation();
				this.ticks = 0;
				return;
			}
			if (this.res != null && r.result.amount > (this.buf - this.res.amount)) {
				decreaseAnimation();
				return;
			}
			if (r.delay > this.ticks) return;
			if (i == 1) this.ticks = 0;
			if (!PowerManager.useEnergyR(this.pp, r.energy, this.unbreaking)) {
				decreaseAnimation();
				return;
			}
			increaseAnimation();
			if (r.ingredient1.isFluidEqual(this.src1)) this.src1.amount -= r.ingredient1.amount;
			else this.src2.amount -= r.ingredient1.amount;
			if (r.ingredient2 != null) {
				if (r.ingredient2.isFluidEqual(this.src2)) this.src2.amount -= r.ingredient2.amount;
				else this.src1.amount -= r.ingredient2.amount;
			}
			if (this.src1 != null && this.src1.amount == 0) this.src1 = null;
			if (this.src2 != null && this.src2.amount == 0) this.src2 = null;
			if (this.res == null) this.res = r.result.copy();
			else this.res.amount += r.result.amount;
		}
	}

	public int getAnimationStage() {
		return this.animationStage;
	}

	private void simpleAnimationIterate() {
		if (this.animationSpeed > 1) {
			this.animationStage += this.animationSpeed;

			if (this.animationStage > 300) {
				this.animationStage = 100;
			}
		} else if (this.animationStage > 0) {
			this.animationStage--;
		}
	}

	private void sendNowPacket() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeInt(this.xCoord);
			dos.writeInt(this.yCoord);
			dos.writeInt(this.zCoord);
			dos.writeByte(PacketHandler.StC_NOW);
			dos.writeFloat(this.animationSpeed);
			PacketDispatcher.sendPacketToAllAround(this.xCoord, this.yCoord, this.zCoord, 256, this.worldObj.provider.dimensionId,
					PacketHandler.composeTilePacket(bos));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void increaseAnimation() {
		float prev = this.animationSpeed;
		if (this.animationSpeed < 2) {
			this.animationSpeed = 2;
		} else if (this.animationSpeed <= 5) {
			this.animationSpeed += 0.1;
		}

		this.animationStage += this.animationSpeed;

		if (this.animationStage > 300) {
			this.animationStage = 100;
		}
		if (this.animationSpeed != prev) sendNowPacket();
	}

	private void decreaseAnimation() {
		float prev = this.animationSpeed;
		if (this.animationSpeed >= 1) {
			this.animationSpeed -= 0.1;

			this.animationStage += this.animationSpeed;

			if (this.animationStage > 300) {
				this.animationStage = 100;
			}
		} else {
			if (this.animationStage > 0) {
				this.animationStage--;
			}
		}
		if (this.animationSpeed != prev) sendNowPacket();
	}

	@Override
	protected void S_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {

	}

	@Override
	protected void C_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		switch (pattern) {
		case PacketHandler.StC_NOW:
			this.animationSpeed = data.readFloat();
			break;
		}
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return this.pp.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider) {}

	@Override
	public World getWorld() {
		return this.worldObj;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (resource.isFluidEqual(this.src1)) {
			int ret = Math.min(this.buf - this.src1.amount, resource.amount);
			if (doFill) this.src1.amount += ret;
			return ret;
		} else if (resource.isFluidEqual(this.src2)) {
			int ret = Math.min(this.buf - this.src2.amount, resource.amount);
			if (doFill) this.src2.amount += ret;
			return ret;
		} else if (this.src1 == null) {
			int ret = Math.min(this.buf, resource.amount);
			if (doFill) {
				this.src1 = resource.copy();
				this.src1.amount = ret;
			}
			return ret;
		} else if (this.src2 == null) {
			int ret = Math.min(this.buf, resource.amount);
			if (doFill) {
				this.src2 = resource.copy();
				this.src2.amount = ret;
			}
			return ret;
		}
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		// if (resource == null) return null;
		if (resource.equals(this.res)) return drain(from, resource.amount, doDrain);
		if (resource.equals(this.src1)) {
			FluidStack ret = this.src1.copy();
			ret.amount = Math.min(resource.amount, ret.amount);
			if (doDrain) {
				this.src1.amount -= ret.amount;
				if (this.src1.amount == 0) this.src1 = null;
			}
			return ret;
		}
		if (resource.equals(this.src2)) {
			FluidStack ret = this.src2.copy();
			ret.amount = Math.min(resource.amount, ret.amount);
			if (doDrain) {
				this.src2.amount -= ret.amount;
				if (this.src2.amount == 0) this.src2 = null;
			}
			return ret;
		}
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if (this.res == null) return null;
		FluidStack ret = this.res.copy();
		ret.amount = Math.min(maxDrain, ret.amount);
		if (doDrain) {
			this.res.amount -= ret.amount;
			if (this.res.amount == 0) this.res = null;
		}
		return ret;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[] { new FluidTankInfo(this.src1, this.buf), new FluidTankInfo(this.src2, this.buf), new FluidTankInfo(this.res, this.buf) };
	}

	@Override
	public byte getEfficiency() {
		return this.efficiency;
	}

	@Override
	public byte getFortune() {
		return this.fortune;
	}

	@Override
	public byte getUnbreaking() {
		return this.unbreaking;
	}

	@Override
	public boolean getSilktouch() {
		return this.silktouch;
	}

	@Override
	public void set(byte pefficiency, byte pfortune, byte punbreaking, boolean psilktouch) {
		this.efficiency = pefficiency;
		this.fortune = pfortune;
		this.unbreaking = punbreaking;
		this.silktouch = psilktouch;
	}
}
