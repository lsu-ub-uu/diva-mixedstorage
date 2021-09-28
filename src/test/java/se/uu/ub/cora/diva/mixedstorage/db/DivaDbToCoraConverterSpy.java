package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.sqldatabase.Row;

public class DivaDbToCoraConverterSpy implements DivaDbToCoraConverter {
	public Row rowToConvert;
	public DataGroup convertedDbDataGroup;
	public List<DataGroup> convertedDataGroups = new ArrayList<>();
	public List<Row> rowsToConvert = new ArrayList<>();

	@Override
	public DataGroup fromMap(Row row) {
		rowsToConvert.add(row);
		rowToConvert = row;

		convertedDbDataGroup = new DataGroupSpy("from Db converter");
		convertedDataGroups.add(convertedDbDataGroup);
		return convertedDbDataGroup;
	}
}
