package org.yogpstop.qp;

import static buildcraft.BuildCraftFactory.frameBlock;
import static buildcraft.core.utils.Utils.addToRandomPipeEntry;
import static cpw.mods.fml.common.network.PacketDispatcher.sendPacketToAllPlayers;
import static org.yogpstop.qp.QuarryPlus.data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.LaserKind;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.core.Box;
import buildcraft.core.IMachine;
import buildcraft.core.proxy.CoreProxy;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

public class TileQuarry extends TileEntity implements IPowerReceptor, IPipeConnection, IMachine {
    public boolean removeLava, removeWater, removeLiquid, buildAdvFrame;

    public final ArrayList<Long> fortuneList = new ArrayList<Long>();
    public boolean fortuneInclude, silktouchInclude;
    public final ArrayList<Long> silktouchList = new ArrayList<Long>();
    private final double[] headPos = new double[3];
    private final Box box = new Box();

    private EntityMechanicalArm heads;

    private byte fortune;
    private boolean silktouch;
    private byte efficiency;

    private boolean initialized = true;

    private PROGRESS now = PROGRESS.NONE;

    private IPowerProvider pp;

    private enum PROGRESS {
        NONE((byte) 0),
        NOTNEEDBREAK((byte) 1),
        MAKEFRAME((byte) 2),
        FILL((byte) 3),
        MOVEHEAD((byte) 4),
        BREAKBLOCK((byte) 5);
        PROGRESS(final byte arg) {
            this.byteValue = arg;
        }

        public byte getByteValue() {
            return this.byteValue;
        }

        public static PROGRESS valueOf(final byte arg) {
            for (PROGRESS d : values()) {
                if (d.getByteValue() == arg) { return d; }
            }
            return null;
        }

        private final byte byteValue;
    }

    private final int[] target = new int[3];

    private ArrayList<ItemStack> cacheItems = new ArrayList<ItemStack>();
    private ArrayList<int[]> cacheFrame = new ArrayList<int[]>();
    private ArrayList<int[]> cacheNonNeeded = new ArrayList<int[]>();
    private ArrayList<int[]> cacheFills = new ArrayList<int[]>();

    public static final byte fortuneAdd = 1;
    public static final byte silktouchAdd = 2;
    public static final byte fortuneRemove = 3;
    public static final byte silktouchRemove = 4;
    public static final byte packetNow = 5;
    public static final byte packetHeadPos = 6;
    public static final byte fortuneTInc = 7;
    public static final byte silktouchTInc = 8;
    public static final byte reinit = 9;
    public static final byte tRemoveWater = 10;
    public static final byte tRemoveLava = 11;
    public static final byte tRemoveLiquid = 12;
    public static final byte tBuildAdvFrame = 13;
    public static final byte openFortuneGui = 14;
    public static final byte openSilktouchGui = 15;
    public static final byte openQuarryGui = 16;
    public static final byte packetFortuneList = 17;
    public static final byte packetSilktouchList = 18;

    private void initPowerProvider() {
        this.pp = PowerFramework.currentFramework.createPowerProvider();
        this.pp.configure(0, 0, 100, 0, 30000);
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
        packet.channel = "QuarryPlusTQ";
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
        packet.channel = "QuarryPlusTQ";
        packet.data = bos.toByteArray();
        packet.length = bos.size();
        packet.isChunkDataPacket = true;

        PacketDispatcher.sendPacketToServer(packet);
    }

    private void sendNowPacket() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeInt(this.xCoord);
            dos.writeInt(this.yCoord);
            dos.writeInt(this.zCoord);
            dos.writeByte(packetNow);
            dos.writeByte(this.now.getByteValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "QuarryPlusTQ";
        packet.data = bos.toByteArray();
        packet.length = bos.size();
        packet.isChunkDataPacket = true;

        PacketDispatcher.sendPacketToAllPlayers(packet);
    }

    private void sendHeadPosPacket() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeInt(this.xCoord);
            dos.writeInt(this.yCoord);
            dos.writeInt(this.zCoord);
            dos.writeByte(packetHeadPos);
            dos.writeDouble(this.headPos[0]);
            dos.writeDouble(this.headPos[1]);
            dos.writeDouble(this.headPos[2]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "QuarryPlusTQ";
        packet.data = bos.toByteArray();
        packet.length = bos.size();
        packet.isChunkDataPacket = true;

        PacketDispatcher.sendPacketToAllPlayers(packet);
    }

    private void sendPacketToPlayer(EntityPlayer ep, byte id, boolean value) {
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
        packet.channel = "QuarryPlusTQ";
        packet.data = bos.toByteArray();
        packet.length = bos.size();
        packet.isChunkDataPacket = true;

        PacketDispatcher.sendPacketToPlayer(packet, (Player) ep);
    }

    private void sendPacketToPlayer(EntityPlayer ep, byte id, ArrayList<Long> value) {
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
        packet.channel = "QuarryPlusTQ";
        packet.data = bos.toByteArray();
        packet.length = bos.size();
        packet.isChunkDataPacket = true;

        PacketDispatcher.sendPacketToPlayer(packet, (Player) ep);
    }

    void recievePacket(ByteArrayDataInput data, EntityPlayer ep) {
        if (this.worldObj.isRemote)
            recievePacketOnClient(data);
        else
            recievePacketOnServer(data, ep);
    }

    private void recievePacketOnServer(ByteArrayDataInput data, EntityPlayer ep) {
        switch (data.readByte()) {
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
        case tRemoveWater:
            this.removeWater = !this.removeWater;
            sendPacketToPlayer(ep, tRemoveWater, this.removeWater);
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdContainerQuarry, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
            break;
        case tRemoveLava:
            this.removeLava = !this.removeLava;
            sendPacketToPlayer(ep, tRemoveLava, this.removeLava);
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdContainerQuarry, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
            break;
        case tRemoveLiquid:
            this.removeLiquid = !this.removeLiquid;
            sendPacketToPlayer(ep, tRemoveLiquid, this.removeLiquid);
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdContainerQuarry, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
            break;
        case tBuildAdvFrame:
            this.buildAdvFrame = !this.buildAdvFrame;
            sendPacketToPlayer(ep, tBuildAdvFrame, this.buildAdvFrame);
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdContainerQuarry, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
            break;
        case openFortuneGui:
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarryFortuneList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
            break;
        case openSilktouchGui:
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarrySilktouchList, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
            break;
        case openQuarryGui:
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdContainerQuarry, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
            break;
        }
    }

    private void recievePacketOnClient(ByteArrayDataInput data) {
        switch (data.readByte()) {
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
        case packetNow:
            this.now = PROGRESS.valueOf(data.readByte());
            initEntities();
            break;
        case packetHeadPos:
            this.headPos[0] = data.readDouble();
            this.headPos[1] = data.readDouble();
            this.headPos[2] = data.readDouble();
            if (this.heads != null)
                this.heads.setHead(this.headPos[0], this.headPos[1], this.headPos[2]);
            break;
        case fortuneTInc:
            this.fortuneInclude = data.readBoolean();
            break;
        case silktouchTInc:
            this.silktouchInclude = data.readBoolean();
            break;
        case tRemoveWater:
            this.removeWater = data.readBoolean();
            break;
        case tRemoveLava:
            this.removeLava = data.readBoolean();
            break;
        case tRemoveLiquid:
            this.removeLiquid = data.readBoolean();
            break;
        case tBuildAdvFrame:
            this.buildAdvFrame = data.readBoolean();
            break;
        }
    }

    private void updateServerEntity() {
        switch (this.now) {
        case NOTNEEDBREAK:
            if (this.cacheNonNeeded.size() > 0) {
                if (breakBlock(this.cacheNonNeeded.get(0)))
                    this.cacheNonNeeded.remove(0);
                break;
            }
            this.now = PROGRESS.MAKEFRAME;
        case MAKEFRAME:
            if (this.cacheFrame.size() > 0) {
                if (makeFrame(this.cacheFrame.get(0)))
                    this.cacheFrame.remove(0);
                break;
            }
            this.now = PROGRESS.FILL;
            this.box.deleteLasers();
            sendNowPacket();
        case FILL:
            if (this.cacheFills.size() > 0 && this.buildAdvFrame) {
                if (makeFrame(this.cacheFills.get(0)))
                    this.cacheFills.remove(0);
                break;
            }
            this.now = PROGRESS.MOVEHEAD;
            this.worldObj.spawnEntityInWorld(new EntityMechanicalArm(this.worldObj, this.box.xMin + 0.75D, this.box.yMax, this.box.zMin + 0.75D, this.box
                    .sizeX() - 1.5D, this.box.sizeZ() - 1.5D, this));
            this.heads.setHead(this.headPos[0], this.headPos[1], this.headPos[2]);
            this.heads.updatePosition();
            sendNowPacket();
            while (!checkTarget()) {
                setNextTarget();
            }
            break;
        case MOVEHEAD:
            boolean done = moveHead();
            if (this.heads != null) {
                this.heads.setHead(this.headPos[0], this.headPos[1], this.headPos[2]);
                this.heads.updatePosition();
                sendHeadPosPacket();
            }
            if (!done)
                break;
            this.now = PROGRESS.BREAKBLOCK;
        case BREAKBLOCK:
            if (breakBlock(this.target)) {
                this.now = PROGRESS.MOVEHEAD;
                while (!checkTarget()) {
                    setNextTarget();
                }
            }
            break;
        default:
            break;
        }
        ArrayList<ItemStack> cache = new ArrayList<ItemStack>();
        for (ItemStack is : this.cacheItems) {
            ItemStack added = addToRandomInventory(is);
            is.stackSize -= added.stackSize;
            if (is.stackSize > 0)
                if (!addToRandomPipeEntry(this, ForgeDirection.UNKNOWN, is))
                    cache.add(is);
        }
        this.cacheItems = cache;
    }

    private static Method aTRI;
    private static int aTRIargc;

    private ItemStack addToRandomInventory(ItemStack is) {
        if (aTRI == null) {
            Method[] mtd = buildcraft.core.utils.Utils.class.getMethods();
            for (Method m : mtd) {
                if (m.getName().equals("addToRandomInventory")) {
                    aTRI=m;
                    aTRIargc=m.getParameterTypes().length;
                    break;
                }
            }
        }

        try {
            if (aTRIargc == 5)
                return (ItemStack) aTRI.invoke(null, is, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
            else if (aTRIargc == 6)
                return (ItemStack) aTRI.invoke(null, is, this.worldObj, this.xCoord, this.yCoord, this.zCoord, ForgeDirection.UNKNOWN);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ItemStack isc = is.copy();
        isc.stackSize=0;
        return isc;
    }

    private void initBlocks() {
        switch (this.now) {
        case NOTNEEDBREAK:
            initNonNeededBlocks();
        case MAKEFRAME:
            initFrames();
        case FILL:
            initFills();
        default:
        }
    }

    private boolean checkTarget() {
        if (this.target[1] < 1) {
            destroy();
            sendNowPacket();
            return true;
        }
        int bid = this.worldObj.getBlockId(this.target[0], this.target[1], this.target[2]);
        if (bid == 0 || bid == Block.bedrock.blockID)
            return false;
        if (!this.removeLava && (bid == Block.lavaMoving.blockID || bid == Block.lavaStill.blockID))
            return false;
        if (!this.removeWater && (bid == Block.waterMoving.blockID || bid == Block.waterStill.blockID))
            return false;
        if (!this.removeLiquid && this.worldObj.getBlockMaterial(this.target[0], this.target[1], this.target[2]).isLiquid())
            return false;
        return true;
    }

    private boolean addX = true;
    private boolean addZ = true;
    private boolean digged = false;

    private void setNextTarget() {
        if (this.addX)
            this.target[0]++;
        else
            this.target[0]--;
        if (this.target[0] <= this.box.xMin || this.box.xMax <= this.target[0]) {
            this.addX = !this.addX;
            this.target[0] = Math.max(this.box.xMin + 1, Math.min(this.target[0], this.box.xMax - 1));
            if (this.addZ)
                this.target[2]++;
            else
                this.target[2]--;
            if (this.target[2] <= this.box.zMin || this.box.zMax <= this.target[2]) {
                this.addZ = !this.addZ;
                this.target[2] = Math.max(this.box.zMin + 1, Math.min(this.target[2], this.box.zMax - 1));
                if (this.digged) {
                    this.addX = !this.addX;
                    this.digged = false;
                } else {
                    this.target[1]--;
                    double aa = getDistance(this.box.xMin + 1, this.target[1], this.box.zMin + 1);
                    double ad = getDistance(this.box.xMin + 1, this.target[1], this.box.zMax - 1);
                    double da = getDistance(this.box.xMax - 1, this.target[1], this.box.zMin + 1);
                    double dd = getDistance(this.box.xMax - 1, this.target[1], this.box.zMax - 1);
                    double res = Math.min(aa, Math.min(ad, Math.min(da, dd)));
                    if (res == aa) {
                        this.addX = true;
                        this.addZ = true;
                        this.target[0] = this.box.xMin + 1;
                        this.target[2] = this.box.zMin + 1;
                    } else if (res == ad) {
                        this.addX = true;
                        this.addZ = false;
                        this.target[0] = this.box.xMin + 1;
                        this.target[2] = this.box.zMax - 1;
                    } else if (res == da) {
                        this.addX = false;
                        this.addZ = true;
                        this.target[0] = this.box.xMax - 1;
                        this.target[2] = this.box.zMin + 1;
                    } else if (res == dd) {
                        this.addX = false;
                        this.addZ = false;
                        this.target[0] = this.box.xMax - 1;
                        this.target[2] = this.box.zMax - 1;
                    }
                }
            }
        }
    }

    private double getDistance(int x, int y, int z) {
        return Math.sqrt(Math.pow(x - this.headPos[0], 2) + Math.pow(y + 1 - this.headPos[1], 2) + Math.pow(z - this.headPos[2], 2));
    }

    private boolean makeFrame(int[] coord) {
        float y = Math.max(-4.8F * this.efficiency + 25F, 0F);
        if (this.pp.useEnergy(y, y, true) != y)
            return false;
        this.worldObj.setBlock(coord[0], coord[1], coord[2], frameBlock.blockID);

        return true;
    }

    private boolean breakBlock(int[] coord) {
        this.digged = true;
        float pw = (-7.93F * this.efficiency + 40F) * blockHardness(coord[0], coord[1], coord[2]);
        if (this.pp.useEnergy(pw, pw, true) != pw)
            return false;
        this.cacheItems.addAll(getDroppedItems(coord[0], coord[1], coord[2]));
        this.worldObj.playAuxSFXAtEntity(null, 2001, coord[0], coord[1], coord[2],
                this.worldObj.getBlockId(coord[0], coord[1], coord[2]) + (this.worldObj.getBlockMetadata(coord[0], coord[1], coord[2]) << 12));
        this.worldObj.setBlockToAir(coord[0], coord[1], coord[2]);
        checkDropItem(coord);
        return true;
    }

    private float blockHardness(int x, int y, int z) {
        Block b = Block.blocksList[this.worldObj.getBlockId(x, y, z)];
        if (b != null) {
            if (this.worldObj.getBlockMaterial(x, y, z).isLiquid())
                return 0;
            return b.getBlockHardness(this.worldObj, x, y, z);
        }
        return 0;
    }

    private ArrayList<ItemStack> getDroppedItems(int x, int y, int z) {
        Block b = Block.blocksList[this.worldObj.getBlockId(x, y, z)];
        int meta = this.worldObj.getBlockMetadata(x, y, z);
        if (b == null)
            return new ArrayList<ItemStack>();
        if (b.canSilkHarvest(this.worldObj, null, x, y, z, meta) && this.silktouch
                && (this.silktouchList.contains(data((short) b.blockID, meta)) == this.silktouchInclude)) {
            ArrayList<ItemStack> al = new ArrayList<ItemStack>();
            al.add(createStackedBlock(b, meta));
            return al;
        }
        return b.getBlockDropped(this.worldObj, x, y, z, meta,
                ((this.fortuneList.contains(data((short) b.blockID, meta)) == this.fortuneInclude) ? this.fortune : 0));
    }

    private static ItemStack createStackedBlock(Block b, int meta) {
        Class cls = b.getClass();
        Method createStackedBlockMethod = getMethodRepeating(cls);
        createStackedBlockMethod.setAccessible(true);
        try {
            return (ItemStack) createStackedBlockMethod.invoke(b, meta);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final String createStackedBlock = "func_71880_c_";

    private static Method getMethodRepeating(Class cls) {
        Method cache = null;
        try {
            cache = cls.getDeclaredMethod(createStackedBlock, int.class);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            cache = getMethodRepeating(cls.getSuperclass());
        }
        return cache;
    }

    private void checkDropItem(int[] coord) {
        AxisAlignedBB axis = AxisAlignedBB.getBoundingBox(coord[0] - 4, coord[1] - 4, coord[2] - 4, coord[0] + 6, coord[1] + 6, coord[2] + 6);
        List result = this.worldObj.getEntitiesWithinAABB(EntityItem.class, axis);
        for (int ii = 0; ii < result.size(); ii++) {
            if (result.get(ii) instanceof EntityItem) {
                EntityItem entity = (EntityItem) result.get(ii);
                if (entity.isDead)
                    continue;
                ItemStack drop = entity.getEntityItem();
                if (drop.stackSize <= 0)
                    continue;
                CoreProxy.proxy.removeEntity(entity);
                this.cacheItems.add(drop);
            }
        }
    }

    public void setEnchantment(ItemStack is) {
        if (this.silktouch)
            is.addEnchantment(Enchantment.enchantmentsList[33], 1);
        if (this.fortune > 0)
            is.addEnchantment(Enchantment.enchantmentsList[35], this.fortune);
        if (this.efficiency > 0)
            is.addEnchantment(Enchantment.enchantmentsList[32], this.efficiency);
    }

    private void createBox() {
        if (!checkIAreaProvider(this.xCoord - 1, this.yCoord, this.zCoord))
            if (!checkIAreaProvider(this.xCoord + 1, this.yCoord, this.zCoord))
                if (!checkIAreaProvider(this.xCoord, this.yCoord, this.zCoord - 1))
                    if (!checkIAreaProvider(this.xCoord, this.yCoord, this.zCoord + 1))
                        if (!checkIAreaProvider(this.xCoord, this.yCoord - 1, this.zCoord))
                            if (!checkIAreaProvider(this.xCoord, this.yCoord + 1, this.zCoord)) {
                                int xMin = 0, zMin = 0;
                                ForgeDirection o = ForgeDirection.values()[this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord)].getOpposite();
                                switch (o) {
                                case EAST:
                                    xMin = this.xCoord + 1;
                                    zMin = this.zCoord - 5;
                                    break;
                                case WEST:
                                    xMin = this.xCoord - 11;
                                    zMin = this.zCoord - 5;
                                    break;
                                case SOUTH:
                                    xMin = this.xCoord - 5;
                                    zMin = this.zCoord + 1;
                                    break;
                                case NORTH:
                                default:
                                    xMin = this.xCoord - 5;
                                    zMin = this.zCoord - 11;
                                    break;
                                }
                                this.box.initialize(xMin, this.yCoord, zMin, xMin + 10, this.yCoord + 4, zMin + 10);
                            }
    }

    private boolean checkIAreaProvider(int x, int y, int z) {
        if (this.worldObj.getBlockTileEntity(x, y, z) instanceof IAreaProvider) {
            this.box.initialize(((IAreaProvider) this.worldObj.getBlockTileEntity(x, y, z)));
            this.box.reorder();
            if (this.box.contains(this.xCoord, this.yCoord, this.zCoord)) {
                this.box.reset();
                return false;
            }
            if (this.box.sizeX() < 3 || this.box.sizeZ() < 3) {
                this.box.reset();
                return false;
            }
            if (this.box.sizeY() <= 1)
                this.box.yMax += 3 - this.box.sizeY();
            ((IAreaProvider) this.worldObj.getBlockTileEntity(x, y, z)).removeFromWorld();
            return true;
        }
        return false;
    }

    private void setFirstPos() {
        this.target[0] = this.box.xMin + 1;
        this.target[2] = this.box.zMin + 1;
        this.target[1] = this.box.yMin;
        this.headPos[0] = this.box.centerX();
        this.headPos[2] = this.box.centerZ();
        this.headPos[1] = this.box.yMax - 1;
    }

    private void initFrames() {
        this.cacheFrame = new ArrayList<int[]>();
        int xn = this.box.xMin;
        int xx = this.box.xMax;
        int yn = this.box.yMin;
        int yx = this.box.yMax;
        int zn = this.box.zMin;
        int zx = this.box.zMax;
        for (int x = xn + 1; x <= xx - 1; x++) {
            checkAndAddFrame(new int[] { x, yn, zn });
            checkAndAddFrame(new int[] { x, yn, zx });
            checkAndAddFrame(new int[] { x, yx, zn });
            checkAndAddFrame(new int[] { x, yx, zx });
        }
        for (int y = yn + 1; y <= yx - 1; y++) {
            checkAndAddFrame(new int[] { xn, y, zn });
            checkAndAddFrame(new int[] { xn, y, zx });
            checkAndAddFrame(new int[] { xx, y, zn });
            checkAndAddFrame(new int[] { xx, y, zx });
        }
        for (int z = zn + 1; z <= zx - 1; z++) {
            checkAndAddFrame(new int[] { xn, yn, z });
            checkAndAddFrame(new int[] { xn, yx, z });
            checkAndAddFrame(new int[] { xx, yn, z });
            checkAndAddFrame(new int[] { xx, yx, z });
        }
        checkAndAddFrame(new int[] { xn, yn, zn });
        checkAndAddFrame(new int[] { xn, yn, zx });
        checkAndAddFrame(new int[] { xn, yx, zn });
        checkAndAddFrame(new int[] { xn, yx, zx });
        checkAndAddFrame(new int[] { xx, yn, zn });
        checkAndAddFrame(new int[] { xx, yn, zx });
        checkAndAddFrame(new int[] { xx, yx, zn });
        checkAndAddFrame(new int[] { xx, yx, zx });
    }

    private void checkAndAddFrame(int[] coord) {
        if (!(this.worldObj.getBlockId(coord[0], coord[1], coord[2]) == frameBlock.blockID && this.worldObj.getBlockMetadata(coord[0], coord[1], coord[2]) == 0))
            this.cacheFrame.add(coord);
    }

    private void initFills() {
        int xn = this.box.xMin;
        int xx = this.box.xMax;
        int yn = this.box.yMin;
        int zn = this.box.zMin;
        int zx = this.box.zMax;
        for (int y = yn; y > 0; y--) {
            for (int x = xn; x <= xx; x++) {
                checkAndAddFill(x, y, zn);
                checkAndAddFill(x, y, zx);
            }
            for (int z = zn + 1; z < zx; z++) {
                checkAndAddFill(xn, y, z);
                checkAndAddFill(xx, y, z);
            }
        }
    }

    private void checkAndAddFill(int x, int y, int z) {
        if (!this.worldObj.getBlockMaterial(x, y, z).isSolid()) {
            this.cacheFills.add(new int[] { x, y, z });
        }
    }

    private void destroyFrames() {
        int xn = this.box.xMin;
        int xx = this.box.xMax;
        int yn = this.box.yMin;
        int yx = this.box.yMax;
        int zn = this.box.zMin;
        int zx = this.box.zMax;
        for (int x = xn; x <= xx; x++) {
            setBreakableFrame(x, yn, zn);
            setBreakableFrame(x, yn, zx);
            setBreakableFrame(x, yx, zn);
            setBreakableFrame(x, yx, zx);
        }
        for (int y = yn; y <= yx; y++) {
            setBreakableFrame(xn, y, zn);
            setBreakableFrame(xn, y, zx);
            setBreakableFrame(xx, y, zn);
            setBreakableFrame(xx, y, zx);
        }
        for (int z = zn; z <= zx; z++) {
            setBreakableFrame(xn, yn, z);
            setBreakableFrame(xn, yx, z);
            setBreakableFrame(xx, yn, z);
            setBreakableFrame(xx, yx, z);
        }
    }

    private void initNonNeededBlocks() {
        this.cacheNonNeeded = new ArrayList<int[]>();
        for (int y = this.box.yMax; y >= this.box.yMin; y--) {
            for (int x = this.box.xMin; x <= this.box.xMax; x++) {
                for (int z = this.box.zMin; z <= this.box.zMax; z++) {
                    int bid = this.worldObj.getBlockId(x, y, z);
                    if (bid != 0 && bid != Block.bedrock.blockID)
                        if (bid == frameBlock.blockID && this.worldObj.getBlockMetadata(x, y, z) == 0) {
                            byte flag = 0;
                            if (x == this.box.xMin || x == this.box.xMax)
                                flag++;
                            if (y == this.box.yMin || y == this.box.yMax)
                                flag++;
                            if (z == this.box.zMin || z == this.box.zMax)
                                flag++;
                            if (flag < 2)
                                this.cacheNonNeeded.add(new int[] { x, y, z });
                        } else
                            this.cacheNonNeeded.add(new int[] { x, y, z });
                }
            }
        }
    }

    private void setBreakableFrame(int x, int y, int z) {
        if (this.worldObj.getBlockId(x, y, z) == frameBlock.blockID) {
            this.worldObj.setBlockMetadataWithNotify(x, y, z, 1, 3);
        }
    }

    private boolean moveHead() {
        float distance = (float) getRestDistance();
        float x = 31.8F;
        float pw = Math.min(2F + this.pp.getEnergyStored() / 500F, ((distance / 2F - 0.1F) * 200F / (this.efficiency * x + 1F)) + 0.01F);
        float used = this.pp.useEnergy(pw, pw, true);
        float blocks = used * (this.efficiency * x + 1F) / 200F + 0.1F;

        if (blocks * 2 > distance) {
            this.headPos[0] = this.target[0];
            this.headPos[1] = this.target[1] + 1;
            this.headPos[2] = this.target[2];
            return true;
        }
        if (used > 0) {
            this.headPos[0] += Math.cos(Math.atan2(this.target[2] - this.headPos[2], this.target[0] - this.headPos[0])) * blocks;
            this.headPos[1] += Math.sin(Math.atan2(this.target[1] + 1 - this.headPos[1], this.target[0] - this.headPos[0])) * blocks;
            this.headPos[2] += Math.sin(Math.atan2(this.target[2] - this.headPos[2], this.target[0] - this.headPos[0])) * blocks;
        }
        return false;
    }

    private double getRestDistance() {
        return Math.sqrt(Math.pow(this.target[0] - this.headPos[0], 2) + Math.pow(this.target[1] + 1 - this.headPos[1], 2)
                + Math.pow(this.target[2] - this.headPos[2], 2));
    }

    private Ticket chunkTicket;

    private void requestTicket() {
        if (this.chunkTicket != null)
            return;
        this.chunkTicket = ForgeChunkManager.requestTicket(QuarryPlus.instance, this.worldObj, Type.NORMAL);
        if (this.chunkTicket == null)
            return;
        this.chunkTicket.getModData().setInteger("quarryX", this.xCoord);
        this.chunkTicket.getModData().setInteger("quarryY", this.yCoord);
        this.chunkTicket.getModData().setInteger("quarryZ", this.zCoord);
        forceChunkLoading(this.chunkTicket);
    }

    private void initFromNBT() {
        initEntities();
        if (this.worldObj != null)
            if (!this.worldObj.isRemote)
                initBlocks();
        this.initialized = true;
    }

    private void initEntities() {
        this.box.deleteLasers();
        switch (this.now) {
        case NOTNEEDBREAK:
        case MAKEFRAME:
            this.box.createLasers(this.worldObj, LaserKind.Stripes);
            break;
        case MOVEHEAD:
        case BREAKBLOCK:
            if (this.heads == null)
                this.worldObj.spawnEntityInWorld(new EntityMechanicalArm(this.worldObj, this.box.xMin + 0.75D, this.box.yMax, this.box.zMin + 0.75D, this.box
                        .sizeX() - 1.5D, this.box.sizeZ() - 1.5D, this));
            break;
        default:
        }

        if (this.heads != null) {
            if (this.now != PROGRESS.BREAKBLOCK && this.now != PROGRESS.MOVEHEAD) {
                this.heads.setDead();
                this.heads = null;
            } else {
                this.heads.setHead(this.headPos[0], this.headPos[1], this.headPos[2]);
                this.heads.updatePosition();
            }
        }
    }

    private void destroy() {
        this.box.deleteLasers();
        this.now = PROGRESS.NONE;
        if (this.heads != null) {
            this.heads.setDead();
            this.heads = null;
        }
        if (!this.worldObj.isRemote) {
            destroyFrames();
        }
    }

    void init(NBTTagList nbttl) {
        if (nbttl != null)
            for (int i = 0; i < nbttl.tagCount(); i++) {
                short id = ((NBTTagCompound) nbttl.tagAt(i)).getShort("id");
                short lvl = ((NBTTagCompound) nbttl.tagAt(i)).getShort("lvl");
                if (id == 33)
                    this.silktouch = true;
                if (id == 35)
                    this.fortune = (byte) lvl;
                if (id == 32)
                    this.efficiency = (byte) lvl;
            }
        createBox();
        requestTicket();
        initPowerProvider();
        reinit();
    }

    void reinit() {
        this.now = PROGRESS.NOTNEEDBREAK;
        if (!this.worldObj.isRemote) {
            setFirstPos();
            initBlocks();
        }
        initEntities();
        sendPacketToAllPlayers(PacketHandler.getPacketFromNBT(this));
    }

    void forceChunkLoading(Ticket ticket) {
        if (this.chunkTicket == null) {
            this.chunkTicket = ticket;
        }

        Set<ChunkCoordIntPair> chunks = Sets.newHashSet();
        ChunkCoordIntPair quarryChunk = new ChunkCoordIntPair(this.xCoord >> 4, this.zCoord >> 4);
        chunks.add(quarryChunk);
        ForgeChunkManager.forceChunk(ticket, quarryChunk);

        for (int chunkX = this.box.xMin >> 4; chunkX <= this.box.xMax >> 4; chunkX++) {
            for (int chunkZ = this.box.zMin >> 4; chunkZ <= this.box.zMax >> 4; chunkZ++) {
                ChunkCoordIntPair chunk = new ChunkCoordIntPair(chunkX, chunkZ);
                ForgeChunkManager.forceChunk(ticket, chunk);
                chunks.add(chunk);
            }
        }
        sendPacketToAllPlayers(PacketHandler.getPacketFromNBT(this));
    }

    void setArm(EntityMechanicalArm ema) {
        this.heads = ema;
    }

    public ArrayList<String> getEnchantments() {
        ArrayList<String> als = new ArrayList<String>();
        if (this.silktouch)
            als.add(Enchantment.enchantmentsList[33].getTranslatedName(1));
        if (this.fortune > 0)
            als.add(Enchantment.enchantmentsList[35].getTranslatedName(this.fortune));
        if (this.efficiency > 0)
            als.add(Enchantment.enchantmentsList[32].getTranslatedName(this.efficiency));
        return als;
    }

    @Override
    public void updateEntity() {
        if (!this.initialized)
            initFromNBT();
        if (!this.worldObj.isRemote)
            updateServerEntity();
    }

    @Override
    public Packet getDescriptionPacket() {
        return PacketHandler.getPacketFromNBT(this);
    }

    @Override
    public void invalidate() {
        destroy();
        if (!this.worldObj.isRemote) {
            ForgeChunkManager.releaseTicket(this.chunkTicket);
        }
        super.invalidate();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttc) {
        super.readFromNBT(nbttc);
        this.box.initialize(nbttc);
        this.addZ = nbttc.getBoolean("addZ");
        this.addX = nbttc.getBoolean("addX");
        this.digged = nbttc.getBoolean("digged");
        this.target[0] = nbttc.getInteger("targetX");
        this.target[1] = nbttc.getInteger("targetY");
        this.target[2] = nbttc.getInteger("targetZ");
        this.now = PROGRESS.valueOf(nbttc.getByte("now"));
        this.silktouch = nbttc.getBoolean("silktouch");
        this.fortune = nbttc.getByte("fortune");
        this.efficiency = nbttc.getByte("efficiency");
        this.headPos[0] = nbttc.getDouble("headPosX");
        this.headPos[1] = nbttc.getDouble("headPosY");
        this.headPos[2] = nbttc.getDouble("headPosZ");
        this.removeWater = nbttc.getBoolean("removeWater");
        this.removeLava = nbttc.getBoolean("removeLava");
        this.removeLiquid = nbttc.getBoolean("removeLiquid");
        this.buildAdvFrame = nbttc.getBoolean("buildAdvFrame");
        this.fortuneInclude = nbttc.getBoolean("fortuneInclude");
        this.silktouchInclude = nbttc.getBoolean("silktouchInclude");
        readArrayList(nbttc.getTagList("fortuneList"), this.fortuneList);
        readArrayList(nbttc.getTagList("silktouchList"), this.silktouchList);
        PowerFramework.currentFramework.loadPowerProvider(this, nbttc);
        this.initialized = false;
    }

    private static void readArrayList(NBTTagList nbttl, ArrayList<Long> target) {
        target.clear();
        for (int i = 0; i < nbttl.tagCount(); i++)
            target.add(((NBTTagLong) nbttl.tagAt(i)).data);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttc) {
        super.writeToNBT(nbttc);
        this.box.writeToNBT(nbttc);
        nbttc.setInteger("targetX", this.target[0]);
        nbttc.setInteger("targetY", this.target[1]);
        nbttc.setInteger("targetZ", this.target[2]);
        nbttc.setBoolean("addZ", this.addZ);
        nbttc.setBoolean("addX", this.addX);
        nbttc.setBoolean("digged", this.digged);
        nbttc.setByte("now", this.now.getByteValue());
        nbttc.setBoolean("silktouch", this.silktouch);
        nbttc.setByte("fortune", this.fortune);
        nbttc.setByte("efficiency", this.efficiency);
        nbttc.setDouble("headPosX", this.headPos[0]);
        nbttc.setDouble("headPosY", this.headPos[1]);
        nbttc.setDouble("headPosZ", this.headPos[2]);
        nbttc.setBoolean("removeWater", this.removeWater);
        nbttc.setBoolean("removeLava", this.removeLava);
        nbttc.setBoolean("removeLiquid", this.removeLiquid);
        nbttc.setBoolean("buildAdvFrame", this.buildAdvFrame);
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
    public void setPowerProvider(IPowerProvider provider) {
        this.pp = provider;

    }

    @Override
    public IPowerProvider getPowerProvider() {
        return this.pp;
    }

    @Override
    public void doWork() {}

    @Override
    public boolean isPipeConnected(ForgeDirection with) {
        return true;
    }

    @Override
    public boolean isActive() {
        return this.now != PROGRESS.NONE;
    }

    @Override
    public boolean manageLiquids() {
        return false;
    }

    @Override
    public boolean manageSolids() {
        return true;
    }

    @Override
    public boolean allowActions() {
        return false;
    }

    @Override
    public int powerRequest(ForgeDirection from) {
        return (int) Math.ceil(Math.min(getPowerProvider().getMaxEnergyReceived(), getPowerProvider().getMaxEnergyStored()
                - getPowerProvider().getEnergyStored()));
    }

}
