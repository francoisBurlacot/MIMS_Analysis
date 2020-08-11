package software;

import javax.swing.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
Copyright (C) 2019-F.Burlacot

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see: https://www.gnu.org/licenses/.**/

/**
 * The principal window of our program
 */
public class Window extends JFrame {

	private static final long serialVersionUID = 1L;

	/* Each List<Double[]> contains the data of each corresponding dataset */
	private List<Double[]> amperometricData;
	private List<Double[]> gasConcentrationData;
	private List<Double[]> gasExchangeRatesData;
	private List<Double[]> cumulatedGasExchangeData;
	private List<Double[]> denoisedGasExchangeRatesData;
	private List<Double[]> denoisedCumulatedGasExchangeData;
	private List<Double[]> o2ExchangeRatesData;
	private List<Double[]> o2ExchangeData;
	private List<Double[]> gasExchangeRateFunctionConcentrationData;
	private List<Double[]> hydrogenaseActivityData;

	/*
	 * gasConcentrationCorrection is used to obtain gasConcentrationData. Each row
	 * contains the factor C_max/((A_max-A_0)) of the corresponding molecule, for
	 * the formula C(t)=A(t)聃_max/((A_max-A_0))
	 */
	private List<Double> gasConcentrationCorrection;

	/*
	 * gasConcentrationConcumption contain the factor k, used to obtain
	 * gasExchangeRatesData (v(t)=(delta(C(t)))/delta(t)-k聃(t))
	 */
	private List<Double> gasConcentrationConsumption;

	/*
	 * Each String[] contains the names of each curves to display of the
	 * corresponding dataset (including the abscissa name)
	 */
	private String[] amperometricColumnName;
	private String[] gasConcentrationColumnName;
	private String[] gasExchangeRatesColumnName;
	private String[] denoisedGasExchangeRatesColumnName;
	private String[] denoisedCumulatedGasExchangeColumnName;
	private String[] o2ExchangeRatesColumnName = new String[4];
	private String[] o2ExchangeColumnName = new String[4];
	private String[] gasExchangeRateFunctionConcentrationColumnName;
	private String[] hydrogenaseActivityColumnName = new String[2];

	/*
	 * working to false terminate each running thread (openData() thread and
	 * display#dataset#Curve() threads)
	 */
	private boolean working = false;
	/*
	 * pause to true paused the treatment of data, and so paused the curves panels
	 */
	private boolean pause = false;

	/* new#####Data warn corresponding thread to add data to corresponding chart */
	private boolean newAmperometricData = false;
	private boolean newGasConcentrationData = false;
	private boolean newGasExchangeRatesData = false;
	private boolean newCumulatedGasExchangeData = false;
	private boolean newDenoisedGasExchangeRatesData = false;
	private boolean newDenoisedCumulatedGasExchangeData = false;
	private boolean newO2ExchangeRatesData = false;
	private boolean newO2ExchangeData = false;
	private boolean newGasExchangeRateFunctionConcentrationData = false;
	private boolean newHydrogenaseActivityData = false;

	/* button in the principal panel, used to display corresponding Curve */
	private JButton amperometricCurve;
	private JButton gasConcentrationCurve;
	private JButton gasExchangeRatesCurve;
	private JButton cumulatedGasExchangeCurve;
	private JButton denoisedGasExchangeRatesCurve;
	private JButton denoisedCumulatedGasExchangeCurve;
	private JButton o2ExchangeRatesCurve;
	private JButton o2ExchangeCurve;
	private JButton gasExchangeRateFunctionConcentrationCurve;
	private JButton hydrogenaseActivityCurve;

	/*
	 * CheckBox are checkbox displayed in a popup window after user clicked on
	 * "Molecule to display" subMenu
	 */
	private JCheckBox h2oPresent = new JCheckBox("Hide H2O if H2O is present");
	private JCheckBox ciPresent = new JCheckBox("Calculate Ci if CO2 is present");

	/* this boolean represent the choice of user regarding displaying H2O and Ci */
	/* h2oT=true mean that h2o isn't displayed */
	private boolean h2oT = true;
	/* ciT=true mean that Ci is calculated and displayed */
	private boolean ciT = true;

	/*
	 * list of all running thread (used to wait for each of them to stopped before
	 * reloading the factor file or the data file
	 */
	private List<Thread> threads = new ArrayList<Thread>();

	/*
	 * the four threads of corresponding curve, that needed to be "restart" if value
	 * of step or molecule change in the fields and drop down menus
	 */
	private Thread exchangeThread = null;
	private Thread denoisedThread = null;
	private Thread oxygenThread = null;
	private Thread functionThread = null;

	/*
	 * JSpinner to get the step of the moving average for gasExchangeRates,
	 * denoisedGazExchangeRates and o2ExchangeRates
	 */
	private JSpinner movingGasExchangeRatesAverage = new JSpinner(new SpinnerNumberModel(10, 0, Integer.MAX_VALUE, 1));
	private JSpinner movingDenoisedGasExchangeRatesAverage = new JSpinner(
			new SpinnerNumberModel(10, 0, Integer.MAX_VALUE, 1));
	private JSpinner movingO2ExchangeAverage = new JSpinner(new SpinnerNumberModel(10, 0, Integer.MAX_VALUE, 1));

	/*
	 * subpanel containing the text and JSpinner for gasExchangeRates,
	 * denoisedGazExchangeRates and o2ExchangeRates
	 */
	private JPanel subPanel2;
	private JPanel subPanel3;
	private JPanel subPanel4;
	/*
	 * subpanel containing the text drop down list for v(gas)=F(concentration(gas))
	 */
	private JPanel subPanel5;

	/* threshold(step) of corresponding movingAverage */
	private int thresholdGasExchangeRatesAverage = 10;
	private int thresholdDenoisedGasExchangeRates = 10;
	private int thresholdO2ExchangeAverage = 10;

	/*
	 * DropDown list to get the two molecules to display V(molecule)=f(c(molecule2))
	 */
	private JComboBox<String> gasConcentrationMoleculeList = new JComboBox<String>();
	private JComboBox<String> denoisedGasExchangeRatesMoleculeList = new JComboBox<String>();

	/* Corresponding chosen Molecule */
	private String gasExchangeRatesMolecule;
	private String gasConcentrationMolecule;

	/* number of amperometric signal columns (curves+ abscissa) to display */
	private int nbAmperometricColumn;
	/* number of molecules (curves + abscissa) to display */
	private int nbMoleculeColumn;

	/* directory paths to save and load files (folder path) */
	private String loadPath = null;
	private String savePath = null;
	private String factorPath = null;

	/* exact path of current data file */
	private String dataFilePath = null;

	/* normalization factor */
	private double normalizationFactorValue = 1;

	/* index of last row received in our csv and added to amperometricData */
	private int nbRow = 0;

	/* abstract action in JMenBar, which open and load data file */
	private OpenData openData;

	/* Map which contain data of the factor file */
	private Map<Integer, String[]> factor;

	/* frame of each corresponding curve */
	private JFrame amperometricFrame = null;
	private JFrame gasConcentrationFrame = null;
	private JFrame gasExchangeRatesFrame = null;
	private JFrame cumulatedGasExchangeFrame = null;
	private JFrame denoisedGasExchangeRatesFrame = null;
	private JFrame denoisedCumulatedGasExchangeFrame = null;
	private JFrame o2ExchangeRatesFrame = null;
	private JFrame o2ExchangeFrame = null;
	private JFrame gasExchangeRateFunctionConcentrationFrame = null;
	private JFrame hydrogenaseActivityFrame = null;

	/* boolean to know if the corresponding frame is displayed */
	private boolean amperometricFrameOpen = false;
	private boolean gasConcentrationFrameOpen = false;
	private boolean gasExchangeRatesFrameOpen = false;
	private boolean cumulatedGasExchangeFrameOpen = false;
	private boolean denoisedGasExchangeRatesFrameOpen = false;
	private boolean denoisedCumulatedGasExchangeFrameOpen = false;
	private boolean o2ExchangeRatesFrameOpen = false;
	private boolean o2ExchangeFrameOpen = false;
	private boolean gasExchangeRateFunctionConcentrationFrameOpen = false;
	private boolean hydrogenaseActivityFrameOpen = false;

	/* logo of our program */
	private ImageIcon img;

	/* subPannel at the bottom of the window, displaying names of opened files */
	private JPanel bottom = new JPanel(new GridLayout(1, 3, 10, 10));

	/**
	 * Window constructor
	 */
	public Window() {

		/* define layout */
		setLayout(new BorderLayout());

		/* central panel, containing all buttons and fields */
		JPanel principalPanel = new JPanel(new GridLayout(2, 3));

		/* Create some new actions, used in the JMenus as button */
		openData = new OpenData("Open Data");
		OpenFactor openFactor = new OpenFactor("Open Factor");
		NormalizationFactor normalizationFactor = new NormalizationFactor("Normalization Factor");
		DisplayParameter DisplayParameter = new DisplayParameter("Molecule to Display");
		Pause pause = new Pause("Pause");
		Play play = new Play("Play");
		License license = new License("License");

		/* create the JMenus and the JMenuBar of the main window */
		JMenu file = new JMenu("File");
		JMenu edit = new JMenu("Edit");
		JMenu display = new JMenu("Display");
		JMenu about = new JMenu("About");
		JMenuBar menuBar = new JMenuBar();

		/* add the actions to the JMenu 'File' */
		file.add(openFactor);
		file.add(openData);

		/* disabled openData until factor file is loaded */
		openData.setEnabled(false);

		/* add the actions to the JMenu 'Edit' */
		edit.add(play);
		edit.add(pause);
		edit.add(normalizationFactor);

		/* add the actions to the JMenu 'Display' */
		display.add(DisplayParameter);

		/* add the action to the JMenu 'About' */
		about.add(license);

		/* add JMenu to JMenuBar and add JMenuBar to the window */
		menuBar.add(file);
		menuBar.add(edit);
		menuBar.add(display);
		menuBar.add(about);
		setJMenuBar(menuBar);

		/*
		 * create column name for o2ExchangeRates, o2Exchange and hydrogenaseActivity
		 * (the names are final and don't depend on the data)
		 */
		o2ExchangeRatesColumnName[0] = "Time (min)";
		o2ExchangeRatesColumnName[1] = "Uo (然 / min)";
		o2ExchangeRatesColumnName[2] = "Eo (然 / min)";
		o2ExchangeRatesColumnName[3] = "Net (然 / min)";

		o2ExchangeColumnName[0] = "Time (min)";
		o2ExchangeColumnName[1] = "Uo (然)";
		o2ExchangeColumnName[2] = "Eo (然)";
		o2ExchangeColumnName[3] = "Net (然)";

		hydrogenaseActivityColumnName[0] = "Time (min)";
		hydrogenaseActivityColumnName[1] = "Hydrogenase Activity  (然 / min)";

		/*
		 * get the last paths of loading factor, loading CSV and saving XLSX (if one
		 * exist)
		 */
		try {
			/* get the paths */
			BufferedReader buffRead = new BufferedReader(new FileReader("path"));
			loadPath = buffRead.readLine();
			savePath = buffRead.readLine();
			factorPath = buffRead.readLine();
			buffRead.close();

		} catch (FileNotFoundException e) {
			/*
			 * if file "Path" don't exist, will create a new file called "Path" when loading
			 * factor file
			 */
			Main.logger.info("Path file don't exist");
		} catch (IOException e) {
			Main.logger.severe(e.toString());
		}

		/*
		 * Create the button that display the amperometric Curve, define is parameters
		 * and hide it
		 */
		amperometricCurve = new JButton("Amperometric Signal");
		amperometricCurve.setVerticalTextPosition(AbstractButton.CENTER);
		amperometricCurve.setHorizontalTextPosition(AbstractButton.LEADING);
		amperometricCurve.setActionCommand("DisplayAmperometric");
		amperometricCurve.setVisible(false);

		/* Create the button that display the gasConcentration Curve */
		gasConcentrationCurve = new JButton("Gas Concentration");
		gasConcentrationCurve.setVerticalTextPosition(AbstractButton.CENTER);
		gasConcentrationCurve.setHorizontalTextPosition(AbstractButton.LEADING);
		gasConcentrationCurve.setActionCommand("DisplayGasConcentration");
		gasConcentrationCurve.setVisible(false);

		/* Create the button that display the gasExchangeRates Curve */
		gasExchangeRatesCurve = new JButton("Gas Exchange Rates");
		gasExchangeRatesCurve.setVerticalTextPosition(AbstractButton.CENTER);
		gasExchangeRatesCurve.setHorizontalTextPosition(AbstractButton.LEADING);
		gasExchangeRatesCurve.setActionCommand("DisplayGasExchangeRates");
		gasExchangeRatesCurve.setVisible(true);

		/* Create the button that display the cumulatedGasExchange Curve */
		cumulatedGasExchangeCurve = new JButton("Cumulated Gas Exchange");
		cumulatedGasExchangeCurve.setVerticalTextPosition(AbstractButton.CENTER);
		cumulatedGasExchangeCurve.setHorizontalTextPosition(AbstractButton.LEADING);
		cumulatedGasExchangeCurve.setActionCommand("DisplayCumulatedGasExchange");
		cumulatedGasExchangeCurve.setVisible(false);

		/* Create the button that display the denoisedGasExchangeRates Curve */
		denoisedGasExchangeRatesCurve = new JButton("Denoised Gas Exchange Rates");
		denoisedGasExchangeRatesCurve.setVerticalTextPosition(AbstractButton.CENTER);
		denoisedGasExchangeRatesCurve.setHorizontalTextPosition(AbstractButton.LEADING);
		denoisedGasExchangeRatesCurve.setActionCommand("DisplayDenoisedGasExchangeRates");
		denoisedGasExchangeRatesCurve.setVisible(false);

		/* Create the button that display the denoisedCumulatedGasExchange Curve */
		denoisedCumulatedGasExchangeCurve = new JButton("Denoised Cumulated Gas Exchange");
		denoisedCumulatedGasExchangeCurve.setVerticalTextPosition(AbstractButton.CENTER);
		denoisedCumulatedGasExchangeCurve.setHorizontalTextPosition(AbstractButton.LEADING);
		denoisedCumulatedGasExchangeCurve.setActionCommand("DisplayDenoisedCumulatedGasExchangeRates");
		denoisedCumulatedGasExchangeCurve.setVisible(false);

		/* Create the button that display the o2ExchangeRates Curve */
		o2ExchangeRatesCurve = new JButton("Oxygen Exchange Rates (denoised)");
		o2ExchangeRatesCurve.setVerticalTextPosition(AbstractButton.CENTER);
		o2ExchangeRatesCurve.setHorizontalTextPosition(AbstractButton.LEADING);
		o2ExchangeRatesCurve.setActionCommand("DisplayO2ExchangeRatesCurve");
		o2ExchangeRatesCurve.setVisible(false);

		/* Create the button that display the o2Exchange Curve */
		o2ExchangeCurve = new JButton("Oxygen Exchange (denoised)");
		o2ExchangeCurve.setVerticalTextPosition(AbstractButton.CENTER);
		o2ExchangeCurve.setHorizontalTextPosition(AbstractButton.LEADING);
		o2ExchangeCurve.setActionCommand("Displayo2ExchangeCurve");
		o2ExchangeCurve.setVisible(false);

		/*
		 * Create the button that display the gasExchangeRateFunctionConcentration Curve
		 */
		gasExchangeRateFunctionConcentrationCurve = new JButton("V(gas 2) = F(concentration(gas 1))");
		gasExchangeRateFunctionConcentrationCurve.setVerticalTextPosition(AbstractButton.CENTER);
		gasExchangeRateFunctionConcentrationCurve.setHorizontalTextPosition(AbstractButton.LEADING);
		gasExchangeRateFunctionConcentrationCurve.setActionCommand("DisplayGasExchangeRateFunctionConcentrationCurve");
		gasExchangeRateFunctionConcentrationCurve.setVisible(false);

		/* Create the button that display the hydrogenaseActivity Curve */
		hydrogenaseActivityCurve = new JButton("Hydrogenase Activity");
		hydrogenaseActivityCurve.setVerticalTextPosition(AbstractButton.CENTER);
		hydrogenaseActivityCurve.setHorizontalTextPosition(AbstractButton.LEADING);
		hydrogenaseActivityCurve.setActionCommand("DisplayHydrogenaseActivityCurve");
		hydrogenaseActivityCurve.setVisible(false);

		movingGasExchangeRatesAverage.setVisible(false);
		movingDenoisedGasExchangeRatesAverage.setVisible(false);
		movingO2ExchangeAverage.setVisible(false);
		gasConcentrationMoleculeList.setVisible(false);
		denoisedGasExchangeRatesMoleculeList.setVisible(false);

		/*
		 * add action listener on the buttons, so when you click on them, will display
		 * the right curve
		 */
		amperometricCurve.addActionListener(new DisplayAmperometric());
		gasConcentrationCurve.addActionListener(new DisplayGasConcentration());
		gasExchangeRatesCurve.addActionListener(new DisplayGasExchangeRates());
		cumulatedGasExchangeCurve.addActionListener(new DisplayCumulatedGasExchange());
		denoisedGasExchangeRatesCurve.addActionListener(new DisplayDenoisedGasExchangeRates());
		denoisedCumulatedGasExchangeCurve.addActionListener(new DisplayDenoisedCumulatedGasExchange());
		o2ExchangeRatesCurve.addActionListener(new DisplayO2ExchangeRatesCurve());
		o2ExchangeCurve.addActionListener(new DisplayO2ExchangeCurve());
		gasExchangeRateFunctionConcentrationCurve
				.addActionListener(new DisplayGasExchangeRateFunctionConcentrationCurve());
		hydrogenaseActivityCurve.addActionListener(new DisplayHydrogenaseActivityCurve());

		/*
		 * define a characteristic grid layout, with borders, used in each element of
		 * the principal panel, to contain two buttons (or one button and one subPanel)
		 */
		GridLayout g = new GridLayout(2, 1, 10, 10);
		/* 10 pixel of space between each column and each row */
		g.setHgap(10);
		g.setVgap(10);

		/* define the 6 subPnale contained in the central panel of our mindow */
		JPanel panel1 = new JPanel(g);
		JPanel panel2 = new JPanel(g);
		JPanel panel3 = new JPanel(g);
		JPanel panel4 = new JPanel(g);
		JPanel panel5 = new JPanel(g);
		JPanel panel6 = new JPanel(g);

		/* create border of 10 pixel */
		panel1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel3.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel4.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel5.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel6.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		/* add each panel to the central principal panel */
		principalPanel.add(panel1);
		principalPanel.add(panel2);
		principalPanel.add(panel3);
		principalPanel.add(panel4);
		principalPanel.add(panel5);
		principalPanel.add(panel6);

		/* color our panels to have a checkerboard (this is just artistic purpose) */
		panel1.setBackground(new Color(193, 226, 140));
		panel2.setBackground(new Color(183, 218, 220));
		panel3.setBackground(new Color(193, 226, 140));
		panel4.setBackground(new Color(183, 218, 220));
		panel5.setBackground(new Color(193, 226, 140));
		panel6.setBackground(new Color(183, 218, 220));

		/*
		 * then we are going to define each subPanel and add their corresponding
		 * elements
		 */

		/* first panel */
		panel1.add(amperometricCurve);
		panel1.add(gasConcentrationCurve);

		/* second panel (top central) of our principal window */
		/* field 2 contains the text and JSpinner */
		JPanel field2 = new JPanel(new FlowLayout());
		field2.add(new JLabel("Step for sliding average:"));
		field2.add(movingGasExchangeRatesAverage);
		/* then add field2 to a subpanel containing it and the first button */
		subPanel2 = new JPanel(new BorderLayout());
		subPanel2.add(field2, BorderLayout.NORTH);
		subPanel2.add(gasExchangeRatesCurve, BorderLayout.CENTER);
		subPanel2.setVisible(false);
		/* to finally add them to the "main" subpannel with the second button */
		panel2.add(subPanel2);
		panel2.add(cumulatedGasExchangeCurve);

		/* third panel (same as before) */
		JPanel field3 = new JPanel(new FlowLayout());
		field3.add(new JLabel("Step for sliding average:"));
		field3.add(movingDenoisedGasExchangeRatesAverage);

		subPanel3 = new JPanel(new BorderLayout());
		subPanel3.add(field3, BorderLayout.NORTH);
		subPanel3.add(denoisedGasExchangeRatesCurve, BorderLayout.CENTER);
		subPanel3.setVisible(false);

		panel3.add(subPanel3);
		panel3.add(denoisedCumulatedGasExchangeCurve);

		/* same as before */
		JPanel field4 = new JPanel(new FlowLayout());
		field4.add(new JLabel("Step for sliding average:"));
		field4.add(movingO2ExchangeAverage);

		subPanel4 = new JPanel(new BorderLayout());
		subPanel4.add(field4, BorderLayout.NORTH);
		subPanel4.add(o2ExchangeRatesCurve, BorderLayout.CENTER);
		subPanel4.setVisible(false);

		panel4.add(subPanel4);
		panel4.add(o2ExchangeCurve);

		/* bottom central pannel */
		/* the JPanel are two first line of the panel */
		JPanel fieldGasConcentration1 = new JPanel(new FlowLayout());
		JPanel fieldGasConcentration2 = new JPanel(new FlowLayout());
		fieldGasConcentration1.add(new JLabel("Select the molecule in abscissa (gas concentration)"));
		fieldGasConcentration2.add(new JLabel("Gas 1: "));
		fieldGasConcentration2.add(gasConcentrationMoleculeList);

		/* then this panel reunite the two */
		JPanel allFieldPanelConcentration = new JPanel(g);
		allFieldPanelConcentration.add(fieldGasConcentration1);
		allFieldPanelConcentration.add(fieldGasConcentration2);

		/* Then two last line of the panel */
		JPanel fieldDenoisedGasExchangeRates1 = new JPanel(new FlowLayout());
		JPanel fieldDenoisedGasExchangeRates2 = new JPanel(new FlowLayout());
		fieldDenoisedGasExchangeRates1.add(new JLabel("Select the molecule in ordinate (denoised gas exchange rates)"));
		fieldDenoisedGasExchangeRates2.add(new JLabel("Gas 2: "));
		fieldDenoisedGasExchangeRates2.add(denoisedGasExchangeRatesMoleculeList);

		/* then this panel reunite the two */
		JPanel allFieldPanelDenoised = new JPanel(g);
		allFieldPanelDenoised.add(fieldDenoisedGasExchangeRates1);
		allFieldPanelDenoised.add(fieldDenoisedGasExchangeRates2);

		/* then reunite the two previous panels (so all text and drop down list) */
		subPanel5 = new JPanel(g);
		subPanel5.add(allFieldPanelConcentration);
		subPanel5.add(allFieldPanelDenoised);
		subPanel5.setVisible(false);

		/* and add it two our pannel with the button */
		panel5.add(subPanel5);
		panel5.add(gasExchangeRateFunctionConcentrationCurve);

		/* the last panel containing the last button */
		panel6.add(hydrogenaseActivityCurve);

		/* Add text two the bottom line panel */
		bottom.add(new JLabel("Factor File: "));
		bottom.add(new JLabel("Data File: "));
		bottom.add(new JLabel("Normalization Factor: " + normalizationFactorValue));

		/* set icon of of our window */
		img = new ImageIcon("logo.png");
		setIconImage(img.getImage());

		/* add principal panel and bottom panel to our window */
		add(principalPanel, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);

		/* set the CheckBox to checked by default */
		h2oPresent.setSelected(true);
		ciPresent.setSelected(true);

		/* Define the popUp window displayed when some one clicked on the exit button */
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				int choice = JOptionPane.showConfirmDialog(Window.this, "Are you sure you want to quit ?", "Quit",
						JOptionPane.YES_NO_OPTION);
				if (choice == JOptionPane.YES_OPTION) {
					/* close the file handler and the window */
					Main.fh.close();
					System.exit(0);
				}

			}
		});

		/* set parameters of main window */
		setTitle("MIMS Analysis");
		pack();
		/* put the window in the center of the screen */
		setLocationRelativeTo(null);
		setVisible(true);
		Main.logger.info("Main Window Created");

	}

	/**
	 * Pause the treatment of data file, so pause the realTime curves, action in the
	 * JMenuBar of each frame
	 */
	class Pause extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public Pause(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			Main.logger.info("Paused the process");
			pause = true;
		}
	}

	/**
	 * Restart the treatment of data, action in the JMenuBar of each frame
	 */
	class Play extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public Play(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			pause = false;
			Main.logger.info("Back to Play");
		}
	}

	/**
	 * Edit the Normalization factor
	 */
	class NormalizationFactor extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private Double factor = (double) 1;

		public NormalizationFactor(String s) {
			super(s);
		}

		/*
		 * function whcih display a pop up window, until the user enter a correct value
		 * or close it
		 */
		public void ChooseFactor() {

			String choice = JOptionPane.showInputDialog(Window.this,
					"Enter the Normalization Factor (Current: " + normalizationFactorValue + ").");

			if (choice != null && choice.length() > 0) {
				try {
					factor = Double.parseDouble(choice);
				} /* not a number entered */
				catch (NumberFormatException e) {
					infoBox(Window.this,
							"There is an issue with your entered value: " + "\n" + "Please enter a number !",
							"Normalization Factor");
					ChooseFactor();

				}
				/* number =0 */
				if (factor == 0) {
					infoBox(Window.this,
							"There is an issue with your entered value: " + "\n" + "Normalization factor can't be 0",
							"Normalization Factor");
					ChooseFactor();
				}

			}
		}

		public void actionPerformed(ActionEvent e) {
			/* popup window */
			ChooseFactor();
			/* edit factor value if needed */
			if (normalizationFactorValue != factor) {
				normalizationFactorValue = factor;
				Main.logger.info("Changed Normalization Factor to " + normalizationFactorValue);
				bottom.remove(2);
				bottom.add(new JLabel("Normalization Factor: " + normalizationFactorValue));
				Window.this.revalidate();

				/* if factor file already opened, relaunch the process and reinitialize data */
				if (dataFilePath != null) {
					getData(dataFilePath);
				}
			}

		}

	}

	/**
	 * Change displayParameters (display H2o and Ci or not)
	 */
	class DisplayParameter extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public DisplayParameter(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			/* get on which button the user clicked */
			int value = JOptionPane.showOptionDialog(Window.this, "Display Molecules", "Displaying",
					JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					new Object[] { h2oPresent, ciPresent, "OK", "CANCEL" }, "CANCEL");
			/* if clicked on "OK" */
			if (value == 2) {
				/* check if the user changed the state of one checkboxes */
				if (ciT != ciPresent.isSelected() || h2oT != h2oPresent.isSelected()) {
					/* change the value of boolean */
					ciT = ciPresent.isSelected();
					h2oT = h2oPresent.isSelected();

					/* if factor file already opened, relaunch the process and reinitialize data */
					if (dataFilePath != null) {
						getData(dataFilePath);
					}
				}

			}

		}

	}

	/**
	 * Display License of the program
	 */
	class License extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public License(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			/* pop up window */
			JOptionPane.showMessageDialog(Window.this, "Copyright (C) 2019 - F.Burlacot, CEA Cadarache, France" + "\n"
					+ "\n"
					+ "This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3."
					+ "\n"

					+ "This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details."
					+ "\n"

					+ "You should have received a copy of the GNU General Public License along with this program. If not, see: https://www.gnu.org/licenses/."
					+ "\n" + "\n" + "Version 1.0.1", "License", JOptionPane.INFORMATION_MESSAGE);
		}

	}

	/**
	 * Open the csv factor file and get data, action in the JMenuBar of each frame
	 */
	class OpenFactor extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public OpenFactor(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			/* Create the filechooser with parameters and filter */
			JFileChooser fc = new JFileChooser(factorPath);
			fc.setDialogTitle("Open the factors");
			fc.addChoosableFileFilter(new CsvFilter());
			fc.setAcceptAllFileFilterUsed(false);

			/* if the user choose a working csv */
			if (fc.showOpenDialog(Window.this) == JFileChooser.APPROVE_OPTION) {

				Main.logger.info("Opening Factor File");

				/* reset datafilePath in case we open a csv for the second time (or more) */
				dataFilePath = null;

				/* reset factor */
				factor = null;
				factor = new HashMap<>();

				/* stop every thread in case we load factors for the second time (or more) */
				working = false;

				/*
				 * Hide every button and subPanel until data file is opened (in next function)
				 */
				amperometricCurve.setVisible(false);
				gasConcentrationCurve.setVisible(false);
				gasExchangeRatesCurve.setVisible(false);
				movingGasExchangeRatesAverage.setVisible(false);
				cumulatedGasExchangeCurve.setVisible(false);
				denoisedGasExchangeRatesCurve.setVisible(false);
				movingDenoisedGasExchangeRatesAverage.setVisible(false);
				denoisedCumulatedGasExchangeCurve.setVisible(false);
				o2ExchangeRatesCurve.setVisible(false);
				movingO2ExchangeAverage.setVisible(false);
				o2ExchangeCurve.setVisible(false);
				gasConcentrationMoleculeList.setVisible(false);
				denoisedGasExchangeRatesMoleculeList.setVisible(false);
				gasExchangeRateFunctionConcentrationCurve.setVisible(false);
				hydrogenaseActivityCurve.setVisible(false);
				subPanel2.setVisible(false);
				subPanel3.setVisible(false);
				subPanel4.setVisible(false);
				subPanel5.setVisible(false);

				/* disabled openData until factor file is loaded */
				openData.setEnabled(false);

				/* close every chart in case some are already opened */
				if (amperometricFrame != null) {
					amperometricFrame.dispatchEvent(new WindowEvent(amperometricFrame, WindowEvent.WINDOW_CLOSING));
				}
				if (gasConcentrationFrame != null) {
					gasConcentrationFrame
							.dispatchEvent(new WindowEvent(gasConcentrationFrame, WindowEvent.WINDOW_CLOSING));
				}
				if (gasExchangeRatesFrame != null) {
					gasExchangeRatesFrame
							.dispatchEvent(new WindowEvent(gasExchangeRatesFrame, WindowEvent.WINDOW_CLOSING));
				}
				if (cumulatedGasExchangeFrame != null) {
					cumulatedGasExchangeFrame
							.dispatchEvent(new WindowEvent(cumulatedGasExchangeFrame, WindowEvent.WINDOW_CLOSING));
				}
				if (denoisedGasExchangeRatesFrame != null) {
					denoisedGasExchangeRatesFrame
							.dispatchEvent(new WindowEvent(denoisedGasExchangeRatesFrame, WindowEvent.WINDOW_CLOSING));
				}
				if (denoisedCumulatedGasExchangeFrame != null) {
					denoisedCumulatedGasExchangeFrame.dispatchEvent(
							new WindowEvent(denoisedCumulatedGasExchangeFrame, WindowEvent.WINDOW_CLOSING));
				}
				if (o2ExchangeRatesFrame != null) {
					o2ExchangeRatesFrame
							.dispatchEvent(new WindowEvent(o2ExchangeRatesFrame, WindowEvent.WINDOW_CLOSING));
				}

				if (o2ExchangeFrame != null) {
					o2ExchangeFrame.dispatchEvent(new WindowEvent(o2ExchangeFrame, WindowEvent.WINDOW_CLOSING));
				}

				if (gasExchangeRateFunctionConcentrationFrame != null) {
					gasExchangeRateFunctionConcentrationFrame.dispatchEvent(
							new WindowEvent(gasExchangeRateFunctionConcentrationFrame, WindowEvent.WINDOW_CLOSING));
				}
				if (hydrogenaseActivityFrame != null) {
					hydrogenaseActivityFrame
							.dispatchEvent(new WindowEvent(hydrogenaseActivityFrame, WindowEvent.WINDOW_CLOSING));
				}

				/* reset frames */
				amperometricFrame = null;
				gasConcentrationFrame = null;
				gasExchangeRatesFrame = null;
				cumulatedGasExchangeFrame = null;
				denoisedGasExchangeRatesFrame = null;
				denoisedCumulatedGasExchangeFrame = null;
				o2ExchangeRatesFrame = null;
				o2ExchangeFrame = null;
				gasExchangeRateFunctionConcentrationFrame = null;
				hydrogenaseActivityFrame = null;

				/*
				 * create a new thread which will wait wait for previous thread to stopped (in
				 * case we loaded factor for a second time or more)
				 */
				Thread t = new Thread() {
					public void run() {
						try {
							/* wait for each thread in threads to stopped */
							for (Thread thread : threads) {
								thread.join();
							}
						} catch (InterruptedException e) {
							Main.logger.severe(e.toString());
						}

						/* get the address of the file selected by the user */
						String workingAddress = fc.getSelectedFile().getAbsolutePath();

						/* reset bottom of our JFrame */
						bottom.remove(2);
						bottom.remove(1);
						bottom.remove(0);
						/* edit the bottom of our window */
						bottom.add(new JLabel("Factor File: " + fc.getSelectedFile().getName()));
						bottom.add(new JLabel("Data File: "));
						bottom.add(new JLabel("Normalization Factor: " + normalizationFactorValue));
						/* refresh our window */
						Window.this.revalidate();
						/*
						 * edit the address of the last opened factor if folder have changed (we check
						 * if the factorPath is different than this path, or if factorPath was null)
						 */
						if (factorPath != null && factorPath.contentEquals(fc.getSelectedFile().getParent()) == false
								|| factorPath == null) {
							factorPath = fc.getSelectedFile().getParent();
							try {
								/* if so, update the Path file (or create it if the file doesn't exist) */
								BufferedWriter buffWrite = new BufferedWriter(new FileWriter("path"));
								if (loadPath != null) {
									buffWrite.write(savePath);
								}
								buffWrite.write('\n');
								if (savePath != null) {
									buffWrite.write(savePath);
								}
								buffWrite.write('\n');
								buffWrite.write(factorPath);
								buffWrite.close();
							}

							catch (IOException e3) {
								Main.logger.severe(e3.toString());
							}
						}
						/* read the file and get the data */
						try {
							BufferedReader fichier = new BufferedReader(new FileReader(workingAddress));
							String line;
							String[] workingLine;
							/* key is the first cell of each row, and represent an element (M/Z) */
							int key;

							/*
							 * delete the first line of factor file (because only contains name of columns,
							 * not important data)
							 */
							fichier.readLine();

							/* get the data */
							while ((line = fichier.readLine()) != null) {
								/* correct line is data after a little treatment */
								String[] correctLine = new String[8];
								/*
								 * transform each row into a list of elements, each cell is one element of the
								 * list
								 */
								workingLine = line.replaceAll(",", ".").split(";");
								/* get the key, which will be the key of factor map */
								key = Integer.parseInt(workingLine[0]);
								for (int i = 0; i < 8; i++) {
									/* check if a value is entered in the corresponding cell of factor file */
									if (workingLine.length > i + 1 && workingLine[i + 1] != "") {
										correctLine[i] = workingLine[i + 1];
									}

									else {
										/* if nothing entered, just set it to 1 */
										correctLine[i] = "1";
									}

								}
								/*
								 * If the key is already in the map (duplicate, two elements with same M/Z), add
								 * the data to the map at key: -M/Z
								 */
								if (factor.containsKey(key)) {
									factor.put(-key, correctLine);
								} else {
									factor.put(key, correctLine);
								}
							}
							fichier.close();

							/* enabled button to load data */
							openData.setEnabled(true);
							Main.logger.info("Factor File Oppened");

						} catch (IOException | NumberFormatException e) {
							/* Warn user that he didn't load a correct factor file */
							Main.logger.warning(e.toString());
							infoBox(Window.this,
									"There is an issue with your factor file: " + "\n"
											+ "Please verify that you use the correct format of factor file",
									"Opening Factor File");
							return;
						}
					}

				};
				t.start();
			}
		}
	}

	/**
	 * Open the working CSV and get the data, action in the JMenuBar
	 */
	class OpenData extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public OpenData(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			/* Create the filechooser with parameters and filter */
			JFileChooser fc = new JFileChooser(loadPath);
			fc.setDialogTitle("Open the working csv");
			fc.addChoosableFileFilter(new CsvFilter());
			fc.setAcceptAllFileFilterUsed(false);

			/* if the user choose a working csv */
			if (fc.showOpenDialog(Window.this) == JFileChooser.APPROVE_OPTION) {

				Main.logger.info("Opening Data File");

				/* get the address of the file selected by the user */
				String workingAddress = fc.getSelectedFile().getAbsolutePath();
				dataFilePath = workingAddress;

				/* reset file name in bottom of our JFrame */
				bottom.remove(2);
				bottom.remove(1);
				/* add file name */
				bottom.add(new JLabel("Data File: " + fc.getSelectedFile().getName()));
				bottom.add(new JLabel("Normalization Factor: " + normalizationFactorValue));
				Window.this.revalidate();

				/* edit the address of the last opened csv directory if necessary */
				if (loadPath != null && loadPath.contentEquals(fc.getSelectedFile().getParent()) == false
						|| loadPath == null) {
					loadPath = fc.getSelectedFile().getParent();
					try {
						BufferedWriter buffWrite = new BufferedWriter(new FileWriter("path"));
						buffWrite.write(loadPath);
						buffWrite.write('\n');
						/* not sure that savePath isn't null */
						if (savePath != null) {
							buffWrite.write(savePath);
						}
						buffWrite.write('\n');
						buffWrite.write(factorPath);
						buffWrite.close();
					}

					catch (IOException e3) {
						Main.logger.severe(e.toString());
					}
				}
				/* launch getData, which will continuously check for new data */
				getData(workingAddress);
			}

		}
	}

	/**
	 * Function which will check for new data, and update each List<double[]>
	 * 
	 * @param address:
	 *            address of the current data file
	 */
	private void getData(String address) {

		String workingAddress = address;

		/* reset data in case we open a csv for the second time (or more) */
		amperometricData = null;
		amperometricColumnName = null;

		gasConcentrationColumnName = null;
		gasConcentrationData = null;
		gasConcentrationCorrection = null;

		gasExchangeRatesData = null;
		gasExchangeRatesColumnName = null;

		cumulatedGasExchangeData = null;

		denoisedGasExchangeRatesData = null;
		denoisedGasExchangeRatesColumnName = null;

		denoisedCumulatedGasExchangeData = null;
		denoisedCumulatedGasExchangeColumnName = null;

		o2ExchangeRatesData = null;
		o2ExchangeData = null;

		gasExchangeRateFunctionConcentrationData = null;
		gasExchangeRateFunctionConcentrationColumnName = null;

		hydrogenaseActivityData = null;

		/* reset list of molecules to display for the v(gas)=f(c(gas)) */
		gasConcentrationMoleculeList.removeAllItems();
		denoisedGasExchangeRatesMoleculeList.removeAllItems();

		/* set data */
		amperometricData = new ArrayList<Double[]>();
		gasConcentrationData = new ArrayList<Double[]>();
		gasConcentrationCorrection = new ArrayList<Double>();
		gasExchangeRatesData = new ArrayList<Double[]>();
		gasConcentrationConsumption = new ArrayList<Double>();
		cumulatedGasExchangeData = new ArrayList<Double[]>();
		denoisedGasExchangeRatesData = new ArrayList<Double[]>();
		denoisedCumulatedGasExchangeData = new ArrayList<Double[]>();
		o2ExchangeRatesData = new ArrayList<Double[]>();
		o2ExchangeData = new ArrayList<Double[]>();
		gasExchangeRateFunctionConcentrationData = new ArrayList<Double[]>();
		hydrogenaseActivityData = new ArrayList<Double[]>();

		/*
		 * Hide every button and subPanel if we load the csv for the second time (or
		 * more)
		 */
		amperometricCurve.setVisible(false);
		gasConcentrationCurve.setVisible(false);
		gasExchangeRatesCurve.setVisible(false);
		movingGasExchangeRatesAverage.setVisible(false);
		cumulatedGasExchangeCurve.setVisible(false);
		denoisedGasExchangeRatesCurve.setVisible(false);
		movingDenoisedGasExchangeRatesAverage.setVisible(false);
		denoisedCumulatedGasExchangeCurve.setVisible(false);
		o2ExchangeRatesCurve.setVisible(false);
		movingO2ExchangeAverage.setVisible(false);
		o2ExchangeCurve.setVisible(false);
		gasConcentrationMoleculeList.setVisible(false);
		denoisedGasExchangeRatesMoleculeList.setVisible(false);
		gasExchangeRateFunctionConcentrationCurve.setVisible(false);
		hydrogenaseActivityCurve.setVisible(false);
		subPanel2.setVisible(false);
		subPanel3.setVisible(false);
		subPanel4.setVisible(false);
		subPanel5.setVisible(false);

		/* close every chart in case some are already opened */
		if (amperometricFrame != null) {
			amperometricFrame.dispatchEvent(new WindowEvent(amperometricFrame, WindowEvent.WINDOW_CLOSING));
		}
		if (gasConcentrationFrame != null) {
			gasConcentrationFrame.dispatchEvent(new WindowEvent(gasConcentrationFrame, WindowEvent.WINDOW_CLOSING));
		}
		if (gasExchangeRatesFrame != null) {
			gasExchangeRatesFrame.dispatchEvent(new WindowEvent(gasExchangeRatesFrame, WindowEvent.WINDOW_CLOSING));
		}
		if (cumulatedGasExchangeFrame != null) {
			cumulatedGasExchangeFrame
					.dispatchEvent(new WindowEvent(cumulatedGasExchangeFrame, WindowEvent.WINDOW_CLOSING));
		}
		if (denoisedGasExchangeRatesFrame != null) {
			denoisedGasExchangeRatesFrame
					.dispatchEvent(new WindowEvent(denoisedGasExchangeRatesFrame, WindowEvent.WINDOW_CLOSING));
		}
		if (denoisedCumulatedGasExchangeFrame != null) {
			denoisedCumulatedGasExchangeFrame
					.dispatchEvent(new WindowEvent(denoisedCumulatedGasExchangeFrame, WindowEvent.WINDOW_CLOSING));
		}
		if (o2ExchangeRatesFrame != null) {
			o2ExchangeRatesFrame.dispatchEvent(new WindowEvent(o2ExchangeRatesFrame, WindowEvent.WINDOW_CLOSING));
		}
		if (o2ExchangeFrame != null) {
			o2ExchangeFrame.dispatchEvent(new WindowEvent(o2ExchangeFrame, WindowEvent.WINDOW_CLOSING));
		}
		if (gasExchangeRateFunctionConcentrationFrame != null) {
			gasExchangeRateFunctionConcentrationFrame.dispatchEvent(
					new WindowEvent(gasExchangeRateFunctionConcentrationFrame, WindowEvent.WINDOW_CLOSING));
		}
		if (hydrogenaseActivityFrame != null) {
			hydrogenaseActivityFrame
					.dispatchEvent(new WindowEvent(hydrogenaseActivityFrame, WindowEvent.WINDOW_CLOSING));
		}

		/* reset frames */
		amperometricFrame = null;
		gasConcentrationFrame = null;
		gasExchangeRatesFrame = null;
		cumulatedGasExchangeFrame = null;
		denoisedGasExchangeRatesFrame = null;
		denoisedCumulatedGasExchangeFrame = null;
		o2ExchangeRatesFrame = null;
		o2ExchangeFrame = null;
		gasExchangeRateFunctionConcentrationFrame = null;
		hydrogenaseActivityFrame = null;

		/* create a new thread, it will check constantly if data are added to the csv */
		Thread t = new Thread() {
			public void run() {
				/*
				 * working serves to stop the previous threads
				 */
				working = false;
				try {
					/* wait until all the previous threads had stopped */
					for (Thread thread : threads) {
						/* for each thread different from this new one */
						if (thread != this) {
							thread.join();
						}
					}
				} catch (InterruptedException e) {
					Main.logger.severe(e.toString());
				}

				/* back to work */
				working = true;

				/* read the file and get the data */
				try {
					BufferedReader fichier = new BufferedReader(new FileReader(workingAddress));
					String line;
					String[] workingLine;

					/* check if we have 12 or 27 for M/Z */
					boolean presence12 = false;
					boolean presence27 = false;

					/* the mass for denoised calculus */
					boolean presenceMass = false;
					int mass = 0;
					try {
						/*
						 * try to get the mass for denoised calculus, it's supposed to be the 7th column
						 * of our factor file, associated with the the row M/Z=2
						 */
						mass = Integer.parseInt(factor.get(2)[7]);
					} /* if the mass isn't empty or a number, stopped everything and warn the user */
					catch (NumberFormatException e) {
						Main.logger.warning(e.toString());
						infoBox(Window.this, "There is an issue with your mass for denoised calculus " + "\n"
								+ "Please verify that the mass is an interger, in the (Mass=2)row, and in the 9th column of the factor file"
								+ "\n" + "If you don't want denoised calculus, let this cell empty", "Mass Definition");
						try {
							fichier.close();
						} catch (IOException e1) {
							Main.logger.severe(e1.toString());
						}
						return;
					}
					int indexMass = 0;

					/* check if we have 32 or 36 for M/Z (for o2Exchangerates and o2Exchange) */
					boolean presence32 = false;
					/*
					 * index represent the column of the molecule in gasExchangeRates and
					 * gasConcentration for further calculus
					 */
					int indexgasExchangeRates32 = 0;
					int indexgasConcentration32 = 0;
					boolean presence36 = false;
					int indexgasExchangeRates36 = 0;
					int indexgasConcentration36 = 0;

					/* check if we have 2,3and4 for M/Z, used then for hydrogenase activity */
					boolean presence2 = false;
					int index2 = 0;
					boolean presence3 = false;
					int index3 = 0;
					boolean presence4 = false;
					int index4 = 0;

					try {
						/* Get the first row, which is the list of (M/Z) */
						line = fichier.readLine();
						/* replace first serve to delete the column "$Flag$ which is empty */
						workingLine = line.replaceFirst(";", "").replaceAll(",", ".").split(";");

						/*
						 * contains the list of M/Z value + time (the -1 is here to delete the last
						 * column, which is empty)
						 */
						amperometricColumnName = new String[workingLine.length - 1];

						/* contains the list of elements (molecules) + time */
						ArrayList<String> listElement = new ArrayList<String>();

						/* set the first row to Time (min) (because workingLine[0]="Time$Flags$") */
						listElement.add("Time (min)");

						/* j serves so that i-j is the index for gasConcentrationData and others */
						int j = 0;

						Main.logger.info("Try to read first line of data file, to get factor file info");
						for (int i = 1; i < amperometricColumnName.length; i++) {
							/* delete "" surrounding our amperometric values */
							String currentMZ = workingLine[i].replaceAll("\"", "");
							/* add M/Z to amperometricColumnName */
							amperometricColumnName[i] = currentMZ;

							/* get the value of M/Z to get our list of molecules */
							int key = Integer.parseInt(currentMZ);

							/*
							 * if we find a key corresponding to our denoised mass (mass can't be 18,12 or
							 * 27)
							 */
							if (key == mass && key != 18 && key != 12 && key != 27) {
								/* note the index */
								indexMass = i - j;
								presenceMass = true;
								/*
								 * don't had the mass to listElement because it will be added at the end (else
								 * condition), because this "if" is not part of all further (else if) conditions
								 */
							}

							if (key == 12) {
								/* note that we have M/Z=12 and do nothing (used further) */
								presence12 = true;
								/* j++ because we "skip one column" */
								j++;

							} else if (key == 27) {
								/* note that we have M/Z=12 and do nothing (used further) */
								presence27 = true;
								j++;
							}
							/* if we have 12 and 44, it means that we have a CO2 and N2O */
							else if (key == 44 && presence12) {
								listElement.add("CO2");
								listElement.add("N2O");

								/* j-- because we write two column in "surplus" */
								j--;

								/*
								 * gasConcentrationCorrection is used to obtain gasConcentrationData, each row
								 * contains the factor C_max/((A_max-A_0)) of the corresponding molecule, for
								 * the formula C(t)=(A(t)-A_0)聃_max/((A_max-A_0))
								 * 
								 * gasConcentrationConcumption contain the factor k, used to obtain
								 * gasExchangeRatesData (v(t)=(delta(C(t)))/delta(t)-k聃(t))
								 */

								/*
								 * we had the corresponding factors for the corresponding molecules to our
								 * arrayList
								 */

								gasConcentrationCorrection.add(
										Double.parseDouble(factor.get(key)[1]) / (Double.parseDouble(factor.get(key)[2])
												- Double.parseDouble(factor.get(key)[3])));

								gasConcentrationConsumption.add(Double.parseDouble(factor.get(key)[4]));
								gasConcentrationCorrection.add(Double.parseDouble(factor.get(-key)[1])
										/ (Double.parseDouble(factor.get(-key)[2])
												- Double.parseDouble(factor.get(-key)[3])));
								gasConcentrationConsumption.add(Double.parseDouble(factor.get(-key)[4]));
								if (ciT) {
									listElement.add("Ci");
									j--;
									/*
									 * Ci=Cco2*(1+exp((-6.4+pH)*ln(10))*(1+exp((-10.3+pH)*ln(10)))), so get
									 * 1+exp((-6.4+pH)*ln(10))*(1+exp((-10.3+pH)*ln(10)))
									 */
									gasConcentrationCorrection
											.add(1 + Math.exp((Double.parseDouble(factor.get(2)[6]) - 6.4)*Math.log(10))
													* (1 + Math.exp((Double.parseDouble(factor.get(2)[6]) - 10.3)*Math.log(10))));
									gasConcentrationConsumption.add(Double.parseDouble(factor.get(key)[4]));
								}

							} /* if we have 44 but not 12 (this condition is here for carbon inorganic) */
							else if (key == 44 && presence12 == false) {
								listElement.add(factor.get(key)[0]);
								gasConcentrationCorrection.add(
										Double.parseDouble(factor.get(key)[1]) / (Double.parseDouble(factor.get(key)[2])
												- Double.parseDouble(factor.get(key)[3])));
								gasConcentrationConsumption.add(Double.parseDouble(factor.get(key)[4]));
								if (ciT) {
									listElement.add("Ci");
									/*
									 * Ci=Cco2*(1+exp((-6.4+pH)*ln(10))*(1+exp((-10.3+pH)*ln(10)))), so get
									 * 1+exp((-6.4+pH)*ln(10))*(1+exp((-10.3+pH)*ln(10)))
									 */
									gasConcentrationCorrection
											.add(1 + Math.exp((Double.parseDouble(factor.get(2)[6]) - 6.4)*Math.log(10))
													* (1 + Math.exp((Double.parseDouble(factor.get(2)[6]) - 10.3)*Math.log(10))));
									gasConcentrationConsumption.add(Double.parseDouble(factor.get(key)[4]));
									j--;
								}
							}
							/* if we have 31 and 27, it means that we have a ETOH and METOH */
							else if (key == 31 && presence27) {
								listElement.add("ETOH");
								listElement.add("METOH");
								gasConcentrationCorrection.add(
										Double.parseDouble(factor.get(key)[1]) / (Double.parseDouble(factor.get(key)[2])
												- Double.parseDouble(factor.get(key)[3])));
								gasConcentrationConsumption.add(Double.parseDouble(factor.get(key)[4]));
								/* METOH info is saved in the map fator at -key */
								gasConcentrationCorrection.add(Double.parseDouble(factor.get(-key)[1])
										/ (Double.parseDouble(factor.get(-key)[2])
												- Double.parseDouble(factor.get(-key)[3])));
								gasConcentrationConsumption.add(Double.parseDouble(factor.get(-key)[4]));
								j--;

							}
							/*
							 * if H2O do nothing (delete H2O, unless the user said so with h2oT=false)
							 */
							else if (key == 18 && h2oT) {
								j++;
							}

							/* in any other case */
							else {
								/* for o2 and hydrogenase we want to know if we have (32&36) and (2&3&4) */
								if (key == 32) {
									indexgasConcentration32 = i - j;
									presence32 = true;
								}
								if (key == 36) {
									indexgasConcentration36 = i - j;
									presence36 = true;
								}
								if (key == 2) {
									index2 = i - j;
									presence2 = true;
								}
								if (key == 3) {
									index3 = i - j;
									presence3 = true;
								}

								if (key == 4) {
									index4 = i - j;
									presence4 = true;
								}

								/* We had the element */
								listElement.add(factor.get(key)[0]);
								gasConcentrationCorrection.add(
										Double.parseDouble(factor.get(key)[1]) / (Double.parseDouble(factor.get(key)[2])
												- Double.parseDouble(factor.get(key)[3])));
								gasConcentrationConsumption.add(Double.parseDouble(factor.get(key)[4]));
							}
						}

						/* create and edit the ColumnName for each Curve */
						gasConcentrationColumnName = new String[listElement.size()];
						gasExchangeRatesColumnName = new String[listElement.size()];

						/*-1 is here do delete the mass*/
						denoisedGasExchangeRatesColumnName = new String[listElement.size() - 1];
						denoisedCumulatedGasExchangeColumnName = new String[listElement.size() - 1];

						/* set first columnName (Time (min)) */
						amperometricColumnName[0] = listElement.get(0);
						gasConcentrationColumnName[0] = listElement.get(0);
						gasExchangeRatesColumnName[0] = listElement.get(0);
						denoisedGasExchangeRatesColumnName[0] = listElement.get(0);
						denoisedCumulatedGasExchangeColumnName[0] = listElement.get(0);

						int k = 1;
						for (int i = 1; i < listElement.size(); i++) {
							gasConcentrationColumnName[i] = listElement.get(i) + "  (然)";
							gasExchangeRatesColumnName[i] = listElement.get(i) + "  (然 / min)";
							gasConcentrationMoleculeList.addItem(listElement.get(i));

							if (presenceMass) {
								/* not display the mass, for denoised curves */
								if (i != indexMass) {
									denoisedGasExchangeRatesColumnName[k] = listElement.get(i) + "  (然 / min)";
									denoisedCumulatedGasExchangeColumnName[k] = listElement.get(i) + "  (然)";
									denoisedGasExchangeRatesMoleculeList.addItem(listElement.get(i));
									k++;
								}

							}
						}
						/*
						 * update index of other elements, because we "delete" the mass column, so index
						 * of 32 and 36 for gasExchangeRate might have changed (2,3,and 4 can't)
						 */
						if (presenceMass && presence32 && presence36) {
							if (indexMass < indexgasConcentration32) {
								indexgasExchangeRates32 = indexgasConcentration32 - 1;
							}
							if (indexMass < indexgasConcentration36) {
								indexgasExchangeRates36 = indexgasConcentration36 - 1;
							}
							denoisedGasExchangeRatesMoleculeList.addItem("Oxygen Exchange Rates");
						}

						/* get the number of amperometric signals and nb of molecules (+time) */
						nbAmperometricColumn = amperometricColumnName.length;
						nbMoleculeColumn = gasConcentrationColumnName.length;

						/* get the second row of our data and treat it */
						line = fichier.readLine();
						workingLine = line.replaceFirst(";", "").replaceAll(",", ".").split(";");

						/* get the first date of the first value and set it to reference */
						Date refDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.S").parse(workingLine[0]);

						/* create line for each data */
						Double[] amperometricLine = new Double[nbAmperometricColumn];
						Double[] gasConcentrationLine = new Double[nbMoleculeColumn];
						Double[] gasExchangeRatesLine = new Double[nbMoleculeColumn];
						Double[] cumulatedGasExchangeRatesLine = new Double[nbMoleculeColumn];
						Double[] denoisedGasExchangeRatesLine = new Double[nbMoleculeColumn - 1];
						Double[] denoisedCumulatedGasExchangeRatesLine = new Double[nbMoleculeColumn - 1];
						Double[] o2ExchangeRatesLine = new Double[4];
						Double[] o2ExchangeLine = new Double[4];
						Double[] hydrogenaseActivityLine = new Double[2];

						/* i-j will be the index again */
						j = 0;

						/* value to store amperometric value of CO2, ETOH and NO */
						double valueCO2 = 0;
						double valueETOH = 0;
						double valueNO = 0;
						int presence30 = -1;
						/* treat every data */
						Main.logger.info("Try to read second line of data file, raw data");
						for (int i = 0; i < nbAmperometricColumn; i++) {

							/* get current time for the row (in min) */
							if (i == 0) {
								Date date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.S").parse(workingLine[0]);
								amperometricLine[i] = (((double) date.getTime() - (double) refDate.getTime()) / 1000
										/ 60);
								gasConcentrationLine[i] = amperometricLine[i];

							} else {
								/* just add the amperometric value to amperometricData */
								amperometricLine[i] = Double.parseDouble(workingLine[i]);

								/* A_(N_2 O) (44)=A(44)-(A_(CO_2 )(12))/0.0871 */
								if (amperometricColumnName[i].startsWith("12")) {
									/*
									 * keep the amperometric value in memory and jump to next column without adding
									 * anything to gasConcentration data (represent (A_(CO_2 )(12))/0.0871)
									 */
									valueCO2 = amperometricLine[i] / Double.parseDouble(factor.get(44)[5]);
									/*
									 * increase j, so the index of gasConcentration data (i-j) will remain the same
									 * next time
									 */
									j++;

									/* A_(ETOH) (31)=A(31)-(A_((METOH)) (27))/0.0871 */
								} else if (amperometricColumnName[i].startsWith("27")) {
									/* same idea */
									valueETOH = amperometricLine[i] / Double.parseDouble(factor.get(31)[5]);
									j++;
								}
								/*
								 * if we have 12 we store the value A(30) and the index because we need the
								 * value of CO2 to get A(NO)(30)
								 */
								else if (amperometricColumnName[i].startsWith("30") && presence12) {
									valueNO = amperometricLine[i];
									/* store the index of element 30 */
									presence30 = i - j;

								} /* if H2O, do nothing (delete H2O if h2oT) */
								else if (amperometricColumnName[i].startsWith("18") && h2oT) {
									j++;
								}
								/* add the two element with their corresponding value to the correct index */
								else if (amperometricColumnName[i].startsWith("31") && presence27) {

									/* A_(ETOH) (31)=(A_(ETOH)(27))/0.2241 */

									gasConcentrationLine[i - j] = ((valueETOH) - Double.parseDouble(factor.get(31)[3]))
											* gasConcentrationCorrection.get(i - j - 1);
									/*
									 * gasConcentrationCorrection.get(i - j - 1) is here because
									 * C(t)=(A(t)-A(0))聃_max/((A_max-A_0))
									 */

									/* decrease j (so increase i-j) */
									j--;

									/* A_(METOH)(31)=A(27)-(A_((METOH))(27))//0.2241 */
									gasConcentrationLine[i - j] = ((amperometricLine[i] - valueETOH)
											- Double.parseDouble(factor.get(-31)[3]))
											* gasConcentrationCorrection.get(i - j - 1);

								} else if (amperometricColumnName[i].startsWith("44") && presence12) {
									/* same than above but with A_(N_2 O) (44)=A(44)-(A_(CO_2 )(12))/0.0871 */
									gasConcentrationLine[i - j] = (valueCO2 - Double.parseDouble(factor.get(44)[3]))
											* gasConcentrationCorrection.get(i - j - 1);
									j--;
									gasConcentrationLine[i - j] = ((amperometricLine[i] - valueCO2)
											- Double.parseDouble(factor.get(-44)[3]))
											* gasConcentrationCorrection.get(i - j - 1);

									if (presence30 != -1) {
										/*
										 * if we have 12 and 30, edit the value of NO (we do that because we need the
										  * value of element N2O) : A_(NO) (30)=(A_(30)-A(N2O)(44)*0.311)
										 */
										gasConcentrationLine[presence30] = ((valueNO
												- (amperometricLine[i] - valueCO2)
														* Double.parseDouble(factor.get(30)[5]))
												- Double.parseDouble(factor.get(30)[3]))
												* gasConcentrationCorrection.get(presence30 - 1);										
									}
									/* if ciT choose by the user */
									if (ciT) {
										j--;
										/*
										 * add carbon inorganic value Ci=Cco2*Cco2*(1+exp((-6.4+pH)*ln(10))*(1+exp((-10.3+pH)*ln(10)))) and
										 * gasConcentrationCorrection.get(i - j - 1)=1+exp((-6.4+pH)*ln(10))*(1+exp((-10.3+pH)*ln(10)))
										 */
										gasConcentrationLine[i - j] = gasConcentrationLine[i - j - 2]
												* gasConcentrationCorrection.get(i - j - 1);
									}

								} /* add this condition for Ci if we don't have 12 */
								else if (amperometricColumnName[i].startsWith("44") && presence12 == false) {
									/* add CO2 */
									gasConcentrationLine[i
											- j] = (amperometricLine[i] - Double.parseDouble(factor.get(44)[3]))
													* gasConcentrationCorrection.get(i - j - 1);
									if (ciT) {
										j--;
										/*
										 * add carbon inorganic value Ci=Cco2*(1+exp((-6.4+pH)*ln(10))*(1+exp((-10.3+pH)*ln(10)))) and
										 * gasConcentrationCorrection.get(i - j - 1)=1+exp((-6.4+pH)*ln(10))*(1+exp((-10.3+pH)*ln(10)))
										 */
										gasConcentrationLine[i - j] = gasConcentrationLine[i - j - 1]
												* gasConcentrationCorrection.get(i - j - 1);
									}

								} else {
									/* just add the value in the dataset */
									gasConcentrationLine[i - j] = (amperometricLine[i] - Double
											.parseDouble(factor.get(Integer.parseInt(amperometricColumnName[i]))[3]))
											* gasConcentrationCorrection.get(i - j - 1);

								}
							}
						}
						/* c(i)=c(i-1) + v(i-1)*(ti-ti-1) but c0= C0 */
						cumulatedGasExchangeRatesLine = gasConcentrationLine;

						/* edit the number of row treated */
						nbRow = 1;

						/* add data to the corresponding ArrayList */
						amperometricData.add(amperometricLine);
						gasConcentrationData.add(gasConcentrationLine);
						cumulatedGasExchangeData.add(cumulatedGasExchangeRatesLine);

						gasConcentrationMoleculeList.setVisible(true);
						denoisedGasExchangeRatesMoleculeList.setVisible(true);

						if (presenceMass) {
							/* calculate denoised cumulated gas exchange */
							k = 0;
							for (int i = 0; i < nbMoleculeColumn; i++) {
								if (i != indexMass) {
									/* cd(i)=cd(i-1) + vd(i-1)*(ti-ti-1) but cd0= C0 */
									denoisedCumulatedGasExchangeRatesLine[k] = gasConcentrationLine[i];
									k++;
								}
							}
							/* set button to visible and add data */
							denoisedCumulatedGasExchangeData.add(denoisedCumulatedGasExchangeRatesLine);
							denoisedCumulatedGasExchangeCurve.setVisible(true);
							denoisedCumulatedGasExchangeCurve.setEnabled(true);
							movingDenoisedGasExchangeRatesAverage.setVisible(true);
							subPanel3.setVisible(true);

						}

						/* create first row of o2ExchangeLine (set to 0) */
						if (presenceMass && presence32 && presence36) {
							o2ExchangeLine[0] = (double) 0;
							o2ExchangeLine[1] = (double) 0;
							o2ExchangeLine[2] = (double) 0;
							o2ExchangeLine[3] = (double) 0;
							o2ExchangeData.add(o2ExchangeLine);
						}

						/* set buttons to visible and enabled */
						amperometricCurve.setVisible(true);
						amperometricCurve.setEnabled(true);
						gasConcentrationCurve.setVisible(true);
						gasConcentrationCurve.setEnabled(true);
						movingGasExchangeRatesAverage.setVisible(true);
						cumulatedGasExchangeCurve.setVisible(true);
						cumulatedGasExchangeCurve.setEnabled(true);
						subPanel2.setVisible(true);

						/* pack the window now that we added buttons */
						Window.this.pack();
						Window.this.setLocationRelativeTo(null);

						/* continuously check if new data arrived in the csv */
						Main.logger.info("Continuously read the data file");
						while (working) {
							/* if pause but working, just wait for play */
							while (pause && working) {
								try {
									Thread.sleep(1500);
								} catch (InterruptedException e1) {
									Main.logger.severe(e1.toString());
								}
							}
							/* check if new data arrived in the csv */
							while ((line = fichier.readLine()) != null) {

								/*
								 * reset the data line
								 */
								amperometricLine = new Double[nbAmperometricColumn];
								gasConcentrationLine = new Double[nbMoleculeColumn];
								gasExchangeRatesLine = new Double[nbMoleculeColumn];
								cumulatedGasExchangeRatesLine = new Double[nbMoleculeColumn];
								denoisedGasExchangeRatesLine = new Double[nbMoleculeColumn - 1];
								denoisedCumulatedGasExchangeRatesLine = new Double[nbMoleculeColumn - 1];
								o2ExchangeLine = new Double[4];
								o2ExchangeRatesLine = new Double[4];
								hydrogenaseActivityLine = new Double[2];

								/* reset elements */
								j = 0;
								valueCO2 = 0;
								valueETOH = 0;
								valueNO = 0;
								presence30 = -1;
								workingLine = line.replaceFirst(";", "").replaceAll(",", ".").split(";");

								/* same as above */
								for (int i = 0; i < nbAmperometricColumn; i++) {

									/* get current time for the row (in min) */
									if (i == 0) {
										Date date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.S").parse(workingLine[0]);
										amperometricLine[i] = (((double) date.getTime() - (double) refDate.getTime())
												/ 1000 / 60);
										gasConcentrationLine[i] = amperometricLine[i];

									} else {
										/* just add the amperometric value to amperometricData */
										amperometricLine[i] = Double.parseDouble(workingLine[i]);

										/* A_(N_2 O) (44)=A(44)-(A_(CO_2 )(12))/0.0871 */
										if (amperometricColumnName[i].startsWith("12")) {
											/*
											 * keep the amperometric value in memory and jump to next column without
											 * adding anything to gasConcentration data (represent (A_(CO_2
											 * )(12))/0.0871)
											 */
											valueCO2 = amperometricLine[i] / Double.parseDouble(factor.get(44)[5]);
											/*
											 * increase j, so the index of gasConcentration data (i-j) will remain the
											 * same next time
											 */
											j++;

											/* A_(ETOH) (31)=A(31)-(A_((METOH)) (27))/0.0871 */
										} else if (amperometricColumnName[i].startsWith("27")) {
											/* same idea */
											valueETOH = amperometricLine[i] / Double.parseDouble(factor.get(31)[5]);
											j++;
										}
										/*
										 * if we have 12 we store the value A(30) and the index because we need the
										 * value of CO2 to get A(NO)(30)
										 */
										else if (amperometricColumnName[i].startsWith("30") && presence12) {
											valueNO = amperometricLine[i];
											/* store the index of element 30 */
											presence30 = i - j;

										} /* if H2O, do nothing (delete H2O) */
										else if (amperometricColumnName[i].startsWith("18") && h2oT) {
											j++;
										}
										/*
										 * add the two element with their corresponding value to the correct index
										 */
										else if (amperometricColumnName[i].startsWith("31") && presence27) {

											/* A_(ETOH) (31)=(A_(ETOH)(27))/0.2241 */

											gasConcentrationLine[i
													- j] = ((valueETOH) - Double.parseDouble(factor.get(31)[3]))
															* gasConcentrationCorrection.get(i - j - 1);
											/*
											 * gasConcentrationCorrection.get(i - j - 1) is here because
											 * C(t)=(A(t)-A(0))聃_max/((A_max-A_0))
											 */

											/* decrease j (so increase i-j) */
											j--;

											/* A_(METOH)(31)=A(27)-(A_((METOH))(27))//0.2241 */
											gasConcentrationLine[i - j] = ((amperometricLine[i] - valueETOH)
													- Double.parseDouble(factor.get(-31)[3]))
													* gasConcentrationCorrection.get(i - j - 1);

										} else if (amperometricColumnName[i].startsWith("44") && presence12) {
											/*
											 * same than above but with A_(N_2 O) (44)=A(44)-(A_(CO_2 )(12))/0.0871
											 */
											gasConcentrationLine[i
													- j] = (valueCO2 - Double.parseDouble(factor.get(44)[3]))
															* gasConcentrationCorrection.get(i - j - 1);
											j--;
											gasConcentrationLine[i - j] = ((amperometricLine[i] - valueCO2)
													- Double.parseDouble(factor.get(-44)[3]))
													* gasConcentrationCorrection.get(i - j - 1);

											if (presence30 != 0) {
												/*
												 * if we have 12 and 30, edit the value of NO (we do that because we
												 * need the value of element N2O) : A_(NO) (30)=(A_(NO)(12)-A(30)*0.311)
												 */
												gasConcentrationLine[presence30] = ((valueNO
														- (amperometricLine[i] - valueCO2)
																* Double.parseDouble(factor.get(30)[5])) 
														- Double.parseDouble(factor.get(30)[3]))
														* gasConcentrationCorrection.get(presence30 - 1);
											}
											if (ciT) {
												j--;
												/*
												 * add carbon inorganic value Ci=Cco2*(1+exp((-6.4+pH)*ln(10))*(1+exp((-10.3+pH)*ln(10)))) and
												 * gasConcentrationCorrection.get(i - j - 1)=1+exp((-6.4+pH)*ln(10))*(1+exp((-10.3+pH)*ln(10)))
												 */
												gasConcentrationLine[i - j] = gasConcentrationLine[i - j - 2]
														* gasConcentrationCorrection.get(i - j - 1);
											}

										} /* add ths condition for Ci if we don't have 12 */
										else if (amperometricColumnName[i].startsWith("44") && presence12 == false) {
											/* add CO2 */
											gasConcentrationLine[i
													- j] = (amperometricLine[i] - Double.parseDouble(factor.get(44)[3]))
															* gasConcentrationCorrection.get(i - j - 1);
											if (ciT) {
												j--;
												/*
												 * add carbon inorganic value Ci=Cco2*(1+exp((-6.4+pH)*ln(10))*(1+exp((-10.3+pH)*ln(10)))) and
												 * gasConcentrationCorrection.get(i - j - 1)=1+exp((-6.4+pH)*ln(10))*(1+exp((-10.3+pH)*ln(10)))
												 */
												gasConcentrationLine[i - j] = gasConcentrationLine[i - j - 1]
														* gasConcentrationCorrection.get(i - j - 1);
											}

										} else {
											/* just add the value in the dataset */
											gasConcentrationLine[i - j] = (amperometricLine[i] - Double.parseDouble(
													factor.get(Integer.parseInt(amperometricColumnName[i]))[3]))
													* gasConcentrationCorrection.get(i - j - 1);

										}
									}
								}
								/* gasConcentrationData.get(nbRow - 1)[0] because we need time(i-1) */
								gasExchangeRatesLine[0] = gasConcentrationData.get(nbRow - 1)[0];
								cumulatedGasExchangeRatesLine[0] = gasConcentrationLine[0];
								if (presenceMass) {
									denoisedGasExchangeRatesLine[0] = gasConcentrationData.get(nbRow - 1)[0];
									denoisedCumulatedGasExchangeRatesLine[0] = gasConcentrationLine[0];
								}

								k = 1;
								for (int i = 1; i < nbMoleculeColumn; i++) {
									/* v(ti-1)=((c(i)-c(i-1))/ti-ti-1)+k*c(i-1) */
									gasExchangeRatesLine[i] = ((gasConcentrationLine[i]
											- gasConcentrationData.get(nbRow - 1)[i])
											/ (gasConcentrationLine[0] - gasExchangeRatesLine[0])
											+ gasConcentrationConsumption.get(i - 1)
													* gasConcentrationData.get(nbRow - 1)[i])
											/ normalizationFactorValue;
									/* c(i)(ti)=c(i-1)(ti-1)+v(i-1)(ti-1)*(ti-ti-1) */
									cumulatedGasExchangeRatesLine[i] = cumulatedGasExchangeData.get(nbRow - 1)[i]
											+ gasExchangeRatesLine[i]
													* (cumulatedGasExchangeRatesLine[0] - gasExchangeRatesLine[0]);
									if (presenceMass) {
										if (i != indexMass) {
											/*
											 * vd(ti-1)=((c(i)-c(i-1))/ti-ti-1)-k*c(i-1)*((c(i)(mass)-c(i-1)(
											 * mass))/ti-ti-1)/c(i-1)(mass)/k(mass)
											 */
											denoisedGasExchangeRatesLine[k] = ((gasConcentrationLine[i]
													- gasConcentrationData.get(nbRow - 1)[i])
													/ (cumulatedGasExchangeRatesLine[0] - gasExchangeRatesLine[0])
													- (gasConcentrationConsumption.get(i - 1)
															* ((gasConcentrationLine[indexMass]
																	- gasConcentrationData.get(nbRow - 1)[indexMass])
																	/ (cumulatedGasExchangeRatesLine[0]
																			- gasExchangeRatesLine[0]))
															/ gasConcentrationData.get(nbRow - 1)[indexMass]
															/ gasConcentrationConsumption.get(indexMass - 1)
															* gasConcentrationData.get(nbRow - 1)[i]))
													/ normalizationFactorValue;
											/* cd(i)(ti)=cd(i-1)(ti-1)+vd(i-1)(ti-1)*(ti-ti-1) */
											denoisedCumulatedGasExchangeRatesLine[k] = denoisedCumulatedGasExchangeData
													.get(nbRow - 1)[k]
													+ denoisedGasExchangeRatesLine[k]
															* (cumulatedGasExchangeRatesLine[0]
																	- gasExchangeRatesLine[0]);
											k++;
										}

									}
								}
								if (presenceMass && presence32 && presence36) {
									/* time (i-1) */
									o2ExchangeRatesLine[0] = gasExchangeRatesLine[0];
									/* U0=v(36)*(1+c(32)/c(36)) */
									o2ExchangeRatesLine[1] = (denoisedGasExchangeRatesLine[indexgasExchangeRates36]
											* (1 + gasConcentrationData.get(nbRow - 1)[indexgasConcentration32]
													/ gasConcentrationData.get(nbRow - 1)[indexgasConcentration36]));
									/* E0=v(32)-c(36)*(c(32)/c(36)) */
									o2ExchangeRatesLine[2] = (denoisedGasExchangeRatesLine[indexgasExchangeRates32]
											- denoisedGasExchangeRatesLine[indexgasExchangeRates36]
													* (gasConcentrationData.get(nbRow - 1)[indexgasConcentration32]
															/ gasConcentrationData
																	.get(nbRow - 1)[indexgasConcentration36]));
									/* Net=Eo+U0 */
									o2ExchangeRatesLine[3] = o2ExchangeRatesLine[1] + o2ExchangeRatesLine[2];

									/* time (i) */
									o2ExchangeLine[0] = gasConcentrationLine[0];
									/* cu0(i)(ti)=cu0(i-1)(ti-1)+U0(i-1)(ti-1)*(ti-ti-1) */
									o2ExchangeLine[1] = (o2ExchangeData.get(nbRow - 1)[1]) + o2ExchangeRatesLine[1]
											* (gasConcentrationLine[0] - gasExchangeRatesLine[0]);
									/* ce0(i)(ti)=ce0(i-1)(ti-1)+e0(i-1)(ti-1)*(ti-ti-1) */
									o2ExchangeLine[2] = (o2ExchangeData.get(nbRow - 1)[2]) + o2ExchangeRatesLine[2]
											* (gasConcentrationLine[0] - gasExchangeRatesLine[0]);
									/* Cnet=Ceo+Cu0 */
									o2ExchangeLine[3] = o2ExchangeLine[1] + o2ExchangeLine[2];
									/* set button to visible and enabled */
									o2ExchangeRatesData.add(o2ExchangeRatesLine);
									o2ExchangeData.add(o2ExchangeLine);
									o2ExchangeRatesCurve.setVisible(true);
									o2ExchangeRatesCurve.setEnabled(true);
									movingO2ExchangeAverage.setVisible(true);
									subPanel4.setVisible(true);
									o2ExchangeCurve.setVisible(true);
									o2ExchangeCurve.setEnabled(true);
								}
								if (presence2 && presence3 && presence4 && presenceMass) {
									/* time (i-1) */
									hydrogenaseActivityLine[0] = gasExchangeRatesLine[0];
									/*
									 * H2activity = (2*v_(H_2 )(t)+v_HD (t))/(C_D_2 (t)+(C_HD (t))/2)/(C_D_2
									 * (t)+C_H_2 (t)+C_HD (t))
									 */
									hydrogenaseActivityLine[1] = (2 * denoisedGasExchangeRatesLine[index2]
											* +denoisedGasExchangeRatesLine[index3])
											/ ((gasConcentrationData.get(nbRow - 1)[index4]
													+ gasConcentrationData.get(nbRow - 1)[index3] / 2)
													/ (gasConcentrationData.get(nbRow - 1)[index4]
															+ gasConcentrationData.get(nbRow - 1)[index2]
															+ gasConcentrationData.get(nbRow - 1)[index3]))
											+ Math.abs(denoisedGasExchangeRatesLine[index2]
													+ denoisedGasExchangeRatesLine[index3]
													+ denoisedGasExchangeRatesLine[index4]);
									/* set button to visible and enabled */
									hydrogenaseActivityData.add(hydrogenaseActivityLine);
									hydrogenaseActivityCurve.setVisible(true);
									hydrogenaseActivityCurve.setEnabled(true);

								}

								/* add other data to corresponding ArrayList */
								amperometricData.add(amperometricLine);
								gasConcentrationData.add(gasConcentrationLine);
								gasExchangeRatesData.add(gasExchangeRatesLine);
								cumulatedGasExchangeData.add(cumulatedGasExchangeRatesLine);

								if (presenceMass) {
									/* set button of denoised data to visible and enabled */
									denoisedGasExchangeRatesData.add(denoisedGasExchangeRatesLine);
									denoisedCumulatedGasExchangeData.add(denoisedCumulatedGasExchangeRatesLine);
									denoisedGasExchangeRatesCurve.setVisible(true);
									denoisedGasExchangeRatesCurve.setEnabled(true);
									gasConcentrationMoleculeList.setVisible(true);
									denoisedGasExchangeRatesMoleculeList.setVisible(true);
									gasExchangeRateFunctionConcentrationCurve.setVisible(true);
									subPanel5.setVisible(true);

								}

								/* Warn each corresponding Thread that new data arrived */
								newAmperometricData = true;
								newGasConcentrationData = true;
								newGasExchangeRatesData = true;
								newCumulatedGasExchangeData = true;
								newDenoisedGasExchangeRatesData = true;
								newDenoisedCumulatedGasExchangeData = true;
								newO2ExchangeRatesData = true;
								newO2ExchangeData = true;
								newGasExchangeRateFunctionConcentrationData = true;
								newHydrogenaseActivityData = true;

								/* update nbRow */
								nbRow++;
								if (nbRow == 2) {
									Main.logger.info("Data File Opened");
								}

								/* set button to visible */
								gasExchangeRatesCurve.setVisible(true);
								gasExchangeRatesCurve.setEnabled(true);
							}

							/* sleep to wait new data */
							try {
								Thread.sleep(1500);
							} catch (InterruptedException e1) {
								Main.logger.severe(e1.toString());
							}

						}
					}

					catch (ParseException | NumberFormatException | IOException e2) {
						Main.logger.warning(e2.toString());
						infoBox(Window.this,
								"There is an issue with your files: " + "\n"
										+ "Please verify that you used the correct format of data and factor file",
								"Opening Data File");
						try {
							fichier.close();
						} catch (IOException e) {
							Main.logger.severe(e.toString());
						}
						return;
					}
					/* if we're done, close the buffer */
					try {
						fichier.close();
					} catch (IOException e) {
						Main.logger.severe(e.toString());
					}

				} catch (FileNotFoundException e3) {
					Main.logger.warning(e3.toString());
					infoBox(Window.this, "There is an issue with your data file: " + "\n"
							+ "We can't find your file, please proceed again", "Opening Data File");

				}

				finally {
				}
			}
		};
		t.start();
		/* add the thread to the list of all running threads */
		threads.add(t);
	}

	/**
	 * The 10 following class are action listeners, which displayed corresponding
	 * curves when user pressed the corresponding button. The code between each one
	 * look really like the same but it was more convenient to do so for further
	 * specific modification compared to a common class.
	 */

	/**
	 * Display the Chart of the amperometricSignal according to time
	 */
	class DisplayAmperometric implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			/*
			 * don't open an other graph if one already exist, and put the frame to first
			 * plan
			 */
			if (amperometricFrameOpen) {
				amperometricFrame.toFront();
				return;
			}
			Main.logger.info("Displaying Amperometric Signal");
			/*
			 * create a thread so that the chart will be updated each time a data arrived
			 * (newData=true)
			 */
			Thread tb = new Thread() {
				public void run() {
					amperometricFrameOpen = true;
					/* create a new window for the chart */
					amperometricFrame = new JFrame("Amperometric Curve");
					/* define the chart */
					DisplayCurve chart = null;
					/* create an object DisplayCurve, which contain the chart */
					try {
						List<String> heading = new ArrayList<String>();
						/* create the heading of the future JTable */
						heading.add("M/Z");
						heading.add("Min");
						heading.add("Max");
						heading.add("Value for T = ");
						heading.add("Average between ");

						/* create the chart */
						chart = new DisplayCurve(amperometricData, amperometricColumnName, nbAmperometricColumn,
								heading, "Amperometric Signal");
						amperometricFrameOpen = true;
					} catch (ParseException e) {
						Main.logger.severe(e.toString());
					}

					/* add the chart and the ScrollPane(principalPanel) to the window */
					amperometricFrame.add(chart.getPanel(), BorderLayout.CENTER);
					amperometricFrame.getContentPane().setBackground(Color.white);
					amperometricFrame.setIconImage(img.getImage());

					/*
					 * when we close the frame, we stopped the thread and disable the
					 * amperometricCurve button, until the thread had stopped
					 */
					amperometricFrame.addWindowListener(new WindowAdapter() {
						public void windowClosing(WindowEvent e) {
							amperometricFrameOpen = false;
							amperometricCurve.setEnabled(false);

						}
					});

					/* create the buttons (menu bar) of the frame */
					SaveAmperometricData saveamperometricData = new SaveAmperometricData("Save Data");
					Pause pause = new Pause("Pause");
					Play play = new Play("Play");

					JMenu file = new JMenu("File");
					JMenu edit = new JMenu("Edit");
					JMenuBar menu = new JMenuBar();

					file.add(saveamperometricData);
					edit.add(play);
					edit.add(pause);

					menu.add(file);
					menu.add(edit);

					/* add elements to the frame */
					amperometricFrame.setJMenuBar(menu);
					amperometricFrame.pack();
					amperometricFrame.setVisible(true);

					/* reload the chart if new data arrived (and if the frame is still opened) */
					while (working && amperometricFrameOpen) {
						try {
							Thread.sleep(1500);
							if (newAmperometricData) {
								/* add new data to the chart */
								chart.addData(amperometricData, nbRow);
								newAmperometricData = false;

							}
						} catch (InterruptedException e) {
							Main.logger.severe(e.toString());
						}
					}
					/* enable the button one the thread had stopped */
					amperometricCurve.setEnabled(true);
				}
			};
			tb.start();
			threads.add(tb);

		}
	}

	/**
	 * Display the Chart of the gasConcentration according to time
	 */
	class DisplayGasConcentration implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			/*
			 * don't open an other graph if one already exist, just put the frame in first
			 * plan
			 */
			if (gasConcentrationFrameOpen) {
				gasConcentrationFrame.toFront();
				return;
			}
			Main.logger.info("Displaying Gas Concentration");
			/*
			 * create a thread so that the chart will be updated each time a data arrived
			 * (newData)
			 */

			Thread tb = new Thread() {
				public void run() {
					/* create a new window for the chart */
					gasConcentrationFrame = new JFrame("Gas Concentration Curve");
					/* define the chart */
					DisplayCurve chart = null;
					/* create an object DisplayCurve, which contain the chart */
					try {
						List<String> heading = new ArrayList<String>();
						/* create the heading of the future JTable */
						heading.add("Molecule");
						heading.add("Min");
						heading.add("Max");
						heading.add("Value for T = ");
						heading.add("Average between ");

						chart = new DisplayCurve(gasConcentrationData, gasConcentrationColumnName, nbMoleculeColumn,
								heading, "Gas Concentration");
						gasConcentrationFrameOpen = true;
					} catch (ParseException e) {
						Main.logger.severe(e.toString());
					}

					/*
					 * add the chart and the ScrollPane (the principal panel of DisplayCurve) to the
					 * window
					 */
					gasConcentrationFrame.add(chart.getPanel(), BorderLayout.CENTER);
					gasConcentrationFrame.getContentPane().setBackground(Color.white);
					gasConcentrationFrame.setIconImage(img.getImage());

					/*
					 * when we close the frame, we stopped the thread and disable the
					 * gasConcentrationCurve button, until the thread had stopped
					 */
					gasConcentrationFrame.addWindowListener(new WindowAdapter() {
						public void windowClosing(WindowEvent e) {
							gasConcentrationFrameOpen = false;
							gasConcentrationCurve.setEnabled(false);

						}
					});

					/* create the buttons (menu bar) of the frame */
					SaveGasConcentrationData savegasConcentrationData = new SaveGasConcentrationData("Save Data");
					Pause pause = new Pause("Pause");
					Play play = new Play("Play");

					JMenu file = new JMenu("File");
					JMenu edit = new JMenu("Edit");
					JMenuBar menu = new JMenuBar();

					file.add(savegasConcentrationData);
					edit.add(play);
					edit.add(pause);

					menu.add(file);
					menu.add(edit);

					/* add elements to the frame */
					gasConcentrationFrame.setJMenuBar(menu);
					gasConcentrationFrame.pack();
					gasConcentrationFrame.setVisible(true);

					/* reload the chart if new data arrived */
					while (working && gasConcentrationFrameOpen) {
						try {
							Thread.sleep(1500);
							if (newGasConcentrationData) {
								/* add new data */
								chart.addData(gasConcentrationData, nbRow);
								newGasConcentrationData = false;

							}
						} catch (InterruptedException e) {
							Main.logger.severe(e.toString());
						}
					}
					gasConcentrationCurve.setEnabled(true);
				}
			};
			tb.start();

			threads.add(tb);
		}
	}

	/**
	 * Display the Chart of the gas exchange rates according to time
	 */
	class DisplayGasExchangeRates implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			/*
			 * don't open an other graph if one already exist, just put the frame in first
			 * plan (unless the moving average entered by the user had changed)
			 */
			if (gasExchangeRatesFrameOpen
					&& thresholdGasExchangeRatesAverage != (Integer) (movingGasExchangeRatesAverage.getValue())) {
				gasExchangeRatesFrame.dispatchEvent(new WindowEvent(gasExchangeRatesFrame, WindowEvent.WINDOW_CLOSING));
				try {
					/* wait for previous thread to finished */
					exchangeThread.join();
				} catch (InterruptedException e1) {
					Main.logger.severe(e1.toString());
				}

			} else if (gasExchangeRatesFrameOpen) {
				gasExchangeRatesFrame.toFront();
				return;
			}

			/* moving average entered by user */
			thresholdGasExchangeRatesAverage = (Integer) movingGasExchangeRatesAverage.getValue();

			if (2 * thresholdGasExchangeRatesAverage >= gasExchangeRatesData.size()) {
				Main.logger.info("Step: " + thresholdGasExchangeRatesAverage + " to big for Moving Average (max: "
						+ Math.round(gasExchangeRatesData.size() / 2) + " )");
				infoBox(Window.this, "Wrong step: " + "\n" + "Please enter a step lower than "
						+ Math.round(gasExchangeRatesData.size() / 2) + " .", "Sliding Average");
				return;
			}

			Main.logger.info("Displaying Gas Exchange Rates");
			/*
			 * create a thread so that the chart will be updated each time a data arrived
			 * (newData)
			 */

			Thread tb = new Thread() {
				public void run() {
					/* treat data to have a moving average */
					int movingAverageIndex = 0;
					List<Double[]> movingAverageExchangeData = new ArrayList<Double[]>();
					/*
					 * take every data between thresholdGasExchangeRatesAverage and
					 * thresholdGasExchangeRatesAverage+gasExchangeRatesData.size()
					 */
					for (int j = thresholdGasExchangeRatesAverage; j < gasExchangeRatesData.size()
							- thresholdGasExchangeRatesAverage; j++) {
						Double[] currentLine = new Double[nbMoleculeColumn];
						/* treat each column */
						for (int i = 0; i < nbMoleculeColumn; i++) {
							if (i == 0) {
								/* time value */
								currentLine[i] = gasExchangeRatesData.get(j)[i];
							} else {
								double value = 0;
								/*
								 * sum data surrounding our current value (-thresholdGasExchangeRatesAverage and
								 * +thresholdGasExchangeRatesAverage)
								 */
								for (int k = -thresholdGasExchangeRatesAverage; k < thresholdGasExchangeRatesAverage
										+ 1; k++) {
									value += gasExchangeRatesData.get(j + k)[i];
								}
								currentLine[i] = value / (2 * thresholdGasExchangeRatesAverage + 1);
							}
						}
						movingAverageExchangeData.add(currentLine);
						currentLine = null;
						movingAverageIndex++;
					}

					/* create a new window for the chart */
					gasExchangeRatesFrame = new JFrame("Gas Exchange Rates Curve");
					/* define the chart */
					DisplayCurve chart = null;
					/* create an object DisplayCurve, which contain the chart */
					try {
						List<String> heading = new ArrayList<String>();
						/* create the heading of the future JTable */
						heading.add("Molecule");
						heading.add("Min");
						heading.add("Max");
						heading.add("Value for T = ");
						heading.add("Average between ");

						chart = new DisplayCurve(movingAverageExchangeData, gasExchangeRatesColumnName,
								nbMoleculeColumn, heading, "Gas Exchange Rates");
						gasExchangeRatesFrameOpen = true;
					} catch (ParseException e) {
						Main.logger.severe(e.toString());
					}

					/*
					 * add the chart and the ScrollPane (the principal panel of DisplayCurve) to the
					 * window
					 */
					gasExchangeRatesFrame.add(chart.getPanel(), BorderLayout.CENTER);
					gasExchangeRatesFrame.getContentPane().setBackground(Color.white);
					gasExchangeRatesFrame.setIconImage(img.getImage());

					/*
					 * when we close the frame, we stopped the thread and disable the
					 * gasExchangeRatesCurve button, until the thread had stopped
					 */
					gasExchangeRatesFrame.addWindowListener(new WindowAdapter() {
						public void windowClosing(WindowEvent e) {
							gasExchangeRatesFrameOpen = false;
							gasExchangeRatesCurve.setEnabled(false);

						}
					});

					/* create the buttons (menu bar) of the frame */
					SaveGasExchangeRatesData savegasExchangeRatesData = new SaveGasExchangeRatesData("Save Data");
					Pause pause = new Pause("Pause");
					Play play = new Play("Play");

					JMenu file = new JMenu("File");
					JMenu edit = new JMenu("Edit");
					JMenuBar menu = new JMenuBar();

					file.add(savegasExchangeRatesData);
					edit.add(play);
					edit.add(pause);

					menu.add(file);
					menu.add(edit);

					/* add elements to the frame */
					gasExchangeRatesFrame.setJMenuBar(menu);
					gasExchangeRatesFrame.pack();
					gasExchangeRatesFrame.setVisible(true);

					/* reload the chart if new data arrived */
					while (working && gasExchangeRatesFrameOpen) {
						try {
							Thread.sleep(1500);
							if (newGasExchangeRatesData) {
								/* treat each row until row =1+2 * thresholdGasExchangeRatesAverage */
								while (1 + movingAverageIndex + 2 * thresholdGasExchangeRatesAverage < nbRow) {
									Double[] currentLine = new Double[nbMoleculeColumn];
									/* same as above */
									for (int i = 0; i < nbMoleculeColumn; i++) {
										if (i == 0) {
											/* time value */
											currentLine[i] = gasExchangeRatesData.get(movingAverageIndex + 1)[i];
										} else {
											double value = 0;
											for (int k = -thresholdGasExchangeRatesAverage; k < thresholdGasExchangeRatesAverage
													+ 1; k++) {
												value += gasExchangeRatesData.get(movingAverageIndex + 1 + k)[i];
											}
											currentLine[i] = value / (2 * thresholdGasExchangeRatesAverage + 1);
										}
									}
									movingAverageExchangeData.add(currentLine);
									currentLine = null;
									movingAverageIndex++;
								}
								/* add new data */
								chart.addData(movingAverageExchangeData, movingAverageIndex);
								newGasExchangeRatesData = false;
							}
						} catch (InterruptedException e) {
							Main.logger.severe(e.toString());
						}
					}
					gasExchangeRatesCurve.setEnabled(true);
				}
			};
			tb.start();
			threads.add(tb);
			exchangeThread = tb;
		}
	}

	/**
	 * Display the Chart of the cumulatedGasExchangeRates gas exchange according to
	 * time
	 */
	class DisplayCumulatedGasExchange implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			/*
			 * don't open an other graph if one already exist, just put the frame in first
			 * plan
			 */
			if (cumulatedGasExchangeFrameOpen) {
				cumulatedGasExchangeFrame.toFront();
				return;
			}
			Main.logger.info("Displaying Cumulated Gas Exchange");
			/*
			 * create a thread so that the chart will be updated each time a data arrived
			 * (newData)
			 */
			Thread tb = new Thread() {
				public void run() {
					/* create a new window for the chart */
					cumulatedGasExchangeFrame = new JFrame("Cumulated Gas Exchange Curve");
					/* define the chart */
					DisplayCurve chart = null;
					/* create an object DisplayCurve, which contain the chart */
					try {
						List<String> heading = new ArrayList<String>();
						/* create the heading of the future JTable */
						heading.add("Molecule");
						heading.add("Min");
						heading.add("Max");
						heading.add("Value for T = ");
						heading.add("Average between ");

						chart = new DisplayCurve(cumulatedGasExchangeData, gasConcentrationColumnName, nbMoleculeColumn,
								heading, "Cumulated Gas Exchange");
						cumulatedGasExchangeFrameOpen = true;
					} catch (ParseException e) {
						Main.logger.severe(e.toString());
					}

					/*
					 * add the chart and the ScrollPane (the principal panel of DisplayCurve) to the
					 * window
					 */
					cumulatedGasExchangeFrame.add(chart.getPanel(), BorderLayout.CENTER);
					cumulatedGasExchangeFrame.getContentPane().setBackground(Color.white);
					cumulatedGasExchangeFrame.setIconImage(img.getImage());

					/*
					 * when we close the frame, we stopped the thread and disable the
					 * cumulatedGasExchangeCurve button, until the thread had stopped
					 */
					cumulatedGasExchangeFrame.addWindowListener(new WindowAdapter() {
						public void windowClosing(WindowEvent e) {
							cumulatedGasExchangeFrameOpen = false;
							cumulatedGasExchangeCurve.setEnabled(false);

						}
					});

					/* create the buttons (menu bar) of the frame */
					SaveCumulatedGasExchangeData savecumulatedGasExchangeRatesData = new SaveCumulatedGasExchangeData(
							"Save Data");
					Pause pause = new Pause("Pause");
					Play play = new Play("Play");

					JMenu file = new JMenu("File");
					JMenu edit = new JMenu("Edit");
					JMenuBar menu = new JMenuBar();

					file.add(savecumulatedGasExchangeRatesData);
					edit.add(play);
					edit.add(pause);

					menu.add(file);
					menu.add(edit);

					/* add elements to the frame */
					cumulatedGasExchangeFrame.setJMenuBar(menu);
					cumulatedGasExchangeFrame.pack();
					cumulatedGasExchangeFrame.setVisible(true);

					/* reload the chart if new data arrived */
					while (working && cumulatedGasExchangeFrameOpen) {
						try {
							Thread.sleep(1500);
							if (newCumulatedGasExchangeData) {
								/* add new data */
								chart.addData(cumulatedGasExchangeData, nbRow);
								newCumulatedGasExchangeData = false;

							}
						} catch (InterruptedException e) {
							Main.logger.severe(e.toString());
						}
					}
					cumulatedGasExchangeCurve.setEnabled(true);
				}
			};
			tb.start();

			threads.add(tb);
		}
	}

	/**
	 * Display the Chart of the denoisedGasExchangeRates gas exchange rates
	 * according to time
	 */
	class DisplayDenoisedGasExchangeRates implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			/*
			 * don't open an other graph if one already exist, just put the frame in first
			 * plan (unless the movingAverage had changed)
			 */

			if (denoisedGasExchangeRatesFrameOpen
					&& thresholdDenoisedGasExchangeRates != (Integer) (movingDenoisedGasExchangeRatesAverage
							.getValue())) {
				denoisedGasExchangeRatesFrame
						.dispatchEvent(new WindowEvent(denoisedGasExchangeRatesFrame, WindowEvent.WINDOW_CLOSING));
				try {
					/* wait for previous thread to finished */
					denoisedThread.join();
				} catch (InterruptedException e1) {
					Main.logger.severe(e1.toString());
				}

			} else if (denoisedGasExchangeRatesFrameOpen) {
				denoisedGasExchangeRatesFrame.toFront();
				return;
			}

			thresholdDenoisedGasExchangeRates = (Integer) movingDenoisedGasExchangeRatesAverage.getValue();

			if (2 * thresholdDenoisedGasExchangeRates >= denoisedGasExchangeRatesData.size()) {
				Main.logger.info("Step: " + thresholdDenoisedGasExchangeRates + " to big for Moving Average (max: "
						+ Math.round(denoisedGasExchangeRatesData.size() / 2) + " )");
				infoBox(Window.this, "Wrong step: " + "\n" + "Please enter a step lower than "
						+ Math.round(denoisedGasExchangeRatesData.size() / 2) + " .", "Sliding Average");
				return;
			}
			Main.logger.info("Displaying Denoised Gas Exchange Rates");
			/*
			 * create a thread so that the chart will be updated each time a data arrived
			 * (newData)
			 */
			Thread tb = new Thread() {
				public void run() {
					/* treat data to have a moving average */
					int movingAverageIndex = 0;
					List<Double[]> movingAverageExchangeData = new ArrayList<Double[]>();
					/*
					 * take every data between thresholdDenoisedGasExchangeRates and
					 * thresholdDenoisedGasExchangeRates+denoisedGasExchangeRatesData.size()
					 */
					for (int j = thresholdDenoisedGasExchangeRates; j < denoisedGasExchangeRatesData.size()
							- thresholdDenoisedGasExchangeRates; j++) {
						Double[] currentLine = new Double[nbMoleculeColumn - 1];
						/* treat each column */
						for (int i = 0; i < nbMoleculeColumn - 1; i++) {
							if (i == 0) {
								/* time value */
								currentLine[i] = denoisedGasExchangeRatesData.get(j)[i];
							} else {
								double value = 0;
								/*
								 * sum data surrounding our current value (-thresholdDenoisedGasExchangeRates
								 * and +thresholdDenoisedGasExchangeRates)
								 */
								for (int k = -thresholdDenoisedGasExchangeRates; k < thresholdDenoisedGasExchangeRates
										+ 1; k++) {
									value += denoisedGasExchangeRatesData.get(j + k)[i];
								}
								currentLine[i] = value / (2 * thresholdDenoisedGasExchangeRates + 1);
							}
						}
						movingAverageExchangeData.add(currentLine);
						currentLine = null;
						movingAverageIndex++;
					}

					/* create a new window for the chart */
					denoisedGasExchangeRatesFrame = new JFrame("Gas Exchange Rates (Denoised) Curves");
					/* define the chart */
					DisplayCurve chart = null;
					/* create an object DisplayCurve, which contain the chart */
					try {
						List<String> heading = new ArrayList<String>();
						/* create the heading of the future JTable */
						heading.add("Molecule");
						heading.add("Min");
						heading.add("Max");
						heading.add("Value for T = ");
						heading.add("Average between ");

						chart = new DisplayCurve(movingAverageExchangeData, denoisedGasExchangeRatesColumnName,
								nbMoleculeColumn - 1, heading, "Gas Exchange Rates (Denoised)");
						denoisedGasExchangeRatesFrameOpen = true;
					} catch (ParseException e) {
						Main.logger.severe(e.toString());
					}

					/*
					 * add the chart and the ScrollPane (the principal panel of DisplayCurve) to the
					 * window
					 */
					denoisedGasExchangeRatesFrame.add(chart.getPanel(), BorderLayout.CENTER);
					denoisedGasExchangeRatesFrame.getContentPane().setBackground(Color.white);
					denoisedGasExchangeRatesFrame.setIconImage(img.getImage());

					/*
					 * when we close the frame, we stopped the thread and disable the
					 * denoisedGasExchangeRatesCurve button, until the thread had stopped
					 */
					denoisedGasExchangeRatesFrame.addWindowListener(new WindowAdapter() {
						public void windowClosing(WindowEvent e) {
							denoisedGasExchangeRatesFrameOpen = false;
							denoisedGasExchangeRatesCurve.setEnabled(false);

						}
					});

					/* create the buttons (menu bar) of the frame */
					SaveDenoisedGasExchangeRatesData savedenoisedGasExchangeRatesData = new SaveDenoisedGasExchangeRatesData(
							"Save Data");
					Pause pause = new Pause("Pause");
					Play play = new Play("Play");

					JMenu file = new JMenu("File");
					JMenu edit = new JMenu("Edit");
					JMenuBar menu = new JMenuBar();

					file.add(savedenoisedGasExchangeRatesData);
					edit.add(play);
					edit.add(pause);

					menu.add(file);
					menu.add(edit);

					/* add elements to the frame */
					denoisedGasExchangeRatesFrame.setJMenuBar(menu);
					denoisedGasExchangeRatesFrame.pack();
					denoisedGasExchangeRatesFrame.setVisible(true);

					/* reload the chart if new data arrived */
					while (working && denoisedGasExchangeRatesFrameOpen) {
						try {
							Thread.sleep(1500);
							if (newDenoisedGasExchangeRatesData) {
								/* treat each row until row =1+2 * thresholdDenoisedGasExchangeRates */
								while (1 + movingAverageIndex + 2 * thresholdDenoisedGasExchangeRates < nbRow) {
									Double[] currentLine = new Double[nbMoleculeColumn - 1];
									/* same as before */
									for (int i = 0; i < nbMoleculeColumn - 1; i++) {
										if (i == 0) {
											currentLine[i] = denoisedGasExchangeRatesData
													.get(movingAverageIndex + 1)[i];
										} else {
											double value = 0;
											for (int k = -thresholdDenoisedGasExchangeRates; k < thresholdDenoisedGasExchangeRates
													+ 1; k++) {
												value += denoisedGasExchangeRatesData
														.get(movingAverageIndex + 1 + k)[i];
											}
											currentLine[i] = value / (2 * thresholdDenoisedGasExchangeRates + 1);
										}
									}
									movingAverageExchangeData.add(currentLine);
									currentLine = null;
									movingAverageIndex++;
								}
								/* add new data */
								chart.addData(movingAverageExchangeData, movingAverageIndex);
								newDenoisedGasExchangeRatesData = false;
							}
						} catch (InterruptedException e) {
							Main.logger.severe(e.toString());
						}
					}
					denoisedGasExchangeRatesCurve.setEnabled(true);
				}
			};
			tb.start();
			threads.add(tb);
			/* set denoisedThread to the current thread */
			denoisedThread = tb;
		}
	}

	/**
	 * Display the Chart of the cumulatedGasExchangeRates gas exchange according to
	 * time
	 */
	class DisplayDenoisedCumulatedGasExchange implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			/*
			 * don't open an other graph if one already exist, just put the frame in first
			 * plan
			 */
			if (denoisedCumulatedGasExchangeFrameOpen) {
				denoisedCumulatedGasExchangeFrame.toFront();
				return;
			}
			Main.logger.info("Displaying Denoised Cumulated Gas Exchange");
			/*
			 * create a thread so that the chart will be updated each time a data arrived
			 * (newData)
			 */
			Thread tb = new Thread() {
				public void run() {
					/* create a new window for the chart */
					denoisedCumulatedGasExchangeFrame = new JFrame("Cumulated Gas Exchange (Denoised) Curve");
					/* define the chart */
					DisplayCurve chart = null;
					/* create an object DisplayCurve, which contain the chart */
					try {
						List<String> heading = new ArrayList<String>();
						/* create the heading of the future JTable */
						heading.add("Molecule");
						heading.add("Min");
						heading.add("Max");
						heading.add("Value for T = ");
						heading.add("Average between ");

						chart = new DisplayCurve(denoisedCumulatedGasExchangeData,
								denoisedCumulatedGasExchangeColumnName, nbMoleculeColumn - 1, heading,
								"Cumulated Gas Exchange (Denoised)");
						denoisedCumulatedGasExchangeFrameOpen = true;
					} catch (ParseException e) {
						Main.logger.severe(e.toString());
					}

					/*
					 * add the chart and the ScrollPane (the principal panel of DisplayCurve) to the
					 * window
					 */
					denoisedCumulatedGasExchangeFrame.add(chart.getPanel(), BorderLayout.CENTER);
					denoisedCumulatedGasExchangeFrame.getContentPane().setBackground(Color.white);
					denoisedCumulatedGasExchangeFrame.setIconImage(img.getImage());

					/*
					 * when we close the frame, we stopped the thread and disable the
					 * denoisedCumulatedGasExchangeCurve button, until the thread had stopped
					 */
					denoisedCumulatedGasExchangeFrame.addWindowListener(new WindowAdapter() {
						public void windowClosing(WindowEvent e) {
							denoisedCumulatedGasExchangeFrameOpen = false;
							denoisedCumulatedGasExchangeCurve.setEnabled(false);

						}
					});

					/* create the buttons (menu bar) of the frame */
					SaveDenoisedCumulatedGasExchangeData saveDenoisedcumulatedGasExchangeRatesData = new SaveDenoisedCumulatedGasExchangeData(
							"Save Data");
					Pause pause = new Pause("Pause");
					Play play = new Play("Play");

					JMenu file = new JMenu("File");
					JMenu edit = new JMenu("Edit");
					JMenuBar menu = new JMenuBar();

					file.add(saveDenoisedcumulatedGasExchangeRatesData);
					edit.add(play);
					edit.add(pause);

					menu.add(file);
					menu.add(edit);

					/* add elements to the frame */
					denoisedCumulatedGasExchangeFrame.setJMenuBar(menu);
					denoisedCumulatedGasExchangeFrame.pack();
					denoisedCumulatedGasExchangeFrame.setVisible(true);

					/* reload the chart if new data arrived */
					while (working && denoisedCumulatedGasExchangeFrameOpen) {
						try {
							Thread.sleep(1500);
							if (newDenoisedCumulatedGasExchangeData) {
								/* add new data */
								chart.addData(denoisedCumulatedGasExchangeData, nbRow);
								newDenoisedCumulatedGasExchangeData = false;

							}
						} catch (InterruptedException e) {
							Main.logger.severe(e.toString());
						}
					}
					denoisedCumulatedGasExchangeCurve.setEnabled(true);
				}
			};
			tb.start();
			threads.add(tb);
		}
	}

	/**
	 * Display the Chart of the gas exchange rates according to time
	 */
	class DisplayO2ExchangeRatesCurve implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			/*
			 * don't open an other graph if one already exist, just put the frame in first
			 * plan (unless the moving average entered by the user had changed)
			 */
			if (o2ExchangeRatesFrameOpen
					&& thresholdO2ExchangeAverage != (Integer) (movingO2ExchangeAverage.getValue())) {
				o2ExchangeRatesFrame.dispatchEvent(new WindowEvent(o2ExchangeRatesFrame, WindowEvent.WINDOW_CLOSING));
				try {
					/* wait for older thread to stopped */
					oxygenThread.join();
				} catch (InterruptedException e1) {
					Main.logger.severe(e1.toString());
				}

			} else if (o2ExchangeRatesFrameOpen) {
				o2ExchangeRatesFrame.toFront();
				return;
			}

			thresholdO2ExchangeAverage = (Integer) movingO2ExchangeAverage.getValue();

			if (2 * thresholdO2ExchangeAverage >= o2ExchangeRatesData.size()) {
				Main.logger.info("Step: " + thresholdO2ExchangeAverage + " to big for Moving Average (max: "
						+ Math.round(o2ExchangeRatesData.size() / 2) + " )");
				infoBox(Window.this, "Wrong step: " + "\n" + "Please enter a step lower than "
						+ Math.round(o2ExchangeRatesData.size() / 2) + " .", "Sliding Average");
				return;
			}
			Main.logger.info("Display O2 Exchange Rates Curve");
			/*
			 * create a thread so that the chart will be updated each time a data arrived
			 * (newData)
			 */
			Thread tb = new Thread() {
				public void run() {
					/* treat data to have a moving average */
					int movingAverageIndex = 0;
					List<Double[]> movingAverageExchangeData = new ArrayList<Double[]>();
					/*
					 * take every data between thresholdO2ExchangeAverage and
					 * thresholdO2ExchangeAverage+o2ExchangeRatesData.size()
					 */
					for (int j = thresholdO2ExchangeAverage; j < o2ExchangeRatesData.size()
							- thresholdO2ExchangeAverage; j++) {
						Double[] currentLine = new Double[4];
						/* treat each column */
						for (int i = 0; i < 4; i++) {
							if (i == 0) {
								currentLine[i] = o2ExchangeRatesData.get(j)[i];
							} else {
								double value = 0;
								/*
								 * sum data surrounding our current value (-thresholdO2ExchangeAverage and
								 * +thresholdO2ExchangeAverage)
								 */
								for (int k = -thresholdO2ExchangeAverage; k < thresholdO2ExchangeAverage + 1; k++) {
									value += o2ExchangeRatesData.get(j + k)[i];
								}
								currentLine[i] = value / (2 * thresholdO2ExchangeAverage + 1);
							}
						}
						movingAverageExchangeData.add(currentLine);
						currentLine = null;
						movingAverageIndex++;
					}

					/* create a new window for the chart */
					o2ExchangeRatesFrame = new JFrame("Oxygen Exchange Rates (Denoised) Curve");
					/* define the chart */
					DisplayCurve chart = null;
					/* create an object DisplayCurve, which contain the chart */
					try {
						List<String> heading = new ArrayList<String>();
						/* create the heading of the future JTable */
						heading.add("Molecule");
						heading.add("Min");
						heading.add("Max");
						heading.add("Value for T = ");
						heading.add("Average between ");

						chart = new DisplayCurve(movingAverageExchangeData, o2ExchangeRatesColumnName, 4, heading,
								"Oxygen Exchange Rates (Denoised)");
						o2ExchangeRatesFrameOpen = true;
					} catch (ParseException e) {
						Main.logger.severe(e.toString());
					}

					/*
					 * add the chart and the ScrollPane (the principal panel of DisplayCurve) to the
					 * window
					 */
					o2ExchangeRatesFrame.add(chart.getPanel(), BorderLayout.CENTER);
					o2ExchangeRatesFrame.getContentPane().setBackground(Color.white);
					o2ExchangeRatesFrame.setIconImage(img.getImage());

					/*
					 * when we close the frame, we stopped the thread and disable the
					 * o2ExchangeRatesCurve button, until the thread had stopped
					 */
					o2ExchangeRatesFrame.addWindowListener(new WindowAdapter() {
						public void windowClosing(WindowEvent e) {
							o2ExchangeRatesFrameOpen = false;
							o2ExchangeRatesCurve.setEnabled(false);

						}
					});

					/* create the buttons (menu bar) of the frame */
					SaveO2ExchangeRatesData saveO2ExchangeRatesData = new SaveO2ExchangeRatesData("Save Data");
					Pause pause = new Pause("Pause");
					Play play = new Play("Play");

					JMenu file = new JMenu("File");
					JMenu edit = new JMenu("Edit");
					JMenuBar menu = new JMenuBar();

					file.add(saveO2ExchangeRatesData);
					edit.add(play);
					edit.add(pause);

					menu.add(file);
					menu.add(edit);

					/* add elements to the frame */
					o2ExchangeRatesFrame.setJMenuBar(menu);
					o2ExchangeRatesFrame.pack();
					o2ExchangeRatesFrame.setVisible(true);

					/* reload the chart if new data arrived */
					while (working && o2ExchangeRatesFrameOpen) {
						try {
							Thread.sleep(1500);
							if (newO2ExchangeRatesData) {
								/* treat each row until row =1+2 * thresholdO2ExchangeAverage */
								while (1 + movingAverageIndex + 2 * thresholdO2ExchangeAverage < nbRow) {
									Double[] currentLine = new Double[4];
									/* same as before */
									for (int i = 0; i < 4; i++) {
										if (i == 0) {
											currentLine[i] = o2ExchangeRatesData.get(movingAverageIndex + 1)[i];
										} else {
											double value = 0;
											for (int k = -thresholdO2ExchangeAverage; k < thresholdO2ExchangeAverage
													+ 1; k++) {
												value += o2ExchangeRatesData.get(movingAverageIndex + 1 + k)[i];
											}
											currentLine[i] = value / (2 * thresholdO2ExchangeAverage + 1);
										}
									}
									movingAverageExchangeData.add(currentLine);
									currentLine = null;
									movingAverageIndex++;
								}
								/* add new data */
								chart.addData(movingAverageExchangeData, movingAverageIndex);
								newO2ExchangeRatesData = false;
							}
						} catch (InterruptedException e) {
							Main.logger.severe(e.toString());
						}
					}
					o2ExchangeRatesCurve.setEnabled(true);
				}
			};
			tb.start();
			threads.add(tb);
			oxygenThread = tb;
		}
	}

	/**
	 * Display the Chart of the cumulatedGasExchangeRates gas ExchangeRates
	 * according to time
	 */
	class DisplayO2ExchangeCurve implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			/*
			 * don't open an other graph if one already exist, just put the frame in first
			 * plan
			 */
			if (o2ExchangeFrameOpen) {
				o2ExchangeFrame.toFront();
				return;
			}
			Main.logger.info("Display O2 Exchange Curve");
			/*
			 * create a thread so that the chart will be updated each time a data arrived
			 * (newData)
			 */
			Thread tb = new Thread() {
				public void run() {
					/* create a new window for the chart */
					o2ExchangeFrame = new JFrame("Oxygen Exchange (Denoised) Curve");
					/* define the chart */
					DisplayCurve chart = null;
					/* create an object DisplayCurve, which contain the chart */
					try {
						List<String> heading = new ArrayList<String>();
						/* create the heading of the future JTable */
						heading.add("Molecule");
						heading.add("Min");
						heading.add("Max");
						heading.add("Value for T = ");
						heading.add("Average between ");

						chart = new DisplayCurve(o2ExchangeData, o2ExchangeColumnName, 4, heading,
								"Oxygen Exchange (Denoised)");
						o2ExchangeFrameOpen = true;
					} catch (ParseException e) {
						Main.logger.severe(e.toString());
					}

					/*
					 * add the chart and the ScrollPane (the principal panel of DisplayCurve) to the
					 * window
					 */
					o2ExchangeFrame.add(chart.getPanel(), BorderLayout.CENTER);
					o2ExchangeFrame.getContentPane().setBackground(Color.white);
					o2ExchangeFrame.setIconImage(img.getImage());

					/*
					 * when we close the frame, we stopped the thread and disable the
					 * o2ExchangeCurve button, until the thread had stopped
					 */
					o2ExchangeFrame.addWindowListener(new WindowAdapter() {
						public void windowClosing(WindowEvent e) {
							o2ExchangeFrameOpen = false;
							o2ExchangeCurve.setEnabled(false);

						}
					});

					/* create the buttons (menu bar) of the frame */
					SaveO2ExchangeData saveo2ExchangeData = new SaveO2ExchangeData("Save Data");
					Pause pause = new Pause("Pause");
					Play play = new Play("Play");

					JMenu file = new JMenu("File");
					JMenu edit = new JMenu("Edit");
					JMenuBar menu = new JMenuBar();

					file.add(saveo2ExchangeData);
					edit.add(play);
					edit.add(pause);

					menu.add(file);
					menu.add(edit);

					/* add elements to the frame */
					o2ExchangeFrame.setJMenuBar(menu);
					o2ExchangeFrame.pack();
					o2ExchangeFrame.setVisible(true);

					/* reload the chart if new data arrived */
					while (working && o2ExchangeFrameOpen) {
						try {
							Thread.sleep(1500);
							if (newO2ExchangeData) {
								/* add new data */
								chart.addData(o2ExchangeData, nbRow);
								newO2ExchangeData = false;

							}
						} catch (InterruptedException e) {
							Main.logger.severe(e.toString());
						}
					}
					o2ExchangeCurve.setEnabled(true);
				}
			};
			tb.start();
			threads.add(tb);
		}
	}

	/**
	 * Display the Chart of the cumulatedGasExchangeRates gas ExchangeRates
	 * according to time
	 */
	class DisplayGasExchangeRateFunctionConcentrationCurve implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			/*
			 * don't open an other graph if one already exist, just put the frame in first
			 * plan (unless the molecules entered by the user had changed)
			 */
			if (gasExchangeRateFunctionConcentrationFrameOpen
					&& (!gasExchangeRatesMolecule.equals(denoisedGasExchangeRatesMoleculeList.getSelectedItem())
							|| !gasConcentrationMolecule.equals(gasConcentrationMoleculeList.getSelectedItem()))) {
				gasExchangeRateFunctionConcentrationFrame.dispatchEvent(
						new WindowEvent(gasExchangeRateFunctionConcentrationFrame, WindowEvent.WINDOW_CLOSING));
				try {
					/* wait for older thread to stopped */
					functionThread.join();
				} catch (InterruptedException e1) {
					Main.logger.severe(e1.toString());

				}
			}
			if (gasExchangeRateFunctionConcentrationFrameOpen) {
				gasExchangeRateFunctionConcentrationFrame.toFront();
				return;
			}
			Main.logger.info("Display Gas Exchange Rate Function Concentration Curve");
			/*
			 * create a thread so that the chart will be updated each time a data arrived
			 * (newData)
			 */
			Thread tb = new Thread() {
				public void run() {
					/* create a new window for the chart */
					gasExchangeRateFunctionConcentrationFrame = new JFrame(
							"Gas Exchange Rate Function Concentration Curve");
					/* create an object DisplayCurve, which contain the chart */
					DisplayCurve chart = null;

					/* get the molecules names entered by the user */
					gasExchangeRatesMolecule = (String) denoisedGasExchangeRatesMoleculeList.getSelectedItem();
					gasConcentrationMolecule = (String) gasConcentrationMoleculeList.getSelectedItem();

					/* arrayList containing the data for the curve */
					gasExchangeRateFunctionConcentrationData = new ArrayList<Double[]>();

					/* get the index of each two molecules in their corresponding data */
					int indexgasExchangeRates = -1;
					int indexgasConcentration = -1;

					/* index of our data */
					int indexgasExchangeRateFunctionConcentration = 0;

					/* get index in gasExchangeRatesdata of our molecule */
					for (int i = 0; i < denoisedGasExchangeRatesColumnName.length; i++) {
						if (denoisedGasExchangeRatesColumnName[i].startsWith(gasExchangeRatesMolecule + " ")) {
							indexgasExchangeRates = i;

						}
					}
					/* get index in gasConcentrationData of our molecule */
					for (int i = 0; i < gasConcentrationColumnName.length; i++) {
						if (gasConcentrationColumnName[i].startsWith(gasConcentrationMolecule + " ")) {
							indexgasConcentration = i;

						}
					}
					
					Double[] line;
					
					/*know if we want oxygen exchange rates or not, in this case, special characteristic*/
					if (gasExchangeRatesMolecule == "Oxygen Exchange Rates") {
						gasExchangeRateFunctionConcentrationColumnName = new String[4];
						gasExchangeRateFunctionConcentrationColumnName[0] = gasConcentrationMolecule + " (然)";
						gasExchangeRateFunctionConcentrationColumnName[1] = "Uo (然 / min)";
						gasExchangeRateFunctionConcentrationColumnName[2] = "Eo (然 / min)";
						gasExchangeRateFunctionConcentrationColumnName[3] = "Net (然 / min)";

						/* get our data if we want o2 in our curve */
						for (int i = 0; i < nbRow - 1; i++) {
							line = new Double[4];
							line[0] = gasConcentrationData.get(i)[indexgasConcentration];
							line[1] = o2ExchangeRatesData.get(i)[1];
							line[2] = o2ExchangeRatesData.get(i)[2];
							line[3] = o2ExchangeRatesData.get(i)[3];
							gasExchangeRateFunctionConcentrationData.add(line);
							line = null;
							indexgasExchangeRateFunctionConcentration++;
						}
						try {
							List<String> heading = new ArrayList<String>();
							/* create the heading of the future JTable */
							heading.add("Gas Exchange Rates of (denoised)");
							heading.add("Min");
							heading.add("Max");
							heading.add("Value for Gas Concentration of " + gasConcentrationMolecule + " = ");
							heading.add("Average between ");

							/* create the object containing our chart */
							chart = new DisplayCurve(gasExchangeRateFunctionConcentrationData,
									gasExchangeRateFunctionConcentrationColumnName, 4, heading,
									"V( Oxygen Exchange Rates ) = F(c("
											+ gasExchangeRateFunctionConcentrationColumnName[0].split(" ")[0] + "))");
							gasExchangeRateFunctionConcentrationFrameOpen = true;
						} catch (ParseException e) {
							Main.logger.severe(e.toString());
						}
					}

					/* else create our dataset with the same idea, but only two column (one curve) */
					else {
						gasExchangeRateFunctionConcentrationColumnName = new String[2];
						gasExchangeRateFunctionConcentrationColumnName[0] = gasConcentrationMolecule + " (然)";
						gasExchangeRateFunctionConcentrationColumnName[1] = gasExchangeRatesMolecule + " (然 / min)";
						/* get our data */
						for (int i = 0; i < nbRow - 1; i++) {
							line = new Double[2];
							line[0] = gasConcentrationData.get(i)[indexgasConcentration];
							line[1] = denoisedGasExchangeRatesData.get(i)[indexgasExchangeRates];
							gasExchangeRateFunctionConcentrationData.add(line);
							line = null;
							indexgasExchangeRateFunctionConcentration++;
						}
						try {
							List<String> heading = new ArrayList<String>();
							/* create the heading of the future JTable */
							heading.add("Gas Exchange Rates of (denoised)");
							heading.add("Min");
							heading.add("Max");
							heading.add("Value for Gas Concentration of " + gasConcentrationMolecule + " = ");
							heading.add("Average between ");

							/* create the object containing our chart */
							chart = new DisplayCurve(gasExchangeRateFunctionConcentrationData,
									gasExchangeRateFunctionConcentrationColumnName, 2, heading,
									"V(" + gasExchangeRateFunctionConcentrationColumnName[1].split(" ")[0] + ") = F(c("
											+ gasExchangeRateFunctionConcentrationColumnName[0].split(" ")[0] + "))");
							gasExchangeRateFunctionConcentrationFrameOpen = true;
						} catch (ParseException e) {
							Main.logger.severe(e.toString());
						}
					}

					/*
					 * add the chart and the ScrollPane (the principal panel of DisplayCurve) to the
					 * window
					 */
					gasExchangeRateFunctionConcentrationFrame.add(chart.getPanel(), BorderLayout.CENTER);
					gasExchangeRateFunctionConcentrationFrame.getContentPane().setBackground(Color.white);
					gasExchangeRateFunctionConcentrationFrame.setIconImage(img.getImage());

					/*
					 * when we close the frame, we stopped the thread and disable the
					 * gasExchangeRateFunctionConcentrationCurve button, until the thread had
					 * stopped
					 */
					gasExchangeRateFunctionConcentrationFrame.addWindowListener(new WindowAdapter() {
						public void windowClosing(WindowEvent e) {
							gasExchangeRateFunctionConcentrationFrameOpen = false;
							gasExchangeRateFunctionConcentrationCurve.setEnabled(false);

						}
					});

					/* create the buttons (menu bar) of the frame */
					SaveGasExchangeRateFunctionConcentrationData savegasExchangeRateFunctionConcentrationData = new SaveGasExchangeRateFunctionConcentrationData(
							"Save Data");
					Pause pause = new Pause("Pause");
					Play play = new Play("Play");

					JMenu file = new JMenu("File");
					JMenu edit = new JMenu("Edit");
					JMenuBar menu = new JMenuBar();

					edit.add(play);
					edit.add(pause);
					file.add(savegasExchangeRateFunctionConcentrationData);

					menu.add(file);
					menu.add(edit);

					/* add elements to the frame */
					gasExchangeRateFunctionConcentrationFrame.setJMenuBar(menu);
					gasExchangeRateFunctionConcentrationFrame.pack();
					gasExchangeRateFunctionConcentrationFrame.setVisible(true);

					/* reload the chart if new data arrived */
					while (working && gasExchangeRateFunctionConcentrationFrameOpen) {
						try {
							Thread.sleep(1500);
							/* if new data */
							if (newGasExchangeRateFunctionConcentrationData) {
								/*if oxygen*/
								if (gasExchangeRatesMolecule == "Oxygen Exchange Rates") {
									for (int i = indexgasExchangeRateFunctionConcentration; i < nbRow - 1; i++) {
										line = new Double[4];
										line[0] = gasConcentrationData.get(i)[indexgasConcentration];
										line[1] = o2ExchangeRatesData.get(i)[1];
										line[2] = o2ExchangeRatesData.get(i)[2];
										line[3] = o2ExchangeRatesData.get(i)[3];
										gasExchangeRateFunctionConcentrationData.add(line);
										line = null;
										indexgasExchangeRateFunctionConcentration++;
									}
								}/*else*/ 
								else {
									for (int i = indexgasExchangeRateFunctionConcentration; i < nbRow - 1; i++) {
										line = new Double[2];
										line[0] = gasConcentrationData.get(i)[indexgasConcentration];
										line[1] = denoisedGasExchangeRatesData.get(i)[indexgasExchangeRates];
										gasExchangeRateFunctionConcentrationData.add(line);
										line = null;
										indexgasExchangeRateFunctionConcentration++;
									}
								}

								/* add new data */
								chart.addData(gasExchangeRateFunctionConcentrationData, nbRow - 1);
								newGasExchangeRateFunctionConcentrationData = false;

							}
						} catch (InterruptedException e) {
							Main.logger.severe(e.toString());
						}
					}
					gasExchangeRateFunctionConcentrationCurve.setEnabled(true);
				}
			};
			tb.start();
			threads.add(tb);
			functionThread = tb;
		}
	}

	class DisplayHydrogenaseActivityCurve implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			/*
			 * don't open an other graph if one already exist, just put the frame in first
			 * plan
			 */

			if (hydrogenaseActivityFrameOpen) {
				hydrogenaseActivityFrame.toFront();
				return;
			}
			Main.logger.info("Display Hydrogenase Activity Curve");
			/*
			 * create a thread so that the chart will be updated each time a data arrived
			 * (newData)
			 */
			Thread tb = new Thread() {
				public void run() {
					/* create a new window for the chart */
					hydrogenaseActivityFrame = new JFrame("Hydrogenase Activity Curve");
					/* define the chart */
					DisplayCurve chart = null;
					/* create an object DisplayCurve, which contain the chart */
					try {
						List<String> heading = new ArrayList<String>();
						/* create the heading of the future JTable */
						heading.add("Molecule");
						heading.add("Min");
						heading.add("Max");
						heading.add("Value for T = ");
						heading.add("Average between ");

						chart = new DisplayCurve(hydrogenaseActivityData, hydrogenaseActivityColumnName, 2, heading,
								"Hydrogenase Activity Curve");
						hydrogenaseActivityFrameOpen = true;
					} catch (ParseException e) {
						Main.logger.severe(e.toString());
					}

					/*
					 * add the chart and the ScrollPane (the principal panel of DisplayCurve) to the
					 * window
					 */
					hydrogenaseActivityFrame.add(chart.getPanel(), BorderLayout.CENTER);
					hydrogenaseActivityFrame.getContentPane().setBackground(Color.white);
					hydrogenaseActivityFrame.setIconImage(img.getImage());

					/*
					 * when we close the frame, we stopped the thread and disable the
					 * hydrogenaseActivityCurve button, until the thread had stopped
					 */
					hydrogenaseActivityFrame.addWindowListener(new WindowAdapter() {
						public void windowClosing(WindowEvent e) {
							hydrogenaseActivityFrameOpen = false;
							hydrogenaseActivityCurve.setEnabled(false);

						}
					});

					/* create the buttons (menu bar) of the frame */
					SaveHydrogenaseActivityData saveHydrogenaseActivityData = new SaveHydrogenaseActivityData(
							"Save Data");
					Pause pause = new Pause("Pause");
					Play play = new Play("Play");

					JMenu file = new JMenu("File");
					JMenu edit = new JMenu("Edit");
					JMenuBar menu = new JMenuBar();

					file.add(saveHydrogenaseActivityData);
					edit.add(play);
					edit.add(pause);

					menu.add(file);
					menu.add(edit);

					/* add elements to the frame */
					hydrogenaseActivityFrame.setJMenuBar(menu);
					hydrogenaseActivityFrame.pack();
					hydrogenaseActivityFrame.setVisible(true);

					/* reload the chart if new data arrived */
					while (working && hydrogenaseActivityFrameOpen) {
						try {
							Thread.sleep(1500);
							if (newHydrogenaseActivityData) {
								/* add new data */
								chart.addData(hydrogenaseActivityData, nbRow - 1);
								newHydrogenaseActivityData = false;

							}
						} catch (InterruptedException e) {
							Main.logger.severe(e.toString());
						}
					}
					hydrogenaseActivityCurve.setEnabled(true);
				}
			};
			tb.start();
			threads.add(tb);
		}
	}

	/**
	 * Define the csv filter for the JFileChooser
	 */

	class CsvFilter extends javax.swing.filechooser.FileFilter {
		public String getDescription() {
			return ".csv";
		}

		public boolean accept(File f) {
			/* check if ends with .csv of if a directory */
			return (f.isDirectory() || f.getAbsolutePath().endsWith(".csv"));
		}

	}

	/**
	 * Define the xls/xlsx filter for the JFileChooser
	 */
	class XlsFilter extends javax.swing.filechooser.FileFilter {
		public String getDescription() {
			return ".xls or .xlsx";
		}

		public boolean accept(File f) {
			return (f.isDirectory() || f.getAbsolutePath().endsWith(".xls") || f.getAbsolutePath().endsWith(".xlsx"));
		}

	}

	/**
	 * The 10 following class are action which save corresponding data to an xlsx
	 * file with the function savedataToExcel The code between each one look really
	 * like the same.
	 */

	/**
	 * Save the amperometric/time Data in an xlsx file
	 */
	class SaveAmperometricData extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public SaveAmperometricData(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				/* save data thanks to the saveDataToexcel function */
				saveDataToExcel(amperometricColumnName, amperometricData);
				Main.logger.info("Save Amperometric data");

			} catch (Exception e1) {
				Main.logger.warning("Error while saving data: " + e1.toString());
				infoBox(amperometricFrame,
						"There is an issue with your saving file: " + "\n"
								+ "Please verify that your file isn't opened somewhere or used by an other program !",
						"Saving");
			}
		}

	}

	/**
	 * Save the gasConcentration/time Data in an xlsx file
	 */
	class SaveGasConcentrationData extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public SaveGasConcentrationData(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				/* save data thanks to the saveDataToexcel function */
				saveDataToExcel(gasConcentrationColumnName, gasConcentrationData);
				Main.logger.info("Save Gas Concentration Data");

			} catch (Exception e1) {
				Main.logger.warning("Error while saving data: " + e1.toString());
				infoBox(gasConcentrationFrame,
						"There is an issue with your saving file: " + "\n"
								+ "Please verify that your file isn't opened somewhere or used by an other program !",
						"Saving");
			}
		}

	}

	/**
	 * Save the cumulatedGasExchange/time Data in an xlsx file
	 */
	class SaveCumulatedGasExchangeData extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public SaveCumulatedGasExchangeData(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				/* save data thanks to the saveDataToexcel function */
				saveDataToExcel(gasConcentrationColumnName, cumulatedGasExchangeData);
				Main.logger.info("Save Cumulated Gas Exchange Data");

			} catch (Exception e1) {
				Main.logger.warning("Error while saving data: " + e1.toString());
				infoBox(cumulatedGasExchangeFrame,
						"There is an issue with your saving file: " + "\n"
								+ "Please verify that your file isn't opened somewhere or used by an other program !",
						"Saving");
			}
		}

	}

	/**
	 * Save the gasExchangeRate/time Data in an xlsx file
	 */
	class SaveGasExchangeRatesData extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public SaveGasExchangeRatesData(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				/* save data thanks to the saveDataToexcel function */
				saveDataToExcel(gasExchangeRatesColumnName, gasExchangeRatesData);
				Main.logger.info("Save Gas Exchange Rates Data");

			} catch (Exception e1) {
				Main.logger.warning("Error while saving data: " + e1.toString());
				infoBox(gasExchangeRatesFrame,
						"There is an issue with your saving file: " + "\n"
								+ "Please verify that your file isn't opened somewhere or used by an other program !",
						"Saving");
			}
		}

	}

	/**
	 * Save the DenoisedcumulatedGasExchange/time Data in an xlsx file
	 */
	class SaveDenoisedCumulatedGasExchangeData extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public SaveDenoisedCumulatedGasExchangeData(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				/* save data thanks to the saveDataToexcel function */
				saveDataToExcel(denoisedCumulatedGasExchangeColumnName, denoisedCumulatedGasExchangeData);
				Main.logger.info("Save Denoised Cumulated Gas Exchange Data");

			} catch (Exception e1) {
				Main.logger.warning("Error while saving data: " + e1.toString());
				infoBox(denoisedCumulatedGasExchangeFrame,
						"There is an issue with your saving file: " + "\n"
								+ "Please verify that your file isn't opened somewhere or used by an other program !",
						"Saving");
			}
		}

	}

	/**
	 * Save the denoisedGasExchangeRate/time Data in an xlsx file
	 */
	class SaveDenoisedGasExchangeRatesData extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public SaveDenoisedGasExchangeRatesData(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				/* save data thanks to the saveDataToexcel function */
				saveDataToExcel(denoisedGasExchangeRatesColumnName, denoisedGasExchangeRatesData);
				Main.logger.info("Save Denoised Gas Exchange Rates Data");

			} catch (Exception e1) {
				Main.logger.warning("Error while saving data: " + e1.toString());
				infoBox(denoisedGasExchangeRatesFrame,
						"There is an issue with your saving file: " + "\n"
								+ "Please verify that your file isn't opened somewhere or used by an other program !",
						"Saving");
			}
		}

	}

	/**
	 * Save the O2ExchangeRates/time Data in an xlsx file
	 */
	class SaveO2ExchangeRatesData extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public SaveO2ExchangeRatesData(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				/* save data thanks to the saveDataToexcel function */
				saveDataToExcel(o2ExchangeRatesColumnName, o2ExchangeRatesData);
				Main.logger.info("Save O2 Exchange Rates Data");

			} catch (Exception e1) {
				Main.logger.warning("Error while saving data: " + e1.toString());
				infoBox(o2ExchangeRatesFrame,
						"There is an issue with your saving file: " + "\n"
								+ "Please verify that your file isn't opened somewhere or used by an other program !",
						"Saving");
			}
		}

	}

	/**
	 * Save the O2ExchangeData/time Data in an xlsx file
	 */
	class SaveO2ExchangeData extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public SaveO2ExchangeData(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				/* save data thanks to the saveDataToexcel function */
				saveDataToExcel(o2ExchangeColumnName, o2ExchangeData);
				Main.logger.info("Save O2 Exchange Data");

			} catch (Exception e1) {
				Main.logger.warning("Error while saving data: " + e1.toString());
				infoBox(o2ExchangeFrame,
						"There is an issue with your saving file: " + "\n"
								+ "Please verify that your file isn't opened somewhere or used by an other program !",
						"Saving");
			}
		}

	}

	/**
	 * Save the gasExchangeRateFunctionConcentration Data in an xlsx file
	 */
	class SaveGasExchangeRateFunctionConcentrationData extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public SaveGasExchangeRateFunctionConcentrationData(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				/* save data thanks to the saveDataToexcel function */
				saveDataToExcel(gasExchangeRateFunctionConcentrationColumnName,
						gasExchangeRateFunctionConcentrationData);
				Main.logger.info("Save Gas Exchange Rate Function Concentration Data");

			} catch (Exception e1) {
				Main.logger.warning("Error while saving data: " + e1.toString());
				infoBox(gasExchangeRateFunctionConcentrationFrame,
						"There is an issue with your saving file: " + "\n"
								+ "Please verify that your file isn't opened somewhere or used by an other program !",
						"Saving");
			}
		}

	}

	/**
	 * Save the HydrogenaseActivity/time Data in an xlsx file
	 */
	class SaveHydrogenaseActivityData extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public SaveHydrogenaseActivityData(String s) {
			super(s);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				/* save data thanks to the saveDataToexcel function */
				saveDataToExcel(hydrogenaseActivityColumnName, hydrogenaseActivityData);
				Main.logger.info("Save Hydrogenase Activity Data");

			} catch (Exception e1) {
				Main.logger.warning("Error while saving data: " + e1.toString());
				infoBox(hydrogenaseActivityFrame,
						"There is an issue with your saving file: " + "\n"
								+ "Please verify that your file isn't opened somewhere or used by an other program !",
						"Saving");
			}
		}

	}

	/**
	 * Function to save data in an xlsx file
	 * 
	 * @param columns:
	 *            title of the columns of the file
	 * @param data:
	 *            data to save in the file
	 * @throws IOException
	 * @throws InvalidFormatException
	 **/
	public void saveDataToExcel(String[] columns, List<Double[]> data) throws IOException, InvalidFormatException {
		Main.logger.info("Saving Data");
		/*
		 * open a new File chooser, so the user chose where to save the data (savePath
		 * is already in memory)
		 */
		JFileChooser fc = new JFileChooser(savePath);
		fc.setDialogTitle("Save the data in xlsx");
		fc.addChoosableFileFilter(new XlsFilter());
		fc.setAcceptAllFileFilterUsed(false);
		/* if the user have choose the save file or folder */
		if (fc.showSaveDialog(Window.this) == JFileChooser.APPROVE_OPTION) {
			/* get the save path and is parent folder */
			String path = fc.getSelectedFile().getAbsolutePath();

			/* edit savePath if necessary */

			if (savePath != null && savePath.contentEquals(fc.getSelectedFile().getParent()) == false
					|| savePath == null) {
				savePath = fc.getSelectedFile().getParent();
				try {
					BufferedWriter buffWrite = new BufferedWriter(new FileWriter("path"));
					buffWrite.write(loadPath);
					buffWrite.write('\n');
					buffWrite.write(savePath);
					buffWrite.write('\n');
					buffWrite.write(factorPath);
					buffWrite.close();
				} catch (IOException e3) {
					Main.logger.severe("Error while saving data: " + e3.toString());
				}

			}

			/*
			 * if the user didn't choose an existing file, had .xlsx at the the end of the
			 * path if necessary
			 */
			if (path.endsWith(".xls") == false && path.endsWith(".xlsx") == false) {
				path = path + ".xlsx";
			}

			/* Create a Workbook */
			Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

			/* Create a Sheet */
			Sheet sheet = workbook.createSheet("Data Saved");

			/* Create a Font for styling header cells */
			org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 14);
			headerFont.setColor(IndexedColors.RED.getIndex());

			/* Create a CellStyle with the font */
			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont((org.apache.poi.ss.usermodel.Font) headerFont);

			/* Create the first Row */
			Row headerRow = sheet.createRow(0);

			/* Create the first line of cells with columns */
			for (int i = 0; i < columns.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerCellStyle);
			}

			/* Create Other rows and cells */
			int rowNum = 1;
			/* create the rows */
			for (int i = 0; i < data.size(); i++) {
				Row row = sheet.createRow(rowNum++);
				/* create the cells of each row */
				for (int j = 0; j < columns.length; j++) {
					/* create a cell with the corresponding data */
					row.createCell(j).setCellValue(data.get(i)[j]);
				}
			}

			/* Resize all columns to fit the content size */
			for (int i = 0; i < columns.length; i++) {
				sheet.autoSizeColumn(i);
			}

			/* Write the output to the file, with the path */
			FileOutputStream fileOut = new FileOutputStream(path);
			workbook.write(fileOut);
			fileOut.close();

			/* Closing the workbook */
			workbook.close();
			Main.logger.info("Data Saved");
		}

	}

	/*
	 * infoBox which display an error message in front of the corresponding window
	 */
	public static void infoBox(JFrame window, String infoMessage, String titleBar) {
		JOptionPane.showMessageDialog(window, infoMessage, "Warning " + titleBar, JOptionPane.ERROR_MESSAGE);

	}

	/*
	 * infoBox which display an error message in front of the corresponding panel
	 * (JTable)
	 */
	public static void infoBox(JPanel panel, String infoMessage, String titleBar) {
		JOptionPane.showMessageDialog(panel, infoMessage, "Warning " + titleBar, JOptionPane.ERROR_MESSAGE);

	}
}