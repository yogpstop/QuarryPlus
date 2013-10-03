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

package org.yogpstop.qp.client;

import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;
import org.yogpstop.qp.QuarryPlus;
import org.yogpstop.qp.TileRefinery;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.core.render.FluidRenderer;

@SideOnly(Side.CLIENT)
public class RenderRefinery extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler {
	public static final RenderRefinery INSTANCE = new RenderRefinery();
	private static final ResourceLocation TEXTURE = new ResourceLocation("yogpstop_qp", "textures/blocks/refinery.png");
	private static final float pixel = (float) (1.0 / 16.0);
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

		setTileEntityRenderer(TileEntityRenderer.instance);
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		render((TileRefinery) tileentity, x, y, z);
	}

	private void render(TileRefinery tile, double x, double y, double z) {
		FluidStack liquid1 = null, liquid2 = null, liquidResult = null;

		float anim = 0;
		int angle = 0;
		ModelRenderer theMagnet = this.magnet[0];
		if (tile != null) {
			liquid1 = tile.src1;
			liquid2 = tile.src2;
			liquidResult = tile.res;

			anim = tile.getAnimationStage();

			angle = 0;
			switch (tile.worldObj.getBlockMetadata(tile.xCoord, tile.yCoord, tile.zCoord)) {
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

		GL11.glPushMatrix();
		GL11.glScalef(0.99F, 0.99F, 0.99F);
		GL11.glTranslatef(-0.51F, trans1 - 0.5F, -0.5F);
		theMagnet.render(pixel);
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glScalef(0.99F, 0.99F, 0.99F);
		GL11.glTranslatef(-0.51F, trans2 - 0.5F, 12F * pixel - 0.5F);
		theMagnet.render(pixel);
		GL11.glPopMatrix();

		if (tile != null) {
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			GL11.glScalef(0.5F, 1, 0.5F);

			if (liquid1 != null && liquid1.amount > 0) {
				int[] list1 = FluidRenderer.getFluidDisplayLists(liquid1, tile.worldObj, false);

				if (list1 != null) {
					bindTexture(FluidRenderer.getFluidSheet(liquid1));
					FluidRenderer.setColorForFluidStack(liquid1);
					GL11.glCallList(list1[(int) (liquid1.amount / (float) tile.buf * (FluidRenderer.DISPLAY_STAGES - 1))]);
				}
			}

			if (liquid2 != null && liquid2.amount > 0) {
				int[] list2 = FluidRenderer.getFluidDisplayLists(liquid2, tile.worldObj, false);

				if (list2 != null) {
					GL11.glPushMatrix();
					GL11.glTranslatef(0, 0, 1);
					bindTexture(FluidRenderer.getFluidSheet(liquid2));
					FluidRenderer.setColorForFluidStack(liquid2);
					GL11.glCallList(list2[(int) (liquid2.amount / (float) tile.buf * (FluidRenderer.DISPLAY_STAGES - 1))]);
					GL11.glPopMatrix();
				}
			}

			if (liquidResult != null && liquidResult.amount > 0) {
				int[] list3 = FluidRenderer.getFluidDisplayLists(liquidResult, tile.worldObj, false);

				if (list3 != null) {
					GL11.glPushMatrix();
					GL11.glTranslatef(1, 0, 0.5F);
					bindTexture(FluidRenderer.getFluidSheet(liquidResult));
					FluidRenderer.setColorForFluidStack(liquidResult);
					GL11.glCallList(list3[(int) (liquidResult.amount / (float) tile.buf * (FluidRenderer.DISPLAY_STAGES - 1))]);
					GL11.glPopMatrix();
				}
			}
			GL11.glPopAttrib();
		}

		GL11.glPopAttrib();
		GL11.glPopMatrix();
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
	public boolean shouldRender3DInInventory() {
		return true;
	}

	@Override
	public int getRenderId() {
		return QuarryPlus.refineryRenderID;
	}
}
