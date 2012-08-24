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

import biomine.bmvis2.layout.BarnesHut2D;
import com.sun.org.apache.xpath.internal.axes.SelfIteratorNoPredicate;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NBodiesTest {
    @Test
    public void testBasicFunctionality() throws Exception {
        BarnesHut2D bh = new BarnesHut2D(2);

        bh.setPos(0, new Vec2(0.0, 0.0));
        bh.setMass(0, 1.0);

        bh.setPos(1, new Vec2(1.0, 1.0));
        bh.setMass(1, 1.0);

        bh.simulate(10000.0);

        Vec2[] forces = new Vec2[2];
        forces[0] = bh.getForce(0);
        forces[1] = bh.getForce(1);

        Assert.assertEquals(forces[0].x, -10.0);
        Assert.assertEquals(forces[0].y, -10.0);
        Assert.assertEquals(forces[1].x, 10.0);
        Assert.assertEquals(forces[1].y, 10.0);
    }
}
