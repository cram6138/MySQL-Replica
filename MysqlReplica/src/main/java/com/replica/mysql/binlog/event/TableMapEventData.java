package com.replica.mysql.binlog.event;

import java.util.BitSet;

public class TableMapEventData implements EventData {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6744238599361743931L;
	private long tableId;
    private String database;
    private String table;
    private byte[] columnTypes;
    private int[] columnMetadata;
    private BitSet columnNullability;
    private TableMapEventMetadata eventMetadata;

    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public byte[] getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypes(byte[] columnTypes) {
        this.columnTypes = columnTypes;
    }

    public int[] getColumnMetadata() {
        return columnMetadata;
    }

    public void setColumnMetadata(int[] columnMetadata) {
        this.columnMetadata = columnMetadata;
    }

    public BitSet getColumnNullability() {
        return columnNullability;
    }

    public void setColumnNullability(BitSet columnNullability) {
        this.columnNullability = columnNullability;
    }

    public TableMapEventMetadata getEventMetadata() { return eventMetadata; }

    public void setEventMetadata(TableMapEventMetadata eventMetadata) { this.eventMetadata = eventMetadata; }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TableMapEventData");
        sb.append("{tableId=").append(tableId);
        sb.append(", database='").append(database).append('\'');
        sb.append(", table='").append(table).append('\'');
        sb.append(", columnTypes=").append(columnTypes == null ? "null" : "");
        for (int i = 0; columnTypes != null && i < columnTypes.length; ++i) {
            sb.append(i == 0 ? "" : ", ").append(columnTypes[i]);
        }
        sb.append(", columnMetadata=").append(columnMetadata == null ? "null" : "");
        for (int i = 0; columnMetadata != null && i < columnMetadata.length; ++i) {
            sb.append(i == 0 ? "" : ", ").append(columnMetadata[i]);
        }
        sb.append(", columnNullability=").append(columnNullability);
        sb.append(", eventMetadata=").append(eventMetadata);
        sb.append('}');
        return sb.toString();
    }
}
