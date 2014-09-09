package com.yogpc.qp.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import com.yogpc.qp.QuarryPlus;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class RenderLaserBlock implements ISimpleBlockRenderingHandler {
  public static final RenderLaserBlock INSTANCE = new RenderLaserBlock();

  private RenderLaserBlock() {}

  @Override
  public int getRenderId() {
    return QuarryPlus.laserRenderID;
  }

  @Override
  public boolean shouldRender3DInInventory(final int modelId) {
    return true;
  }

  @Override
  public boolean renderWorldBlock(final IBlockAccess iblockaccess, final int x, final int y,
      final int z, final Block block, final int l, final RenderBlocks renderblocks) {
    switch (ForgeDirection.values()[iblockaccess.getBlockMetadata(x, y, z)]) {
      case EAST:
        renderblocks.uvRotateEast = 2;
        renderblocks.uvRotateWest = 1;
        renderblocks.uvRotateTop = 1;
        renderblocks.uvRotateBottom = 2;

        renderblocks.setRenderBounds(0.0F, 0.0F, 0.0F, 4F / 16F, 1, 1);
        renderblocks.renderStandardBlock(block, x, y, z);

        renderblocks.setRenderBounds(4F / 16F, 5F / 16F, 5F / 16F, 13F / 16F, 11F / 16F, 11F / 16F);
        renderblocks.renderStandardBlock(block, x, y, z);
        break;
      case WEST:
        renderblocks.uvRotateEast = 1;
        renderblocks.uvRotateWest = 2;
        renderblocks.uvRotateTop = 2;
        renderblocks.uvRotateBottom = 1;

        renderblocks.setRenderBounds(1F - 4F / 16F, 0.0F, 0.0F, 1, 1, 1);
        renderblocks.renderStandardBlock(block, x, y, z);

        renderblocks.setRenderBounds(1F - 13F / 16F, 5F / 16F, 5F / 16F, 1F - 4F / 16F, 11F / 16F,
            11F / 16F);
        renderblocks.renderStandardBlock(block, x, y, z);
        break;
      case NORTH:
        renderblocks.uvRotateSouth = 1;
        renderblocks.uvRotateNorth = 2;

        renderblocks.setRenderBounds(0.0F, 0.0F, 1F - 4F / 16F, 1, 1, 1);
        renderblocks.renderStandardBlock(block, x, y, z);

        renderblocks.setRenderBounds(5F / 16F, 5F / 16F, 1F - 13F / 16F, 11F / 16F, 11F / 16F,
            1F - 4F / 16F);
        renderblocks.renderStandardBlock(block, x, y, z);
        break;
      case SOUTH:
        renderblocks.uvRotateSouth = 2;
        renderblocks.uvRotateNorth = 1;
        renderblocks.uvRotateTop = 3;
        renderblocks.uvRotateBottom = 3;

        renderblocks.setRenderBounds(0.0F, 0.0F, 0.0F, 1, 1, 4F / 16F);
        renderblocks.renderStandardBlock(block, x, y, z);

        renderblocks.setRenderBounds(5F / 16F, 5F / 16F, 4F / 16F, 11F / 16F, 11F / 16F, 13F / 16F);
        renderblocks.renderStandardBlock(block, x, y, z);
        break;
      case DOWN:
        renderblocks.uvRotateEast = 3;
        renderblocks.uvRotateWest = 3;
        renderblocks.uvRotateSouth = 3;
        renderblocks.uvRotateNorth = 3;

        renderblocks.setRenderBounds(0.0F, 1.0F - 4F / 16F, 0.0F, 1.0F, 1.0F, 1.0F);
        renderblocks.renderStandardBlock(block, x, y, z);

        renderblocks.setRenderBounds(5F / 16F, 1F - 13F / 16F, 5F / 16F, 11F / 16F, 1F - 4F / 16F,
            11F / 16F);
        renderblocks.renderStandardBlock(block, x, y, z);
        break;
      default:
        renderblocks.setRenderBounds(0.0F, 0.0F, 0.0F, 1, 4F / 16F, 1);
        renderblocks.renderStandardBlock(block, x, y, z);

        renderblocks.setRenderBounds(5F / 16F, 4F / 16F, 5F / 16F, 11F / 16F, 13F / 16F, 11F / 16F);
        renderblocks.renderStandardBlock(block, x, y, z);
        break;
    }
    renderblocks.uvRotateEast = 0;
    renderblocks.uvRotateWest = 0;
    renderblocks.uvRotateSouth = 0;
    renderblocks.uvRotateNorth = 0;
    renderblocks.uvRotateTop = 0;
    renderblocks.uvRotateBottom = 0;
    return true;
  }

  @Override
  public void renderInventoryBlock(final Block block, final int i, final int j,
      final RenderBlocks renderblocks) {
    GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
    renderblocks.setRenderBounds(0.0F, 0.0F, 0.0F, 1, 4F / 16F, 1);
    renderBlockInInv(renderblocks, block, 0);
    renderblocks.setRenderBounds(5F / 16F, 4F / 16F, 5F / 16F, 11F / 16F, 13F / 16F, 11F / 16F);
    renderBlockInInv(renderblocks, block, 1);
    GL11.glTranslatef(0.5F, 0.5F, 0.5F);
  }

  static void renderBlockInInv(final RenderBlocks renderblocks, final Block block, final int i) {
    final Tessellator tessellator = Tessellator.instance;

    tessellator.startDrawingQuads();
    tessellator.setNormal(0.0F, -1F, 0.0F);
    renderblocks.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, i));
    tessellator.draw();

    tessellator.startDrawingQuads();
    tessellator.setNormal(0.0F, 1.0F, 0.0F);
    renderblocks.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, i));
    tessellator.draw();

    tessellator.startDrawingQuads();
    tessellator.setNormal(0.0F, 0.0F, -1F);
    renderblocks.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, i));
    tessellator.draw();

    tessellator.startDrawingQuads();
    tessellator.setNormal(0.0F, 0.0F, 1.0F);
    renderblocks.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, i));
    tessellator.draw();

    tessellator.startDrawingQuads();
    tessellator.setNormal(-1F, 0.0F, 0.0F);
    renderblocks.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, i));
    tessellator.draw();

    tessellator.startDrawingQuads();
    tessellator.setNormal(1.0F, 0.0F, 0.0F);
    renderblocks.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, i));
    tessellator.draw();
  }
}
