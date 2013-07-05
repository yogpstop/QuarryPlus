package org.yogpstop.qp;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.ForgeDirection;

import buildcraft.BuildCraftCore;

import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.TriggerParameter;

import buildcraft.core.triggers.ActionTriggerIconProvider;

public class TriggerQuarryPlus implements ITrigger {

	boolean active;
	int id;

	public TriggerQuarryPlus(int pid, boolean active) {
		this.id = pid;
		this.active = active;
		ActionManager.triggers[pid] = this;
	}

	@Override
	public String getDescription() {
		if (this.active) return "QuarryPlus is working";
		return "QuarryPlus's work is done";
	}

	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		if (tile instanceof TileQuarry) {
			if (this.active) return ((TileQuarry) tile).getNow() != TileQuarry.NONE;
			return ((TileQuarry) tile).getNow() == TileQuarry.NONE;
		}
		return false;
	}

	@Override
	public int getIconIndex() {
		if (this.active) return ActionTriggerIconProvider.Trigger_Machine_Active;
		return ActionTriggerIconProvider.Trigger_Machine_Inactive;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftCore.instance.actionTriggerIconProvider;
	}

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public ITriggerParameter createParameter() {
		return new TriggerParameter();
	}

}
