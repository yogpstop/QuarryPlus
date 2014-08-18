package com.yogpc.qp;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import buildcraft.api.tools.IToolWrench;

public class BuildCraftHelper {
	public static final boolean isWrench(Item equipped, EntityPlayer ep, int x, int y, int z) {
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(ep, x, y, z)) {
			((IToolWrench) equipped).wrenchUsed(ep, x, y, z);
			return true;
		}
		return false;
	}
}
