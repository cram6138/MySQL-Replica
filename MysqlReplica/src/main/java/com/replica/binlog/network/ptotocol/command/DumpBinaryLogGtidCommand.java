package com.replica.binlog.network.ptotocol.command;

import java.io.IOException;
import java.util.Collection;

import com.replica.mysql.binlog.GtidSet;
import com.replica.mysql.binlog.io.ByteArrayOutputStream;

public class DumpBinaryLogGtidCommand implements Command {

    private long serverId;
    private String binlogFilename;
    private long binlogPosition;
    private GtidSet gtidSet;

    public DumpBinaryLogGtidCommand(long serverId, String binlogFilename, long binlogPosition, GtidSet gtidSet) {
        this.serverId = serverId;
        this.binlogFilename = binlogFilename;
        this.binlogPosition = binlogPosition;
        this.gtidSet = gtidSet;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.writeInteger(CommandType.BINLOG_DUMP_GTID.ordinal(), 1);
        buffer.writeInteger(0, 2); // flag
        buffer.writeLong(this.serverId, 4);
        buffer.writeInteger(this.binlogFilename.length(), 4);
        buffer.writeString(this.binlogFilename);
        buffer.writeLong(this.binlogPosition, 8);
        Collection<GtidSet.UUIDSet> uuidSets = gtidSet.getUUIDSets();
        int dataSize = 8 /* number of uuidSets */;
        for (GtidSet.UUIDSet uuidSet : uuidSets) {
            dataSize += 16 /* uuid */ + 8 /* number of intervals */ +
                uuidSet.getIntervals().size() /* number of intervals */ * 16 /* start-end */;
        }
        buffer.writeInteger(dataSize, 4);
        buffer.writeLong(uuidSets.size(), 8);
        for (GtidSet.UUIDSet uuidSet : uuidSets) {
            buffer.write(hexToByteArray(uuidSet.getUUID().replace("-", "")));
            Collection<GtidSet.Interval> intervals = uuidSet.getIntervals();
            buffer.writeLong(intervals.size(), 8);
            for (GtidSet.Interval interval : intervals) {
                buffer.writeLong(interval.getStart(), 8);
                buffer.writeLong(interval.getEnd() + 1 /* right-open */, 8);
            }
        }
        return buffer.toByteArray();
    }

    private static byte[] hexToByteArray(String uuid) {
        byte[] b = new byte[uuid.length() / 2];
        for (int i = 0, j = 0; j < uuid.length(); j += 2) {
            b[i++] = (byte) Integer.parseInt(uuid.charAt(j) + "" + uuid.charAt(j + 1), 16);
        }
        return b;
    }

}
