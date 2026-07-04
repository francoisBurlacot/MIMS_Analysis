package software;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
Copyright (C) 2019-F.Burlacot

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see: https://www.gnu.org/licenses/.**/

/**
 * Persists user preferences (normalization factor, pKa constants, display
 * options, UI theme) between runs, in a small properties file next to the
 * jar. Separate from the older "path" file, which keeps remembering the last
 * used folders.
 */
class Settings {
	private static final String FILE_NAME = "mims_analysis.properties";

	private Settings() {
	}

	/**
	 * Load saved settings, or an empty Properties object if none exist yet.
	 */
	static Properties load() {
		Properties properties = new Properties();
		try (FileInputStream in = new FileInputStream(FILE_NAME)) {
			properties.load(in);
		} catch (IOException e) {
			Main.logger.info("No saved settings found (" + e.toString() + "), using defaults");
		}
		return properties;
	}

	/**
	 * Save settings, overwriting the previous file.
	 */
	static void save(Properties properties) {
		try (FileOutputStream out = new FileOutputStream(FILE_NAME)) {
			properties.store(out, "MIMS Analysis settings");
		} catch (IOException e) {
			Main.logger.severe("Could not save settings: " + e.toString());
		}
	}
}
