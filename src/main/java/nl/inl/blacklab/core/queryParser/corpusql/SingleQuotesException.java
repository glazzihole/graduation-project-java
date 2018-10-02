package nl.inl.blacklab.core.queryParser.corpusql;

public class SingleQuotesException extends ParseException {
	public SingleQuotesException() {
		super("Only double quoted strings are allowed in CorpusQL query");
	}
}
