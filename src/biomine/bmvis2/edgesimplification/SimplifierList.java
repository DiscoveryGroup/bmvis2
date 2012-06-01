package biomine.bmvis2.edgesimplification;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

public class SimplifierList {

    static Simplifier[] simplifiers = {
            new KappaSimplifier(),
            new XreosSimplifier()
    };

    public static Collection<Simplifier> getSimplifiers() {
        return Arrays.asList(simplifiers);
    }

    public static String simplifierName(Simplifier g) {
        Class c = g.getClass();
        try {
            Field f = c.getField("NAME");
            Object strObject = f.get(g);
            return strObject.toString();
        } catch (SecurityException e) {
            //	e.printStackTrace();
        } catch (NoSuchFieldException e) {
            //	e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            //	e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            //	e.printStackTrace();
        }

        return c.getSimpleName();
    }
}
