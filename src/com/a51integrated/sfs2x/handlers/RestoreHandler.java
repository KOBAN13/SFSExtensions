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

        var toEmail = params.getUtfString(SFSResponseHelper.USER_EMAIL);

        result.putUtfString(SFSResponseHelper.RESTORE_ACTION, action);
        result.putUtfString(SFSResponseHelper.CMD, SFSResponseHelper.REGISTER_RESULT);

        try
        {
            var db = getParentExtension().getParentZone().getDBManager();
            var config = getParentExtension().getConfigProperties();

            var userTable = config.getProperty("db.table.users");

            var passwordResetTable = config.getProperty("db.table.reset_tokens");

            var tokenLengthString = config.getProperty("reset.token.length");

            var tokenLength = Integer.parseInt(config.getProperty("reset.token.length"));
            var tokenTtlMinutes = Integer.parseInt(config.getProperty("reset.token.ttl.minutes"));
            var resetPasswordUrl = config.getProperty("reset.resetUrlBase");

            var userService = new UserService(db, userTable);
            var tokenService = new TokenService(db, passwordResetTable, tokenLength, tokenTtlMinutes);

            if ("start".equals(action))
            {
                if (toEmail == null || !toEmail.contains("@"))
                {
                    trace("Email no valid");
                    throw new IllegalArgumentException("Email is empty or invalid email");
                }

                var optionalUser = userService.findByEmail(toEmail);

                if (optionalUser.isEmpty())
                {
                    result.putBool(SFSResponseHelper.OK,false);
                    result.putUtfString(SFSResponseHelper.ERROR, "Don't find email in register users");

                    send(SFSResponseHelper.RESTORE_RESULT, result, user);

                    trace("Email no find");
                    return;
                }

                var id = optionalUser.get().id;
                var token = tokenService.issueToken(id);

                var emailFrom = config.getProperty("mail.from");
                var apiKey = config.getProperty("mail.api.key");
                var apiUrl = config.getProperty("mail.api.url");

                trace(emailFrom);
                trace(apiKey);
                trace(apiUrl);

                var mailService = new MailService(apiKey, apiUrl);

                var link = resetPasswordUrl + token;

                var htmlPage = "<p>Для смены пароля перейдите по ссылке:</p><p><a href=\"" + link + "\">" + link + "</a></p>"
                        + "<p>Срок действия ссылки: " + tokenTtlMinutes + " минут.</p>";


                trace("Email send");

                var message = mailService.send(emailFrom, toEmail, "Восстановление пароля", htmlPage);
                trace(message);
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
            trace("IllegalArgumentException: " + iaeException.getMessage());

            result.putBool(SFSResponseHelper.OK, false);
            result.putUtfString(SFSResponseHelper.ERROR, iaeException.getMessage());
            send(SFSResponseHelper.RESTORE_RESULT, result, user);
        }

        catch (SQLException sqlException)
        {
            result.putBool(SFSResponseHelper.OK, false);
            result.putUtfString(SFSResponseHelper.ERROR, sqlException.getMessage());

            trace("SQL" + sqlException.getMessage());

            send(SFSResponseHelper.RESTORE_RESULT, result, user);
        }
        catch (Exception exception)
        {
            result.putBool(SFSResponseHelper.OK, false);
            result.putUtfString(SFSResponseHelper.ERROR, "Internal error");

            trace("expetion" + exception.getMessage());

            send(SFSResponseHelper.RESTORE_RESULT, result, user);
        }

        send(SFSResponseHelper.RESTORE_RESULT, result, user);
    }
}
