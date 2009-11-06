package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.exception.DatabaseException;
import org.webharvest.utils.CommonUtil;

import java.sql.*;

/**
 * Support for database operations.
 */
public class DatabasePlugin extends WebHarvestPlugin {

    private class ColumnDescription {
        private String name;
        private int type;
        private String identifier;

        private ColumnDescription(String name, int type) {
            this.name = name;
            this.type = type;
            this.identifier = CommonUtil.getValidIdentifier(name);
        }
    }

    public String getName() {
        return "database";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        String jdbc = evaluateAttribute("jdbcclass", scraper);
        String connection = evaluateAttribute("connection", scraper);
        String username = evaluateAttribute("username", scraper);
        String password = evaluateAttribute("password", scraper);
        String output = evaluateAttribute("output", scraper);
        if ( output == null || !"text".equalsIgnoreCase(output.trim()) ) {
            output = "xml";
        }
        boolean isXmlOutput = "xml".equalsIgnoreCase(output);
        String fieldSeparator = CommonUtil.nvl( evaluateAttribute("fieldseparator", scraper), "," );
        String rowElement = evaluateAttribute("rowelement", scraper);
        if ( rowElement == null || "".equals(rowElement.trim()) ) {
            rowElement = "row";
        }
        int maxRows = evaluateAttributeAsInteger("max", -1, scraper);
        boolean isAutoCommit = evaluateAttributeAsBoolean("autocommit", true, scraper);

        Connection conn = scraper.getConnection(jdbc, connection, username, password);
        Variable body = executeBody(scraper, context);
        String sql = body.toString();

        try {
            conn.setAutoCommit(isAutoCommit);
            PreparedStatement statement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();

            if (resultSet != null) {
                ResultSetMetaData metadata = resultSet.getMetaData();
                ListVariable queryResult = new ListVariable();
                int columnCount = metadata.getColumnCount();
                ColumnDescription colDescs[] = new ColumnDescription[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    String colName = metadata.getColumnLabel(i);
                    int colType = metadata.getColumnType(i);
                    colDescs[i - 1] = new ColumnDescription(colName, colType);
                }

                int rowCount = 0;
                while ( resultSet.next() && (maxRows < 0 || rowCount < maxRows) ) {
                    StringBuffer row = isXmlOutput ? new StringBuffer("<" + rowElement + ">") : new StringBuffer();
                    for (int i = 1; i <= columnCount; i++) {
                        String colName = colDescs[i - 1].name;
                        String colIdentifier = colDescs[i - 1].identifier;
                        String field = resultSet.getString(colName);
                        if (isXmlOutput) {
                            row.append("<").append(colIdentifier).append(">");
                            row.append(CommonUtil.escapeXml(field));
                            row.append("</").append(colIdentifier).append(">");
                        } else {
                            row.append(field);
                            if (i < columnCount) {
                                row.append(fieldSeparator);
                            }
                        }
                    }
                    if (isXmlOutput) {
                        row.append("</").append(rowElement).append(">");
                    } else {
                        row.append("\n");
                    }

                    queryResult.addVariable( new NodeVariable(row) );
                    rowCount++;
                }
                return queryResult;
            } else {
                return new EmptyVariable();
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        

    }

    public String[] getValidAttributes() {
        return new String[] {
                "jdbcclass",
                "connection",
                "username",
                "password",
                "output",
                "rowelement",
                "fieldseparator",
                "max",
                "autocommit"};
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

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("output".equalsIgnoreCase(attributeName)) {
            return new String[] {"text", "xml"};
        } else if ("autocommit".equalsIgnoreCase(attributeName)) {
            return new String[] {"true", "false"};
        }
        return null;
    }

}