package com.spartans.grabon.maps;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Author : Sudha Amarnath on 2020-04-03
 */
public class PlaceApi {

    public ArrayList<String> autoComplete(String input){
        ArrayList<String> arrayList=new ArrayList();
        HttpURLConnection connection=null;
        StringBuilder jsonResult=new StringBuilder();
        try {

            StringBuilder sb=new StringBuilder("https://maps.googleapis.com/maps/api/place/autocomplete/json?");
            String key = "AIzaSyAqzQcih2a11zkRCQAH08nSkZHVhUIHmzE";
            sb.append("input="+input);
            sb.append("&key="+key);
            URL url=new URL(sb.toString());
            connection=(HttpURLConnection)url.openConnection();
            InputStreamReader inputStreamReader=new InputStreamReader(connection.getInputStream());

            int read;

            char[] buff=new char[1024];
            while ((read=inputStreamReader.read(buff))!=-1){
                jsonResult.append(buff,0,read);
            }

            Log.d("JSon",jsonResult.toString());
        }
        catch (MalformedURLException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            if(connection!=null){
                connection.disconnect();
            }
        }

        try {
            JSONObject jsonObject=new JSONObject(jsonResult.toString());
            JSONArray prediction=jsonObject.getJSONArray("predictions");
            for(int i=0;i<prediction.length();i++){
                arrayList.add(prediction.getJSONObject(i).getString("description"));
            }
        }
        catch (JSONException e){
            e.printStackTrace();
        }

        return arrayList;
    }


}