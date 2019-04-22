package services;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright (c) Anton on 17.10.2018.
 */

public class ConnectionManager {

    public static JSONObject getRequest(String requestString, ArrayList<Header> headerList) {
        Logger.getLogger("org.apache").setLevel(Level.OFF);
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient client = builder.build();
        HttpGet request = new HttpGet(requestString);
        if (headerList != null){
            for (Header header : headerList) {
                request.addHeader(header);
            }
        }
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

    public static JSONObject postRequest(String url, String data, Map<String, String> headers)
    {
        try
        {
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; JAVA AWT)");

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }
            con.setUseCaches(false);
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();
            //return sb.toString();
            System.out.println(sb.toString());
            System.out.println();
            return null;

        }
        catch (Exception e)
        {
        }
        return null;
    }
    public static JSONObject sendPostRequest(String url, String data, Map<String, String> headers) {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost http = new HttpPost(url);

            http.addHeader( new BasicHeader("User-Agent", "Mozilla/4.0 (compatible; JAVA AWT)"));

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                http.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
            }
            http.setEntity(new ByteArrayEntity(data.getBytes(), ContentType.APPLICATION_FORM_URLENCODED));
            //http.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));

            HttpResponse response;
            JSONObject jsonObject;

            try {
                response = client.execute(http);
                HttpEntity entity = response.getEntity();
                jsonObject = new JSONObject(EntityUtils.toString(entity));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() != 200) {
                    System.out.println("Bad response: " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
                    System.out.println("Request: " + data);
                    return null;
                }
            } catch (Exception e) {
                System.out.println("Could not get response: " + e.getMessage());
                System.out.println("Request: " + data);
                return null;
            }

            return jsonObject;
    }



    public static JSONArray sendBasicGetRequest(String requestString, String login, String password) {
        Logger.getLogger("org.apache").setLevel(Level.OFF);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
        HttpGet request = new HttpGet(requestString);
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36" +
                " (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
        HttpResponse response;
        JSONArray jsonArray;
        try {
            response = client.execute(request);
            HttpEntity entity = response.getEntity();
            jsonArray = new JSONArray(EntityUtils.toString(entity));
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
        return jsonArray;
    }

    public static JSONObject sendBasicPostRequest(String requestString, ArrayList<NameValuePair> data, String login, String password) {
        Logger.getLogger("org.apache").setLevel(Level.OFF);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
        HttpPost request = new HttpPost(requestString);
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36" +
                " (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");


        try {
            request.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));

            HttpResponse response;
            JSONObject jsonArray;
            response = client.execute(request);
            HttpEntity entity = response.getEntity();
            jsonArray = new JSONObject(EntityUtils.toString(entity));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != 200) {
                System.out.println("Bad response: " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
                System.out.println("Request: " + requestString);
                System.out.println("Response: " + jsonArray);
                return null;
            }
            return jsonArray;
        } catch (Exception e) {
            System.out.println("Could not get response: " + e.getMessage());
            System.out.println("Request: " + requestString);
            return null;
        }

    }
    public static JSONArray sendBasicDeleteRequest(String requestString,String login, String password) {
        Logger.getLogger("org.apache").setLevel(Level.OFF);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();


        HttpDelete request = new HttpDelete(requestString);
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36" +
                " (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");

        try {

            HttpResponse response;
            JSONArray jsonArray;
            response = client.execute(request);
            HttpEntity entity = response.getEntity();
            jsonArray = new JSONArray(EntityUtils.toString(entity));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != 200) {
                System.out.println("Bad response: " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
                System.out.println("Request: " + requestString);
                System.out.println("Response: " + jsonArray);
                return null;
            }
            return jsonArray;
        } catch (Exception e) {
            System.out.println("Could not get response: " + e.getMessage());
            System.out.println("Request: " + requestString);
            return null;
        }

    }

    public static String calculateHMAC(String data, String key) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HMACSHA512");
        Mac mac = null;
        try {
            mac = Mac.getInstance("HMACSHA512");
            mac.init(secretKeySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        byte[] hmac = mac.doFinal(data.getBytes());
        return Hex.encodeHexString(hmac);
    }

    public static Connection getDBconnection(String url, String login, String password) throws SQLException, ClassNotFoundException {
        //Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(url, login, password);
    }


}



