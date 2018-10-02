package nl.inl.blacklab.core.search;

/**
 * (Part of) the contents of a document.
 */
public abstract class DocContents {

	@Override
	public String toString() {
		return "DocContents: " + getXml();
	}

	public abstract String getXml();

}
