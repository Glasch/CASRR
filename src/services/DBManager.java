package services;

import exchanges.Exchange;
import model.Order;
import model.OrderType;
import model.Pair;
import org.postgresql.util.PSQLException;

import java.sql.*;
import java.util.ArrayList;

/**
 * Copyright (c) Anton on 03.12.2018.
 */
public class DBManager {
    Updater updater;
    private String url = "jdbc:postgresql://localhost:5432/cas";
    private String login = "postgres";
    private String password = "postgrespass";

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
                            saveMarket(connection, marketId, pair);
                        }

                    }
                }
            }
        }
        connection.close();
    }

    private void saveMarket(Connection connection, Integer marketId, Pair pair ) throws SQLException {
        String sql = "INSERT INTO \"order\" (market_id, market, timestamp) VALUES (?, ?, ?) ";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, marketId);
        statement.setString(2, castMarket2JSON(pair));
        statement.setObject(3, new Date(Updater.getTimestamp().getTime()));
        statement.executeUpdate();
    }

    private String castMarket2JSON(Pair pair) {
        // {
        // "bids' : [[price, amount]...]
        // 'asks' : [[price, amount]...]
        // }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{ \"bids\" : [" );
        for (Order order : pair.getOrders(OrderType.BID)) {
            stringBuilder.append("[");
            stringBuilder.append(order.getPrice() + "," + order.getAmount());
            stringBuilder.append("],");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.append("],");
        stringBuilder.append("\"asks\" : [" );
        for (Order order : pair.getOrders(OrderType.ASK)) {
            stringBuilder.append("[");
            stringBuilder.append(order.getPrice() + "," + order.getAmount());
            stringBuilder.append("],");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.append("]");
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

    private void saveExchangesAndPairs(ArrayList <Exchange> exchanges, Connection connection) throws Exception {
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



