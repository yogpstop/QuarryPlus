package org.yogpstop.qp;

import java.util.Collection;
import java.util.LinkedList;

import com.google.common.io.ByteArrayDataInput;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import buildcraft.api.core.IAreaProvider;

public class TileMarker extends APacketTile implements IAreaProvider {

	private static int maxSize = 256;
	private Link obj;

	class Link {
		int xx, xn, yx, yn, zx, zn;
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
	}

	public Collection<ItemStack> removeFromWorldWithItem() {
		Collection<ItemStack> ret = new LinkedList<ItemStack>();
		if (this.obj == null) {
			QuarryPlus.blockMarker.dropBlockAsItem(this.worldObj, this.xCoord, this.yCoord, this.zCoord, QuarryPlus.blockMarker.blockID, 0);
			this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
		} else {

		}
		return ret;
	}

	void updateSignals() {
		// TODO 自動生成されたメソッド・スタブ

	}

	private void renewConnection() {
		int i;
		TileEntity tx = null, ty = null, tz = null;
		if (this.obj.xx == this.obj.xn) {
			for (i = 0; i < maxSize; i++) {
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
			for (i = 0; i < maxSize; i++) {
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
			for (i = 0; i < maxSize; i++) {
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
			if (ty != null) ((TileMarker) ty).renewConnection();
			if (tz != null) ((TileMarker) tz).renewConnection();
		}
		if (this.obj.yx == this.obj.yn) {
			if (tx != null) ((TileMarker) tx).renewConnection();
			if (tz != null) ((TileMarker) tz).renewConnection();
		}
		if (this.obj.zx == this.obj.zn) {
			if (tx != null) ((TileMarker) tx).renewConnection();
			if (ty != null) ((TileMarker) ty).renewConnection();
		}
	}

	void tryConnection() {
		if (this.worldObj.isRemote) return;
		TileEntity tx;
		this.obj = new Link();
		this.obj.xx = this.obj.xn = this.xCoord;
		this.obj.yx = this.obj.yn = this.yCoord;
		this.obj.zx = this.obj.zn = this.zCoord;
		renewConnection();
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
	}

	void destroy() {

	}

	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
		super.readFromNBT(par1NBTTagCompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeToNBT(par1NBTTagCompound);
	}

	@Override
	void recievePacketOnServer(byte pattern, ByteArrayDataInput data, EntityPlayer ep) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	void recievePacketOnClient(byte pattern, ByteArrayDataInput data) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void invalidate() {
		super.invalidate();
	}

}
