package com.replica.mysql.binlog.event.deserialization;

import java.io.IOException;

import com.replica.mysql.binlog.event.TableMapEventData;
import com.replica.mysql.binlog.event.TableMapEventMetadata;
import com.replica.mysql.binlog.io.ByteArrayInputStream;

public class TableMapEventDataDeserializer implements EventDataDeserializer<TableMapEventData> {

    private final TableMapEventMetadataDeserializer metadataDeserializer = new TableMapEventMetadataDeserializer();

    @Override
    public TableMapEventData deserialize(ByteArrayInputStream inputStream) throws IOException {
        TableMapEventData eventData = new TableMapEventData();
        eventData.setTableId(inputStream.readLong(6));
        inputStream.skip(3); // 2 bytes reserved for future use + 1 for the length of database name
        eventData.setDatabase(inputStream.readZeroTerminatedString());
        inputStream.skip(1); // table name
        eventData.setTable(inputStream.readZeroTerminatedString());
        int numberOfColumns = inputStream.readPackedInteger();
        eventData.setColumnTypes(inputStream.read(numberOfColumns));
        inputStream.readPackedInteger(); // metadata length
        eventData.setColumnMetadata(readMetadata(inputStream, eventData.getColumnTypes()));
        eventData.setColumnNullability(inputStream.readBitSet(numberOfColumns, true));
        int metadataLength = inputStream.available();
        TableMapEventMetadata metadata = null;
        if (metadataLength > 0) {
            metadata = metadataDeserializer.deserialize(
                new ByteArrayInputStream(inputStream.read(metadataLength)),
                numericColumnCount(eventData.getColumnTypes())
            );
        }
        eventData.setEventMetadata(metadata);
        return eventData;
    }

    private int numericColumnCount(byte[] types) {
        int count = 0;
        for (int i = 0; i < types.length; i++) {
            switch (ColumnType.byCode(types[i] & 0xff)) {
                case TINY:
                case SHORT:
                case INT24:
                case LONG:
                case LONGLONG:
                case NEWDECIMAL:
                case FLOAT:
                case DOUBLE:
                    count++;
                    break;
                default:
                    break;
            }
        }
        return count;
    }

    private int[] readMetadata(ByteArrayInputStream inputStream, byte[] columnTypes) throws IOException {
        int[] metadata = new int[columnTypes.length];
        for (int i = 0; i < columnTypes.length; i++) {
            switch(ColumnType.byCode(columnTypes[i] & 0xFF)) {
                case FLOAT:
                case DOUBLE:
                case BLOB:
                case JSON:
                case GEOMETRY:
                    metadata[i] = inputStream.readInteger(1);
                    break;
                case BIT:
                case VARCHAR:
                case NEWDECIMAL:
                    metadata[i] = inputStream.readInteger(2);
                    break;
                case SET:
                case ENUM:
                case STRING:
                    metadata[i] = bigEndianInteger(inputStream.read(2), 0, 2);
                    break;
                case TIME_V2:
                case DATETIME_V2:
                case TIMESTAMP_V2:
                    metadata[i] = inputStream.readInteger(1); // fsp (@see {@link ColumnType})
                    break;
                default:
                    metadata[i] = 0;
            }
        }
        return metadata;
    }

    private static int bigEndianInteger(byte[] bytes, int offset, int length) {
        int result = 0;
        for (int i = offset; i < (offset + length); i++) {
            byte b = bytes[i];
            result = (result << 8) | (b >= 0 ? (int) b : (b + 256));
        }
        return result;
    }

}
