package services;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class UsdConverter {
    private static UsdConverter instance;

    private static final Map<String, Integer> currencyIds = new HashMap <String, Integer>() {{
        put("BTC", 1);
        put("ETH", 1027);
    }};

    private Map <String, BigDecimal> currencyPrices = new HashMap <String, BigDecimal>() {{
        put("USD", BigDecimal.valueOf(1));
        put("USDT", BigDecimal.valueOf(1));
    }};

    public static UsdConverter getInstance() {
        if (instance == null) {
            instance = new UsdConverter();
        }
        return instance;
    }

    void loadData() {
        for (String currencyName : currencyIds.keySet()) {
            JSONObject coinListings = ConnectionManager.readJSONFromRequest("https://api.coinmarketcap.com/v2/ticker/" +
                currencyIds.get(currencyName));
            if (coinListings == null) {
                System.out.println(getClass().getName() + ": unable to get coin listings for " + currencyName + " to dollar");
            } else {
                currencyPrices.put(currencyName,
                        coinListings.getJSONObject("data").getJSONObject("quotes").getJSONObject("USD").getBigDecimal("price"));
            }
        }
    }


    public BigDecimal convert(String pair, BigDecimal Quantity) {
        String[] sub = pair.split("/");
        return BigDecimal.ZERO;  //currencyPrices.get(sub[1]).multiply(Quantity);
    }

    Map <String, BigDecimal> getCurrencyPrices() {
        return currencyPrices;
    }
}
