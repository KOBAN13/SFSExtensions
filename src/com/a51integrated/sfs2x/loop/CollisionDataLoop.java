package com.a51integrated.sfs2x.loop;

import com.a51integrated.sfs2x.extensions.GameExtension;
import com.a51integrated.sfs2x.data.collision.ServerCollisionData;
import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.a51integrated.sfs2x.services.collision.CollisionMapService;
import com.a51integrated.sfs2x.services.room.RoomStateService;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class CollisionDataLoop implements Runnable {

    private final GameExtension game;
    private final CollisionMapService collisionMapService;
    private final RoomStateService  roomStateService;

    public CollisionDataLoop(GameExtension game, CollisionMapService collisionMapService, RoomStateService roomStateService) {
        this.game = game;
        this.collisionMapService = collisionMapService;
        this.roomStateService = roomStateService;
    }

    @Override
    public void run()
    {
        var room = roomStateService.getRoom();
        var users = room.getPlayersList();

        for (var user : users)
        {
            var array = new SFSArray();
            var result = new SFSObject();

            for (var shape: collisionMapService.getShapes())
            {
                var serverCollision = ServerCollisionData.fromShape(shape);
                var sfsObject = serverCollision.toSfs();

                array.addSFSObject(sfsObject);
            }

            for (var shape: collisionMapService.getPlayerShapes())
            {
                var serverCollision = ServerCollisionData.fromShape(shape);
                var sfsObject = serverCollision.toSfs();

                array.addSFSObject(sfsObject);
            }

            result.putSFSArray("shapesData", array);

            game.send(SFSResponseHelper.COLLISION_DATA, result, user);
        }
    }
}
