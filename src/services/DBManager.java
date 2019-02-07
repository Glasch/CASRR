package services;

import exchanges.Exchange;
import model.Order;
import model.OrderType;
import model.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Copyright (c) Anton on 03.12.2018.
 */
public class DBManager {
    private Updater updater;
    private String url = "jdbc:postgresql://185.246.153.215:5432/cas";
    private String login = "postgres";
    private String password = "tMXVuD8JrJ8egE";

    private DBManager(Updater updater) {
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

    private void getMarketsFromDB(Updater updater) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionManager.getDBconnection(url, login, password);

        Map <Exchange, Integer> exchangeToId = initExchangeToId(updater, connection);
        Map <String, Integer> pairToId = initPairToId(updater, connection);

        for (Exchange exchange : exchangeToId.keySet()) {
            for (String pair : pairToId.keySet()) {
                ResultSet marketResultSet = getMarketResultSet(connection, exchangeToId.get(exchange), pairToId.get(pair));
                if (marketResultSet.next()) {
                    Integer marketId = marketResultSet.getInt("id");
                    ResultSet orderResultSet = getOrderResultSet(connection, marketId);
                    if (orderResultSet.next()) {
                        JSONObject json = new JSONObject(orderResultSet.getString("market"));
                        System.out.println(json);
                        JSONArray bids = json.getJSONArray("bids");
                        for (Object bid : bids) {
                            System.out.println(bid);
                        }
                    }
                }
            }
        }

    }

    private Map <Exchange, Integer> initExchangeToId(Updater updater, Connection connection) throws SQLException {
        Map <Exchange, Integer> exchangeToId = new HashMap <>();
        for (Exchange exchange : updater.getExchanges()) {
            ResultSet resultSet = getExchangeResultSet(connection, exchange);
            if (resultSet.next()) {
                exchangeToId.put(exchange, resultSet.getInt("id"));
            }
        }
        return exchangeToId;
    }

    private Map <String, Integer> initPairToId(Updater updater, Connection connection) throws SQLException {
        Map <String, Integer> pairToId = new HashMap <>();
        Set <String> pairs = new HashSet <>();
        for (Exchange exchange : updater.getExchanges()) {
            pairs.addAll(exchange.getPairs());
        }
        for (String pair : pairs) {
            ResultSet resultSet = getPairResultSet(connection, pair);
            if (resultSet.next()) {
                pairToId.put(pair, resultSet.getInt("id"));
            }
        }
        return pairToId;
    }


    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Updater updater = Updater.getInstance();
        DBManager dbManager = new DBManager(updater);
        dbManager.getMarketsFromDB(updater);
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



