package org.yogpstop.qp.client;

import org.yogpstop.qp.CommonProxy;
import org.yogpstop.qp.EntityMechanicalArm;
import org.yogpstop.qp.TileRefinery;

import buildcraft.core.render.RenderVoid;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	@Override
	public void registerTextures() {
		RenderingRegistry.registerEntityRenderingHandler(EntityMechanicalArm.class, new RenderVoid());
		ClientRegistry.bindTileEntitySpecialRenderer(TileRefinery.class, RenderRefinery.INSTANCE);
		RenderingRegistry.registerBlockHandler(RenderRefinery.INSTANCE);
	}

}