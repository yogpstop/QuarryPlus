package org.yogpstop.qp;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.yogpstop.qp.client.GuiMover;
import org.yogpstop.qp.client.GuiQuarry;

import cpw.mods.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler {
	public void registerTextures() {
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if (ID == QuarryPlus.guiIdContainerQuarry) {
			return new GuiQuarry(player, world, x, y, z);
		} else if (ID == QuarryPlus.guiIdContainerMover) {
			return new GuiMover(player, world, x, y, z);
		}

		return null;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if (ID == QuarryPlus.guiIdContainerQuarry) {
			return new ContainerQuarry(player, world, x, y, z, null);
		} else if (ID == QuarryPlus.guiIdContainerMover) {
			return new ContainerMover(player, world, x, y, z, null);
		}

		return null;
	}

	public World getClientWorld() {
		return null;
	}
}