package org.yogpstop.qp;

import static buildcraft.BuildCraftFactory.frameBlock;
import static buildcraft.core.utils.Utils.addToRandomInventory;
import static buildcraft.core.utils.Utils.addToRandomPipeEntry;
import static cpw.mods.fml.common.network.PacketDispatcher.sendPacketToAllPlayers;
import static org.yogpstop.qp.QuarryPlus.data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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
            byteValue = arg;
        }

        public byte getByteValue() {
            return byteValue;
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
        pp = PowerFramework.currentFramework.createPowerProvider();
        pp.configure(0, 0, 100, 0, 30000);
    }

    public void sendPacketToServer(byte id) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeInt(xCoord);
            dos.writeInt(yCoord);
            dos.writeInt(zCoord);
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
            dos.writeInt(xCoord);
            dos.writeInt(yCoord);
            dos.writeInt(zCoord);
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
            dos.writeInt(xCoord);
            dos.writeInt(yCoord);
            dos.writeInt(zCoord);
            dos.writeByte(packetNow);
            dos.writeByte(now.getByteValue());
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
            dos.writeInt(xCoord);
            dos.writeInt(yCoord);
            dos.writeInt(zCoord);
            dos.writeByte(packetHeadPos);
            dos.writeDouble(headPos[0]);
            dos.writeDouble(headPos[1]);
            dos.writeDouble(headPos[2]);
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
            dos.writeInt(xCoord);
            dos.writeInt(yCoord);
            dos.writeInt(zCoord);
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
            dos.writeInt(xCoord);
            dos.writeInt(yCoord);
            dos.writeInt(zCoord);
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
        if (worldObj.isRemote)
            recievePacketOnClient(data);
        else
            recievePacketOnServer(data, ep);
    }

    private void recievePacketOnServer(ByteArrayDataInput data, EntityPlayer ep) {
        switch (data.readByte()) {
        case fortuneAdd:
            fortuneList.add(data.readLong());
            sendPacketToPlayer(ep, packetFortuneList, fortuneList);
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarryFortuneList, worldObj, xCoord, yCoord, zCoord);
            break;
        case fortuneRemove:
            fortuneList.remove(data.readLong());
            sendPacketToPlayer(ep, packetFortuneList, fortuneList);
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarryFortuneList, worldObj, xCoord, yCoord, zCoord);
            break;
        case silktouchAdd:
            silktouchList.add(data.readLong());
            sendPacketToPlayer(ep, packetSilktouchList, silktouchList);
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarrySilktouchList, worldObj, xCoord, yCoord, zCoord);
            break;
        case silktouchRemove:
            silktouchList.remove(data.readLong());
            sendPacketToPlayer(ep, packetSilktouchList, silktouchList);
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarrySilktouchList, worldObj, xCoord, yCoord, zCoord);
            break;
        case fortuneTInc:
            fortuneInclude = !fortuneInclude;
            sendPacketToPlayer(ep, fortuneTInc, fortuneInclude);
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarryFortuneList, worldObj, xCoord, yCoord, zCoord);
            break;
        case silktouchTInc:
            silktouchInclude = !silktouchInclude;
            sendPacketToPlayer(ep, silktouchTInc, silktouchInclude);
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarrySilktouchList, worldObj, xCoord, yCoord, zCoord);
            break;
        case reinit:
            reinit();
            break;
        case tRemoveWater:
            removeWater = !removeWater;
            sendPacketToPlayer(ep, tRemoveWater, removeWater);
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdContainerQuarry, worldObj, xCoord, yCoord, zCoord);
            break;
        case tRemoveLava:
            removeLava = !removeLava;
            sendPacketToPlayer(ep, tRemoveLava, removeLava);
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdContainerQuarry, worldObj, xCoord, yCoord, zCoord);
            break;
        case tRemoveLiquid:
            removeLiquid = !removeLiquid;
            sendPacketToPlayer(ep, tRemoveLiquid, removeLiquid);
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdContainerQuarry, worldObj, xCoord, yCoord, zCoord);
            break;
        case tBuildAdvFrame:
            buildAdvFrame = !buildAdvFrame;
            sendPacketToPlayer(ep, tBuildAdvFrame, buildAdvFrame);
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdContainerQuarry, worldObj, xCoord, yCoord, zCoord);
            break;
        case openFortuneGui:
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarryFortuneList, worldObj, xCoord, yCoord, zCoord);
            break;
        case openSilktouchGui:
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdGuiQuarrySilktouchList, worldObj, xCoord, yCoord, zCoord);
            break;
        case openQuarryGui:
            ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdContainerQuarry, worldObj, xCoord, yCoord, zCoord);
            break;
        }
    }

    private void recievePacketOnClient(ByteArrayDataInput data) {
        switch (data.readByte()) {
        case packetFortuneList:
            fortuneList.clear();
            int fsize = data.readInt();
            for (int i = 0; i < fsize; i++) {
                fortuneList.add(data.readLong());
            }
            break;
        case packetSilktouchList:
            silktouchList.clear();
            int ssize = data.readInt();
            for (int i = 0; i < ssize; i++) {
                silktouchList.add(data.readLong());
            }
            break;
        case packetNow:
            now = PROGRESS.valueOf(data.readByte());
            initEntities();
            break;
        case packetHeadPos:
            headPos[0] = data.readDouble();
            headPos[1] = data.readDouble();
            headPos[2] = data.readDouble();
            heads.setHead(headPos[0], headPos[1], headPos[2]);
            break;
        case fortuneTInc:
            fortuneInclude = data.readBoolean();
            break;
        case silktouchTInc:
            silktouchInclude = data.readBoolean();
            break;
        case tRemoveWater:
            removeWater = data.readBoolean();
            break;
        case tRemoveLava:
            removeLava = data.readBoolean();
            break;
        case tRemoveLiquid:
            removeLiquid = data.readBoolean();
            break;
        case tBuildAdvFrame:
            buildAdvFrame = data.readBoolean();
            break;
        }
    }

    private void updateServerEntity() {
        switch (now) {
        case NOTNEEDBREAK:
            if (cacheNonNeeded.size() > 0) {
                if (breakBlock(cacheNonNeeded.get(0)))
                    cacheNonNeeded.remove(0);
                break;
            }
            now = PROGRESS.MAKEFRAME;
        case MAKEFRAME:
            if (cacheFrame.size() > 0) {
                if (makeFrame(cacheFrame.get(0)))
                    cacheFrame.remove(0);
                break;
            }
            now = PROGRESS.FILL;
            box.deleteLasers();
            sendNowPacket();
        case FILL:
            if (cacheFills.size() > 0 && buildAdvFrame) {
                if (makeFrame(cacheFills.get(0)))
                    cacheFills.remove(0);
                break;
            }
            now = PROGRESS.MOVEHEAD;
            worldObj.spawnEntityInWorld(new EntityMechanicalArm(worldObj, box.xMin + 0.75D, box.yMax, box.zMin + 0.75D, box.sizeX() - 1.5D, box.sizeZ() - 1.5D,
                    this));
            heads.setHead(headPos[0], headPos[1], headPos[2]);
            heads.updatePosition();
            sendNowPacket();
            while (!checkTarget()) {
                setNextTarget();
            }
            break;
        case MOVEHEAD:
            boolean done = moveHead();
            heads.setHead(headPos[0], headPos[1], headPos[2]);
            heads.updatePosition();
            sendHeadPosPacket();
            if (!done)
                break;
            now = PROGRESS.BREAKBLOCK;
        case BREAKBLOCK:
            if (breakBlock(target)) {
                now = PROGRESS.MOVEHEAD;
                while (!checkTarget()) {
                    setNextTarget();
                }
            }
            break;
        default:
            break;
        }
        ArrayList<ItemStack> cache = new ArrayList<ItemStack>();
        for (ItemStack is : cacheItems) {
            ItemStack added = addToRandomInventory(is, worldObj, xCoord, yCoord, zCoord, ForgeDirection.UNKNOWN);
            is.stackSize -= added.stackSize;
            if (is.stackSize > 0)
                if (!addToRandomPipeEntry(this, ForgeDirection.UNKNOWN, is))
                    cache.add(is);
        }
        cacheItems = cache;
    }

    private void initBlocks() {
        switch (now) {
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
        if (target[1] < 1) {
            destroy();
            sendNowPacket();
            return true;
        }
        int bid = worldObj.getBlockId(target[0], target[1], target[2]);
        if (bid == 0 || bid == Block.bedrock.blockID)
            return false;
        if (!removeLava && (bid == Block.lavaMoving.blockID || bid == Block.lavaStill.blockID))
            return false;
        if (!removeWater && (bid == Block.waterMoving.blockID || bid == Block.waterStill.blockID))
            return false;
        if (!removeLiquid && worldObj.getBlockMaterial(target[0], target[1], target[2]).isLiquid())
            return false;
        return true;
    }

    private boolean makeFrame(int[] coord) {
        float y = Math.max(-4.8F * (float) efficiency + 25F, 0F);
        if (pp.useEnergy(y, y, true) != y)
            return false;
        worldObj.setBlock(coord[0], coord[1], coord[2], frameBlock.blockID);

        return true;
    }

    private boolean addX = true;
    private boolean addZ = true;
    private Ticket chunkTicket;

    private void setNextTarget() {
        if (addX)
            target[0]++;
        else
            target[0]--;
        if (target[0] == box.xMax || target[0] == box.xMin) {
            addX = !addX;
            if (addX)
                target[0]++;
            else
                target[0]--;
            if (addZ)
                target[2]++;
            else
                target[2]--;
            if (target[2] == box.zMax || target[2] == box.zMin) {
                addZ = !addZ;
                if (addZ)
                    target[2]++;
                else
                    target[2]--;
                target[1]--;
            }
        }
    }

    private boolean breakBlock(int[] coord) {
        float pw = (-7.93F * (float) efficiency + 40F) * blockHardness(coord[0], coord[1], coord[2]);
        if (pp.useEnergy(pw, pw, true) != pw)
            return false;
        cacheItems.addAll(getDroppedItems(coord[0], coord[1], coord[2]));
        worldObj.playAuxSFXAtEntity(null, 2001, coord[0], coord[1], coord[2],
                worldObj.getBlockId(coord[0], coord[1], coord[2]) + (worldObj.getBlockMetadata(coord[0], coord[1], coord[2]) << 12));
        worldObj.setBlockToAir(coord[0], coord[1], coord[2]);
        checkDropItem(coord);
        return true;
    }

    private void dropItem() {
        ItemStack is = new ItemStack(QuarryPlus.blockQuarry);
        setEnchantment(is);
        float var6 = 0.7F;
        double var7 = (double) (worldObj.rand.nextFloat() * var6) + (double) (1.0F - var6) * 0.5D;
        double var9 = (double) (worldObj.rand.nextFloat() * var6) + (double) (1.0F - var6) * 0.5D;
        double var11 = (double) (worldObj.rand.nextFloat() * var6) + (double) (1.0F - var6) * 0.5D;
        EntityItem var13 = new EntityItem(worldObj, (double) xCoord + var7, (double) yCoord + var9, (double) zCoord + var11, is);
        var13.delayBeforeCanPickup = 10;
        worldObj.spawnEntityInWorld(var13);
    }

    private float blockHardness(int x, int y, int z) {
        Block b = Block.blocksList[worldObj.getBlockId(x, y, z)];
        if (b != null) {
            if (worldObj.getBlockMaterial(x, y, z).isLiquid())
                return 0;
            return b.getBlockHardness(worldObj, x, y, z);
        }
        return (float) 0;
    }

    private ArrayList<ItemStack> getDroppedItems(int x, int y, int z) {
        Block b = Block.blocksList[worldObj.getBlockId(x, y, z)];
        int meta = worldObj.getBlockMetadata(x, y, z);
        if (b == null)
            return new ArrayList<ItemStack>();
        if (b.canSilkHarvest(worldObj, null, x, y, z, meta) && silktouch && (silktouchList.contains(data((short) b.blockID, meta)) == silktouchInclude)) {
            ArrayList<ItemStack> al = new ArrayList<ItemStack>();
            al.add(new ItemStack(b, 1, meta));
            return al;
        }
        return b.getBlockDropped(worldObj, x, y, z, meta, ((fortuneList.contains(data((short) b.blockID, meta)) == fortuneInclude) ? fortune : 0));
    }

    @SuppressWarnings("rawtypes")
    private void checkDropItem(int[] coord) {
        AxisAlignedBB axis = AxisAlignedBB.getBoundingBox(coord[0] - 4, coord[1] - 4, coord[2] - 4, coord[0] + 6, coord[1] + 6, coord[2] + 6);
        List result = worldObj.getEntitiesWithinAABB(EntityItem.class, axis);
        for (int ii = 0; ii < result.size(); ii++) {
            if (result.get(ii) instanceof EntityItem) {
                EntityItem entity = (EntityItem) result.get(ii);
                if (entity.isDead)
                    continue;
                ItemStack drop = entity.getEntityItem();
                if (drop.stackSize <= 0)
                    continue;
                CoreProxy.proxy.removeEntity(entity);
                cacheItems.add(drop);
            }
        }
    }

    private void setEnchantment(ItemStack is) {
        if (silktouch)
            is.addEnchantment(Enchantment.enchantmentsList[33], 1);
        if (fortune > 0)
            is.addEnchantment(Enchantment.enchantmentsList[35], fortune);
        if (efficiency > 0)
            is.addEnchantment(Enchantment.enchantmentsList[32], efficiency);
    }

    private void createBox() {
        if (!checkIAreaProvider(xCoord - 1, yCoord, zCoord))
            if (!checkIAreaProvider(xCoord + 1, yCoord, zCoord))
                if (!checkIAreaProvider(xCoord, yCoord, zCoord - 1))
                    if (!checkIAreaProvider(xCoord, yCoord, zCoord + 1))
                        if (!checkIAreaProvider(xCoord, yCoord - 1, zCoord))
                            if (!checkIAreaProvider(xCoord, yCoord + 1, zCoord)) {
                                int xMin = 0, zMin = 0;
                                ForgeDirection o = ForgeDirection.values()[worldObj.getBlockMetadata(xCoord, yCoord, zCoord)].getOpposite();
                                switch (o) {
                                case EAST:
                                    xMin = xCoord + 1;
                                    zMin = zCoord - 5;
                                    break;
                                case WEST:
                                    xMin = xCoord - 11;
                                    zMin = zCoord - 5;
                                    break;
                                case SOUTH:
                                    xMin = xCoord - 5;
                                    zMin = zCoord + 1;
                                    break;
                                case NORTH:
                                default:
                                    xMin = xCoord - 5;
                                    zMin = zCoord - 11;
                                    break;
                                }
                                box.initialize(xMin, yCoord, zMin, xMin + 10, yCoord + 4, zMin + 10);
                            }
    }

    private boolean checkIAreaProvider(int x, int y, int z) {
        if (worldObj.getBlockTileEntity(x, y, z) instanceof IAreaProvider) {
            box.initialize(((IAreaProvider) worldObj.getBlockTileEntity(x, y, z)));
            box.reorder();
            if (box.contains(xCoord, yCoord, zCoord)) {
                box.reset();
                return false;
            }
            if (box.sizeX() < 3 || box.sizeZ() < 3) {
                box.reset();
                return false;
            }
            if (box.sizeY() <= 1)
                box.yMax += 3 - box.sizeY();
            ((IAreaProvider) worldObj.getBlockTileEntity(x, y, z)).removeFromWorld();
            return true;
        }
        return false;
    }

    private void setFirstPos() {
        target[0] = box.xMin + 1;
        target[2] = box.zMin + 1;
        target[1] = box.yMin;
        headPos[0] = box.centerX();
        headPos[2] = box.centerZ();
        headPos[1] = box.yMax - 1;
    }

    private void initFrames() {
        cacheFrame = new ArrayList<int[]>();
        int xn = box.xMin;
        int xx = box.xMax;
        int yn = box.yMin;
        int yx = box.yMax;
        int zn = box.zMin;
        int zx = box.zMax;
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
        if (!(worldObj.getBlockId(coord[0], coord[1], coord[2]) == frameBlock.blockID && worldObj.getBlockMetadata(coord[0], coord[1], coord[2]) == 0))
            cacheFrame.add(coord);
    }

    private void initFills() {
        int xn = box.xMin;
        int xx = box.xMax;
        int yn = box.yMin;
        int zn = box.zMin;
        int zx = box.zMax;
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
        if (!worldObj.getBlockMaterial(x, y, z).isSolid()) {
            cacheFills.add(new int[] { x, y, z });
        }
    }

    private void destroyFrames() {
        int xn = box.xMin;
        int xx = box.xMax;
        int yn = box.yMin;
        int yx = box.yMax;
        int zn = box.zMin;
        int zx = box.zMax;
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
        cacheNonNeeded = new ArrayList<int[]>();
        for (int y = box.yMax; y >= box.yMin; y--) {
            for (int x = box.xMin; x <= box.xMax; x++) {
                for (int z = box.zMin; z <= box.zMax; z++) {
                    int bid = worldObj.getBlockId(x, y, z);
                    if (bid != 0 && bid != Block.bedrock.blockID)
                        if (bid == frameBlock.blockID && worldObj.getBlockMetadata(x, y, z) == 0) {
                            byte flag = 0;
                            if (x == box.xMin || x == box.xMax)
                                flag++;
                            if (y == box.yMin || y == box.yMax)
                                flag++;
                            if (z == box.zMin || z == box.zMax)
                                flag++;
                            if (flag < 2)
                                cacheNonNeeded.add(new int[] { x, y, z });
                        } else
                            cacheNonNeeded.add(new int[] { x, y, z });
                }
            }
        }
    }

    private void setBreakableFrame(int x, int y, int z) {
        if (worldObj.getBlockId(x, y, z) == frameBlock.blockID) {
            worldObj.setBlockMetadataWithNotify(x, y, z, 1, 3);
        }
    }

    private boolean moveHead() {
        float distance = (float) getRestDistance();
        float x = 31.8F;
        float pw = Math.min(2F + pp.getEnergyStored() / 500F, ((distance / 2F - 0.1F) * 200F / (efficiency * x + 1F)) + 0.01F);
        float used = pp.useEnergy(pw, pw, true);
        float blocks = used * ((float) efficiency * x + 1F) / 200F + 0.1F;

        if (blocks * 2 > distance) {
            headPos[0] = target[0];
            headPos[1] = target[1] + 1;
            headPos[2] = target[2];
            return true;
        }
        if (used > 0) {
            headPos[0] += Math.cos(Math.atan2(target[2] - headPos[2], target[0] - headPos[0])) * blocks;
            headPos[1] += Math.sin(Math.atan2(target[1] + 1 - headPos[1], target[0] - headPos[0])) * blocks;
            headPos[2] += Math.sin(Math.atan2(target[2] - headPos[2], target[0] - headPos[0])) * blocks;
        }
        return false;
    }

    private double getRestDistance() {
        return Math.sqrt(Math.pow(target[0] - headPos[0], 2) + Math.pow(target[1] + 1 - headPos[1], 2) + Math.pow(target[2] - headPos[2], 2));
    }

    private void requestTicket() {
        if (chunkTicket != null)
            return;
        chunkTicket = ForgeChunkManager.requestTicket(QuarryPlus.instance, worldObj, Type.NORMAL);
        if (chunkTicket == null)
            return;
        chunkTicket.getModData().setInteger("quarryX", xCoord);
        chunkTicket.getModData().setInteger("quarryY", yCoord);
        chunkTicket.getModData().setInteger("quarryZ", zCoord);
        forceChunkLoading(chunkTicket);
    }

    private void initFromNBT() {
        initEntities();
        if (worldObj != null)
            if (!worldObj.isRemote)
                initBlocks();
        initialized = true;
    }

    private void initEntities() {
        box.deleteLasers();
        switch (now) {
        case NOTNEEDBREAK:
        case MAKEFRAME:
            box.createLasers(worldObj, LaserKind.Stripes);
            break;
        case MOVEHEAD:
        case BREAKBLOCK:
            if (heads == null)
                worldObj.spawnEntityInWorld(new EntityMechanicalArm(worldObj, box.xMin + 0.75D, box.yMax, box.zMin + 0.75D, box.sizeX() - 1.5D,
                        box.sizeZ() - 1.5D, this));
            break;
        default:
        }

        if (heads != null) {
            if (now != PROGRESS.BREAKBLOCK && now != PROGRESS.MOVEHEAD) {
                heads.setDead();
                heads = null;
            } else {
                heads.setHead(headPos[0], headPos[1], headPos[2]);
                heads.updatePosition();
            }
        }
    }

    private void destroy() {
        box.deleteLasers();
        now = PROGRESS.NONE;
        if (heads != null) {
            heads.setDead();
            heads = null;
        }
        if (!worldObj.isRemote) {
            destroyFrames();
        }
    }

    void init(NBTTagList nbttl) {
        if (nbttl != null)
            for (int i = 0; i < nbttl.tagCount(); i++) {
                short id = ((NBTTagCompound) nbttl.tagAt(i)).getShort("id");
                short lvl = ((NBTTagCompound) nbttl.tagAt(i)).getShort("lvl");
                if (id == 33)
                    silktouch = true;
                if (id == 35)
                    fortune = (byte) lvl;
                if (id == 32)
                    efficiency = (byte) lvl;
            }
        createBox();
        requestTicket();
        initPowerProvider();
        reinit();
    }

    void reinit() {
        now = PROGRESS.NOTNEEDBREAK;
        if (!worldObj.isRemote) {
            setFirstPos();
            initBlocks();
        }
        initEntities();
        sendPacketToAllPlayers(PacketHandler.getPacketFromNBT(this));
    }

    void forceChunkLoading(Ticket ticket) {
        if (chunkTicket == null) {
            chunkTicket = ticket;
        }

        Set<ChunkCoordIntPair> chunks = Sets.newHashSet();
        ChunkCoordIntPair quarryChunk = new ChunkCoordIntPair(xCoord >> 4, zCoord >> 4);
        chunks.add(quarryChunk);
        ForgeChunkManager.forceChunk(ticket, quarryChunk);

        for (int chunkX = box.xMin >> 4; chunkX <= box.xMax >> 4; chunkX++) {
            for (int chunkZ = box.zMin >> 4; chunkZ <= box.zMax >> 4; chunkZ++) {
                ChunkCoordIntPair chunk = new ChunkCoordIntPair(chunkX, chunkZ);
                ForgeChunkManager.forceChunk(ticket, chunk);
                chunks.add(chunk);
            }
        }
        sendPacketToAllPlayers(PacketHandler.getPacketFromNBT(this));
    }

    void setArm(EntityMechanicalArm ema) {
        heads = ema;
    }

    public ArrayList<String> getEnchantments() {
        ArrayList<String> als = new ArrayList<String>();
        if (silktouch)
            als.add(Enchantment.enchantmentsList[33].getTranslatedName(1));
        if (fortune > 0)
            als.add(Enchantment.enchantmentsList[35].getTranslatedName(fortune));
        if (efficiency > 0)
            als.add(Enchantment.enchantmentsList[32].getTranslatedName(efficiency));
        return als;
    }

    @Override
    public void updateEntity() {
        if (!initialized)
            initFromNBT();
        if (!worldObj.isRemote)
            updateServerEntity();
    }

    @Override
    public Packet getDescriptionPacket() {
        return PacketHandler.getPacketFromNBT(this);
    }

    @Override
    public void invalidate() {
        destroy();
        if (!worldObj.isRemote) {
            ForgeChunkManager.releaseTicket(chunkTicket);
            if (worldObj.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
                dropItem();
            }
        }
        super.invalidate();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttc) {
        super.readFromNBT(nbttc);
        box.initialize(nbttc);
        addZ = nbttc.getBoolean("addZ");
        addX = nbttc.getBoolean("addX");
        target[0] = nbttc.getInteger("targetX");
        target[1] = nbttc.getInteger("targetY");
        target[2] = nbttc.getInteger("targetZ");
        now = PROGRESS.valueOf(nbttc.getByte("now"));
        silktouch = nbttc.getBoolean("silktouch");
        fortune = nbttc.getByte("fortune");
        efficiency = nbttc.getByte("efficiency");
        headPos[0] = nbttc.getDouble("headPosX");
        headPos[1] = nbttc.getDouble("headPosY");
        headPos[2] = nbttc.getDouble("headPosZ");
        removeWater = nbttc.getBoolean("removeWater");
        removeLava = nbttc.getBoolean("removeLava");
        removeLiquid = nbttc.getBoolean("removeLiquid");
        buildAdvFrame = nbttc.getBoolean("buildAdvFrame");
        fortuneInclude = nbttc.getBoolean("fortuneInclude");
        silktouchInclude = nbttc.getBoolean("silktouchInclude");
        readArrayList(nbttc.getTagList("fortuneList"), fortuneList);
        readArrayList(nbttc.getTagList("silktouchList"), silktouchList);
        PowerFramework.currentFramework.loadPowerProvider(this, nbttc);
        initialized = false;
    }

    private void readArrayList(NBTTagList nbttl, ArrayList<Long> target) {
        target.clear();
        for (int i = 0; i < nbttl.tagCount(); i++)
            target.add(((NBTTagLong) nbttl.tagAt(i)).data);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttc) {
        super.writeToNBT(nbttc);
        box.writeToNBT(nbttc);
        nbttc.setInteger("targetX", target[0]);
        nbttc.setInteger("targetY", target[1]);
        nbttc.setInteger("targetZ", target[2]);
        nbttc.setBoolean("addZ", addZ);
        nbttc.setBoolean("addX", addX);
        nbttc.setByte("now", now.getByteValue());
        nbttc.setBoolean("silktouch", silktouch);
        nbttc.setByte("fortune", fortune);
        nbttc.setByte("efficiency", efficiency);
        nbttc.setDouble("headPosX", headPos[0]);
        nbttc.setDouble("headPosY", headPos[1]);
        nbttc.setDouble("headPosZ", headPos[2]);
        nbttc.setBoolean("removeWater", removeWater);
        nbttc.setBoolean("removeLava", removeLava);
        nbttc.setBoolean("removeLiquid", removeLiquid);
        nbttc.setBoolean("buildAdvFrame", buildAdvFrame);
        nbttc.setBoolean("fortuneInclude", fortuneInclude);
        nbttc.setBoolean("silktouchInclude", silktouchInclude);
        nbttc.setTag("fortuneList", writeArrayList(fortuneList));
        nbttc.setTag("silktouchList", writeArrayList(silktouchList));
        PowerFramework.currentFramework.savePowerProvider(this, nbttc);
    }

    private NBTTagList writeArrayList(ArrayList<Long> target) {
        NBTTagList nbttl = new NBTTagList();
        for (Long l : target)
            nbttl.appendTag(new NBTTagLong("", l));
        return nbttl;
    }

    @Override
    public void setPowerProvider(IPowerProvider provider) {
        pp = provider;

    }

    @Override
    public IPowerProvider getPowerProvider() {
        return pp;
    }

    @Override
    public void doWork() {}

    @Override
    public boolean isPipeConnected(ForgeDirection with) {
        return true;
    }

    @Override
    public boolean isActive() {
        return now != PROGRESS.NONE;
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
