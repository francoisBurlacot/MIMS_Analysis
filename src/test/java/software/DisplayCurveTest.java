package software;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class DisplayCurveTest {

	/*
	 * significantDigit is a plain static utility (round to N significant
	 * digits); these values were captured from the current implementation and
	 * pin down its behavior so future refactors don't silently change the
	 * numbers shown in the software.
	 */
	@Test
	void roundsToThreeSignificantDigits() {
		assertEquals(327.3, DisplayCurve.significantDigit(327.3456, 3));
		assertEquals(-45.67, DisplayCurve.significantDigit(-45.678, 3));
		assertEquals(3.0, DisplayCurve.significantDigit(3.0, 3));
		assertEquals(100.0, DisplayCurve.significantDigit(100.0, 3));
		assertEquals(0.999, DisplayCurve.significantDigit(0.999999, 3));
		assertEquals(46.6, DisplayCurve.significantDigit(46.6, 3));
		assertEquals(22.89, DisplayCurve.significantDigit(22.891, 3));
	}

	/**
	 * Builds a minimal two-curve dataset (time, value) to exercise
	 * calculateValue/calculateAverage without needing a real data file.
	 */
	private DisplayCurve newTestCurve() throws ParseException {
		List<Double[]> data = new ArrayList<>();
		data.add(new Double[] { 0.0, 10.0 });
		data.add(new Double[] { 1.0, 20.0 });
		data.add(new Double[] { 2.0, 30.0 });
		data.add(new Double[] { 3.0, 40.0 });
		String[] column = { "Time (min)", "Value" };
		List<String> heading = List.of("Molecule", "Min", "Max", "Value for T = ", "Average between ");
		return new DisplayCurve(data, column, 2, heading, "Test Curve");
	}

	@Test
	void calculateValueInterpolatesBetweenSurroundingPoints() throws ParseException {
		DisplayCurve curve = newTestCurve();
		/* at t=1.5, the value should be halfway between the rows at t=1 (20) and t=2 (30) */
		List<Double> value = curve.calculateValue(curve.data, 1.5);
		assertEquals(25.0, value.get(0));
	}

	@Test
	void calculateAverageOverFullRangeMatchesArithmeticMean() throws ParseException {
		DisplayCurve curve = newTestCurve();
		List<Double> average = curve.calculateAverage(curve.data, 0.0, 3.0);
		/* (10+20+30+40)/4 = 25 */
		assertEquals(25.0, average.get(0));
	}
}
