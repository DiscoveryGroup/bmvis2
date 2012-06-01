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
