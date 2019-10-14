package software;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
Copyright (C) 2019-F.Burlacot

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see: https://www.gnu.org/licenses/.**/

/**
 * Main class of our software
 */
public class Main {
	/* logger and fileHandler for the all software */
	public static Logger logger;
	public static FileHandler fh;

	/**
	 * Main Method of our program, creating a new Window
	 */
	public static void main(String[] args) {

		/* logger of our program */
		logger = Logger.getLogger("MIMS_Analysis");

		try {

			/*
			 * create a file log for each currently running instance of our program. If a
			 * file "MIMS_Analysis_#number#.log" exist but isn't used by a fileHandler, will
			 * overwrite it.
			 */

			/* f serves to check if a fileHandler is open (file handler end with .lck */
			File f = new File("MIMS_Analysis_1.log.lck");
			String logPath = "MIMS_Analysis_1.log";
			int index = 2;
			while ((f.exists())) {
				f = new File("MIMS_Analysis_" + index + ".log.lck");
				logPath = "MIMS_Analysis_" + index + ".log";
				index++;
			}
			/* configure the handler and formatter */
			fh = new FileHandler(logPath);
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

		} catch (SecurityException | IOException e) {
			Main.logger.info("Error: " + e.toString());
		}
		try {
			/* creating the main window */
			logger.info("Creating the main Pannel");
			new Window();
		} catch (Exception e) {
			Main.logger.severe("Error: " + e.toString());
			System.exit(1);
		}
	}
}
