/*
 * Copyright (C) 2012,2013 yogpstop
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the
 * GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.TileRefinery;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRefinery extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler {
	private static final ResourceLocation TEXTURE = new ResourceLocation("yogpstop_qp", "textures/blocks/refinery.png");
	private static final float pixel = (float) (1.0 / 16.0);
	private static final RenderBlocks renderBlocks = new RenderBlocks();
	public static final RenderRefinery INSTANCE = new RenderRefinery();
	private final ModelRenderer tank;
	private final ModelRenderer magnet[] = new ModelRenderer[4];
	private final ModelBase model = new ModelBase() {};

	private RenderRefinery() {
		this.tank = new ModelRenderer(this.model, 0, 0);
		this.tank.addBox(-4F, -8F, -4F, 8, 16, 8);
		this.tank.rotationPointX = 8;
		this.tank.rotationPointY = 8;
		this.tank.rotationPointZ = 8;

		for (int i = 0; i < 4; ++i) {
			this.magnet[i] = new ModelRenderer(this.model, 32, i * 8);
			this.magnet[i].addBox(0, -8F, -8F, 8, 4, 4);
			this.magnet[i].rotationPointX = 8;
			this.magnet[i].rotationPointY = 8;
			this.magnet[i].rotationPointZ = 8;
		}
	}

	private static final void setColor(int i) {
		GL11.glColor4b((byte) (i >> 16), (byte) (i >> 8), (byte) i, Byte.MAX_VALUE);
	}

	private void render(TileRefinery tile, double x, double y, double z) {
		float anim = 0;
		int angle = 0;
		ModelRenderer theMagnet = this.magnet[0];
		if (tile != null) {
			anim = tile.getAnimationStage();
			angle = 0;
			switch (tile.getWorldObj().getBlockMetadata(tile.xCoord, tile.yCoord, tile.zCoord)) {
			case 2:
				angle = 90;
				break;
			case 3:
				angle = 270;
				break;
			case 4:
				angle = 180;
				break;
			case 5:
				angle = 0;
				break;
			}

			if (tile.animationSpeed <= 1) {
				theMagnet = this.magnet[0];
			} else if (tile.animationSpeed <= 2.5) {
				theMagnet = this.magnet[1];
			} else if (tile.animationSpeed <= 4.5) {
				theMagnet = this.magnet[2];
			} else {
				theMagnet = this.magnet[3];
			}
		}

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);

		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
		GL11.glScalef(0.99F, 0.99F, 0.99F);

		GL11.glRotatef(angle, 0, 1, 0);

		bindTexture(TEXTURE);

		GL11.glPushMatrix();
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		GL11.glTranslatef(-4F * pixel, 0, -4F * pixel);
		this.tank.render(pixel);
		GL11.glTranslatef(4F * pixel, 0, 4F * pixel);

		GL11.glTranslatef(-4F * pixel, 0, 4F * pixel);
		this.tank.render(pixel);
		GL11.glTranslatef(4F * pixel, 0, -4F * pixel);

		GL11.glTranslatef(4F * pixel, 0, 0);
		this.tank.render(pixel);
		GL11.glTranslatef(-4F * pixel, 0, 0);
		GL11.glPopMatrix();

		float trans1, trans2;

		if (anim <= 100) {
			trans1 = 12F * pixel * anim / 100F;
			trans2 = 0;
		} else if (anim <= 200) {
			trans1 = 12F * pixel - (12F * pixel * (anim - 100F) / 100F);
			trans2 = 12F * pixel * (anim - 100F) / 100F;
		} else {
			trans1 = 12F * pixel * (anim - 200F) / 100F;
			trans2 = 12F * pixel - (12F * pixel * (anim - 200F) / 100F);
		}

		renderMagnet(trans1, theMagnet, 0);
		renderMagnet(trans2, theMagnet, 12 * pixel);

		if (tile != null) {
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			GL11.glScalef(0.5F, 1, 0.5F);
			renderFluid(tile.src1, 0, 0, 0, tile.buf);
			renderFluid(tile.src2, 0, 0, 1, tile.buf);
			renderFluid(tile.res, 1, 0, 0.5F, tile.buf);
			GL11.glPopAttrib();
		}
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	private static void renderMagnet(float trans, ModelRenderer magnet, float offset) {
		GL11.glPushMatrix();
		GL11.glScalef(0.99F, 0.99F, 0.99F);
		GL11.glTranslatef(-0.51F, trans - 0.5F, offset - 0.5F);
		magnet.render(pixel);
		GL11.glPopMatrix();
	}

	private void renderFluid(FluidStack liquid, float tx, float ty, float tz, float buf) {
		if (liquid != null && liquid.amount > 0) {
			int[] list = getFluidDisplayLists(liquid);
			if (list != null) {
				if (tx != 0 || ty != 0 || tz != 0) {
					GL11.glPushMatrix();
					GL11.glTranslatef(tx, ty, tz);
				}
				bindTexture(TextureMap.locationBlocksTexture);
				setColor(liquid.getFluid().getColor(liquid));
				GL11.glCallList(list[(int) (liquid.amount / buf * 99)]);
				if (tx != 0 || ty != 0 || tz != 0) GL11.glPopMatrix();
			}
		}
	}

	private static final Map<Fluid, int[]> stillRenderCache = new HashMap<Fluid, int[]>();

	private static int[] getFluidDisplayLists(FluidStack fluidStack) {
		Fluid fluid = fluidStack.getFluid();
		if (fluid == null) return null;
		Map<Fluid, int[]> cache = stillRenderCache;
		int[] diplayLists = cache.get(fluid);
		if (diplayLists != null) return diplayLists;
		diplayLists = new int[100];
		Block baseBlock;
		IIcon texture;
		if (fluid.getBlock() != null) {
			baseBlock = fluid.getBlock();
			texture = fluid.getStillIcon();
		} else {
			baseBlock = Blocks.water;
			texture = fluid.getStillIcon();
		}
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_CULL_FACE);
		for (int s = 0; s < 100; ++s) {
			diplayLists[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(diplayLists[s], GL11.GL_COMPILE);
			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			renderBlocks.setRenderBounds(0.01, 0, 0.01, 0.99, (float) s / 100, 0.99);
			renderBlocks.renderFaceYNeg(baseBlock, 0, 0, 0, texture != null ? texture : baseBlock.getBlockTextureFromSide(0));
			renderBlocks.renderFaceYPos(baseBlock, 0, 0, 0, texture != null ? texture : baseBlock.getBlockTextureFromSide(1));
			renderBlocks.renderFaceZNeg(baseBlock, 0, 0, 0, texture != null ? texture : baseBlock.getBlockTextureFromSide(2));
			renderBlocks.renderFaceZPos(baseBlock, 0, 0, 0, texture != null ? texture : baseBlock.getBlockTextureFromSide(3));
			renderBlocks.renderFaceXNeg(baseBlock, 0, 0, 0, texture != null ? texture : baseBlock.getBlockTextureFromSide(4));
			renderBlocks.renderFaceXPos(baseBlock, 0, 0, 0, texture != null ? texture : baseBlock.getBlockTextureFromSide(5));
			tessellator.draw();
			GL11.glEndList();
		}
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LIGHTING);
		cache.put(fluid, diplayLists);
		return diplayLists;
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		render((TileRefinery) tileentity, x, y, z);
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
		if (block.getRenderType() != getRenderId()) return;
		render(null, -0.5, -0.5, -0.5);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int arg0) {
		return true;
	}

	@Override
	public int getRenderId() {
		return QuarryPlus.refineryRenderID;
	}
}
