package com.MyFarmerApp.MyFarmer.util;

public class MonthExtractor {

    private MonthExtractor() {
        // prevent object creation
    }

    public static Integer extractMonth(String text) {
        if (text == null) return null;

        text = text.toLowerCase();

        if (text.contains("january") || text.contains("जनवरी")) return 1;
        if (text.contains("february") || text.contains("फरवरी")) return 2;
        if (text.contains("march") || text.contains("मार्च")) return 3;
        if (text.contains("april") || text.contains("अप्रैल")) return 4;
        if (text.contains("may") || text.contains("मई")) return 5;
        if (text.contains("june") || text.contains("जून")) return 6;
        if (text.contains("july") || text.contains("जुलाई")) return 7;
        if (text.contains("august") || text.contains("अगस्त")) return 8;
        if (text.contains("september") || text.contains("सितंबर")) return 9;
        if (text.contains("october") || text.contains("अक्टूबर")) return 10;
        if (text.contains("november") || text.contains("नवंबर")) return 11;
        if (text.contains("december") || text.contains("दिसंबर")) return 12;

        return null;
    }
}
