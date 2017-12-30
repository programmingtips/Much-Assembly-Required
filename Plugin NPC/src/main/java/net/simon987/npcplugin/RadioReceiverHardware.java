package net.simon987.npcplugin;

import net.simon987.server.GameServer;
import net.simon987.server.assembly.CpuHardware;
import net.simon987.server.assembly.Status;
import net.simon987.server.assembly.Util;
import net.simon987.server.game.Action;
import net.simon987.server.game.ControllableUnit;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class RadioReceiverHardware extends CpuHardware {

    public static final char HWID = 0xC; //12

    private static final int LISTEN = 1;

    public static final int DEFAULT_ADDRESS = 0xC;

    private ControllableUnit cubot;

    public RadioReceiverHardware(ControllableUnit cubot) {
        this.cubot = cubot;
    }

    @Override
    public void handleInterrupt(Status status) {
        int x = getCpu().getRegisterSet().getRegister("X").getValue();
        int a = getCpu().getRegisterSet().getRegister("A").getValue();

        if (a == LISTEN) {

            //Find the nearest Radio Tower and query it
            cubot.setAction(Action.LISTENING);

            ArrayList<char[]> messages = new ArrayList<>(6);

            ArrayList<RadioTower> towers = new ArrayList<>(NpcPlugin.getRadioTowers()); //Avoid ConcurrentModificationException
            for (RadioTower tower : towers) {
                if (Util.manhattanDist(tower.getX(), tower.getY(), cubot.getX(), cubot.getY()) <= RadioTower.MAX_RANGE) {
                    //Tower is in range
                    messages.addAll(tower.getMessages());
                }
            }

            //Write messages to memory
            int offset = 0;

            for (char[] message : messages) {

                getCpu().getMemory().write(x + offset, message, 0, message.length);
                offset += message.length;
            }

            //Write the amount of messages received to B
            getCpu().getRegisterSet().getRegister("B").setValue(messages.size());
        }
    }

    @Override
    public char getId() {
        return HWID;
    }

    @Override
    public JSONObject serialise() {
        JSONObject json = new JSONObject();
        json.put("hwid", (int) HWID);
        json.put("cubot", cubot.getObjectId());

        return json;
    }

    public static RadioReceiverHardware deserialize(JSONObject json) {
        return new RadioReceiverHardware((ControllableUnit) GameServer.INSTANCE.getGameUniverse().getObject((int) (long) json.get("cubot")));
    }
}