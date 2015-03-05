package com.yogpc.qp.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import com.yogpc.qp.tile.TileLaser;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderLaser extends TileEntitySpecialRenderer {
  private static final ModelBase model = new ModelBase() {};
  private static final ModelRenderer[] box = new ModelRenderer[40];
  static {
    for (int k = 0; k < box.length; ++k) {
      box[k] = new ModelRenderer(model, box.length - k, 0);
      box[k].addBox(0, -0.5F, -0.5F, 16, 1, 1);
    }
  }
  public static final RenderLaser INSTANCE = new RenderLaser();

  private RenderLaser() {}

  static void renderLaser(final TextureManager tm, final double fx, final double fy,
      final double fz, final double tx, final double ty, final double tz, final int b,
      final ResourceLocation tex) {
    GL11.glPushMatrix();
    GL11.glTranslated(tx, ty, tz);
    final double dx = tx - fx, dy = ty - fy, dz = tz - fz;
    final double total = Math.sqrt(dx * dx + dy * dy + dz * dz);
    GL11.glRotatef((float) (360 - (Math.atan2(dz, dx) * 180.0 / Math.PI + 180.0)), 0, 1, 0);
    GL11.glRotatef((float) (-Math.atan2(dy, Math.sqrt(total * total - dy * dy)) * 180.0 / Math.PI),
        0, 0, 1);
    tm.bindTexture(tex);
    int i = 0;
    while (i <= total - 1) {
      box[b].render(1F / 16);
      GL11.glTranslated(1, 0, 0);
      i++;
    }
    GL11.glScaled(total - i, 1, 1);
    box[b].render(1F / 16);
    GL11.glPopMatrix();
  }

  @Override
  public void renderTileEntityAt(final TileEntity te, final double x, final double y,
      final double z, final float f) {
    final TileLaser laser = (TileLaser) te;
    if (laser != null && laser.lasers != null) {
      GL11.glPushMatrix();
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);// TODO lightmap
      GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
      GL11.glEnable(GL11.GL_CULL_FACE);
      GL11.glEnable(GL11.GL_LIGHTING);
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GL11.glTranslated(x - te.xCoord, y - te.yCoord, z - te.zCoord);
      for (final TileLaser.Position l : laser.lasers) {
        final ForgeDirection fd =
            ForgeDirection.values()[te.getWorldObj().getBlockMetadata(te.xCoord, te.yCoord,
                te.zCoord)];
        renderLaser(this.field_147501_a.field_147553_e, te.xCoord + 0.5 + 0.3 * fd.offsetX,
            te.yCoord + 0.5 + 0.3 * fd.offsetY, te.zCoord + 0.5 + 0.3 * fd.offsetZ, l.x, l.y, l.z,
            (int) (laser.getWorldObj().getWorldTime() % 40), laser.getTexture());
      }
      GL11.glPopAttrib();
      GL11.glPopMatrix();
    }
  }
}
