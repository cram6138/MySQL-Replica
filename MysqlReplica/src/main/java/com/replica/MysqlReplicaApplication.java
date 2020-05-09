package com.replica;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.replica.data.parser.MapResultSetIntoObject;
import com.replica.gererated.object.ContainerInfo;
import com.replica.gererated.object.MarketInfo;
import com.replica.mysql.binlog.BinaryLogClient;
import com.replica.mysql.binlog.event.DeleteRowsEventData;
import com.replica.mysql.binlog.event.EventData;
import com.replica.mysql.binlog.event.TableMapEventData;
import com.replica.mysql.binlog.event.UpdateRowsEventData;
import com.replica.mysql.binlog.event.WriteRowsEventData;

@SpringBootApplication
public class MysqlReplicaApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(MysqlReplicaApplication.class, args);
		
		final Map<String, Long> tableMap = new HashMap<String, Long>();
		BinaryLogClient client = new BinaryLogClient("cram", "cram@6138");
		client.registerEventListener(event -> {
			EventData data = event.getData();
			// System.out.println("DATA :::::::::: " + data);

			if (data instanceof TableMapEventData) {
				TableMapEventData tableData = (TableMapEventData) data;
				tableMap.put(tableData.getTable(), tableData.getTableId());
			} else if (data instanceof WriteRowsEventData) {
				WriteRowsEventData eventData = (WriteRowsEventData) data;
				if (tableMap.get("container") != null && eventData.getTableId() == tableMap.get("container")) {
					for (Serializable[] row : eventData.getRows()) {
						ContainerInfo containerInfo = MapResultSetIntoObject.parseContainerInfo(row);
						System.out.println(containerInfo);
					}
				}
				if (tableMap.get("market") != null && eventData.getTableId() == tableMap.get("market")) {
					for (Serializable[] row : eventData.getRows()) {
						MarketInfo marketInfo = MapResultSetIntoObject.parseMarketInfo(row);
						System.out.println("Added New Market Record :::::: "+marketInfo);
					}
				}
			} else if (data instanceof UpdateRowsEventData) {
				UpdateRowsEventData eventData = (UpdateRowsEventData) data;
				
				if (tableMap.get("market") != null && eventData.getTableId() == tableMap.get("market")) {
					for (Entry<Serializable[], Serializable[]> row : eventData.getRows()) {
						MarketInfo marketInfo = MapResultSetIntoObject.parseMarketInfo(row.getValue());
						System.out.println("Updated Market record :::::: "+marketInfo);
					}
				} else if (tableMap.get("container") != null && eventData.getTableId() == tableMap.get("container")) {
					List<ContainerInfo> updatedContainerRecords = new ArrayList<ContainerInfo>(); 
					for (Entry<Serializable[], Serializable[]> row : eventData.getRows()) {
						updatedContainerRecords.add(MapResultSetIntoObject.parseContainerInfo(row.getValue()));
					}
					System.out.println(updatedContainerRecords);
				}
			} else if (data instanceof DeleteRowsEventData) {
				DeleteRowsEventData eventData = (DeleteRowsEventData)data;
				if (tableMap.get("market") != null && eventData.getTableId() == tableMap.get("market")) {
					for (Serializable[] row : eventData.getRows()) {
						MarketInfo marketInfo = MapResultSetIntoObject.parseMarketInfo(row);
						System.out.println("Deleted market Record :::::: "+marketInfo);
					}
				}
			}
		});
		client.connect();

	}
	
	public void getInfoChangedEvent() {
		
	}

}
