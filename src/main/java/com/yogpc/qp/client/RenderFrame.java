package com.yogpc.qp.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import com.yogpc.qp.BlockFrame;
import com.yogpc.qp.QuarryPlus;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class RenderFrame implements ISimpleBlockRenderingHandler {
	public static final RenderFrame INSTANCE = new RenderFrame();

	private RenderFrame() {}

	@Override
	public int getRenderId() {
		return QuarryPlus.frameRenderID;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess w, int i, int j, int k, Block b, int m, RenderBlocks r) {
		if (w.getBlock(i - 1, j, k) == b) {
			r.setRenderBounds(0.0F, 0.25F, 0.25F, 0.25F, 0.75F, 0.75F);
			((BlockFrame) b).setSides(true, true, true, true, false, false);
		} else {
			r.setRenderBounds(0.25F, 0.25F, 0.25F, 0.75F, 0.75F, 0.75F);
			((BlockFrame) b).setSides(false, false, false, false, true, false);
		}
		r.renderStandardBlock(b, i, j, k);
		if (w.getBlock(i + 1, j, k) == b) {
			r.setRenderBounds(0.75F, 0.25F, 0.25F, 1.0F, 0.75F, 0.75F);
			((BlockFrame) b).setSides(true, true, true, true, false, false);
		} else {
			r.setRenderBounds(0.25F, 0.25F, 0.25F, 0.75F, 0.75F, 0.75F);
			((BlockFrame) b).setSides(false, false, false, false, false, true);
		}
		r.renderStandardBlock(b, i, j, k);
		if (w.getBlock(i, j - 1, k) == b) {
			r.setRenderBounds(0.25F, 0.0F, 0.25F, 0.75F, 0.25F, 0.75F);
			((BlockFrame) b).setSides(false, false, true, true, true, true);
		} else {
			r.setRenderBounds(0.25F, 0.25F, 0.25F, 0.75F, 0.75F, 0.75F);
			((BlockFrame) b).setSides(true, false, false, false, false, false);
		}
		r.renderStandardBlock(b, i, j, k);
		if (w.getBlock(i, j + 1, k) == b) {
			r.setRenderBounds(0.25F, 0.75F, 0.25F, 0.75F, 1.0F, 0.75F);
			((BlockFrame) b).setSides(false, false, true, true, true, true);
		} else {
			r.setRenderBounds(0.25F, 0.25F, 0.25F, 0.75F, 0.75F, 0.75F);
			((BlockFrame) b).setSides(false, true, false, false, false, false);
		}
		r.renderStandardBlock(b, i, j, k);
		if (w.getBlock(i, j, k - 1) == b) {
			r.setRenderBounds(0.25F, 0.25F, 0.0F, 0.75F, 0.75F, 0.25F);
			((BlockFrame) b).setSides(true, true, false, false, true, true);
		} else {
			r.setRenderBounds(0.25F, 0.25F, 0.25F, 0.75F, 0.75F, 0.75F);
			((BlockFrame) b).setSides(false, false, true, false, false, false);
		}
		r.renderStandardBlock(b, i, j, k);
		if (w.getBlock(i, j, k + 1) == b) {
			r.setRenderBounds(0.25F, 0.25F, 0.75F, 0.75F, 0.75F, 1.0F);
			((BlockFrame) b).setSides(true, true, false, false, true, true);
		} else {
			r.setRenderBounds(0.25F, 0.25F, 0.25F, 0.75F, 0.75F, 0.75F);
			((BlockFrame) b).setSides(false, false, false, true, false, false);
		}
		r.renderStandardBlock(b, i, j, k);
		((BlockFrame) b).setSides(true, true, true, true, true, true);
		return true;
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		renderer.setRenderBounds(0.25F, 0.0F, 0.25F, 0.75F, 1.0F, 0.75F);
		RenderLaserBlock.renderBlockInInv(renderer, block, metadata);
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}
}
