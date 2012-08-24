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

package biomine.bmvis2;

import java.awt.geom.Point2D;
import java.util.Random;

/**
 * Simple immutable  2d vector class. Might be a bit slow sometimes, as
 * operations create new instances.
 * @author alhartik
 *
 */
public final class Vec2 {
	public static final Vec2 ZERO = new Vec2(0,0);
	public final double x,y;
    
    public Vec2(double xx,double yy) {
        x=xx;
        y=yy;
    }
    
    public Vec2(Point2D p2) {
		x = p2.getX();
		y = p2.getY();
	}

    public Vec2(String xCommaYString) {
        String[] parts = xCommaYString.split(",");
        x = Double.parseDouble(parts[0]);
        y = Double.parseDouble(parts[1]);
    }

    /*
	public Vec2 copy()
    {
        Vec2 v = new Vec2(x,y);
        return v;
    }
    */

    public double length2()  {
        return (x*x+y*y);
    }

    public double dist(Vec2 v) {
    	return Math.sqrt((v.x-x)*(v.x-x)+(v.y-y)*(v.y-y));
    }

    public double length() {
        return Math.sqrt(length2());
    }

    public double dot(Vec2 o) {
        return x*o.x+y*o.y;
    }

    public Vec2 plus(Vec2 o) {
        Vec2 ret = new Vec2(x+o.x,y+o.y);
        return ret;
    }

    public Vec2 minus(Vec2 o) {
        Vec2 ret = new Vec2(x-o.x,y-o.y);
        return ret;
    }

    public Vec2 minus(double ax,double ay) {
        Vec2 ret = new Vec2(x-ax,y-ay);
        return ret;
    }

    /*
    public void scale(double d)
    {
        x*=d;
        y*=d;
        y*=d;
    }
    */
    public Vec2 scaled(double d)
    {
        Vec2 v = new Vec2(x*d,y*d);
        return v;
    }
    /*
    public void add(Vec2 v)
    {
    	x+=v.x;
    	y+=v.y;
    }
    public void subtract(Vec2 v)
    {
    	x-=v.x;
    	y-=v.y;
    }
    */
    Vec2 normal(){
    	return new Vec2(y,-x);
    }
    
    public String toString(){
    	return ""+x+","+y;
    }
    
    public boolean equals(Object o){
    	if(o instanceof Vec2){
    		Vec2 v = (Vec2)o;
    		return x==v.x && y==v.y;
    	
    	}
    	return false;
    }
    
    public int hashCode(){
    	return new Double(x).hashCode()^new Double(y+Math.PI*Math.E).hashCode();
    }
    
    public Point2D toPoint(){
    	return new Point2D.Double(x,y);
    }

	public Vec2 plus(double bx, double by) {
		return new Vec2(x+bx,y+by);
	}
	public static Vec2 sum(Vec2[] arr){
		Vec2 ret = arr[0];
		for(int i=1;i<arr.length;i++){
			ret = ret.plus(arr[i]);
		}
		return ret;
	}

    /**
     * @return Vec2 randomly shifted by [0 ... 1] from the one before
     */
    public Vec2 randomShift() {
        Random r = new Random();
        return new Vec2(this.x + r.nextDouble(), this.y + r.nextDouble());
    }
}
