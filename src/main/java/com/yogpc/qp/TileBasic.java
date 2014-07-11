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
import java.util.Queue;

import com.google.common.io.ByteArrayDataInput;
import com.yogpc.qp.QuarryPlus.BlockData;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.gates.IAction;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.IMachine;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.Transactor;
import buildcraft.energy.TileEngine;

public abstract class TileBasic extends APowerTile implements IMachine, IEnchantableTile {
	protected ForgeDirection pump = ForgeDirection.UNKNOWN;

	public final List<BlockData> fortuneList = new ArrayList<BlockData>();
	public final List<BlockData> silktouchList = new ArrayList<BlockData>();
	public boolean fortuneInclude, silktouchInclude;

	protected byte unbreaking;
	protected byte fortune;
	protected boolean silktouch;
	protected byte efficiency;

	protected final Queue<ItemStack> cacheItems = new LinkedList<ItemStack>();

	void sendOpenGUI(EntityPlayer ep, byte id) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(this.xCoord);
			dos.writeInt(this.yCoord);
			dos.writeInt(this.zCoord);
			dos.writeByte(id);
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
		PacketHandler.sendPacketToPlayer(new QuarryPlusPacket(PacketHandler.Tile, bos.toByteArray()), ep);
	}

	@Override
	protected void S_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		switch (pattern) {
		case PacketHandler.CtS_ADD_FORTUNE:
			this.fortuneList.add(new BlockData(data.readUTF(), data.readInt()));
			sendOpenGUI(ep, PacketHandler.StC_OPENGUI_FORTUNE);
			break;
		case PacketHandler.CtS_REMOVE_FORTUNE:
			this.fortuneList.remove(new BlockData(data.readUTF(), data.readInt()));
			sendOpenGUI(ep, PacketHandler.StC_OPENGUI_FORTUNE);
			break;
		case PacketHandler.CtS_ADD_SILKTOUCH:
			this.silktouchList.add(new BlockData(data.readUTF(), data.readInt()));
			sendOpenGUI(ep, PacketHandler.StC_OPENGUI_SILKTOUCH);
			break;
		case PacketHandler.CtS_REMOVE_SILKTOUCH:
			this.silktouchList.remove(new BlockData(data.readUTF(), data.readInt()));
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
	protected void C_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		switch (pattern) {
		case PacketHandler.StC_OPENGUI_FORTUNE:
			this.fortuneInclude = data.readBoolean();
			this.fortuneList.clear();
			int fsize = data.readInt();
			for (int i = 0; i < fsize; i++) {
				this.fortuneList.add(new BlockData(data.readUTF(), data.readInt()));
			}
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdFList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		case PacketHandler.StC_OPENGUI_SILKTOUCH:
			this.silktouchInclude = data.readBoolean();
			this.silktouchList.clear();
			int ssize = data.readInt();
			for (int i = 0; i < ssize; i++) {
				this.silktouchList.add(new BlockData(data.readUTF(), data.readInt()));
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

	static int injectToNearTile(World w, int x, int y, int z, ItemStack is) {
		List<IPipeTile> pp = new LinkedList<IPipeTile>();
		List<ForgeDirection> ppd = new LinkedList<ForgeDirection>();
		List<ITransactor> pi = new LinkedList<ITransactor>();
		List<ForgeDirection> pid = new LinkedList<ForgeDirection>();
		for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity t = w.getTileEntity(x + d.offsetX, y + d.offsetY, z + d.offsetZ);
			ITransactor i = Transactor.getTransactorFor(t);
			if (i != null && !(t instanceof TileEngine) && i.add(is, d.getOpposite(), false).stackSize > 0) {
				pi.add(i);
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
			return pi.get(i).add(is, pid.get(i), true).stackSize;
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

	@Override
	public final boolean manageFluids() {
		return false;
	}

	@Override
	public final boolean manageSolids() {
		return true;
	}

	@Override
	public final boolean allowAction(IAction action) {
		return false;
	}

	static final Method createStackedBlock;

	static {
		Method buf = null;
		try {
			buf = Block.class.getDeclaredMethod("func_149644_j", int.class);
			buf.setAccessible(true);
		} catch (Exception e1) {
			try {
				buf = Block.class.getDeclaredMethod("func_71880_c_", int.class);
				buf.setAccessible(true);
			} catch (Exception e2) {
				try {
					buf = Block.class.getDeclaredMethod("createStackedBlock", int.class);
					buf.setAccessible(true);
				} catch (Exception e3) {
					e1.printStackTrace();
					e2.printStackTrace();
					e3.printStackTrace();
				}
			}
		}
		createStackedBlock = buf;
	}

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
}
