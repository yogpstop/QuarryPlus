package org.yogpstop.qp;

import java.io.DataOutputStream;

import org.yogpstop.qp.client.GuiQuarry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ContainerQuarry extends Container {
	private IInventory playerInventory;
	private TileQuarry tileQuarry;
	private GuiQuarry gui;
	private World world;
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private int buttonId = -1;

	public ContainerQuarry(EntityPlayer player, World world, int x, int y,
			int z, GuiQuarry gq) {
		this.tileQuarry = (TileQuarry) world.getBlockTileEntity(x, y, z);
		this.playerInventory = player.inventory;
		this.world = world;
		this.xCoord = x;
		this.yCoord = y;
		this.zCoord = z;
		this.gui = gq;

		for (int rows = 0; rows < 3; ++rows) {
			for (int slotIndex = 0; slotIndex < 9; ++slotIndex) {
				addSlotToContainer(new Slot(playerInventory, slotIndex + rows
						* 9 + 9, 48 + slotIndex * 18, 157 + rows * 18));
			}
		}

		for (int slotIndex = 0; slotIndex < 9; ++slotIndex) {
			addSlotToContainer(new Slot(playerInventory, slotIndex,
					48 + slotIndex * 18, 215));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityPlayer) {
		return this.world.getBlockId(this.xCoord, this.yCoord, this.zCoord) != QuarryPlus.blockQuarry.blockID ? false
				: entityPlayer.getDistanceSq((double) this.xCoord + 0.5D,
						(double) this.yCoord + 0.5D,
						(double) this.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
		return null;
	}

	public void onButtonPushed(int buttonId) {
		this.buttonId = (byte) buttonId;
		PacketDispatcher.sendPacketToServer(PacketHandler.getPacket(this));
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		if (gui != null) {
			gui.setNames();
		}
	}

	public void readPacketData(ByteArrayDataInput data, Player p) {
		if (!world.isRemote) {
			try {
				this.buttonId = data.readByte();
			} catch (Exception e) {
				e.printStackTrace();
			}
			switch (buttonId) {
			case 0:
				tileQuarry.reinit();
				break;
			case 3:
				tileQuarry.buildAdvFrame = !tileQuarry.buildAdvFrame;
				break;
			case 4:
				tileQuarry.removeWater = !tileQuarry.removeWater;
				break;
			case 5:
				tileQuarry.removeLava = !tileQuarry.removeLava;
				break;
			case 6:
				tileQuarry.removeLiquid = !tileQuarry.removeLiquid;
				break;
			}
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler
					.getPacket(tileQuarry));
			PacketDispatcher.sendPacketToPlayer(PacketHandler.getPacket(this),
					p);
		} else {
			gui.setNames();
		}
	}

	public void writePacketData(DataOutputStream dos) {
		try {
			dos.writeByte(this.buttonId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}