package biomine.bmvis2.help;

import biomine.bmvis2.Logging;
import biomine.bmvis2.Vis;
import biomine.bmvis2.utils.ResourceUtils;
//import com.sun.jndi.toolkit.url.UrlUtil;

import java.awt.Component;
import java.io.*;
import java.net.URL;

import javax.swing.JOptionPane;

public class Help {
	private static String mouseHelp;

	static {
		try {
            ResourceUtils.printResourceFiles(Vis.class, "resources/");
            URL u = Vis.class.getResource("resources/mousehelp.html");
            Logging.debug("ui", "u: " + u + ",  u.getFile(): ");
            File f = new File("resources/mousehelp.html");
			BufferedReader br = new BufferedReader(new FileReader(f));
			try {
				StringBuilder bld = new StringBuilder();
				while (true) {
					String line = br.readLine();
					if (line == null)
						break;
					bld.append(line);
				}
				mouseHelp = bld.toString();

			} finally {
				br.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void showHelp(Component parent) {
		if (mouseHelp == null)
			return;
		JOptionPane.showMessageDialog(parent, mouseHelp);
	}
}
