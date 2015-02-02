/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.inventory.IInventoryConnection;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.yogpc.mc_lib.APowerTile;
import com.yogpc.mc_lib.InvUtils;
import com.yogpc.mc_lib.PacketHandler;
import com.yogpc.mc_lib.ReflectionHelper;
import com.yogpc.qp.QuarryPlus.BlockData;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.registry.GameData;

@Optional.Interface(iface = "cofh.api.inventory.IInventoryConnection", modid = "CoFHCore")
public abstract class TileBasic extends APowerTile implements IEnchantableTile, IInventory,
    IInventoryConnection {
  protected ForgeDirection pump = ForgeDirection.UNKNOWN;

  public final List<BlockData> fortuneList = new ArrayList<BlockData>();
  public final List<BlockData> silktouchList = new ArrayList<BlockData>();
  public boolean fortuneInclude, silktouchInclude;

  protected byte unbreaking;
  protected byte fortune;
  protected boolean silktouch;
  protected byte efficiency;

  protected final LinkedList<ItemStack> cacheItems = new LinkedList<ItemStack>();

  @Override
  protected void S_recievePacket(final byte id, final byte[] data, final EntityPlayer ep) {
    final ByteArrayDataInput badi = ByteStreams.newDataInput(data);
    switch (id) {
      case PacketHandler.CtS_REMOVE_FORTUNE:
        this.fortuneList.remove(new BlockData(badi.readUTF(), badi.readInt()));
        break;
      case PacketHandler.CtS_REMOVE_SILKTOUCH:
        this.silktouchList.remove(new BlockData(badi.readUTF(), badi.readInt()));
        break;
      case PacketHandler.CtS_TOGGLE_FORTUNE:
        this.fortuneInclude = !this.fortuneInclude;
        break;
      case PacketHandler.CtS_TOGGLE_SILKTOUCH:
        this.silktouchInclude = !this.silktouchInclude;
        break;
    }
  }

  protected abstract void G_renew_powerConfigure();

  protected abstract void G_destroy();

  @Override
  public final void onChunkUnload() {
    G_destroy();
    super.onChunkUnload();
  }

  @Override
  protected void C_recievePacket(final byte id, final byte[] data, final EntityPlayer ep) {}

  protected void S_pollItems() {
    ItemStack is;
    while (null != (is = this.cacheItems.poll())) {
      InvUtils.injectToNearTile(this.worldObj, this.xCoord, this.yCoord, this.zCoord, is);
      if (is.stackSize > 0) {
        this.cacheItems.add(is);
        break;
      }
    }
  }

  protected boolean S_breakBlock(final int x, final int y, final int z) {
    final Collection<ItemStack> dropped = new LinkedList<ItemStack>();
    final Block b =
        this.worldObj.getChunkProvider().loadChunk(x >> 4, z >> 4).getBlock(x & 0xF, y, z & 0xF);
    if (b == null || b.isAir(this.worldObj, x, y, z))
      return true;
    if (TilePump.isLiquid(b, false, null, 0, 0, 0, 0)) {
      final TileEntity te =
          this.worldObj.getTileEntity(this.xCoord + this.pump.offsetX, this.yCoord
              + this.pump.offsetY, this.zCoord + this.pump.offsetZ);
      if (!(te instanceof TilePump)) {
        this.pump = ForgeDirection.UNKNOWN;
        G_renew_powerConfigure();
        return true;
      }
      return ((TilePump) te).S_removeLiquids(this, x, y, z);
    }
    if (!PowerManager.useEnergyB(this, b.getBlockHardness(this.worldObj, x, y, z),
        S_addDroppedItems(dropped, b, x, y, z), this.unbreaking, this))
      return false;
    this.cacheItems.addAll(dropped);
    this.worldObj.playAuxSFXAtEntity(null, 2001, x, y, z,
        Block.getIdFromBlock(b) | this.worldObj.getBlockMetadata(x, y, z) << 12);
    this.worldObj.setBlockToAir(x, y, z);
    return true;
  }

  boolean S_connect(final ForgeDirection fd) {
    final TileEntity te =
        this.worldObj.getTileEntity(this.xCoord + this.pump.offsetX, this.yCoord
            + this.pump.offsetY, this.zCoord + this.pump.offsetZ);
    if (te instanceof TilePump && this.pump != fd)
      return false;
    this.pump = fd;
    G_renew_powerConfigure();
    return true;
  }

  private byte S_addDroppedItems(final Collection<ItemStack> list, final Block b, final int x,
      final int y, final int z) {
    final int meta = this.worldObj.getBlockMetadata(x, y, z);
    if (b.canSilkHarvest(this.worldObj, null, x, y, z, meta)
        && this.silktouch
        && this.silktouchList.contains(new BlockData(GameData.getBlockRegistry()
            .getNameForObject(b), meta)) == this.silktouchInclude) {
      list.add((ItemStack) ReflectionHelper.invoke(createStackedBlock, b, new Integer(meta)));
      return -1;
    }
    if (this.fortuneList.contains(new BlockData(GameData.getBlockRegistry().getNameForObject(b),
        meta)) == this.fortuneInclude) {
      list.addAll(b.getDrops(this.worldObj, x, y, z, meta, this.fortune));
      return this.fortune;
    }
    list.addAll(b.getDrops(this.worldObj, x, y, z, meta, 0));
    return 0;
  }

  static final Method createStackedBlock = ReflectionHelper.getMethod(Block.class, new String[] {
      "func_149644_j", "createStackedBlock"}, new Class<?>[] {int.class});

  @Override
  public void readFromNBT(final NBTTagCompound nbttc) {
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

  private static void readLongCollection(final NBTTagList nbttl, final Collection<BlockData> target) {
    target.clear();
    for (int i = 0; i < nbttl.tagCount(); i++) {
      final NBTTagCompound c = nbttl.getCompoundTagAt(i);
      target.add(new BlockData(c.getString("name"), c.getInteger("meta")));
    }
  }

  @Override
  public void writeToNBT(final NBTTagCompound nbttc) {
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

  private static NBTTagList writeLongCollection(final Collection<BlockData> target) {
    final NBTTagList nbttl = new NBTTagList();
    for (final BlockData l : target) {
      final NBTTagCompound c = new NBTTagCompound();
      c.setString("name", l.name);
      c.setInteger("meta", l.meta);
      nbttl.appendTag(c);
    }
    return nbttl;
  }

  @Override
  public Map<Integer, Byte> get() {
    final Map<Integer, Byte> ret = new HashMap<Integer, Byte>();
    if (this.efficiency > 0)
      ret.put(Integer.valueOf(Enchantment.efficiency.effectId), Byte.valueOf(this.efficiency));
    if (this.fortune > 0)
      ret.put(Integer.valueOf(Enchantment.fortune.effectId), Byte.valueOf(this.fortune));
    if (this.unbreaking > 0)
      ret.put(Integer.valueOf(Enchantment.unbreaking.effectId), Byte.valueOf(this.unbreaking));
    if (this.silktouch)
      ret.put(Integer.valueOf(Enchantment.silkTouch.effectId), Byte.valueOf((byte) 1));
    return ret;
  }

  @Override
  public void set(final int id, final byte val) {
    if (id == Enchantment.efficiency.effectId)
      this.efficiency = val;
    else if (id == Enchantment.fortune.effectId)
      this.fortune = val;
    else if (id == Enchantment.unbreaking.effectId)
      this.unbreaking = val;
    else if (id == Enchantment.silkTouch.effectId && val > 0)
      this.silktouch = true;
  }

  @Override
  public int getSizeInventory() {
    return Math.max(1, this.cacheItems.size());
  }

  @Override
  public ItemStack getStackInSlot(final int i) {// NOTE better way?
    return i < 0 || i >= this.cacheItems.size() ? new ItemStack(Blocks.cobblestone, 0)
        : this.cacheItems.get(i);
  }

  @Override
  public ItemStack decrStackSize(final int i, final int a) {
    if (i < 0 || i >= this.cacheItems.size())
      return null;
    final ItemStack from = this.cacheItems.get(i);
    final ItemStack res =
        new ItemStack(from.getItem(), Math.min(a, from.stackSize), from.getItemDamage());
    if (from.stackTagCompound != null)
      res.stackTagCompound = (NBTTagCompound) from.stackTagCompound.copy();
    from.stackSize -= res.stackSize;
    if (from.stackSize == 0)
      this.cacheItems.remove(i);
    return res;
  }

  @Override
  public ItemStack getStackInSlotOnClosing(final int i) {
    return i < 0 || i >= this.cacheItems.size() ? new ItemStack(Blocks.cobblestone, 0)
        : this.cacheItems.get(i);
  }

  @Override
  public void setInventorySlotContents(final int i, final ItemStack is) {
    if (is != null && is.stackSize > 0)
      System.err.println("QuarryPlus WARN: call setInventorySlotContents with non null ItemStack.");
    if (i >= 0 && i < this.cacheItems.size())
      this.cacheItems.remove(i);
  }

  @Override
  public String getInventoryName() {
    return "container.yog.basic";
  }

  @Override
  public boolean hasCustomInventoryName() {
    return false;
  }

  @Override
  public int getInventoryStackLimit() {
    return 0;
  }

  @Override
  public void markDirty() {}

  @Override
  public boolean isUseableByPlayer(final EntityPlayer p_70300_1_) {
    return false;
  }

  @Override
  public void openInventory() {}

  @Override
  public void closeInventory() {}

  @Override
  public boolean isItemValidForSlot(final int p_94041_1_, final ItemStack p_94041_2_) {
    return false;
  }

  @Override
  @Optional.Method(modid = "CoFHCore")
  public ConnectionType canConnectInventory(final ForgeDirection arg0) {
    return ConnectionType.FORCE;
  }
}
