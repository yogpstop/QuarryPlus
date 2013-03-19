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
        buttonList.add(new GuiButton(TileQuarry.openQuarryGui, this.width / 2 - 125, this.height - 26, 250, 20, StatCollector.translateToLocal("gui.done")));
        buttonList.add(new GuiButton(TileQuarry.fortuneTInc + targetid - 1, this.width * 2 / 3 + 10, 50, 100, 20, StatCollector
                .translateToLocal(include() ? "tof.include" : "tof.exclude")));
        buttonList.add(new GuiButton(-1, this.width * 2 / 3 + 10, 80, 100, 20, StatCollector.translateToLocal("tof.addnewore") + "("
                + StatCollector.translateToLocal("tof.manualinput") + ")"));
        buttonList.add(delete = new GuiButton(TileQuarry.fortuneRemove + targetid - 1, this.width * 2 / 3 + 10, 110, 100, 20, StatCollector
                .translateToLocal("selectServer.delete")));
        oreslot = new GuiSlotQuarryList(mc, this.width * 3 / 5, this.height, 30, this.height - 30, 18, this, targetid == 1 ? quarry.fortuneList
                : quarry.silktouchList);
    }

    public boolean include() {
        if (targetid == 1)
            return quarry.fortuneInclude;
        return quarry.silktouchInclude;
    }

    @Override
    public void actionPerformed(GuiButton par1) {
        switch (par1.id) {
        case -1:
            mc.displayGuiScreen(new GuiQuarryManual(this, targetid, quarry));
            break;
        case TileQuarry.fortuneRemove:
        case TileQuarry.silktouchRemove:
            quarry.sendPacketToServer((byte) par1.id, oreslot.target.get(oreslot.currentore));
            break;
        default:
            quarry.sendPacketToServer((byte) par1.id);
            break;
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        if (par2 == 1 || par1 == 'e') {
            PacketHandler.sendOpenGUIPacket(QuarryPlus.guiIdContainerQuarry, quarry.xCoord, quarry.yCoord, quarry.zCoord);
        }
    }

    @Override
    public void drawScreen(int i, int j, float k) {
        drawDefaultBackground();
        oreslot.drawScreen(i, j, k);
        String title = StatCollector.translateToLocal("tof.setting");
        fontRenderer.drawStringWithShadow(title, (this.width - fontRenderer.getStringWidth(title)) / 2, 8, 0xFFFFFF);
        if ((targetid == 1 ? quarry.fortuneList : quarry.silktouchList).isEmpty()) {
            delete.enabled = false;
        }
        super.drawScreen(i, j, k);
    }
}
