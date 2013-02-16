package org.yogpstop.qp.client;

import java.util.ArrayList;

import org.yogpstop.qp.PacketHandler;
import org.yogpstop.qp.QuarryPlus;
import org.yogpstop.qp.TileQuarry;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;

public class GuiQuarryList extends GuiScreen {
	private GuiSlotQuarryList oreslot;
	private GuiButton delete;
	private ArrayList<Long> target;
	private TileQuarry quarry;

	public GuiQuarryList(ArrayList<Long> all, TileQuarry tq) {
		super();
		target = all;
		quarry = tq;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		controlList.add(new GuiButton(-1, this.width / 2 - 125,
				this.height - 26, 250, 20, StatCollector
						.translateToLocal("gui.done")));
		controlList.add(new GuiButton(3, this.width * 2 / 3 + 10, 80, 100, 20,
				StatCollector.translateToLocal("tof.addnewore") + "("
						+ StatCollector.translateToLocal("tof.manualinput")
						+ ")"));
		controlList
				.add(delete = new GuiButton(2, this.width * 2 / 3 + 10, 110,
						100, 20, StatCollector
								.translateToLocal("selectServer.delete")));
		oreslot = new GuiSlotQuarryList(mc, this.width * 3 / 5, this.height,
				30, this.height - 30, 18, this, target);
	}

	@Override
	public void actionPerformed(GuiButton par1) {
		switch (par1.id) {
		case -1:
			PacketHandler.sendOpenGUIPacket(QuarryPlus.guiIdContainerQuarry,
					quarry.xCoord, quarry.yCoord, quarry.zCoord);
			break;
		case 2:
			byte listid = 0;
			if (target == quarry.fortuneList)
				listid = 1;
			if (target == quarry.silktouchList)
				listid = 2;
			PacketHandler.sendTileQuarryListPacket(listid, (byte) 2,
					oreslot.target.get(oreslot.currentore), quarry.xCoord,
					quarry.yCoord, quarry.zCoord);
			break;
		case 3:
			mc.displayGuiScreen(new GuiQuarryManual(this, target, quarry));
			break;
		default:
			break;
		}
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == 1 || par1 == 'e') {
			PacketHandler.sendOpenGUIPacket(QuarryPlus.guiIdContainerQuarry,
					quarry.xCoord, quarry.yCoord, quarry.zCoord);
		}
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		drawDefaultBackground();
		oreslot.drawScreen(i, j, k);
		String title = StatCollector.translateToLocal("tof.setting");
		fontRenderer.drawStringWithShadow(title,
				(this.width - fontRenderer.getStringWidth(title)) / 2, 8,
				0xFFFFFF);
		if (target.size() == 0) {
			delete.enabled = false;
		}
		super.drawScreen(i, j, k);
	}
}
