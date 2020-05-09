package com.replica.mysql.binlog.event;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

/**
 * @author bhajuram.c
 */
public class UpdateRowsEventData implements EventData {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6858182951382714736L;
	private long tableId;
    private BitSet includedColumnsBeforeUpdate;
    private BitSet includedColumns;
    
    private List<Map.Entry<Serializable[], Serializable[]>> rows;

    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
    }

    public BitSet getIncludedColumnsBeforeUpdate() {
        return includedColumnsBeforeUpdate;
    }

    public void setIncludedColumnsBeforeUpdate(BitSet includedColumnsBeforeUpdate) {
        this.includedColumnsBeforeUpdate = includedColumnsBeforeUpdate;
    }

    public BitSet getIncludedColumns() {
        return includedColumns;
    }

    public void setIncludedColumns(BitSet includedColumns) {
        this.includedColumns = includedColumns;
    }

    public List<Map.Entry<Serializable[], Serializable[]>> getRows() {
        return rows;
    }

    public void setRows(List<Map.Entry<Serializable[], Serializable[]>> rows) {
        this.rows = rows;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("UpdateRowsEventData");
        sb.append("{tableId=").append(tableId);
        sb.append(", includedColumnsBeforeUpdate=").append(includedColumnsBeforeUpdate);
        sb.append(", includedColumns=").append(includedColumns);
        sb.append(", rows=[");
        for (Map.Entry<Serializable[], Serializable[]> row : rows) {
            sb.append("\n    ").
                    append("{before=").append(Arrays.toString(row.getKey())).
                    append(", after=").append(Arrays.toString(row.getValue())).
               append("},");
        }
        if (!rows.isEmpty()) {
            sb.replace(sb.length() - 1, sb.length(), "\n");
        }
        sb.append("]}");
        return sb.toString();
    }
}
