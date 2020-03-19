package com.dreamfish.com.autocalc.database;

public class CalcHistoryDbSchema {
    public static final class CalcHistoryDTable {
        public static final String NAME = "history";
        public static final String[] COLS = new String[]{ "formula" };
        public static final class Cols {
            public static final String FORMULA = "formula";
        }
    }
}
