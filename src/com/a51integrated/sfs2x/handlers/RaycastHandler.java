package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.data.RaycastHit;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.CollisionMapService;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import org.joml.Vector3f;

public class RaycastHandler extends BaseClientRequestHandler
{
    private final CollisionMapService collisionMapService;
    private final float MAX_DISTANCE = 200f;

    public RaycastHandler(CollisionMapService collisionMapService) {
        this.collisionMapService = collisionMapService;
    }

    @Override
    public void handleClientRequest(User sender, ISFSObject object)
    {
        var result = SFSObject.newInstance();

        var originData = object.getSFSArray("originVector");
        var directionData = object.getSFSArray("directionVector");

        if (originData.size() < 3 || directionData.size() < 3)
        {
            sendError(result, "Origin or Direction Vector Size Mismatch", sender);
            return;
        }

        var ox = originData.getFloat(0);
        var oy = originData.getFloat(1);
        var oz = originData.getFloat(2);

        var dx = directionData.getFloat(0);
        var dy = directionData.getFloat(1);
        var dz = directionData.getFloat(2);

        var distance = object.containsKey("distance") ? object.getFloat("distance") : 50f;
        var layerMask = object.containsKey("layerMask") ? object.getInt("layerMask") : -1;

        if (distance > MAX_DISTANCE)
            distance = MAX_DISTANCE;

        if (distance < 0)
            distance = 0;

        var raycastService = collisionMapService.getRaycastService();

        var originVector = new Vector3f(ox, oy, oz);
        var directionVector = new Vector3f(dx, dy, dz);

        var raycastHit = raycastService.raycast(originVector, directionVector, distance, layerMask);

        sendSuccess(result, raycastHit, sender);
    }

    private void sendError(SFSObject resultObject, String message, User sender)
    {
        resultObject.putUtfString(SFSResponseHelper.ERROR, message);
        resultObject.putBool(SFSResponseHelper.OK, false);
        send(SFSResponseHelper.RAYCAST, resultObject, sender);
    }

    private void sendSuccess(SFSObject resultObject, RaycastHit raycastHit, User targetUser)
    {
        resultObject.putBool(SFSResponseHelper.OK, true);

        resultObject.putBool("hit", raycastHit.getHit());
        resultObject.putFloat("distance", raycastHit.getDistance());

        var pointVector = raycastHit.getPoint();
        resultObject.putFloat("xPoint", pointVector.x);
        resultObject.putFloat("yPoint", pointVector.y);
        resultObject.putFloat("zPoint", pointVector.z);

        send(SFSResponseHelper.RAYCAST, resultObject, targetUser);
    }
}
