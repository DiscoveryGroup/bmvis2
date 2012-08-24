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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Fetches info of existing databases.
 * @author alhartik
 *
 */
public class Databases {

	private static ArrayList<String> dbs =null;
	
	public static Collection<String> getDatabases(){
		if(dbs!=null)return dbs;
		
		try {
			String url = WebConstants.BIOMINE_URL+"stats/index.cgi?json_action=getdbs";
			String cont = URLUtils.getURLContents(new URL(url));
			Object arr = JSONValue.parse(cont);
			if(arr instanceof JSONArray){
				JSONArray jarr = (JSONArray)arr;
				dbs = new ArrayList();
				for(Object dbo:jarr){
					JSONObject jdb = (JSONObject)dbo;
					Object no = jdb.get("name");
					if(no!=null)
						dbs.add(no.toString());
				}
				Collections.reverse(dbs);
				return dbs;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassCastException e) {
			// TODO: handle exception
		}
		return Collections.EMPTY_LIST;
	}
	
//	public static void main(String... args){
//		for(String db:getDatabases()){
//			System.out.println(db);
//		}
//	}
//	public static void registerDatabase(String db){
//		dbs.add(db);
//	}
}
