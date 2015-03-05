package com.yogpc.qp.render;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.yogpc.qp.entity.EntityLaser;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEntityLaser extends Render {
  public static IIcon[] icons;
  public static RenderEntityLaser INSTANCE = new RenderEntityLaser();

  private RenderEntityLaser() {}

  @Override
  protected ResourceLocation getEntityTexture(final Entity entity) {
    return TextureMap.locationBlocksTexture;
  }

  @Override
  public void doRender(final Entity e, final double i, final double j, final double k,
      final float f, final float f1) {
    if (e.isDead)
      return;
    GL11.glPushMatrix();
    GL11.glTranslated(i - e.posX, j - e.posY, k - e.posZ);
    doRender(this.renderManager.renderEngine, (EntityLaser) e);
    GL11.glPopMatrix();
  }

  static void doRender(final TextureManager tm, final double posX, final double posY,
      final double posZ, final double headX, final double headY, final double headZ,
      final double armXSize, final double armZSize) {
    doRender(tm, posX, posY, headZ + 0.25, armXSize, 0.5, 0.5, EntityLaser.DRILL);
    doRender(tm, headX + 0.25, headY + 1, headZ + 0.25, 0.5, posY - headY - 1, 0.5,
        EntityLaser.DRILL);
    doRender(tm, headX + 0.25, posY, posZ, 0.5, 0.5, armZSize, EntityLaser.DRILL);
    doRender(tm, headX + 0.4, headY, headZ + 0.4, 0.2, 1, 0.2, EntityLaser.DRILL_HEAD);
  }

  private static void doRender(final TextureManager tm, final EntityLaser ed) {
    doRender(tm, ed.posX, ed.posY, ed.posZ, ed.iSize, ed.jSize, ed.kSize, ed.texture);
  }

  private static void doRender(final TextureManager tm, final double i, final double j,
      final double k, final double iSize, final double jSize, final double kSize, final int tex) {
    GL11.glPushMatrix();
    GL11.glTranslated(i, j, k);
    tm.bindTexture(TextureMap.locationBlocksTexture);
    final Tessellator t = Tessellator.instance;
    final RenderBlocks rb = new RenderBlocks();
    for (int iBase = 0; iBase < iSize; ++iBase)
      for (int jBase = 0; jBase < jSize; ++jBase)
        for (int kBase = 0; kBase < kSize; ++kBase) {
          final double remainX = iSize - iBase;
          final double remainY = jSize - jBase;
          final double remainZ = kSize - kBase;
          GL11.glPushMatrix();
          GL11.glTranslatef(iBase, jBase, kBase);
          IIcon texture = icons[tex];
          if (texture == null)
            texture = Blocks.sand.getBlockTextureFromSide(0);
          t.startDrawingQuads();
          rb.setRenderBounds(0, 0, 0, remainX > 1.0 ? 1.0 : remainX, remainY > 1.0 ? 1.0 : remainY,
              remainZ > 1.0 ? 1.0 : remainZ);
          rb.renderFaceYNeg(Blocks.sand, 0, 0, 0, texture);
          rb.renderFaceYPos(Blocks.sand, 0, 0, 0, texture);
          rb.renderFaceZNeg(Blocks.sand, 0, 0, 0, texture);
          rb.renderFaceZPos(Blocks.sand, 0, 0, 0, texture);
          rb.renderFaceXNeg(Blocks.sand, 0, 0, 0, texture);
          rb.renderFaceXPos(Blocks.sand, 0, 0, 0, texture);
          t.draw();
          GL11.glPopMatrix();

        }
    GL11.glPopMatrix();
  }
}
