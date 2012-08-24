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

package biomine.nodeimportancecompression;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SimpleHashMap<T,U> {
	public static class SHMEntry<T,U> implements Map.Entry<T, U>{
		protected SHMEntry(T t,U u,SHMEntry<T,U> n){
			key = t;
			value = u;
			next = n;
		}
		private SHMEntry<T,U> next;
		private T key;
		private U value;
		@Override
		public T getKey() {
			return key;
		}
		@Override
		public U getValue() {
			return value;
		}
		@Override
		public U setValue(U value) {
			U r = this.value;
			this.value=value;
			return r;
		}
		protected SHMEntry<T,U> getNext(){
			return next;
		}
	};
	
	SHMEntry<T, U>[] arr;
	int szp;
	private int count;
	
	private int idx(T o,int s){
		int h = o.hashCode();
		int k = 1327217885;
		int mask = (1<<s)-1;
		h = (k*h) & mask;
		return h;
	}
	private int idx(T o){
		return idx(o,szp);
	}
	
	
	
	@SuppressWarnings("unchecked")
	public SimpleHashMap() {
		arr = new SHMEntry[4];
		szp = 2;
		count = 0;
	}
	@SuppressWarnings("unchecked")
	private void grow(){
		szp++;
		SHMEntry<T,U>[] old = arr;
		arr = new SHMEntry[1<<szp];
		for(int i=0;i<old.length;i++){
			for(SHMEntry<T,U> j=old[i];j!=null;){
				int h = idx(j.getKey());
				SHMEntry<T, U> nj = j.getNext();
				j.next = arr[h];
				arr[h] = j;
				j = nj;
			}
		}
	}
	
	public SHMEntry<T, U> getEntry(T t){
		SHMEntry<T,U> j = arr[idx(t)];
		while(j!=null && !t.equals(j.getKey()))
			j = j.getNext();
		return j;
	}
	
	public SHMEntry<T, U> put(T t,U u){
		
		SHMEntry<T, U> n ;
		if((n = getEntry(t))!=null){
			n.setValue(u);
			return n;
		}
		
		int i = idx(t);
		n = new SHMEntry<T, U>(t,u,arr[i]);
		arr[i] = n;
		count++;
		
		if(count*4 > arr.length*3){
			grow();
		}
		
		return n;
	}
	
	public void remove(SHMEntry<T,U> e){
		
		int i = idx(e.getKey());
		SHMEntry<T,U> j = arr[i];
		count--;
		if(j==e){
			arr[i] = e.getNext();
			return;
		}
		while(j!=null){
			if(j.getNext()==e){
				j.next = e.getNext();
				return;
			}
			j =  j.getNext();
		}
		throw new IllegalArgumentException("Entry was not found from map");
	}
}
