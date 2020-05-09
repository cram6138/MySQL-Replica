package com.replica.data.parser;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Date;

import com.replica.gererated.object.ContainerInfo;
import com.replica.gererated.object.MarketInfo;

//@Component
public class MapResultSetIntoObject {

	public static MarketInfo parseMarketInfo(Serializable[] value) {
		MarketInfo marketInfo = new MarketInfo();
		marketInfo.setId((Integer) value[0]);
		marketInfo.setCode((String) value[1]);
		marketInfo.setName((String) value[2]);
		marketInfo.setTerritoryId((Integer) value[3]);
		return marketInfo;
	}

	public static ContainerInfo parseContainerInfo(Serializable[] value) {
		ContainerInfo containerInfo = new ContainerInfo();
		containerInfo.setId((Integer) value[0]);
		containerInfo.setPSProject((String) value[1]);
		containerInfo.setCatsStatus((String) value[2]);
		containerInfo.setContainerCode((String) value[3]);
		containerInfo.setFuzeReservationId((String) value[4]);
		containerInfo.setFuzeStatus((String) value[5]);
		containerInfo.setLocalMarket((String) value[6]);
		containerInfo.setMarket((String) value[7]);
		containerInfo.setMROrderCode((String) value[8]);
		containerInfo.setPslc((String) value[9]);
		if ((Date) value[10] != null) {
			containerInfo.setReservationCreationDate((Date) value[10]);
		}
		containerInfo.setReserved(((BitSet) value[11]).isEmpty() ? false : true);
		containerInfo.setReservedUsername((String) value[12]);
		containerInfo.setSubMarket((String) value[13]);
		containerInfo.setTerritory((String) value[14]);
		if ((Date) value[10] != null) {
			containerInfo.setUseByDate((Date) value[15]);
		}
		containerInfo.setBuyerId((Integer) value[16]);
		containerInfo.setReservationNotes((String) value[18]);
		return containerInfo;
	}

}
