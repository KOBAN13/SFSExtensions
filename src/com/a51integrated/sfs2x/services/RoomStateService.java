package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.data.PlayerState;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import java.util.concurrent.ConcurrentHashMap;

public class RoomStateService
{
    private final ConcurrentHashMap<Integer, PlayerState> players = new ConcurrentHashMap<>();
    private final Room room;

    public RoomStateService(Room room)
    {
        this.room = room;
    }

    public PlayerState get(User user)
    {
        return players.computeIfAbsent(user.getId(), PlayerState::new);
    }

    public void remove(User user)
    {
        players.remove(user.getId());
    }

    public ISFSObject toSFSObject()
    {
        var sfs = new SFSObject();
        var sfsArray = new SFSArray();

        for (var playerState : players.values())
        {
            var item = new SFSObject();
            item.putInt("id", playerState.id);
            item.putFloat("x", playerState.x);
            item.putFloat("y", playerState.y);
            item.putFloat("z", playerState.z);
            item.putUtfString("animationState", playerState.animationState);

            sfsArray.addSFSObject(item);
        }

        sfs.putSFSArray("players", sfsArray);
        return sfs;
    }

    public Room getRoom()
    {
        return room;
    }
}
