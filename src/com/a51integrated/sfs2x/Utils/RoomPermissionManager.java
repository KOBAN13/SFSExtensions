package com.a51integrated.sfs2x.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RoomPermissionManager
{
    private static final Map<ERoomRole, Set<RoomAction>> permisions = new HashMap<>();

    static
    {
        permisions.put(ERoomRole.OWNER, Set.of(RoomAction.CREATE, RoomAction.DELETE, RoomAction.UPDATE, RoomAction.KICK_USER));
        permisions.put(ERoomRole.PLAYER, Set.of());
    }

    public static boolean hasPermission(ERoomRole role, RoomAction action)
    {
        return permisions.getOrDefault(role, Set.of()).contains(action);
    }
}
