package com.yogpc.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIQuarryPlusConfig implements IConfigureNEI {

  @Override
  public String getName() {
    return "QuarryPlus NEI Plugin";
  }

  @Override
  public String getVersion() {
    return "{version}";
  }

  @Override
  public void loadConfig() {
    API.registerRecipeHandler(new WBPRecipeHandler());
  }

}
