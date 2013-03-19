package org.yogpstop.qp;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockQuarry extends BlockContainer {
    Icon textureTop;
    Icon textureFront;

    public BlockQuarry(int i) {
        super(i, Material.iron);
        setHardness(1.5F);
        setResistance(10F);
        setStepSound(soundStoneFootstep);
        setCreativeTab(null);
    }

    @Override
    public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {
        return new ArrayList<ItemStack>();
    }

    @Override
    public int idPicked(World w, int x, int y, int z) {
        return QuarryPlus.blockQuarry.blockID;
    }

    @Override
    public Icon getBlockTextureFromSideAndMetadata(int i, int j) {
        if (j == 0 && i == 3)
            return textureFront;

        if (i == j)
            return textureFront;

        switch (i) {
        case 1:
            return textureTop;
        default:
            return field_94336_cN;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void func_94332_a(IconRegister par1IconRegister) {
        field_94336_cN = par1IconRegister.func_94245_a("yogpstop/quarryplus:quarry");
        textureTop = par1IconRegister.func_94245_a("yogpstop/quarryplus:quarry_top");
        textureFront = par1IconRegister.func_94245_a("yogpstop/quarryplus:quarry_front");
    }

    @Override
    public TileEntity createNewTileEntity(World w) {
        return new TileQuarry();
    }

    @Override
    public void onBlockPlacedBy(World w, int x, int y, int z, EntityLiving el, ItemStack stack) {
        super.onBlockPlacedBy(w, x, y, z, el, stack);
        ForgeDirection orientation = get2dOrientation(el.posX, el.posZ, x, z);
        w.setBlockMetadataWithNotify(x, y, z, orientation.getOpposite().ordinal(), 1);
        ((TileQuarry) w.getBlockTileEntity(x, y, z)).init(stack.getEnchantmentTagList());
    }

    private static ForgeDirection get2dOrientation(double x1, double z1, double x2, double z2) {
        double Dx = x1 - x2;
        double Dz = z1 - z2;
        double angle = Math.atan2(Dz, Dx) / Math.PI * 180 + 180;

        if (angle < 45 || angle > 315)
            return ForgeDirection.EAST;
        else if (angle < 135)
            return ForgeDirection.SOUTH;
        else if (angle < 225)
            return ForgeDirection.WEST;
        else
            return ForgeDirection.NORTH;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int par6, float par7, float par8, float par9) {
        if (ep.isSneaking())
            return false;
        ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdContainerQuarry, world, x, y, z);
        return true;
    }

}