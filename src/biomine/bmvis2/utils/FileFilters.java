/*
 * Copyright 2012 University of Helsinki.
 *
 * This file is part of BMVis².
 *
 * BMVis² is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * BMVis² is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BMVis².  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package biomine.bmvis2.utils;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileFilters {
	public static class GraphFileFilter extends FileFilter {
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			String ext = f.getName().replaceFirst("^([^.]*[.])*", "");
			return ("bmg".equalsIgnoreCase(ext) || "txt".equalsIgnoreCase(ext) || "bmgraph"
					.equalsIgnoreCase(ext));
		}

		public String getDescription() {
			return "Biomine graphs (*.bmg, *.bmgraph, *.txt)";
		}
	}
	public static final GraphFileFilter FILTER_GRAPH = new GraphFileFilter();

	public static class PNGFileFilter extends FileFilter {
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			String ext = f.getName().replaceFirst("^([^.]*[.])*", "");
			return ("png".equalsIgnoreCase(ext));
		}

		public String getDescription() {
			return "Portable Network Graphics (*.png)";
		}
	}
	public static final PNGFileFilter FILTER_PNG = new PNGFileFilter();
	
	public static class JSONFileFilter extends FileFilter {
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			String ext = f.getName().replaceFirst("^([^.]*[.])*", "");
			return ("json".equalsIgnoreCase(ext));
		}

		public String getDescription() {
			return "JavaScript Object Notation (*.json)";
		}
	}
	public static final JSONFileFilter FILTER_JSON = new JSONFileFilter();
}
