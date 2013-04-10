/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package org.yogpstop.qp;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.LaserKind;
import buildcraft.api.core.Position;
import buildcraft.core.EntityBlock;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.network.TileNetworkData;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;

public class TileMarker extends TileBuildCraft implements IAreaProvider {

    private static int maxSize = 256;

    public static class TileWrapper {

        public @TileNetworkData
        int x, y, z;

        public TileWrapper() {
            this.x = Integer.MAX_VALUE;
            this.y = Integer.MAX_VALUE;
            this.z = Integer.MAX_VALUE;
        }

        public TileWrapper(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private TileMarker marker;

        public boolean isSet() {
            return this.x != Integer.MAX_VALUE;
        }

        public TileMarker getMarker(World world) {
            if (!isSet())
                return null;

            if (this.marker == null) {
                this.marker = (TileMarker) world.getBlockTileEntity(this.x, this.y, this.z);
            }

            return this.marker;
        }

        public void reset() {
            this.x = Integer.MAX_VALUE;
            this.y = Integer.MAX_VALUE;
            this.z = Integer.MAX_VALUE;
        }
    }

    public static class Origin {

        public boolean isSet() {
            return this.vectO.isSet();
        }

        public @TileNetworkData
        TileWrapper vectO = new TileWrapper();
        public @TileNetworkData(staticSize = 3)
        TileWrapper[] vect = { new TileWrapper(), new TileWrapper(), new TileWrapper() };
        public @TileNetworkData
        int xMin, yMin, zMin, xMax, yMax, zMax;
    }

    public @TileNetworkData
    Origin origin = new Origin();

    private EntityBlock[] lasers;
    private EntityBlock[] signals;
    public @TileNetworkData
    boolean showSignals = false;

    public void updateSignals() {
        if (CoreProxy.proxy.isSimulating(this.worldObj)) {
            this.showSignals = this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
            sendNetworkUpdate();
        }
    }

    private void switchSignals() {
        if (this.signals != null) {
            for (EntityBlock b : this.signals) {
                if (b != null) {
                    CoreProxy.proxy.removeEntity(b);
                }
            }
            this.signals = null;
        }
        if (this.showSignals) {
            this.signals = new EntityBlock[6];
            if (!this.origin.isSet() || !this.origin.vect[0].isSet()) {
                this.signals[0] = Utils.createLaser(this.worldObj, new Position(this.xCoord, this.yCoord, this.zCoord), new Position(this.xCoord + maxSize - 1,
                        this.yCoord, this.zCoord), LaserKind.Blue);
                this.signals[1] = Utils.createLaser(this.worldObj, new Position(this.xCoord - maxSize + 1, this.yCoord, this.zCoord), new Position(this.xCoord,
                        this.yCoord, this.zCoord), LaserKind.Blue);
            }

            if (!this.origin.isSet() || !this.origin.vect[1].isSet()) {
                this.signals[2] = Utils.createLaser(this.worldObj, new Position(this.xCoord, this.yCoord, this.zCoord), new Position(this.xCoord, this.yCoord
                        + maxSize - 1, this.zCoord), LaserKind.Blue);
                this.signals[3] = Utils.createLaser(this.worldObj, new Position(this.xCoord, this.yCoord - maxSize + 1, this.zCoord), new Position(this.xCoord,
                        this.yCoord, this.zCoord), LaserKind.Blue);
            }

            if (!this.origin.isSet() || !this.origin.vect[2].isSet()) {
                this.signals[4] = Utils.createLaser(this.worldObj, new Position(this.xCoord, this.yCoord, this.zCoord), new Position(this.xCoord, this.yCoord,
                        this.zCoord + maxSize - 1), LaserKind.Blue);
                this.signals[5] = Utils.createLaser(this.worldObj, new Position(this.xCoord, this.yCoord, this.zCoord - maxSize + 1), new Position(this.xCoord,
                        this.yCoord, this.zCoord), LaserKind.Blue);
            }
        }
    }

    private Position initVectO, initVect[];

    @Override
    public void initialize() {
        super.initialize();

        updateSignals();

        if (this.initVectO != null) {
            this.origin = new Origin();

            this.origin.vectO = new TileWrapper((int) this.initVectO.x, (int) this.initVectO.y, (int) this.initVectO.z);

            for (int i = 0; i < 3; ++i) {
                if (this.initVect[i] != null) {
                    linkTo((TileMarker) this.worldObj.getBlockTileEntity((int) this.initVect[i].x, (int) this.initVect[i].y, (int) this.initVect[i].z), i);
                }
            }
        }
    }

    public void tryConnection() {
        if (CoreProxy.proxy.isRenderWorld(this.worldObj))
            return;

        for (int j = 0; j < 3; ++j) {
            if (!this.origin.isSet() || !this.origin.vect[j].isSet()) {
                setVect(j);
            }
        }

        sendNetworkUpdate();
    }

    void setVect(int n) {
        int markerId = QuarryPlus.blockMarker.blockID;

        int[] coords = new int[3];

        coords[0] = this.xCoord;
        coords[1] = this.yCoord;
        coords[2] = this.zCoord;

        if (!this.origin.isSet() || !this.origin.vect[n].isSet()) {
            for (int j = 1; j < maxSize; ++j) {
                coords[n] += j;

                int blockId = this.worldObj.getBlockId(coords[0], coords[1], coords[2]);

                if (blockId == markerId) {
                    TileMarker marker = (TileMarker) this.worldObj.getBlockTileEntity(coords[0], coords[1], coords[2]);

                    if (linkTo(marker, n)) {
                        break;
                    }
                }

                coords[n] -= j;
                coords[n] -= j;

                blockId = this.worldObj.getBlockId(coords[0], coords[1], coords[2]);

                if (blockId == markerId) {
                    TileMarker marker = (TileMarker) this.worldObj.getBlockTileEntity(coords[0], coords[1], coords[2]);

                    if (linkTo(marker, n)) {
                        break;
                    }
                }

                coords[n] += j;
            }
        }
    }

    private boolean linkTo(TileMarker marker, int n) {
        if (marker == null)
            return false;

        if (this.origin.isSet() && marker.origin.isSet())
            return false;

        if (!this.origin.isSet() && !marker.origin.isSet()) {
            this.origin = new Origin();
            marker.origin = this.origin;
            this.origin.vectO = new TileWrapper(this.xCoord, this.yCoord, this.zCoord);
            this.origin.vect[n] = new TileWrapper(marker.xCoord, marker.yCoord, marker.zCoord);
        } else if (!this.origin.isSet()) {
            this.origin = marker.origin;
            this.origin.vect[n] = new TileWrapper(this.xCoord, this.yCoord, this.zCoord);
        } else {
            marker.origin = this.origin;
            this.origin.vect[n] = new TileWrapper(marker.xCoord, marker.yCoord, marker.zCoord);
        }

        this.origin.vectO.getMarker(this.worldObj).createLasers();
        updateSignals();
        marker.updateSignals();

        return true;
    }

    private void createLasers() {
        if (this.lasers != null) {
            for (EntityBlock entity : this.lasers) {
                if (entity != null) {
                    CoreProxy.proxy.removeEntity(entity);
                }
            }
        }

        this.lasers = new EntityBlock[12];
        Origin o = this.origin;

        if (!this.origin.vect[0].isSet()) {
            o.xMin = this.origin.vectO.x;
            o.xMax = this.origin.vectO.x;
        } else if (this.origin.vect[0].x < this.xCoord) {
            o.xMin = this.origin.vect[0].x;
            o.xMax = this.xCoord;
        } else {
            o.xMin = this.xCoord;
            o.xMax = this.origin.vect[0].x;
        }

        if (!this.origin.vect[1].isSet()) {
            o.yMin = this.origin.vectO.y;
            o.yMax = this.origin.vectO.y;
        } else if (this.origin.vect[1].y < this.yCoord) {
            o.yMin = this.origin.vect[1].y;
            o.yMax = this.yCoord;
        } else {
            o.yMin = this.yCoord;
            o.yMax = this.origin.vect[1].y;
        }

        if (!this.origin.vect[2].isSet()) {
            o.zMin = this.origin.vectO.z;
            o.zMax = this.origin.vectO.z;
        } else if (this.origin.vect[2].z < this.zCoord) {
            o.zMin = this.origin.vect[2].z;
            o.zMax = this.zCoord;
        } else {
            o.zMin = this.zCoord;
            o.zMax = this.origin.vect[2].z;
        }

        this.lasers = Utils.createLaserBox(this.worldObj, o.xMin, o.yMin, o.zMin, o.xMax, o.yMax, o.zMax, LaserKind.Red);
    }

    @Override
    public int xMin() {
        if (this.origin.isSet())
            return this.origin.xMin;
        return this.xCoord;
    }

    @Override
    public int yMin() {
        if (this.origin.isSet())
            return this.origin.yMin;
        return this.yCoord;
    }

    @Override
    public int zMin() {
        if (this.origin.isSet())
            return this.origin.zMin;
        return this.zCoord;
    }

    @Override
    public int xMax() {
        if (this.origin.isSet())
            return this.origin.xMax;
        return this.xCoord;
    }

    @Override
    public int yMax() {
        if (this.origin.isSet())
            return this.origin.yMax;
        return this.yCoord;
    }

    @Override
    public int zMax() {
        if (this.origin.isSet())
            return this.origin.zMax;
        return this.zCoord;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        destroy();
    }

    @Override
    public void destroy() {
        TileMarker markerOrigin = null;

        if (this.origin.isSet()) {
            markerOrigin = this.origin.vectO.getMarker(this.worldObj);
            if (markerOrigin != null) {
                Origin o = this.origin;

                if (markerOrigin.lasers != null) {
                    for (EntityBlock entity : markerOrigin.lasers) {
                        if (entity != null) {
                            entity.setDead();
                        }
                    }
                    markerOrigin.lasers = null;
                }

                for (TileWrapper m : o.vect) {
                    TileMarker mark = m.getMarker(this.worldObj);

                    if (mark != null) {
                        if (mark.lasers != null) {
                            for (EntityBlock entity : mark.lasers) {
                                if (entity != null) {
                                    entity.setDead();
                                }
                            }
                            mark.lasers = null;
                        }

                        if (mark != this) {
                            mark.origin = new Origin();
                        }
                    }
                }

                if (markerOrigin != this) {
                    markerOrigin.origin = new Origin();
                }

                for (TileWrapper wrapper : o.vect) {
                    TileMarker mark = wrapper.getMarker(this.worldObj);

                    if (mark != null) {
                        mark.updateSignals();
                    }
                }

                markerOrigin.updateSignals();
            }
        }

        if (this.signals != null) {
            for (EntityBlock block : this.signals) {
                if (block != null) {
                    block.setDead();
                }
            }
        }

        this.signals = null;

        if (CoreProxy.proxy.isSimulating(this.worldObj) && markerOrigin != null && markerOrigin != this) {
            markerOrigin.sendNetworkUpdate();
        }
    }

    @Override
    public void removeFromWorld() {
        if (!this.origin.isSet())
            return;

        Origin o = this.origin;

        for (TileWrapper m : o.vect.clone()) {
            if (m.isSet()) {
                this.worldObj.setBlock(m.x, m.y, m.z, 0);

                QuarryPlus.blockMarker.dropBlockAsItem(this.worldObj, m.x, m.y, m.z, QuarryPlus.blockMarker.blockID, 0);
            }
        }

        this.worldObj.setBlock(o.vectO.x, o.vectO.y, o.vectO.z, 0);

        QuarryPlus.blockMarker.dropBlockAsItem(this.worldObj, o.vectO.x, o.vectO.y, o.vectO.z, QuarryPlus.blockMarker.blockID, 0);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);

        if (nbttagcompound.hasKey("vectO")) {
            this.initVectO = new Position(nbttagcompound.getCompoundTag("vectO"));
            this.initVect = new Position[3];

            for (int i = 0; i < 3; ++i) {
                if (nbttagcompound.hasKey("vect" + i)) {
                    this.initVect[i] = new Position(nbttagcompound.getCompoundTag("vect" + i));
                }
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);

        if (this.origin.isSet() && this.origin.vectO.getMarker(this.worldObj) == this) {
            NBTTagCompound vectO = new NBTTagCompound();

            new Position(this.origin.vectO.getMarker(this.worldObj)).writeToNBT(vectO);
            nbttagcompound.setTag("vectO", vectO);

            for (int i = 0; i < 3; ++i) {
                if (this.origin.vect[i].isSet()) {
                    NBTTagCompound vect = new NBTTagCompound();
                    new Position(this.origin.vect[i].x, this.origin.vect[i].y, this.origin.vect[i].z).writeToNBT(vect);
                    nbttagcompound.setTag("vect" + i, vect);
                }
            }

        }
    }

    @Override
    public void postPacketHandling(PacketUpdate packet) {
        super.postPacketHandling(packet);

        switchSignals();

        if (this.origin.vectO.isSet()) {
            this.origin.vectO.getMarker(this.worldObj).updateSignals();

            for (TileWrapper w : this.origin.vect) {
                TileMarker m = w.getMarker(this.worldObj);

                if (m != null) {
                    m.updateSignals();
                }
            }
        }

        createLasers();
    }

}
