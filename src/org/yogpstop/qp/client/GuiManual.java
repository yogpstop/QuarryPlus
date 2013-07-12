package org.yogpstop.qp.client;

import org.yogpstop.qp.PacketHandler;
import org.yogpstop.qp.QuarryPlus;
import org.yogpstop.qp.TileBasic;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.StatCollector;

import static org.yogpstop.qp.QuarryPlus.getname;
import static org.yogpstop.qp.QuarryPlus.data;

public class GuiManual extends GuiScreen {
	private GuiScreen parent;
	private GuiTextField blockid;
	private GuiTextField meta;
	private byte targetid;
	private TileBasic tile;

	public GuiManual(GuiScreen parents, byte id, TileBasic tq) {
		this.parent = parents;
		this.targetid = id;
		this.tile = tq;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(-1, this.width / 2 - 150, this.height - 26, 140, 20, StatCollector.translateToLocal("gui.done")));
		this.buttonList.add(new GuiButton(-2, this.width / 2 + 10, this.height - 26, 140, 20, StatCollector.translateToLocal("gui.cancel")));
		this.blockid = new GuiTextField(this.fontRenderer, this.width / 2 - 50, 50, 100, 20);
		this.meta = new GuiTextField(this.fontRenderer, this.width / 2 - 50, 80, 100, 20);
		this.blockid.setFocused(true);
	}

	@Override
	public void actionPerformed(GuiButton par1) {
		switch (par1.id) {
		case -1:
			short bid;
			int metaid;
			try {
				bid = Short.parseShort(this.blockid.getText());
			} catch (Exception e) {
				this.blockid.setText(StatCollector.translateToLocal("tof.error"));
				return;
			}
			try {
				if (this.meta.getText().equals("")) metaid = 0;
				else metaid = Integer.parseInt(this.meta.getText());
			} catch (Exception e) {
				this.meta.setText(StatCollector.translateToLocal("tof.error"));
				return;
			}
			if ((this.targetid == 1 ? this.tile.fortuneList : this.tile.silktouchList).contains(data(bid, metaid))) {
				this.mc.displayGuiScreen(new GuiError(this, StatCollector.translateToLocal("tof.alreadyerror"), getname(bid, metaid)));
				return;
			}
			PacketHandler.sendTilePacketToServer(this.tile, (byte) (PacketHandler.fortuneAdd + this.targetid - 1), QuarryPlus.data(bid, metaid));
			break;
		case -2:
			this.mc.displayGuiScreen(this.parent);
			break;
		}
	}

	@Override
	public void updateScreen() {
		this.meta.updateCursorCounter();
		this.blockid.updateCursorCounter();
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (this.blockid.isFocused()) {
			this.blockid.textboxKeyTyped(par1, par2);
			return;
		} else if (this.meta.isFocused()) {
			this.meta.textboxKeyTyped(par1, par2);
			return;
		}
		if (par2 == 1 || par1 == 'e') {
			this.mc.displayGuiScreen(this.parent);
		}
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);

		this.blockid.mouseClicked(par1, par2, par3);
		this.meta.mouseClicked(par1, par2, par3);
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		drawDefaultBackground();
		String title = StatCollector.translateToLocal("tof.selectblock");
		this.fontRenderer.drawStringWithShadow(title, (this.width - this.fontRenderer.getStringWidth(title)) / 2, 8, 0xFFFFFF);
		this.fontRenderer.drawStringWithShadow(StatCollector.translateToLocal("tof.blockid"),
				this.width / 2 - 60 - this.fontRenderer.getStringWidth(StatCollector.translateToLocal("tof.blockid")), 50, 0xFFFFFF);
		this.fontRenderer.drawStringWithShadow(StatCollector.translateToLocal("tof.meta"),
				this.width / 2 - 60 - this.fontRenderer.getStringWidth(StatCollector.translateToLocal("tof.meta")), 80, 0xFFFFFF);
		this.fontRenderer.drawString(StatCollector.translateToLocal("tof.tipsmeta"), 16, 110, 0xFFFFFF);
		this.blockid.drawTextBox();
		this.meta.drawTextBox();
		super.drawScreen(i, j, k);
	}

}
