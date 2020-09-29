package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.diva.mixedstorage.db.organisation.MultipleRowDbToDataReader;

public class DivaDbFactorySpy implements DivaDbFactory {
	public boolean factorWasCalled = false;
	public DivaDbSpy factored;
	public String type;
	public List<DivaDbSpy> factoredList = new ArrayList<>();
	public List<MultipleRowDbToDataReaderSpy> listOfFactoredMultiples = new ArrayList<>();
	public List<String> usedTypes = new ArrayList<>();
	public boolean returnEmptyResult = false;
	public MultipleRowDbToDataReaderSpy factoredMultiple;

	@Override
	public DivaDbReader factor(String type) {
		factorWasCalled = true;
		this.type = type;
		factored = new DivaDbSpy();
		factoredList.add(factored);
		return factored;
	}

	@Override
	public MultipleRowDbToDataReader factorMultipleReader(String type) {
		usedTypes.add(type);
		factoredMultiple = new MultipleRowDbToDataReaderSpy();
		factoredMultiple.returnEmptyResult = returnEmptyResult;
		listOfFactoredMultiples.add(factoredMultiple);
		return factoredMultiple;
	}

}
