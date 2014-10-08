package com.yogpc.ip;

import java.lang.reflect.Modifier;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockController extends Block {
  private int cur = Integer.MIN_VALUE;
  private static final LinkedList<String> list = new LinkedList<String>();

  public BlockController() {
    super(Material.circuits);
    setBlockName("spawnercontroller");
    setBlockTextureName("spawnercontroller");
    setHardness(1.0f);
    setCreativeTab(CreativeTabs.tabRedstone);
  }

  private static final MobSpawnerBaseLogic getSpawner(final World w, final int x, final int y,
      final int z) {
    for (final ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
      final TileEntity t = w.getTileEntity(x + d.offsetX, y + d.offsetY, z + d.offsetZ);
      if (!(t instanceof TileEntityMobSpawner))
        continue;
      final MobSpawnerBaseLogic l = ((TileEntityMobSpawner) t).func_145881_a();
      if (l == null)
        continue;
      return l;
    }
    return null;
  }

  @Override
  public void onBlockClicked(final World w, final int x, final int y, final int z,
      final EntityPlayer e) {
    if (w.isRemote)
      return;
    final MobSpawnerBaseLogic l = getSpawner(w, x, y, z);
    if (l == null)
      return;
    if (this.cur == Integer.MIN_VALUE || e.isSneaking()) {
      list.clear();
      list.addAll(EntityList.stringToClassMapping.keySet());
      this.cur = list.indexOf(l.getEntityNameToSpawn());
      e.addChatMessage(new ChatComponentText("Initialize Spawner Controller"));
      e.addChatMessage(new ChatComponentText(list.get(this.cur)));
    } else {
      l.setEntityName(list.get(this.cur));
      e.addChatMessage(new ChatComponentText("Spawner's mob is set"));
    }
  }

  @Override
  public boolean onBlockActivated(final World w, final int x, final int y, final int z,
      final EntityPlayer e, final int s, final float hx, final float hy, final float hz) {
    if (!w.isRemote)
      if (this.cur == Integer.MIN_VALUE)
        onBlockClicked(w, x, y, z, e);
      else {
        do {
          this.cur++;
          if (this.cur >= list.size())
            this.cur = 0;
        } while (Modifier.isAbstract(((Class<?>) EntityList.stringToClassMapping.get(list
            .get(this.cur))).getModifiers()));
        e.addChatMessage(new ChatComponentText(list.get(this.cur)));
      }
    return true;
  }

  @Override
  public void onNeighborBlockChange(final World w, final int x, final int y, final int z,
      final Block b) {
    if (w.isRemote)
      return;
    final boolean r =
        w.isBlockIndirectlyGettingPowered(x, y, z)
            || w.isBlockIndirectlyGettingPowered(x, y + 1, z);
    final int m = w.getBlockMetadata(x, y, z);
    if (r && m == 0) {
      final MobSpawnerBaseLogic l = getSpawner(w, x, y, z);
      if (l != null) {
        l.spawnDelay = 0;
        final EntityPlayer p = FakePlayerFactory.getMinecraft((WorldServer) w);
        p.setWorld(l.getSpawnerWorld());
        p.setPosition(l.getSpawnerX(), l.getSpawnerY(), l.getSpawnerZ());
        l.getSpawnerWorld().playerEntities.add(p);
        l.updateSpawner();
        l.getSpawnerWorld().playerEntities.remove(p);
      }
      w.setBlockMetadataWithNotify(x, y, z, 1, 4);
    } else if (!r && m == 1)
      w.setBlockMetadataWithNotify(x, y, z, 0, 4);
  }

}
