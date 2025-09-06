package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.models.GameUser;
import com.a51integrated.sfs2x.services.UserService;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
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
        var clientUser = (User) event.getParameter(SFSEventParam.USER);

        ISFSObject result = SFSObject.newInstance();

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
            result.putBool(SFSResponseHelper.OK, false);
            result.putUtfString(SFSResponseHelper.ERROR, sqlException.getMessage());
            trace(sqlException);
            return;
        }

        if (optionalUser.isEmpty() || !BCrypt.checkpw(password, optionalUser.get().password_hash))
        {
            SFSErrorData errorData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);

            errorData.addParameter("Invalid username or password");

            throw new SFSLoginException("Invalid credentials", errorData);
        }

        var user = optionalUser.get();

        result.putLong(SFSResponseHelper.USER_ID, user.id);
        result.putUtfString(SFSResponseHelper.USER_NAME, user.name);
        result.putUtfString(SFSResponseHelper.USER_EMAIL, user.email);

        send(SFSResponseHelper.LOGIN_RESULT, result, clientUser);
    }
}
