package org.yogpstop.qp;

import java.io.DataOutputStream;
import java.util.HashMap;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;


public class ContainerMover extends Container {
	public IInventory craftMatrix = new InventoryBasic("Matrix", 2);
	public IInventory craftResult = new InventoryBasic("Result", 3);
	public IInventory craftOutput = new InventoryBasic("Output", 3);
	private IInventory playerInventory;
	private World worldObj;
	private int posX;
	private int posY;
	private int posZ;
	private int buttonId = -1;

	public ContainerMover(EntityPlayer player, World par2World, int par3,
			int par4, int par5) {
		this.worldObj = par2World;
		this.playerInventory = player.inventory;
		this.posX = par3;
		this.posY = par4;
		this.posZ = par5;
		int var6;
		int var7;

		for (var6 = 0; var6 < 2; ++var6) {
			this.addSlotToContainer(new SlotMoveMatrix(this.craftMatrix, var6,
					8 + var6 * 36, 35, this));
		}
		for (var6 = 0; var6 < 3; ++var6) {
			this.addSlotToContainer(new SlotMoveResult(this.craftResult, var6,
					80, 10 + var6 * 25));
		}
		for (var6 = 0; var6 < 3; ++var6) {
			this.addSlotToContainer(new SlotMoveOutput(this.craftOutput, var6,
					152, 10 + var6 * 25));
		}

		for (var6 = 0; var6 < 3; ++var6) {
			for (var7 = 0; var7 < 9; ++var7) {
				this.addSlotToContainer(new Slot(this.playerInventory, var7
						+ var6 * 9 + 9, 8 + var7 * 18, 84 + var6 * 18));
			}
		}

		for (var6 = 0; var6 < 9; ++var6) {
			this.addSlotToContainer(new Slot(this.playerInventory, var6,
					8 + var6 * 18, 142));
		}
	}

	@Override
	public void onCraftMatrixChanged(IInventory par1IInventory) {
		int[] cache = canCopyList();
		if (((craftMatrix.getStackInSlot(1) == null) ? 0 : craftMatrix
				.getStackInSlot(1).stackSize) >= cache[0] + cache[1] + cache[2]) {
			if (cache[1] > 0) {
				this.craftResult.setInventorySlotContents(0, new ItemStack(
						QuarryPlus.itemSilktouch, cache[1]));
			} else {
				this.craftResult.setInventorySlotContents(0, null);
			}
			if (cache[2] > 0) {
				this.craftResult.setInventorySlotContents(1, new ItemStack(
						QuarryPlus.itemFortune, cache[2]));
			} else {
				this.craftResult.setInventorySlotContents(1, null);
			}
			if (cache[0] > 0) {
				this.craftResult.setInventorySlotContents(2, new ItemStack(
						QuarryPlus.itemEfficiency, cache[0]));
			} else {
				this.craftResult.setInventorySlotContents(2, null);
			}
		} else {
			this.craftResult.setInventorySlotContents(0, null);
			this.craftResult.setInventorySlotContents(1, null);
			this.craftResult.setInventorySlotContents(2, null);
		}
	}

	@Override
	public void onCraftGuiClosed(EntityPlayer par1EntityPlayer) {
		super.onCraftGuiClosed(par1EntityPlayer);

		if (!this.worldObj.isRemote) {
			for (int var2 = 0; var2 < 2; ++var2) {
				ItemStack var3 = this.craftMatrix.getStackInSlotOnClosing(var2);

				if (var3 != null) {
					par1EntityPlayer.dropPlayerItem(var3);
				}
			}
			for (int var2 = 0; var2 < 3; ++var2) {
				ItemStack var3 = this.craftOutput.getStackInSlotOnClosing(var2);

				if (var3 != null) {
					par1EntityPlayer.dropPlayerItem(var3);
				}
			}
		}
	}

	private int[] canCopyList() {
		if (this.craftMatrix.getStackInSlot(0) == null) {
			return new int[3];
		} else {
			if (this.craftMatrix.getStackInSlot(0).getEnchantmentTagList() != null) {
				int count = this.craftMatrix.getStackInSlot(0)
						.getEnchantmentTagList().tagCount();
				HashMap<Short, Integer> map = new HashMap<Short, Integer>();
				for (int i = 0; i < count; i++) {
					short id = ((NBTTagCompound) this.craftMatrix
							.getStackInSlot(0).getEnchantmentTagList().tagAt(i))
							.getShort("id");
					int lvl = ((NBTTagCompound) this.craftMatrix
							.getStackInSlot(0).getEnchantmentTagList().tagAt(i))
							.getShort("lvl");
					map.put(id, lvl);
				}
				int[] cache = new int[3];
				cache[1] = (map.get((short) Enchantment.silkTouch.effectId) == null) ? 0
						: map.get((short) Enchantment.silkTouch.effectId);
				cache[2] = (map.get((short) Enchantment.fortune.effectId) == null) ? 0
						: map.get((short) Enchantment.fortune.effectId);
				cache[0] = (map.get((short) Enchantment.efficiency.effectId) == null) ? 0
						: map.get((short) Enchantment.efficiency.effectId);
				return cache;
			}

			else
				return new int[3];
		}

	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return this.worldObj.getBlockId(this.posX, this.posY, this.posZ) != QuarryPlus.blockMover.blockID ? false
				: var1.getDistanceSq((double) this.posX + 0.5D,
						(double) this.posY + 0.5D, (double) this.posZ + 0.5D) <= 64.0D;
	}

	public void craft() {
		ItemStack rs1 = this.craftResult.getStackInSlot(0);
		ItemStack rs2 = this.craftResult.getStackInSlot(1);
		ItemStack rs3 = this.craftResult.getStackInSlot(2);
		ItemStack os1 = this.craftOutput.getStackInSlot(0);
		ItemStack os2 = this.craftOutput.getStackInSlot(1);
		ItemStack os3 = this.craftOutput.getStackInSlot(2);
		if (!checkInv(rs1, rs2, rs3, os1, os2, os3))
			return;
		int count = (rs1 == null ? 0 : rs1.stackSize)
				+ (rs2 == null ? 0 : rs2.stackSize)
				+ (rs3 == null ? 0 : rs3.stackSize);

		this.craftOutput.setInventorySlotContents(0,
				((rs1 == null ? 0 : rs1.stackSize) + (os1 == null ? 0
						: os1.stackSize)) == 0 ? null : new ItemStack(
						QuarryPlus.itemSilktouch, (rs1 == null ? 0
								: rs1.stackSize)
								+ (os1 == null ? 0 : os1.stackSize)));
		this.craftOutput.setInventorySlotContents(1,
				((rs2 == null ? 0 : rs2.stackSize) + (os2 == null ? 0
						: os2.stackSize)) == 0 ? null : new ItemStack(
						QuarryPlus.itemFortune, (rs2 == null ? 0
								: rs2.stackSize)
								+ (os2 == null ? 0 : os2.stackSize)));
		this.craftOutput.setInventorySlotContents(2,
				((rs3 == null ? 0 : rs3.stackSize) + (os3 == null ? 0
						: os3.stackSize)) == 0 ? null : new ItemStack(
						QuarryPlus.itemEfficiency, (rs3 == null ? 0
								: rs3.stackSize)
								+ (os3 == null ? 0 : os3.stackSize)));
		this.craftMatrix.setInventorySlotContents(0, null);
		this.craftResult.setInventorySlotContents(0, null);
		this.craftResult.setInventorySlotContents(1, null);
		this.craftResult.setInventorySlotContents(2, null);
		this.craftMatrix
				.setInventorySlotContents(
						1,
						this.craftMatrix.getStackInSlot(1).stackSize - count == 0 ? null
								: new ItemStack(
										this.craftMatrix.getStackInSlot(1)
												.getItem(),
										this.craftMatrix.getStackInSlot(1).stackSize
												- count));
	}

	private boolean checkInv(ItemStack rs1, ItemStack rs2, ItemStack rs3,
			ItemStack os1, ItemStack os2, ItemStack os3) {
		if (rs1 == null && rs2 == null && rs3 == null)
			return false;
		if (((rs1 == null || os1 == null) ? false : rs1.itemID != os1.itemID)
				|| ((rs2 == null || os2 == null) ? false
						: rs2.itemID != os2.itemID)
				|| ((rs3 == null || os3 == null) ? false
						: rs3.itemID != os3.itemID))
			return false;
		if (((rs1 == null ? 0 : rs1.stackSize)
				+ (os1 == null ? 0 : os1.stackSize) > 64)
				|| ((rs2 == null ? 0 : rs2.stackSize)
						+ (os2 == null ? 0 : os2.stackSize) > 64)
				|| ((rs3 == null ? 0 : rs3.stackSize)
						+ (os3 == null ? 0 : os3.stackSize) > 64))
			return false;
		return true;
	}

	@Override
	public void updateCraftingResults() {
		super.updateCraftingResults();

		if (buttonId > -1) {
			if (buttonId == 0) {
				this.craft();
				this.buttonId = -1;
			}
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
		return null;
	}

	public void onButtonPushed(int buttonId) {
		this.buttonId = (byte) buttonId;
		PacketDispatcher.sendPacketToServer(PacketHandler.getPacket(this));
	}

	public void readPacketData(ByteArrayDataInput data) {
		try {
			this.buttonId = data.readByte();
		} catch (Exception e) {
			e.printStackTrace();
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
