package org.yogpstop.qp;

import static org.yogpstop.qp.QuarryPlus.data;
import static org.yogpstop.qp.PacketHandler.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.gates.IAction;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.core.IMachine;

public abstract class TileBasic extends APacketTile implements IPowerReceptor, IMachine {
	protected ForgeDirection pump = ForgeDirection.UNKNOWN;

	protected PowerHandler pp = new PowerHandler(this, PowerHandler.Type.MACHINE);

	public final List<Long> fortuneList = new ArrayList<Long>();
	public final List<Long> silktouchList = new ArrayList<Long>();
	public boolean fortuneInclude, silktouchInclude;

	protected byte unbreaking;
	protected byte fortune;
	protected boolean silktouch;
	protected byte efficiency;

	protected List<ItemStack> cacheItems = new LinkedList<ItemStack>();

	@Override
	protected void S_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		switch (pattern) {
		case fortuneAdd:
			this.fortuneList.add(data.readLong());
			sendPacketToPlayer(this, ep, packetFortuneList, this.fortuneList);
			PacketDispatcher.sendPacketToPlayer(PacketHandler.G_makeOpenGUIPacket(QuarryPlus.guiIdFList, this.xCoord, this.yCoord, this.zCoord), (Player) ep);
			break;
		case fortuneRemove:
			this.fortuneList.remove(data.readLong());
			sendPacketToPlayer(this, ep, packetFortuneList, this.fortuneList);
			PacketDispatcher.sendPacketToPlayer(PacketHandler.G_makeOpenGUIPacket(QuarryPlus.guiIdFList, this.xCoord, this.yCoord, this.zCoord), (Player) ep);
			break;
		case silktouchAdd:
			this.silktouchList.add(data.readLong());
			sendPacketToPlayer(this, ep, packetSilktouchList, this.silktouchList);
			PacketDispatcher.sendPacketToPlayer(PacketHandler.G_makeOpenGUIPacket(QuarryPlus.guiIdSList, this.xCoord, this.yCoord, this.zCoord), (Player) ep);
			break;
		case silktouchRemove:
			this.silktouchList.remove(data.readLong());
			sendPacketToPlayer(this, ep, packetSilktouchList, this.silktouchList);
			PacketDispatcher.sendPacketToPlayer(PacketHandler.G_makeOpenGUIPacket(QuarryPlus.guiIdSList, this.xCoord, this.yCoord, this.zCoord), (Player) ep);
			break;
		case fortuneTInc:
			this.fortuneInclude = !this.fortuneInclude;
			sendPacketToPlayer(this, ep, fortuneTInc, this.fortuneInclude);
			PacketDispatcher.sendPacketToPlayer(PacketHandler.G_makeOpenGUIPacket(QuarryPlus.guiIdFList, this.xCoord, this.yCoord, this.zCoord), (Player) ep);
			break;
		case silktouchTInc:
			this.silktouchInclude = !this.silktouchInclude;
			sendPacketToPlayer(this, ep, silktouchTInc, this.silktouchInclude);
			PacketDispatcher.sendPacketToPlayer(PacketHandler.G_makeOpenGUIPacket(QuarryPlus.guiIdSList, this.xCoord, this.yCoord, this.zCoord), (Player) ep);
			break;
		}
	}

	protected abstract void G_reinit();

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
	protected void C_recievePacket(byte pattern, ByteArrayDataInput data) {
		switch (pattern) {
		case packetFortuneList:
			this.fortuneList.clear();
			int fsize = data.readInt();
			for (int i = 0; i < fsize; i++) {
				this.fortuneList.add(data.readLong());
			}
			break;
		case packetSilktouchList:
			this.silktouchList.clear();
			int ssize = data.readInt();
			for (int i = 0; i < ssize; i++) {
				this.silktouchList.add(data.readLong());
			}
			break;
		case fortuneTInc:
			this.fortuneInclude = data.readBoolean();
			break;
		case silktouchTInc:
			this.silktouchInclude = data.readBoolean();
			break;
		}
	}

	void G_init(NBTTagList nbttl) {
		if (nbttl != null) for (int i = 0; i < nbttl.tagCount(); i++) {
			short id = ((NBTTagCompound) nbttl.tagAt(i)).getShort("id");
			short lvl = ((NBTTagCompound) nbttl.tagAt(i)).getShort("lvl");
			if (id == 32) this.efficiency = (byte) lvl;
			if (id == 33) this.silktouch = true;
			if (id == 34) this.unbreaking = (byte) lvl;
			if (id == 35) this.fortune = (byte) lvl;
		}
		G_reinit();
	}

	protected boolean S_breakBlock(int x, int y, int z, double BP, double CS, double CF) {
		Collection<ItemStack> dropped = new LinkedList<ItemStack>();
		if (this.worldObj.getBlockMaterial(x, y, z).isLiquid()) {
			TileEntity te = this.worldObj.getBlockTileEntity(this.xCoord + this.pump.offsetX, this.yCoord + this.pump.offsetY, this.zCoord + this.pump.offsetZ);
			if (!(te instanceof TilePump)) {
				this.pump = ForgeDirection.UNKNOWN;
				return true;
			}
			return ((TilePump) te).S_removeLiquids(this.pp, x, y, z);
		}
		float pw = (float) Math.max(BP * S_blockHardness(x, y, z) * S_addDroppedItems(dropped, x, y, z, CS, CF) / (this.unbreaking + 1), 0D);
		float used = this.pp.useEnergy(pw, pw, false);
		if (used != pw) return false;
		used = this.pp.useEnergy(pw, pw, true);
		this.cacheItems.addAll(dropped);
		this.worldObj.playAuxSFXAtEntity(null, 2001, x, y, z, this.worldObj.getBlockId(x, y, z) | (this.worldObj.getBlockMetadata(x, y, z) << 12));
		this.worldObj.setBlockToAir(x, y, z);

		return true;
	}

	boolean S_connect(ForgeDirection fd) {
		TileEntity te = this.worldObj.getBlockTileEntity(this.xCoord + this.pump.offsetX, this.yCoord + this.pump.offsetY, this.zCoord + this.pump.offsetZ);
		if (te instanceof TilePump && this.pump != fd) return false;
		this.pump = fd;
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

	private double S_addDroppedItems(Collection<ItemStack> list, int x, int y, int z, double CS, double CF) {
		Block b = Block.blocksList[this.worldObj.getBlockId(x, y, z)];
		int meta = this.worldObj.getBlockMetadata(x, y, z);
		if (b == null) return 1;
		if (b.canSilkHarvest(this.worldObj, null, x, y, z, meta) && this.silktouch
				&& (this.silktouchList.contains(data((short) b.blockID, meta)) == this.silktouchInclude)) {
			try {
				list.add(S_createStackedBlock(b, meta));
				return CS;
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error e) {
				e.printStackTrace();
			}
		}
		if (this.fortuneList.contains(data((short) b.blockID, meta)) == this.fortuneInclude) {
			list.addAll(b.getBlockDropped(this.worldObj, x, y, z, meta, this.fortune));
			return Math.pow(CF, this.fortune);
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

	protected static ItemStack S_createStackedBlock(Block b, int meta) throws SecurityException, NoClassDefFoundError, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Class<? extends Block> cls = b.getClass();
		Method createStackedBlockMethod;
		try {
			createStackedBlockMethod = S_getMethodRepeating(cls);
		} catch (NoClassDefFoundError e) {
			throw new NoClassDefFoundError(String.format("yogpstop:%d:%d-%s-%s-%s", b.blockID, meta, cls.getName(), b.getUnlocalizedName(), e.getMessage()));
		}
		createStackedBlockMethod.setAccessible(true);
		return (ItemStack) createStackedBlockMethod.invoke(b, meta);
	}

	private static final String createStackedBlock = "func_71880_c_";

	private static Method S_getMethodRepeating(Class<?> cls) throws SecurityException, NoClassDefFoundError {
		Method cache = null;
		try {
			cache = cls.getDeclaredMethod(createStackedBlock, int.class);
		} catch (NoSuchMethodException e) {
			cache = S_getMethodRepeating(cls.getSuperclass());
		}
		return cache;
	}

	public Collection<String> C_getEnchantments() {
		ArrayList<String> als = new ArrayList<String>();
		if (this.efficiency > 0) als.add(Enchantment.enchantmentsList[32].getTranslatedName(this.efficiency));
		if (this.silktouch) als.add(Enchantment.enchantmentsList[33].getTranslatedName(1));
		if (this.unbreaking > 0) als.add(Enchantment.enchantmentsList[34].getTranslatedName(this.unbreaking));
		if (this.fortune > 0) als.add(Enchantment.enchantmentsList[35].getTranslatedName(this.fortune));
		return als;
	}

	void S_setEnchantment(ItemStack is) {
		if (this.efficiency > 0) is.addEnchantment(Enchantment.enchantmentsList[32], this.efficiency);
		if (this.silktouch) is.addEnchantment(Enchantment.enchantmentsList[33], 1);
		if (this.unbreaking > 0) is.addEnchantment(Enchantment.enchantmentsList[34], this.unbreaking);
		if (this.fortune > 0) is.addEnchantment(Enchantment.enchantmentsList[35], this.fortune);
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
		readArrayList(nbttc.getTagList("fortuneList"), this.fortuneList);
		readArrayList(nbttc.getTagList("silktouchList"), this.silktouchList);
		this.pp.readFromNBT(nbttc);
	}

	private static void readArrayList(NBTTagList nbttl, Collection<Long> target) {
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
		nbttc.setTag("fortuneList", writeArrayList(this.fortuneList));
		nbttc.setTag("silktouchList", writeArrayList(this.silktouchList));
		this.pp.writeToNBT(nbttc);
	}

	private static NBTTagList writeArrayList(Collection<Long> target) {
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
}
