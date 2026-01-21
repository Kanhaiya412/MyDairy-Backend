package com.MyFarmerApp.MyFarmer.util;

public class EventPayload {

    // ==============================================
    // 👤 USER / AUTH EVENTS
    // ==============================================
    public static String json(String event, String username, String role, boolean success, String note) {
        long timestamp = System.currentTimeMillis();
        return String.format(
                "{\"event\":\"%s\",\"user\":\"%s\",\"role\":\"%s\",\"success\":%s,\"note\":\"%s\",\"timestamp\":%d}",
                escape(event), escape(username), escape(role), success, escape(note), timestamp
        );
    }

    // ==============================================
    // 🥛 MILK ENTRY EVENTS
    // ==============================================
    public static String json(String event, String username, String role, boolean success, String note,
                              double milkQuantity, double fat, String shift, double fatPrice, double totalPayment) {
        long timestamp = System.currentTimeMillis();
        return String.format(
                "{\"event\":\"%s\",\"user\":\"%s\",\"role\":\"%s\",\"success\":%s,\"note\":\"%s\"," +
                        "\"quantity\":%.2f,\"fat\":%.2f,\"shift\":\"%s\",\"fatPrice\":%.2f," +
                        "\"totalPayment\":%.2f,\"timestamp\":%d}",
                escape(event), escape(username), escape(role), success, escape(note),
                milkQuantity, fat, escape(shift), fatPrice, totalPayment, timestamp
        );
    }

    // ==============================================
    // 🐄 CATTLE ENTRY EVENTS
    // ==============================================
    public static String cattleJson(String event,
                                    String username,
                                    String role,
                                    boolean status,
                                    String message,
                                    String cattleId,
                                    String category,
                                    String breed,
                                    Double purchasePrice,
                                    String cattleName,
                                    Integer totalCattle) {
        return String.format("""
            {
              "event": "%s",
              "username": "%s",
              "role": "%s",
              "status": %b,
              "message": "%s",
              "data": {
                "cattleId": "%s",
                "category": "%s",
                "breed": "%s",
                "purchasePrice": %.2f,
                "cattleName": "%s",
                "totalCattle": %d
              }
            }
            """, event, username, role, status, message,
                cattleId, category, breed, purchasePrice, cattleName, totalCattle);
    }

    // ==============================================
    // 💰 EXPENSE MANAGEMENT EVENTS
    // ==============================================
    public static String expenseJson(String event,
                                     String username,
                                     String role,
                                     boolean status,
                                     String message,
                                     String expenseId,
                                     String itemCategory,
                                     String itemName,
                                     Double totalCost,
                                     String itemBuyer,
                                     String purchaseDay) {
        return String.format("""
            {
              "event": "%s",
              "username": "%s",
              "role": "%s",
              "status": %b,
              "message": "%s",
              "data": {
                "expenseId": "%s",
                "itemCategory": "%s",
                "itemName": "%s",
                "totalCost": %.2f,
                "itemBuyer": "%s",
                "purchaseDay": "%s"
              }
            }
            """, event, username, role, status, message,
                expenseId, itemCategory, itemName, totalCost, itemBuyer, purchaseDay);
    }

    public static String animalJson(String event,
                                    String username,
                                    String role,
                                    boolean status,
                                    String message,
                                    String cattleId,
                                    String cattleName,
                                    String healthStatus,
                                    String lastCheckupDate,
                                    String nextCheckupDate,
                                    String lastHeatDate,
                                    String lastAIDate) {

        return String.format("""
        {
          "event": "%s",
          "username": "%s",
          "role": "%s",
          "status": %b,
          "message": "%s",
          "data": {
            "cattleId": "%s",
            "cattleName": "%s",
            "healthStatus": "%s",
            "lastCheckupDate": "%s",
            "nextCheckupDate": "%s",
            "lastHeatDate": "%s",
            "lastAIDate": "%s"
          }
        }
        """,
                escape(event),
                escape(username),
                escape(role),
                status,
                escape(message),
                escape(cattleId),
                escape(cattleName),
                escape(healthStatus),
                escape(lastCheckupDate),
                escape(nextCheckupDate),
                escape(lastHeatDate),
                escape(lastAIDate)
        );
    }
    public static String labourJson(String event,
                                    String username,
                                    String role,
                                    boolean status,
                                    String message,
                                    Long labourId,
                                    String labourName,
                                    Double dailyWage,
                                    String mobile,
                                    String joiningDate,
                                    Integer presentDays,
                                    Integer manualDays,
                                    Double totalSalary,
                                    Integer month,
                                    Integer year) {

        return String.format("""
        {
          "event": "%s",
          "username": "%s",
          "role": "%s",
          "status": %b,
          "message": "%s",
          "data": {
            "labourId": %d,
            "labourName": "%s",
            "dailyWage": %.2f,
            "mobile": "%s",
            "joiningDate": "%s",
            "presentDays": %d,
            "manualDays": %d,
            "totalSalary": %.2f,
            "month": %d,
            "year": %d
          }
        }
        """,
                escape(event),
                escape(username),
                escape(role),
                status,
                escape(message),
                labourId,
                escape(labourName),
                dailyWage,
                escape(mobile),
                escape(joiningDate),
                presentDays == null ? 0 : presentDays,
                manualDays == null ? 0 : manualDays,
                totalSalary == null ? 0 : totalSalary,
                month == null ? 0 : month,
                year == null ? 0 : year
        );
    }
    // ==============================================
    // 🔐 COMMON HELPER
    // ==============================================
    private static String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
