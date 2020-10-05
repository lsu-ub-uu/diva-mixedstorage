package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;

public class DivaDbToCoraConverterSpy implements DivaDbToCoraConverter {
	public Map<String, Object> mapToConvert;
	public DataGroup convertedDbDataGroup;
	public List<DataGroup> convertedDataGroups = new ArrayList<>();
	public List<Map<String, Object>> mapsToConvert = new ArrayList<>();

	@Override
	public DataGroup fromMap(Map<String, Object> map) {
		mapsToConvert.add(map);
		mapToConvert = map;
		convertedDbDataGroup = new DataGroupSpy("from Db converter");
		convertedDataGroups.add(convertedDbDataGroup);
		return convertedDbDataGroup;
	}
}
