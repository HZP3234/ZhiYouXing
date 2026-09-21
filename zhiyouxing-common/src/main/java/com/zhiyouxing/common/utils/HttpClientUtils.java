package com.zhiyouxing.common.utils;

import cn.hutool.http.HttpUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * HttpClient工具类
 */
public class HttpClientUtils {

    /**
     * @param uri
     * @return String
     * @description get请求方式
     * @author: long.he01
     */
    public static String doGet(String uri) {

        StringBuilder result = new StringBuilder();
        try {
            String res = "";
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                res += line+"\n";
            }
            in.close();
            return res;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String doPost(String url, Map<String, String> param) {
        try {
            Map<String, Object> form = new LinkedHashMap<>(param);
            return HttpUtil.post(url, form);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
