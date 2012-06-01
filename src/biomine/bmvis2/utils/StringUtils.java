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
