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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.LaserKind;
import buildcraft.core.EntityBlock;
import buildcraft.core.proxy.CoreProxy;

public class TileMarker extends APacketTile implements IAreaProvider {
	static final ArrayList<Link> linkList = new ArrayList<Link>();
	static final ArrayList<Laser> laserList = new ArrayList<Laser>();

	private static final int MAX_SIZE = 256;
	public Link link;
	public Laser laser;

	static void recieveLinkPacket(byte[] pdata) {
		ByteArrayDataInput data = ByteStreams.newDataInput(pdata);
		final byte flag = data.readByte();
		final int dimId = data.readInt();
		final World w = QuarryPlus.proxy.getClientWorld();
		if (w.provider.dimensionId != dimId) return;
		if (flag == PacketHandler.remove_link) {
			final int index = TileMarker.linkList.indexOf(new TileMarker.Link(w, data.readInt(), data.readInt(), data.readInt(), data.readInt(),
					data.readInt(), data.readInt()));
			if (index >= 0) TileMarker.linkList.get(index).removeConnection(false);
		} else if (flag == PacketHandler.remove_laser) {
			final int index = TileMarker.laserList.indexOf(new TileMarker.BlockIndex(w, data.readInt(), data.readInt(), data.readInt()));
			if (index >= 0) TileMarker.laserList.get(index).destructor();
		}
	}

	static class BlockIndex {
		final World w;
		final int x, y, z;

		BlockIndex(final World pw, final int px, final int py, final int pz) {
			this.w = pw;
			this.x = px;
			this.y = py;
			this.z = pz;
		}
	}

	static class Laser {
		final World w;
		final int x, y, z;
		private final EntityBlock[] lasers = new EntityBlock[3];

		Laser(final World pw, final int px, final int py, final int pz, final Link l) {
			final double a = 0.5, b = 0.45, c = 0.1;
			this.x = px;
			this.y = py;
			this.z = pz;
			this.w = pw;
			if (l == null || l.xn == l.xx) {
				this.lasers[0] = CoreProxy.proxy.newEntityBlock(pw, px - MAX_SIZE + a, py + b, pz + b, MAX_SIZE * 2, c, c, LaserKind.Blue);
			}
			if (l == null || l.yn == l.yx) {
				this.lasers[1] = CoreProxy.proxy.newEntityBlock(pw, px + b, a, pz + b, c, 255, c, LaserKind.Blue);
			}
			if (l == null || l.zn == l.zx) {
				this.lasers[2] = CoreProxy.proxy.newEntityBlock(pw, px + b, py + b, pz - MAX_SIZE + a, c, c, MAX_SIZE * 2, LaserKind.Blue);
			}
			for (EntityBlock eb : this.lasers)
				if (eb != null) eb.worldObj.spawnEntityInWorld(eb);
			int i = TileMarker.laserList.indexOf(this);
			if (i >= 0) TileMarker.laserList.get(i).destructor();
			TileMarker.laserList.add(this);
		}

		void destructor() {
			TileMarker.laserList.remove(this);
			if (!this.w.isRemote) {
				try {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(bos);
					dos.writeByte(PacketHandler.remove_laser);
					dos.writeBoolean(!this.w.isRemote);
					dos.writeInt(this.w.provider.dimensionId);
					dos.writeInt(this.x);
					dos.writeInt(this.y);
					dos.writeInt(this.z);
					Packet250CustomPayload packet = new Packet250CustomPayload();
					packet.channel = PacketHandler.Marker;
					packet.data = bos.toByteArray();
					packet.length = bos.size();
					PacketDispatcher.sendPacketToAllInDimension(packet, this.w.provider.dimensionId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			for (EntityBlock eb : this.lasers)
				if (eb != null) QuarryPlus.proxy.removeEntity(eb);
		}

		@Override
		public boolean equals(final Object o) {
			if (o instanceof BlockIndex) {
				final BlockIndex bi = (BlockIndex) o;
				return bi.x == this.x && bi.y == this.y && bi.z == this.z && bi.w == this.w;
			}
			if (o instanceof TileEntity) {
				final TileEntity te = (TileEntity) o;
				return te.xCoord == this.x && te.yCoord == this.y && te.zCoord == this.z && this.w == te.worldObj;
			}
			if (!(o instanceof Laser)) return false;
			final Laser l = (Laser) o;
			return l.x == this.x && l.y == this.y && l.z == this.z && l.w == this.w;
		}

		@Override
		public int hashCode() {
			return this.x << 21 ^ this.y << 11 ^ this.z;
		}
	}

	static class Link {
		int xx, xn, yx, yn, zx, zn;
		private final EntityBlock[] lasers = new EntityBlock[12];
		final World w;

		Link(final World pw, final int vx, final int vy, final int vz) {
			this.xx = vx;
			this.xn = vx;
			this.yx = vy;
			this.yn = vy;
			this.zx = vz;
			this.zn = vz;
			this.w = pw;
		}

		Link(final World pw, final int vxx, final int vxn, final int vyx, final int vyn, final int vzx, final int vzn) {
			this.xx = vxx;
			this.xn = vxn;
			this.yx = vyx;
			this.yn = vyn;
			this.zx = vzx;
			this.zn = vzn;
			this.w = pw;
		}

		private final void connect(final TileEntity te) {
			if (te instanceof TileMarker) {
				if (((TileMarker) te).link != null && ((TileMarker) te).link != this) ((TileMarker) te).link.removeConnection(false);
				((TileMarker) te).link = this;
			}
		}

		private final ArrayList<ItemStack> removeLink(final int x, final int y, final int z, final boolean bb) {
			final ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
			final TileEntity te = this.w.getBlockTileEntity(x, y, z);
			final Block b = Block.blocksList[this.w.getBlockId(x, y, z)];
			if (b instanceof BlockMarker) {
				if (te instanceof TileMarker) ((TileMarker) te).link = null;
				ret.addAll(b.getBlockDropped(this.w, x, y, z, this.w.getBlockMetadata(x, y, z), 0));
				if (bb) this.w.setBlockToAir(x, y, z);
			}
			return ret;
		}

		void init() {
			int i = TileMarker.linkList.indexOf(this);
			if (i >= 0) TileMarker.linkList.get(i).removeConnection(false);
			TileMarker.linkList.add(this);
			connect(this.w.getBlockTileEntity(this.xn, this.yn, this.zn));
			connect(this.w.getBlockTileEntity(this.xn, this.yn, this.zx));
			connect(this.w.getBlockTileEntity(this.xn, this.yx, this.zn));
			connect(this.w.getBlockTileEntity(this.xn, this.yx, this.zx));
			connect(this.w.getBlockTileEntity(this.xx, this.yn, this.zn));
			connect(this.w.getBlockTileEntity(this.xx, this.yn, this.zx));
			connect(this.w.getBlockTileEntity(this.xx, this.yx, this.zn));
			connect(this.w.getBlockTileEntity(this.xx, this.yx, this.zx));
		}

		ArrayList<ItemStack> removeConnection(final boolean bb) {
			TileMarker.linkList.remove(this);
			if (!this.w.isRemote) {
				try {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(bos);
					dos.writeByte(PacketHandler.remove_link);
					dos.writeInt(this.w.provider.dimensionId);
					dos.writeInt(this.xx);
					dos.writeInt(this.xn);
					dos.writeInt(this.yx);
					dos.writeInt(this.yn);
					dos.writeInt(this.zx);
					dos.writeInt(this.zn);
					Packet250CustomPayload packet = new Packet250CustomPayload();
					packet.channel = PacketHandler.Marker;
					packet.data = bos.toByteArray();
					packet.length = bos.size();
					PacketDispatcher.sendPacketToAllInDimension(packet, this.w.provider.dimensionId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			deleteLaser();
			ArrayList<ItemStack> i = new ArrayList<ItemStack>();
			i.addAll(removeLink(this.xn, this.yn, this.zn, bb));
			i.addAll(removeLink(this.xn, this.yn, this.zx, bb));
			i.addAll(removeLink(this.xn, this.yx, this.zn, bb));
			i.addAll(removeLink(this.xn, this.yx, this.zx, bb));
			i.addAll(removeLink(this.xx, this.yn, this.zn, bb));
			i.addAll(removeLink(this.xx, this.yn, this.zx, bb));
			i.addAll(removeLink(this.xx, this.yx, this.zn, bb));
			i.addAll(removeLink(this.xx, this.yx, this.zx, bb));
			return i;
		}

		void makeLaser() {
			deleteLaser();
			byte flag = 0;
			final double a = 0.5, b = 0.45, c = 0.1;
			if (this.xn != this.xx) flag |= 1;
			if (this.yn != this.yx) flag |= 2;
			if (this.zn != this.zx) flag |= 4;
			if ((flag & 1) == 1) {
				this.lasers[0] = CoreProxy.proxy.newEntityBlock(this.w, this.xn + a, this.yn + b, this.zn + b, this.xx - this.xn, c, c, LaserKind.Red);
			}
			if ((flag & 2) == 2) {
				this.lasers[4] = CoreProxy.proxy.newEntityBlock(this.w, this.xn + b, this.yn + a, this.zn + b, c, this.yx - this.yn, c, LaserKind.Red);
			}
			if ((flag & 4) == 4) {
				this.lasers[8] = CoreProxy.proxy.newEntityBlock(this.w, this.xn + b, this.yn + b, this.zn + a, c, c, this.zx - this.zn, LaserKind.Red);
			}
			if ((flag & 3) == 3) {
				this.lasers[2] = CoreProxy.proxy.newEntityBlock(this.w, this.xn + a, this.yx + b, this.zn + b, this.xx - this.xn, c, c, LaserKind.Red);
				this.lasers[6] = CoreProxy.proxy.newEntityBlock(this.w, this.xx + b, this.yn + a, this.zn + b, c, this.yx - this.yn, c, LaserKind.Red);
			}
			if ((flag & 5) == 5) {
				this.lasers[1] = CoreProxy.proxy.newEntityBlock(this.w, this.xn + a, this.yn + b, this.zx + b, this.xx - this.xn, c, c, LaserKind.Red);
				this.lasers[9] = CoreProxy.proxy.newEntityBlock(this.w, this.xx + b, this.yn + b, this.zn + a, c, c, this.zx - this.zn, LaserKind.Red);
			}
			if ((flag & 6) == 6) {
				this.lasers[5] = CoreProxy.proxy.newEntityBlock(this.w, this.xn + b, this.yn + a, this.zx + b, c, this.yx - this.yn, c, LaserKind.Red);
				this.lasers[10] = CoreProxy.proxy.newEntityBlock(this.w, this.xn + b, this.yx + b, this.zn + a, c, c, this.zx - this.zn, LaserKind.Red);
			}
			if ((flag & 7) == 7) {
				this.lasers[3] = CoreProxy.proxy.newEntityBlock(this.w, this.xn + a, this.yx + b, this.zx + b, this.xx - this.xn, c, c, LaserKind.Red);
				this.lasers[7] = CoreProxy.proxy.newEntityBlock(this.w, this.xx + b, this.yn + a, this.zx + b, c, this.yx - this.yn, c, LaserKind.Red);
				this.lasers[11] = CoreProxy.proxy.newEntityBlock(this.w, this.xx + b, this.yx + b, this.zn + a, c, c, this.zx - this.zn, LaserKind.Red);
			}
			for (EntityBlock eb : this.lasers)
				if (eb != null) eb.worldObj.spawnEntityInWorld(eb);
		}

		void deleteLaser() {
			for (EntityBlock eb : this.lasers) {
				if (eb != null) QuarryPlus.proxy.removeEntity(eb);
			}
		}

		@Override
		public boolean equals(final Object o) {
			if (o instanceof BlockIndex) {
				final BlockIndex bi = (BlockIndex) o;
				return (bi.x == this.xn || bi.x == this.xx) && (bi.y == this.yn || bi.y == this.yx) && (bi.z == this.zn || bi.z == this.zx) && this.w == bi.w;
			}
			if (o instanceof TileEntity) {
				final TileEntity te = (TileEntity) o;
				return (te.xCoord == this.xn || te.xCoord == this.xx) && (te.yCoord == this.yn || te.yCoord == this.yx)
						&& (te.zCoord == this.zn || te.zCoord == this.zx) && this.w == te.worldObj;
			}
			if (!(o instanceof Link)) return false;
			final Link l = (Link) o;
			return l.xn == this.xn && l.xx == this.xx && l.yn == this.yn && l.yx == this.yx && l.zn == this.zn && l.zx == this.zx && l.w == this.w;
		}

		@Override
		public int hashCode() {
			return this.xn << 26 ^ this.xx << 21 ^ this.yn << 16 ^ this.yx << 11 ^ this.zn << 6 ^ this.zx;
		}
	}

	@Override
	public int xMin() {
		return this.link == null ? this.xCoord : this.link.xn;
	}

	@Override
	public int yMin() {
		return this.link == null ? this.yCoord : this.link.yn;
	}

	@Override
	public int zMin() {
		return this.link == null ? this.zCoord : this.link.zn;
	}

	@Override
	public int xMax() {
		return this.link == null ? this.xCoord : this.link.xx;
	}

	@Override
	public int yMax() {
		return this.link == null ? this.yCoord : this.link.yx;
	}

	@Override
	public int zMax() {
		return this.link == null ? this.zCoord : this.link.zx;
	}

	@Override
	public void removeFromWorld() {
		if (this.link == null) {
			QuarryPlus.blockMarker.dropBlockAsItem(this.worldObj, this.xCoord, this.yCoord, this.zCoord, QuarryPlus.blockMarker.blockID, 0);
			this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
			return;
		}
		ArrayList<ItemStack> al = this.link.removeConnection(true);
		for (ItemStack is : al) {
			float f = 0.7F;
			double d0 = this.worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
			double d1 = this.worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
			double d2 = this.worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
			EntityItem entityitem = new EntityItem(this.worldObj, this.xCoord + d0, this.yCoord + d1, this.zCoord + d2, is);
			entityitem.delayBeforeCanPickup = 10;
			this.worldObj.spawnEntityInWorld(entityitem);
		}
	}

	public Collection<ItemStack> removeFromWorldWithItem() {
		if (this.link != null) return this.link.removeConnection(true);
		Collection<ItemStack> ret = new LinkedList<ItemStack>();
		ret.addAll(QuarryPlus.blockMarker.getBlockDropped(this.worldObj, this.xCoord, this.yCoord, this.zCoord, 0, 0));
		this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
		return ret;
	}

	private static void S_renewConnection(final Link l, final World w, final int x, final int y, final int z) {
		int tx = 0, ty = 0, tz = 0;
		Block b;
		if (l.xx == l.xn) {
			for (tx = 1; tx <= MAX_SIZE; tx++) {
				b = Block.blocksList[w.getBlockId(x + tx, y, z)];
				if (b instanceof BlockMarker && !linkList.contains(new BlockIndex(w, x + tx, y, z))) {
					l.xx = x + tx;
					break;
				}
				b = Block.blocksList[w.getBlockId(x - tx, y, z)];
				if (b instanceof BlockMarker && !linkList.contains(new BlockIndex(w, x - tx, y, z))) {
					tx = -tx;
					l.xn = x + tx;
					break;
				}
			}
			if (l.xx == l.xn) tx = 0;
		}
		if (l.yx == l.yn) {
			for (ty = 1; ty <= MAX_SIZE; ty++) {
				b = Block.blocksList[w.getBlockId(x, y + ty, z)];
				if (b instanceof BlockMarker && !linkList.contains(new BlockIndex(w, x, y + ty, z))) {
					l.yx = y + ty;
					break;
				}
				b = Block.blocksList[w.getBlockId(x, y - ty, z)];
				if (b instanceof BlockMarker && !linkList.contains(new BlockIndex(w, x, y - ty, z))) {
					ty = -ty;
					l.yn = y + ty;
					break;
				}
			}
			if (l.yx == l.yn) ty = 0;
		}
		if (l.zx == l.zn) {
			for (tz = 1; tz <= MAX_SIZE; tz++) {
				b = Block.blocksList[w.getBlockId(x, y, z + tz)];
				if (b instanceof BlockMarker && !linkList.contains(new BlockIndex(w, x, y, z + tz))) {
					l.zx = z + tz;
					break;
				}
				b = Block.blocksList[w.getBlockId(x, y, z - tz)];
				if (b instanceof BlockMarker && !linkList.contains(new BlockIndex(w, x, y, z - tz))) {
					tz = -tz;
					l.zn = z + tz;
					break;
				}
			}
			if (l.zx == l.zn) tz = 0;
		}
		if (l.xx == l.xn && ty != 0) TileMarker.S_renewConnection(l, w, x, y + ty, z);
		if (l.xx == l.xn && tz != 0) TileMarker.S_renewConnection(l, w, x, y, z + tz);
		if (l.yx == l.yn && tx != 0) TileMarker.S_renewConnection(l, w, x + tx, y, z);
		if (l.yx == l.yn && tz != 0) TileMarker.S_renewConnection(l, w, x, y, z + tz);
		if (l.zx == l.zn && tx != 0) TileMarker.S_renewConnection(l, w, x + tx, y, z);
		if (l.zx == l.zn && ty != 0) TileMarker.S_renewConnection(l, w, x, y + ty, z);

	}

	void G_updateSignal() {
		if (this.laser != null) {
			this.laser.destructor();
			this.laser = null;
		}
		if ((this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord) || this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord,
				this.yCoord + 1, this.zCoord))
				&& (this.link == null || this.link.xn == this.link.xx || this.link.yn == this.link.yx || this.link.zn == this.link.zx)) {
			this.laser = new Laser(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.link);
		}
		if (!this.worldObj.isRemote) PacketHandler.sendPacketToAround(this, PacketHandler.StC_UPDATE_MARKER);
	}

	void S_tryConnection() {// onBlockActivated
		if (this.link != null) this.link.removeConnection(false);
		this.link = new Link(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		S_renewConnection(this.link, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		if (this.link.xx == this.link.xn && this.link.yx == this.link.yn && this.link.zx == this.link.zn) {
			this.link = null;
			return;
		}
		this.link.init();
		this.link.makeLaser();
		{
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				dos.writeInt(this.xCoord);
				dos.writeInt(this.yCoord);
				dos.writeInt(this.zCoord);
				dos.writeByte(PacketHandler.StC_LINK_RES);
				dos.writeInt(this.link.xx);
				dos.writeInt(this.link.xn);
				dos.writeInt(this.link.yx);
				dos.writeInt(this.link.yn);
				dos.writeInt(this.link.zx);
				dos.writeInt(this.link.zn);
				PacketDispatcher.sendPacketToAllAround(this.xCoord, this.yCoord, this.zCoord, 256, this.worldObj.provider.dimensionId,
						PacketHandler.composeTilePacket(bos));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		G_updateSignal();
	}

	@Override
	void S_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {// onPacketData
		switch (pattern) {
		case PacketHandler.CtS_LINK_REQ:
			if (this.link != null) {
				try {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(bos);
					dos.writeInt(this.xCoord);
					dos.writeInt(this.yCoord);
					dos.writeInt(this.zCoord);
					dos.writeByte(PacketHandler.StC_LINK_RES);
					dos.writeInt(this.link.xx);
					dos.writeInt(this.link.xn);
					dos.writeInt(this.link.yx);
					dos.writeInt(this.link.yn);
					dos.writeInt(this.link.zx);
					dos.writeInt(this.link.zn);
					PacketDispatcher.sendPacketToPlayer(PacketHandler.composeTilePacket(bos), (Player) ep);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	void G_destroy() {
		if (this.link != null) this.link.removeConnection(false);
		if (this.laser != null) this.laser.destructor();
		ForgeChunkManager.releaseTicket(this.chunkTicket);
	}

	@Override
	void C_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {// onPacketData
		switch (pattern) {
		case PacketHandler.StC_LINK_RES:
			if (this.link != null) this.link.removeConnection(false);
			this.link = new Link(this.worldObj, data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
			this.link.init();
			this.link.makeLaser();
		case PacketHandler.StC_UPDATE_MARKER:
			G_updateSignal();
			break;
		}
	}

	private Ticket chunkTicket;

	void requestTicket() {// onPostBlockPlaced
		if (this.chunkTicket != null) return;
		this.chunkTicket = ForgeChunkManager.requestTicket(QuarryPlus.instance, this.worldObj, Type.NORMAL);
		if (this.chunkTicket == null) return;
		NBTTagCompound tag = this.chunkTicket.getModData();
		tag.setInteger("quarryX", this.xCoord);
		tag.setInteger("quarryY", this.yCoord);
		tag.setInteger("quarryZ", this.zCoord);
		forceChunkLoading(this.chunkTicket);
	}

	void forceChunkLoading(Ticket ticket) {// ticketsLoaded
		if (this.chunkTicket == null) this.chunkTicket = ticket;
		Set<ChunkCoordIntPair> chunks = Sets.newHashSet();
		ChunkCoordIntPair quarryChunk = new ChunkCoordIntPair(this.xCoord >> 4, this.zCoord >> 4);
		chunks.add(quarryChunk);
		ForgeChunkManager.forceChunk(ticket, quarryChunk);
	}

	private boolean vlF;

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (this.vlF) {
			this.vlF = false;
			int i = linkList.indexOf(this);
			if (i >= 0) this.link = linkList.get(i);
			i = laserList.indexOf(this);
			if (i >= 0) this.laser = laserList.get(i);
			G_updateSignal();
			if (this.worldObj.isRemote) PacketHandler.sendPacketToServer(this, PacketHandler.CtS_LINK_REQ);
		}
	}

	@Override
	public void validate() {
		super.validate();
		this.vlF = true;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord) != QuarryPlus.blockMarker.blockID) G_destroy();
	}
}
