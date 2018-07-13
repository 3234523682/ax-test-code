package com.ax.code.string;

import java.util.Random;

public class StringInternTest {

    static final int MAX = 1000 * 10000;
    static final String[] arr = new String[MAX];

    public static void main(String[] args) {
        String s1 = "ax";
        String s2 = new String("ax");
        String s3 = new String("ax").intern();
        System.out.println(s1 == s2);
        System.out.println(s1 == s3);
        System.out.println(s2 == s3);
    }

    private static void StringInternPropertyTest() {
        Integer[] DB_DATA = new Integer[10];
        Random random = new Random(10 * 10000);
        for (int i = 0; i < DB_DATA.length; i++) {
            DB_DATA[i] = random.nextInt();
        }
        long beginM = System.currentTimeMillis();
        for (int i = 0; i < MAX; i++) {
            //arr[i] = String.valueOf(DB_DATA[i % DB_DATA.length]);
            arr[i] = String.valueOf(DB_DATA[i % DB_DATA.length]).intern();
        }
        long endM = System.currentTimeMillis();
        System.out.println("use intern ：" + (endM - beginM));
    }

}
