package com.a51integrated.sfs2x.handlers;

import com.a51integrated.sfs2x.helpers.SFSResponseHelper;
import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.SFSRoomRemoveMode;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.exceptions.SFSCreateRoomException;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import koban.roomModule.RoleService;

import java.util.List;

public class CreateGameHandler extends BaseClientRequestHandler
{
    private final String ROOM_GROUP_NAME = "Game";

    @Override
    public void handleClientRequest(User sender, ISFSObject isfsObject)
    {
        final var lobbyRoom = sender.getLastJoinedRoom();
        final var users = lobbyRoom.getUserList();
        final var result = new SFSObject();

        if (RoleService.isOwner(lobbyRoom, sender))
        {
            CreateRoom(lobbyRoom, result, users, sender);
        }
        else
        {
            JoinToGameRoom(sender, lobbyRoom, result);
        }
    }

    private void JoinToGameRoom(User sender, Room lobbyRoom, ISFSObject result)
    {
        var gameRoomId = lobbyRoom.getVariable("gameRoomId").getIntValue();

        var gameRoom = getParentExtension().getParentZone().getRoomById(gameRoomId);

        if(gameRoom.containsUser(sender))
        {
            trace("User " + sender.getName() + " joined the game");
            return;
        }

        try
        {
            getApi().joinRoom(sender, gameRoom, gameRoom.getPassword(), false, lobbyRoom, true, true);
        }
        catch (SFSJoinRoomException exception)
        {
            trace("Error creating or joining room", exception);

            result.putBool(SFSResponseHelper.OK, false);
            send(SFSResponseHelper.CREATE_GAME_ROOM, result, sender);
        }
    }

    private void CreateRoom(Room lobbyRoom, SFSObject result, List<User> users, User sender)
    {
        try
        {
            final var roomName = generateRoomName(lobbyRoom);

            final var settings = createGameRoomSettings(lobbyRoom, roomName);
            final var gameRoom = getApi().createRoom(getParentExtension().getParentZone(), settings, sender);

            var lobbyRoomVariable = new SFSRoomVariable("gameRoomId", gameRoom.getId());
            lobbyRoomVariable.setPrivate(true);
            lobbyRoomVariable.setGlobal(false);
            lobbyRoomVariable.setPersistent(true);

            lobbyRoom.setVariable(lobbyRoomVariable);

            for (User user : users)
            {
                getApi().joinRoom(user, gameRoom, gameRoom.getPassword(), false, lobbyRoom, true, true);
            }

            result.putBool(SFSResponseHelper.OK, true);
            send(SFSResponseHelper.CREATE_GAME_ROOM, result, users);

        }
        catch (SFSCreateRoomException | SFSJoinRoomException | SFSVariableException exception)
        {
            trace("Error creating or joining room", exception);

            result.putBool(SFSResponseHelper.OK, false);
            send(SFSResponseHelper.CREATE_GAME_ROOM, result, users);
        }
    }

    private String generateRoomName(Room lobbyRoom)
    {
        final var base = String.valueOf(lobbyRoom.getId());
        final var hash = Integer.toHexString((int)(System.currentTimeMillis() & 0xFFFF));
        final var combined = base + hash;

        return combined.substring(0, Math.min(10, combined.length()));
    }

    private CreateRoomSettings createGameRoomSettings(Room lobbyRoom, String roomName)
    {
        var settings = new CreateRoomSettings();

        settings.setHidden(true);
        settings.setName(roomName);
        settings.setGame(true);
        settings.setMaxUsers(lobbyRoom.getMaxUsers());
        settings.setDynamic(true);
        settings.setGroupId(ROOM_GROUP_NAME);
        settings.setAutoRemoveMode(SFSRoomRemoveMode.WHEN_EMPTY_AND_CREATOR_IS_GONE);

        var lobbyPassword = lobbyRoom.getPassword();

        if (!lobbyPassword.isEmpty()) {
            settings.setPassword(lobbyPassword);
        }

        settings.setExtension(new CreateRoomSettings.RoomExtensionSettings(
                "ServerExtensions",
                "com.a51integrated.sfs2x.GameExtension"
        ));

        return settings;
    }
}
