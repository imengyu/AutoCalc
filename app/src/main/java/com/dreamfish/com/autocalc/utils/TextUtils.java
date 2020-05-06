package com.dreamfish.com.autocalc.utils;

import java.util.regex.Pattern;

public class TextUtils {
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }
    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }
    public static boolean isNumber(CharSequence s) {
        return !isEmpty(s) && Pattern.matches("-?[0-9]*(\\.?)[0-9]*", s);
    }

}
