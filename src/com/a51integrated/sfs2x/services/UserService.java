package com.a51integrated.sfs2x.services;

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
        var sqlRequest = String.format("SELECT id, username, password_hash FROM %s WHERE username = ?", tableUser);

        try(var connection = dbManager.getConnection(); var stmt = connection.prepareStatement(sqlRequest))
        {
            stmt.setString(1, username);
            var rs = stmt.executeQuery();

            if (rs.next())
                return Optional.of(map(rs));

            return Optional.empty();
        }
        catch (SQLException e)
        {
            throw new SQLException("Connection to the database failed dont find user by name " + username);
        }
    }

    public Optional<GameUser> findByEmail(String email) throws SFSException, SQLException
    {
        var sqlRequest = String.format("SELECT id, username, password_hash FROM %s WHERE email = ?", tableUser);

        try(var connection = dbManager.getConnection(); var stmt = connection.prepareStatement(sqlRequest))
        {
            stmt.setString(1, email);
            var rs = stmt.executeQuery();

            if (rs.next())
                return Optional.of(map(rs));

            return Optional.empty();
        }
        catch (SQLException e)
        {
            throw new SQLException("Connection to the database failed dont find user by email " + email);
        }
    }

    public long createUser(String username, String email, String password_hash) throws SFSException, SQLException
    {
        var sqlRequest = String.format("INSERT INTO %s (username, email, password_hash, created_at)", tableUser);

        try(var connection = dbManager.getConnection(); var stmt = connection.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS))
        {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password_hash);
            stmt.executeUpdate();

            var generatedKey = stmt.getGeneratedKeys();

            if (generatedKey.next())
                return generatedKey.getLong(1);

            throw new SFSException("Failed to create user");
        }
        catch (SQLException e)
        {
            throw new SQLException("Error create user");
        }
    }

    public void updatePassword(long used_id, String newPasswordHash) throws SFSException, SQLException
    {
        var sqlRequest = String.format("UPDATE %s SET password_hash = ? WHERE id = ?", tableUser);

        try (var connection = dbManager.getConnection(); var stmt = connection.prepareStatement(sqlRequest))
        {
            stmt.setString(1, newPasswordHash);
            stmt.setLong(2, used_id);
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new SQLException("Error create user");
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
