package org.yogpstop.qp;

import org.yogpstop.qp.client.GuiMover;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class ContainerMover extends Container {
	public IInventory craftMatrix = new InventoryBasic("Matrix", false, 2);
	private World worldObj;
	private GuiMover gui;
	private int posX;
	private int posY;
	private int posZ;

	public ContainerMover(EntityPlayer player, World par2World, int par3,
			int par4, int par5, GuiMover gm) {
		this.gui = gm;
		this.worldObj = par2World;
		this.posX = par3;
		this.posY = par4;
		this.posZ = par5;
		int var6;
		int var7;

		for (var6 = 0; var6 < 2; ++var6) {
			this.addSlotToContainer(new SlotMover(this.craftMatrix, var6,
					8 + var6 * 144, 35, this));
		}

		for (var6 = 0; var6 < 3; ++var6) {
			for (var7 = 0; var7 < 9; ++var7) {
				this.addSlotToContainer(new Slot(player.inventory, var7 + var6
						* 9 + 9, 8 + var7 * 18, 84 + var6 * 18));
			}
		}

		for (var6 = 0; var6 < 9; ++var6) {
			this.addSlotToContainer(new Slot(player.inventory, var6,
					8 + var6 * 18, 142));
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
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return this.worldObj.getBlockId(this.posX, this.posY, this.posZ) != QuarryPlus.blockMover.blockID ? false
				: var1.getDistanceSq((double) this.posX + 0.5D,
						(double) this.posY + 0.5D, (double) this.posZ + 0.5D) <= 64.0D;
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		if (gui != null) {
			checkInventory();
		}
	}

	private void moveEnchant(short eid) {
		if (!checkTo(eid))
			return;
		ItemStack is = craftMatrix.getStackInSlot(0);
		NBTTagList list = is.getEnchantmentTagList();
		if (list == null)
			return;
		for (int i = 0; i < list.tagCount(); i++) {
			short id = ((NBTTagCompound) list.tagAt(i)).getShort("id");
			short lvl = ((NBTTagCompound) list.tagAt(i)).getShort("lvl");
			if (lvl < 1)
				continue;
			if (id == eid) {
				NBTTagList nbttl = new NBTTagList();
				for (int j = 0; j < list.tagCount(); j++) {
					if (i == j)
						continue;
					nbttl.appendTag(list.tagAt(j));
				}
				is.getTagCompound().removeTag("ench");
				is.getTagCompound().setTag("ench", nbttl);
				list = nbttl;
				if (lvl > 1) {
					NBTTagCompound ench = new NBTTagCompound();
					ench.setShort("id", id);
					ench.setShort("lvl", --lvl);
					list.appendTag(ench);
				}
				break;
			}
		}
		if (list.tagCount() == 0) {
			is.getTagCompound().removeTag("ench");
			if (is.getTagCompound().hasNoTags())
				is.setTagCompound(null);
		}
		is = craftMatrix.getStackInSlot(1);
		list = is.getEnchantmentTagList();
		if (list == null) {
			if (!is.hasTagCompound())
				is.setTagCompound(new NBTTagCompound());
			NBTTagCompound nbttc = is.getTagCompound();
			list = new NBTTagList();
			nbttc.setTag("ench", list);
		}
		boolean done = false;
		for (int i = 0; i < list.tagCount(); i++) {
			short id = ((NBTTagCompound) list.tagAt(i)).getShort("id");
			short lvl = ((NBTTagCompound) list.tagAt(i)).getShort("lvl");
			if (lvl < 1)
				continue;
			if (id == eid) {
				NBTTagList nbttl = new NBTTagList();
				for (int j = 0; j < list.tagCount(); j++) {
					if (i == j)
						continue;
					nbttl.appendTag(list.tagAt(j));
				}
				is.getTagCompound().removeTag("ench");
				is.getTagCompound().setTag("ench", nbttl);
				list = nbttl;
				NBTTagCompound ench = new NBTTagCompound();
				ench.setShort("id", id);
				ench.setShort("lvl", ++lvl);
				list.appendTag(ench);
				done = true;
				break;
			}
		}
		if (!done) {
			NBTTagCompound ench = new NBTTagCompound();
			ench.setShort("id", (short) eid);
			ench.setShort("lvl", (short) 1);
			list.appendTag(ench);
		}
	}

	private void checkInventory() {
		gui.a1.enabled = false;
		gui.a3.enabled = false;
		gui.a5.enabled = false;
		if (craftMatrix.getStackInSlot(1) == null)
			return;
		ItemStack pickaxeIs = craftMatrix.getStackInSlot(0);
		if (pickaxeIs != null) {
			NBTTagList pickaxeE = pickaxeIs.getEnchantmentTagList();
			if (pickaxeE != null)
				for (int i = 0; i < pickaxeE.tagCount(); i++) {
					short id = ((NBTTagCompound) pickaxeE.tagAt(i))
							.getShort("id");
					short lvl = ((NBTTagCompound) pickaxeE.tagAt(i))
							.getShort("lvl");
					if (!checkTo(id))
						continue;
					if (lvl < 1)
						continue;
					switch (id) {
					case 32:
						gui.a5.enabled = true;
						break;
					case 33:
						gui.a1.enabled = true;
						break;
					case 35:
						gui.a3.enabled = true;
						break;
					}

				}
		}
	}

	private boolean checkTo(short id) {
		ItemStack quarryIs = craftMatrix.getStackInSlot(1);
		if (quarryIs != null) {
			NBTTagList quarryE = quarryIs.getEnchantmentTagList();
			if (quarryE != null) {
				for (int i = 0; i < quarryE.tagCount(); i++) {
					if (id == ((NBTTagCompound) quarryE.tagAt(i))
							.getShort("id")) {
						if (Enchantment.enchantmentsList[id].getMaxLevel() > ((NBTTagCompound) quarryE
								.tagAt(i)).getShort("lvl"))
							return true;
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
		return null;
	}

	public void readPacketData(ByteArrayDataInput data) {
		switch (data.readByte()) {
		case 1:
			moveEnchant((short) 33);
			break;
		case 3:
			moveEnchant((short) 35);
			break;
		case 5:
			moveEnchant((short) 32);
			break;
		}
	}
}
