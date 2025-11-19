package com.MyFarmerApp.MyFarmer.util;

public class UserEventPayload {

    public static String json(String event, String username, String role, boolean success, String note) {
        long timestamp = System.currentTimeMillis();
        return String.format(
                "{\"event\":\"%s\",\"user\":\"%s\",\"role\":\"%s\",\"success\":%s,\"note\":\"%s\",\"timestamp\":%d}",
                escape(event), escape(username), escape(role), success, escape(note), timestamp
        );
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
