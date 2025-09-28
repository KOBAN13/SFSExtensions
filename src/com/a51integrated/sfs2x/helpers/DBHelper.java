package com.a51integrated.sfs2x.helpers;

import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DBHelper
{
    public static String getTable(SFSExtension parentExtension, String key, String tableName)
    {
        return parentExtension.getConfigProperties().getProperty(key, tableName);
    }

    public static IDBManager getDb(SFSExtension parentExtension)
    {
        return parentExtension.getParentZone().getDBManager();
    }

    public static PreparedStatement getStatement(IDBManager dbManager, String sqlRequest, int statementArgument) throws SQLException
    {
        var connection = dbManager.getConnection();
        return connection.prepareStatement(sqlRequest, statementArgument);
    }

    public static PreparedStatement getStatement(IDBManager dbManager, String sqlRequest) throws SQLException
    {
        var connection = dbManager.getConnection();
        return connection.prepareStatement(sqlRequest);
    }
}
