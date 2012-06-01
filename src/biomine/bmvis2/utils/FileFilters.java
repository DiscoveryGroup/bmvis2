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
