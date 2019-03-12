package services;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright (c) Anton on 17.10.2018.
 */

public class ConnectionManager {

    public static JSONObject readJSONFromRequest(String requestString) {
        Logger.getLogger("org.apache").setLevel(Level.OFF);

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient client = builder.build();

        HttpGet request = new HttpGet(requestString);

        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36" +
                " (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
        HttpResponse response;
        JSONObject jsonObject;
        try {
            response = client.execute(request);
            HttpEntity entity = response.getEntity();
            jsonObject = new JSONObject(EntityUtils.toString(entity));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != 200) {
                System.out.println("Bad response: " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
                System.out.println("Request: " + requestString);
                return null;
            }
        } catch (Exception e) {
            System.out.println("Could not get response: " + e.getMessage());
            System.out.println("Request: " + requestString);
            return null;
        }
        return jsonObject;
    }

    public static JSONArray getHitBtcBalanceJsonArray(String requestString, String login, String password) throws AuthenticationException {

        Logger.getLogger("org.apache").setLevel(Level.OFF);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(login, password));
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
        HttpGet request = new HttpGet(requestString);
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36" +
                " (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
        HttpResponse response;
        JSONArray jsonObject;
        try {
            response = client.execute(request);
            HttpEntity entity = response.getEntity();
            jsonObject = new JSONArray(EntityUtils.toString(entity));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != 200) {
                System.out.println("Bad response: " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
                System.out.println("Request: " + requestString);
                return null;
            }
        } catch (Exception e) {
            System.out.println("Could not get response: " + e.getMessage());
            System.out.println("Request: " + requestString);
            return null;
        }
        return jsonObject;
    }


    public static Connection getDBconnection(String url, String login, String password) throws SQLException, ClassNotFoundException {
        //Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(url, login, password);
    }


}



