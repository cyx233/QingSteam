package com.java.tanghao;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.*;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class QingUtils {
    static class GetHttpResponseTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder sb = new StringBuilder();
            for (String s : strings) sb.append(s);
            String httpurl = sb.toString();
            HttpURLConnection connection = null;
            InputStream is = null;
            BufferedReader br = null;
            String result = null;
            try {
                URL url = new URL(httpurl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(60000);
                connection.connect();
                if (connection.getResponseCode() == 200) {
                    is = connection.getInputStream();
                    br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    StringBuffer sbf = new StringBuffer();
                    String temp = null;
                    while ((temp = br.readLine()) != null) {
                        sbf.append(temp);
                        sbf.append("\r\n");
                    }
                    result = sbf.toString();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
//            System.out.println(result);
                if (null != br) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (null != is) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                connection.disconnect();
                return result;
            }
        }
    }


    static class ParseNewsTask extends AsyncTask<String, Void, News[]> {
        @Override
        protected News[] doInBackground(String... strings) {
            ExclusionStrategy myExclusionStrategy = new ExclusionStrategy() {

                @Override
                public boolean shouldSkipField(FieldAttributes fa) {
                    return fa.getName().equals("isRead") || fa.getName().equals("isFavorite"); // <---
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            };

            Gson gson = new GsonBuilder()
                    .setExclusionStrategies(myExclusionStrategy)
                    .create();
            String s = null;
            if (strings[0] != null) s = strings[0];
            else s = "";
            NewsApi na = gson.fromJson(s, NewsApi.class);
            if (na != null) return na.getData();
            return new News[0];
        }
    }

    static class ParseYiqingDataTask extends AsyncTask<String, Void, YiqingData[]> {
        @Override
        protected YiqingData[] doInBackground(String... strings) {
            GsonBuilder builder = new GsonBuilder();
            builder.serializeNulls();
            Gson gson = builder.create();
            String s = null;
            if (strings[0] != null) s = strings[0];
            else s = "";
            Type mapType = new TypeToken<Map<String, YiqingDataApi>>() {
            }.getType();
            Map<String, YiqingDataApi> j = gson.fromJson(s, mapType);
            YiqingData[] result = new YiqingData[j.size()];
            int index = 0;
            for (Map.Entry<String, YiqingDataApi> entry : j.entrySet()) {
                result[index++] = new YiqingData(entry.getKey(), entry.getValue());
            }
            return result;
        }

    }

    static class ParseYiqingEntityTask extends AsyncTask<String, Void, YiqingEntity[]>{
        @Override
        protected YiqingEntity[] doInBackground(String... strings) {
            Gson gson = new Gson();
            String s = null;
            if (strings[0] != null) s = strings[0];
            else s = "";
            YiqingEntityApi j = gson.fromJson(s, YiqingEntityApi.class);
            if(j == null)
                return new YiqingEntity[0];
            return j.getData();
        }
    }

    static class ParseYiqingScholarTask extends AsyncTask<String, Void, YiqingScholar[]>{
        @Override
        protected YiqingScholar[] doInBackground(String... strings) {
            Gson gson = new Gson();
            String s = null;
            if (strings[0] != null) s = strings[0];
            else s = "";
            YiqingScholarApi j = gson.fromJson(s, YiqingScholarApi.class);
            if(j == null)
                return new YiqingScholar[0];
            return j.getData();
        }
    }
}



