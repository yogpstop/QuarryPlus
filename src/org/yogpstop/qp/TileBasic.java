package org.yogpstop.qp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.ForgeDirection;

import buildcraft.api.gates.ITrigger;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.api.transport.IPipedItem;

public abstract class TileBasic extends TileEntity implements IPowerReceptor, IPipeEntry {
	public static final ITrigger active = new TriggerPlusMachine(754, true);
	public static final ITrigger deactive = new TriggerPlusMachine(755, false);

	protected IPowerProvider pp;

	public final ArrayList<Long> fortuneList = new ArrayList<Long>();
	public final ArrayList<Long> silktouchList = new ArrayList<Long>();
	public boolean fortuneInclude, silktouchInclude;

	protected byte fortune;
	protected boolean silktouch;
	protected byte efficiency;

	protected ArrayList<ItemStack> cacheItems = new ArrayList<ItemStack>();

	public static final byte fortuneAdd = 1;
	public static final byte silktouchAdd = 2;
	public static final byte fortuneRemove = 3;
	public static final byte silktouchRemove = 4;
	public static final byte fortuneTInc = 7;
	public static final byte silktouchTInc = 8;
	public static final byte reinit = 9;
	public static final byte openFortuneGui = 14;
	public static final byte openSilktouchGui = 15;
	public static final byte openMainGui = 16;
	public static final byte packetFortuneList = 17;
	public static final byte packetSilktouchList = 18;

	public TileBasic() {
		super();
		initPowerProvider();
	}

	public void sendPacketToServer(byte id) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(this.xCoord);
			dos.writeInt(this.yCoord);
			dos.writeInt(this.zCoord);
			dos.writeByte(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "QuarryPlusTB";
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		packet.isChunkDataPacket = true;

		PacketDispatcher.sendPacketToServer(packet);
	}

	public void sendPacketToServer(byte id, long value) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(this.xCoord);
			dos.writeInt(this.yCoord);
			dos.writeInt(this.zCoord);
			dos.writeByte(id);
			dos.writeLong(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "QuarryPlusTB";
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		packet.isChunkDataPacket = true;

		PacketDispatcher.sendPacketToServer(packet);
	}

	protected void sendPacketToPlayer(EntityPlayer ep, byte id, boolean value) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(this.xCoord);
			dos.writeInt(this.yCoord);
			dos.writeInt(this.zCoord);
			dos.writeByte(id);
			dos.writeBoolean(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "QuarryPlusTB";
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		packet.isChunkDataPacket = true;

		PacketDispatcher.sendPacketToPlayer(packet, (Player) ep);
	}

	protected void sendPacketToPlayer(EntityPlayer ep, byte id, ArrayList<Long> value) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(this.xCoord);
			dos.writeInt(this.yCoord);
			dos.writeInt(this.zCoord);
			dos.writeByte(id);
			dos.writeInt(value.size());
			for (Long l : value)
				dos.writeLong(l);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "QuarryPlusTB";
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		packet.isChunkDataPacket = true;

		PacketDispatcher.sendPacketToPlayer(packet, (Player) ep);
	}

	void recievePacket(ByteArrayDataInput data, EntityPlayer ep) {
		if (this.worldObj.isRemote) recievePacketOnClient(data.readByte(), data);
		else recievePacketOnServer(data.readByte(), data, ep);
	}

	protected void recievePacketOnServer(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		switch (pattern) {
		case fortuneAdd:
			this.fortuneList.add(data.readLong());
			sendPacketToPlayer(ep, packetFortuneList, this.fortuneList);
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarryFortuneList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		case fortuneRemove:
			this.fortuneList.remove(data.readLong());
			sendPacketToPlayer(ep, packetFortuneList, this.fortuneList);
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarryFortuneList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		case silktouchAdd:
			this.silktouchList.add(data.readLong());
			sendPacketToPlayer(ep, packetSilktouchList, this.silktouchList);
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarrySilktouchList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		case silktouchRemove:
			this.silktouchList.remove(data.readLong());
			sendPacketToPlayer(ep, packetSilktouchList, this.silktouchList);
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarrySilktouchList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		case fortuneTInc:
			this.fortuneInclude = !this.fortuneInclude;
			sendPacketToPlayer(ep, fortuneTInc, this.fortuneInclude);
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarryFortuneList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		case silktouchTInc:
			this.silktouchInclude = !this.silktouchInclude;
			sendPacketToPlayer(ep, silktouchTInc, this.silktouchInclude);
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarrySilktouchList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		case reinit:
			reinit();
			break;
		case openFortuneGui:
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarryFortuneList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		case openSilktouchGui:
			ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarrySilktouchList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			break;
		}
	}

	protected abstract void reinit();

	protected void recievePacketOnClient(byte pattern, ByteArrayDataInput data) {
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

	void init(NBTTagList nbttl) {
		if (nbttl != null) for (int i = 0; i < nbttl.tagCount(); i++) {
			short id = ((NBTTagCompound) nbttl.tagAt(i)).getShort("id");
			short lvl = ((NBTTagCompound) nbttl.tagAt(i)).getShort("lvl");
			if (id == 33) this.silktouch = true;
			if (id == 35) this.fortune = (byte) lvl;
			if (id == 32) this.efficiency = (byte) lvl;
		}
		reinit();
	}

	@Override
	public void entityEntering(ItemStack payload, ForgeDirection orientation) {}

	@Override
	public void entityEntering(IPipedItem item, ForgeDirection orientation) {}

	@Override
	public boolean acceptItems() {
		return false;
	}

	@Override
	public void setPowerProvider(IPowerProvider provider) {
		this.pp = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		return this.pp;
	}

	@Override
	public int powerRequest(ForgeDirection from) {
		return (int) Math.ceil(Math.min(getPowerProvider().getMaxEnergyReceived(), getPowerProvider().getMaxEnergyStored()
				- getPowerProvider().getEnergyStored()));
	}

	protected void initPowerProvider() {
		this.pp = PowerFramework.currentFramework.createPowerProvider();
		this.pp.configure(0, 0, 100, 0, 30000);
	}

	private static Method aTRI;
	private static int aTRIargc;
	static {
		Method[] mtd = buildcraft.core.utils.Utils.class.getMethods();
		for (Method m : mtd) {
			if (m.getName().equals("addToRandomInventory")) {
				aTRI = m;
				aTRIargc = m.getParameterTypes().length;
				break;
			}
		}
	}

	protected ItemStack addToRandomInventory(ItemStack is) {
		try {
			if (aTRIargc == 5) return (ItemStack) aTRI.invoke(null, is, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			else if (aTRIargc == 6) return (ItemStack) aTRI.invoke(null, is, this.worldObj, this.xCoord, this.yCoord, this.zCoord, ForgeDirection.UNKNOWN);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(String.format("yogpstop: error item %d:%d", is.itemID, is.getItemDamage()));
			if (Item.itemsList[is.itemID] == null) return is;
		}
		ItemStack isc = is.copy();
		isc.stackSize = 0;
		return isc;
	}

	protected static ItemStack createStackedBlock(Block b, int meta) throws SecurityException, NoClassDefFoundError, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Class<? extends Block> cls = b.getClass();
		Method createStackedBlockMethod;
		try {
			createStackedBlockMethod = getMethodRepeating(cls);
		} catch (NoClassDefFoundError e) {
			throw new NoClassDefFoundError(String.format("yogpstop: %d:%d %s %s", b.blockID, meta, b.getUnlocalizedName(), e.getMessage()));
		}
		createStackedBlockMethod.setAccessible(true);
		return (ItemStack) createStackedBlockMethod.invoke(b, meta);
	}

	private static final String createStackedBlock = "func_71880_c_";

	private static Method getMethodRepeating(Class<?> cls) throws SecurityException, NoClassDefFoundError {
		Method cache = null;
		try {
			cache = cls.getDeclaredMethod(createStackedBlock, int.class);
		} catch (NoSuchMethodException e) {
			cache = getMethodRepeating(cls.getSuperclass());
		}
		return cache;
	}

	public ArrayList<String> getEnchantments() {
		ArrayList<String> als = new ArrayList<String>();
		if (this.silktouch) als.add(Enchantment.enchantmentsList[33].getTranslatedName(1));
		if (this.fortune > 0) als.add(Enchantment.enchantmentsList[35].getTranslatedName(this.fortune));
		if (this.efficiency > 0) als.add(Enchantment.enchantmentsList[32].getTranslatedName(this.efficiency));
		return als;
	}

	void setEnchantment(ItemStack is) {
		if (this.silktouch) is.addEnchantment(Enchantment.enchantmentsList[33], 1);
		if (this.fortune > 0) is.addEnchantment(Enchantment.enchantmentsList[35], this.fortune);
		if (this.efficiency > 0) is.addEnchantment(Enchantment.enchantmentsList[32], this.efficiency);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttc) {
		super.readFromNBT(nbttc);
		this.silktouch = nbttc.getBoolean("silktouch");
		this.fortune = nbttc.getByte("fortune");
		this.efficiency = nbttc.getByte("efficiency");
		this.fortuneInclude = nbttc.getBoolean("fortuneInclude");
		this.silktouchInclude = nbttc.getBoolean("silktouchInclude");
		readArrayList(nbttc.getTagList("fortuneList"), this.fortuneList);
		readArrayList(nbttc.getTagList("silktouchList"), this.silktouchList);
		PowerFramework.currentFramework.loadPowerProvider(this, nbttc);
	}

	private static void readArrayList(NBTTagList nbttl, ArrayList<Long> target) {
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
		nbttc.setBoolean("fortuneInclude", this.fortuneInclude);
		nbttc.setBoolean("silktouchInclude", this.silktouchInclude);
		nbttc.setTag("fortuneList", writeArrayList(this.fortuneList));
		nbttc.setTag("silktouchList", writeArrayList(this.silktouchList));
		PowerFramework.currentFramework.savePowerProvider(this, nbttc);
	}

	private static NBTTagList writeArrayList(ArrayList<Long> target) {
		NBTTagList nbttl = new NBTTagList();
		for (Long l : target)
			nbttl.appendTag(new NBTTagLong("", l));
		return nbttl;
	}

	@Override
	public Packet getDescriptionPacket() {
		return PacketHandler.getPacketFromNBT(this);
	}

	@Override
	public void doWork() {}
}
