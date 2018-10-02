package nl.inl.blacklab.server.jobs;

import nl.inl.blacklab.core.perdocument.DocProperty;

import java.util.Map;

public class DocGroupSettings {

	DocProperty groupBy;

	public DocGroupSettings(DocProperty groupBy) {
		super();
		this.groupBy = groupBy;
	}

	public DocProperty groupBy() {
		return groupBy;
	}

	@Override
	public String toString() {
		return "docgroup=" + groupBy.serialize();
	}

	public void getUrlParam(Map<String, String> param) {
		param.put("group", groupBy.serialize());
	}

}
