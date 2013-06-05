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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class AlgoUtils {
	public static <T> Collection<T> take(Collection<T> c,int i){
		Collection<T> ret = new LinkedHashSet();
		for(T t:c){
			if(i==0)break;
			i--;
			ret.add(t);
		}
		return ret;
	}
	
	
	private static void test(){
		List<String> sl = new ArrayList<String>();
		sl.add("kissa");
		sl.add("koira");
		
		Collection<String> k = take(sl,1);
	}
}
