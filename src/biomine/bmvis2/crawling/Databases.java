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
