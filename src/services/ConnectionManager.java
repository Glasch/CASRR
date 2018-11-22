 package services;

 import org.json.JSONObject;

 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;

/**
 * Copyright (c) Anton on 17.10.2018.
 */

public class ConnectionManager {

    public static JSONObject readJSONFromRequest(String request) throws IOException {
        StringBuilder jsonText;
        jsonText = new StringBuilder();
        URL url = new URL(request);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36" +
                " (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
        InputStream is;
        is = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            jsonText.append(line);
        }
        reader.close();
        return new JSONObject(jsonText.toString());
    }
}

