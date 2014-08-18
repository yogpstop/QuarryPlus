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

package com.yogpc.qp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.yogpc.mc_lib.PacketHandler;
import com.yogpc.mc_lib.ReflectionHelper;
import com.yogpc.mc_lib.YogpstopPacket;
import com.yogpc.qp.QuarryPlus.BlockData;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.transport.IPipeTile;

public abstract class TileBasic extends APowerTile implements IInventory, IEnchantableTile {
	protected ForgeDirection pump = ForgeDirection.UNKNOWN;

	public final List<BlockData> fortuneList = new ArrayList<BlockData>();
	public final List<BlockData> silktouchList = new ArrayList<BlockData>();
	public boolean fortuneInclude, silktouchInclude;

	protected byte unbreaking;
	protected byte fortune;
	protected boolean silktouch;
	protected byte efficiency;

	protected final LinkedList<ItemStack> cacheItems = new LinkedList<ItemStack>();

	void sendOpenGUI(EntityPlayer ep, byte id) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeBoolean(id == PacketHandler.StC_OPENGUI_FORTUNE ? this.fortuneInclude : this.silktouchInclude);
			List<BlockData> target = id == PacketHandler.StC_OPENGUI_FORTUNE ? this.fortuneList : this.silktouchList;
			dos.writeInt(target.size());
			for (BlockData l : target) {
				dos.writeUTF(l.name);
				dos.writeInt(l.meta);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		PacketHandler.sendPacketToPlayer(new YogpstopPacket(bos.toByteArray(), this, id), ep);
	}

	@Override
	protected void S_recievePacket(byte id, byte[] data, EntityPlayer ep) {
		ByteArrayDataInput badi = ByteStreams.newDataInput(data);
		switch (id) {
		case PacketHandler.CtS_ADD_FORTUNE:
			this.fortuneList.add(new BlockData(badi.readUTF(), badi.readInt()));
			sendOpenGUI(ep, PacketHandler.StC_OPENGUI_FORTUNE);
			break;
		case PacketHandler.CtS_REMOVE_FORTUNE:
			this.fortuneList.remove(new BlockData(badi.readUTF(), badi.readInt()));
			sendOpenGUI(ep, PacketHandler.StC_OPENGUI_FORTUNE);
			break;
		case PacketHandler.CtS_ADD_SILKTOUCH:
			this.silktouchList.add(new BlockData(badi.readUTF(), badi.readInt()));
			sendOpenGUI(ep, PacketHandler.StC_OPENGUI_SILKTOUCH);
			break;
		case PacketHandler.CtS_REMOVE_SILKTOUCH:
			this.silktouchList.remove(new BlockData(badi.readUTF(), badi.readInt()));
			sendOpenGUI(ep, PacketHandler.StC_OPENGUI_SILKTOUCH);
			break;
		case PacketHandler.CtS_TOGGLE_FORTUNE:
			this.fortuneInclude = !this.fortuneInclude;
			sendOpenGUI(ep, PacketHandler.StC_OPENGUI_FORTUNE);
			break;
		case PacketHandler.CtS_TOGGLE_SILKTOUCH:
			this.silktouchInclude = !this.silktouchInclude;
			sendOpenGUI(ep, PacketHandler.StC_OPENGUI_SILKTOUCH);
			break;
		}
	}

	protected abstract void G_renew_powerConfigure();

	protected abstract void G_destroy();

	@Override
	public final void invalidate() {
		G_destroy();
		super.invalidate();
	}

	@Override
	public final void onChunkUnload() {
		G_destroy();
		super.onChunkUnload();
	}

	@Override
	protected void C_recievePacket(byte id, byte[] data, EntityPlayer ep) {
		ByteArrayDataInput badi = ByteStreams.newDataInput(data);
		switch (id) {
		case PacketHandler.StC_OPENGUI_FORTUNE:
			this.fortuneInclude = badi.readBoolean();
			this.fortuneList.clear();
			int fsize = badi.readInt();
			for (int i = 0; i < fsize; i++) {
				this.fortuneList.add(new BlockData(badi.readUTF(), badi.readInt()));
			}
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdFList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		case PacketHandler.StC_OPENGUI_SILKTOUCH:
			this.silktouchInclude = badi.readBoolean();
			this.silktouchList.clear();
			int ssize = badi.readInt();
			for (int i = 0; i < ssize; i++) {
				this.silktouchList.add(new BlockData(badi.readUTF(), badi.readInt()));
			}
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdSList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		}
	}

	protected void S_pollItems() {
		ItemStack is;
		while (null != (is = this.cacheItems.poll())) {
			is.stackSize -= injectToNearTile(this.worldObj, this.xCoord, this.yCoord, this.zCoord, is);
			if (is.stackSize > 0) {
				this.cacheItems.add(is);
				break;
			}
		}
	}

	static final int addToIInv(IInventory ii, ItemStack is1, ForgeDirection fd, boolean doAdd) {
		IInventory iii;
		int[] a;
		if (ii instanceof TileEntityChest) {
			TileEntityChest chest = (TileEntityChest) ii;
			TileEntityChest adjacent = null;
			if (chest.adjacentChestXNeg != null) adjacent = chest.adjacentChestXNeg;
			if (chest.adjacentChestXPos != null) adjacent = chest.adjacentChestXPos;
			if (chest.adjacentChestZNeg != null) adjacent = chest.adjacentChestZNeg;
			if (chest.adjacentChestZPos != null) adjacent = chest.adjacentChestZPos;
			if (adjacent != null) iii = new InventoryLargeChest("", ii, adjacent);
			else iii = ii;
		} else {
			iii = ii;
		}
		if (iii instanceof ISidedInventory) {
			a = ((ISidedInventory) iii).getAccessibleSlotsFromSide(fd.ordinal());
		} else {
			a = new int[iii.getSizeInventory()];
			for (int i = 0; i < a.length; i++)
				a[i] = i;
		}
		int moved = 0, buf;
		for (int i : a) {
			ItemStack is2 = iii.getStackInSlot(i);
			if (iii instanceof ISidedInventory) {
				if (!((ISidedInventory) iii).canInsertItem(i, is2, fd.ordinal())) continue;
			} else {
				if (!iii.isItemValidForSlot(i, is2)) continue;
			}
			if (is2 == null) continue;
			if (!is2.isItemEqual(is1)) continue;
			if (!ItemStack.areItemStackTagsEqual(is2, is1)) continue;
			buf = Math.min(iii.getInventoryStackLimit(), Math.min(is2.stackSize + is1.stackSize, is2.getMaxStackSize()));
			if (buf > is2.stackSize) {
				moved += buf - is2.stackSize;
				if (doAdd) is2.stackSize = buf;
				if (moved >= is1.stackSize) break;
			}
		}
		if (doAdd) is1.stackSize -= moved;
		return moved;
	}

	static int injectToNearTile(World w, int x, int y, int z, ItemStack is) {
		List<IPipeTile> pp = new LinkedList<IPipeTile>();
		List<ForgeDirection> ppd = new LinkedList<ForgeDirection>();
		List<IInventory> pi = new LinkedList<IInventory>();
		List<ForgeDirection> pid = new LinkedList<ForgeDirection>();
		for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity t = w.getTileEntity(x + d.offsetX, y + d.offsetY, z + d.offsetZ);
			if ((t instanceof IInventory) && !(t instanceof IPowerEmitter) && addToIInv((IInventory) t, is, d.getOpposite(), false) > 0) {
				pi.add((IInventory) t);
				pid.add(d.getOpposite());
			}
			if (t instanceof IPipeTile) {
				IPipeTile p = (IPipeTile) t;
				if (p.getPipeType() != IPipeTile.PipeType.ITEM || !p.isPipeConnected(d.getOpposite())) continue;
				pp.add(p);
				ppd.add(d.getOpposite());
			}
		}
		if (pi.size() > 0) {
			int i = w.rand.nextInt(pi.size());
			return addToIInv(pi.get(i), is, pid.get(i), true);
		}
		if (pp.size() > 0) {
			int i = w.rand.nextInt(pp.size());
			return pp.get(i).injectItem(is, true, ppd.get(i));
		}
		return 0;
	}

	protected boolean S_breakBlock(int x, int y, int z) {
		Collection<ItemStack> dropped = new LinkedList<ItemStack>();
		Block b = this.worldObj.getChunkProvider().loadChunk(x >> 4, z >> 4).getBlock(x & 0xF, y, z & 0xF);
		if (b == null || b.isAir(this.worldObj, x, y, z)) return true;
		if (TilePump.isLiquid(b, false, null, 0, 0, 0, 0)) {
			TileEntity te = this.worldObj.getTileEntity(this.xCoord + this.pump.offsetX, this.yCoord + this.pump.offsetY, this.zCoord + this.pump.offsetZ);
			if (!(te instanceof TilePump)) {
				this.pump = ForgeDirection.UNKNOWN;
				G_renew_powerConfigure();
				return true;
			}
			return ((TilePump) te).S_removeLiquids(this, x, y, z);
		}
		if (!PowerManager.useEnergyB(this, b.getBlockHardness(this.worldObj, x, y, z), S_addDroppedItems(dropped, b, x, y, z), this.unbreaking, this)) return false;
		this.cacheItems.addAll(dropped);
		this.worldObj.playAuxSFXAtEntity(null, 2001, x, y, z, Block.getIdFromBlock(b) | (this.worldObj.getBlockMetadata(x, y, z) << 12));
		this.worldObj.setBlockToAir(x, y, z);
		return true;
	}

	boolean S_connect(ForgeDirection fd) {
		TileEntity te = this.worldObj.getTileEntity(this.xCoord + this.pump.offsetX, this.yCoord + this.pump.offsetY, this.zCoord + this.pump.offsetZ);
		if (te instanceof TilePump && this.pump != fd) return false;
		this.pump = fd;
		G_renew_powerConfigure();
		return true;
	}

	private byte S_addDroppedItems(Collection<ItemStack> list, Block b, int x, int y, int z) {
		int meta = this.worldObj.getBlockMetadata(x, y, z);
		if (b.canSilkHarvest(this.worldObj, null, x, y, z, meta) && this.silktouch
				&& (this.silktouchList.contains(new BlockData(GameData.getBlockRegistry().getNameForObject(b), meta)) == this.silktouchInclude)) {
			try {
				list.add((ItemStack) createStackedBlock.invoke(b, meta));
				return -1;
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error e) {
				e.printStackTrace();
			}
		}
		if (this.fortuneList.contains(new BlockData(GameData.getBlockRegistry().getNameForObject(b), meta)) == this.fortuneInclude) {
			list.addAll(b.getDrops(this.worldObj, x, y, z, meta, this.fortune));
			return this.fortune;
		}
		list.addAll(b.getDrops(this.worldObj, x, y, z, meta, 0));
		return 0;
	}

	static final Method createStackedBlock = ReflectionHelper.getDeclaredMethod(Block.class, new String[] { "func_149644_j", "createStackedBlock" },
			new Class<?>[] { int.class });

	@Override
	public void readFromNBT(NBTTagCompound nbttc) {
		super.readFromNBT(nbttc);
		this.silktouch = nbttc.getBoolean("silktouch");
		this.fortune = nbttc.getByte("fortune");
		this.efficiency = nbttc.getByte("efficiency");
		this.unbreaking = nbttc.getByte("unbreaking");
		this.fortuneInclude = nbttc.getBoolean("fortuneInclude");
		this.silktouchInclude = nbttc.getBoolean("silktouchInclude");
		readLongCollection(nbttc.getTagList("fortuneList", 10), this.fortuneList);
		readLongCollection(nbttc.getTagList("silktouchList", 10), this.silktouchList);
	}

	private static void readLongCollection(NBTTagList nbttl, Collection<BlockData> target) {
		target.clear();
		for (int i = 0; i < nbttl.tagCount(); i++) {
			NBTTagCompound c = nbttl.getCompoundTagAt(i);
			target.add(new BlockData(c.getString("name"), c.getInteger("meta")));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttc) {
		super.writeToNBT(nbttc);
		nbttc.setBoolean("silktouch", this.silktouch);
		nbttc.setByte("fortune", this.fortune);
		nbttc.setByte("efficiency", this.efficiency);
		nbttc.setByte("unbreaking", this.unbreaking);
		nbttc.setBoolean("fortuneInclude", this.fortuneInclude);
		nbttc.setBoolean("silktouchInclude", this.silktouchInclude);
		nbttc.setTag("fortuneList", writeLongCollection(this.fortuneList));
		nbttc.setTag("silktouchList", writeLongCollection(this.silktouchList));
	}

	private static NBTTagList writeLongCollection(Collection<BlockData> target) {
		NBTTagList nbttl = new NBTTagList();
		for (BlockData l : target) {
			NBTTagCompound c = new NBTTagCompound();
			c.setString("name", l.name);
			c.setInteger("meta", l.meta);
			nbttl.appendTag(c);
		}
		return nbttl;
	}

	@Override
	public byte getEfficiency() {
		return this.efficiency;
	}

	@Override
	public byte getFortune() {
		return this.fortune;
	}

	@Override
	public byte getUnbreaking() {
		return this.unbreaking;
	}

	@Override
	public boolean getSilktouch() {
		return this.silktouch;
	}

	@Override
	public void set(byte pefficiency, byte pfortune, byte punbreaking, boolean psilktouch) {
		this.efficiency = pefficiency;
		this.fortune = pfortune;
		this.unbreaking = punbreaking;
		this.silktouch = psilktouch;
	}

	@Override
	public int getSizeInventory() {
		return Math.max(1, this.cacheItems.size());
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return (i < 0 || i >= this.cacheItems.size()) ? null : this.cacheItems.get(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int a) {
		ItemStack from = this.cacheItems.get(i);
		ItemStack res = new ItemStack(from.getItem(), Math.min(a, from.stackSize), from.getItemDamage());
		if (from.stackTagCompound != null) res.stackTagCompound = (NBTTagCompound) from.stackTagCompound.copy();
		from.stackSize -= res.stackSize;
		if (from.stackSize == 0) this.cacheItems.remove(i);
		return res;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return this.cacheItems.get(i);
	}

	@Override
	public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {}

	@Override
	public String getInventoryName() {
		return null;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 0;
	}

	@Override
	public void markDirty() {}

	@Override
	public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {
		return false;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
		return false;
	}
}
