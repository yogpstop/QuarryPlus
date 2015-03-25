package com.yogpc.qp.compat;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import net.minecraftforge.common.MinecraftForge;

public class IC2Helper {
  public static final void ic2load(final IEnergySink i) {
    MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(i));
  }

  public static final void ic2unload(final IEnergySink i) {
    MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(i));
  }
}
