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

public class ContainerMover extends Container {
	public IInventory craftMatrix = new InventoryBasic("Matrix", false, 2);
	private World worldObj;
	private GuiMover gui;
	private int posX;
	private int posY;
	private int posZ;
	private EntityPlayer ep;

	public ContainerMover(EntityPlayer player, World par2World, int par3, int par4, int par5, GuiMover gm) {
		this.gui = gm;
		this.worldObj = par2World;
		this.posX = par3;
		this.posY = par4;
		this.posZ = par5;
		this.ep = player;
		int var6;
		int var7;

		for (var6 = 0; var6 < 2; ++var6) {
			this.addSlotToContainer(new SlotMover(this.craftMatrix, var6, 8 + var6 * 144, 35, this));
		}

		for (var6 = 0; var6 < 3; ++var6) {
			for (var7 = 0; var7 < 9; ++var7) {
				this.addSlotToContainer(new Slot(player.inventory, var7 + var6 * 9 + 9, 8 + var7 * 18, 84 + var6 * 18));
			}
		}

		for (var6 = 0; var6 < 9; ++var6) {
			this.addSlotToContainer(new Slot(player.inventory, var6, 8 + var6 * 18, 142));
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		super.onContainerClosed(par1EntityPlayer);

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
		return this.worldObj.getBlockId(this.posX, this.posY, this.posZ) != QuarryPlus.blockMover.blockID ? false : var1.getDistanceSq(this.posX + 0.5D,
				this.posY + 0.5D, this.posZ + 0.5D) <= 64.0D;
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		if (this.gui != null) {
			checkInventory();
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer pl, int i) {
		return null;
	}

	void moveEnchant(short eid) {
		if (!checkTo(eid)) return;
		ItemStack is;
		NBTTagList list;
		if (!this.ep.capabilities.isCreativeMode) {
			is = this.craftMatrix.getStackInSlot(0);
			list = is.getEnchantmentTagList();
			if (list == null) return;
			for (int i = 0; i < list.tagCount(); i++) {
				short lvl = ((NBTTagCompound) list.tagAt(i)).getShort("lvl");
				if (lvl < 1) continue;
				if (((NBTTagCompound) list.tagAt(i)).getShort("id") == eid) {
					if (lvl > 1) ((NBTTagCompound) list.tagAt(i)).setShort("lvl", --lvl);
					else {
						NBTTagList nlist = new NBTTagList();
						for (int j = 0; j < list.tagCount(); j++) {
							if (i != j) nlist.appendTag(list.tagAt(j));
						}
						is.getTagCompound().setTag("ench", nlist);
						list = is.getEnchantmentTagList();
					}
					break;
				}
			}
			if (list.tagCount() == 0) {
				is.getTagCompound().removeTag("ench");
				if (is.getTagCompound().hasNoTags()) is.setTagCompound(null);
			}
		}
		is = this.craftMatrix.getStackInSlot(1);
		list = is.getEnchantmentTagList();
		boolean done = false;
		if (list == null) {
			if (!is.hasTagCompound()) is.setTagCompound(new NBTTagCompound());
			NBTTagCompound nbttc = is.getTagCompound();
			list = new NBTTagList();
			nbttc.setTag("ench", list);
		} else {
			for (int i = 0; i < list.tagCount(); i++) {
				short id = ((NBTTagCompound) list.tagAt(i)).getShort("id");
				short lvl = ((NBTTagCompound) list.tagAt(i)).getShort("lvl");
				if (lvl < 1) continue;
				if (id == eid) {
					((NBTTagCompound) list.tagAt(i)).setShort("lvl", ++lvl);
					done = true;
					break;
				}
			}
		}
		if (!done) {
			NBTTagCompound ench = new NBTTagCompound();
			ench.setShort("id", eid);
			ench.setShort("lvl", (short) 1);
			list.appendTag(ench);
		}
	}

	private void checkInventory() {
		this.gui.b32.enabled = this.gui.b33.enabled = this.gui.b34.enabled = this.gui.b35.enabled = false;
		if (this.craftMatrix.getStackInSlot(1) == null) return;
		ItemStack pickaxeIs = this.craftMatrix.getStackInSlot(0);
		if (pickaxeIs != null) {
			NBTTagList pickaxeE = pickaxeIs.getEnchantmentTagList();
			if (pickaxeE != null) for (int i = 0; i < pickaxeE.tagCount(); i++) {
				short id = ((NBTTagCompound) pickaxeE.tagAt(i)).getShort("id");
				short lvl = ((NBTTagCompound) pickaxeE.tagAt(i)).getShort("lvl");
				if (!checkTo(id)) continue;
				if (lvl < 1) continue;
				switch (id) {
				case 32:
					this.gui.b32.enabled = true;
					break;
				case 33:
					this.gui.b33.enabled = true;
					break;
				case 34:
					this.gui.b34.enabled = true;
					break;
				case 35:
					this.gui.b35.enabled = true;
					break;
				}

			}
		} else if (this.ep.capabilities.isCreativeMode) {
			if (checkTo((short) 32)) this.gui.b32.enabled = true;
			if (checkTo((short) 33)) this.gui.b33.enabled = true;
			if (checkTo((short) 34)) this.gui.b34.enabled = true;
			if (checkTo((short) 35)) this.gui.b35.enabled = true;
		}
	}

	private boolean checkTo(final short id) {
		ItemStack target = this.craftMatrix.getStackInSlot(1);
		if (target != null) {
			if (!(target.getItem() instanceof IEnchantableItem) || !((IEnchantableItem) target.getItem()).canMove(target, id, target.getItemDamage())) return false;
			NBTTagList quarryE = target.getEnchantmentTagList();
			if (quarryE != null) for (int i = 0; i < quarryE.tagCount(); i++) {
				if (id == ((NBTTagCompound) quarryE.tagAt(i)).getShort("id")) {
					if (Enchantment.enchantmentsList[id].getMaxLevel() > ((NBTTagCompound) quarryE.tagAt(i)).getShort("lvl")) return true;
					return false;
				}
			}
		}
		return true;
	}
}
