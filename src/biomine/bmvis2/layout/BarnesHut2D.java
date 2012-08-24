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

package biomine.bmvis2.layout;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import biomine.bmvis2.MutableVec2;
import biomine.bmvis2.Vec2;

/**
 * Algorithm for n-body in 2 dimensions.
 * http://en.wikipedia.org/wiki/Barnes-Hut_simulation
 * 
 * @author alhartik
 * 
 */
public class BarnesHut2D {
	private Vec2[] resultForce;
    private Body[] bodies;

	static class Body {
		int id;
		Vec2 pos;
		double mass;
	}

	public BarnesHut2D(int s) {
		reset(s);
	}

	public void reset(int size) {
		if(bodies!=null && bodies.length==size)return;
		bodies = new Body[size];
		for (int i = 0; i < size; i++) {
			bodies[i] = new Body();
			bodies[i].id = i;
		}
		resultForce = new Vec2[size];

	}

	public void setPos(int i, Vec2 v) {
		bodies[i].pos = v;
	}

	public void setMass(int i, double m) {
		bodies[i].mass = m;
	}

	public Vec2 getForce(int i) {
		return resultForce[i];
	}
	private class QuadNode{
		int leaf=-1;
		double mass=0;
		Vec2 massCenter=Vec2.ZERO;
		Vec2 center=Vec2.ZERO;
		double w=0;
		
		/**
		 * 0 1
		 * 2 3
		 * 
		 * or
		 * first bit is x second is y
		 * x = i%2
		 * y = i/2
		 */
		QuadNode[] children=null;
		void split(){
				children = new QuadNode[4];
				Vec2 dpos[] = {
						new Vec2(-0.25,-0.25),
						new Vec2(0.25,-0.25),
						new Vec2(-0.25,0.25),
						new Vec2(0.25,0.25),
						};
				for(int i=0;i<4;i++){
					Vec2 c = center.plus(dpos[i].scaled(w));
					children[i] = new QuadNode();
					children[i].center = c;
					children[i].mass = 0.0;
					children[i].w = w/2;
					children[i].leaf = -1;
				}
				
			}
			void add(Body b){
				if(leaf>=0){
					//SUBDIVIDE
					int ol = leaf;
					leaf = -1;
					
					split();
					
					int c = 0;
					if(b.pos.x>center.x)
						c+=1;
					if(b.pos.y>center.y)
						c+=2;
					children[c].leaf = b.id;
					massCenter = b.pos;
					add(bodies[ol]);
				}else if(children!=null){
					int c = 0;
					if(b.pos.x>center.x)
						c+=1;
					if(b.pos.y>center.y)
						c+=2;
					children[c].add(b);
				}else // empty node 
				{
					leaf=b.id;
					mass = b.mass;
					massCenter = b.pos;
					split();
				}
			}
			public boolean isEmpty() {
				return children==null&&leaf<0;
			}
			void calcMass(){
				if(leaf>=0){
					mass = bodies[leaf].mass;
					massCenter = bodies[leaf].pos;
				}else if(!isEmpty()){
					mass = 0;
					massCenter = Vec2.ZERO;
					for(int i=0;i<4;i++){
						children[i].calcMass();
						mass+=children[i].mass;
						massCenter =massCenter.plus(children[i].massCenter.scaled(children[i].mass));
					}
					massCenter = massCenter.scaled(1/mass);
				}
			}
		}
		private static class KDNode {
	
			int leaf;
			double mass;
			Vec2 massCenter;
	
			/**
			 * ul = up or left part br = bottom or right part
			 */
			KDNode left, right;
		}
	
		int[] temp;
		Random rand = new Random();
	
		/**
		 * @param from
		 * @param to
		 * @return random integer in range [from,to[
		 */
		private int getRand(int from, int to) {
			return from + rand.nextInt(to - from);
		}
	
		private QuadNode makeTree(){
			QuadNode ret = new QuadNode();
			double minx=1e8;
			double maxx=-1e8;
			double miny=1e8;
			double maxy=-1e8;
			for(Body b:bodies){
				minx = Math.min(minx,b.pos.x);
				maxx = Math.max(maxx,b.pos.x);
				miny = Math.min(miny,b.pos.y);
				maxy = Math.max(maxy,b.pos.y);
			}
			
			ret.center = new Vec2(0.5*(minx+maxx),0.5*(miny+maxy));
			ret.w = Math.ceil(Math.max(maxx-minx,maxy-miny));
			
			for(Body b:bodies)
				ret.add(b);
			ret.calcMass();
			return ret;
		}
		
		private double repulsive_k=1;
		void recurTree(KDNode n, Stack<KDNode> poles) {
			if (n.leaf >= 0) {
				Body t = bodies[n.leaf];
				Vec2 force = Vec2.ZERO;
				for (KDNode k : poles) {
					
					Vec2 add = k.massCenter.minus(t.pos);
					double dist = add.length();
					// dist*=dist;
					if(dist<1)dist=1;
					double strength = -repulsive_k * t.mass * k.mass
							/ (dist * dist);
	
					// add = add.scaled(1 / dist);
					add = add.scaled(strength / dist);
	
					force = force.plus(add);
				}
				resultForce[n.leaf]=force;
			}else{
				poles.push(n.right);
				recurTree(n.left,poles);
				poles.pop();
				
				poles.push(n.left);
				recurTree(n.right,poles);
				poles.pop();
			}
		}
	
		void printQuadTree(QuadNode tr,int depth){
			for(int i=0;i<depth;i++)
				System.out.print("\t");
			
			if(tr.isEmpty())
				System.out.println("EMPTY");
			else if(tr.leaf>=0)
				System.out.println("leaf:"+tr.leaf+ " mass = "+tr.mass);
			else{
				System.out.println("innernode mass = "+tr.mass);
				for(int i=0;i<4;i++)
					printQuadTree(tr.children[i],depth+1);
			}
		}
		/**
		 * runs one step of simulation. resulting forces can be fetched using
		 * getForce method
		 * 
		 * @param dt
		 */
		private static boolean first = true;
		public void simulate(double repulsive) {
            Set<Vec2> positions = new HashSet<Vec2>();
            for (Body body : bodies) {
                if (positions.contains(body.pos))
                    body.pos = body.pos.randomShift();
                positions.add(body.pos);
            }

			this.repulsive_k=repulsive;
			QuadNode root = makeTree();
			
			MutableVec2 mv = new MutableVec2(0,0);
			for(int i=0;i<bodies.length;i++){
				mv.setZero();
				recurForce(bodies[i],root,mv);
				resultForce[i] = mv.toVec2();
			}
			//KDNode root = makeTree(p, 0, p.length);
			//recurTree(root,new Stack<KDNode>());
			
//			if(first){
//				printQuadTree(root,0);
//				System.out.println("size = "+bodies.length);
//				first=false;
//			}
	
		}
	
		private static MutableVec2 add = new MutableVec2(0,0);
		void addForce(Body b,QuadNode n,MutableVec2 force){
			if(b.id==n.leaf)return;
			
			double ax = n.massCenter.x-b.pos.x;
			double ay = n.massCenter.y-b.pos.y;
			add.set(ax,ay);
			double dist = add.length();
			
			
			// dist*=dist;
			//add = add.scaled(1 / dist);
			if(dist<10)
				dist=10;
			
			add.scale(1/dist);
			
			double strength = -repulsive_k * b.mass * n.mass
					/ (dist * dist);
	
			add.scale(strength );
			force.add(add);
		}
		
		private void recurForce(Body body,QuadNode tree,MutableVec2 force) {
			if(tree.isEmpty())return;
			if(tree.leaf>=0){
				addForce(body,tree,force);
				return;
			}
			double dist = tree.massCenter.dist(body.pos);
			double s = tree.w/dist;

            double theta = 0.8;
            if(s> theta){
				double x,y;
				x=y=0;
				
				for(int i=0;i<4;i++)
				{
					recurForce(body,tree.children[i],force);
				}
			}else{
				addForce(body,tree,force);
			}
		}
	}
