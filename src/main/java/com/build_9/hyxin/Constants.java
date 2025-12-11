package com.build_9.hyxin;

public class Constants {

    public static final String NAME = "Hyxin";
    public static final String BUILTIN_MIXIN_CONFIG = "hyxin.mixin.json";

    public static void log(String message) {
        IO.println("[" + NAME + "] " + message);
    }
}
