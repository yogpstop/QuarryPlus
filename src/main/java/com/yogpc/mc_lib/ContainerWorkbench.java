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
        addSlotToContainer(new SlotUnlimited(tw, col + row * 9, 8 + col * 18, 18 + row * 18));

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
    if (27 <= i && i < 45)
      return null;
    ItemStack src = null;
    final Slot slot = (Slot) this.inventorySlots.get(i);
    if (slot != null && slot.getHasStack()) {
      final ItemStack remain = slot.getStack();
      src = remain.copy();
      if (i < 27) {
        if (!mergeItemStack(remain, 45, 81, true))
          return null;
      } else if (!mergeItemStack(remain, 0, 27, false))
        return null;
      if (remain.stackSize == 0)
        slot.putStack((ItemStack) null);
      else
        slot.onSlotChanged();
      if (remain.stackSize == src.stackSize)
        return null;
      slot.onPickupFromSlot(ep, remain);
    }
    return src;
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
  /*
   * drag_state 0 start 1 content 2 end
   */
  private int drag_state;
  private final Set<Slot> dragged = new HashSet<Slot>();

  @Override
  protected void func_94533_d() {
    this.drag_state = 0;
    this.dragged.clear();
  }

  public static boolean canDrag(final Slot slot, final ItemStack is, final boolean always_true) {
    boolean can = slot == null || !slot.getHasStack();
    if (slot != null && slot.getHasStack() && is != null && is.isItemEqual(slot.getStack())
        && ItemStack.areItemStackTagsEqual(slot.getStack(), is)) {
      final int i = always_true ? 0 : is.stackSize;
      can |=
          slot.getStack().stackSize + i <= (slot instanceof SlotUnlimited ? slot
              .getSlotStackLimit() : is.getMaxStackSize());
    }
    return can;
  }

  /*
   * type 0 default 1 shift-click 2 hotbar 3 pickup 4 drop 5 dragged 6 double-click
   */
  @Override
  public ItemStack slotClick(final int i, final int param, final int type, final EntityPlayer ep) {
    ItemStack ret = null;
    final InventoryPlayer player_inv = ep.inventory;
    if (type == 5) {
      final int prev_drag_state = this.drag_state;
      this.drag_state = func_94532_c(param);
      if ((prev_drag_state != 1 || this.drag_state != 2) && prev_drag_state != this.drag_state)
        func_94533_d();
      else if (player_inv.getItemStack() == null)
        func_94533_d();
      else if (this.drag_state == 0) {
        this.field_94535_f = func_94529_b(param);
        if (func_94528_d(this.field_94535_f)) {
          this.drag_state = 1;
          this.dragged.clear();
        } else
          func_94533_d();
      } else if (this.drag_state == 1) {
        final Slot slot = (Slot) this.inventorySlots.get(i);
        if (slot != null && canDrag(slot, player_inv.getItemStack(), true)
            && slot.isItemValid(player_inv.getItemStack())
            && player_inv.getItemStack().stackSize > this.dragged.size() && canDragIntoSlot(slot))
          this.dragged.add(slot);
      } else if (this.drag_state == 2) {
        if (!this.dragged.isEmpty()) {
          ItemStack player_stack = player_inv.getItemStack().copy();
          int i1 = player_inv.getItemStack().stackSize;
          final Iterator<Slot> iterator = this.dragged.iterator();
          while (iterator.hasNext()) {
            final Slot slot = iterator.next();
            if (slot != null && canDrag(slot, player_inv.getItemStack(), true)
                && slot.isItemValid(player_inv.getItemStack())
                && player_inv.getItemStack().stackSize >= this.dragged.size()
                && canDragIntoSlot(slot)) {
              final ItemStack slot_stack = player_stack.copy();
              final int j1 = slot.getHasStack() ? slot.getStack().stackSize : 0;
              func_94525_a(this.dragged, this.field_94535_f, slot_stack, j1);
              if (!(slot instanceof SlotUnlimited)
                  && slot_stack.stackSize > slot_stack.getMaxStackSize())
                slot_stack.stackSize = slot_stack.getMaxStackSize();
              if (slot_stack.stackSize > slot.getSlotStackLimit())
                slot_stack.stackSize = slot.getSlotStackLimit();
              i1 -= slot_stack.stackSize - j1;
              slot.putStack(slot_stack);
            }
          }
          player_stack.stackSize = i1;
          if (player_stack.stackSize <= 0)
            player_stack = null;
          player_inv.setItemStack(player_stack);
        }
        func_94533_d();
      } else
        func_94533_d();
    } else if (this.drag_state != 0)
      func_94533_d();
    else if ((type == 0 || type == 1) && (param == 0 || param == 1)) {
      if (i == -999) {
        if (player_inv.getItemStack() != null && i == -999) {
          if (param == 0) {
            ep.dropPlayerItemWithRandomChoice(player_inv.getItemStack(), true);
            player_inv.setItemStack(null);
          }
          if (param == 1) {
            ep.dropPlayerItemWithRandomChoice(player_inv.getItemStack().splitStack(1), true);
            if (player_inv.getItemStack().stackSize == 0)
              player_inv.setItemStack(null);
          }
        }
      } else if (type == 1) {
        if (i < 0)
          return null;
        final Slot slot = (Slot) this.inventorySlots.get(i);
        if (slot != null && slot.canTakeStack(ep)) {
          final ItemStack itemstack3 = transferStackInSlot(ep, i);
          if (itemstack3 != null) {
            final Item item = itemstack3.getItem();
            ret = itemstack3.copy();
            if (slot.getStack() != null && slot.getStack().getItem() == item)
              retrySlotClick(i, param, true, ep);
          }
        }
      } else {
        if (i < 0)
          return null;
        final Slot slot = (Slot) this.inventorySlots.get(i);
        if (slot != null) {
          ItemStack slot_stack = slot.getStack();
          final ItemStack player_stack = player_inv.getItemStack();
          if (slot_stack != null)
            ret = slot_stack.copy();
          if (slot_stack == null) {
            if (player_stack != null && slot.isItemValid(player_stack)) {
              int l1 = param == 0 ? player_stack.stackSize : 1;
              if (l1 > slot.getSlotStackLimit())
                l1 = slot.getSlotStackLimit();
              if (player_stack.stackSize >= l1)
                slot.putStack(player_stack.splitStack(l1));
              if (player_stack.stackSize == 0)
                player_inv.setItemStack(null);
            }
          } else if (slot.canTakeStack(ep))
            if (player_stack == null) {
              final int l1 = param == 0 ? slot_stack.stackSize : (slot_stack.stackSize + 1) / 2;
              player_inv.setItemStack(slot.decrStackSize(l1));
              if (slot_stack.stackSize == 0)
                slot.putStack(null);
              slot.onPickupFromSlot(ep, player_inv.getItemStack());
            } else if (slot.isItemValid(player_stack)) {
              if (slot_stack.getItem() == player_stack.getItem()
                  && slot_stack.getItemDamage() == player_stack.getItemDamage()
                  && ItemStack.areItemStackTagsEqual(slot_stack, player_stack)) {
                int player2slot = param == 0 ? player_stack.stackSize : 1;
                if (player2slot > slot.getSlotStackLimit() - slot_stack.stackSize)
                  player2slot = slot.getSlotStackLimit() - slot_stack.stackSize;
                if (!(slot instanceof SlotUnlimited)
                    && player2slot > player_stack.getMaxStackSize() - slot_stack.stackSize)
                  player2slot = player_stack.getMaxStackSize() - slot_stack.stackSize;
                player_stack.stackSize -= player2slot;
                if (player_stack.stackSize == 0)
                  player_inv.setItemStack(null);
                slot_stack.stackSize += player2slot;
              } else if (player_stack.stackSize <= slot.getSlotStackLimit()) {
                slot.putStack(player_stack);
                player_inv.setItemStack(slot_stack);
              }
            } else if (slot_stack.getItem() == player_stack.getItem()
                && player_stack.getMaxStackSize() > 1
                && (!slot_stack.getHasSubtypes() || slot_stack.getItemDamage() == player_stack
                    .getItemDamage()) && ItemStack.areItemStackTagsEqual(slot_stack, player_stack)) {
              final int add2player = slot_stack.stackSize;
              if (add2player > 0
                  && add2player + player_stack.stackSize <= player_stack.getMaxStackSize()) {
                player_stack.stackSize += add2player;
                slot_stack = slot.decrStackSize(add2player);
                if (slot_stack.stackSize == 0)
                  slot.putStack(null);
                slot.onPickupFromSlot(ep, player_inv.getItemStack());
              }
            }
          slot.onSlotChanged();
        }
      }
    } else if (type == 2 && param >= 0 && param < 9) {
      final Slot slot = (Slot) this.inventorySlots.get(i);
      if (slot.canTakeStack(ep)) {
        final ItemStack player_stack = player_inv.getStackInSlot(param);
        boolean can2player =
            player_stack == null || slot.inventory == player_inv && slot.isItemValid(player_stack);
        int first_empty = -1;
        if (!can2player) {
          first_empty = player_inv.getFirstEmptyStack();
          can2player |= first_empty > -1;
        }
        if (slot.getHasStack() && can2player) {// to player
          final ItemStack slot_stack = slot.getStack();
          player_inv.setInventorySlotContents(param, slot_stack.copy());
          if ((slot.inventory != player_inv || !slot.isItemValid(player_stack))
              && player_stack != null) {// prev2player
            if (first_empty > -1) {
              player_inv.addItemStackToInventory(player_stack);
              slot.decrStackSize(slot_stack.stackSize);
              slot.putStack(null);
              slot.onPickupFromSlot(ep, slot_stack);
            }
          } else {// prev2slot
            slot.decrStackSize(slot_stack.stackSize);
            slot.putStack(player_stack);
            slot.onPickupFromSlot(ep, slot_stack);
          }
        } else if (!slot.getHasStack() && player_stack != null && slot.isItemValid(player_stack)) {
          // player2slot
          player_inv.setInventorySlotContents(param, null);
          slot.putStack(player_stack);
        }
      }
    } else if (type == 3 && ep.capabilities.isCreativeMode && player_inv.getItemStack() == null
        && i >= 0) {
      final Slot slot = (Slot) this.inventorySlots.get(i);
      if (slot != null && slot.getHasStack()) {
        final ItemStack player_stack = slot.getStack().copy();
        player_stack.stackSize = player_stack.getMaxStackSize();
        player_inv.setItemStack(player_stack);
      }
    } else if (type == 4 && player_inv.getItemStack() == null && i >= 0) {
      final Slot slot = (Slot) this.inventorySlots.get(i);
      if (slot != null && slot.getHasStack() && slot.canTakeStack(ep)) {
        final ItemStack drop_stack = slot.decrStackSize(param == 0 ? 1 : slot.getStack().stackSize);
        slot.onPickupFromSlot(ep, drop_stack);
        ep.dropPlayerItemWithRandomChoice(drop_stack, true);
      }
    } else if (type == 6 && i >= 0) {
      final Slot slot = (Slot) this.inventorySlots.get(i);
      final ItemStack player_stack = player_inv.getItemStack();
      if (player_stack != null && (slot == null || !slot.getHasStack() || !slot.canTakeStack(ep))) {
        final int i1 = param == 0 ? 0 : this.inventorySlots.size() - 1;
        final int l1 = param == 0 ? 1 : -1;
        for (int i2 = 0; i2 < 2; ++i2)
          for (int j2 = i1; j2 >= 0 && j2 < this.inventorySlots.size()
              && player_stack.stackSize < player_stack.getMaxStackSize(); j2 += l1) {
            final Slot slot3 = (Slot) this.inventorySlots.get(j2);
            if (slot3.getHasStack() && canDrag(slot3, player_stack, true) && slot3.canTakeStack(ep)
                && func_94530_a(player_stack, slot3)
                && (i2 != 0 || slot3.getStack().stackSize != slot3.getStack().getMaxStackSize())) {
              final int k1 =
                  Math.min(player_stack.getMaxStackSize() - player_stack.stackSize,
                      slot3.getStack().stackSize);
              final ItemStack itemstack2 = slot3.decrStackSize(k1);
              player_stack.stackSize += k1;
              if (itemstack2.stackSize <= 0)
                slot3.putStack(null);
              slot3.onPickupFromSlot(ep, itemstack2);
            }
          }
      }
      detectAndSendChanges();
    }
    return ret;
  }

  @Override
  protected boolean mergeItemStack(final ItemStack is, final int from, final int to,
      final boolean invert) {
    boolean changed = false;
    int k = invert ? to - 1 : from;
    if (is.isStackable())
      while (is.stackSize > 0 && (!invert && k < to || invert && k >= from)) {
        final Slot slot = (Slot) this.inventorySlots.get(k);
        final ItemStack slot_stack = slot.getStack();
        if (slot_stack != null && slot_stack.getItem() == is.getItem()
            && (!is.getHasSubtypes() || is.getItemDamage() == slot_stack.getItemDamage())
            && ItemStack.areItemStackTagsEqual(is, slot_stack)) {
          int total = slot_stack.stackSize + is.stackSize;
          if (!(slot instanceof SlotUnlimited) && total > slot_stack.getMaxStackSize())
            total = slot_stack.getMaxStackSize();
          if (total > slot.getSlotStackLimit())
            total = slot.getSlotStackLimit();
          if (total > slot_stack.stackSize) {
            is.stackSize = slot_stack.stackSize + is.stackSize - total;
            slot_stack.stackSize = total;
            slot.onSlotChanged();
            changed = true;
          }
        }
        if (invert)
          --k;
        else
          ++k;
      }
    k = invert ? to - 1 : from;
    while (is.stackSize > 0 && (!invert && k < to || invert && k >= from)) {
      final Slot slot = (Slot) this.inventorySlots.get(k);
      if (slot.getStack() == null) {
        final ItemStack slot_stack = is.copy();
        if (!(slot instanceof SlotUnlimited) && slot_stack.stackSize > slot_stack.getMaxStackSize())
          slot_stack.stackSize = slot_stack.getMaxStackSize();
        if (slot_stack.stackSize > slot.getSlotStackLimit())
          slot_stack.stackSize = slot.getSlotStackLimit();
        slot.putStack(slot_stack);
        slot.onSlotChanged();
        is.stackSize -= slot_stack.stackSize;
        changed = true;
      }
      if (invert)
        --k;
      else
        ++k;
    }
    return changed;
  }
}
