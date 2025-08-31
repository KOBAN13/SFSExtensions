package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.services.UserService;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject sfsObject)
    {
        var username = sfsObject.getUtfString("username");
        var email = sfsObject.getUtfString("email");
        var password = sfsObject.getUtfString("password");

        ISFSObject result = SFSObject.newInstance();
        result.putUtfString("cmd", "registerResult");

        try
        {
            if (username == null || username.length() < 3)
                throw new IllegalArgumentException("Username is empty or too short");

            if (password == null || password.length() < 6)
                throw new IllegalArgumentException("Password is empty or too short");

            if (email == null || !email.contains("@"))
                throw new IllegalArgumentException("Email is empty or invalid email");

            var dbData = getParentExtension().getParentZone().getDBManager();
            var table = getParentExtension().getConfigProperties().getProperty("db.table.users", "game_users");

            var userService = new UserService(dbData, table);

            if (userService.findByName(username).isPresent())
                throw new IllegalArgumentException("Username already exists");

            if (userService.findByEmail(email).isPresent())
                throw new IllegalArgumentException("Email already exists");

            var passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
            var userId = userService.createUser(username, email, passwordHash);

            result.putBool("ok", true);
            result.putLong("userId", userId);

            //Надо добавить в users service отдачу эксепшенов
        }
        catch (IllegalArgumentException iaeException)
        {
            result.putBool("ok", false);
            result.putUtfString("error", iaeException.getMessage());
        }
        catch (Exception exception)
        {
            result.putBool("ok", false);
            result.putUtfString("error", "Internal error");
            trace(exception);
        }

        send("registerResult", result, user);
    }
}
