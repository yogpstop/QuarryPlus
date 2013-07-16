package org.yogpstop.qp.client;

import org.yogpstop.qp.PacketHandler;
import org.yogpstop.qp.TileBasic;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;

public class GuiList extends GuiScreen {
	private GuiSlotList oreslot;
	private GuiButton delete;
	private TileBasic tile;
	private byte targetid;

	public GuiList(byte id, TileBasic tq) {
		super();
		this.targetid = id;
		this.tile = tq;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(PacketHandler.fortuneTInc + this.targetid - 1, this.width * 2 / 3 + 10, 50, 100, 20, StatCollector
				.translateToLocal(include() ? "tof.include" : "tof.exclude")));
		this.buttonList.add(new GuiButton(-1, this.width * 2 / 3 + 10, 80, 100, 20, StatCollector.translateToLocal("tof.addnewore") + "("
				+ StatCollector.translateToLocal("tof.manualinput") + ")"));
		this.buttonList.add(this.delete = new GuiButton(PacketHandler.fortuneRemove + this.targetid - 1, this.width * 2 / 3 + 10, 110, 100, 20, StatCollector
				.translateToLocal("selectServer.delete")));
		this.oreslot = new GuiSlotList(this.mc, this.width * 3 / 5, this.height, 30, this.height - 30, 18, this, this.targetid == 1 ? this.tile.fortuneList
				: this.tile.silktouchList);
	}

	public boolean include() {
		if (this.targetid == 1) return this.tile.fortuneInclude;
		return this.tile.silktouchInclude;
	}

	@Override
	public void actionPerformed(GuiButton par1) {
		switch (par1.id) {
		case -1:
			this.mc.displayGuiScreen(new GuiManual(this, this.targetid, this.tile));
			break;
		case PacketHandler.fortuneRemove:
		case PacketHandler.silktouchRemove:
			PacketHandler.sendPacketToServer(this.tile, (byte) par1.id, this.oreslot.target.get(this.oreslot.currentore));
			break;
		default:
			PacketHandler.sendPacketToServer(this.tile, (byte) par1.id);
			break;
		}
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		drawDefaultBackground();
		this.oreslot.drawScreen(i, j, k);
		String title = StatCollector.translateToLocal("tof.setting")+(this.targetid==1?" Fortune":" Silktouch");
		this.fontRenderer.drawStringWithShadow(title, (this.width - this.fontRenderer.getStringWidth(title)) / 2, 8, 0xFFFFFF);
		if ((this.targetid == 1 ? this.tile.fortuneList : this.tile.silktouchList).isEmpty()) {
			this.delete.enabled = false;
		}
		super.drawScreen(i, j, k);
	}
}
