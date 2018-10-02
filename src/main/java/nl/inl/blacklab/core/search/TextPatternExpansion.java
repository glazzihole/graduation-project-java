package nl.inl.blacklab.core.search;

import nl.inl.blacklab.core.search.lucene.BLSpanQuery;
import nl.inl.blacklab.core.search.lucene.SpanQueryExpansion;

public class TextPatternExpansion extends TextPattern {

	protected TextPattern clause;

	protected boolean expandToLeft;

	/*
	 * The minimum number of tokens in this stretch.
	 */
	protected int min;

	/*
	 * The maximum number of tokens in this stretch.
	 */
	protected int max;

	public TextPatternExpansion(TextPattern clause, boolean expandToLeft, int min, int max) {
		this.clause = clause;
		this.expandToLeft = expandToLeft;
		this.min = min;
		this.max = max == -1 ? MAX_UNLIMITED : max;
		if (min > this.max) {
            throw new IllegalArgumentException("min > max");
        }
		if (min < 0 || this.max < 0) {
            throw new IllegalArgumentException("min or max can't be negative");
        }
	}

	@Override
	public BLSpanQuery translate(QueryExecutionContext context) {
		SpanQueryExpansion spanQueryExpansion = new SpanQueryExpansion(clause.translate(context), expandToLeft, min, max);
		spanQueryExpansion.setIgnoreLastToken(context.alwaysHasClosingToken());
		return spanQueryExpansion;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TextPatternExpansion) {
			TextPatternExpansion tp = ((TextPatternExpansion) obj);
			return clause.equals(tp.clause) && expandToLeft == tp.expandToLeft && min == tp.min && max == tp.max;
		}
		return false;
	}

	public int getMinExpand() {
		return min;
	}

	public int getMaxExpand() {
		return max;
	}

	public boolean isExpandToLeft() {
		return expandToLeft;
	}

	public TextPattern getClause() {
		return clause;
	}

	@Override
	public int hashCode() {
		return clause.hashCode() + 1023 * (expandToLeft ? 1 : 0) + 13 * min + 31 * max;
	}

	@Deprecated
	@Override
	public String toString(QueryExecutionContext context) {
		return "EXPAND(" + clause.toString(context) + ", " + (expandToLeft ? "L" : "R") + ", " + min + ", " + inf(max) + ")";
	}

	@Override
	public String toString() {
		return "EXPAND(" + clause + ", " + (expandToLeft ? "L" : "R") + ", " + min + ", " + inf(max) + ")";
	}

}
