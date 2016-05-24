/*
 * Copyright 2012-2016 University of Helsinki.
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

import java.util.Collection;


/**
 * Interface to observe changes in graphs.
 */
public interface GraphObserver {
	void graphStructureChanged(VisualGraph g);
	void pointsOfInterestsChanged(VisualGraph g);
	void visibleNodesChanged(VisualGraph g);
	void selectionChanged(VisualGraph g);
	void colorsChanged(VisualGraph g);
    void zoomRequested(VisualGraph g, Collection<LayoutItem> items);
}
