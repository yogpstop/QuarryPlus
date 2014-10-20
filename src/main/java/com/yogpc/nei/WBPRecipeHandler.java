package com.yogpc.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

import com.yogpc.mc_lib.GuiWorkbench;
import com.yogpc.mc_lib.WorkbenchRecipe;

public class WBPRecipeHandler extends TemplateRecipeHandler {
  // All offset is (x, y) = (5, 11)
  private class WBPRecipe extends TemplateRecipeHandler.CachedRecipe {
    final List<PositionedStack> input = new ArrayList<PositionedStack>();
    private final PositionedStack output;
    final double energy;

    WBPRecipe(final WorkbenchRecipe wbr) {
      this.energy = wbr.power;
      this.output = new PositionedStack(wbr.output, 3, 79);
      int row = 0;
      int col = 0;
      for (final WorkbenchRecipe.WBIS is : wbr.input) {
        if (is.getAmount() <= 0)
          continue;
        this.input.add(new PositionedStack(is.getItemStack(), 3 + col * 18, 7 + row * 18));
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
    return "tile.WorkbenchPlus.name";
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
  public void drawBackground(final int recipe) {
    GL11.glColor4f(1, 1, 1, 1);
    GuiDraw.changeTexture(getGuiTexture());
    GuiDraw.drawTexturedModalRect(0, 0, 5, 11, 166, 121);
  }

  @Override
  public void loadCraftingRecipes(final String outputId, final Object... results) {
    if (outputId.equals("workbenchPlus") && getClass() == WBPRecipeHandler.class)
      for (final WorkbenchRecipe wbr : WorkbenchRecipe.getRecipes())
        this.arecipes.add(new WBPRecipe(wbr));
    else
      super.loadCraftingRecipes(outputId, results);
  }

  @Override
  public void loadCraftingRecipes(final ItemStack result) {
    for (final WorkbenchRecipe wbr : WorkbenchRecipe.getRecipes())
      if (NEIServerUtils.areStacksSameTypeCrafting(wbr.output, result))
        this.arecipes.add(new WBPRecipe(wbr));
  }

  @Override
  public void loadUsageRecipes(final ItemStack ingredient) {
    for (final WorkbenchRecipe wbr : WorkbenchRecipe.getRecipes()) {
      final WBPRecipe recipe = new WBPRecipe(wbr);
      if (recipe.contains(recipe.input, ingredient))
        this.arecipes.add(recipe);
    }
  }

  @Override
  public void drawExtras(final int recipeIndex) {
    drawProgressBar(3, 67, 0, 222, 160, 4, 40, 0);
    final WBPRecipe recipe = (WBPRecipe) this.arecipes.get(recipeIndex);
    Minecraft.getMinecraft().fontRenderer.drawString(Double.toString(recipe.energy), 3, 121,
        0x404040);
  }

  @Override
  public void loadTransferRects() {
    this.transferRects.add(new RecipeTransferRect(new Rectangle(2, 66, 162, 6), "workbenchPlus"));
  }
}
