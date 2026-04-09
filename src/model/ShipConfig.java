package model;

import java.util.ArrayList;
import java.util.List;

public final class ShipConfig {

    private ShipConfig() {}

    private static final ShipEntry[] ENTRIES = {
        new ShipEntry(5, 1, "Thiết Giáp Hạm"),
        new ShipEntry(4, 1, "Tuần Dương Hạm"),
        new ShipEntry(3, 2, "Khu Trục Hạm"),
        new ShipEntry(2, 1, "Tàu Tuần Tra"),
    };

    public static final class ShipEntry {
        public final int    length;  
        public final int    count;  
        public final String name;

        public ShipEntry(int length, int count, String name) {
            this.length = length;
            this.count  = count;
            this.name   = name;
        }
    }
   
    public static ShipEntry[] getEntries() {
        return ENTRIES;
    }

    public static int[] getSizesFlat() {
        int total = 0;
        for (ShipEntry e : ENTRIES) total += e.count;
        int[] sizes = new int[total];
        int idx = 0;
        for (ShipEntry e : ENTRIES) {
            for (int i = 0; i < e.count; i++) {
                sizes[idx++] = e.length;
            }
        }
        return sizes;
    }

    public static List<Integer> getSizesList() {
        List<Integer> list = new ArrayList<>();
        for (ShipEntry e : ENTRIES) {
            for (int i = 0; i < e.count; i++) {
                list.add(e.length);
            }
        }
        return list;
    }

    public static int getTotalShipCount() {
        int total = 0;
        for (ShipEntry e : ENTRIES) total += e.count;
        return total;
    }

    public static String[] getNamesFlat() {
        int total = 0;
        for (ShipEntry e : ENTRIES) total += e.count;
        String[] names = new String[total];
        int idx = 0;
        for (ShipEntry e : ENTRIES) {
            if (e.count == 1) {
                names[idx++] = e.name;
            } else {
                for (int i = 0; i < e.count; i++) {
                    names[idx++] = e.name + " " + (char)('A' + i);
                }
            }
        }
        return names;
    }
}