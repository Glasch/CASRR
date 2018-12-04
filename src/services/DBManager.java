package services;

import exchanges.Exchange;
import model.Order;
import model.OrderType;
import org.postgresql.util.PSQLException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.TimeZone;

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
                            for (Order orderBid : exchange.getMarket().get(pairName).getOrders(OrderType.BID)) {
                                BigDecimal bidPrice = orderBid.getPrice();
                                BigDecimal bidAmount = orderBid.getAmount();
                                for (Order orderAsk : exchange.getMarket().get(pairName).getOrders(OrderType.ASK)) {
                                    BigDecimal askPrice = orderAsk.getPrice();
                                    BigDecimal askAmount = orderAsk.getAmount();
                                    String sql = "INSERT INTO \"order\" (market_id, bid_price, bid_amount, ask_price, ask_amount, timestamp) VALUES (?, ?, ?, ?, ?, ?) ";
                                    PreparedStatement statement = connection.prepareStatement(sql);
                                    statement.setInt(1, marketId);
                                    statement.setBigDecimal(2, bidPrice);
                                    statement.setBigDecimal(3, bidAmount);
                                    statement.setBigDecimal(4, askPrice);
                                    statement.setBigDecimal(5,askAmount);
                                    statement.setObject(6, new java.sql.Date(Updater.getTimestamp().getTime()));
                                    statement.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        }
        connection.close();
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



