package com.zpedroo.plotsquared.mysql;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.Currency;
import com.zpedroo.plotsquared.objects.SellingPlot;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DBManager {

    public void saveSellingPlot(SellingPlot sellingPlot) {
        if (contains(sellingPlot.getPlot().getId().toString(), "plot_id")) {
            String query = "UPDATE `" + DBConnection.TABLE + "` SET" +
                    "`plot_id`='" + sellingPlot.getPlot().getId().toString() + "', " +
                    "`price`='" + sellingPlot.getPrice().toString() + "', " +
                    "`currency`='" + sellingPlot.getCurrency().getFileName() + "' " +
                    "WHERE `plot_id`='" + sellingPlot.getPlot().getId().toString() + "';";
            executeUpdate(query);
            return;
        }

        String query = "INSERT INTO `" + DBConnection.TABLE + "` (`plot_id`, `price`, `currency`) VALUES " +
                "('" + sellingPlot.getPlot().getId().toString() + "', " +
                "'" + sellingPlot.getPrice().toString() + "', " +
                "'" + sellingPlot.getCurrency().getFileName() + "');";
        executeUpdate(query);
    }

    public List<SellingPlot> getSellingPlots() {
        List<SellingPlot> ret = new LinkedList<>();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "`;";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            List<PlotArea> plotAreas = new ArrayList<>(PS.get().getPlotAreas());
            while (result.next()) {
                PlotId plotId = PlotId.fromString(result.getString(1));
                BigInteger price = result.getBigDecimal(2).toBigInteger();
                Currency currency = CurrencyAPI.getCurrency(result.getString(3));
                if (currency == null) continue;

                Plot plot = plotAreas.get(0).getPlot(plotId);
                if (plot == null) continue;

                ret.add(new SellingPlot(plot, price, currency));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, result, preparedStatement, null);
        }

        return ret;
    }


    public void deleteSellingPlot(SellingPlot sellingPlot) {
        String query = "DELETE FROM `" + DBConnection.TABLE + "` WHERE `plot_id`='" + sellingPlot.getPlot().getId().toString() + "';";
        executeUpdate(query);
    }

    private Boolean contains(String value, String column) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT `" + column + "` FROM `" + DBConnection.TABLE + "` WHERE `" + column + "`='" + value + "';";
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();
            return result.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, result, preparedStatement, null);
        }

        return false;
    }

    private void executeUpdate(String query) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, null, null, statement);
        }
    }

    private void closeConnection(Connection connection, ResultSet resultSet, PreparedStatement preparedStatement, Statement statement) {
        try {
            if (connection != null) connection.close();
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (statement != null) statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS `" + DBConnection.TABLE + "` (`plot_id` VARCHAR(255), `price` DECIMAL(40,0), `currency` VARCHAR(16), PRIMARY KEY(`plot_id`));";
        executeUpdate(query);
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }
}