package software;

/**
Copyright (C) 2019-F.Burlacot

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see: https://www.gnu.org/licenses/.**/

/**
 * Class which create each row of our table.
 */
public class TableRow {
	private String name;
	private double min;
	private double max;
	private double average;
	private double value;

	/**
	 * Constructor of TableRow
	 * 
	 * @param name:    title of our Row (first cell of our row)
	 * @param min:     min value for the current row
	 * @param max:     max value for the current row
	 * @param value:   value for a certain abscissa for the current row
	 * @param average: average for a certain frame, for the current row
	 */
	public TableRow(String name, Double min, Double max, Double value, Double average) {
		super();

		this.name = name;
		this.min = min;
		this.max = max;
		this.average = average;
		this.value = value;
	}

	/**
	 * Getter for the name of our row
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for the name of our row
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter for the min of our row
	 */
	public Double getMin() {
		return min;
	}

	/**
	 * Setter for the min of our row
	 */
	public void setMin(Double min) {
		this.min = min;
	}

	/**
	 * Getter for the max of our row
	 */
	public Double getMax() {
		return max;
	}

	/**
	 * Setter for the max of our row
	 */
	public void setMax(Double max) {
		this.max = max;
	}

	/**
	 * Getter for the average of our row
	 */
	public Double getAverage() {
		return average;
	}

	/**
	 * Setter for the average of our row
	 */
	public void setAverage(Double average) {
		this.average = average;
	}

	/**
	 * Getter for the value of our row
	 */
	public Double getValue() {
		return value;
	}

	/**
	 * Setter for the value of our row
	 */
	public void setValue(Double value) {
		this.value = value;
	}
}