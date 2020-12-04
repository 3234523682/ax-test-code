package com.ax.code.string;

import com.alibaba.fastjson.JSON;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StringInternTest {

    static final int MAX = 1000 * 10000;
    static final String[] arr = new String[MAX];
    private static final String URL = "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0&from=zh-Hans&to=zh-Hant";
    private static final String SUBSCRIPTION_KEY = "3a0607e87f0b408d94cce881500b9a8b";

    public static void main(String[] args) throws IOException {
        test("测试%s时%s代发生%d的话");
    }

    private static void test(String msg, Object... valList) {
        System.out.println(valList.length);
        System.out.println(String.format(msg, valList));
    }

    public static String translate() throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        Map<String, String> contentMap = new HashMap<String, String>();
        contentMap.put("Text", "爱车");
        RequestBody body = RequestBody.create(mediaType,
                JSON.toJSONString(contentMap));
        Request request = new Request.Builder()
                .url(URL).post(body)
                .addHeader("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY)
                .addHeader("Content-type", "application/json").build();
        Response response = client.newCall(request).execute();
        return response.body().string();
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

    static class Pig {

        private String name;

        private Integer age;

        Pig(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return "name：" + this.name + "、age：" + this.age;
        }
    }

}
