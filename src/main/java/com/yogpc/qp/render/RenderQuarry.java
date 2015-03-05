package com.yogpc.qp.render;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import com.yogpc.qp.tile.TileLaser;
import com.yogpc.qp.tile.TileQuarry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderQuarry extends TileEntitySpecialRenderer {
  public static final RenderQuarry INSTANCE = new RenderQuarry();

  private RenderQuarry() {}

  @Override
  public void renderTileEntityAt(final TileEntity te, final double x, final double y,
      final double z, final float f) {
    GL11.glPushMatrix();
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);// TODO lightmap
    GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
    GL11.glEnable(GL11.GL_CULL_FACE);
    GL11.glEnable(GL11.GL_LIGHTING);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GL11.glTranslated(x - te.xCoord, y - te.yCoord, z - te.zCoord);
    final TileQuarry tq = (TileQuarry) te;
    if ((tq.G_getNow() == TileQuarry.NOTNEEDBREAK || tq.G_getNow() == TileQuarry.MAKEFRAME)
        && tq.yMax != Integer.MIN_VALUE) {
      GL11.glPushMatrix();
      GL11.glTranslated(0.5, 0.5, 0.5);
      RenderLaser.renderLaser(this.field_147501_a.field_147553_e, tq.xMin - 0.03125, tq.yMin,
          tq.zMin, tq.xMax + 0.03125, tq.yMin, tq.zMin, 0, TileLaser.LASER_TEXTURES[4]);
      RenderLaser.renderLaser(this.field_147501_a.field_147553_e, tq.xMin, tq.yMin - 0.03125,
          tq.zMin, tq.xMin, tq.yMax + 0.03125, tq.zMin, 0, TileLaser.LASER_TEXTURES[4]);
      RenderLaser.renderLaser(this.field_147501_a.field_147553_e, tq.xMin, tq.yMin,
          tq.zMin - 0.03125, tq.xMin, tq.yMin, tq.zMax + 0.03125, 0, TileLaser.LASER_TEXTURES[4]);
      RenderLaser.renderLaser(this.field_147501_a.field_147553_e, tq.xMin - 0.03125, tq.yMax,
          tq.zMax, tq.xMax + 0.03125, tq.yMax, tq.zMax, 0, TileLaser.LASER_TEXTURES[4]);
      RenderLaser.renderLaser(this.field_147501_a.field_147553_e, tq.xMin, tq.yMax + 0.03125,
          tq.zMax, tq.xMin, tq.yMin - 0.03125, tq.zMax, 0, TileLaser.LASER_TEXTURES[4]);
      RenderLaser.renderLaser(this.field_147501_a.field_147553_e, tq.xMin, tq.yMax,
          tq.zMax + 0.03125, tq.xMin, tq.yMax, tq.zMin - 0.03125, 0, TileLaser.LASER_TEXTURES[4]);
      RenderLaser.renderLaser(this.field_147501_a.field_147553_e, tq.xMax + 0.03125, tq.yMin,
          tq.zMax, tq.xMin - 0.03125, tq.yMin, tq.zMax, 0, TileLaser.LASER_TEXTURES[4]);
      RenderLaser.renderLaser(this.field_147501_a.field_147553_e, tq.xMax, tq.yMin - 0.03125,
          tq.zMax, tq.xMax, tq.yMax + 0.03125, tq.zMax, 0, TileLaser.LASER_TEXTURES[4]);
      RenderLaser.renderLaser(this.field_147501_a.field_147553_e, tq.xMax, tq.yMin,
          tq.zMax + 0.03125, tq.xMax, tq.yMin, tq.zMin - 0.03125, 0, TileLaser.LASER_TEXTURES[4]);
      RenderLaser.renderLaser(this.field_147501_a.field_147553_e, tq.xMax + 0.03125, tq.yMax,
          tq.zMin, tq.xMin - 0.03125, tq.yMax, tq.zMin, 0, TileLaser.LASER_TEXTURES[4]);
      RenderLaser.renderLaser(this.field_147501_a.field_147553_e, tq.xMax, tq.yMax + 0.03125,
          tq.zMin, tq.xMax, tq.yMin - 0.03125, tq.zMin, 0, TileLaser.LASER_TEXTURES[4]);
      RenderLaser.renderLaser(this.field_147501_a.field_147553_e, tq.xMax, tq.yMax,
          tq.zMin - 0.03125, tq.xMax, tq.yMax, tq.zMax + 0.03125, 0, TileLaser.LASER_TEXTURES[4]);
      GL11.glPopMatrix();
    }
    if (tq.G_getNow() == TileQuarry.BREAKBLOCK || tq.G_getNow() == TileQuarry.MOVEHEAD)
      RenderEntityLaser.doRender(this.field_147501_a.field_147553_e, tq.xMin + 0.75,
          tq.yMax + 0.25, tq.zMin + 0.75, tq.headPosX, tq.headPosY, tq.headPosZ, tq.xMax - tq.xMin
              - 0.5, tq.zMax - tq.zMin - 0.5);
    GL11.glPopAttrib();
    GL11.glPopMatrix();
  }
}
