package com.yogpc.mc_lib;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

public class ContainerWorkbench extends Container implements IPacketContainer {
  private static final ItemStack readItemStack(final ByteArrayDataInput bb) throws IOException {
    final short id = bb.readShort();
    if (id < 0)
      return null;
    final ItemStack is = new ItemStack(Item.getItemById(id), bb.readInt(), bb.readShort());
    final short nbtlen = bb.readShort();
    if (nbtlen >= 0) {
      final byte[] ba = new byte[nbtlen];
      bb.readFully(ba);
      is.setTagCompound(CompressedStreamTools.func_152457_a(ba, NBTSizeTracker.field_152451_a));
    }
    return is;
  }

  private static final void writeItemStack(final ItemStack is, final DataOutputStream bb)
      throws IOException {
    if (is == null) {
      bb.writeShort((short) -1);
      return;
    }
    bb.writeShort((short) Item.getIdFromItem(is.getItem()));
    bb.writeInt(is.stackSize);
    bb.writeShort((short) is.getItemDamage());
    NBTTagCompound nbttagcompound = null;
    if (is.getItem().isDamageable() || is.getItem().getShareTag())
      nbttagcompound = is.stackTagCompound;
    if (nbttagcompound != null) {
      final byte[] ba = CompressedStreamTools.compress(nbttagcompound);
      bb.writeShort((short) ba.length);
      bb.write(ba);
    } else
      bb.writeShort((short) -1);
  }

  private final TileWorkbench tile;

  public ContainerWorkbench(final IInventory pi, final TileWorkbench tw) {
    this.tile = tw;
    int row;
    int col;

    for (row = 0; row < 3; ++row)
      for (col = 0; col < 9; ++col)
        addSlotToContainer(new Slot(tw, col + row * 9, 8 + col * 18, 18 + row * 18));

    for (row = 0; row < 2; ++row)
      for (col = 0; col < 9; ++col)
        addSlotToContainer(new SlotWorkbench(tw, col + row * 9 + 27, 8 + col * 18, 90 + row * 18));

    for (row = 0; row < 3; ++row)
      for (col = 0; col < 9; ++col)
        addSlotToContainer(new Slot(pi, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));

    for (col = 0; col < 9; ++col)
      addSlotToContainer(new Slot(pi, col, 8 + col * 18, 198));
  }

  @Override
  public boolean canInteractWith(final EntityPlayer ep) {
    return this.tile.isUseableByPlayer(ep);
  }

  @Override
  public ItemStack transferStackInSlot(final EntityPlayer ep, final int i) {
    return null;// TODO
  }

  @Override
  public void addCraftingToCrafters(final ICrafting p_75132_1_) {
    if (this.crafters.contains(p_75132_1_))
      throw new IllegalArgumentException("Listener already listening");
    this.crafters.add(p_75132_1_);
    try {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream(bos);
      for (int i = 0; i < this.inventorySlots.size(); ++i) {
        dos.writeByte(i);
        writeItemStack(((Slot) this.inventorySlots.get(i)).getStack(), dos);
      }
      dos.writeByte(101);
      dos.writeShort(this.tile.cpower);
      dos.writeByte(102);
      dos.writeByte(this.tile.cur_recipe);
      dos.writeByte(-1);
      PacketHandler.sendPacketToPlayer(new YogpstopPacket(this, bos.toByteArray()),
          (EntityPlayer) p_75132_1_);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    detectAndSendChanges();
  }

  private int precipe;
  private short ppower;

  @Override
  public void detectAndSendChanges() {
    try {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream(bos);
      for (int i = 0; i < this.inventorySlots.size(); ++i) {
        final ItemStack itemstack = ((Slot) this.inventorySlots.get(i)).getStack();
        ItemStack itemstack1 = (ItemStack) this.inventoryItemStacks.get(i);
        if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
          itemstack1 = itemstack == null ? null : itemstack.copy();
          this.inventoryItemStacks.set(i, itemstack1);
          dos.writeByte(i);
          writeItemStack(itemstack1, dos);
        }
      }
      if (this.ppower != this.tile.cpower) {
        this.ppower = this.tile.cpower;
        dos.writeByte(101);
        dos.writeShort(this.tile.cpower);
      }
      if (this.precipe != this.tile.cur_recipe) {
        this.precipe = this.tile.cur_recipe;
        dos.writeByte(102);
        dos.writeByte(this.tile.cur_recipe);
      }
      if (dos.size() > 0) {
        dos.writeByte(-1);
        for (int j = 0; j < this.crafters.size(); ++j)
          PacketHandler.sendPacketToPlayer(new YogpstopPacket(this, bos.toByteArray()),
              (EntityPlayer) this.crafters.get(j));
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void receivePacket(final byte[] ba) throws IOException {
    final ByteArrayDataInput bb = ByteStreams.newDataInput(ba);
    byte slot;
    while ((slot = bb.readByte()) >= 0)
      if (slot == 101)
        this.tile.cpower = bb.readShort();
      else if (slot == 102)
        this.tile.cur_recipe = bb.readByte();
      else
        getSlot(slot).putStack(readItemStack(bb));
    return;
  }

  private int field_94535_f = -1;
  private int field_94536_g;
  private final Set<Slot> field_94537_h = new HashSet<Slot>();

  @Override
  protected void func_94533_d() {
    this.field_94536_g = 0;
    this.field_94537_h.clear();
  }

  public static boolean canDrag(final Slot slot, final ItemStack is/* , boolean always_true */) {
    boolean can = slot == null || !slot.getHasStack();

    if (slot != null && slot.getHasStack() && is != null && is.isItemEqual(slot.getStack())
        && ItemStack.areItemStackTagsEqual(slot.getStack(), is))
      // int i = always_true ? 0 : is.stackSize;
      can |= true/* slot.getStack().stackSize + i <= is.getMaxStackSize() */;

    return can;
  }

  /*
   * event_type 0 default 1 shift-click 2 hotbar 3 pickup 4 drop 5 dragged 6 double-click
   */
  @Override
  public ItemStack slotClick(final int slot_number, final int event_param, final int event_type,
      final EntityPlayer player) {
    if (0 < slot_number && slot_number <= this.inventorySlots.size()) {
      final Slot c = (Slot) this.inventorySlots.get(slot_number);
      if (c instanceof SlotWorkbench) {
        if (event_type == 0 && c.getHasStack()) {
          this.tile.cur_recipe = c.getSlotIndex();
          this.tile.markDirty();
        }
        return null;
      }
    }
    ItemStack ret_value = null;
    final InventoryPlayer inventoryplayer = player.inventory;
    int i1;
    ItemStack itemstack3;

    if (event_type == 5) {
      final int l = this.field_94536_g;
      this.field_94536_g = func_94532_c(event_param);

      if ((l != 1 || this.field_94536_g != 2) && l != this.field_94536_g)
        func_94533_d();
      else if (inventoryplayer.getItemStack() == null)
        func_94533_d();
      else if (this.field_94536_g == 0) {
        this.field_94535_f = func_94529_b(event_param);

        if (func_94528_d(this.field_94535_f)) {
          this.field_94536_g = 1;
          this.field_94537_h.clear();
        } else
          func_94533_d();
      } else if (this.field_94536_g == 1) {
        final Slot slot = (Slot) this.inventorySlots.get(slot_number);

        if (slot != null && canDrag(slot, inventoryplayer.getItemStack()/* , true */)
            && slot.isItemValid(inventoryplayer.getItemStack())
            && inventoryplayer.getItemStack().stackSize > this.field_94537_h.size()
            && canDragIntoSlot(slot))
          this.field_94537_h.add(slot);
      } else if (this.field_94536_g == 2) {
        if (!this.field_94537_h.isEmpty()) {
          itemstack3 = inventoryplayer.getItemStack().copy();
          i1 = inventoryplayer.getItemStack().stackSize;
          final Iterator<Slot> iterator = this.field_94537_h.iterator();

          while (iterator.hasNext()) {
            final Slot slot1 = iterator.next();

            if (slot1 != null && canDrag(slot1, inventoryplayer.getItemStack()/* , true */)
                && slot1.isItemValid(inventoryplayer.getItemStack())
                && inventoryplayer.getItemStack().stackSize >= this.field_94537_h.size()
                && canDragIntoSlot(slot1)) {
              final ItemStack itemstack1 = itemstack3.copy();
              final int j1 = slot1.getHasStack() ? slot1.getStack().stackSize : 0;
              func_94525_a(this.field_94537_h, this.field_94535_f, itemstack1, j1);
              /*
               * if (itemstack1.stackSize > itemstack1.getMaxStackSize()) { itemstack1.stackSize =
               * itemstack1.getMaxStackSize(); }
               */// Remove max stack size

              if (itemstack1.stackSize > slot1.getSlotStackLimit())
                itemstack1.stackSize = slot1.getSlotStackLimit();

              i1 -= itemstack1.stackSize - j1;
              slot1.putStack(itemstack1);
            }
          }

          itemstack3.stackSize = i1;

          if (itemstack3.stackSize <= 0)
            itemstack3 = null;

          inventoryplayer.setItemStack(itemstack3);
        }

        func_94533_d();
      } else
        func_94533_d();
    } else if (this.field_94536_g != 0)
      func_94533_d();
    else {
      Slot slot2;
      int l1;
      ItemStack itemstack5;

      if ((event_type == 0 || event_type == 1) && (event_param == 0 || event_param == 1)) {
        if (slot_number == -999) {
          if (inventoryplayer.getItemStack() != null && slot_number == -999) {
            if (event_param == 0) {
              player.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack(), true);
              inventoryplayer.setItemStack((ItemStack) null);
            }

            if (event_param == 1) {
              player.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack().splitStack(1),
                  true);

              if (inventoryplayer.getItemStack().stackSize == 0)
                inventoryplayer.setItemStack((ItemStack) null);
            }
          }
        } else if (event_type == 1) {
          if (slot_number < 0)
            return null;

          slot2 = (Slot) this.inventorySlots.get(slot_number);

          if (slot2 != null && slot2.canTakeStack(player)) {
            itemstack3 = transferStackInSlot(player, slot_number);

            if (itemstack3 != null) {
              final Item item = itemstack3.getItem();
              ret_value = itemstack3.copy();

              if (slot2.getStack() != null && slot2.getStack().getItem() == item)
                retrySlotClick(slot_number, event_param, true, player);
            }
          }
        } else {
          if (slot_number < 0)
            return null;

          slot2 = (Slot) this.inventorySlots.get(slot_number);

          if (slot2 != null) {
            itemstack3 = slot2.getStack();
            final ItemStack player_stack = inventoryplayer.getItemStack();

            if (itemstack3 != null)
              ret_value = itemstack3.copy();

            if (itemstack3 == null) {
              if (player_stack != null && slot2.isItemValid(player_stack)) {
                l1 = event_param == 0 ? player_stack.stackSize : 1;

                if (l1 > slot2.getSlotStackLimit())
                  l1 = slot2.getSlotStackLimit();

                if (player_stack.stackSize >= l1)
                  slot2.putStack(player_stack.splitStack(l1));

                if (player_stack.stackSize == 0)
                  inventoryplayer.setItemStack((ItemStack) null);
              }
            } else if (slot2.canTakeStack(player))
              if (player_stack == null) {
                l1 = event_param == 0 ? itemstack3.stackSize : (itemstack3.stackSize + 1) / 2;
                itemstack5 = slot2.decrStackSize(l1);
                inventoryplayer.setItemStack(itemstack5);

                if (itemstack3.stackSize == 0)
                  slot2.putStack((ItemStack) null);

                slot2.onPickupFromSlot(player, inventoryplayer.getItemStack());
              } else if (slot2.isItemValid(player_stack)) {
                if (itemstack3.getItem() == player_stack.getItem()
                    && itemstack3.getItemDamage() == player_stack.getItemDamage()
                    && ItemStack.areItemStackTagsEqual(itemstack3, player_stack)) {
                  l1 = event_param == 0 ? player_stack.stackSize : 1;

                  if (l1 > slot2.getSlotStackLimit() - itemstack3.stackSize)
                    l1 = slot2.getSlotStackLimit() - itemstack3.stackSize;

                  player_stack.splitStack(l1);

                  if (player_stack.stackSize == 0)
                    inventoryplayer.setItemStack((ItemStack) null);

                  itemstack3.stackSize += l1;
                } else if (player_stack.stackSize <= slot2.getSlotStackLimit()) {
                  slot2.putStack(player_stack);
                  inventoryplayer.setItemStack(itemstack3);
                }
              } else if (itemstack3.getItem() == player_stack.getItem()
                  && player_stack.getMaxStackSize() > 1
                  && (!itemstack3.getHasSubtypes() || itemstack3.getItemDamage() == player_stack
                      .getItemDamage())
                  && ItemStack.areItemStackTagsEqual(itemstack3, player_stack)) {
                l1 = itemstack3.stackSize;

                if (l1 > 0 && l1 + player_stack.stackSize <= player_stack.getMaxStackSize()) {
                  player_stack.stackSize += l1;
                  itemstack3 = slot2.decrStackSize(l1);

                  if (itemstack3.stackSize == 0)
                    slot2.putStack((ItemStack) null);

                  slot2.onPickupFromSlot(player, inventoryplayer.getItemStack());
                }
              }

            slot2.onSlotChanged();
          }
        }
      } else if (event_type == 2 && event_param >= 0 && event_param < 9) {
        slot2 = (Slot) this.inventorySlots.get(slot_number);

        if (slot2.canTakeStack(player)) {
          itemstack3 = inventoryplayer.getStackInSlot(event_param);
          boolean flag =
              itemstack3 == null || slot2.inventory == inventoryplayer
                  && slot2.isItemValid(itemstack3);
          l1 = -1;

          if (!flag) {
            l1 = inventoryplayer.getFirstEmptyStack();
            flag |= l1 > -1;
          }

          if (slot2.getHasStack() && flag) {
            itemstack5 = slot2.getStack();
            inventoryplayer.setInventorySlotContents(event_param, itemstack5.copy());

            if ((slot2.inventory != inventoryplayer || !slot2.isItemValid(itemstack3))
                && itemstack3 != null) {
              if (l1 > -1) {
                inventoryplayer.addItemStackToInventory(itemstack3);
                slot2.decrStackSize(itemstack5.stackSize);
                slot2.putStack((ItemStack) null);
                slot2.onPickupFromSlot(player, itemstack5);
              }
            } else {
              slot2.decrStackSize(itemstack5.stackSize);
              slot2.putStack(itemstack3);
              slot2.onPickupFromSlot(player, itemstack5);
            }
          } else if (!slot2.getHasStack() && itemstack3 != null && slot2.isItemValid(itemstack3)) {
            inventoryplayer.setInventorySlotContents(event_param, (ItemStack) null);
            slot2.putStack(itemstack3);
          }
        }
      } else if (event_type == 3 && player.capabilities.isCreativeMode
          && inventoryplayer.getItemStack() == null && slot_number >= 0) {
        slot2 = (Slot) this.inventorySlots.get(slot_number);

        if (slot2 != null && slot2.getHasStack()) {
          itemstack3 = slot2.getStack().copy();
          itemstack3.stackSize = itemstack3.getMaxStackSize();
          inventoryplayer.setItemStack(itemstack3);
        }
      } else if (event_type == 4 && inventoryplayer.getItemStack() == null && slot_number >= 0) {
        slot2 = (Slot) this.inventorySlots.get(slot_number);

        if (slot2 != null && slot2.getHasStack() && slot2.canTakeStack(player)) {
          itemstack3 = slot2.decrStackSize(event_param == 0 ? 1 : slot2.getStack().stackSize);
          slot2.onPickupFromSlot(player, itemstack3);
          player.dropPlayerItemWithRandomChoice(itemstack3, true);
        }
      } else if (event_type == 6 && slot_number >= 0) {
        slot2 = (Slot) this.inventorySlots.get(slot_number);
        itemstack3 = inventoryplayer.getItemStack();

        if (itemstack3 != null
            && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(player))) {
          i1 = event_param == 0 ? 0 : this.inventorySlots.size() - 1;
          l1 = event_param == 0 ? 1 : -1;

          for (int i2 = 0; i2 < 2; ++i2)
            for (int j2 = i1; j2 >= 0 && j2 < this.inventorySlots.size()
                && itemstack3.stackSize < itemstack3.getMaxStackSize(); j2 += l1) {
              final Slot slot3 = (Slot) this.inventorySlots.get(j2);

              if (slot3.getHasStack() && canDrag(slot3, itemstack3/* , true */)
                  && slot3.canTakeStack(player) && func_94530_a(itemstack3, slot3)
                  && (i2 != 0 || slot3.getStack().stackSize != slot3.getStack().getMaxStackSize())) {
                final int k1 =
                    Math.min(itemstack3.getMaxStackSize() - itemstack3.stackSize,
                        slot3.getStack().stackSize);
                final ItemStack itemstack2 = slot3.decrStackSize(k1);
                itemstack3.stackSize += k1;

                if (itemstack2.stackSize <= 0)
                  slot3.putStack((ItemStack) null);

                slot3.onPickupFromSlot(player, itemstack2);
              }
            }
        }

        detectAndSendChanges();
      }
    }

    return ret_value;
  }

  @Override
  protected boolean mergeItemStack(final ItemStack p_75135_1_, final int p_75135_2_,
      final int p_75135_3_, final boolean p_75135_4_) {
    boolean flag1 = false;
    int k = p_75135_2_;

    if (p_75135_4_)
      k = p_75135_3_ - 1;

    Slot slot;
    ItemStack itemstack1;

    if (p_75135_1_.isStackable())
      while (p_75135_1_.stackSize > 0
          && (!p_75135_4_ && k < p_75135_3_ || p_75135_4_ && k >= p_75135_2_)) {
        slot = (Slot) this.inventorySlots.get(k);
        itemstack1 = slot.getStack();

        if (itemstack1 != null
            && itemstack1.getItem() == p_75135_1_.getItem()
            && (!p_75135_1_.getHasSubtypes() || p_75135_1_.getItemDamage() == itemstack1
                .getItemDamage()) && ItemStack.areItemStackTagsEqual(p_75135_1_, itemstack1)) {
          final int l = itemstack1.stackSize + p_75135_1_.stackSize;

          if (l <= slot.getSlotStackLimit()) {
            p_75135_1_.stackSize = 0;
            itemstack1.stackSize = l;
            slot.onSlotChanged();
            flag1 = true;
          } else if (itemstack1.stackSize < slot.getSlotStackLimit()) {
            p_75135_1_.stackSize -= slot.getSlotStackLimit() - itemstack1.stackSize;
            itemstack1.stackSize = slot.getSlotStackLimit();
            slot.onSlotChanged();
            flag1 = true;
          }
        }

        if (p_75135_4_)
          --k;
        else
          ++k;
      }

    if (p_75135_1_.stackSize > 0) {
      if (p_75135_4_)
        k = p_75135_3_ - 1;
      else
        k = p_75135_2_;

      while (!p_75135_4_ && k < p_75135_3_ || p_75135_4_ && k >= p_75135_2_) {
        slot = (Slot) this.inventorySlots.get(k);
        itemstack1 = slot.getStack();

        if (itemstack1 == null) {
          slot.putStack(p_75135_1_.copy());
          slot.onSlotChanged();
          p_75135_1_.stackSize = 0;
          flag1 = true;
          break;
        }

        if (p_75135_4_)
          --k;
        else
          ++k;
      }
    }

    return flag1;
  }
}
