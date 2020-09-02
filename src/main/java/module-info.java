module se.uu.ub.cora.diva.mixedstorage {
	requires transitive se.uu.ub.cora.sqldatabase;
	requires transitive se.uu.ub.cora.httphandler;
	requires transitive java.xml;
	requires se.uu.ub.cora.logger;
	requires se.uu.ub.cora.basicstorage;
	requires transitive se.uu.ub.cora.storage;
	requires se.uu.ub.cora.searchstorage;
	requires se.uu.ub.cora.gatekeeper;
	requires java.sql;

	provides se.uu.ub.cora.storage.RecordStorageProvider
			with se.uu.ub.cora.diva.mixedstorage.DivaMixedRecordStorageProvider;
	provides se.uu.ub.cora.storage.MetadataStorageProvider
			with se.uu.ub.cora.diva.mixedstorage.DivaMixedRecordStorageProvider;

	uses se.uu.ub.cora.gatekeeper.user.GuestUserStorageProvider;

	opens person;
}