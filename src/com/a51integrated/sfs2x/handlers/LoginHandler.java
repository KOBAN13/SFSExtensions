package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.models.GameUser;
import com.a51integrated.sfs2x.services.UserService;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

public class LoginHandler extends BaseServerEventHandler
{
    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException
    {
        var username = (String) event.getParameter(SFSEventParam.LOGIN_NAME);
        var password = (String) event.getParameter(SFSEventParam.LOGIN_PASSWORD);
        var resultLogin = (ISFSObject) event.getParameter(SFSEventParam.LOGIN_OUT_DATA);

        var parentExtension = getParentExtension();

        var dataBase = parentExtension.getParentZone().getDBManager();

        var users = new UserService(dataBase, parentExtension.getConfigProperties().getProperty("db.table.users", "game_users"));

        Optional<GameUser> optionalUser;

        try
        {
            optionalUser = users.findByName(username);
        }
        catch (SQLException sqlException)
        {
            var error = new SFSErrorData(SFSErrorCode.GENERIC_ERROR);
            error.addParameter(sqlException.getMessage());

            throw new SFSLoginException("Login failed", error);
        }

        if (optionalUser.isEmpty())
        {
            var errorMessage = String.format("User %s not found", username);

            var error = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);
            error.addParameter(errorMessage);

            throw new SFSLoginException("Login failed", error);
        }

        if (!BCrypt.checkpw(password, optionalUser.get().password_hash))
        {
            var errorMessage = String.format("incorrect password for the user %s", username);

            var error = new SFSErrorData(SFSErrorCode.LOGIN_BAD_PASSWORD);
            error.addParameter(errorMessage);

            throw new SFSLoginException("Login failed", error);
        }

        var user = optionalUser.get();

        resultLogin.putBool(SFSResponseHelper.OK, true);
        resultLogin.putLong(SFSResponseHelper.USER_ID, user.id);
        resultLogin.putUtfString(SFSResponseHelper.USER_NAME, user.name);
        resultLogin.putUtfString(SFSResponseHelper.USER_EMAIL, user.email);
    }
}
