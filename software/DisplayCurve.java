package software;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
Copyright (C) 2019-F.Burlacot

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see: https://www.gnu.org/licenses/.**/

/**
 * The class that create a JChart and a JTable for each data.
 */
public class DisplayCurve extends JPanel {
	private static final long serialVersionUID = 1L;
	private ChartPanel chartPanel;

	/* The scrollPane contain the JTable */
	private JScrollPane scrollPane;

	/* contains data displayed */
	List<Double[]> data;

	/* heading of JTable */
	private List<String> heading = new ArrayList<String>();

	/* list of dataset (one for each column of data, except abscissa) */
	private List<XYSeriesCollection> dataset = new ArrayList<XYSeriesCollection>();

	/* list of XYSeries (one for each column of data, except abscissa) */
	private List<XYSeries> listSerie = new ArrayList<XYSeries>();

	/* list of axis (one for each column of data, except abscissa) */
	private List<NumberAxis> axis = new ArrayList<NumberAxis>();

	/* The last row treated and added to the chart */
	private int currentRow = 0;

	/* the chart */
	private JFreeChart finalChart;
	/* nb of curves to display+1 */
	private int nbColumn;

	/* list of min and max for each curves */
	private List<Double> max = new ArrayList<Double>();
	private List<Double> min = new ArrayList<Double>();

	/* JTable */
	private JTable table;

	/* Panel which contain the chart and the JTable */
	private JPanel principalPanel = new JPanel(new BorderLayout());

	/* The fields for the user, used to choose data to calculate */
	private JTextField valueAt = new JTextField();
	private JTextField lowerBound = new JTextField();
	private JTextField upperBound = new JTextField();

	/* panel which contain the JTable, buttons and textField */
	private JPanel tablePanel = new JPanel(new BorderLayout());
	/* name of each curves to display + abscissa */
	private String[] column;
	private String title;

	/* lower and uper abscissa value */
	private double upperAbscissa = Double.NEGATIVE_INFINITY;
	private double lowerAbscissa = Double.POSITIVE_INFINITY;

	/*
	 * boolean used two avoid two process from updating the jTable at the same time
	 */
	private boolean updatingTable = false;

	/**
	 * Constructor of the JChart and the JTable
	 * 
	 * @param datas:    The data to display in the chart
	 * @param column:   The name of each curves to display in the chart, including
	 *                  the abscissa (the abscissa is the first element of the list)
	 * @param nbColumn: Number of each curves to display in the chart, including the
	 *                  abscissa (the abscissa is the first element of the list)
	 * @param heading:  Heading of each row of the JTable
	 * @param title:    Title of the JChart
	 * @throws ParseException: Parsing the data to Double or Int
	 */

	public DisplayCurve(List<Double[]> datas, String[] column, int nbColumn, List<String> heading, String title)
			throws ParseException {

		/* name of curves */
		this.column = column;
		/* heading of the Jtable */
		this.heading = heading;
		/* get the nb of curves +1 */
		this.nbColumn = nbColumn;
		this.title = title;
		this.data = datas;

		/* create the dataset with the data */
		dataset = createDataset(data);
		/* create the chart with the dataset */
		finalChart = createChart(dataset);
		/* create the chartPanel which contain the chart */
		chartPanel = new ChartPanel(finalChart);
		chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		chartPanel.setBackground(Color.white);

		/*
		 * set the value of JtextField with lower and upper abscissa value (of the
		 * current time, JField aren't updated to let the user get information while
		 * process is running)
		 */
		valueAt = new JTextField(String.valueOf(lowerAbscissa));
		lowerBound = new JTextField(String.valueOf(lowerAbscissa));
		upperBound = new JTextField(String.valueOf(upperAbscissa));
		/* Edit the heading of the JTable */
		List<String> currentHeading = updateHeading();

		/*
		 * create the JTable with the data (using lower and upper abscissa at the
		 * begining)
		 */
		table = new JTable(new TableData(currentHeading, nbColumn, column, min, max,
				calculateValue(data, lowerAbscissa), calculateAverage(data, lowerAbscissa, upperAbscissa)));
		/*
		 * put the JTable in an JScrollPane and set his size to view correctly each row
		 */
		scrollPane = new JScrollPane(table);
		table.setPreferredScrollableViewportSize(
				new Dimension(table.getPreferredSize().width, table.getRowHeight() * table.getRowCount()));

		/* Add button to calculate the data and edit the JTable */
		JButton calculate = new JButton("Calculate");
		calculate.setVerticalTextPosition(AbstractButton.CENTER);
		calculate.setHorizontalTextPosition(AbstractButton.LEADING);
		calculate.setActionCommand("Calculate");
		calculate.setVisible(true);

		/* add action listener on the buttons */
		calculate.addActionListener(new Calculate());

		/* Set size of JTextField */
		lowerBound.setColumns(10);
		upperBound.setColumns(10);
		valueAt.setColumns(10);

		/* JPanel which contain the button and JtextField */
		JPanel field = new JPanel(new FlowLayout());
		field.add(new JLabel("Value of abscissa:"));
		field.add(valueAt);
		field.add(new JLabel("     Lower Bound for Average:"));
		field.add(lowerBound);
		field.add(new JLabel("     Upper Bound for Average:"));
		field.add(upperBound);
		field.add(calculate);

		/* panel which contain the table and the text field */
		tablePanel.add(field, BorderLayout.NORTH);
		tablePanel.add(scrollPane, BorderLayout.CENTER);

		/* create the panel which contain everything */
		principalPanel.add(chartPanel, BorderLayout.CENTER);
		principalPanel.add(tablePanel, BorderLayout.SOUTH);

	}

	/**
	 * Transform the data into a dataset (each curves is a XYSeriesCollection)
	 * 
	 * @param datas
	 * @return
	 * @throws ParseException
	 */
	private List<XYSeriesCollection> createDataset(List<Double[]> datas) throws ParseException {

		/* Create a XYSeriesCollection for each curves */
		for (int j = 0; j < datas.size(); j++) {
			if (upperAbscissa < datas.get(j)[0]) {
				upperAbscissa = datas.get(j)[0];
			}
			if (lowerAbscissa > datas.get(j)[0]) {
				lowerAbscissa = datas.get(j)[0];
			}
		}
		for (int i = 1; i < nbColumn; i++) {

			/*
			 * we create a XYSerieCollection which contain one XYSeries. All XYSeries aren't
			 * in the same Collection because we want personalize vertical axes for each
			 * curves
			 */
			XYSeriesCollection dataserie = new XYSeriesCollection();
			/*
			 * the i+1 represent the fact that we take the data of each curves, abscissa as
			 * already be taken in account
			 */
			XYSeries serie = new XYSeries(column[i]);
			for (int j = 0; j < datas.size(); j++) {
				/* we add the (X,Y) to the XYserie */
				serie.add(datas.get(j)[0], datas.get(j)[i]);
			}
			/* we had the XYseries to the XYSeriecollection and then to the dataset */
			listSerie.add(serie);
			dataserie.addSeries(serie);
			dataset.add(dataserie);
		}
		/* we change the index of the last data row treated */
		currentRow += datas.size();
		return dataset;
	}

	/**
	 * Create the Chart
	 * 
	 * @param dataset: XYSeriesCollection, containing data for each curves
	 * @return
	 */
	private JFreeChart createChart(final List<XYSeriesCollection> dataset) {
		/* create a new plot, a personalize one */
		XYPlot plot = new XYPlot();
		/* define the horizontal axis */
		plot.setDomainAxis(new NumberAxis(column[0]));

		/* define a list of vertical axis, one for each curves */
		axis = new ArrayList<NumberAxis>();
		/* define a list of renderer, one for each curves */
		List<XYLineAndShapeRenderer> renderer = new ArrayList<XYLineAndShapeRenderer>();

		/* Define a list of 18 differentiable colors */
		List<Color> color = new ArrayList<Color>();
		color.add(new Color(167, 182, 53));
		color.add(new Color(139, 96, 209));
		color.add(new Color(91, 188, 79));
		color.add(new Color(201, 86, 176));
		color.add(new Color(95, 150, 69));
		color.add(new Color(213, 65, 99));
		color.add(new Color(85, 195, 170));
		color.add(new Color(206, 79, 47));
		color.add(new Color(97, 159, 216));
		color.add(new Color(215, 152, 51));
		color.add(new Color(117, 117, 194));
		color.add(new Color(118, 116, 32));
		color.add(new Color(185, 101, 140));
		color.add(new Color(58, 139, 98));
		color.add(new Color(225, 136, 113));
		color.add(new Color(109, 115, 59));
		color.add(new Color(158, 97, 47));
		color.add(new Color(190, 174, 102));

		/* For each future curves we edit the renderer and the axis */
		for (int i = 0; i < nbColumn - 1; i++) {
			Color util = null;

			/* set the renderer and add legend to the axis */
			renderer.add(new XYLineAndShapeRenderer());
			axis.add(new NumberAxis(column[i + 1]));

			/* plot the corresponding XYSeriesCollection */
			plot.setDataset(i, dataset.get(i));

			/* change color of curves, axis and caption depending of curve name */
			if (column[i + 1].startsWith("H2 ")) {
				util = color.get(0);
			} else if (column[i + 1].startsWith("HD")) {
				util = color.get(1);
			} else if (column[i + 1].startsWith("D2")) {
				util = color.get(2);
			} else if (column[i + 1].startsWith("H2O")) {
				util = color.get(3);
			} else if (column[i + 1].startsWith("N2 ")) {

				util = color.get(4);
			} else if (column[i + 1].startsWith("NO")) {
				util = color.get(5);
			} else if (column[i + 1].startsWith("ETOH")) {
				util = color.get(6);
			} else if (column[i + 1].startsWith("MetOH")) {
				util = color.get(7);
			} else if (column[i + 1].startsWith("O2")) {
				util = color.get(8);
			} else if (column[i + 1].startsWith("H2S")) {
				util = color.get(9);
			} else if (column[i + 1].startsWith("18O2")) {
				util = color.get(10);
			} else if (column[i + 1].startsWith("Ar")) {
				util = color.get(11);
			} else if (column[i + 1].startsWith("CO2")) {
				util = color.get(12);
			} else if (column[i + 1].startsWith("N20")) {
				util = color.get(13);
			} else if (column[i + 1].startsWith("13CO2")) {
				util = color.get(14);
			} else if (column[i + 1].startsWith("13C18OO")) {
				util = color.get(15);
			} else if (column[i + 1].startsWith("13C18O2")) {
				util = color.get(16);
			} else if (column[i + 1].startsWith("Ci")) {
				util = color.get(17);
			} else if (column[i + 1].startsWith("Uo")) {
				util = Color.red;
			} else if (column[i + 1].startsWith("Eo")) {
				util = Color.blue;
			} else if (column[i + 1].startsWith("Net")) {
				util = Color.black;
			} else if (column[i + 1].startsWith("Vuo")) {
				util = Color.red;
			} else if (column[i + 1].startsWith("Veo")) {
				util = Color.blue;
			} else if (column[i + 1].startsWith("Vnet")) {
				util = Color.black;
			} /* just take a color in other case */
			else {
				util = color.get((i) % 17);
			}

			/* set axis parameters and color */
			axis.get(i).setTickLabelPaint(util);
			axis.get(i).setTickMarkPaint(util);
			axis.get(i).setAxisLinePaint(util);
			axis.get(i).setLabelPaint(util);
			renderer.get(i).setSeriesPaint(0, util);

			/* plot the renderer with the corresponding curves */
			plot.setRenderer(i, renderer.get(i));

			/* get max and min for each curves (with 3 significant figure) */
			max.add(significantDigit(dataset.get(i).getRangeUpperBound(true), 3));
			min.add(significantDigit(dataset.get(i).getRangeLowerBound(true), 3));

			/* set range of vertical axis */
			axis.get(i).setRange(min.get(i) - Math.abs(min.get(i) / 100), max.get(i) + Math.abs(max.get(i) / 100));
			plot.setRangeAxis(i, axis.get(i));
			plot.mapDatasetToRangeAxis(i, i);

		}

		/* set plot parameters */
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinesVisible(true);
		plot.setBackgroundPaint(Color.white);

		/* create the Chart and return it */
		JFreeChart chart = new JFreeChart(title, getFont(), plot, true);
		chart.getLegend().setFrame(BlockBorder.NONE);
		chart.setBackgroundPaint(Color.white);
		chart.setTitle(new TextTitle(title, new Font("Serif", Font.BOLD, 18)));
		return (chart);
	}

	/**
	 * Getter for the principalPanel (which contains the ScrollPane and the Chart)
	 */
	public JPanel getPanel() {
		return (principalPanel);
	}

	/**
	 * Getter of the abscissa value, typed by the user in the corresponding field
	 */
	private double getValue() {
		String string = valueAt.getText();
		double returnValue = (double) 0;
		try {
			returnValue = Double.parseDouble(string);
		} catch (NumberFormatException e) {
			Main.logger.info(e.toString());
			/* if the user didn't enter a correct value, return -infinite and a warning */
			Window.infoBox(principalPanel,
					"There is an issue with your entered abscissa value: " + "\n" + "Please enter a number !",
					"Entered Abscissa Value");
			return (Double.POSITIVE_INFINITY);
		}
		return (returnValue);
	}

	/**
	 * Getter of the lowerBound typed by the user in the corresponding field
	 */
	public double getLowerBound() {
		String string = lowerBound.getText();
		double returnValue = (double) 0;
		try {
			returnValue = Double.parseDouble(string);
		} catch (NumberFormatException e) {
			Main.logger.info(e.toString());
			/* if the user didn't enter a correct value, return +infinite and a warning */
			Window.infoBox(principalPanel,
					"There is an issue with your entered lowerBound: " + "\n" + "Please enter a number !",
					"Entered Lower Bound");
			return (Double.POSITIVE_INFINITY);
		}
		return (returnValue);
	}

	/**
	 * Getter of the upperBound typed by the user in the corresponding field
	 */
	public double getUpperBound() {
		String string = upperBound.getText();
		double returnValue = (double) 0;
		try {
			returnValue = Double.parseDouble(string);
		} catch (NumberFormatException e) {
			Main.logger.info(e.toString());
			/* if the user didn't enter a correct value, return -infinite and a warning */
			Window.infoBox(principalPanel,
					"There is an issue with your entered upperBound: " + "\n" + "Please enter a number !",
					"Entered Upper Bound");
			return (Double.NEGATIVE_INFINITY);
		}
		return (returnValue);
	}

	/**
	 * The actionListener which start the procedure to update the JPanel
	 */
	class Calculate implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			updateTable(data);
		}
	}

	/**
	 * The function which calculate the average for each curves, with bounds entered
	 * by the user.
	 */
	public List<Double> calculateAverage(List<Double[]> datas, double lowerBound, double upperBound) {

		List<Double> result = new ArrayList<Double>();
		/* deals with three recurrent erros */
		if (lowerBound > upperBound) {
			Window.infoBox(principalPanel, "There is an issue with your entered values: " + "\n"
					+ "Please enter an lowerBound inferior than the upperBound!", "Entered Values");
		} else if (lowerBound > upperAbscissa) {
			Window.infoBox(principalPanel, "There is an issue with your entered values: " + "\n"
					+ "Please enter a lowerBound inferior than " + upperAbscissa + " !", "Entered Value");
		} else if (upperBound < lowerAbscissa) {
			Window.infoBox(principalPanel, "There is an issue with your entered values: " + "\n"
					+ "Please enter an upperBound superior than " + lowerAbscissa + " !", "Entered Value");
		}
		/* calculate the average */
		for (int i = 1; i < nbColumn; i++) {
			double average = 0;
			int nbValue = 0;
			for (int j = 0; j < datas.size(); j++) {
				if ((datas.get(j)[0]) >= lowerBound && (datas.get(j)[0]) <= upperBound) {
					average += datas.get(j)[i];
					nbValue++;
				}
			}
			double calcul = (average / nbValue);
			result.add(significantDigit(calcul, 3));

		}
		return (result);
	}

	/**
	 * The function which calculate the value for a certain abscissa for each
	 * curves, with the value entered by the user.
	 */
	public List<Double> calculateValue(List<Double[]> datas, double value) {
		List<Double> result = new ArrayList<Double>();
		/* if the abscissa value is not in the dataset, warn the user */
		if (value < lowerAbscissa || value > upperAbscissa) {
			Window.infoBox(
					principalPanel, "There is an issue with your entered value: " + "\n"
							+ "Please entered a number between " + lowerAbscissa + " and " + upperAbscissa + "  !",
					"Entered Value");
			for (int i = 1; i < nbColumn; i++) {
				result.add((double) 0);
			}
			return (result);
		}

		/*
		 * We return the value with the nearer abscissa of the wanted value
		 */
		for (int i = 1; i < nbColumn; i++) {
			Double[] surrond = { Double.POSITIVE_INFINITY, (double) 0, (double) 0, (double) 0 };
			/* get the two nearer values */
			/*don't take first and last row data; because of the +1 and -1*/
			for (int j = 1; j < datas.size() - 1; j++) {
				if (Math.abs((datas.get(j)[0]) - value) <= Math.abs(surrond[0] - value)) {
					if (datas.get(j)[0] <= value) {
						surrond[0] = datas.get(j)[0];
						surrond[1] = datas.get(j)[i];
						surrond[2] = datas.get(j + 1)[0];
						surrond[3] = datas.get(j + 1)[i];
					} else {
						surrond[0] = datas.get(j)[0];
						surrond[1] = datas.get(j)[i];
						surrond[2] = datas.get(j - 1)[0];
						surrond[3] = datas.get(j - 1)[i];
					}
				}
			}
			/* do a linear regression */
			double b = (surrond[3] - surrond[1]) / (surrond[2] - surrond[0]);
			double a = (surrond[3] - b * surrond[2]);
			double calcul = (b * value + a);
			result.add(significantDigit(calcul, 3));
		}
		return (result);
	}

	/**
	 * The function which add the new received data to the chart, and edit the
	 * JTable if needed for each curves.
	 *
	 * @param data: current data of our chart
	 */
	public void addData(List<Double[]> datas, int nbRow) {
		data = datas;
		/* boolean to know if we have to change the JTable */
		boolean tableChange = false;
		/* add data while the current data row isn't equal to our last added data row */
		while (currentRow < nbRow) {
			for (int i = 0; i < nbColumn - 1; i++) {
				/* boolean to know if we have to change the axis range */
				boolean axisChange = false;
				/* add the new data to our serie for each serie of data */
				listSerie.get(i).addOrUpdate(data.get(currentRow)[0], data.get(currentRow)[i + 1]);

				/* edit values of upper and lower abscissa if needed */
				if (upperAbscissa < data.get(currentRow)[0]) {
					upperAbscissa = data.get(currentRow)[0];
				}
				if (lowerAbscissa > data.get(currentRow)[0]) {
					lowerAbscissa = data.get(currentRow)[0];
				}

				/* if new max, edit the max and the JTable */
				if (data.get(currentRow)[i + 1] > max.get(i)) {
					max.set(i, significantDigit(data.get(currentRow)[i + 1], 3));
					axisChange = true;
					tableChange = true;
				}
				/* if new min, edit the min and the JTable */
				if (data.get(currentRow)[i + 1] < min.get(i)) {
					min.set(i, significantDigit(data.get(currentRow)[i + 1], 3));
					axisChange = true;
					tableChange = true;
				}
				/* if new max or min, edit the axis range */
				if (axisChange) {
					axis.get(i).setRange(min.get(i) - Math.abs(min.get(i) / 100),
							max.get(i) + Math.abs(min.get(i) / 100));
				}
			}
			currentRow++;

		}

		/* if new max or min, edit the JTable */
		if (tableChange) {
			updateTable(data);
		}

	}

	/**
	 * Update our JTable when the user click on calculate or if min or max changed
	 * after new data arrived
	 * 
	 * @param data: current data of our chart
	 */
	public void updateTable(List<Double[]> datas) {
		/* be sure that the table is update by only one thread */
		while (updatingTable) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				Main.logger.severe(e1.toString());
			}

		}
		updatingTable = true;
		data = datas;
		/* if the user did'nt entered a correct value, don't edit the JTable */
		if (getValue() == Double.POSITIVE_INFINITY || getLowerBound() == Double.POSITIVE_INFINITY
				|| getUpperBound() == Double.NEGATIVE_INFINITY) {
			return;
		}
		/* update heading of our JTable */
		List<String> currentHeading = updateHeading();
		/* remove the scrollpane */
		tablePanel.remove(scrollPane);
		/* create a new JTable with the correct values */
		table = new JTable(new TableData(currentHeading, nbColumn, column, min, max, calculateValue(data, getValue()),
				calculateAverage(data, getLowerBound(), getUpperBound())));
		/* add the new JTable */
		scrollPane = new JScrollPane(table);
		table.setPreferredScrollableViewportSize(
				new Dimension(table.getPreferredSize().width, table.getRowHeight() * table.getRowCount()));
		tablePanel.add(scrollPane, BorderLayout.CENTER);
		principalPanel.revalidate();
		updatingTable = false;
	}

	/**
	 * Update the heading of our JTabel with the textField
	 */
	public List<String> updateHeading() {
		List<String> currentHeading = new ArrayList<String>();
		currentHeading.add(heading.get(0));
		currentHeading.add(heading.get(1));
		currentHeading.add(heading.get(2));
		/* if value in field not double, edit to NaN */
		if (getValue() == Double.POSITIVE_INFINITY) {
			currentHeading.add(heading.get(3) + "NaN");
		} else {
			currentHeading.add(heading.get(3) + getValue());
		}
		/* if value in field not double, edit to NaN */
		if (getLowerBound() == Double.POSITIVE_INFINITY) {
			if (getUpperBound() == Double.NEGATIVE_INFINITY) {
				currentHeading.add(heading.get(4) + "NaN and NaN ");
			} else {
				currentHeading.add(heading.get(4) + "NaN and " + getUpperBound());
			}
		} else if (getUpperBound() == Double.NEGATIVE_INFINITY) {

			currentHeading.add(heading.get(4) + getLowerBound() + " and NaN");
		}

		else {
			currentHeading.add(heading.get(4) + getLowerBound() + " and " + getUpperBound());
		}
		return currentHeading;

	}

	/**
	 * Function which return a round number (with less significants digits)
	 * 
	 * @param value:  value to approximate
	 * @param number: number of significant digit to return
	 * @return approximate value
	 */
	public static double significantDigit(Double value, int number) {
		/* transform the value to a string */
		String valueToCrop = Double.toString(value);
		String cropResult = valueToCrop;
		int indexE = 0;
		int significant = number + 2;
		/* get the index of the E, in case we have power */
		for (int index = 0; index < valueToCrop.length(); index++) {
			if (valueToCrop.charAt(index) == 'E') {
				indexE = index;
				break;
			}
		}
		/*
		 * if we have a "-" add 1 to significant to have the right number of significant
		 * digit
		 */
		if (valueToCrop.charAt(0) == '-') {
			significant++;
		}
		/* if we have a E */
		if (indexE != 0) {
			/* if our number is longer than significant and last figure to erase is >=5 */
			if (indexE > significant && Integer.parseInt(String.valueOf(valueToCrop.charAt(significant))) >= 5) {
				/*
				 * add 1 to previous figure if <9, and concatenate this number with the power
				 * (E^10 for example)
				 */
				if (Integer.parseInt(String.valueOf(valueToCrop.charAt(significant - 1))) < 9) {
					cropResult = valueToCrop.substring(0, significant - 1)
							+ Integer
									.toString(Integer.parseInt(String.valueOf(valueToCrop.charAt(significant - 1))) + 1)
							+ valueToCrop.substring(indexE, valueToCrop.length());
				}
				/* previous figure ==9 */
				else {
					int j = significant - 1;
					/*
					 * zeros is what we will add to the end of our cropedNumber once we find a
					 * figure !=9
					 */
					String zeros = "";
					/* until one figure isn't 9 and we haven't been throw all figures */
					while (j > 0) {
						/* special case if we have a comma */
						if (valueToCrop.charAt(j) == '.') {
							j--;
							zeros = '.' + zeros;
						}
						/* if we find oune figure */
						else if (Integer.parseInt(String.valueOf(valueToCrop.charAt(j))) < 9) {
							break;
						} else {
							j--;
							zeros = 0 + zeros;
						}
					}
					/* crop the result */
					cropResult = valueToCrop.substring(0, j)
							+ Integer.toString(Integer.parseInt(String.valueOf(valueToCrop.charAt(j))) + 1) + zeros
							+ valueToCrop.substring(indexE, valueToCrop.length());
				}

			} /* if the last figure cropped is <5 */
			else if (indexE > significant) {
				cropResult = valueToCrop.substring(0, significant)
						+ valueToCrop.substring(indexE, valueToCrop.length());

			}
			/* if our number is already with less or the good number of significant digit */
			else {
				cropResult = valueToCrop;
			}
		}
		/* Exactly the same but we don't have a 'E' in our number */
		else {
			if (indexE > significant && Integer.parseInt(String.valueOf(valueToCrop.charAt(significant))) >= 5) {
				if (Integer.parseInt(String.valueOf(valueToCrop.charAt(significant - 1))) < 9) {
					cropResult = valueToCrop.substring(0, significant - 1) + Integer
							.toString(Integer.parseInt(String.valueOf(valueToCrop.charAt(significant - 1))) + 1);
				} else {
					int j = significant - 1;
					String zeros = "";
					while (j > 0) {
						if (valueToCrop.charAt(j) == '.') {
							j--;
							zeros = '.' + zeros;
						} else if (Integer.parseInt(String.valueOf(valueToCrop.charAt(j))) < 9) {
							break;
						} else {
							j--;
							zeros = 0 + zeros;
						}
					}
					cropResult = valueToCrop.substring(0, j)
							+ Integer.toString(Integer.parseInt(String.valueOf(valueToCrop.charAt(j))) + 1) + zeros;
				}

			} else if (valueToCrop.length() > significant) {
				cropResult = valueToCrop.substring(0, significant);
			} else {

				cropResult = valueToCrop;
			}
		}
		return (Double.parseDouble(cropResult));
	}
}
