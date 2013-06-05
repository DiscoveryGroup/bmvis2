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

package biomine.bmvis2.group;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

import discovery.compression.kdd2011.old.CorrectionLimitCompressor;
import discovery.compression.kdd2011.old.FastGreedyCompressor;
import discovery.compression.kdd2011.old.RandomizedCompressor;
import discovery.compression.kdd2011.old.SimilarityCompressor;
import discovery.compression.kdd2011.ratio.RatioCompressionGrouper;

public class GrouperList {

	public static Collection<Grouper> getGroupers() {
		Grouper[] groupers = {
				new FastGreedyCompressor(), 
				new RandomizedCompressor(),
				new CorrectionLimitCompressor(),
				new SimilarityCompressor(),
				new EdgeTripleGrouper(),
				new ManualEdgeTripleGrouper(),
				new ParallelGrouper(),
				new PathGrouper(), 
				new GirvanNewmanClustering(),
				new LabelPropagationCommunity(), 
				new KernighanLin(),
				new RatioCompressionGrouper(),
				new NodeImportanceGrouper()
//				new InitialGrouper() 
				};

		return Arrays.asList(groupers);
	}

	public static String grouperName(Grouper g) {
		Class c = g.getClass();
		
		try {
			Field f = c.getField("SIMPLE_NAME");
			Object strObject = f.get(g);
			return strObject.toString();
		}
		catch (SecurityException e) { }
		catch (NoSuchFieldException e) { }
		catch (IllegalArgumentException e) { }
		catch (IllegalAccessException e) { }
		
		try {
			Field f = c.getField("NAME");
			Object strObject = f.get(g);
			return strObject.toString();
		} catch (SecurityException e) {
			// e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return c.getSimpleName();
	}
}
