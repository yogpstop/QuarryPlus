package com.yogpc.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIQuarryPlusConfig implements IConfigureNEI {

  @Override
  public String getName() {
    return "nei.plugin.qp";
  }

  @Override
  public String getVersion() {
    return "{version}";
  }

  @Override
  public void loadConfig() {
    API.registerRecipeHandler(new WBPRecipeHandler());
    API.registerUsageHandler(new WBPRecipeHandler());
    // TODO Machine Usage is disabled.
    // See also, QuarryPlusUsageHandler, UsageList
  }

}
