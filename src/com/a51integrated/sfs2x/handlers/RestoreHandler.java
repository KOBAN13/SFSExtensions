package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.MailService;
import com.a51integrated.sfs2x.services.TokenService;
import com.a51integrated.sfs2x.services.UserService;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class RestoreHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject params)
    {
        var action = params.getUtfString(SFSResponseHelper.RESTORE_ACTION);
        ISFSObject result = SFSObject.newInstance();

        result.putUtfString(SFSResponseHelper.RESTORE_ACTION, action);

        try
        {
            var db = getParentExtension().getParentZone().getDBManager();

            var config = getParentExtension().getConfigProperties();

            var userTable = config.getProperty("db.table.users");
            var passwordResetTable = config.getProperty("db.table.reset_tokens");

            var tokenLength = Integer.parseInt(config.getProperty("reset.token.length"));
            var tokenTtlMinutes = Integer.parseInt(config.getProperty("reset.token.ttl.minutes"));
            var resetPasswordUrl = config.getProperty("reset.resetUrlBase");

            var userService = new UserService(db, userTable);
            var tokenService = new TokenService(db, passwordResetTable, tokenLength, tokenTtlMinutes);

            if ("start".equals(action))
            {
                var email = params.getUtfString(SFSResponseHelper.USER_EMAIL);

                if (email == null || !email.contains("@"))
                    throw new IllegalArgumentException("Email is empty or invalid email");

                var optionalUser = userService.findByEmail(email);

                if (optionalUser.isEmpty())
                {
                    result.putBool(SFSResponseHelper.OK,false);
                    result.putUtfString(SFSResponseHelper.ERROR, "Dont find email in register users");
                    return;
                }

                var id = optionalUser.get().id;
                var token = tokenService.issueToken(id);

                var host = config.getProperty("mail.host");
                var port = Integer.parseInt(config.getProperty("mail.port"));
                var tls = Boolean.parseBoolean(config.getProperty("mail.tls"));
                var username = config.getProperty("mail.username");
                var password = config.getProperty("mail.password");
                var from = config.getProperty("mail.from");

                var mailService = new MailService(host, port, tls, username, password, from);

                var link = resetPasswordUrl + token;
                var htmlPage = "<p>Для смены пароля перейдите по ссылке:</p><p><a href=\"" + link + "\">" + link + "</a></p>"
                        + "<p>Срок действия ссылки: " + tokenTtlMinutes + " минут.</p>";

                mailService.send(email, "Восстановление пароля", htmlPage);
            }
            else if ("confirm".equals(action))
            {
                var token = params.getUtfString("token");
                var newPassword = params.getUtfString("newPassword");

                if (token == null || token.length() < 8)
                    throw new IllegalArgumentException("Bad token");

                if (newPassword == null || newPassword.length() < 6)
                    throw new IllegalArgumentException("Password is empty or too short");

                var optionalIdUser = tokenService.consumeToken(token);

                if (optionalIdUser.isEmpty())
                {
                    result.putBool(SFSResponseHelper.OK, false);
                    result.putUtfString(SFSResponseHelper.ERROR, "Token invalid or expired");
                }
                else
                {
                    var hash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
                    userService.updatePassword(optionalIdUser.get(), hash);
                    result.putBool(SFSResponseHelper.OK, true);
                }
            }
            else
            {
                throw new IllegalArgumentException("Unknown action: " + action);
            }
        }

        catch (IllegalArgumentException iaeException)
        {
            result.putBool(SFSResponseHelper.OK, false);
            result.putUtfString(SFSResponseHelper.ERROR, iaeException.getMessage());
        }
        catch (SQLException sqlException)
        {
            result.putBool(SFSResponseHelper.OK, false);
            result.putUtfString(SFSResponseHelper.ERROR, sqlException.getMessage());
            trace(sqlException);
        }
        catch (Exception exception)
        {
            result.putBool(SFSResponseHelper.OK, false);
            result.putUtfString(SFSResponseHelper.ERROR, "Internal error");
            trace(exception);
        }

        send(SFSResponseHelper.RESTORE_RESULT, result, user);
    }
}
