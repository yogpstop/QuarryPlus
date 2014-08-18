package com.yogpc.mc_lib;

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
		for (Key k : Key.values()) {
			if (k.name != null) {
				k.binding = new KeyBinding(k.name, k.id, "YogpstopLib");
				ClientRegistry.registerKeyBinding((KeyBinding) k.binding);
			}
		}
	}

	public ProxyClient() {
		FMLCommonHandler.instance().bus().register(this);
	}

	@SubscribeEvent
	public void keyUpdate(TickEvent.ClientTickEvent e) {
		if (e.phase != TickEvent.Phase.START) return;
		int prev = this.key;
		this.key = 0;
		GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		if ((currentScreen == null) || (currentScreen.allowUserInput)) {
			for (Key k : Key.values()) {
				if (k.binding instanceof KeyBinding) {
					if (GameSettings.isKeyDown((KeyBinding) k.binding)) {
						this.key |= 1 << k.ordinal();
					}
				} else {
					switch (k) {
					case forward:
						if (GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindForward)) {
							this.key |= 1 << k.ordinal();
						}
						break;
					case jump:
						if (GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindJump)) {
							this.key |= 1 << k.ordinal();
						}
						break;
					default:
						break;
					}
				}
			}
		}
		if (this.key != prev) {
			PacketHandler.sendPacketToServer(new YogpstopPacket(this.key));
			super.setKeys(getPacketPlayer(null), this.key);
		}
	}

	@Override
	public EntityPlayer getPacketPlayer(INetHandler inh) {
		if (inh instanceof NetHandlerPlayServer) return ((NetHandlerPlayServer) inh).playerEntity;
		return Minecraft.getMinecraft().thePlayer;
	}

	@Override
	public int addNewArmourRendererPrefix(String s) {
		return RenderingRegistry.addNewArmourRendererPrefix(s);
	}

	@Override
	public void removeEntity(Entity e) {
		e.worldObj.removeEntity(e);
		if (e.worldObj.isRemote) ((WorldClient) e.worldObj).removeEntityFromWorld(e.getEntityId());
	}

	@Override
	public World getClientWorld() {
		return Minecraft.getMinecraft().theWorld;
	}
}
