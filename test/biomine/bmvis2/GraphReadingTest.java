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

import java.io.ByteArrayInputStream;

import biomine.bmgraph.BMGraph;
import biomine.bmgraph.BMGraphUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GraphReadingTest {
    private VisualGraph oldGroups;
    private VisualGraph newGroups;

    public static final String oldStyleGroupString = "Island_Java\n" +
            "Island_Sumatra\n" +

            "# _group Island_grp1x2 2 Island Borneo,Kalimantan\n" +

            "Island_grp1x2 Country_Indonesia is_part_of\n" +

            "Island_Java Country_Indonesia is_part_of\n" +
            "Island_Sumatra Country_Indonesia is_part_of";

    public static final String newStyleGroupString = "Island_Java\n" +
            "Island_Sumatra\n" +
            "Island_Borneo Group_IndonesianIsles belongs_to\n" +

            "Island_Kalimantan Group_IndonesianIsles belongs_to\n" +
            "Group_IndonesianIsles Country_Indonesia is_part_of\n" +

            "Island_Java Country_Indonesia is_part_of\n" +
            "Island_Sumatra Country_Indonesia is_part_of\n" +
            "Island_Borneo Country_Indonesia is_part_of\n" +
            "Island_Kalimantan Country_Indonesia is_part_of";

    public static BMGraph stringToBMGraph(String in) {
        ByteArrayInputStream bs = new ByteArrayInputStream(in.getBytes());
        return BMGraphUtils.readBMGraph(bs);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        BMGraph oldStyleGroups = GraphReadingTest.stringToBMGraph(oldStyleGroupString);
        BMGraph newStyleGroups = GraphReadingTest.stringToBMGraph(newStyleGroupString);
        oldGroups = new VisualGraph(oldStyleGroups);
        newGroups = new VisualGraph(newStyleGroups);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        this.oldGroups = null;
        this.newGroups = null;
    }

    @Test
    public void testReadToVisualGraph() throws Exception {
        Assert.assertEquals(this.oldGroups.getAllNodes().size(), 7);
        Assert.assertEquals(this.newGroups.getAllNodes().size(), 7);
    }
}
