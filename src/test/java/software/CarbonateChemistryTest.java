package software;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CarbonateChemistryTest {

	private static final double DELTA = 1e-9;

	@Test
	void factorIsOneWhenPhIsFarBelowBothPka() {
		/* pH << pKa1 << pKa2: both correction terms vanish, Ci ~= Cco2 */
		double factor = CarbonateChemistry.ciCorrectionFactor(1, 6.4, 10.3);
		assertEquals(1.0, factor, 1e-4);
	}

	@Test
	void factorGrowsWithIncreasingPh() {
		double lowPh = CarbonateChemistry.ciCorrectionFactor(6, 6.4, 10.3);
		double highPh = CarbonateChemistry.ciCorrectionFactor(9, 6.4, 10.3);
		assertTrue(highPh > lowPh, "correction factor should increase with pH");
	}

	@Test
	void matchesClosedFormAtPkaDefaults() {
		double pH = 7.2;
		double pKa1 = 6.4;
		double pKa2 = 10.3;
		double expected = (1 + Math.pow(10, pH - pKa1)) * (1 + Math.pow(10, pH - pKa2));
		assertEquals(expected, CarbonateChemistry.ciCorrectionFactor(pH, pKa1, pKa2), DELTA);
	}

	@Test
	void reactsToCustomPkaValues() {
		double pH = 7;
		double defaultFactor = CarbonateChemistry.ciCorrectionFactor(pH, 6.4, 10.3);
		double customFactor = CarbonateChemistry.ciCorrectionFactor(pH, 5, 9);
		assertTrue(customFactor > defaultFactor,
				"lowering pKa1/pKa2 towards the sample pH should increase the correction factor");
	}
}
