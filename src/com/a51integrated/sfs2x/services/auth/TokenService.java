package com.a51integrated.sfs2x.services.auth;

import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.exceptions.SFSException;

import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;

public class TokenService
{
    private final IDBManager dbManager;
    private final String passwordResetTable;
    private final int tokenLength;
    private final int ttlMinutes;

    private final SecureRandom random = new SecureRandom();

    public TokenService(IDBManager dbManager, String passwordResetTable, int tokenLength, int ttlMinutes)
    {
        this.dbManager = dbManager;
        this.passwordResetTable = passwordResetTable;
        this.tokenLength = tokenLength;
        this.ttlMinutes = ttlMinutes;
    }

    public String issueToken(long userId) throws SFSException, SQLException
    {
        var token = randomUrlToken(tokenLength);

        var sqlRequest = String.format("INSERT INTO %s (user_id, token, expires_at) VALUES (?,?, NOW() + (? || ' minutes')::INTERVAL)", passwordResetTable);

        try (var connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlRequest))
        {
            statement.setLong(1, userId);
            statement.setString(2, token);
            statement.setInt(3, ttlMinutes);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new SQLException(e.getMessage());
        }

        return token;
    }

    public Optional<Long> consumeToken(String token) throws SFSException, SQLException
    {
        var sqlSelectRequest = String.format("SELECT user_id FROM %s WHERE token = ? AND expires_at > NOW()", passwordResetTable);
        var sqlRemoveRequest = String.format("DELETE FROM %s WHERE token = ?", passwordResetTable);

        try (var connection = dbManager.getConnection())
        {
            connection.setAutoCommit(false);

            try (var stmt = connection.prepareStatement(sqlSelectRequest))
            {
                stmt.setString(1, token);
                var result = stmt.executeQuery();

                if (!result.next())
                {
                    connection.rollback();
                    stmt.close();
                    return Optional.empty();
                }

                var userId = result.getLong(1);

                try (var deleteStmt = connection.prepareStatement(sqlRemoveRequest))
                {
                    deleteStmt.setString(1, token);
                    deleteStmt.executeUpdate();
                }

                connection.commit();
                return Optional.of(userId);
            }

            catch (SQLException e)
            {
                connection.rollback();
                throw new SQLException("Error consuming token");
            }

            finally
            {
                connection.setAutoCommit(true);
            }
        }
    }

    private String randomUrlToken(int tokenLength)
    {
        var arrayByte = new byte[tokenLength];
        random.nextBytes(arrayByte);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(arrayByte);
    }
}
