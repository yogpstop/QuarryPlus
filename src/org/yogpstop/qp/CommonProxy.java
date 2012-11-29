package org.yogpstop.qp;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

import org.yogpstop.qp.client.GuiQuarry;

import cpw.mods.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler
{
	public void registerTextures()
	{
		
	}
	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if (ID == QuarryPlus.guiIdContainerQuarry)
		{
			return new GuiQuarry(player, world, x, y, z);
		}
		
		return null;
	}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if (ID == QuarryPlus.guiIdContainerQuarry)
		{
			return new ContainerQuarry(player, world, x, y, z);
		}
		
		return null;
	}
	
}