package org.yogpstop.qp.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSmallButton;
import net.minecraft.util.StatCollector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiYesNo extends GuiScreen {
	private GuiScreen parentScreen;

	private String message1;
	private String message2;

	private String buttonText1;
	private String buttonText2;

	private int oreId;

	public GuiYesNo(GuiScreen par1GuiScreen, String par2Str, String par3Str, int par4) {
		this.parentScreen = par1GuiScreen;
		this.message1 = par2Str;
		this.message2 = par3Str;
		this.buttonText1 = StatCollector.translateToLocal("gui.yes");
		this.buttonText2 = StatCollector.translateToLocal("gui.no");
		this.oreId = par4;
	}

	@Override
	public void initGui() {
		this.buttonList.add(new GuiSmallButton(0, this.width / 2 - 155, this.height / 6 + 96, this.buttonText1));
		this.buttonList.add(new GuiSmallButton(1, this.width / 2 - 155 + 160, this.height / 6 + 96, this.buttonText2));
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		this.parentScreen.confirmClicked(par1GuiButton.id == 0, this.oreId);
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, this.message1, this.width / 2, 70, 16777215);
		this.drawCenteredString(this.fontRenderer, this.message2, this.width / 2, 90, 16777215);
		super.drawScreen(par1, par2, par3);
	}
}
