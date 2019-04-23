package services;

import exchanges.Exchange;
import model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

//import org.postgresql.util;

/**
 * Copyright (c) Anton on 03.12.2018.
 */
public class DBManager {
    private Updater updater;
    private String url = "jdbc:postgresql://localhost:5432/cas";
    private String login = "postgres";
    private String password = "tMXVuD8JrJ8egE";

    public DBManager(Updater updater) {
        this.updater = updater;
    }

    public void saveStaticData() throws Exception {
        Connection connection = ConnectionManager.getDBconnection(url, login, password);
        saveExchangesAndPairs(updater.getExchanges(), connection);
        saveMarket(connection);
        connection.close();
    }

    public void saveOrders() throws Exception {
        Connection connection = ConnectionManager.getDBconnection(url, login, password);
        for (Exchange exchange : updater.getExchanges()) {
            ResultSet exchangeResSet = getExchangeResultSet(connection, exchange);
            if (exchangeResSet.next()) {
                Integer exchangeID = exchangeResSet.getInt("id");
                for (String pairName : exchange.getMarket().keySet()) {
                    ResultSet pairResSet = getPairResultSet(connection, pairName);
                    if (pairResSet.next()) {
                        Integer pairID = pairResSet.getInt("id");
                        ResultSet marketResSet = getMarketResultSet(connection, exchangeID, pairID);
                        if (marketResSet.next()) {
                            Integer marketId = marketResSet.getInt("id");
                            Pair pair = exchange.getMarket().get(pairName);
                            saveOrder(connection, marketId, pair);
                        }
                    }
                }
            }
        }
        connection.close();
    }

    public void getMarketsFromDB(
            Timestamp timestamp,
            Map<Integer, Exchange> idToExchange,
            Map<Integer, String> idToPair,
            Connection connection) throws SQLException, ClassNotFoundException {
        String sql = "select \"order\".id, market, market.exchange_id, market.pair_id\n" +
                "from public.\"order\"\n" +
                "join market\n" +
                "on market_id = market.id\n" +
                "where \"timestamp\" = '" + timestamp + "'";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            Integer id = resultSet.getInt("id");
            Exchange exchange = idToExchange.get(resultSet.getInt("exchange_id"));
            String pair = idToPair.get(resultSet.getInt("pair_id"));
            JSONObject marketJs = new JSONObject(resultSet.getString("market"));
            saveDataToExchange(exchange, pair, marketJs, id);
        }
    }


    private void saveDataToExchange(Exchange exchange, String pair, JSONObject marketJs, Integer id) {
        List<Order> bidsList = getOrderListFromJson(exchange, marketJs, OrderType.BID, id);
        List<Order> asksList = getOrderListFromJson(exchange, marketJs, OrderType.ASK, id);
        Pair bigPair = new Pair();
        bigPair.setOrders(OrderType.BID, bidsList);
        bigPair.setOrders(OrderType.ASK, asksList);
        exchange.getMarket().put(pair, bigPair);
    }

    private List<Order> getOrderListFromJson(Exchange exchange, JSONObject marketJs, OrderType type, Integer id) {
        List<Order> orders = new ArrayList<>();
        JSONArray jsonArray = (JSONArray) marketJs.get(type.getJSONKey(type));
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray bid = (JSONArray) jsonArray.get(i);
            BigDecimal price = new BigDecimal(bid.getDouble(0));
            BigDecimal amount = new BigDecimal(bid.getDouble(1));
            orders.add(new Order(exchange, price, amount, id));
        }
        return orders;
    }

    public ArrayList<Timestamp> getTimestamps(Connection connection) throws SQLException {
        ArrayList<Timestamp> timestamps = new ArrayList<>();
        String sql = "select timestamp from public.\"order\" group by timestamp  order by timestamp ASC";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            timestamps.add(resultSet.getTimestamp("timestamp"));
        }
        return timestamps;
    }

    public Map<Integer, Exchange> getIdToExchange(Updater updater, Connection connection) throws SQLException {
        Map<Integer, Exchange> exchangeToId = new HashMap<>();
        for (Exchange exchange : updater.getExchanges()) {
            ResultSet resultSet = getExchangeResultSet(connection, exchange);
            if (resultSet.next()) {
                exchangeToId.put(resultSet.getInt("id"), exchange);
            }
        }
        return exchangeToId;
    }

    public Map<Integer, String> getIdToPair(Updater updater, Connection connection) throws SQLException {
        Map<Integer, String> pairToId = new HashMap<>();
        Set<String> pairs = new HashSet<>();
        for (Exchange exchange : updater.getExchanges()) {
            pairs.addAll(exchange.getPairs());
        }
        for (String pair : pairs) {
            ResultSet resultSet = getPairResultSet(connection, pair);
            if (resultSet.next()) {
                pairToId.put(resultSet.getInt("id"), pair);
            }
        }
        return pairToId;
    }

    private void saveOrder(Connection connection, Integer marketId, Pair pair) throws SQLException {
        String sql = "INSERT INTO \"order\" (market_id, market, timestamp) VALUES (?, ?, ?) ";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, marketId);
        statement.setString(2, castMarket2JSON(pair));
        statement.setObject(3, Updater.getTimestamp());
        statement.executeUpdate();
    }

    private String castMarket2JSON(Pair pair) {
        StringBuilder stringBuilder = new StringBuilder();
//      {
//      "bids" : [[price, amount],...],
//      "asks" : [[price, amount],...]
//      }
        stringBuilder.append("{ \"bids\" : [");
        for (Order order : pair.getOrders(OrderType.BID)) {
            stringBuilder.append("[").append(order.getPrice()).append(",").append(order.getAmount()).append("],");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1).append("],");
        stringBuilder.append("\"asks\" : [");
        for (Order order : pair.getOrders(OrderType.ASK)) {
            stringBuilder.append("[").append(order.getPrice()).append(",").append(order.getAmount()).append("],");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1).append("]");
        stringBuilder.append("}");
        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }


    private void saveMarket(Connection connection) throws SQLException {
        for (Exchange exchange : Updater.getInstance().getExchanges()) {
            ResultSet exchangeResSet = getExchangeResultSet(connection, exchange);
            if (exchangeResSet.next()) {
                Integer exchangeID = exchangeResSet.getInt("id");
                for (String pair : exchange.getPairs()) {
                    ResultSet pairResSet = getPairResultSet(connection, pair);
                    if (pairResSet.next()) {
                        Integer pairID = pairResSet.getInt("id");
                        String sql = "INSERT INTO market (exchange_id, pair_id) VALUES (?,?)";
                        PreparedStatement statement = connection.prepareStatement(sql);
                        try {
                            statement.setInt(1, exchangeID);
                            statement.setInt(2, pairID);
                            statement.executeUpdate();
                        } catch (SQLException ignored) {
                        }
                    }
                }
            }
        }
    }

    private ResultSet getMarketResultSet(Connection connection, int exchangeID, int pairID) throws SQLException {
        String sql = "SELECT * FROM market where exchange_id = ? AND pair_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, exchangeID);
        statement.setInt(2, pairID);
        return statement.executeQuery();
    }

    private ResultSet getOrderResultSet(Connection connection, int marketId) throws SQLException {
        String sql = "SELECT * from \"order\" where market_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, marketId);
        return statement.executeQuery();
    }


    private ResultSet getPairResultSet(Connection connection, String pair) throws SQLException {
        String sql = "SELECT * FROM pair where name = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, pair);
        return statement.executeQuery();
    }

    private ResultSet getExchangeResultSet(Connection connection, Exchange exchange) throws SQLException {
        String sql = "SELECT * FROM exchange where name = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, exchange.getClass().getSimpleName());
        return statement.executeQuery();
    }

    private void saveExchangesAndPairs(List<Exchange> exchanges, Connection connection) throws Exception {
        for (Exchange exchange : exchanges) {
            String sql = "INSERT INTO exchange (name) VALUES (?)";
            PreparedStatement statement1 = connection.prepareStatement(sql);
            try {
                statement1.setString(1, exchange.getClass().getSimpleName());
                statement1.executeUpdate();
            } catch (SQLException ignored) {
            }
            for (String pair : exchange.getPairs()) {
                sql = "INSERT INTO pair (name) VALUES (?)";
                PreparedStatement statement2 = connection.prepareStatement(sql);
                try {
                    statement2.setString(1, pair);
                    statement2.executeUpdate();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public void saveRoute(Connection connection,
                          Route route,
                          Map<Exchange, Integer> exchangeToId,
                          Map<String, Integer> pairToId) throws SQLException {
        String sql = "INSERT INTO route (exchangeFrom_id,exchangeTo_id,pair_id,amount,spread,taxFrom,taxto) VALUES (\n" +
                "?,?,?,?,?,?,?\n" +
                ")\n";
        PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        statement.setInt(1, exchangeToId.get(route.getExchangeFrom()));
        statement.setInt(2, exchangeToId.get(route.getExchangeTo()));
        statement.setInt(3, pairToId.get(route.getPairName()));
        statement.setBigDecimal(4, route.getAmount());
        statement.setBigDecimal(5, route.getSpread());
        statement.setBigDecimal(6, route.getTaxFrom());
        statement.setBigDecimal(7, route.getTaxTo());

        statement.executeUpdate();

        ResultSet keys = statement.getGeneratedKeys();
        System.out.println();
        System.out.println();
        keys.next();
        int key = keys.getInt(1);

        for (Deal deal : route.getSortedEVDeals()) {
            saveDeal(deal, connection,key);
        }
    }

    private void saveDeal(Deal deal, Connection connection, int key) throws SQLException {
        String sql = "INSERT INTO deal (route_id, amount, spread, order_from_id, order_to_id) VALUES (\n" +
                "?,?,?,?,?\n" +
                ")\n";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, key);
        statement.setBigDecimal(2, deal.getEffectiveAmount());
        statement.setBigDecimal(3, deal.getSpread());
        statement.setInt(4, deal.getBid().getId());
        statement.setInt(5, deal.getAsk().getId());
        statement.executeUpdate();
        System.out.println();


    }
}



