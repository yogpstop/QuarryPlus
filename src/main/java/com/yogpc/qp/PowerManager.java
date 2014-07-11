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

package com.yogpc.qp;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class PowerManager {
	static double B_CF, B_CS, W_CF, W_CS;
	private static double B_BP, B_CE, B_CU, B_XR, B_MS;// Quarry:BreakBlock
	private static double F_BP, F_CE, F_CU, F_XR, F_MS;// Quarry:MakeFrame
	private static double W_BP, W_CE, W_CU, W_XR, W_MS;// MiningWell
	private static double L_BP, L_CE, L_CU, L_XR, L_MS, L_CF, L_CS;// Laser
	private static double R_CE, R_CU, R_XR, R_MS;// Refinery
	private static double PF_BP, PF_CU;// Pump:Frame
	private static double PL_BP, PL_CU;// Pump:Liquid
	private static double H_BP, H_CU;// Quarry:MoveHead

	private static double get(ConfigCategory c, String name, double def) {
		if (c.containsKey(name)) {
			Property prop = c.get(name);
			if (prop.getType() == null) {
				prop = new Property(prop.getName(), prop.getString(), Property.Type.DOUBLE);
				c.put(name, prop);
			}
			return prop.getDouble(def);
		}
		Property prop = new Property(name, Double.toString(def), Property.Type.DOUBLE);
		c.put(name, prop);
		return prop.getDouble(def);
	}

	static void loadConfiguration(Configuration cg) throws RuntimeException {
		String cn = new StringBuilder().append(Configuration.CATEGORY_GENERAL).append(Configuration.CATEGORY_SPLITTER).append("PowerSetting")
				.append(Configuration.CATEGORY_SPLITTER).toString();

		String cn2 = new StringBuilder().append(cn).append("Quarry").append(Configuration.CATEGORY_SPLITTER).toString();
		ConfigCategory c = cg.getCategory(new StringBuilder().append(cn2).append("BreakBlock").toString());
		B_BP = get(c, "BasePower", 40);
		B_CE = get(c, "EfficiencyCoefficient", 1.3);
		B_CU = get(c, "UnbreakingCoefficient", 1);
		B_CF = get(c, "FortuneCoefficient", 1.3);
		B_CS = get(c, "SilktouchCoefficient", 2);
		B_XR = get(c, "BaseMaxRecieve", 300);
		B_MS = get(c, "BaseMaxStored", 15000);

		c = cg.getCategory(new StringBuilder().append(cn2).append("BreakBlock").append(Configuration.CATEGORY_SPLITTER).append("MoveHead").toString());
		H_BP = get(c, "BasePower", 200);
		H_CU = get(c, "UnbreakingCoefficient", 1);

		c = cg.getCategory(new StringBuilder().append(cn2).append("MakeFrame").toString());
		F_BP = get(c, "BasePower", 25);
		F_CE = get(c, "EfficiencyCoefficient", 1.3);
		F_CU = get(c, "UnbreakingCoefficient", 1);
		F_XR = get(c, "BaseMaxRecieve", 100);
		F_MS = get(c, "BaseMaxStored", 15000);

		cn2 = new StringBuilder().append(cn).append("Pump").append(Configuration.CATEGORY_SPLITTER).toString();
		c = cg.getCategory(new StringBuilder().append(cn2).append("DrainLiquid").toString());
		PL_BP = get(c, "BasePower", 10);
		PL_CU = get(c, "UnbreakingCoefficient", 1);

		c = cg.getCategory(new StringBuilder().append(cn2).append("MakeFrame").toString());
		PF_BP = get(c, "BasePower", 25);
		PF_CU = get(c, "UnbreakingCoefficient", 1);

		c = cg.getCategory(new StringBuilder().append(cn).append("MiningWell").toString());
		W_BP = get(c, "BasePower", 40);
		W_CE = get(c, "EfficiencyCoefficient", 1.3);
		W_CU = get(c, "UnbreakingCoefficient", 1);
		W_CF = get(c, "FortuneCoefficient", 1.3);
		W_CS = get(c, "SilktouchCoefficient", 2);
		W_XR = get(c, "BaseMaxRecieve", 100);
		W_MS = get(c, "BaseMaxStored", 1000);

		c = cg.getCategory(new StringBuilder().append(cn).append("Laser").toString());
		L_BP = get(c, "BasePower", 4);
		L_CE = get(c, "EfficiencyCoefficient", 2);
		L_CU = get(c, "UnbreakingCoefficient", 0.1);
		L_CF = get(c, "FortuneCoefficient", 1.05);
		L_CS = get(c, "SilktouchCoefficient", 1.1);
		L_XR = get(c, "BaseMaxRecieve", 100);
		L_MS = get(c, "BaseMaxStored", 1000);

		c = cg.getCategory(new StringBuilder().append(cn).append("Refinery").toString());
		R_CE = get(c, "EfficiencyCoefficient", 1.3);
		R_CU = get(c, "UnbreakingCoefficient", 1);
		R_XR = get(c, "BaseMaxRecieve", 100);
		R_MS = get(c, "BaseMaxStored", 1000);

		StringBuilder sb = new StringBuilder();

		if (B_BP < 0) sb.append("general.PowerSetting.Quarry.BreakBlock.BasePower value is bad.\n");
		if (B_CE < 0) sb.append("general.PowerSetting.Quarry.BreakBlock.EfficiencyCoefficient value is bad.\n");
		if (B_CU < 0) sb.append("general.PowerSetting.Quarry.BreakBlock.UnbreakingCoefficient value is bad.\n");
		if (B_CF < 0) sb.append("general.PowerSetting.Quarry.BreakBlock.FortuneCoefficient value is bad.\n");
		if (B_CS < 0) sb.append("general.PowerSetting.Quarry.BreakBlock.SilktouchCoefficient value is bad.\n");
		if (B_MS <= 0) sb.append("general.PowerSetting.Quarry.BreakBlock.BaseMaxStored value is bad.\n");

		if (H_BP < 0) sb.append("general.PowerSetting.Quarry.BreakBlock.MoveHead.BasePower value is bad.\n");
		if (H_CU < 0) sb.append("general.PowerSetting.Quarry.BreakBlock.MoveHead.UnbreakingCoefficient value is bad.\n");

		if (F_BP < 0) sb.append("general.PowerSetting.Quarry.MakeFrame.BasePower value is bad.\n");
		if (F_CE < 0) sb.append("general.PowerSetting.Quarry.MakeFrame.EfficiencyCoefficient value is bad.\n");
		if (F_CU < 0) sb.append("general.PowerSetting.Quarry.MakeFrame.UnbreakingCoefficient value is bad.\n");
		if (F_MS <= 0) sb.append("general.PowerSetting.Quarry.MakeFrame.BaseMaxStored value is bad.\n");

		if (PL_BP < 0) sb.append("general.PowerSetting.Pump.DrainLiquid.BasePower value is bad.\n");
		if (PL_CU < 0) sb.append("general.PowerSetting.Pump.DrainLiquid.UnbreakingCoefficient value is bad.\n");

		if (PF_BP < 0) sb.append("general.PowerSetting.Pump.MakeFrame.BasePower value is bad.\n");
		if (PF_CU < 0) sb.append("general.PowerSetting.Pump.MakeFrame.UnbreakingCoefficient value is bad.\n");

		if (W_BP < 0) sb.append("general.PowerSetting.MiningWell.BasePower value is bad.\n");
		if (W_CE < 0) sb.append("general.PowerSetting.MiningWell.EfficiencyCoefficient value is bad.\n");
		if (W_CU < 0) sb.append("general.PowerSetting.MiningWell.UnbreakingCoefficient value is bad.\n");
		if (W_CF < 0) sb.append("general.PowerSetting.MiningWell.FortuneCoefficient value is bad.\n");
		if (W_CS < 0) sb.append("general.PowerSetting.MiningWell.SilktouchCoefficient value is bad.\n");
		if (W_MS <= 0) sb.append("general.PowerSetting.MiningWell.BaseMaxStored value is bad.\n");

		if (L_BP < 0) sb.append("general.PowerSetting.Laser.BasePower value is bad.\n");
		if (L_CE < 0) sb.append("general.PowerSetting.Laser.EfficiencyCoefficient value is bad.\n");
		if (L_CU < 0) sb.append("general.PowerSetting.Laser.UnbreakingCoefficient value is bad.\n");
		if (L_CF < 0) sb.append("general.PowerSetting.Laser.FortuneCoefficient value is bad.\n");
		if (L_CS < 0) sb.append("general.PowerSetting.Laser.SilktouchCoefficient value is bad.\n");
		if (L_MS <= 0) sb.append("general.PowerSetting.Laser.BaseMaxStored value is bad.\n");

		if (R_CE < 0) sb.append("general.PowerSetting.Refinery.EfficiencyCoefficient value is bad.\n");
		if (R_CU < 0) sb.append("general.PowerSetting.Refinery.UnbreakingCoefficient value is bad.\n");
		if (R_MS <= 0) sb.append("general.PowerSetting.Refinery.BaseMaxStored value is bad.\n");

		if (sb.length() != 0) throw new RuntimeException(sb.toString());
	}

	static void configure0(APowerTile pp) {
		pp.configure(0, pp.getMaxStored());
	}

	private static void configure(APowerTile pp, double CE, byte E, byte U, double CU, double XR, double MS, byte pump) {
		pp.configure(XR * Math.pow(CE, E) / (U * CU + 1), MS * Math.pow(CE, E) / (U * CU + 1)
				+ (pump > 0 ? ((65536 * PL_BP / ((pump * PL_CU + 1))) + (1020 * PF_BP / ((pump * PF_CU + 1)))) : 0));
	}

	private static void configure15(APowerTile pp, double CE, byte E, byte U, double CU, double XR, double MS, byte pump) {
		pp.configure(XR * Math.pow(CE, E) / (U * CU + 1), MS * Math.pow(CE, E) / (U * CU + 1)
				+ (pump > 0 ? ((65536 * PL_BP / ((pump * PL_CU + 1))) + (1020 * PF_BP / ((pump * PF_CU + 1)))) : 0));
	}

	static void configureB(APowerTile pp, byte E, byte U, byte pump) {
		configure15(pp, B_CE, E, U, B_CU, B_XR, B_MS, pump);
	}

	static void configureW(APowerTile pp, byte E, byte U, byte pump) {
		configure15(pp, W_CE, E, U, W_CU, W_XR, W_MS, pump);
	}

	static void configureL(APowerTile pp, byte E, byte U) {
		configure(pp, L_CE, E, U, L_CU, L_XR, L_MS, (byte) 0);
	}

	static void configureF(APowerTile pp, byte E, byte U, byte pump) {
		configure(pp, F_CE, E, U, F_CU, F_XR, F_MS, pump);
	}

	static void configureR(APowerTile pp, byte E, byte U) {
		configure(pp, R_CE, E, U, R_CU, R_XR, R_MS, (byte) 0);
	}

	private static boolean useEnergy(APowerTile pp, double BP, float H, double CSP, byte U, double CU) {
		double pw = BP * H * CSP / (U * CU + 1);
		if (pp.useEnergy(pw, pw, false) != pw) return false;
		pp.useEnergy(pw, pw, true);
		return true;
	}

	private static boolean useEnergy(APowerTile pp, double BP, byte U, double CU) {
		double pw = BP / (U * CU + 1);
		if (pp.useEnergy(pw, pw, false) != pw) return false;
		pp.useEnergy(pw, pw, true);
		return true;
	}

	private static boolean useEnergy(APowerTile pp, double BP1, long amount1, double BP2, long amount2, byte U, double CU1, double CU2) {
		double pw = (BP1 * amount1 / (U * CU1 + 1)) + (BP2 * amount2 / (U * CU2 + 1));
		if (pp.useEnergy(pw, pw, false) != pw) return false;
		pp.useEnergy(pw, pw, true);
		return true;
	}

	private static double useEnergy(APowerTile pp, double D, double BP, byte U, double CU) {
		double pw = Math.min(2 + pp.getStoredEnergy() / 500, (D - 0.1) * BP / (U * CU + 1));
		pw = pp.useEnergy(pw, pw, true);
		return pw * (U * CU + 1) / BP + 0.1;
	}

	private static double useEnergy(APowerTile pp, double BP, byte U, double CU, byte F, double CF, boolean S, double CS, byte E, double CE) {
		double pwc = BP * Math.pow(CF, F) * Math.pow(CE, E) / (U * CU + 1);
		if (S) pwc *= CS;
		pwc = pp.useEnergy(0, pwc, true);
		if (S) pwc = pwc / CS;
		pwc /= Math.pow(CF, F);
		pwc *= U * CU + 1;
		return pwc;
	}

	static boolean useEnergyB(APowerTile pp, float H, byte SF, byte U, TileBasic t) {
		double BP, CU, CSP;
		if (t instanceof TileMiningWell) {
			BP = W_BP;
			CU = W_CU;
			if (SF < 0) CSP = W_CS;
			else CSP = Math.pow(W_CF, SF);
		} else {
			BP = B_BP;
			CU = B_CU;
			if (SF < 0) CSP = B_CS;
			else CSP = Math.pow(B_CF, SF);
		}
		return useEnergy(pp, BP, H, CSP, U, CU);
	}

	static boolean useEnergyF(APowerTile pp, byte U) {
		return useEnergy(pp, F_BP, U, F_CU);
	}

	static boolean useEnergyR(APowerTile pp, int energy, byte U) {
		return useEnergy(pp, energy, U, R_CU);
	}

	static boolean useEnergyP(APowerTile pp, byte U, long liquids, long frames) {
		return useEnergy(pp, PL_BP, liquids, PF_BP, frames, U, PL_CU, PF_CU);
	}

	static double useEnergyH(APowerTile pp, double distance, byte U) {
		return useEnergy(pp, distance, H_BP, U, H_CU);
	}

	static double useEnergyL(APowerTile pp, byte U, byte F, boolean S, byte E) {
		return useEnergy(pp, L_BP, U, L_CU, F, L_CF, S, L_CS, E, L_CE);
	}

}
