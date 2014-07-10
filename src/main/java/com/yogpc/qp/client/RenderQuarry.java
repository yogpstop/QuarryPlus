package com.yogpc.qp.client;

import org.lwjgl.opengl.GL11;

import static buildcraft.core.DefaultProps.TEXTURE_PATH_ENTITIES;

import com.yogpc.qp.TileQuarry;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class RenderQuarry extends TileEntitySpecialRenderer {

	private static void render(double fx, double fy, double fz, double tx, double ty, double tz) {

		GL11.glPushMatrix();
		GL11.glTranslated(0.5F, 0.5F, 0.5F);
		RenderLaser.renderLaser(fx, fy, fz, tx, ty, tz, 0, new ResourceLocation("buildcraft", TEXTURE_PATH_ENTITIES + "/stripes.png"));
		GL11.glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float f) {
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glTranslated(-te.xCoord, -te.yCoord, -te.zCoord);

		TileQuarry tq = (TileQuarry) te;

		if ((tq.G_getNow() == TileQuarry.NOTNEEDBREAK || tq.G_getNow() == TileQuarry.MAKEFRAME) && tq.yMax != Integer.MIN_VALUE) {
			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_LIGHTING);

			render(tq.xMin, tq.yMin, tq.zMin, tq.xMax, tq.yMin, tq.zMin);
			render(tq.xMin, tq.yMin, tq.zMin, tq.xMin, tq.yMax, tq.zMin);
			render(tq.xMin, tq.yMin, tq.zMin, tq.xMin, tq.yMin, tq.zMax);
			render(tq.xMin, tq.yMax, tq.zMax, tq.xMax, tq.yMax, tq.zMax);
			render(tq.xMin, tq.yMax, tq.zMax, tq.xMin, tq.yMin, tq.zMax);
			render(tq.xMin, tq.yMax, tq.zMax, tq.xMin, tq.yMax, tq.zMin);
			render(tq.xMax, tq.yMin, tq.zMax, tq.xMin, tq.yMin, tq.zMax);
			render(tq.xMax, tq.yMin, tq.zMax, tq.xMax, tq.yMax, tq.zMax);
			render(tq.xMax, tq.yMin, tq.zMax, tq.xMax, tq.yMin, tq.zMin);
			render(tq.xMax, tq.yMax, tq.zMin, tq.xMin, tq.yMax, tq.zMin);
			render(tq.xMax, tq.yMax, tq.zMin, tq.xMax, tq.yMin, tq.zMin);
			render(tq.xMax, tq.yMax, tq.zMin, tq.xMax, tq.yMax, tq.zMax);

			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}

		GL11.glPopMatrix();

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

}
