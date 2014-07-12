#!/bin/bash
set -eu
. replace_helper.sh
cd src
rm -rf 164a
cp -r main 164a
cd 164a/java
rm com/yogpc/qp/QuarryPlusPacketCodec.java
FILES=`find -type f`
simple_replace "net\\.minecraftforge\\.common\\.util" "net.minecraftforge.common"
simple_replace "net\\.minecraftforge\\.common\\.config" "net.minecraftforge.common"
simple_replace "net\\.minecraft\\.network\\.Packet" "net\\.minecraft\\.network\\.packet\\.Packet"
simple_replace "new ChatComponentText" ""
simple_replace "getEntityId\(\)" "entityId"
simple_replace "import +net\\.minecraft\\.util\\.ChatComponentText *; *\n" ""
simple_replace "import +cpw\\.mods\\.fml\\.common\\.network\\.internal\\.FMLProxyPacket *; *\n" ""
simple_replace "import +io\\.netty\\.[^;]+; *\n" ""
simple_replace "net\\.minecraft\\.init\\.Blocks" "net.minecraft.block.Block"
simple_replace "net\\.minecraft\\.init\\.Items" "net.minecraft.item.Item"
simple_replace "cpw\\.mods\\.fml\\.common\\.eventhandler\\.SubscribeEvent" "net.minecraftforge.event.ForgeSubscribe"
simple_replace "SubscribeEvent" "ForgeSubscribe"
simple_replace "IIcon" "Icon"
simple_replace "setBlockName" "setUnlocalizedName"
simple_replace "soundTypeStone" "soundStoneFootstep"
simple_replace "getTileEntity" "getBlockTileEntity"
simple_replace "registerBlockIcons" "registerIcons"
simple_replace "getDrops" "getBlockDropped"
simple_replace "Item +([^ ]*) *= *getItemDropped" "int \\1 = idDropped"
simple_replace "getItemDropped" "idDropped"
simple_replace "([a-zA-Z\\. ]+)getCompoundTagAt(\\([^\\(\\)]+\\))" "((NBTTagCompound)\1tagAt\2)"
simple_replace "([a-zA-Z\\. ]+)getStringTagAt(\\([^\\(\\)]+\\))" "(((NBTTagString)\1tagAt\2).data)"
simple_replace "getTagList *\\(([^\\(\\),]+),[^\\(\\),]+\\)" "getTagList(\1)"
simple_replace "isAir([^B])" "isAirBlock\1"
simple_replace "markDirty" "onInventoryChanged"
simple_replace "fontRendererObj" "fontRenderer"
simple_replace "getInventoryName" "getInvName"
simple_replace "hasCustomInventoryName" "isInvNameLocalized"
simple_replace "removedByPlayer" "removeBlockByPlayer"
simple_replace "setLightLevel" "setLightValue"
simple_replace "I18n\\.format" "I18n.getString"
simple_replace "NetworkRegistry\\.INSTANCE" "NetworkRegistry.instance()"
simple_replace "buildcraft\\.core\\.recipes\\.RefineryRecipeManager" "buildcraft.api.recipes.RefineryRecipes"
simple_replace "RefineryRecipeManager\\.RefineryRecipe" "RefineryRecipes.Recipe"
simple_replace "RefineryRecipeManager\\.INSTANCE\\." "RefineryRecipes."
simple_replace "RefineryRecipeManager" "RefineryRecipes"
simple_replace "Blocks\\.flowing_water" "Block.waterMoving"
simple_replace "Blocks\\.water" "Block.waterStill"
simple_replace "Blocks\\.flowing_lava" "Block.lavaMoving"
simple_replace "Blocks\\.lava" "Block.lavaStill"
simple_replace "Blocks\\.diamond_block" "Block.blockDiamond"
simple_replace "Blocks\\.gold_block" "Block.blockGold"
simple_replace "Items\\.diamond_pickaxe" "Item.pickaxeDiamond"
simple_replace "Items\\.iron_pickaxe" "Item.pickaxeIron"
simple_replace "Items\\.redstone" "Item.redstone"
simple_replace "Items\\.sign" "Item.sign"
simple_replace "Items\\.paper" "Item.paper"
simple_replace "Items\\.book" "Item.book"
simple_replace "Items\\.writable_book" "Item.writableBook"
simple_replace "Blocks\\.anvil" "Block.anvil"
simple_replace "Blocks\\.glass" "Block.glass"
simple_replace "Blocks\\.portal" "Block.portal"
simple_replace "Blocks\\.chest" "Block.chest"
simple_replace "Blocks\\.obsidian" "Block.obsidian"
simple_replace "Blocks\\.dispenser" "Block.dispenser"
simple_replace "Items\\.bucket" "Item.bucketEmpty"
simple_replace "Items\\.water_bucket" "Item.bucketWater"
simple_replace "Items\\.lava_bucket" "Item.bucketLava"
simple_replace "Items\\.glowstone_dust" "Item.glowstone"
simple_replace "Blocks\\.enchanting_table" "Block.enchantmentTable"
simple_replace "field_147501_a" "tileEntityRenderer"
simple_replace "field_147553_e" "renderEngine"
method_declaration boolean boolean shouldRender3DInInventory shouldRender3DInInventory int @SKIP@
method_declaration void void elementClicked elementClicked int int boolean boolean int @SKIP@ int @SKIP@
method_declaration void void drawSlot drawSlot int int int int int int int int Tessellator Tessellator int @SKIP@ int @SKIP@
method_declaration void void openInventory openChest
method_declaration void void closeInventory closeChest
method_declaration void void getSubItems getSubItems Item int CreativeTabs CreativeTabs List List
method_declaration TileEntity TileEntity createNewTileEntity createNewTileEntity World World int @SKIP@
method_declaration void void onNeighborBlockChange onNeighborBlockChange World World int int int int int int Block int
method_declaration void void breakBlock breakBlock World World int int int int int int Block int int int
method_declaration Icon Icon getIcon getBlockTexture IBlockAccess IBlockAccess int int int int int int int int
method_declaration boolean boolean isSideSolid isBlockSolidOnSide IBlockAccess World int int int int int int ForgeDirection ForgeDirection
simple_replace "isSideSolid" "isBlockSolidOnSide"
simple_replace "func_147453_f" "func_96440_m"
simple_replace "getIcon *(\\([^,\\)]+,[^,\\)]+,[^,\\)]+,[^,\\)]+,[^,\\)]+\\))" "getBlockTexture\1"
cd ../..
rm -rf 164
cp -r 164a 164
cd 164
cat ../../172_164.patch | patch -p1
cd ../..
echo -e "[pj]\nsrc = src/164/java/\nversion = 1.6.4-`cat VERSION`\nres = src/164/resources/\napi = BuildCraft422\nmcv = 1.6.4-9.11.1.965" >build.cfg
