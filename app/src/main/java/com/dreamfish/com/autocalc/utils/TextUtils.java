package com.dreamfish.com.autocalc.utils;

public class TextUtils {

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }
    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }
}
