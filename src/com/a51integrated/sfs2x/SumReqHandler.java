package com.a51integrated.sfs2x;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class SumReqHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject)
    {
        var n1 = isfsObject.getInt("n1");
        var n2 = isfsObject.getInt("n2");

        ISFSObject object = new SFSObject();
        object.putInt("sum", n1 + n2);

        TestExtension sumReqHandler = (TestExtension) getParentExtension();

        sumReqHandler.send("math", object, user);
    }
}
