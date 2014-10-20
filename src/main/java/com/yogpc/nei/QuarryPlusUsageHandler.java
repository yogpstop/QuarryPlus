package com.yogpc.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.IUsageHandler;

public final class QuarryPlusUsageHandler implements IUsageHandler {
  private PositionedStack output;
  private String message;

  @Override
  public int numRecipes() {
    return 0;
  }

  @Override
  public void drawForeground(final int recipe) {
    if (this.message != null)
      Minecraft.getMinecraft().fontRenderer.drawSplitString(this.message, 10, 25, 150, 0);
  }

  @Override
  public PositionedStack getResultStack(final int recipe) {
    return this.output;
  }

  public void loadUsageRecipes(final ItemStack m) {
    this.output = new PositionedStack(new ItemStack(m.getItem(), 1, m.getItemDamage()), 75, 4);
    // TODO usage load
  }

  @Override
  public IUsageHandler getUsageHandler(final String inputId, final Object... ingredients) {
    final QuarryPlusUsageHandler handler = new QuarryPlusUsageHandler();
    if (inputId.equals("item"))
      loadUsageRecipes((ItemStack) ingredients[0]);
    return handler;
  }

  @Override
  public int recipiesPerPage() {
    return 1;
  }

  @Override
  public String getRecipeName() {
    return "nei.handler.qpusage";
  }

  @Override
  public void drawBackground(final int recipe) {}

  @Override
  public void onUpdate() {}

  @Override
  public List<String> handleTooltip(final GuiRecipe gui, final List<String> currenttip,
      final int recipe) {
    return currenttip;
  }

  @Override
  public List<String> handleItemTooltip(final GuiRecipe gui, final ItemStack stack,
      final List<String> currenttip, final int recipe) {
    return currenttip;
  }

  @Override
  public List<PositionedStack> getIngredientStacks(final int recipe) {
    return new ArrayList<PositionedStack>();
  }

  @Override
  public List<PositionedStack> getOtherStacks(final int recipetype) {
    return new ArrayList<PositionedStack>();
  }

  @Override
  public boolean keyTyped(final GuiRecipe gui, final char keyChar, final int keyCode,
      final int recipe) {
    return false;
  }

  @Override
  public boolean mouseClicked(final GuiRecipe gui, final int button, final int recipe) {
    return false;
  }

  @Override
  public boolean hasOverlay(final GuiContainer gui, final Container container, final int recipe) {
    return false;
  }

  @Override
  public IRecipeOverlayRenderer getOverlayRenderer(final GuiContainer gui, final int recipe) {
    return null;
  }

  @Override
  public IOverlayHandler getOverlayHandler(final GuiContainer gui, final int recipe) {
    return null;
  }
}
