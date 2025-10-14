package com.a51integrated.sfs2x.handlers;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class ChangeUserPrivilege extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject)
    {
        var userPrivilege = isfsObject.getShort("privilege");

        trace("userPrivilege: " + userPrivilege);

        user.setPrivilegeId(userPrivilege);
    }
}
