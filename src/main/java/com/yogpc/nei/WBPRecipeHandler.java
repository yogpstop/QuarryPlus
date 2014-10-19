package com.yogpc.nei;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.yogpc.mc_lib.GuiWorkbench;
import com.yogpc.mc_lib.WorkbenchRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class WBPRecipeHandler extends TemplateRecipeHandler {
  // All offset is (x, y) = (5, 11)
  private class WBPRecipe extends TemplateRecipeHandler.CachedRecipe {
    private final List<PositionedStack> input = new ArrayList<PositionedStack>();
    private final PositionedStack output;
    final double energy;

    WBPRecipe(WorkbenchRecipe wbr) {
      this.energy = wbr.power;
      this.output = new PositionedStack(wbr.output, 3, 79);
      int row = 0;
      int col = 0;
      for (ItemStack is : wbr.input) {
        this.input.add(new PositionedStack(is, 3 + col * 18, 7 + row * 18));
        col++;
        if (col >= 9) {
          row++;
          col = 0;
        }
      }
    }

    @Override
    public PositionedStack getResult() {
      return this.output;
    }

    @Override
    public List<PositionedStack> getIngredients() {
      return this.input;
    }
  }

  @Override
  public String getRecipeName() {
    return "WorkbenchPlus";
  }

  @Override
  public String getGuiTexture() {
    return "yogpstop_qp:textures/gui/workbench.png";
  }

  @Override
  public Class<? extends GuiContainer> getGuiClass() {
    return GuiWorkbench.class;
  }

  @Override
  public int recipiesPerPage() {
    return 1;
  }

  @Override
  public void drawBackground(int recipe) {
    GL11.glColor4f(1, 1, 1, 1);
    GuiDraw.changeTexture(getGuiTexture());
    GuiDraw.drawTexturedModalRect(0, 0, 5, 11, 166, 121);
  }

  @Override
  public void loadCraftingRecipes(ItemStack result) {
    for (WorkbenchRecipe wbr : WorkbenchRecipe.getRecipes()) {
      if (ItemStack.areItemStacksEqual(wbr.output, result))
        this.arecipes.add(new WBPRecipe(wbr));
    }
  }

  @Override
  public void drawExtras(int recipeIndex) {
    drawProgressBar(3, 67, 0, 222, 160, 4, 40, 0);
    WBPRecipe recipe = (WBPRecipe) this.arecipes.get(recipeIndex);
    Minecraft.getMinecraft().fontRenderer.drawString(Double.toString(recipe.energy), 3, 121,
        0x404040);
  }
}
