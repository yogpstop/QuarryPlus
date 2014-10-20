package com.yogpc.mc_lib;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;

public class ProxyCommon {
  private final Map<EntityPlayer, Integer> keys = new WeakHashMap<EntityPlayer, Integer>();

  public boolean getKey(final EntityPlayer p, final Key k) {
    if (!this.keys.containsKey(p))
      return false;
    return (this.keys.get(p).intValue() & 1 << k.ordinal()) != 0;
  }

  public int keysToInt(final EntityPlayer p) {
    return this.keys.containsKey(p) ? this.keys.get(p).intValue() : 0;
  }

  public void setKeys(final EntityPlayer p, final int r) {
    this.keys.put(p, new Integer(r));
  }

  public static enum Key {
    forward, mode("key.hover", 50), jump;
    public Object binding;
    public final String name;
    public final int id;

    private Key() {
      this(null, 0);
    }

    private Key(final String n, final int i) {
      this.name = n;
      this.id = i;
    }
  }

  public EntityPlayer getPacketPlayer(final INetHandler inh) {
    if (inh instanceof NetHandlerPlayServer)
      return ((NetHandlerPlayServer) inh).playerEntity;
    return null;
  }

  public int addNewArmourRendererPrefix(final String s) {
    return 0;
  }

  public void removeEntity(final Entity e) {
    e.worldObj.removeEntity(e);
  }

  public World getClientWorld() {
    return null;
  }

  public Object getGuiController(final int d, final int x, final int y, final int z,
      final List<String> l) {
    return null;
  }
}
