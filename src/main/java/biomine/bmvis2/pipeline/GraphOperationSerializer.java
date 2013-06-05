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

package biomine.bmvis2.pipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GraphOperationSerializer {

	public static GraphOperation fromJSON(JSONObject o)
			throws GraphOperationSerializationException {

		try {
			String className = o.get("class").toString();

			Class cl = GraphOperationSerializer.class.getClassLoader()
					.loadClass(className);

			GraphOperation ret = (GraphOperation) cl.newInstance();
			JSONObject retJSON = (JSONObject) o.get("object");
			ret.fromJSON(retJSON);
			return ret;
		} catch (Exception e) {
			throw new GraphOperationSerializationException(e);
		}
	}

	private static boolean isOperation(JSONObject o) {
		return o.containsKey("class");
	}

	private static List<GraphOperation> loadList(JSONArray arr)
			throws GraphOperationSerializationException {
		ArrayList<GraphOperation> ret = new ArrayList<GraphOperation>();
		for (Object o : arr) {
			if (o instanceof JSONObject) {
				JSONObject jo = (JSONObject) o;
				GraphOperation op = fromJSON(jo);
				ret.add(op);
			}
		}
		return ret;
	}

	public static List<GraphOperation> loadList(File f)
			throws FileNotFoundException, GraphOperationSerializationException {

		FileReader rd = new FileReader(f);
		BufferedReader br = new BufferedReader(rd);
		JSONArray arr = (JSONArray) JSONValue.parse(br);
		return loadList(arr);
	}

	private static Map<String, Double> getNodesOfInterest(JSONArray arr) {
		Map<String, Double> ret = new HashMap();
		for (Object o : arr) {
			if (o instanceof JSONArray) {
				JSONArray a = (JSONArray) o;
				for (Object ao : a) {
					JSONObject jo = (JSONObject) ao;
					ret
							.put(jo.get("node").toString(), (Double) jo
									.get("value"));
				}
			}
		}
		return ret;
	}

	public static Map<String, Double> getNodesOfInterest(File f)
			throws FileNotFoundException {
		FileReader rd = new FileReader(f);
		BufferedReader br = new BufferedReader(rd);
		JSONArray arr = (JSONArray) JSONValue.parse(br);
		return getNodesOfInterest(arr);
	}

	public static Map<String, Double> getNodesOfInterest(String json) {
		JSONArray arr = (JSONArray) JSONValue.parse(json);
		return getNodesOfInterest(arr);
	}

	public static List<GraphOperation> loadList(String json)
			throws GraphOperationSerializationException {

		JSONParser par = new JSONParser();
		JSONArray arr = null;

		try {
			System.out.println("obj = " + par.parse(json));
			arr = (JSONArray) par.parse(json);
		} catch (ParseException e) {
			System.out.println(e);
		}
		return loadList(arr);

	}

	public static void saveList(Collection<GraphOperation> ops,File f,Map<String,Double> queryNodes) throws IOException{
		FileWriter wr = new FileWriter(f);
		JSONArray arr= new JSONArray();
		for(GraphOperation op:ops){
			JSONObject obj = op.toJSON();
			JSONObject mark = new JSONObject();
			mark.put("class", op.getClass().getName());
			mark.put("object",obj);
			arr.add(mark);
		}
		
		JSONArray noi = new JSONArray();
		for(String z : queryNodes.keySet()){
			JSONObject jo = new JSONObject();
			jo.put("node",z);
			Double val = queryNodes.get(z);
			jo.put("value",val);
			noi.add(jo);
		}
		arr.add(noi);
		
		arr.writeJSONString(wr);
		wr.close();
	}
}
