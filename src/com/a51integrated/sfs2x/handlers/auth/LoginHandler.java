package com.a51integrated.sfs2x.handlers.auth;

import com.a51integrated.sfs2x.helpers.DBHelper;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.models.GameUser;
import com.a51integrated.sfs2x.services.auth.UserService;
import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.api.ISFSApi;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import com.smartfoxserver.v2.security.DefaultPermissionProfile;

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
        var session = (ISession) event.getParameter(SFSEventParam.SESSION);

        var parentExtension = getParentExtension();

        var dbData = DBHelper.getDb(parentExtension);
        var table = DBHelper.getProperty(parentExtension,"db.table.users");

        var gameUser = getGameUser(dbData, table, username);
        checkPassword(gameUser, getApi(), session, password, username);

        session.setProperty("$permission", DefaultPermissionProfile.STANDARD);

        resultLogin.putLong(SFSResponseHelper.USER_ID, gameUser.id);
        resultLogin.putUtfString(SFSResponseHelper.USER_NAME, gameUser.name);
        resultLogin.putUtfString(SFSResponseHelper.USER_EMAIL, gameUser.email);
    }

    private static void checkPassword(GameUser gameUser, ISFSApi sfsApi, ISession session, String password, String username)
            throws SFSException
    {
        if (!sfsApi.checkSecurePassword(session, gameUser.password_hash, password))
        {
            var errorMessage = String.format("incorrect password for the user %s", username);

            var error = new SFSErrorData(SFSErrorCode.LOGIN_BAD_PASSWORD);
            error.addParameter(errorMessage);

            throw new SFSLoginException("Login failed", error);
        }
    }

    private static GameUser getGameUser(IDBManager dbData, String table, String username) throws SFSException
    {
        var users = new UserService(dbData, table);

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

        return optionalUser.get();
    }
}
