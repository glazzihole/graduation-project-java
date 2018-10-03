package nl.inl.blacklab.server.jobs;

import java.util.Map;

import nl.inl.blacklab.perdocument.DocGroupProperty;

public class DocGroupSortSettings {

	private DocGroupProperty sortBy;

	private boolean reverse;

	public DocGroupSortSettings(DocGroupProperty sortBy, boolean reverse) {
		super();
		this.sortBy = sortBy;
		this.reverse = reverse;
	}

	public DocGroupProperty sortBy() {
		return sortBy;
	}

	public boolean reverse() {
		return reverse;
	}

	@Override
	public String toString() {
		return "docgroupsort=" + sortBy + ", sortreverse=" + reverse;
	}

	public void getUrlParam(Map<String, String> param) {
		param.put("sort", (reverse ? "-" : "") + sortBy.serialize());
	}

}
