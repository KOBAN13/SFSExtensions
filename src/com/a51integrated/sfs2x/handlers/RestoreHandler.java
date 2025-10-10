package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.helpers.DBHelper;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.MailService;
import com.a51integrated.sfs2x.services.TokenService;
import com.a51integrated.sfs2x.services.UserService;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.io.IOException;
import java.sql.SQLException;

public class RestoreHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject params)
    {
        ISFSObject result = createResultObject();

        try
        {
            var toEmail = params.getUtfString(SFSResponseHelper.USER_EMAIL);

            validateEmail(toEmail);

            var parentExtension = getParentExtension();

            var db = DBHelper.getDb(parentExtension);
            var userTable = DBHelper.getProperty(parentExtension, "db.table.users");
            var passwordResetTable = DBHelper.getProperty(parentExtension, "db.table.reset_tokens");
            var tokenLength = Integer.parseInt(DBHelper.getProperty(parentExtension, "reset.token.length"));
            var tokenTtlMinutes = Integer.parseInt(DBHelper.getProperty(parentExtension, "reset.token.ttl.minutes"));
            var resetPasswordUrl = DBHelper.getProperty(parentExtension, "reset.resetUrlBase");

            var userService = new UserService(db, userTable);
            var tokenService = new TokenService(db, passwordResetTable, tokenLength, tokenTtlMinutes);

            var optionalUser = userService.findByEmail(toEmail);

            if (optionalUser.isEmpty())
            {
                sendError(result, user, "Don't find email in register users");
                return;
            }

            var userId = optionalUser.get().id;
            var token = tokenService.issueToken(userId);
            var link = resetPasswordUrl + token;

            sendResetEmail(parentExtension, toEmail, link, tokenTtlMinutes);

            result.putBool(SFSResponseHelper.OK, true);
            result.putUtfString(SFSResponseHelper.USER_EMAIL, toEmail);
        }
        catch (IllegalArgumentException ex)
        {
            trace("Validation error: " + ex.getMessage());
            sendError(result, user, ex.getMessage());
            return;
        }
        catch (SQLException ex)
        {
            trace("Database error: " + ex.getMessage());
            sendError(result, user, "Database error");
            return;
        }
        catch (Exception ex)
        {
            trace("Unhandled error: " + ex.getMessage());
            sendError(result, user, "Internal error");
            return;
        }

        send(SFSResponseHelper.RESTORE_RESULT, result, user);
    }

    private ISFSObject createResultObject()
    {
        ISFSObject result = SFSObject.newInstance();
        result.putUtfString(SFSResponseHelper.CMD, SFSResponseHelper.REGISTER_RESULT);
        return result;
    }

    private void validateEmail(String email)
    {
        if (email == null || !email.contains("@"))
        {
            throw new IllegalArgumentException("Email is empty or invalid");
        }
    }

    private void sendResetEmail(
            SFSExtension parentExtension,
            String toEmail,
            String link,
            int ttlMinutes
    ) throws IOException, InterruptedException
    {
        var emailFrom = DBHelper.getProperty(parentExtension, "mail.from");
        var apiKey = DBHelper.getProperty(parentExtension, "mail.api.key");
        var apiUrl = DBHelper.getProperty(parentExtension, "mail.api.url");

        var mailService = new MailService(apiKey, apiUrl);
        var subject = "Восстановление пароля";

        var html = "<p>Для смены пароля перейдите по ссылке:</p>"
                + "<p><a href=\"" + link + "\">" + link + "</a></p>"
                + "<p>Срок действия ссылки: " + ttlMinutes + " минут.</p>";

        mailService.send(emailFrom, toEmail, subject, html);
    }

    private void sendError(ISFSObject result, User user, String message)
    {
        result.putBool(SFSResponseHelper.OK, false);
        result.putUtfString(SFSResponseHelper.ERROR, message);
        send(SFSResponseHelper.RESTORE_RESULT, result, user);
    }
}
