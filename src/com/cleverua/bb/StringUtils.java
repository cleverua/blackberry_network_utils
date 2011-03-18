package com.cleverua.bb;


public class StringUtils {
    private static final String EMPTY = "";
    private static final String BLANK = " ";
    private static final String BLANK_ENCODED = "%20";
    
    public static boolean isBlank(String str) {
        return (str == null || str.length() == 0);
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String safe(String str) {
        return str == null ? EMPTY : str;
    }

    public static String safe(String str, String defaultValue) {
        return str == null ? defaultValue : str;
    }

    public static int linesCount(String str) {
        if (str.length() == 0) {
            return 0;
        }
        
        int result = 1;
        for (int indx = 0; (indx = str.indexOf('\n', indx)) != -1; indx++, result++);
        
        return result;
    }

    // http://supportforums.blackberry.com/rim/board/message?board.id=java_dev&message.id=34183
    public static String replaceAll(String source, String pattern, String replacement) {
        if (source == null) {
            return EMPTY;
        }

        StringBuffer sb = new StringBuffer();
        int idx = -1;
        int patIdx = 0;

        while ((idx = source.indexOf(pattern, patIdx)) != -1) {
            sb.append(source.substring(patIdx, idx));
            sb.append(replacement);
            patIdx = idx + pattern.length();
        }
        sb.append(source.substring(patIdx));

        return sb.toString();
    }

    public static String encodeUrl(String url) {
        return replaceAll(url, BLANK, BLANK_ENCODED);
    }
}
