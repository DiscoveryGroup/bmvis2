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

public final class OrderedPair<T> implements Comparable<OrderedPair>{
	public final T first;
	public final T second;
	
	public OrderedPair(T a,T b){
		Comparable cf = (Comparable) a;
		if(cf.compareTo(b)<0){
			first = (T) a;
			second = (T) b;
		}else
		{
			first = (T) b;
			second = (T) a;
		}
	}
	
	@Override
	public int compareTo(OrderedPair o) {
		Comparable cf = (Comparable) first;
		int r = cf.compareTo(o.first);
		if(r!=0)return r;
		Comparable cs = (Comparable)second;
		r = cs.compareTo(o.second);
		return r;
	}
	
	@Override
	public boolean equals(Object o){
		if(o.getClass()==this.getClass()){
			OrderedPair<T> z = (OrderedPair<T>) o;
			return z.first.equals(first) && z.second.equals(second);
		}
		return false;
	}
	@Override
	public int hashCode(){
		return first.hashCode()^Integer.rotateLeft(second.hashCode(), 16);
	}
 }
