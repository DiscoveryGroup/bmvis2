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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import biomine.bmgraph.BMGraph;
import biomine.bmgraph.BMGraphUtils;

/**
 * Pulls graph data from crawlers web api.
 * @author alhartik
 *
 */
public class CrawlerFetch {

	
	private URL statusUrl;

	public CrawlerFetch(Collection<String> queryNodes,boolean onlyNeighborHood,String database) throws IOException {
		
		URL queryUrl = null;

		queryUrl = new URL(WebConstants.DEFAULT_BASEURL + "/query.cgi");
		StringBuffer startNodesBuf = new StringBuffer();
		
		for (String nod : queryNodes)
			startNodesBuf.append(nod.replace(" ","\\ ") + " ");
		
		if(queryNodes.isEmpty()==false)
			startNodesBuf.deleteCharAt(startNodesBuf.length() - 1);

		String key=onlyNeighborHood?"end_nodes":"start_nodes";
		
		HashMap<String,String> parameters = new HashMap<String, String>();
		parameters.put(key, startNodesBuf.toString());
		if(database!=null)
			parameters.put("database", database);
		String qc = URLUtils.getURLContents(queryUrl,parameters);
		
		queryId = qc.split("\"")[3];
		
//		System.out.println("q = " + queryId);

		statusUrl = new URL(WebConstants.DEFAULT_BASEURL + "/status.cgi?query=" + queryId);
	}
	private void d(Object o){
		//System.err.println("QUERY:"+o.toString());
	}
	private String queryId;
	String status;
	JSONObject statusObject;
	ArrayList<String> messages = new ArrayList<String>();

	int lastage=-1;
	public void update() throws IOException {

		status = URLUtils.getURLContents(statusUrl);
		statusObject = (JSONObject) JSONValue.parse(status);
		JSONObject obj = (JSONObject) JSONValue.parse(status);
		JSONArray errors = ((JSONArray) obj.get("errors"));

		d("obj = "+obj);			
		int age = ((Long)obj.get("age")).intValue();
		d("age = "+age);

		lastage=age;
		messages.clear();
		for (Object o : errors) {
			JSONObject error = (JSONObject) o;
			JSONArray mesarr = (JSONArray) error.get("messages");

			for (int i = 0; i < mesarr.size(); i++){
				String err = mesarr.get(i).toString();
				messages.add(err);
				d("err = "+err);
			}
		}
		
		JSONArray mesarr = (JSONArray) obj.get("messages");

		for (int i = 0; i < mesarr.size(); i++){
			String err = mesarr.get(i).toString();
			messages.add(err);
			d("err = "+err);
		}
	}

	public String getMessages() {
		StringBuffer ret = new StringBuffer();
		for(String str:messages){
			ret.append(str);
			ret.append("\n");
		}
		return ret.toString();
	}

	public boolean isReady() {
		if(statusObject==null)return false;
		String state = statusObject.get("status").toString();

		return state.equals("completed");
	}
	public boolean isError(){
		if(statusObject==null)return false;
		String state = statusObject.get("status").toString();
		return state.matches("errors?");
	}
	public boolean isDone(){
		return isReady()||isError();
	}
	public BMGraph getBMGraph() throws IOException {
		if(!isReady())return null;
		URL graphUrl = new URL(WebConstants.DEFAULT_BASEURL+"download.cgi?file="+queryId+"/graph.bmg");
		InputStream is = graphUrl.openConnection().getInputStream();
		return BMGraphUtils.readBMGraph(is);
		
	}

	
	public String getState() {
		if(statusObject==null)return "";
		return statusObject.get("status").toString();
	}

//	public static void main(String[] args) throws IOException,
//			InterruptedException {
//
//		CrawlerQuery c = new CrawlerQuery(Collections.singleton("APP"));
//		while(!c.isReady()&&!c.isError()){
//			c.update();
//
//			String messages = c.getMessages();
//				System.out.println(messages);
//		
//			Thread.sleep(1000);
//		}
//		if(c.isReady()){
//			BMGraph graph = c.getBMGraph();
//			System.out.println(graph.getNodes());
//		}
//	}
}
