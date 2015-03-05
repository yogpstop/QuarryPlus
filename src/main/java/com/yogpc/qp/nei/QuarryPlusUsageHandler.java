package com.yogpc.qp.nei;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public final class QuarryPlusUsageHandler extends TemplateRecipeHandler {
  private class Usage extends TemplateRecipeHandler.CachedRecipe {
    private final PositionedStack output;
    final String desc;

    Usage(final ItemStack is, final String dsc) {
      this.output = new PositionedStack(is, 75, 4);
      this.desc = dsc;
    }

    @Override
    public PositionedStack getResult() {
      return this.output;
    }
  }

  @Override
  public String getRecipeName() {
    return StatCollector.translateToLocal("nei.handler.qpusage");
  }

  @Override
  public String getGuiTexture() {
    return "nei:textures/gui/recipebg.png";
  }

  @Override
  public int recipiesPerPage() {
    return 1;
  }

  @Override
  public void loadCraftingRecipes(final String outputId, final Object... results) {
    if (outputId.equals("qpUsage") && getClass() == QuarryPlusUsageHandler.class)
      for (final Map.Entry<ItemStack, String[]> e : UsageList.getAll().entrySet())
        for (final String s : e.getValue())
          this.arecipes.add(new Usage(e.getKey(), s));
    else
      super.loadCraftingRecipes(outputId, results);
  }

  @Override
  public void loadUsageRecipes(final ItemStack ingredient) {
    for (final String s : UsageList.getFromItemStack(ingredient))
      this.arecipes.add(new Usage(ingredient, s));
  }

  @Override
  public void drawExtras(final int recipeIndex) {
    drawProgressBar(3, 67, 0, 222, 160, 4, 40, 0);
    final Usage recipe = (Usage) this.arecipes.get(recipeIndex);
    Minecraft.getMinecraft().fontRenderer.drawSplitString(recipe.desc, 0, 0, 166, 0x404040);
  }
}
