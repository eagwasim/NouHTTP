package com.noubug.lib.nouhttp;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Agwasim Emmanuel
 * <p>
 * on 8/5/16.
 */
public class NouHTTP {

    private String url = null;
    private Context context = null;
    private Future tFuture = null;
    private Object body = null;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();
    private String type = "GET";
    private AsyncTask task;
    private static final Gson GSON = new Gson();
    private static boolean logEnabled = false;

    public static void enableLogging() {
        logEnabled = true;
    }

    public static void disableLogging() {
        logEnabled = false;
    }

    public NouHTTP setBody(Object body) {
        this.body = body;
        return this;
    }

    private NouHTTP(Context context) {
        this.context = context;
    }

    public static NouHTTP with(@NonNull Context context) {
        NouHTTP nouHTTP = new NouHTTP(context);
        return nouHTTP;
    }

    public NouHTTP get(@NonNull String url) {
        this.url = url;
        this.type = "GET";
        return this;
    }

    public NouHTTP post(@NonNull String url) {
        this.url = url;
        this.type = "POST";
        return this;
    }

    public NouHTTP addHeader(@NonNull String key, @NonNull String value) {
        this.headers.put(key, value);
        return this;
    }

    public NouHTTP addParam(@NonNull String key, @NonNull String value) {
        this.params.put(key, value);
        return this;
    }

    public NouHTTP nou() {
        this.task = new AsyncTask<Void, Void, NouResponse>() {
            NouHTTP nou = NouHTTP.this;

            @Override
            protected NouResponse doInBackground(Void... voids) {
                if (nou.type.equalsIgnoreCase("GET")) {
                    return doGet();
                } else {
                    return doPost();
                }
            }

            @Override
            protected void onPostExecute(NouResponse nouResponse) {
                super.onPostExecute(nouResponse);
                if (NouHTTP.this.tFuture != null) {
                    if (nouResponse == null) {
                        NouHTTP.this.tFuture.completed(new Exception("Unknown Error"), -1, null);
                    } else if (nouResponse.exception != null) {
                        NouHTTP.this.tFuture.completed(nouResponse.exception, -1, null);
                    } else {
                        try {
                            NouHTTP.this.tFuture.completed(null, nouResponse.responseCode, nouResponse.responseBody);

                        } catch (Exception e) {
                            NouHTTP.this.tFuture.completed(e, -1, null);
                        }
                    }
                }
            }
        }.execute();

        return this;
    }

    public void cancel(boolean cancelQuietly) {
        try {
            if (task == null) {
                if (!cancelQuietly) {
                    if (this.tFuture != null) {
                        this.tFuture.completed(new Exception("Task not running or completed"), -1, null);
                    }
                }
            } else {
                task.cancel(true);
            }
        } catch (Exception e) {
            if (logEnabled) {
                e.printStackTrace();
            }
        }
    }

    public NouHTTP inTheFuture(Future tFuture) {
        this.tFuture = tFuture;
        return this;
    }

    public interface Future {
        void completed(Exception e, int statusCode, String t);
    }

    private class NouResponse {
        private String responseBody;
        private int responseCode;
        private Exception exception;

        public NouResponse(String responseBody, Exception exception, int responseCode) {
            this.responseBody = responseBody;
            this.exception = exception;
            this.responseCode = responseCode;
        }
    }

    public class NouHttpException extends Exception {
    }

    private static void logInfo(@NonNull String message) {
        Log.i(NouHTTP.class.getSimpleName(), message);
    }

    private static void logError(@NonNull String message, Throwable e) {
        if (e == null) {
            Log.e(NouHTTP.class.getSimpleName(), message);
        } else {
            Log.e(NouHTTP.class.getSimpleName(), message, e);
        }
    }

    private static void logDebug(@NonNull String message, Throwable throwable) {
        if (throwable == null) {
            Log.e(NouHTTP.class.getSimpleName(), message);
        } else {
            Log.e(NouHTTP.class.getSimpleName(), message, throwable);
        }
    }

    private NouResponse doGet() {
        try {
            if (this.url == null) {
                throw new NullPointerException("URL IS NULL");
            }

            StringBuilder urlBuilder = new StringBuilder("");

            if (!this.url.trim().endsWith("?")) {
                urlBuilder.append("?");
            } else {
                urlBuilder.append("&");
            }

            for (String key : this.params.keySet()) {
                if (key.isEmpty()) {
                    continue;
                }
                urlBuilder.append(key.trim());
                urlBuilder.append("=");
                urlBuilder.append(URLEncoder.encode(this.params.get(key), "UTF-8"));
                urlBuilder.append("&");
            }

            String params = urlBuilder.toString().contains("&") ? urlBuilder.substring(0, urlBuilder.lastIndexOf("&")) : urlBuilder.toString();
            String finalUrl = this.url + params;

            if (logEnabled) {
                logInfo("Making Get url call to " + finalUrl);
            }

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            for (String header : this.headers.keySet()) {
                if (header.isEmpty()) {
                    continue;
                }
                con.setRequestProperty(header, this.headers.get(header));
            }

            int responseCode = con.getResponseCode();

            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            return new NouResponse(response.toString(), null, responseCode);

        } catch (Exception e) {
            if (logEnabled) {
                e.printStackTrace();
            }
            return new NouResponse(null, e, -1);
        }
    }

    private NouResponse doPost() {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            StringBuilder urlBody = new StringBuilder("");

            if (!params.isEmpty()) {
                for (String param : params.keySet()) {
                    urlBody.append(param);
                    urlBody.append("=");
                    urlBody.append(URLEncoder.encode(params.get(param), "UTF-8"));
                    urlBody.append("&");
                }
            } else if (body != null) {
                if (body instanceof JSONObject || body instanceof JSONArray) {
                    urlBody.append(body.toString());
                } else {
                    urlBody.append(GSON.toJson(body));
                }
            }

            for (String header : this.headers.keySet()) {
                if (header.isEmpty()) {
                    continue;
                }
                con.setRequestProperty(header, this.headers.get(header));
            }

            String body = urlBody.toString();
            // Send post request

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(body);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            if (logEnabled) {
                logInfo("\nSending 'POST' request to URL : " + url);
                logInfo("Post parameters : " + body);
                logInfo("Response Code : " + responseCode);
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String postResponse = response.toString();
            //print result
            logInfo("\nPOST RESPONSE : " + postResponse);
            return new NouResponse(postResponse, null, responseCode);

        } catch (Exception e) {
            if (logEnabled) {
                e.printStackTrace();
            }
            return new NouResponse(null, e, -1);
        }
    }
}
