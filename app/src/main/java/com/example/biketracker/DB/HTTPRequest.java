package com.example.biketracker.DB;

import android.util.Log;

import androidx.core.util.Consumer;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
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

    public HTTPRequest () {

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
            Log.v("HTTPRequest", "Hurray 200");
        } else {
            // request was not successful
            Log.v("HTTPRequest", "Boo");
        }
        ResponseBody body = tokenResponse.body();
        if (body != null) {
            String responseString = body.string();
            JSONObject json = new JSONObject(responseString);
            Log.v("HTTPRequest", String.valueOf(json));
            token = json.getString("token");
        }

        return token;
    }


    public JSONObject requestLocation(String id) throws IOException, JSONException {

        JSONObject obj = new JSONObject();

        Request locationRequest = new Request.Builder()
                .url("https://kraftringen.yggio.net/api/iotnodes/" + id)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + requestToken())
                .build();

        Response locationResponse = client.newCall(locationRequest).execute();

        int responseCode = locationResponse.code();
        if (responseCode == 200) {
            // request was successful
            Log.v("HTTPRequest", "Hurray 200");
        } else {
            // request was not successful
            Log.v("HTTPRequest", "Boo");
        }
        ResponseBody body = locationResponse.body();
        if (body != null) {
            String responseString = body.string();
            Log.v("HTTPRequest", responseString);
            JSONObject json = new JSONObject(responseString);
            String name = json.getString("name");
            String latlng = json.getString("latlng");

            obj.put("name", name);
            obj.put("latLng", latlng);
            Log.v("HTTPRequest", obj.toString());
        }
        return obj;
    }


}

