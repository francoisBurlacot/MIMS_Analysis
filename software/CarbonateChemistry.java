package software;

/**
Copyright (C) 2019-F.Burlacot

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see: https://www.gnu.org/licenses/.**/

/**
 * Carbonic acid equilibrium helper used to compute the dissolved inorganic
 * carbon (Ci) from the CO2 concentration and pH.
 */
public final class CarbonateChemistry {

	private CarbonateChemistry() {
	}

	/**
	 * Returns the factor such that Ci = Cco2 * factor, with
	 * factor = (1 + 10^(pH - pKa1)) * (1 + 10^(pH - pKa2)).
	 *
	 * @param pH:   pH of the sample
	 * @param pKa1: pKa of the CO2/HCO3- equilibrium (defaults to 6.4)
	 * @param pKa2: pKa of the HCO3-/CO3-- equilibrium (defaults to 10.3)
	 */
	public static double ciCorrectionFactor(double pH, double pKa1, double pKa2) {
		return (1 + Math.exp((pH - pKa1) * Math.log(10))) * (1 + Math.exp((pH - pKa2) * Math.log(10)));
	}
}
