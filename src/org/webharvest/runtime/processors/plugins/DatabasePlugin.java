package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.exception.DatabaseException;
import org.webharvest.utils.CommonUtil;

import java.sql.*;

/**
 * Support for database operations.
 */
public class DatabasePlugin extends WebHarvestPlugin {

    public String getName() {
        return "database";
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        String jdbc = evaluateAttribute("jdbcclass", scraper);
        String connection = evaluateAttribute("connection", scraper);
        String username = evaluateAttribute("username", scraper);
        String password = evaluateAttribute("password", scraper);
        String rowElement = evaluateAttribute("rowelement", scraper);
        if ( rowElement == null || "".equals(rowElement.trim()) ) {
            rowElement = "row";
        }

        Connection conn = scraper.getConnection(jdbc, connection, username, password);
        Variable body = executeBody(scraper, context);
        String sql = body.toString();

        ListVariable queryResult = new ListVariable();
        try {
            PreparedStatement statement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();

            while (resultSet.next()) {
                StringBuffer row = new StringBuffer("<" + rowElement + ">");
                for (int i = 1; i <= columnCount; i++) {
                    String colName = metadata.getColumnLabel(i);
                    int colType = metadata.getColumnType(i);
                    String field = resultSet.getString(colName);
                    row.append("<" + colName + ">");
                    row.append(CommonUtil.escapeXml(field));
                    row.append("</" + colName + ">");
                }
                row.append("</" + rowElement + ">");

                queryResult.addVariable( new NodeVariable(row) );
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        
        return queryResult;
    }

    public String[] getValidAttributes() {
        return new String[] {"jdbcclass", "connection", "username", "password"};
    }

    public String[] getRequiredAttributes() {
        return new String[] {"jdbcclass", "connection"};
    }

    public String[] getValidSubprocessors() {
        return null;
    }

    public String[] getRequiredSubprocessors() {
        return null;
    }
    
}