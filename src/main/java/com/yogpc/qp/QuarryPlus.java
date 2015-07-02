/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = "QuarryPlus",
    name = "QuarryPlus",
    version = "{version}",
    dependencies = "after:BuildCraftAPI|core;after:BuildCraftAPI|recipes;after:BuildCraftAPI|tools;after:BuildCraftAPI|transport;after:CoFHAPI|block;after:CoFHAPI|energy;after:CoFHAPI|inventory;after:IC2")
public class QuarryPlus {
  @SidedProxy(clientSide = "com.yogpc.qp.ProxyClient", serverSide = "com.yogpc.qp.ProxyCommon")
  public static ProxyCommon proxy;
  @Mod.Instance("QuarryPlus")
  public static QuarryPlus I;

  @Mod.EventHandler
  public void preInit(final FMLPreInitializationEvent event) {
    QuarryPlusI.preInit(event);
  }

  @Mod.EventHandler
  public void init(final FMLInitializationEvent event) {
    QuarryPlusI.init();
  }
}
