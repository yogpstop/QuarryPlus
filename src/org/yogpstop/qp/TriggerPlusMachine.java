package org.yogpstop.qp;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.TriggerParameter;
import buildcraft.core.triggers.ActionTriggerIconProvider;

public class TriggerPlusMachine implements ITrigger {

	boolean active;
	int id;

	public TriggerPlusMachine(int pid, boolean active) {
		this.id = pid;
		this.active = active;
		ActionManager.triggers.put(getUniqueTag(), this);
	}

	@Override
	public String getDescription() {
		if (this.active) return StatCollector.translateToLocal("trigger.plus_working");
		return StatCollector.translateToLocal("trigger.plus_done");
	}

	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		if (tile instanceof TileQuarry) {
			if (this.active) return ((TileQuarry) tile).G_getNow() != TileQuarry.NONE;
			return ((TileQuarry) tile).G_getNow() == TileQuarry.NONE;
		} else if (tile instanceof TileMiningWell) {
			if (this.active) return ((TileMiningWell) tile).G_isWorking();
			return !((TileMiningWell) tile).G_isWorking();
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon() {
		if (this.active) return ActionTriggerIconProvider.INSTANCE.getIcon(ActionTriggerIconProvider.Trigger_Machine_Active);
		return ActionTriggerIconProvider.INSTANCE.getIcon(ActionTriggerIconProvider.Trigger_Machine_Inactive);
	}

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public ITriggerParameter createParameter() {
		return new TriggerParameter();
	}

	@Override
	public int getLegacyId() {
		return this.id;
	}

	@Override
	public String getUniqueTag() {
		return this.active ? "PlusActive" : "PlusDeactive";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {}

}
