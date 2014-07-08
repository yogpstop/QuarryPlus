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

import static buildcraft.core.utils.Utils.addToRandomInventoryAround;
import static buildcraft.core.utils.Utils.addToRandomPipeAround;
import static com.yogpc.qp.PacketHandler.*;

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

import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.gates.IAction;
import buildcraft.core.IMachine;

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
			dos.writeBoolean(id == StC_OPENGUI_FORTUNE ? this.fortuneInclude : this.silktouchInclude);
			List<BlockData> target = id == StC_OPENGUI_FORTUNE ? this.fortuneList : this.silktouchList;
			dos.writeInt(target.size());
			for (BlockData l : target) {
				dos.writeUTF(l.name);
				dos.writeInt(l.meta);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		PacketHandler.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
		PacketHandler.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(ep);
		PacketHandler.channels.get(Side.SERVER).writeOutbound(new QuarryPlusPacket(PacketHandler.Tile, bos.toByteArray()));
	}

	@Override
	protected void S_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		switch (pattern) {
		case CtS_ADD_FORTUNE:
			this.fortuneList.add(new BlockData(data.readUTF(), data.readInt()));
			sendOpenGUI(ep, StC_OPENGUI_FORTUNE);
			break;
		case CtS_REMOVE_FORTUNE:
			this.fortuneList.remove(new BlockData(data.readUTF(), data.readInt()));
			sendOpenGUI(ep, StC_OPENGUI_FORTUNE);
			break;
		case CtS_ADD_SILKTOUCH:
			this.silktouchList.add(new BlockData(data.readUTF(), data.readInt()));
			sendOpenGUI(ep, StC_OPENGUI_SILKTOUCH);
			break;
		case CtS_REMOVE_SILKTOUCH:
			this.silktouchList.remove(new BlockData(data.readUTF(), data.readInt()));
			sendOpenGUI(ep, StC_OPENGUI_SILKTOUCH);
			break;
		case CtS_TOGGLE_FORTUNE:
			this.fortuneInclude = !this.fortuneInclude;
			sendOpenGUI(ep, StC_OPENGUI_FORTUNE);
			break;
		case CtS_TOGGLE_SILKTOUCH:
			this.silktouchInclude = !this.silktouchInclude;
			sendOpenGUI(ep, StC_OPENGUI_SILKTOUCH);
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
		case StC_OPENGUI_FORTUNE:
			this.fortuneInclude = data.readBoolean();
			this.fortuneList.clear();
			int fsize = data.readInt();
			for (int i = 0; i < fsize; i++) {
				this.fortuneList.add(new BlockData(data.readUTF(), data.readInt()));
			}
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdFList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		case StC_OPENGUI_SILKTOUCH:
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
			int added = addToRandomInventoryAround(this.worldObj, this.xCoord, this.yCoord, this.zCoord, is);
			is.stackSize -= added;
			if (is.stackSize > 0) {
				added = addToRandomPipeAround(this.worldObj, this.xCoord, this.yCoord, this.zCoord, ForgeDirection.UNKNOWN, is);
				is.stackSize -= added;
			}
			if (is.stackSize > 0) {
				this.cacheItems.add(is);
				break;
			}
		}
	}

	protected boolean S_breakBlock(int x, int y, int z) {
		Collection<ItemStack> dropped = new LinkedList<ItemStack>();
		Block b = this.worldObj.getBlock(x, y, z);
		if (b == null || b.isAir(this.worldObj, x, y, z)) return true;
		if (TilePump.isLiquid(b, false, null, 0, 0, 0, 0)) {
			TileEntity te = this.worldObj.getTileEntity(this.xCoord + this.pump.offsetX, this.yCoord + this.pump.offsetY, this.zCoord + this.pump.offsetZ);
			if (!(te instanceof TilePump)) {
				this.pump = ForgeDirection.UNKNOWN;
				G_renew_powerConfigure();
				return true;
			}
			if (!TilePump.isLiquid(b, true, this.worldObj, x, y, z, this.worldObj.getBlockMetadata(x, y, z))) return true;
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
				&& (this.silktouchList.contains(new ItemStack(b, meta)) == this.silktouchInclude)) {// TODO
			try {
				list.add((ItemStack) createStackedBlock.invoke(b, meta));
				return -1;
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error e) {
				e.printStackTrace();
			}
		}
		if (this.fortuneList.contains(new ItemStack(b, meta)) == this.fortuneInclude) {// TODO
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
			buf = Block.class.getDeclaredMethod("func_71880_c_", int.class);
			buf.setAccessible(true);
		} catch (Exception e1) {
			try {
				buf = Block.class.getDeclaredMethod("createStackedBlock", int.class);
				buf.setAccessible(true);
			} catch (Exception e2) {
				e1.printStackTrace();
				e2.printStackTrace();
				buf = null;
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
