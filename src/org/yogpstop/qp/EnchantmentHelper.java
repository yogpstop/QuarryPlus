/*
 * Copyright (C) 2012,2013 yogpstop
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the
 * GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.yogpstop.qp;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;

public final class EnchantmentHelper {
	static final void init(IEnchantableTile te, NBTTagList nbttl) {
		byte efficiency = 0, unbreaking = 0, fortune = 0;
		boolean silktouch = false;
		if (nbttl != null) for (int i = 0; i < nbttl.tagCount(); i++) {
			short id = ((NBTTagCompound) nbttl.tagAt(i)).getShort("id");
			short lvl = ((NBTTagCompound) nbttl.tagAt(i)).getShort("lvl");
			if (id == 32) efficiency = (byte) lvl;
			if (id == 33) silktouch = true;
			if (id == 34) unbreaking = (byte) lvl;
			if (id == 35) fortune = (byte) lvl;
		}
		te.set(efficiency, fortune, unbreaking, silktouch);
		te.G_reinit();
	}

	public static Collection<String> getEnchantmentsChat(IEnchantableTile te) {
		ArrayList<String> als = new ArrayList<String>();
		if (te.getEfficiency() <= 0 && !te.getSilktouch() && te.getUnbreaking() <= 0 && te.getFortune() <= 0) als.add(StatCollector
				.translateToLocal("chat.plusenchantno"));
		else als.add(StatCollector.translateToLocal("chat.plusenchant"));
		if (te.getEfficiency() > 0) als.add(new StringBuilder().append("    ").append(Enchantment.enchantmentsList[32].getTranslatedName(te.getEfficiency()))
				.toString());
		if (te.getSilktouch()) als.add(new StringBuilder().append("    ").append(Enchantment.enchantmentsList[33].getTranslatedName(1)).toString());
		if (te.getUnbreaking() > 0) als.add(new StringBuilder().append("    ").append(Enchantment.enchantmentsList[34].getTranslatedName(te.getUnbreaking()))
				.toString());
		if (te.getFortune() > 0) als.add(new StringBuilder().append("    ").append(Enchantment.enchantmentsList[35].getTranslatedName(te.getFortune()))
				.toString());
		return als;
	}

	static void enchantmentToIS(IEnchantableTile te, ItemStack is) {
		if (te.getEfficiency() > 0) is.addEnchantment(Enchantment.enchantmentsList[32], te.getEfficiency());
		if (te.getSilktouch()) is.addEnchantment(Enchantment.enchantmentsList[33], 1);
		if (te.getUnbreaking() > 0) is.addEnchantment(Enchantment.enchantmentsList[34], te.getUnbreaking());
		if (te.getFortune() > 0) is.addEnchantment(Enchantment.enchantmentsList[35], te.getFortune());
	}
}
