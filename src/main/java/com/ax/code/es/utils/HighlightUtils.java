package com.ax.code.es.utils;

import org.elasticsearch.common.text.Text;

/**
 * @author lj
 */
public class HighlightUtils {
    public HighlightUtils() {
    }

    public static String textToString(Text[] a) {
        if (a == null) {
            return "null";
        } else {
            int iMax = a.length - 1;
            if (iMax == -1) {
                return "";
            } else {
                StringBuilder b = new StringBuilder();
                int i = 0;

                while(true) {
                    b.append(String.valueOf(a[i]));
                    if (i == iMax) {
                        return b.toString();
                    }

                    ++i;
                }
            }
        }
    }
}
