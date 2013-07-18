package org.yogpstop.qp;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.yogpstop.Inline;

import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataInput;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.LaserKind;
import buildcraft.core.EntityBlock;
import buildcraft.core.proxy.CoreProxy;

public class TileMarker extends APacketTile implements IAreaProvider {

	private static final int MAX_SIZE = 256;
	public Link obj;
	private EntityBlock[] slasers;

	public class Link {
		int xx, xn, yx, yn, zx, zn;
		EntityBlock[] lasers;

		Link() {}

		Link(World w, int vxx, int vxn, int vyx, int vyn, int vzx, int vzn) {
			this.xx = vxx;
			this.xn = vxn;
			this.yx = vyx;
			this.yn = vyn;
			this.zx = vzx;
			this.zn = vzn;
			TileEntity te = w.getBlockTileEntity(this.xn, this.yn, this.zn);
			if (te instanceof TileMarker) if (((TileMarker) te).obj == null) ((TileMarker) te).obj = this;
			else if (((TileMarker) te).obj.equals(this)) {
				((TileMarker) te).obj.removeConnection(w);
				((TileMarker) te).obj = this;
			}
			te = w.getBlockTileEntity(this.xn, this.yn, this.zx);
			if (te instanceof TileMarker) if (((TileMarker) te).obj == null) ((TileMarker) te).obj = this;
			else if (((TileMarker) te).obj.equals(this)) {
				((TileMarker) te).obj.removeConnection(w);
				((TileMarker) te).obj = this;
			}
			te = w.getBlockTileEntity(this.xn, this.yx, this.zn);
			if (te instanceof TileMarker) if (((TileMarker) te).obj == null) ((TileMarker) te).obj = this;
			else if (((TileMarker) te).obj.equals(this)) {
				((TileMarker) te).obj.removeConnection(w);
				((TileMarker) te).obj = this;
			}
			te = w.getBlockTileEntity(this.xn, this.yx, this.zx);
			if (te instanceof TileMarker) if (((TileMarker) te).obj == null) ((TileMarker) te).obj = this;
			else if (((TileMarker) te).obj.equals(this)) {
				((TileMarker) te).obj.removeConnection(w);
				((TileMarker) te).obj = this;
			}
			te = w.getBlockTileEntity(this.xx, this.yn, this.zn);
			if (te instanceof TileMarker) if (((TileMarker) te).obj == null) ((TileMarker) te).obj = this;
			else if (((TileMarker) te).obj.equals(this)) {
				((TileMarker) te).obj.removeConnection(w);
				((TileMarker) te).obj = this;
			}
			te = w.getBlockTileEntity(this.xx, this.yn, this.zx);
			if (te instanceof TileMarker) if (((TileMarker) te).obj == null) ((TileMarker) te).obj = this;
			else if (((TileMarker) te).obj.equals(this)) {
				((TileMarker) te).obj.removeConnection(w);
				((TileMarker) te).obj = this;
			}
			te = w.getBlockTileEntity(this.xx, this.yx, this.zn);
			if (te instanceof TileMarker) if (((TileMarker) te).obj == null) ((TileMarker) te).obj = this;
			else if (((TileMarker) te).obj.equals(this)) {
				((TileMarker) te).obj.removeConnection(w);
				((TileMarker) te).obj = this;
			}
			te = w.getBlockTileEntity(this.xx, this.yx, this.zx);
			if (te instanceof TileMarker) if (((TileMarker) te).obj == null) ((TileMarker) te).obj = this;
			else if (((TileMarker) te).obj.equals(this)) {
				((TileMarker) te).obj.removeConnection(w);
				((TileMarker) te).obj = this;
			}
		}

		boolean equals(Link l) {
			if (l == this) return false;
			if (l.xn == this.xn && l.xx == this.xx && l.yn == this.yn && l.yx == this.yx && l.zx == this.zx && l.zn == this.zn) return true;
			return false;
		}

		void removeConnectionIfCannotHold(World w) {
			boolean nnn = false, nnx = false, nxn = false, nxx = false, xnn = false, xnx = false, xxn = false, xxx = false;
			nnn = Inline.isMine(this, w.getBlockTileEntity(this.xn, this.yn, this.zn));
			nnx = Inline.isMine(this, w.getBlockTileEntity(this.xn, this.yn, this.zx));
			nxn = Inline.isMine(this, w.getBlockTileEntity(this.xn, this.yx, this.zn));
			nxx = Inline.isMine(this, w.getBlockTileEntity(this.xn, this.yx, this.zx));
			xnn = Inline.isMine(this, w.getBlockTileEntity(this.xx, this.yn, this.zn));
			xnx = Inline.isMine(this, w.getBlockTileEntity(this.xx, this.yn, this.zx));
			xxn = Inline.isMine(this, w.getBlockTileEntity(this.xx, this.yx, this.zn));
			xxx = Inline.isMine(this, w.getBlockTileEntity(this.xx, this.yx, this.zx));
			boolean nnnnnx = nnn && nnx, nnnnxn = nnn && nxn, nnnxnn = nnn && xnn;
			boolean xnnxxn = xnn && xxn, xnnxnx = xnn && xnx;
			boolean nxnxxn = nxn && xxn, nxnnxx = nxn && nxx;
			boolean nnxnxx = nnx && nxx, nnxxnx = nnx && xnx;
			boolean xxnxxx = xxn && xxx, xnxxxx = xnx && xxx, nxxxxx = nxx && xxx;
			if (!((nnnnnx && nnnnxn && nnnxnn) || (nnnnnx && nnxnxx && nnxxnx) || (nnnnxn && nxnxxn && nxnnxx) || (nnnxnn && xnnxxn && xnnxnx)
					|| (nxxxxx && nnxnxx && nxnnxx) || (nnxxnx && xnxxxx && xnnxnx) || (nxnxxn && xnnxxn && xxnxxx) || (nxxxxx && xnxxxx && xxnxxx))) removeConnection(w);
		}

		void removeConnection(World w) {
			deleteLaser(w);
			Inline.removeConnection(this, w.getBlockTileEntity(this.xn, this.yn, this.zn));
			Inline.removeConnection(this, w.getBlockTileEntity(this.xn, this.yn, this.zx));
			Inline.removeConnection(this, w.getBlockTileEntity(this.xn, this.yx, this.zn));
			Inline.removeConnection(this, w.getBlockTileEntity(this.xn, this.yx, this.zx));
			Inline.removeConnection(this, w.getBlockTileEntity(this.xx, this.yn, this.zn));
			Inline.removeConnection(this, w.getBlockTileEntity(this.xx, this.yn, this.zx));
			Inline.removeConnection(this, w.getBlockTileEntity(this.xx, this.yx, this.zn));
			Inline.removeConnection(this, w.getBlockTileEntity(this.xx, this.yx, this.zx));
		}

		void makeLaser(World w) {
			deleteLaser(w);
			this.lasers = new EntityBlock[12];
			if (this.xn != this.xx) {
				this.lasers[0] = CoreProxy.proxy
						.newEntityBlock(w, this.xn + 0.5D, this.yn + 0.45D, this.zn + 0.45D, this.xx - this.xn, 0.1, 0.1, LaserKind.Red);
				this.lasers[1] = CoreProxy.proxy
						.newEntityBlock(w, this.xn + 0.5D, this.yn + 0.45D, this.zx + 0.45D, this.xx - this.xn, 0.1, 0.1, LaserKind.Red);
				this.lasers[2] = CoreProxy.proxy
						.newEntityBlock(w, this.xn + 0.5D, this.yx + 0.45D, this.zn + 0.45D, this.xx - this.xn, 0.1, 0.1, LaserKind.Red);
				this.lasers[3] = CoreProxy.proxy
						.newEntityBlock(w, this.xn + 0.5D, this.yx + 0.45D, this.zx + 0.45D, this.xx - this.xn, 0.1, 0.1, LaserKind.Red);
			}
			if (this.yn != this.yx) {
				this.lasers[4] = CoreProxy.proxy
						.newEntityBlock(w, this.xn + 0.45D, this.yn + 0.5D, this.zn + 0.45D, 0.1, this.yx - this.yn, 0.1, LaserKind.Red);
				this.lasers[5] = CoreProxy.proxy
						.newEntityBlock(w, this.xn + 0.45D, this.yn + 0.5D, this.zx + 0.45D, 0.1, this.yx - this.yn, 0.1, LaserKind.Red);
				this.lasers[6] = CoreProxy.proxy
						.newEntityBlock(w, this.xx + 0.45D, this.yn + 0.5D, this.zn + 0.45D, 0.1, this.yx - this.yn, 0.1, LaserKind.Red);
				this.lasers[7] = CoreProxy.proxy
						.newEntityBlock(w, this.xx + 0.45D, this.yn + 0.5D, this.zx + 0.45D, 0.1, this.yx - this.yn, 0.1, LaserKind.Red);
			}
			if (this.zn != this.zx) {
				this.lasers[8] = CoreProxy.proxy
						.newEntityBlock(w, this.xn + 0.45D, this.yn + 0.45D, this.zn + 0.5D, 0.1, 0.1, this.zx - this.zn, LaserKind.Red);
				this.lasers[9] = CoreProxy.proxy
						.newEntityBlock(w, this.xx + 0.45D, this.yn + 0.45D, this.zn + 0.5D, 0.1, 0.1, this.zx - this.zn, LaserKind.Red);
				this.lasers[10] = CoreProxy.proxy.newEntityBlock(w, this.xn + 0.45D, this.yx + 0.45D, this.zn + 0.5D, 0.1, 0.1, this.zx - this.zn,
						LaserKind.Red);
				this.lasers[11] = CoreProxy.proxy.newEntityBlock(w, this.xx + 0.45D, this.yx + 0.45D, this.zn + 0.5D, 0.1, 0.1, this.zx - this.zn,
						LaserKind.Red);
			}
			for (EntityBlock eb : this.lasers)
				if (eb != null) w.spawnEntityInWorld(eb);
		}

		void deleteLaser(World w) {
			if (this.lasers != null) for (EntityBlock eb : this.lasers) {
				if (eb != null) {
					w.removeEntity(eb);
					if (w.isRemote) ((WorldClient) w).removeEntityFromWorld(eb.entityId);
				}
			}
		}
	}

	@Override
	public int xMin() {
		return this.obj == null ? this.xCoord : this.obj.xn;
	}

	@Override
	public int yMin() {
		return this.obj == null ? this.yCoord : this.obj.yn;
	}

	@Override
	public int zMin() {
		return this.obj == null ? this.zCoord : this.obj.zn;
	}

	@Override
	public int xMax() {
		return this.obj == null ? this.xCoord : this.obj.xx;
	}

	@Override
	public int yMax() {
		return this.obj == null ? this.yCoord : this.obj.yx;
	}

	@Override
	public int zMax() {
		return this.obj == null ? this.zCoord : this.obj.zx;
	}

	@Override
	public void removeFromWorld() {
		if (this.obj == null) {
			QuarryPlus.blockMarker.dropBlockAsItem(this.worldObj, this.xCoord, this.yCoord, this.zCoord, QuarryPlus.blockMarker.blockID, 0);
			this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
			return;
		}
		Link l = this.obj;
		l.deleteLaser(this.worldObj);
		Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xn, l.yn, l.zn));
		Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xn, l.yn, l.zx));
		Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xn, l.yx, l.zn));
		Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xn, l.yx, l.zx));
		Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xx, l.yn, l.zn));
		Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xx, l.yn, l.zx));
		Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xx, l.yx, l.zn));
		Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xx, l.yx, l.zx));
	}

	public Collection<ItemStack> removeFromWorldWithItem() {
		Collection<ItemStack> ret = new LinkedList<ItemStack>();
		if (this.obj != null) {
			Link l = this.obj;
			l.deleteLaser(this.worldObj);
			Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xn, l.yn, l.zn), ret);
			Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xn, l.yn, l.zx), ret);
			Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xn, l.yx, l.zn), ret);
			Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xn, l.yx, l.zx), ret);
			Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xx, l.yn, l.zn), ret);
			Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xx, l.yn, l.zx), ret);
			Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xx, l.yx, l.zn), ret);
			Inline.removeFromWorld(l, this.worldObj.getBlockTileEntity(l.xx, l.yx, l.zx), ret);
		} else {
			ret.addAll(QuarryPlus.blockMarker.getBlockDropped(this.worldObj, this.xCoord, this.yCoord, this.zCoord, 0, 0));
			this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
		}
		return ret;
	}

	private void S_renewConnection() {
		int i;
		TileEntity tx = null, ty = null, tz = null;
		if (this.obj.xx == this.obj.xn) {
			for (i = 1; i < MAX_SIZE; i++) {
				tx = this.worldObj.getBlockTileEntity(this.xCoord + i, this.yCoord, this.zCoord);
				if (tx instanceof TileMarker && ((TileMarker) tx).obj == null) {
					this.obj.xx = tx.xCoord;
					((TileMarker) tx).obj = this.obj;
					break;
				}
				tx = this.worldObj.getBlockTileEntity(this.xCoord - i, this.yCoord, this.zCoord);
				if (tx instanceof TileMarker && ((TileMarker) tx).obj == null) {
					this.obj.xn = tx.xCoord;
					((TileMarker) tx).obj = this.obj;
					break;
				}
				tx = null;
			}
		}
		if (this.obj.yx == this.obj.yn) {
			for (i = 1; i < MAX_SIZE; i++) {
				ty = this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord + i, this.zCoord);
				if (ty instanceof TileMarker && ((TileMarker) ty).obj == null) {
					this.obj.yx = ty.yCoord;
					((TileMarker) ty).obj = this.obj;
					break;
				}
				ty = this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord - i, this.zCoord);
				if (ty instanceof TileMarker && ((TileMarker) ty).obj == null) {
					this.obj.yn = ty.yCoord;
					((TileMarker) ty).obj = this.obj;
					break;
				}
				ty = null;
			}
		}
		if (this.obj.zx == this.obj.zn) {
			for (i = 1; i < MAX_SIZE; i++) {
				tz = this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord + i);
				if (tz instanceof TileMarker && ((TileMarker) tz).obj == null) {
					this.obj.zx = tz.zCoord;
					((TileMarker) tz).obj = this.obj;
					break;
				}
				tz = this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord - i);
				if (tz instanceof TileMarker && ((TileMarker) tz).obj == null) {
					this.obj.zn = tz.zCoord;
					((TileMarker) tz).obj = this.obj;
					break;
				}
				tz = null;
			}
		}
		if (this.obj.xx == this.obj.xn) {
			if (ty != null) ((TileMarker) ty).S_renewConnection();
			if (tz != null) ((TileMarker) tz).S_renewConnection();
		}
		if (this.obj.yx == this.obj.yn) {
			if (tx != null) ((TileMarker) tx).S_renewConnection();
			if (tz != null) ((TileMarker) tz).S_renewConnection();
		}
		if (this.obj.zx == this.obj.zn) {
			if (tx != null) ((TileMarker) tx).S_renewConnection();
			if (ty != null) ((TileMarker) ty).S_renewConnection();
		}
	}

	void S_updateSignal() {// onNeighborBlockChange
		boolean powered = this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
		G_removeSignal();
		if (powered) {
			this.slasers = new EntityBlock[3];
			if (this.obj == null) {
				this.slasers[0] = CoreProxy.proxy.newEntityBlock(this.worldObj, this.xCoord - MAX_SIZE + 0.5D, this.yCoord + 0.45D, this.zCoord + 0.45D,
						MAX_SIZE * 2 + 0.5D, 0.1, 0.1, LaserKind.Blue);
				this.slasers[1] = CoreProxy.proxy.newEntityBlock(this.worldObj, this.xCoord + 0.45D, Math.max(0, this.yCoord - MAX_SIZE) + 0.5D,
						this.zCoord + 0.45D, 0.1, 256, 0.1, LaserKind.Blue);
				this.slasers[2] = CoreProxy.proxy.newEntityBlock(this.worldObj, this.xCoord + 0.45D, this.yCoord + 0.45D, this.zCoord - MAX_SIZE + 0.5D, 0.1,
						0.1, MAX_SIZE * 2 + 0.5D, LaserKind.Blue);
			} else {
				if (this.obj.xn == this.obj.xx) this.slasers[0] = CoreProxy.proxy.newEntityBlock(this.worldObj, this.xCoord - MAX_SIZE + 0.5D,
						this.yCoord + 0.45D, this.zCoord + 0.45D, MAX_SIZE * 2 + 0.5D, 0.1, 0.1, LaserKind.Blue);
				if (this.obj.yn == this.obj.yx) this.slasers[1] = CoreProxy.proxy.newEntityBlock(this.worldObj, this.xCoord + 0.45D,
						Math.max(0, this.yCoord - MAX_SIZE) + 0.5D, this.zCoord + 0.45D, 0.1, 256, 0.1, LaserKind.Blue);
				if (this.obj.zn == this.obj.zx) this.slasers[2] = CoreProxy.proxy.newEntityBlock(this.worldObj, this.xCoord + 0.45D, this.yCoord + 0.45D,
						this.zCoord - MAX_SIZE + 0.5D, 0.1, 0.1, MAX_SIZE * 2 + 0.5D, LaserKind.Blue);
			}
			for (EntityBlock eb : this.slasers)
				if (eb != null) this.worldObj.spawnEntityInWorld(eb);
		}
		PacketHandler.sendMarkerPacket(this, PacketHandler.signal, powered);
	}

	void S_tryConnection() {// onBlockActivated
		TileEntity tx;
		if (this.obj != null) this.obj.removeConnection(this.worldObj);
		this.obj = new Link();
		this.obj.xx = this.obj.xn = this.xCoord;
		this.obj.yx = this.obj.yn = this.yCoord;
		this.obj.zx = this.obj.zn = this.zCoord;
		S_renewConnection();
		if (this.obj.xx == this.obj.xn && this.obj.yx == this.obj.yn && this.obj.zx == this.obj.zn) {
			this.obj = null;
			return;
		}
		tx = this.worldObj.getBlockTileEntity(this.obj.xn, this.obj.yn, this.obj.zn);
		if (tx instanceof TileMarker && ((TileMarker) tx).obj == null) ((TileMarker) tx).obj = this.obj;
		tx = this.worldObj.getBlockTileEntity(this.obj.xn, this.obj.yn, this.obj.zx);
		if (tx instanceof TileMarker && ((TileMarker) tx).obj == null) ((TileMarker) tx).obj = this.obj;
		tx = this.worldObj.getBlockTileEntity(this.obj.xn, this.obj.yx, this.obj.zn);
		if (tx instanceof TileMarker && ((TileMarker) tx).obj == null) ((TileMarker) tx).obj = this.obj;
		tx = this.worldObj.getBlockTileEntity(this.obj.xn, this.obj.yx, this.obj.zx);
		if (tx instanceof TileMarker && ((TileMarker) tx).obj == null) ((TileMarker) tx).obj = this.obj;
		tx = this.worldObj.getBlockTileEntity(this.obj.xx, this.obj.yn, this.obj.zn);
		if (tx instanceof TileMarker && ((TileMarker) tx).obj == null) ((TileMarker) tx).obj = this.obj;
		tx = this.worldObj.getBlockTileEntity(this.obj.xx, this.obj.yn, this.obj.zx);
		if (tx instanceof TileMarker && ((TileMarker) tx).obj == null) ((TileMarker) tx).obj = this.obj;
		tx = this.worldObj.getBlockTileEntity(this.obj.xx, this.obj.yx, this.obj.zn);
		if (tx instanceof TileMarker && ((TileMarker) tx).obj == null) ((TileMarker) tx).obj = this.obj;
		tx = this.worldObj.getBlockTileEntity(this.obj.xx, this.obj.yx, this.obj.zx);
		if (tx instanceof TileMarker && ((TileMarker) tx).obj == null) ((TileMarker) tx).obj = this.obj;
		this.obj.makeLaser(this.worldObj);
		PacketHandler.sendLinkPacket(this, this.obj);
		S_updateSignal();
	}

	@Override
	void S_recievePacket(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {// onPacketData
		switch (pattern) {
		case PacketHandler.link:
			if (this.obj != null) PacketHandler.sendLinkPacket(this, this.obj, ep);
			S_updateSignal();
		}
	}

	private void G_removeSignal() {
		if (this.slasers != null) for (EntityBlock eb : this.slasers)
			if (eb != null) {
				this.worldObj.removeEntity(eb);
				if (this.worldObj.isRemote) ((WorldClient) this.worldObj).removeEntityFromWorld(eb.entityId);
			}
	}

	private void G_destroy() {
		if (this.obj != null) this.obj.removeConnectionIfCannotHold(this.worldObj);
		G_removeSignal();
		ForgeChunkManager.releaseTicket(this.chunkTicket);
	}

	@Override
	void C_recievePacket(byte pattern, ByteArrayDataInput data) {// onPacketData
		switch (pattern) {
		case PacketHandler.signal:
			C_updateSignal(data.readBoolean());
			break;
		case PacketHandler.link:
			if (this.obj != null) this.obj.removeConnection(this.worldObj);
			this.obj = new Link(this.worldObj, data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
			this.obj.makeLaser(this.worldObj);
		}
	}

	private void C_updateSignal(boolean powered) {
		G_removeSignal();
		if (!powered) return;
		this.slasers = new EntityBlock[3];
		if (this.obj == null) {
			this.slasers[0] = CoreProxy.proxy.newEntityBlock(this.worldObj, this.xCoord - MAX_SIZE + 0.5D, this.yCoord + 0.45D, this.zCoord + 0.45D,
					MAX_SIZE * 2 + 0.5D, 0.1, 0.1, LaserKind.Blue);
			this.slasers[1] = CoreProxy.proxy.newEntityBlock(this.worldObj, this.xCoord + 0.45D, Math.max(0, this.yCoord - MAX_SIZE) + 0.5D,
					this.zCoord + 0.45D, 0.1, 256, 0.1, LaserKind.Blue);
			this.slasers[2] = CoreProxy.proxy.newEntityBlock(this.worldObj, this.xCoord + 0.45D, this.yCoord + 0.45D, this.zCoord - MAX_SIZE + 0.5D, 0.1, 0.1,
					MAX_SIZE * 2 + 0.5D, LaserKind.Blue);
		} else {
			if (this.obj.xn == this.obj.xx) this.slasers[0] = CoreProxy.proxy.newEntityBlock(this.worldObj, this.xCoord - MAX_SIZE + 0.5D, this.yCoord + 0.45D,
					this.zCoord + 0.45D, MAX_SIZE * 2 + 0.5D, 0.1, 0.1, LaserKind.Blue);
			if (this.obj.yn == this.obj.yx) this.slasers[1] = CoreProxy.proxy.newEntityBlock(this.worldObj, this.xCoord + 0.45D,
					Math.max(0, this.yCoord - MAX_SIZE) + 0.5D, this.zCoord + 0.45D, 0.1, 256, 0.1, LaserKind.Blue);
			if (this.obj.zn == this.obj.zx) this.slasers[2] = CoreProxy.proxy.newEntityBlock(this.worldObj, this.xCoord + 0.45D, this.yCoord + 0.45D,
					this.zCoord - MAX_SIZE + 0.5D, 0.1, 0.1, MAX_SIZE * 2 + 0.5D, LaserKind.Blue);
		}
		for (EntityBlock eb : this.slasers)
			if (eb != null) this.worldObj.spawnEntityInWorld(eb);
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
			if (!this.worldObj.isRemote) S_updateSignal();
			else PacketHandler.sendPacketToServer(this, PacketHandler.link);
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
		G_destroy();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		G_destroy();
	}

}
