package biomine.bmvis2.algoutils;

import java.util.AbstractSet;
import java.util.Iterator;

public class RangeSet extends AbstractSet<Integer>{
	private int start,end;
	
	public RangeSet(int a,int b){
		start=a;
		end=b;
	}
	
	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			private int val=start;
			@Override
			
			public boolean hasNext() {
				return val!=end;
			}

			@Override
			public Integer next() {
				return val++;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	@Override
	public int size() {
		return end-start;
	}
	
}