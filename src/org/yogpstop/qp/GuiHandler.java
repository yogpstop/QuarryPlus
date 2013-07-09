package org.yogpstop.qp;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.yogpstop.qp.client.GuiMiningWell;
import org.yogpstop.qp.client.GuiMover;
import org.yogpstop.qp.client.GuiQuarry;
import org.yogpstop.qp.client.GuiList;

import static org.yogpstop.qp.QuarryPlus.*;

import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
		case guiIdContainerMiner:
			if (world.getBlockId(x, y, z) == blockQuarry.blockID) return new GuiQuarry(player, world, x, y, z);
			return new GuiMiningWell(player, world, x, y, z);
		case guiIdContainerMover:
			return new GuiMover(player, world, x, y, z);
		case guiIdGuiQuarryFortuneList:
			return new GuiList((byte) 1, (TileBasic) world.getBlockTileEntity(x, y, z));
		case guiIdGuiQuarrySilktouchList:
			return new GuiList((byte) 2, (TileBasic) world.getBlockTileEntity(x, y, z));
		}

		return null;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
		case guiIdContainerMiner:
			return new ContainerPlayer(player, world, x, y, z);
		case guiIdContainerMover:
			return new ContainerMover(player, world, x, y, z, null);
		}

		return new ContainerDummy(x, y, z);
	}
}
