package com.yogpc.qp.client;

import org.lwjgl.opengl.GL11;

import com.yogpc.qp.TileLaser;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import buildcraft.core.LaserData;
import buildcraft.core.render.RenderLaser;

public class RenderLaserData extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		TileLaser laser = (TileLaser) tileentity;

		if (laser != null && laser.lasers != null) {
			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			GL11.glTranslated(x, y, z);
			GL11.glTranslated(-tileentity.xCoord, -tileentity.yCoord, -tileentity.zCoord);

			GL11.glPushMatrix();
			for (LaserData l : laser.lasers) {
				RenderLaser.doRenderLaser(TileEntityRendererDispatcher.instance.field_147553_e, l, laser.getTexture());
			}
			GL11.glPopMatrix();

			// GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}
	}
}
