package com.a51integrated.sfs2x;

import com.a51integrated.sfs2x.helpers.DBHelper;
import com.smartfoxserver.v2.extensions.SFSExtension;
import com.smartfoxserver.v2.util.TaskScheduler;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class GlobalMaintenanceExtension extends SFSExtension
{
    @Override
    public void init()
    {
        StartTaskScheduler();
        trace("Starting BackendExtension");
    }

    private void StartTaskScheduler()
    {
        TaskScheduler scheduler = new TaskScheduler(4);

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                ClearExpiredTokens();
            }
        }, 0, 60, TimeUnit.MINUTES);
    }

    private void ClearExpiredTokens()
    {
        trace("Clear Expired Tokens");

        var passwordResetTable = getConfigProperties().getProperty("db.table.reset_tokens");

        var sql = String.format("DELETE FROM %s WHERE expires_at < NOW()", passwordResetTable);

        try(var statement = DBHelper.getStatement(getParentZone().getDBManager(), sql))
        {
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            trace("Error clearing expired tokens");
        }
    }
}
