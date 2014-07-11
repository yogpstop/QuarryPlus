package com.yogpc.qp.client;

import org.lwjgl.opengl.GL11;

import com.yogpc.qp.TileLaser;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

public class RenderLaser extends TileEntitySpecialRenderer {
	private static ModelBase model = new ModelBase() {};
	private static ModelRenderer[] box;

	private static ModelRenderer getBox(int index) {
		if (box == null) {
			box = new ModelRenderer[40];
			for (int j = 0; j < box.length; ++j) {
				box[j] = new ModelRenderer(model, box.length - j, 0);
				box[j].addBox(0, -0.5F, -0.5F, 16, 1, 1);
				box[j].rotationPointX = 0;
				box[j].rotationPointY = 0;
				box[j].rotationPointZ = 0;
			}
		}

		return box[index];
	}

	public static void renderLaser(TextureManager tm, double fx, double fy, double fz, double tx, double ty, double tz, int b, ResourceLocation tex) {
		GL11.glPushMatrix();
		GL11.glTranslated(tx, ty, tz);
		double dx = tx - fx, dy = ty - fy, dz = tz - fz;
		double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
		GL11.glRotated(360 - (Math.atan2(dz, dx) * 180.0 / Math.PI + 180.0), 0, 1, 0);
		GL11.glRotated(-Math.atan2(dy, Math.sqrt(len * len - dy * dy)) * 180.0 / Math.PI, 0, 0, 1);
		tm.bindTexture(tex);
		float lasti = 0;
		if (len - 1 > 0) {
			for (float i = 0; i <= len - 1; i += 1) {
				getBox(b).render(1F / 16F);
				GL11.glTranslated(1, 0, 0);
				lasti = i;
			}
			lasti++;
		}
		GL11.glPushMatrix();
		GL11.glScaled(len - lasti, 1, 1);
		getBox(b).render(1F / 16F);
		GL11.glPopMatrix();
		GL11.glTranslated(len - lasti, 0, 0);
		GL11.glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float f) {
		TileLaser laser = (TileLaser) te;
		if (laser != null && laser.lasers != null) {
			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			GL11.glTranslated(x, y, z);
			GL11.glTranslated(-te.xCoord, -te.yCoord, -te.zCoord);

			GL11.glPushMatrix();
			for (TileLaser.Position l : laser.lasers) {
				l.l = (l.l + 1) % 40;
				ForgeDirection fd = ForgeDirection.values()[te.getWorldObj().getBlockMetadata(te.xCoord, te.yCoord, te.zCoord)];
				renderLaser(this.field_147501_a.field_147553_e, l.x, l.y, l.z, te.xCoord + 0.5 + 0.3 * fd.offsetX, te.yCoord + 0.5 + 0.3 * fd.offsetY,
						te.zCoord + 0.5 + 0.3 * fd.offsetZ, l.l, laser.getTexture());
			}
			GL11.glPopMatrix();
			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}
	}
}
