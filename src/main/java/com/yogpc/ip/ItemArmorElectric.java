package com.yogpc.ip;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.util.EnumHelper;
import cofh.api.energy.IEnergyContainerItem;

import com.yogpc.mc_lib.ProxyCommon;
import com.yogpc.mc_lib.ReflectionHelper;
import com.yogpc.mc_lib.YogpstopLib;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemArmorElectric extends ItemArmor implements ISpecialArmor, IElectricItem,
    IEnergyContainerItem {
  public ItemArmorElectric() {
    super(EnumHelper.addArmorMaterial("ELECTRIC", 33, new int[] {3, 8, 6, 3}, 0), YogpstopLib.proxy
        .addNewArmourRendererPrefix("electric"), 1);
    setUnlocalizedName("electric_armor");
    setCreativeTab(CreativeTabs.tabCombat);
    setTextureName("yogpstop_qp:elecArmor");
    setMaxDamage(100);
    setMaxStackSize(1);
  }

  @Override
  public String getArmorTexture(final ItemStack i, final Entity e, final int s, final String t) {
    return "yogpstop_qp:textures/models/armor/elecArmor_layer_1.png";
  }

  private static final Field f = ReflectionHelper.getField(NetHandlerPlayServer.class,
      "field_147365_f", "floatingTickCount");

  private static final double maxFallSpeedOnHover = 0.35;

  private void useJetpack(final EntityPlayer p, final ItemStack jetpack, final boolean hover) {
    final boolean jumping = YogpstopLib.proxy.getKey(p, ProxyCommon.Key.jump);
    if (jumping || hover && p.motionY < -maxFallSpeedOnHover) {
      final double charge = ElectricItemManager.getCharge(jetpack);
      if (charge <= 0.0)
        return;
      float power = 0.7f;
      if (charge / getMaxCharge(jetpack) <= 0.05f)
        power *= charge / getMaxCharge(jetpack) * 0.05;
      if (YogpstopLib.proxy.getKey(p, ProxyCommon.Key.forward) && power > 0.0f)
        p.moveFlying(0.0f, 0.4f * power * (hover ? 0.65f : 0.3f) * 2.0f, 0.02f);
      {// set motionY
        final int maxFlightHeight = (int) (p.worldObj.getHeight() / 1.28f);
        double y = p.posY;
        if (y > maxFlightHeight) {
          if (y > maxFlightHeight)
            y = maxFlightHeight;
          power *= (maxFlightHeight - y) / 25.0;
        }
        final double prevmotion = p.motionY;
        p.motionY = Math.min(p.motionY + power * 0.2f, 0.6f);
        if (hover) {
          final float maxHoverY = jumping ? 0.1f : -0.1f;
          if (p.motionY > maxHoverY) {
            p.motionY = maxHoverY;
            if (prevmotion > p.motionY)
              p.motionY = prevmotion;
          }
        }
      }
      ElectricItemManager.discharge(jetpack, hover ? 7 : 8, getMaxCharge(jetpack));
      {
        p.fallDistance = 0.0f;
        p.distanceWalkedModified = 0.0f;
        if (p instanceof EntityPlayerMP)
          try {
            f.setInt(((EntityPlayerMP) p).playerNetServerHandler, 0);
          } catch (final IllegalArgumentException e) {
            e.printStackTrace();
          } catch (final IllegalAccessException e) {
            e.printStackTrace();
          }
      }
      p.inventoryContainer.detectAndSendChanges();
    }
  }

  private static boolean toggleHover(final EntityPlayer p, final ItemStack itemStack) {
    NBTTagCompound nbtData = itemStack.getTagCompound();
    if (nbtData == null)
      itemStack.setTagCompound(nbtData = new NBTTagCompound());
    boolean hoverMode = nbtData.getBoolean("hoverMode");
    byte toggleTimer = nbtData.getByte("toggleTimer");
    if (YogpstopLib.proxy.getKey(p, ProxyCommon.Key.jump)
        && YogpstopLib.proxy.getKey(p, ProxyCommon.Key.mode) && toggleTimer == 0) {
      toggleTimer = 10;
      hoverMode = !hoverMode;
      if (!FMLCommonHandler.instance().getEffectiveSide().isClient()) {
        nbtData.setBoolean("hoverMode", hoverMode);
        if (hoverMode)
          p.addChatMessage(new ChatComponentText("Hover Mode enabled."));
        else
          p.addChatMessage(new ChatComponentText("Hover Mode disabled."));
      }
    }
    if (!FMLCommonHandler.instance().getEffectiveSide().isClient() && toggleTimer > 0) {
      toggleTimer = (byte) (toggleTimer - 1);
      nbtData.setByte("toggleTimer", toggleTimer);
    }
    return hoverMode;
  }

  @Override
  public void onArmorTick(final World world, final EntityPlayer player, final ItemStack is) {
    if (ElectricItem.manager != null)
      ElectricItemManager.charge(is, ElectricItem.manager.discharge(is, Double.MAX_VALUE,
          Integer.MAX_VALUE, true, false, false), getMaxCharge(is));
    if (player.inventory.armorInventory[2] != is)
      return;
    useJetpack(player, is, toggleHover(player, is));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public EnumRarity getRarity(final ItemStack stack) {
    return EnumRarity.uncommon;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubItems(final Item item, final CreativeTabs par2CreativeTabs, final List itemList) {
    final ItemStack charged = new ItemStack(this, 1);
    ElectricItemManager.charge(charged, Double.POSITIVE_INFINITY, getMaxCharge(charged));
    itemList.add(charged);
    itemList.add(new ItemStack(this, 1, getMaxDamage()));
  }

  @Override
  public ISpecialArmor.ArmorProperties getProperties(final EntityLivingBase player,
      final ItemStack armor, final DamageSource source, final double damage, final int slot) {
    if (source.isUnblockable())
      return new ISpecialArmor.ArmorProperties(0, 0.0, 0);
    final int damageLimit =
        (int) Math.min(Integer.MAX_VALUE, 25.0 * ElectricItemManager.getCharge(armor) / 5000);
    return new ISpecialArmor.ArmorProperties(0, 0.4 * 0.9, damageLimit);
  }

  @Override
  public int getArmorDisplay(final EntityPlayer player, final ItemStack armor, final int slot) {
    if (ElectricItemManager.getCharge(armor) >= 5000)
      return (int) Math.round(20.0 * 0.4 * 0.9);
    return 0;
  }

  @Override
  public void damageArmor(final EntityLivingBase entity, final ItemStack is,
      final DamageSource source, final int damage, final int slot) {
    ElectricItemManager.discharge(is, damage * 5000, getMaxCharge(is));
  }

  @Override
  public boolean canProvideEnergy(final ItemStack itemStack) {
    return true;
  }

  @Override
  public Item getChargedItem(final ItemStack itemStack) {
    return this;
  }

  @Override
  public Item getEmptyItem(final ItemStack itemStack) {
    return this;
  }

  @Override
  public double getMaxCharge(final ItemStack itemStack) {
    return 20000000;
  }

  @Override
  public int getTier(final ItemStack itemStack) {
    return 4;
  }

  @Override
  public double getTransferLimit(final ItemStack itemStack) {
    return 2500;
  }

  @Override
  public int extractEnergy(final ItemStack is, final int am, final boolean sim) {
    return 0;
  }

  @Override
  public int getEnergyStored(final ItemStack is) {
    return (int) (ElectricItemManager.getCharge(is) * 4);
  }

  @Override
  public int getMaxEnergyStored(final ItemStack is) {
    return (int) (getMaxCharge(is) * 4);
  }

  @Override
  public int receiveEnergy(final ItemStack is, final int am, final boolean sim) {
    return (int) (ElectricItemManager.charge(is, Math.min((double) am / 4, getTransferLimit(is)),
        getMaxCharge(is)) * 4);
  }
}
