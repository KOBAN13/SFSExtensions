package com.a51integrated.sfs2x.handlers;

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

public class LoginEventHandler extends BaseServerEventHandler
{
    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException
    {
        var username = (String) event.getParameter(SFSEventParam.LOGIN_NAME);
        var password = (String) event.getParameter(SFSEventParam.LOGIN_PASSWORD);
        var clientUser = (User) event.getParameter(SFSEventParam.USER);

        var parentExtension = getParentExtension();

        var dataBase = parentExtension.getParentZone().getDBManager();

        var users = new UserService(dataBase, parentExtension.getConfigProperties().getProperty("db.table.users", "game_users"));

        var optionalUser = users.findByName(username);

        if (optionalUser.isEmpty() || !BCrypt.checkpw(password, optionalUser.get().password_hash))
        {
            SFSErrorData errorData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);

            errorData.addParameter("Invalid username or password");

            throw new SFSLoginException("Invalid credentials", errorData);
        }

        var user = optionalUser.get();

        ISFSObject outData = SFSObject.newInstance();

        outData.putLong("userId", user.id);
        outData.putUtfString("username", user.name);
        outData.putUtfString("email", user.email);

        //В ТЕОРИИ ЭТОТ КОД РАБОТАТЬ НЕ БУДЕТ
        send(SFSEventParam.LOGIN_OUT_DATA.name(), outData, clientUser);
    }
}
