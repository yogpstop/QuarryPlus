package com.yogpc.ip;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMirror extends Item {
  public ItemMirror() {
    super();
    setUnlocalizedName("magicmirror");
    setTextureName("magicmirror");
    setCreativeTab(CreativeTabs.tabTools);
  }

  @Override
  public ItemStack onEaten(final ItemStack i, final World w, final EntityPlayer p) {
    if (p instanceof EntityPlayerMP) {
      if (i.getItemDamage() != 0) {
        if (p.dimension != 0)
          p.travelToDimension(0);
      } else if (!p.worldObj.provider.canRespawnHere())
        p.travelToDimension(p.worldObj.provider.getRespawnDimension((EntityPlayerMP) p));
      ChunkCoordinates c = p.getBedLocation(p.dimension);
      if (c != null)
        c = EntityPlayer.verifyRespawnCoordinates(p.worldObj, c, p.isSpawnForced(p.dimension));
      else
        c = p.worldObj.provider.getRandomizedSpawnPoint();
      p.setPositionAndUpdate(c.posX + 0.5D, c.posY + 0.1D, c.posZ + 0.5D);
    }
    return i;
  }

  @Override
  public int getMaxItemUseDuration(final ItemStack i) {
    return 100;
  }

  @Override
  public EnumAction getItemUseAction(final ItemStack i) {
    return EnumAction.block;
  }

  @Override
  public ItemStack onItemRightClick(final ItemStack i, final World w, final EntityPlayer p) {
    p.setItemInUse(i, getMaxItemUseDuration(i));
    return i;
  }

  @Override
  public String getUnlocalizedName(final ItemStack is) {
    switch (is.getItemDamage()) {
      case 1:
        return "item.dimensionmirror";
    }
    return "item.magicmirror";
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubItems(final Item i, final CreativeTabs par2CreativeTabs, final List par3List) {
    par3List.add(new ItemStack(i, 1, 0));
    par3List.add(new ItemStack(i, 1, 1));
  }
}
