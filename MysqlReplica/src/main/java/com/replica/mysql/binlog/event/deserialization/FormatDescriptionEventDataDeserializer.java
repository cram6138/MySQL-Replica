package com.replica.mysql.binlog.event.deserialization;

import java.io.IOException;

import com.replica.mysql.binlog.event.EventType;
import com.replica.mysql.binlog.event.FormatDescriptionEventData;
import com.replica.mysql.binlog.io.ByteArrayInputStream;

public class FormatDescriptionEventDataDeserializer implements EventDataDeserializer<FormatDescriptionEventData> {

    @Override
    public FormatDescriptionEventData deserialize(ByteArrayInputStream inputStream) throws IOException {
        int eventBodyLength = inputStream.available();
        FormatDescriptionEventData eventData = new FormatDescriptionEventData();
        eventData.setBinlogVersion(inputStream.readInteger(2));
        eventData.setServerVersion(inputStream.readString(50).trim());
        inputStream.skip(4); // redundant, present in a header
        eventData.setHeaderLength(inputStream.readInteger(1));
        inputStream.skip(EventType.FORMAT_DESCRIPTION.ordinal() - 1);
        eventData.setDataLength(inputStream.readInteger(1));
        int checksumBlockLength = eventBodyLength - eventData.getDataLength();
        ChecksumType checksumType = ChecksumType.NONE;
        if (checksumBlockLength > 0) {
            inputStream.skip(inputStream.available() - checksumBlockLength);
            checksumType = ChecksumType.byOrdinal(inputStream.read());
        }
        eventData.setChecksumType(checksumType);
        return eventData;
    }
}
