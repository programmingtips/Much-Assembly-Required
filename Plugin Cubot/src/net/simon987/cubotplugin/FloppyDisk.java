package net.simon987.cubotplugin;


import net.simon987.server.assembly.Memory;
import net.simon987.server.io.JSONSerialisable;
import net.simon987.server.logging.LogManager;
import org.json.simple.JSONObject;

/**
 * Represents a floppy disk that is inside a floppy drive.
 * Floppies contains 80 tracks with 18 sectors per track.
 * That's 1440 sectors of 512 words. (total 1,474,560 bytes / 737,280 words / 1.44Mb)
 */
public class FloppyDisk implements JSONSerialisable {

    /**
     * Contents of the disk
     */
    private Memory memory;

    /**
     * Current location of the read/write head.
     * Used to calculate seek time
     */
    private int rwHeadTrack = 0;


    public FloppyDisk() {
        this.memory = new Memory(1024 * 1440);
    }

    /**
     * Read 512 words from the specified sector to cpu memory at specified address
     *
     * @param sector     sector to read (0-1440)
     * @param cpuMemory  Cpu memory to write to
     * @param ramAddress address of the data to write in CPU memory
     * @return Whether or not the read operation was in the same track as the last r/w
     */
    public boolean readSector(int sector, Memory cpuMemory, int ramAddress) {

        cpuMemory.write(ramAddress, memory.getBytes(), sector * 512, 1024);

        LogManager.LOGGER.fine("Read 512 words from floppy sector:" + sector + " to memory addr:" + ramAddress);

        //Calculate seek time
        int deltaTrack = (sector / 80) - rwHeadTrack;

        if (deltaTrack != 0) {
            rwHeadTrack = (sector / 80);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Write 512 words to the specified sector from cpu memory at the specified address
     *
     * @param sector     sector to write (0-1440)
     * @param cpuMemory  Cpu memory to read from
     * @param ramAddress address of the data to read in CPU memory
     * @return Whether or not the read operation was in the same track as the last r/w
     */
    public boolean writeSector(int sector, Memory cpuMemory, int ramAddress) {

        memory.write(sector * 512, cpuMemory.getBytes(), ramAddress * 2, 1024);

        LogManager.LOGGER.fine("Wrote 512 words to floppy sector:" + sector + " from memory addr:" + ramAddress);

        //Calculate seek time
        int deltaTrack = (sector / 80) - rwHeadTrack;

        if (deltaTrack != 0) {
            rwHeadTrack = (sector / 80);
            return false;
        } else {
            return true;
        }
    }


    @Override
    public JSONObject serialise() {

        JSONObject json = new JSONObject();
        json.put("rwHeadTrack", rwHeadTrack);
        json.put("memory", memory.serialise());

        return json;
    }

    public static FloppyDisk deserialise(JSONObject json) {

        FloppyDisk floppyDisk = new FloppyDisk();

        floppyDisk.rwHeadTrack = (int) (long) json.get("rwHeadTrack");
        floppyDisk.memory = Memory.deserialize((JSONObject) json.get("memory"));

        return floppyDisk;
    }

    public Memory getMemory() {
        return memory;
    }
}