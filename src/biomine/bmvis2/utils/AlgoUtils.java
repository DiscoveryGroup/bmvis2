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
