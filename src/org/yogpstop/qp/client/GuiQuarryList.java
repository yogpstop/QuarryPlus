package org.yogpstop.qp.client;

import org.yogpstop.qp.PacketHandler;
import org.yogpstop.qp.QuarryPlus;
import org.yogpstop.qp.TileQuarry;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;

public class GuiQuarryList extends GuiScreen {
	private GuiSlotQuarryList oreslot;
	private GuiButton delete;
	private TileQuarry quarry;
	private byte targetid;

	public GuiQuarryList(byte id, TileQuarry tq) {
		super();
		this.targetid = id;
		this.quarry = tq;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		this.buttonList
				.add(new GuiButton(TileQuarry.openQuarryGui, this.width / 2 - 125, this.height - 26, 250, 20, StatCollector.translateToLocal("gui.done")));
		this.buttonList.add(new GuiButton(TileQuarry.fortuneTInc + this.targetid - 1, this.width * 2 / 3 + 10, 50, 100, 20, StatCollector
				.translateToLocal(include() ? "tof.include" : "tof.exclude")));
		this.buttonList.add(new GuiButton(-1, this.width * 2 / 3 + 10, 80, 100, 20, StatCollector.translateToLocal("tof.addnewore") + "("
				+ StatCollector.translateToLocal("tof.manualinput") + ")"));
		this.buttonList.add(this.delete = new GuiButton(TileQuarry.fortuneRemove + this.targetid - 1, this.width * 2 / 3 + 10, 110, 100, 20, StatCollector
				.translateToLocal("selectServer.delete")));
		this.oreslot = new GuiSlotQuarryList(this.mc, this.width * 3 / 5, this.height, 30, this.height - 30, 18, this,
				this.targetid == 1 ? this.quarry.fortuneList : this.quarry.silktouchList);
	}

	public boolean include() {
		if (this.targetid == 1) return this.quarry.fortuneInclude;
		return this.quarry.silktouchInclude;
	}

	@Override
	public void actionPerformed(GuiButton par1) {
		switch (par1.id) {
		case -1:
			this.mc.displayGuiScreen(new GuiQuarryManual(this, this.targetid, this.quarry));
			break;
		case TileQuarry.fortuneRemove:
		case TileQuarry.silktouchRemove:
			this.quarry.sendPacketToServer((byte) par1.id, this.oreslot.target.get(this.oreslot.currentore));
			break;
		default:
			this.quarry.sendPacketToServer((byte) par1.id);
			break;
		}
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == 1 || par1 == 'e') {
			PacketHandler.sendOpenGUIPacket(QuarryPlus.guiIdContainerQuarry, this.quarry.xCoord, this.quarry.yCoord, this.quarry.zCoord);
		}
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		drawDefaultBackground();
		this.oreslot.drawScreen(i, j, k);
		String title = StatCollector.translateToLocal("tof.setting");
		this.fontRenderer.drawStringWithShadow(title, (this.width - this.fontRenderer.getStringWidth(title)) / 2, 8, 0xFFFFFF);
		if ((this.targetid == 1 ? this.quarry.fortuneList : this.quarry.silktouchList).isEmpty()) {
			this.delete.enabled = false;
		}
		super.drawScreen(i, j, k);
	}
}
