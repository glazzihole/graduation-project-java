package nl.inl.blacklab.server.requesthandlers;

import nl.inl.blacklab.search.Searcher;
import nl.inl.blacklab.search.indexstructure.ComplexFieldDesc;
import nl.inl.blacklab.search.indexstructure.IndexStructure;
import nl.inl.blacklab.search.indexstructure.IndexStructure.MetadataGroup;
import nl.inl.blacklab.search.indexstructure.MetadataFieldDesc;
import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.datastream.DataStream;
import nl.inl.blacklab.server.exceptions.BlsException;
import nl.inl.blacklab.server.jobs.User;
import nl.inl.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Get information about the structure of an index.
 */
public class RequestHandlerIndexStructure extends RequestHandler {

	public RequestHandlerIndexStructure(BlackLabServer servlet, HttpServletRequest request, User user, String indexName, String urlResource, String urlPathPart) {
		super(servlet, request, user, indexName, urlResource, urlPathPart);
	}

	@Override
	public boolean isCacheAllowed() {
		return false; // because status might change (or you might reindex)
	}

	@Override
	public int handle(DataStream ds) throws BlsException {
		Searcher searcher = getSearcher();
		IndexStructure struct = searcher.getIndexStructure();

		// Assemble response
		ds.startMap()
			.entry("indexName", indexName)
			.entry("displayName", struct.getDisplayName())
			.entry("description", struct.getDescription())
			.entry("status", indexMan.getIndexStatus(indexName))
			.entry("contentViewable", struct.contentViewable());
		String documentFormat = struct.getDocumentFormat();
		if (documentFormat != null && documentFormat.length() > 0) {
            ds.entry("documentFormat", documentFormat);
        }
		if (struct.getTokenCount() > 0) {
            ds.entry("tokenCount", struct.getTokenCount());
        }

		ds.startEntry("versionInfo").startMap()
			.entry("blackLabBuildTime", struct.getIndexBlackLabBuildTime())
			.entry("blackLabVersion", struct.getIndexBlackLabVersion())
			.entry("indexFormat", struct.getIndexFormat())
			.entry("timeCreated", struct.getTimeCreated())
			.entry("timeModified", struct.getTimeModified())
		.endMap().endEntry();

		ds.startEntry("fieldInfo").startMap()
			.entry("pidField", StringUtil.nullToEmpty(struct.pidField()))
			.entry("titleField", StringUtil.nullToEmpty(struct.titleField()))
			.entry("authorField", StringUtil.nullToEmpty(struct.authorField()))
			.entry("dateField", StringUtil.nullToEmpty(struct.dateField()))
		.endMap().endEntry();

		ds.startEntry("complexFields").startMap();
		// Complex fields
		//DataObjectMapAttribute doComplexFields = new DataObjectMapAttribute("complexField", "name");
		for (String name: struct.getComplexFields()) {
			ds.startAttrEntry("complexField", "name", name);

	        Set<String> setShowValuesFor = searchParam.listValuesFor();
	        Set<String> setShowSubpropsFor = searchParam.listSubpropsFor();
			ComplexFieldDesc fieldDesc = struct.getComplexFieldDesc(name);
            RequestHandlerFieldInfo.describeComplexField(ds, null, name, fieldDesc, searcher, setShowValuesFor, setShowSubpropsFor);

			ds.endAttrEntry();
		}
		ds.endMap().endEntry();

		ds.startEntry("metadataFields").startMap();
		// Metadata fields
		//DataObjectMapAttribute doMetaFields = new DataObjectMapAttribute("metadataField", "name");
		for (String name: struct.getMetadataFields()) {
			ds.startAttrEntry("metadataField", "name", name);

            MetadataFieldDesc fd = struct.getMetadataFieldDesc(name);
			RequestHandlerFieldInfo.describeMetadataField(ds, null, name, fd, true);

			ds.endAttrEntry();
		}
		ds.endMap().endEntry();

        Map<String, MetadataGroup> metaGroups = struct.getMetaFieldGroups();
		Set<String> metadataFieldsNotInGroups = new HashSet<>(struct.getMetadataFields());
        for (MetadataGroup metaGroup: metaGroups.values()) {
            for (String field: metaGroup.getFields()) {
                metadataFieldsNotInGroups.remove(field);
            }
        }
        ds.startEntry("metadataFieldGroups").startList();
        boolean addedRemaining = false;
        for (MetadataGroup metaGroup: metaGroups.values()) {
            ds.startItem("metadataFieldGroup").startMap();
            ds.entry("name", metaGroup.getName());
            ds.startEntry("fields").startList();
            for (String field: metaGroup.getFields()) {
                ds.item("field", field);
            }
            if (!addedRemaining && metaGroup.addRemainingFields()) {
                addedRemaining = true;
                for (String field: metadataFieldsNotInGroups) {
                    ds.item("field", field);
                }
            }
            ds.endList().endEntry();
            ds.endMap().endItem();
        }
        ds.endList().endEntry();

		// Remove any empty settings
		//response.removeEmptyMapValues();

		ds.endMap();

		return HTTP_OK;
	}

}
