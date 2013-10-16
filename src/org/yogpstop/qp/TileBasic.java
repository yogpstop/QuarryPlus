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

import static org.yogpstop.qp.QuarryPlus.data;
import static org.yogpstop.qp.PacketHandler.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.IFluidBlock;
import buildcraft.api.gates.IAction;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.core.IMachine;

public abstract class TileBasic extends APacketTile implements IPowerReceptor, IMachine, IEnchantableTile {
	protected ForgeDirection pump = ForgeDirection.UNKNOWN;

	protected PowerHandler pp = new PowerHandler(this, PowerHandler.Type.MACHINE);

	public final List<Long> fortuneList = new ArrayList<Long>();
	public final List<Long> silktouchList = new ArrayList<Long>();
	public boolean fortuneInclude, silktouchInclude;

	protected byte unbreaking;
	protected byte fortune;
	protected boolean silktouch;
	protected byte efficiency;

	protected final List<ItemStack> cacheItems = new LinkedList<ItemStack>();

	void sendOpenGUI(EntityPlayer ep, byte id) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(this.xCoord);
			dos.writeInt(this.yCoord);
			dos.writeInt(this.zCoord);
			dos.writeByte(id);
			dos.writeBoolean(id == StC_OPENGUI_FORTUNE ? this.fortuneInclude : this.silktouchInclude);
			List<Long> target = id == StC_OPENGUI_FORTUNE ? this.fortuneList : this.silktouchList;
			dos.writeInt(target.size());
			for (Long l : target)
				dos.writeLong(l);
		} catch (Exception e) {
			e.printStackTrace();
		}
		PacketDispatcher.sendPacketToPlayer(composeTilePacket(bos), (Player) ep);
	}

	@Override
	protected void S_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		switch (pattern) {
		case CtS_ADD_FORTUNE:
			this.fortuneList.add(data.readLong());
			sendOpenGUI(ep, StC_OPENGUI_FORTUNE);
			break;
		case CtS_REMOVE_FORTUNE:
			this.fortuneList.remove(data.readLong());
			sendOpenGUI(ep, StC_OPENGUI_FORTUNE);
			break;
		case CtS_ADD_SILKTOUCH:
			this.silktouchList.add(data.readLong());
			sendOpenGUI(ep, StC_OPENGUI_SILKTOUCH);
			break;
		case CtS_REMOVE_SILKTOUCH:
			this.silktouchList.remove(data.readLong());
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
				this.fortuneList.add(data.readLong());
			}
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdFList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		case StC_OPENGUI_SILKTOUCH:
			this.silktouchInclude = data.readBoolean();
			this.silktouchList.clear();
			int ssize = data.readInt();
			for (int i = 0; i < ssize; i++) {
				this.silktouchList.add(data.readLong());
			}
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdSList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		}
	}

	protected boolean S_breakBlock(int x, int y, int z, PowerManager.BreakType t) {
		Collection<ItemStack> dropped = new LinkedList<ItemStack>();
		Block b = Block.blocksList[this.worldObj.getBlockId(x, y, z)];
		if (b instanceof IFluidBlock || b == Block.waterStill || b == Block.waterMoving || b == Block.lavaStill || b == Block.lavaMoving) {
			TileEntity te = this.worldObj.getBlockTileEntity(this.xCoord + this.pump.offsetX, this.yCoord + this.pump.offsetY, this.zCoord + this.pump.offsetZ);
			if (!(te instanceof TilePump)) {
				this.pump = ForgeDirection.UNKNOWN;
				G_renew_powerConfigure();
				return true;
			}
			return ((TilePump) te).S_removeLiquids(this.pp, x, y, z);
		}
		if (!PowerManager.useEnergyB(this.pp, S_blockHardness(x, y, z), S_addDroppedItems(dropped, x, y, z, t), this.unbreaking, t)) return false;
		this.cacheItems.addAll(dropped);
		this.worldObj.playAuxSFXAtEntity(null, 2001, x, y, z, this.worldObj.getBlockId(x, y, z) | (this.worldObj.getBlockMetadata(x, y, z) << 12));
		this.worldObj.setBlockToAir(x, y, z);

		return true;
	}

	boolean S_connect(ForgeDirection fd) {
		TileEntity te = this.worldObj.getBlockTileEntity(this.xCoord + this.pump.offsetX, this.yCoord + this.pump.offsetY, this.zCoord + this.pump.offsetZ);
		if (te instanceof TilePump && this.pump != fd) return false;
		this.pump = fd;
		G_renew_powerConfigure();
		return true;
	}

	protected float S_blockHardness(int x, int y, int z) {
		Block b = Block.blocksList[this.worldObj.getBlockId(x, y, z)];
		if (b != null) {
			if (this.worldObj.getBlockMaterial(x, y, z).isLiquid()) return 0;
			return b.getBlockHardness(this.worldObj, x, y, z);
		}
		return 0;
	}

	private double S_addDroppedItems(Collection<ItemStack> list, int x, int y, int z, PowerManager.BreakType t) {
		Block b = Block.blocksList[this.worldObj.getBlockId(x, y, z)];
		int meta = this.worldObj.getBlockMetadata(x, y, z);
		if (b == null) return 1;
		if (b.canSilkHarvest(this.worldObj, null, x, y, z, meta) && this.silktouch
				&& (this.silktouchList.contains(data((short) b.blockID, meta)) == this.silktouchInclude)) {
			try {
				list.add((ItemStack) createStackedBlock.invoke(b, meta));
				return t == PowerManager.BreakType.Quarry ? PowerManager.B_CS : PowerManager.W_CS;
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error e) {
				e.printStackTrace();
			}
		}
		if (this.fortuneList.contains(data((short) b.blockID, meta)) == this.fortuneInclude) {
			list.addAll(b.getBlockDropped(this.worldObj, x, y, z, meta, this.fortune));
			return Math.pow(t == PowerManager.BreakType.Quarry ? PowerManager.B_CF : PowerManager.W_CF, this.fortune);
		}
		list.addAll(b.getBlockDropped(this.worldObj, x, y, z, meta, 0));
		return 1;
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

	@Override
	public final PowerReceiver getPowerReceiver(ForgeDirection side) {
		return this.pp.getPowerReceiver();
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
		readLongCollection(nbttc.getTagList("fortuneList"), this.fortuneList);
		readLongCollection(nbttc.getTagList("silktouchList"), this.silktouchList);
		this.pp.readFromNBT(nbttc);
	}

	private static void readLongCollection(NBTTagList nbttl, Collection<Long> target) {
		target.clear();
		for (int i = 0; i < nbttl.tagCount(); i++)
			target.add(((NBTTagLong) nbttl.tagAt(i)).data);
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
		this.pp.writeToNBT(nbttc);
	}

	private static NBTTagList writeLongCollection(Collection<Long> target) {
		NBTTagList nbttl = new NBTTagList();
		for (Long l : target)
			nbttl.appendTag(new NBTTagLong("", l));
		return nbttl;
	}

	@Override
	public final void doWork(PowerHandler workProvider) {}

	@Override
	public World getWorld() {
		return this.worldObj;
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
