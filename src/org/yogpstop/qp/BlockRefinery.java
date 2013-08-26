package org.yogpstop.qp;

import static buildcraft.core.CreativeTabBuildCraft.tabBuildCraft;

import java.util.ArrayList;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class BlockRefinery extends BlockContainer {

	public BlockRefinery(int par1) {
		super(par1, Material.iron);
		setHardness(5F);
		setCreativeTab(tabBuildCraft);
		setUnlocalizedName("RefineryPlus");
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileRefinery();
	}

	private final ArrayList<ItemStack> drop = new ArrayList<ItemStack>();

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
		this.drop.clear();
		TileRefinery tp = (TileRefinery) world.getBlockTileEntity(x, y, z);
		if (world.isRemote || tp == null) return;
		int count = quantityDropped(meta, 0, world.rand);
		int id1 = idDropped(meta, world.rand, 0);
		if (id1 > 0) {
			for (int i = 0; i < count; i++) {
				ItemStack is = new ItemStack(id1, 1, damageDropped(meta));
				tp.S_setEnchantment(is);
				this.drop.add(is);
			}
		}
		super.breakBlock(world, x, y, z, id, meta);
	}

	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {
		return this.drop;
	}

	@Override
	public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase el, ItemStack stack) {
		super.onBlockPlacedBy(w, x, y, z, el, stack);
		((TileRefinery) w.getBlockTileEntity(x, y, z)).G_init(stack.getEnchantmentTagList());
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int side, float par7, float par8, float par9) {
		Item equipped = ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof ItemTool) {
			if (ep.getCurrentEquippedItem().getItemDamage() == 0) {
				if (world.isRemote) return true;
				PacketDispatcher.sendPacketToPlayer(new Packet3Chat(ChatMessageComponent.func_111066_d(StatCollector.translateToLocal("chat.plusenchant"))),
						(Player) ep);
				for (String s : ((TileRefinery) world.getBlockTileEntity(x, y, z)).C_getEnchantments())
					PacketDispatcher.sendPacketToPlayer(new Packet3Chat(ChatMessageComponent.func_111066_d(s)), (Player) ep);
				return true;
			}
		}
		return false;
	}
}
