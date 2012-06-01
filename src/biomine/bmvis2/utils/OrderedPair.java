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
