package org.yogpstop.qp.client;

import org.yogpstop.qp.CommonProxy;

import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;



@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	public void registerTextures()
	{
		
		MinecraftForgeClient.preloadTexture("/org/yogpstop/qp/blocks.png");
	}
	
}