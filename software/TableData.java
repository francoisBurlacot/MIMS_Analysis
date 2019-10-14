package software;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
Copyright (C) 2019-F.Burlacot

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see: https://www.gnu.org/licenses/.**/



/**
 * The class which contains the formatted data, used to create the JTable with
 * TableRow.
 */
public class TableData extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private final List<TableRow> table = new ArrayList<TableRow>();
	private final int nbColumn;
	private final List<String> heading;

	/**
	 * Constructor of TableData
	 *
	 * @param heading:  the title of the columns for the table
	 * @param nbColumn: number of column
	 * @param caption:  name of each row (first column of our table)
	 * @param min:      min value measured in Display Curves
	 * @param max:      max value measured in Display Curves
	 * @param value:    value for a certain abscissa, measured in Display Curves
	 * @param average:  value for a certain framing, measured in Display Curves
	 */
	public TableData(List<String> heading, int nbColumn, String[] caption, List<Double> min, List<Double> max,
			List<Double> value, List<Double> average) {
		super();
		this.heading = heading;
		this.nbColumn = nbColumn;
		for (int i = 0; i < nbColumn - 1; i++) {
			table.add(new TableRow(caption[i + 1], min.get(i), max.get(i), value.get(i), average.get(i)));
		}

	}

	/**
	 * Getter of the number of row for our table
	 */
	public int getRowCount() {
		return nbColumn - 1;
	}

	/**
	 * Getter of the number of column for our table
	 */
	public int getColumnCount() {
		return heading.size();
	}

	/**
	 * Getter of the title of each column for our table
	 */
	public String getColumnName(int columnIndex) {
		return heading.get(columnIndex);
	}

	/**
	 * Getter of each cell
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return table.get(rowIndex).getName();
		case 1:
			return table.get(rowIndex).getMin();
		case 2:
			return table.get(rowIndex).getMax();
		case 3:
			return table.get(rowIndex).getValue();
		case 4:
			return table.get(rowIndex).getAverage();
		default:
			return null;
		}
	}
}