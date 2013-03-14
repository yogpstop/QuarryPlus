package org.yogpstop.qp.client;

import org.yogpstop.qp.QuarryPlus;
import org.yogpstop.qp.TileQuarry;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.StatCollector;

import static org.yogpstop.qp.QuarryPlus.getname;
import static org.yogpstop.qp.QuarryPlus.data;

public class GuiQuarryManual extends GuiScreen {
	private GuiScreen parent;
	private GuiTextField blockid;
	private GuiTextField meta;
	private byte targetid;
	private TileQuarry quarry;

	public GuiQuarryManual(GuiScreen parents, byte id, TileQuarry tq) {
		parent = parents;
		targetid = id;
		quarry = tq;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		buttonList.add(new GuiButton(-1, this.width / 2 - 150,
				this.height - 26, 140, 20, StatCollector
						.translateToLocal("gui.done")));
		buttonList.add(new GuiButton(-2, this.width / 2 + 10,
				this.height - 26, 140, 20, StatCollector
						.translateToLocal("gui.cancel")));
		blockid = new GuiTextField(fontRenderer, this.width / 2 - 50, 50, 100,
				20);
		meta = new GuiTextField(fontRenderer, this.width / 2 - 50, 80, 100, 20);
		this.blockid.setFocused(true);
	}

	@Override
	public void actionPerformed(GuiButton par1) {
		switch (par1.id) {
		case -1:
			short bid;
			int metaid;
			try {
				bid = Short.parseShort(blockid.getText());
			} catch (Exception e) {
				blockid.setText(StatCollector.translateToLocal("tof.error"));
				return;
			}
			try {
				if (meta.getText().equals(""))
					metaid = 0;
				else
					metaid = Integer.parseInt(meta.getText());
			} catch (Exception e) {
				meta.setText(StatCollector.translateToLocal("tof.error"));
				return;
			}
			if ((targetid == 1 ? quarry.fortuneList : quarry.silktouchList)
					.contains(data(bid, metaid))) {
				mc.displayGuiScreen(new GuiError(this, StatCollector
						.translateToLocal("tof.alreadyerror"), getname(bid,
						metaid)));
				return;
			}
			quarry.sendPacketToServer(
					(byte) (TileQuarry.fortuneAdd + targetid - 1),
					QuarryPlus.data(bid, metaid));
			break;
		case -2:
			mc.displayGuiScreen(parent);
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
			mc.displayGuiScreen(parent);
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
		fontRenderer.drawStringWithShadow(title,
				(this.width - fontRenderer.getStringWidth(title)) / 2, 8,
				0xFFFFFF);
		fontRenderer
				.drawStringWithShadow(
						StatCollector.translateToLocal("tof.blockid"),
						this.width
								/ 2
								- 60
								- fontRenderer.getStringWidth(StatCollector
										.translateToLocal("tof.blockid")), 50,
						0xFFFFFF);
		fontRenderer.drawStringWithShadow(
				StatCollector.translateToLocal("tof.meta"),
				this.width
						/ 2
						- 60
						- fontRenderer.getStringWidth(StatCollector
								.translateToLocal("tof.meta")), 80, 0xFFFFFF);
		fontRenderer.drawString(StatCollector.translateToLocal("tof.tipsmeta"),
				16, 110, 0xFFFFFF);
		this.blockid.drawTextBox();
		this.meta.drawTextBox();
		super.drawScreen(i, j, k);
	}

}
