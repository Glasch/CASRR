package services;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;

import org.json.JSONObject;

public class UsdConverter {
    private static UsdConverter instance;

    private static final HashMap<String, Integer> currencyIds = new HashMap<String, Integer>() {{
        put("BTC", 1);
        put("ETH", 1027);
    }};

    private HashMap<String, BigDecimal> currencyPrices = new HashMap<String, BigDecimal>() {{
        put("USD", BigDecimal.valueOf(1));
        put("USDT", BigDecimal.valueOf(1));
    }};

    public static UsdConverter getInstance() {
        if (instance == null) {
            instance = new UsdConverter();
        }
        return instance;
    }

    private UsdConverter() {
    }

    public void loadData() {
        for (String currencyName : currencyIds.keySet()) {
            JSONObject coinListings = ConnectionManager.readJSONFromRequest("https://api.coinmarketcap.com/v2/ticker/" +
                    currencyIds.get(currencyName).toString());
            if (coinListings == null) {
                System.out.println(getClass().getName() + ": unable to get coin listings for " + currencyName + " to dollar");
            } else {
                this.currencyPrices.put(currencyName,
                        coinListings.getJSONObject("data").getJSONObject("quotes").getJSONObject("USD").getBigDecimal("price"));
            }
        }
    }


    public BigDecimal convert(String pair, BigDecimal Quantity) {
        String[] sub = pair.split("/");
        return currencyPrices.get(sub[1]).multiply(Quantity);
    }
}
