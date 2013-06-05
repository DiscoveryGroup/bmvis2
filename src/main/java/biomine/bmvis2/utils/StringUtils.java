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

package biomine.bmvis2.utils;

import java.util.ArrayList;
import java.util.Collection;

public class StringUtils {
    public static String joinStrings(Collection<String> strings) {
        Collection<Object> ret = new ArrayList<Object>();
        for (String s : strings)
            ret.add(s);
        return StringUtils.joinObjects(ret);
    }

    public static String joinObjects(Collection<Object> objects) {
        return joinObjects(objects, "");
    }

    public static String joinObjects(Collection<Object> objects, String delimiter) {
        StringBuffer buf = new StringBuffer();

        for (Object item : objects) {
            if (item == null)
                buf.append("null");
            else if (item instanceof String)
                buf.append(item);
            else
                buf.append(item.toString());
            buf.append(delimiter);
        }

        if (buf.length() > delimiter.length() &&
                buf.substring(buf.length() - delimiter.length() - 1, buf.length() - 1).equals(delimiter))
            return buf.substring(buf.length() - delimiter.length() - 2);

        return buf.toString();
    }
}
