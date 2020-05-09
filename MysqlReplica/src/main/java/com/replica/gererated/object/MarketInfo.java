package com.replica.gererated.object;

import java.util.List;

public class MarketInfo {

	private int id;
	private String name;
	private String code;
	private int territoryId;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getTerritoryId() {
		return territoryId;
	}
	public void setTerritoryId(int territoryId) {
		this.territoryId = territoryId;
	}
	@Override
	public String toString() {
		return "MarketInfo [id=" + id + ", name=" + name + ", code=" + code + ", territoryId=" + territoryId + "]";
	}
	
	
}
