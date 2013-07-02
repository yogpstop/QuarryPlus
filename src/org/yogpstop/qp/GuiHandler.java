package org.yogpstop.qp;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.yogpstop.qp.client.GuiMover;
import org.yogpstop.qp.client.GuiQuarry;
import org.yogpstop.qp.client.GuiQuarryList;

import static org.yogpstop.qp.QuarryPlus.*;

import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
		case guiIdContainerQuarry:
			return new GuiQuarry(player, world, x, y, z);
		case guiIdContainerMover:
			return new GuiMover(player, world, x, y, z);
		case guiIdGuiQuarryFortuneList:
			return new GuiQuarryList((byte) 1, (TileQuarry) world.getBlockTileEntity(x, y, z));
		case guiIdGuiQuarrySilktouchList:
			return new GuiQuarryList((byte) 2, (TileQuarry) world.getBlockTileEntity(x, y, z));
		}

		return null;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
		case guiIdContainerQuarry:
			return new ContainerQuarry(player, world, x, y, z);
		case guiIdContainerMover:
			return new ContainerMover(player, world, x, y, z, null);
		}

		return new ContainerDummy(x, y, z);
	}
}
