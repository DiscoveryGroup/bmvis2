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
