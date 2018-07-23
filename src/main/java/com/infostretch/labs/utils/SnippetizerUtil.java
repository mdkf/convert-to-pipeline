package com.infostretch.labs.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jenkinsci.plugins.workflow.cps.Snippetizer;

public class SnippetizerUtil {
    public static String object2Groovy(StringBuilder b, Object o, boolean nestedExp) throws UnsupportedOperationException {
        try {
            Method m = Snippetizer.class.getDeclaredMethod("object2Groovy", StringBuilder.class, Object.class, boolean.class);
            m.setAccessible(true);
            return m.invoke(null, b, o, nestedExp).toString();
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            Logger.getLogger(SnippetizerUtil.class.getName()).log(Level.INFO, "Failed to convert snippetize " + o.getClass().getName());
            // can't happen
        }
        return null;
    }
}
