package se.uu.ub.cora.diva.mixedstorage.db;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.sqldatabase.table.TableFacade;

public class DivaDbSpy implements DivaDbReader {

	public String type;
	public String id;
	public DataGroup dataGroup;
	public TableFacade tableFacade;

	@Override
	public DataGroup read(TableFacade tableFacade, String type, String id) {
		this.tableFacade = tableFacade;
		this.type = type;
		this.id = id;
		dataGroup = new DataGroupSpy("DataGroupFromSpy");
		return dataGroup;
	}

}
