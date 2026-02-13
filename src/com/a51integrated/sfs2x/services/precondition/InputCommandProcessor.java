package com.a51integrated.sfs2x.services.precondition;

import com.a51integrated.sfs2x.data.state.InputFrame;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentHashMap;

public class InputCommandProcessor
{
    private final ConcurrentHashMap<Integer, ConcurrentSkipListMap<Long, InputFrame>> inputCommands = new ConcurrentHashMap<>();

    public void AddInputFrame(int userId, InputFrame inputFrame)
    {
        var buffer = inputCommands.computeIfAbsent(userId, k -> new ConcurrentSkipListMap<>());

        buffer.putIfAbsent(inputFrame.inputTick, inputFrame);
    }

    public InputFrame pollNext(int userId)
    {
        var buffer = inputCommands.get(userId);

        if (buffer == null || buffer.isEmpty())
            return null;

        var entry = buffer.pollFirstEntry();

        return entry == null ? null : entry.getValue();
    }

    public void removePlayer(int userId)
    {
        inputCommands.remove(userId);
    }
}

