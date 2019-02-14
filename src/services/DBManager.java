package services;

import exchanges.Exchange;
import model.Order;
import model.OrderType;
import model.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PSQLException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/**
 * Copyright (c) Anton on 03.12.2018.
 */
public class DBManager {
    private Updater updater;
    private String url = "jdbc:postgresql://185.246.153.215:5432/cas";
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
            Map <Integer, Exchange> idToExchange,
            Map <Integer, String> idToPair,
            Connection connection) throws SQLException, ClassNotFoundException {
        String sql = "select market, market.exchange_id, market.pair_id\n" +
                "from public.\"order\"\n" +
                "join market\n" +
                "on market_id = market.id\n" +
                "where \"timestamp\" = '" + timestamp + "'";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            Exchange exchange = idToExchange.get(resultSet.getInt("exchange_id"));
            String pair = idToPair.get(resultSet.getInt("pair_id"));
            JSONObject marketJs = new JSONObject(resultSet.getString("market"));
            savaDataToExchange(exchange, pair, marketJs);
        }
    }


    private void savaDataToExchange(Exchange exchange, String pair, JSONObject marketJs) {
        List <Order> bidsList = getOrderListFromJson(exchange, marketJs, OrderType.BID);
        List <Order> asksList = getOrderListFromJson(exchange, marketJs, OrderType.ASK);
        Pair bigPair = new Pair();
        bigPair.setOrders(OrderType.BID, bidsList);
        bigPair.setOrders(OrderType.ASK, asksList);
        exchange.getMarket().put(pair, bigPair);
    }

    private List <Order> getOrderListFromJson(Exchange exchange, JSONObject marketJs, OrderType type) {
        List <Order> orders = new ArrayList <>();
        JSONArray jsonArray = (JSONArray) marketJs.get(type.getJSONKey(type));
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray bid = (JSONArray) jsonArray.get(i);
            BigDecimal prize = new BigDecimal(bid.getDouble(0));
            BigDecimal amount = new BigDecimal(bid.getDouble(1));
            orders.add(new Order(exchange, prize, amount));
        }
        return orders;
    }

    public Set <Timestamp> getTimestamps(Connection connection) throws SQLException {
        Set <Timestamp> timestamps = new HashSet <>();
        String sql = "select timestamp from public.\"order\" group by timestamp  order by timestamp ASC";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            timestamps.add(resultSet.getTimestamp("timestamp"));
        }
        return timestamps;
    }

    public Map <Integer, Exchange> getIdToExchange(Updater updater, Connection connection) throws SQLException {
        Map <Integer, Exchange> exchangeToId = new HashMap <>();
        for (Exchange exchange : updater.getExchanges()) {
            ResultSet resultSet = getExchangeResultSet(connection, exchange);
            if (resultSet.next()) {
                exchangeToId.put(resultSet.getInt("id"), exchange);
            }
        }
        return exchangeToId;
    }

    public Map <Integer, String> getIdToPair(Updater updater, Connection connection) throws SQLException {
        Map <Integer, String> pairToId = new HashMap <>();
        Set <String> pairs = new HashSet <>();
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
                        } catch (PSQLException ignored) {
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

    private void saveExchangesAndPairs(List <Exchange> exchanges, Connection connection) throws Exception {
        for (Exchange exchange : exchanges) {
            String sql = "INSERT INTO exchange (name) VALUES (?)";
            PreparedStatement statement1 = connection.prepareStatement(sql);
            try {
                statement1.setString(1, exchange.getClass().getSimpleName());
                statement1.executeUpdate();
            } catch (PSQLException ignored) {
            }
            for (String pair : exchange.getPairs()) {
                sql = "INSERT INTO pair (name) VALUES (?)";
                PreparedStatement statement2 = connection.prepareStatement(sql);
                try {
                    statement2.setString(1, pair);
                    statement2.executeUpdate();
                } catch (PSQLException ignored) {
                }
            }
        }
    }
}



