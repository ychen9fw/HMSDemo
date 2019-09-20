package com.example.hmsdemo.utils;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPManager {

    public static JSONObject getData(String uri){

        BufferedReader reader = null;
        JSONObject jsonObject = null;
        try{
            URL obj = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setUseCaches(false);
            con.setReadTimeout(15 * 1000);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.connect();

            int responseCode = con.getResponseCode();
            Log.i("GET Response Code :: ", String.valueOf(responseCode));
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                jsonObject = new JSONObject(response.toString());

            }

            return jsonObject;

        } catch (Exception e){
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
