/*
 * Copyright (C) 2012,2013 yogpstop
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the
 * GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.yogpstop.qp.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.yogpstop.qp.PacketHandler;
import org.yogpstop.qp.TilePump;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiP_SelectSide extends GuiScreenA {
	private TilePump tile;
	private boolean copy;
	private byte to;

	public GuiP_SelectSide(TilePump ptile, GuiP_List pparent, boolean pcopy) {
		super(pparent);
		this.tile = ptile;
		this.copy = pcopy;
		this.to = pparent.dir;
	}

	@Override
	public void initGui() {
		this.buttonList
				.add(new GuiButton(ForgeDirection.UP.ordinal(), this.width / 2 - 50, this.height / 2 - 60, 100, 20, StatCollector.translateToLocal("up")));
		this.buttonList.add(new GuiButton(ForgeDirection.DOWN.ordinal(), this.width / 2 - 50, this.height / 2 + 40, 100, 20, StatCollector
				.translateToLocal("down")));
		this.buttonList.add(new GuiButton(ForgeDirection.SOUTH.ordinal(), this.width / 2 - 50, this.height / 2 + 15, 100, 20, StatCollector
				.translateToLocal("south")));
		this.buttonList.add(new GuiButton(ForgeDirection.NORTH.ordinal(), this.width / 2 - 50, this.height / 2 - 35, 100, 20, StatCollector
				.translateToLocal("north")));
		this.buttonList.add(new GuiButton(ForgeDirection.EAST.ordinal(), this.width / 2 + 40, this.height / 2 - 10, 100, 20, StatCollector
				.translateToLocal("east")));
		this.buttonList.add(new GuiButton(ForgeDirection.WEST.ordinal(), this.width / 2 - 140, this.height / 2 - 10, 100, 20, StatCollector
				.translateToLocal("west")));
	}

	@Override
	public void actionPerformed(GuiButton par1) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeInt(this.tile.xCoord);
			dos.writeInt(this.tile.yCoord);
			dos.writeInt(this.tile.zCoord);
			if (this.copy) {
				dos.writeByte(PacketHandler.CtS_COPY_MAPPING);
				dos.writeByte(par1.id);
				dos.writeByte(this.to);
			} else {
				dos.writeByte(PacketHandler.CtS_RENEW_DIRECTION);
				dos.writeByte(par1.id);
			}
			PacketDispatcher.sendPacketToServer(PacketHandler.composeTilePacket(bos));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, StatCollector.translateToLocal(this.copy ? "pp.copy.select" : "pp.set.select"), this.width / 2, 8, 0xFFFFFF);
		super.drawScreen(i, j, k);
	}

}
