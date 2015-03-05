package com.yogpc.qp;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;

import com.yogpc.qp.entity.EntityLaser;
import com.yogpc.qp.gui.GuiController;
import com.yogpc.qp.render.RenderEntityLaser;
import com.yogpc.qp.render.RenderFrame;
import com.yogpc.qp.render.RenderLaser;
import com.yogpc.qp.render.RenderLaserBlock;
import com.yogpc.qp.render.RenderMarker;
import com.yogpc.qp.render.RenderQuarry;
import com.yogpc.qp.render.RenderRefinery;
import com.yogpc.qp.tile.TileLaser;
import com.yogpc.qp.tile.TileQuarry;
import com.yogpc.qp.tile.TileRefinery;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ProxyClient extends ProxyCommon {
  private int key = 0;

  static {
    for (final Key k : Key.values())
      if (k.name != null) {
        k.binding = new KeyBinding(k.name, k.id, "key.yoglib");
        ClientRegistry.registerKeyBinding((KeyBinding) k.binding);
      }
  }

  public ProxyClient() {
    FMLCommonHandler.instance().bus().register(this);
  }

  @SubscribeEvent
  public void keyUpdate(final TickEvent.ClientTickEvent e) {
    if (e.phase != TickEvent.Phase.START)
      return;
    final int prev = this.key;
    this.key = 0;
    final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
    if (currentScreen == null || currentScreen.allowUserInput)
      for (final Key k : Key.values())
        if (k.binding instanceof KeyBinding) {
          if (GameSettings.isKeyDown((KeyBinding) k.binding))
            this.key |= 1 << k.ordinal();
        } else
          switch (k) {
            case forward:
              if (GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindForward))
                this.key |= 1 << k.ordinal();
              break;
            case jump:
              if (GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindJump))
                this.key |= 1 << k.ordinal();
              break;
            default:
              break;
          }
    if (this.key != prev) {
      PacketHandler.sendPacketToServer(new YogpstopPacket(this.key));
      super.setKeys(Minecraft.getMinecraft().thePlayer, this.key);
    }
  }

  @Override
  public EntityPlayer getPacketPlayer(final INetHandler inh) {
    if (inh instanceof NetHandlerPlayServer)
      return ((NetHandlerPlayServer) inh).playerEntity;
    return Minecraft.getMinecraft().thePlayer;
  }

  @Override
  public int addNewArmourRendererPrefix(final String s) {
    return RenderingRegistry.addNewArmourRendererPrefix(s);
  }

  @Override
  public void removeEntity(final Entity e) {
    e.worldObj.removeEntity(e);
    if (e.worldObj.isRemote)
      ((WorldClient) e.worldObj).removeEntityFromWorld(e.getEntityId());
  }

  @Override
  public World getClientWorld() {
    return Minecraft.getMinecraft().theWorld;
  }

  @Override
  public Object getGuiController(final int d, final int x, final int y, final int z,
      final List<String> l) {
    return new GuiController(d, x, y, z, l);
  }

  @Override
  public void registerTextures() {
    RenderingRegistry.registerEntityRenderingHandler(EntityLaser.class, RenderEntityLaser.INSTANCE);
    ClientRegistry.bindTileEntitySpecialRenderer(TileRefinery.class, RenderRefinery.INSTANCE);
    ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry.class, RenderQuarry.INSTANCE);
    ClientRegistry.bindTileEntitySpecialRenderer(TileLaser.class, RenderLaser.INSTANCE);
    RenderingRegistry.registerBlockHandler(RenderRefinery.INSTANCE);
    RenderingRegistry.registerBlockHandler(RenderLaserBlock.INSTANCE);
    RenderingRegistry.registerBlockHandler(RenderMarker.INSTANCE);
    RenderingRegistry.registerBlockHandler(RenderFrame.INSTANCE);
  }
}
