package com.yogpc.mc_lib;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class TileWorkbench extends APowerTile implements IInventory {
  private static final ItemStack loadItemStackFromNBT(final NBTTagCompound nbt) {
    final Item i = Item.getItemById(nbt.getShort("id"));
    if (i == null)
      return null;
    final int a = nbt.getInteger("Count");
    int d = nbt.getShort("Damage");
    if (d < 0)
      d = 0;
    final ItemStack r = new ItemStack(i, a, d);
    if (nbt.hasKey("tag", 10))
      r.setTagCompound(nbt.getCompoundTag("tag"));
    return r;
  }

  private static final void writeItemStackToNBT(final ItemStack is, final NBTTagCompound nbt) {
    nbt.setShort("id", (short) Item.getIdFromItem(is.getItem()));
    nbt.setInteger("Count", is.stackSize);
    nbt.setShort("Damage", (short) is.getItemDamage());

    if (is.stackTagCompound != null)
      nbt.setTag("tag", is.stackTagCompound);
  }

  final ItemStack[] inv = new ItemStack[45];
  final WorkbenchRecipe[] recipies = new WorkbenchRecipe[45];// 0-26 always null
  int cur_recipe = -1;
  short cpower = 0;

  public TileWorkbench() {
    configure(250, 0);
  }

  @Override
  public void updateEntity() {
    super.updateEntity();
    if (this.worldObj.isRemote)
      return;
    if (this.cur_recipe >= 0) {
      this.cpower = (short) (160 * getStoredEnergy() / this.recipies[this.cur_recipe].power);
      if (this.recipies[this.cur_recipe].power <= getStoredEnergy()) {
        useEnergy(this.recipies[this.cur_recipe].power, this.recipies[this.cur_recipe].power, true);
        this.recipies[this.cur_recipe].check(this.inv);
        for (int i = 0; i < 27; ++i)
          if (this.inv[i] != null && this.inv[i].stackSize <= 0)
            this.inv[i] = null;
        final ItemStack is = this.inv[this.cur_recipe].copy();
        InvUtils.injectToNearTile(this.worldObj, this.xCoord, this.yCoord, this.zCoord, is);
        if (is.stackSize > 0) {
          final float f = 0.7F;
          final double d0 = this.worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
          final double d1 = this.worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
          final double d2 = this.worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
          final EntityItem entityitem =
              new EntityItem(this.worldObj, this.xCoord + d0, this.yCoord + d1, this.zCoord + d2,
                  is);
          entityitem.delayBeforeCanPickup = 10;
          this.worldObj.spawnEntityInWorld(entityitem);
        }
        markDirty();
      }
    } else
      this.cpower = 0;
  }

  @Override
  public void readFromNBT(final NBTTagCompound roottag) {
    super.readFromNBT(roottag);
    final NBTTagList islist = roottag.getTagList("Items", 10);
    for (int i = 0; i < islist.tagCount(); ++i) {
      final NBTTagCompound istag = islist.getCompoundTagAt(i);
      final int j = istag.getByte("Slot") & 255;
      if (j >= 0 && j < 27)
        this.inv[j] = loadItemStackFromNBT(istag);
    }
    WorkbenchRecipe.getRecipes(this.inv, this.recipies, -1);
    this.cur_recipe = roottag.getInteger("Recipe");
    if (this.cur_recipe >= 0 && this.recipies[this.cur_recipe] == null)
      this.cur_recipe = -1;
    if (this.cur_recipe >= 0)
      configure(250, this.recipies[this.cur_recipe].power * 2);
    else
      configure(250, 0);
  }

  @Override
  public void writeToNBT(final NBTTagCompound roottag) {
    super.writeToNBT(roottag);
    final NBTTagList islist = new NBTTagList();
    for (int i = 0; i < 27; ++i)
      if (this.inv[i] != null) {
        final NBTTagCompound istag = new NBTTagCompound();
        istag.setByte("Slot", (byte) i);
        writeItemStackToNBT(this.inv[i], istag);
        islist.appendTag(istag);
      }
    roottag.setTag("Items", islist);
    roottag.setInteger("Recipe", this.cur_recipe);
  }

  @Override
  public void markDirty() {
    super.markDirty();
    this.cur_recipe = WorkbenchRecipe.getRecipes(this.inv, this.recipies, this.cur_recipe);
    if (this.cur_recipe >= 0)
      configure(250, this.recipies[this.cur_recipe].power * 2);
    else
      configure(250, 0);
  }

  @Override
  protected void S_recievePacket(final byte id, final byte[] data, final EntityPlayer ep) {}

  @Override
  protected void C_recievePacket(final byte id, final byte[] data, final EntityPlayer ep) {}

  @Override
  public int getSizeInventory() {
    return this.inv.length;
  }

  @Override
  public ItemStack getStackInSlot(final int i) {
    return this.inv[i];
  }

  @Override
  public ItemStack getStackInSlotOnClosing(final int i) {
    final ItemStack is = this.inv[i];
    this.inv[i] = null;
    markDirty();
    return is;
  }

  @Override
  public ItemStack decrStackSize(final int i, final int _a) {
    if (this.inv[i] != null) {
      final int a = Math.min(_a, this.inv[i].getMaxStackSize());
      ItemStack itemstack;

      if (this.inv[i].stackSize <= a) {
        itemstack = this.inv[i];
        this.inv[i] = null;
        markDirty();
        return itemstack;
      }
      itemstack = this.inv[i].splitStack(a);

      if (this.inv[i].stackSize == 0)
        this.inv[i] = null;

      markDirty();
      return itemstack;
    }
    return null;
  }

  @Override
  public void setInventorySlotContents(final int i, final ItemStack is) {
    this.inv[i] = is;
    markDirty();
  }

  @Override
  public boolean isItemValidForSlot(final int p_94041_1_, final ItemStack p_94041_2_) {
    return true;
  }

  @Override
  public String getInventoryName() {
    return "";
  }

  @Override
  public boolean hasCustomInventoryName() {
    return false;
  }

  @Override
  public int getInventoryStackLimit() {
    return Integer.MAX_VALUE;
  }

  @Override
  public boolean isUseableByPlayer(final EntityPlayer ep) {
    return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : ep
        .getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
  }

  @Override
  public void openInventory() {}

  @Override
  public void closeInventory() {}
}
