package com.getmyuri.constants;

import java.util.Set;

public class ServiceConstants {
    public static final String REDIS_CLICK = "redis-click";
    public static final String MONGO = "mongo";
    public static final String ORACLE = "oracle";
    public static final String POSTGRES = "postgres";

    public static final String ALL = "all";
    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.~!$";
    private static final int BASE = CHARSET.length();

    public static final Set<String> KNOWN_SERVICES = Set.of(
            REDIS_CLICK,
            MONGO,
            POSTGRES);
}
