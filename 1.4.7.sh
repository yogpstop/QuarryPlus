FILES=`find -type f`
simple_replace "^import +buildcraft\\.core\\.render\\.LiquidRenderer *; *\n" ""
simple_replace "LiquidRenderer\\." ""
simple_replace "^import +net\\.minecraft\\.client\\.renderer\\.texture\\..*; *\n" ""
simple_replace "^import +net\\.minecraft\\.util\\.Icon *; *\n" ""
simple_replace "^import +net\\.minecraftforge\\.common\\.FakePlayerFactory *; *\n" ""
simple_replace "([^a-zA-Z0-9_])Icon([^a-zA-Z0-9_])" "\\1int\\2"
simple_replace "FakePlayerFactory\\.getMinecraft" "CoreProxy.proxy.getBuildCraftPlayer"
simple_replace "OreDictionary\\.WILDCARD_VALUE" "QuarryPlus.WILDCARD_VALUE"
simple_replace "getLocalizedName" "translateBlockName"
simple_replace "itemIcon" "iconIndex"
simple_replace "blockIcon" "blockIndexInTexture"
simple_replace "buttonList" "controlList"
simple_replace "func_71880_c_" "f_"
simple_replace "Item\\.pickaxeIron" "Item\\.pickaxeSteel"
simple_replace "setUnlocalizedName" "setBlockName"
simple_replace "new +InventoryBasic *\\(([^,\\(\\)]+),[^,\\(\\)]+,([^,\\(\\)]+)\\)" "new InventoryBasic(\1,\2)"
simple_replace "setBlockToAir *\\(([^,\\(\\)]+,[^,\\(\\)]+,[^,\\(\\)]+)\\)" "setBlock\\(\\1, 0\\)"
method_declaration Icon int getIcon getBlockTextureFromSideAndMetadata int int int int
method_declaration void void onBlockPlacedBy onBlockPlacedBy World World int int int int int int EntityLiving EntityLiving ItemStack @SKIP@
method_declaration _ _ setBlockMetadataWithNotify setBlockMetadataWithNotify _ _ _ _ _ _ _ _ _ @SKIP@
method_declaration Icon int getBlockTexture getBlockTexture IBlockAccess IBlockAccess int int int int int int int int
method_declaration boolean boolean isBookEnchantable getIsRepairable ItemStack ItemStack ItemStack ItemStack
method_declaration String String getUnlocalizedName getItemNameIS ItemStack ItemStack
method_declaration boolean boolean allowAction allowActions IAction @SKIP@
method_declaration int int powerRequest powerRequest ForgeDirection @SKIP@
