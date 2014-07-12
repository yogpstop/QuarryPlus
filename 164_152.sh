#!/bin/bash
set -eu
. replace_helper.sh
cd src
rm -rf 152a
cp -r 164 152a
mv 152a/resources/assets 152a/resources/mods
cd 152a/java
FILES=`find -type f`
simple_replace "buildcraft\\.core\\.fluids\\.FluidUtils" "buildcraft.core.liquids.LiquidUtils"
simple_replace "RefineryRecipes" "RefineryRecipe"
simple_replace "RefineryRecipe\\.Recipe" "RefineryRecipe"
simple_replace "isFluidEqual" "isLiquidEqual"
simple_replace "manageFluids" "manageLiquids"
simple_replace "pipeFluids" "pipeLiquids"
simple_replace "FluidUtils" "LiquidUtils"
simple_replace "IFluidBlock" "ILiquid"
simple_replace "FluidContainerRegistry" "LiquidContainerRegistry"
simple_replace "IFluidHandler" "ITankContainer"
simple_replace "import +buildcraft\\.api\\.power\\.PowerHandler\\.Type *; *\n" ""
simple_replace "import +buildcraft\\.api\\.power\\.PowerHandler\\.PowerReceiver *; *\n" ""
simple_replace "PowerHandler" "IPowerProvider"
simple_replace "\\. *getPowerReceiver *\\( *\\) *" ""
simple_replace "FluidTankInfo" "LiquidTank"
simple_replace "(PowerHandler\\.|)PowerReceiver" "IPowerProvider"
method_declaration IPowerProvider IPowerProvider getIPowerProvider getPowerProvider ForgeDirection @SKIP@
simple_replace "getIPowerProvider" "getPowerProvider"
simple_replace "\\. *getPowerProvider *\\([^\\(\\),]+\\) *" ".getPowerProvider()"
method_declaration void void doWork doWork IPowerProvider @SKIP@
method_declaration "LiquidTank\\[\\]" "ILiquidTank\\[\\]" getTankInfo getTanks ForgeDirection ForgeDirection
simple_replace getTankInfo getTanks
simple_replace "net\\.minecraft\\.client\\.resources\\.I18n" "net.minecraft.util.StatCollector"
simple_replace "I18n\\.getString" "StatCollector.translateToLocal"
simple_replace "FluidRenderer\\.setColorForFluidStack *\\([^\\(\\),]+\\) *;? *" ""
simple_replace "net\\.minecraftforge\\.fluids" "net\\.minecraftforge\\.liquids"
simple_replace "FluidRenderer" "LiquidRenderer"
simple_replace "getFluidDisplayLists" "getLiquidDisplayLists"
simple_replace "getFluidSheet" "getLiquidSheet"
simple_replace "FluidStack" "LiquidStack"
simple_replace "FluidRegistry" "LiquidDictionary"
simple_replace "getRegisteredFluidIDs" "getLiquids"
simple_replace "EntityLivingBase" "EntityLiving"
simple_replace "onContainerClosed" "onCraftGuiClosed"
simple_replace "Item\\.glowstone" "Item.lightStoneDust"
simple_replace "isItemValidForSlot" "isStackValidForSlot"
simple_replace "getTextureManager\\( *\\)" "renderEngine"
simple_replace "(texture\\.|)TextureManager" "RenderEngine"
simple_replace "import +net\\.minecraft\\.util\\.ResourceLocation *; *\n" ""
simple_replace "ResourceLocation" "String"
cd ../..
rm -rf 152
cp -r 152a 152
cd 152
cat ../../164_152.patch | patch -p1
cd ../..
echo -e "[pj]\nsrc = src/152/java/\nversion = 1.5.2-`cat VERSION`\nres = src/152/resources/\napi = BuildCraft372\nmcv = 1.5.2-7.8.1.738" >build.cfg
