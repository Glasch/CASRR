package services;

import oauth.signpost.http.HttpParameters;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
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

    public static JSONObject readJSONFromSignedPostRequest(String url, String data, String key, String secretKey) throws DecoderException {
        String dataFromServer = "";

        String signedData = calculateHMAC(data, secretKey);
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);

            httpPost.addHeader("Key", key);
            httpPost.addHeader("Sign", signedData);


            httpPost.setEntity(new ByteArrayEntity(data.getBytes(), ContentType.APPLICATION_FORM_URLENCODED));

            CloseableHttpResponse response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
               dataFromServer = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
            } else throw new NullPointerException("entity = null!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONObject(dataFromServer);
    }

    public static JSONObject readTradeJSONFromSignedPostRequest(String url, String data, String key, String secretKey) throws DecoderException, URISyntaxException {
        String dataFromServer = "";
        ArrayList<NameValuePair> postParameters;
        String signedData = calculateHMAC(data, secretKey);
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Key", key);
            httpPost.addHeader("Sign", signedData);

//            postParameters = new ArrayList<>();
//            postParameters.add(new BasicNameValuePair("pair", "usd_btc"));
//            postParameters.add(new BasicNameValuePair("type", "sell"));
//            postParameters.add(new BasicNameValuePair("rate", "0.002"));
//            postParameters.add(new BasicNameValuePair("amount", "1"));
//            postParameters.add(new BasicNameValuePair("method", "Trade"));
//            postParameters.add(new BasicNameValuePair("nonce", String.valueOf(Instant.now().getEpochSecond())));

          httpPost.setEntity(new ByteArrayEntity(data.getBytes(), ContentType.APPLICATION_FORM_URLENCODED));
          //; todo Если так, то он логинится, но error = "invalid Pair"
//            httpPost.setEntity(new UrlEncodedFormEntity(postParameters)); // todo А если так, то он не логиниться, но судя по всям гайдам должен отправить нормальный пост запрос на сервак
            // TODO: 18.03.2019 И я короч хз как объединить это все в одну историю.

            CloseableHttpResponse response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                dataFromServer = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
            } else throw new NullPointerException("entity = null!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONObject(dataFromServer);
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

    public static JSONObject getHitBtcPostJsonArray(String requestString, String data, String login, String password) throws AuthenticationException {
        Logger.getLogger("org.apache").setLevel(Level.OFF);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(login, password));
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
        HttpPost request = new HttpPost(requestString);
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36" +
                " (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");

        request.setEntity(new ByteArrayEntity(data.getBytes(), ContentType.APPLICATION_FORM_URLENCODED));

        HttpResponse response;
        JSONObject jsonArray;
        try {
            response = client.execute(request);
            HttpEntity entity = response.getEntity();
            jsonArray = new JSONObject(EntityUtils.toString(entity));
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

    private static String calculateHMAC(String data, String key) throws DecoderException {
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



