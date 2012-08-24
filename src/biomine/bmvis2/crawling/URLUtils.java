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

package biomine.bmvis2.crawling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utilities for fetching data using http.
 * @author alhartik
 *
 */
public class URLUtils {
	
	public static String getURLContents(URL u, Map<String, String> data)
			throws IOException {

		URLConnection conn = u.openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter postWriter = new OutputStreamWriter(conn
				.getOutputStream());

		boolean first = true;

		for (Entry<String, String> ent : data.entrySet()) {
			if (!first)
				postWriter.write("&");
			String set = URLEncoder.encode(ent.getKey(), "UTF-8") + "="
					+ URLEncoder.encode(ent.getValue(), "UTF-8");
			postWriter.write(set);
			System.out.println(set);
			first=false;
		}
		postWriter.flush();

		conn.setReadTimeout(10000);
		InputStream istream = conn.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				istream));
		String line;
		StringBuffer ret = new StringBuffer();
		
		System.out.println("startRead");
		while ((line = reader.readLine()) != null){
			ret.append(line);
		}
		System.out.println("endRead");
		
		return ret.toString();
	}
	public static String getURLContents(URL queryUrl) throws IOException {
		return getURLContents(queryUrl, Collections.EMPTY_MAP);
	}

}
