package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.helpers.DBHelper;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.UserService;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

import java.sql.SQLException;

public class RegisterHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject sfsObject)
    {
        var username = sfsObject.getUtfString(SFSResponseHelper.USER_NAME);
        var email = sfsObject.getUtfString(SFSResponseHelper.USER_EMAIL);
        var password = sfsObject.getUtfString(SFSResponseHelper.PASSWORD);

        ISFSObject result = SFSObject.newInstance();

        result.putUtfString(SFSResponseHelper.CMD, SFSResponseHelper.REGISTER_RESULT);

        try
        {
            validateInputData(username, password, email);

            var parentExtension = getParentExtension();

            var dbData = DBHelper.getDb(parentExtension);
            var table = DBHelper.getProperty(parentExtension, "db.table.users");

            var userService = new UserService(dbData, table);

            checkUserUniqueness(userService, username, email);

            var userId = userService.createUser(username, email, password);

            sendSuccess(result, user, userId);

            send(SFSResponseHelper.REGISTER_RESULT, result, user);
        }
        catch (IllegalArgumentException iaeException)
        {
            sendError(result, user, iaeException.getMessage());
            trace(iaeException.getMessage());
        }
        catch (SQLException sqlException)
        {
            sendError(result, user, sqlException.getMessage());
            trace(sqlException + "Error in registering user");
        }
        catch (Exception exception)
        {
            sendError(result, user, exception.getMessage());
            trace(exception);
        }
    }

    private void validateInputData(String username, String password, String email)
    {
        if (username == null || username.length() < 3)
            throw new IllegalArgumentException("Username is empty or too short");

        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("Password is empty or too short");

        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Email is empty or invalid email");
    }

    private void checkUserUniqueness(UserService userService, String username, String email) throws SQLException, SFSException
    {
        if (userService.findByName(username).isPresent())
            throw new IllegalArgumentException("Username already exists");

        if (userService.findByEmail(email).isPresent())
            throw new IllegalArgumentException("Email already exists");
    }

    private void sendSuccess(ISFSObject result, User user, long userId)
    {
        result.putBool(SFSResponseHelper.OK, true);
        result.putLong(SFSResponseHelper.USER_ID, userId);

        send(SFSResponseHelper.REGISTER_RESULT, result, user);
    }

    private void sendError(ISFSObject result, User user, String error)
    {
        result.putBool(SFSResponseHelper.OK, false);
        result.putUtfString(SFSResponseHelper.ERROR, error);

        send(SFSResponseHelper.REGISTER_RESULT, result, user);
    }
}
