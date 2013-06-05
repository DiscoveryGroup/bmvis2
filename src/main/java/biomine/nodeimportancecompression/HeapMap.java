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

import java.util.ArrayList;

import biomine.nodeimportancecompression.SimpleHashMap.SHMEntry;

public class HeapMap<T,U extends Comparable<U>>{
	
	private static class Value<U>{
		U val;
		int idx;
	}
	
	SimpleHashMap<T,Value<U>> map;
	
	ArrayList<SHMEntry<T, Value<U>>> heap;
	
	
	public HeapMap() {
		heap = new ArrayList<SHMEntry<T,Value<U>>>();
		map = new SimpleHashMap<T, Value<U>>();
	}
	private void swap(int x,int y){
		
		SHMEntry<T,Value<U>> a = heap.get(x);
		SHMEntry<T,Value<U>> b = heap.get(y);
		
		heap.set(x,b);
		heap.set(y,a);
		
		heap.get(x).getValue().idx=x;
		heap.get(y).getValue().idx=y;
	}
	private int cmp(int x,int y){
		U a = heap.get(x).getValue().val;
		U b = heap.get(y).getValue().val;
		return a.compareTo(b);
	}

	private void bubble_down(int x){
		x++;
		while(x*2<heap.size()){
			int x2;
			if(cmp(x*2,x*2-1)<0){
				x2 = x*2;
			}else{
				x2 = x*2-1;
			}
			if(cmp(x-1,x2)>0){
				swap(x-1, x2);
				x = x2+1;
			}else{
				break;
			}
		}
		if(x*2==heap.size()){
			if(cmp(x-1,x*2-1)>0){
				swap(x-1,x*2-1);
			}
		}
	}
	private void bubble_up(int x){
		x++;
		while(x>1){
			//T a = heap.get(x-1);
			//T b = heap.get(x/2-1);
			if(cmp(x-1,x/2-1)<0){
				swap(x-1,x/2-1);
				x/=2;
			}else{
				break;
			}
		}
	}
	
	/**
	 * Insert new items to queue. If t is already in queue, modifies its
	 * value and adjusts heap appropriately
	 * @param t item to add
	 */
	
	public void put(T t,U u){
		
		SHMEntry<T, Value<U>> e = map.getEntry(t);
		if(e==null){
			Value<U> v = new Value<U>();
			v.val = u;
			v.idx = heap.size();
			e = map.put(t, v);
			heap.add(e);
			bubble_up(heap.size()-1);
		}else{
			U ou = e.getValue().val;
			e.getValue().val = u;
			if(ou.compareTo(u) < 0){
				//new value is larger, bubble down
				bubble_down(e.getValue().idx);
			}else{
				//new value is smaller, bubble up
				bubble_up(e.getValue().idx);
			}
		}
	}
	
	public U get(T t){
		SHMEntry<T, Value<U>> e = map.getEntry(t);
		if(e==null)return null;
		return e.getValue().val;
		
	}
	
	public boolean isEmpty(){
		return heap.size()==0;
	}
	public int size(){
		return heap.size();
	}
	public T topKey(){
		return heap.get(0).getKey();
	}
	public U topValue(){
		return heap.get(0).getValue().val;
	}
	/**
	 * Removes top element from queue (and map)
	 */
	public void pop(){
		swap(0, heap.size()-1);
		map.remove(heap.get(heap.size()-1));
		heap.remove(heap.size()-1);
		bubble_down(0);
	}
	/**
	 * Remove t from queue.
	 * @param t
	 */
	public void remove(T t){
		SHMEntry<T, Value<U>> e = map.getEntry(t);
		if(e==null)return;
		int i = e.getValue().idx;
		U ou = heap.get(i).getValue().val;
		U u = heap.get(heap.size()-1).getValue().val;
		swap(i, heap.size()-1);
		map.remove(heap.get(heap.size()-1));
		heap.remove(heap.size()-1);
		if(u.compareTo(ou)<0){
			//new key in i is smaller
			bubble_up(i);
		}else{
			//new key in i is larger
			bubble_down(i);
		}
	}
	boolean checkIntegrity(){
		for(int i=0;i<heap.size();i++){
			if(heap.get(i).getValue().idx!=i)throw new RuntimeException("Error with idx");
		}
		return true;
	}
	public boolean containsKey(T r) {
		return map.getEntry(r)!=null;
	}
}
