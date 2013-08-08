package org.yogpstop.qp.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;
import org.yogpstop.qp.ContainerMover;
import org.yogpstop.qp.PacketHandler;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class GuiMover extends GuiContainer {
	public GuiButton a1, a3, a5;
	private static final ResourceLocation gui = new ResourceLocation("yogpstop_qp", "textures/gui/mover.png");

	public GuiMover(EntityPlayer player, World world, int x, int y, int z) {
		super(null);
		this.inventorySlots = new ContainerMover(player, world, x, y, z, this);
	}

	@Override
	public void initGui() {
		super.initGui();
		int i = this.width - this.xSize >> 1;
		int j = this.height - this.ySize >> 1;
		this.a1 = new GuiButton(1, i + 126, j + 20, 20, 15, ">>");
		this.a3 = new GuiButton(3, i + 126, j + 36, 20, 15, ">>");
		this.a5 = new GuiButton(5, i + 126, j + 52, 20, 15, ">>");
		this.buttonList.add(this.a1);
		this.buttonList.add(this.a3);
		this.buttonList.add(this.a5);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		drawString(StatCollector.translateToLocal("tile.EnchantMover.name"), 0);
		drawString(StatCollector.translateToLocal("enchantment.untouching"), 1);
		drawString(StatCollector.translateToLocal("enchantment.lootBonusDigger"), 2);
		drawString(StatCollector.translateToLocal("enchantment.digging"), 3);
		drawString(StatCollector.translateToLocal("container.inventory"), 4);
	}

	private void drawString(String s, int i) {
		this.fontRenderer.drawString(s, (this.xSize - this.fontRenderer.getStringWidth(s)) / 2, (int) (6F + 16.5F * i), 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.func_110434_K().func_110577_a(gui);
		int l = this.width - this.xSize >> 1;
		int i1 = this.height - this.ySize >> 1;
		drawTexturedModalRect(l, i1, 0, 0, this.xSize, this.ySize);
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (!par1GuiButton.enabled) { return; }
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		try {
			dos.writeByte(par1GuiButton.id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = PacketHandler.BTN;
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		packet.isChunkDataPacket = true;

		PacketDispatcher.sendPacketToServer(packet);
	}
}