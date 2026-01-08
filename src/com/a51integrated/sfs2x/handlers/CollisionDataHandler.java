package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.data.ServerCollisionData;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.CollisionMapService;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class CollisionDataHandler extends BaseClientRequestHandler {

    private final CollisionMapService collisionMapService;

    public CollisionDataHandler(CollisionMapService collisionMapService) {
        this.collisionMapService = collisionMapService;
    }

    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {

        var array = new SFSArray();
        var result = new SFSObject();

        for (var shape: collisionMapService.getShapes())
        {
            var serverCollision = ServerCollisionData.fromShape(shape);
            var sfsObject = serverCollision.toSfs();

            array.addSFSObject(sfsObject);
        }

        result.putSFSArray("shapesData", array);

        send(SFSResponseHelper.COLLISION_DATA, result, user);
    }
}
