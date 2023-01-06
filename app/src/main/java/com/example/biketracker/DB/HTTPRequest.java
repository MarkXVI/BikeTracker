package com.example.biketracker.DB;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;


public class HTTPRequest {

    OkHttpClient client;
    Context context;

    public HTTPRequest (Context context) {
        this.context = context;

        client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request();

                    // print the request method, URL, and headers
                    Log.v("HTTPRequest",request.method() + " " + request.url());
                    if (request.headers().names().size() != 0) {
                        for (String name : request.headers().names()) {
                            Log.v("HTTPRequest", name + ": " + request.headers().get(name));
                        }
                    } else
                        Log.v("HTTPRequest","No head...");

                    // read the request body into a string
                    if (request.body() != null) {
                        Buffer buffer = new Buffer();
                        Objects.requireNonNull(request.body()).writeTo(buffer);
                        String bodyString = buffer.readUtf8();

                        // print the body
                        Log.v("HTTPRequest", bodyString);
                    } else
                        Log.v("HTTPRequest","No body...");

                    return chain.proceed(request);
                })
                .build();
    }

    private String requestToken() throws IOException, JSONException {

        String token = "";

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody tokenBody = MultipartBody.create(mediaType,
                "{\"username\":\"Byutveckling AB Smarta Byar\",\"password\":\"UnityTest2\"}");

        Request tokenRequest = new Request.Builder()
                .url("https://kraftringen.yggio.net/api/auth/local")
                .post(tokenBody)
                .addHeader("accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build();

        Response tokenResponse = client.newCall(tokenRequest).execute();

        int responseCode = tokenResponse.code();
        if (responseCode == 200) {
            // request was successful
            Log.v("HTTPRequest Token", "Hurray 200");
        } else {
            // request was not successful
            Log.v("HTTPRequest Token", "Response Code: " + responseCode + ". Boo!");
        }
        ResponseBody body = tokenResponse.body();
        if (body != null) {
            String responseString = body.string();
            JSONObject json = new JSONObject(responseString);
            Log.v("HTTPRequest Token", String.valueOf(json));
            token = json.getString("token");
        }

        return token;
    }


    public JSONObject requestLocation(String id) throws IOException, JSONException {

        String token = SaveSharedPreference.getYggioToken(context);
        if (token.equals("")) {
            token = requestToken();
            SaveSharedPreference.setYggioToken(context, token);
        }


        JSONObject obj = new JSONObject();

        Request locationRequest = new Request.Builder()
                .url("https://kraftringen.yggio.net/api/iotnodes/" + id)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        Response locationResponse = client.newCall(locationRequest).execute();

        int responseCode = locationResponse.code();
        if (responseCode == 200) {
            // request was successful
            Log.v("HTTPRequest Location", "Hurray 200");
        } else {
            // request was not successful
            Log.v("HTTPRequest Location", "Response Code: " + responseCode + ". Boo!");
        }
        ResponseBody body = locationResponse.body();
        if (body != null) {
            String responseString = body.string();
            Log.v("HTTPRequest Location", responseString);
            JSONObject json = new JSONObject(responseString);
            String name = json.getString("name");
            String latlng = json.getString("latlng");

            obj.put("name", name);
            obj.put("latLng", latlng);
            Log.v("HTTPRequest Location", obj.toString());
        }
        return obj;
    }


}

