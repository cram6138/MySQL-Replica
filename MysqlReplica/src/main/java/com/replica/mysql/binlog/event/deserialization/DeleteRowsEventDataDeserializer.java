package com.replica.mysql.binlog.event.deserialization;


import java.io.IOException;
import java.io.Serializable;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.replica.mysql.binlog.event.DeleteRowsEventData;
import com.replica.mysql.binlog.event.TableMapEventData;
import com.replica.mysql.binlog.io.ByteArrayInputStream;

/**
 * @author bhajuram.c
 */
public class DeleteRowsEventDataDeserializer extends AbstractRowsEventDataDeserializer<DeleteRowsEventData> {

    private boolean mayContainExtraInformation;

    public DeleteRowsEventDataDeserializer(Map<Long, TableMapEventData> tableMapEventByTableId) {
        super(tableMapEventByTableId);
    }

    public DeleteRowsEventDataDeserializer setMayContainExtraInformation(boolean mayContainExtraInformation) {
        this.mayContainExtraInformation = mayContainExtraInformation;
        return this;
    }

    @Override
    public DeleteRowsEventData deserialize(ByteArrayInputStream inputStream) throws IOException {
        DeleteRowsEventData eventData = new DeleteRowsEventData();
        eventData.setTableId(inputStream.readLong(6));
        inputStream.readInteger(2); // reserved
        if (mayContainExtraInformation) {
            int extraInfoLength = inputStream.readInteger(2);
            inputStream.skip(extraInfoLength - 2);
        }
        int numberOfColumns = inputStream.readPackedInteger();
        eventData.setIncludedColumns(inputStream.readBitSet(numberOfColumns, true));
        eventData.setRows(deserializeRows(eventData.getTableId(), eventData.getIncludedColumns(), inputStream));
        return eventData;
    }

    private List<Serializable[]> deserializeRows(long tableId, BitSet includedColumns, ByteArrayInputStream inputStream)
            throws IOException {
        List<Serializable[]> result = new LinkedList<Serializable[]>();
        while (inputStream.available() > 0) {
            result.add(deserializeRow(tableId, includedColumns, inputStream));
        }
        return result;
    }

}
