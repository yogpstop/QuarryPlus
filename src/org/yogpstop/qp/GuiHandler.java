package org.yogpstop.qp;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.yogpstop.qp.client.GuiInfMJSrc;
import org.yogpstop.qp.client.GuiMover;
import org.yogpstop.qp.client.GuiList;

import static org.yogpstop.qp.QuarryPlus.*;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
		case guiIdMover:
			return new GuiMover(player, world, x, y, z);
		case guiIdFortuneList:
			return new GuiList((byte) 1, (TileBasic) world.getBlockTileEntity(x, y, z));
		case guiIdSilktouchList:
			return new GuiList((byte) 2, (TileBasic) world.getBlockTileEntity(x, y, z));
		case guiIdInfMJSrc:
			return new GuiInfMJSrc(x, y, z, world);
		}

		return null;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
		case guiIdMover:
			return new ContainerMover(player, world, x, y, z, null);
		}
		return null;
	}
}
