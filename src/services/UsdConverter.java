package services;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import org.json.JSONObject;

public class UsdConverter {
    private HashMap<String, BigDecimal> currencyPrices = new HashMap<String, BigDecimal> (){{
        put("USD", BigDecimal.valueOf(1));
        put("USDT", BigDecimal.valueOf(1));
    }};
    private static final HashMap<String, Integer> currencyIds = new HashMap (){{
        put("BTC", 1);
        put("ETH", 1027);
    }};

    public UsdConverter() throws IOException {
        for (String currencyName : currencyIds.keySet()) {
            JSONObject coinListings = ConnectionManager.readJSONFromRequest("https://api.coinmarketcap.com/v2/ticker/" + currencyIds.get(currencyName).toString());
            this.currencyPrices.put( currencyName,  coinListings.getJSONObject("data").getJSONObject("quotes").getJSONObject("USD").getBigDecimal("price"));
        }

        currencyIds.clear();
    }


   synchronized public BigDecimal convert(String pair, BigDecimal Quantity)
    {
        String[] sub = pair.split("/");
        BigDecimal res;
        try {
            res = currencyPrices.get(sub[1]).multiply(Quantity);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println();
            return  BigDecimal.ZERO;
        }
        return res;
    }
}
