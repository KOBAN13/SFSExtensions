package com.a51integrated.sfs2x.services.auth;

import com.a51integrated.sfs2x.models.GameUser;
import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.exceptions.SFSException;

import java.sql.*;
import java.util.Optional;

public class UserService
{
    private final IDBManager dbManager;
    private final String tableUser;

    public UserService(IDBManager dbManager, String tableUser)
    {
        this.dbManager = dbManager;
        this.tableUser = tableUser;
    }

    public Optional<GameUser> findByName(String username) throws SFSException, SQLException
    {
        var sqlRequest = String.format("SELECT id, username, email, password_hash FROM %s WHERE username = ?", tableUser);

        return findUserByDetails(sqlRequest, username);
    }

    public Optional<GameUser> findByEmail(String email) throws SFSException, SQLException
    {
        var sqlRequest = String.format("SELECT id, username, email, password_hash FROM %s WHERE email = ?", tableUser);

        return findUserByDetails(sqlRequest, email);
    }

    public long createUser(String username, String email, String password_hash) throws SFSException, SQLException
    {
        var sqlRequest = String.format("INSERT INTO %s (username, email, password_hash) VALUES (?, ?, ?)", tableUser);

        try(var connection = dbManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS))
        {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password_hash);
            statement.executeUpdate();

            var generatedKey = statement.getGeneratedKeys();

            if (generatedKey.next())
                return generatedKey.getLong(1);

            throw new SFSException("Failed to create user");
        }
        catch (SQLException e)
        {
            throw new SQLException(e.getMessage());
        }
    }

    public void updatePassword(long used_id, String newPasswordHash) throws SFSException, SQLException
    {
        var sqlRequest = String.format("UPDATE %s SET password_hash = ? WHERE id = ?", tableUser);

        try(var connection = dbManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlRequest))
        {
            statement.setString(1, newPasswordHash);
            statement.setLong(2, used_id);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new SQLException("Error update password ; " + e.getMessage());
        }
    }

    private Optional<GameUser> findUserByDetails(String sqlRequest, String details) throws SFSException, SQLException
    {
        try(var connection = dbManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlRequest))
        {
            statement.setString(1, details);
            var rs = statement.executeQuery();

            if (rs.next())
                return Optional.of(map(rs));

            return Optional.empty();
        }
        catch (SQLException e)
        {
            throw new SQLException(e.getMessage());
        }
    }

    private GameUser map(ResultSet resultSet) throws SQLException
    {
        return new GameUser(
                resultSet.getLong("id"),
                resultSet.getString("username"),
                resultSet.getString("email"),
                resultSet.getString("password_hash")
        );
    }
}
