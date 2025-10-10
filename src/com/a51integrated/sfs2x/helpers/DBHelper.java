package com.a51integrated.sfs2x.helpers;

import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBHelper
{
    public static String getProperty(SFSExtension parentExtension, String propertyName)
    {
        return parentExtension.getConfigProperties().getProperty(propertyName);
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
