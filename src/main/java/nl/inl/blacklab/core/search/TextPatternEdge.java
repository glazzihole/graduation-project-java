/*******************************************************************************
 * Copyright (c) 2010, 2012 Institute for Dutch Lexicology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package nl.inl.blacklab.core.search;

import nl.inl.blacklab.core.search.lucene.BLSpanQuery;
import nl.inl.blacklab.core.search.lucene.SpanQueryEdge;

/**
 * Returns either the left edge or right edge of the specified query.
 *
 * Note that the results of this query are zero-length spans.
 */
public class TextPatternEdge extends TextPatternCombiner {

	private boolean rightEdge;

	public TextPatternEdge(TextPattern clause, boolean rightEdge) {
		super(clause);
		this.rightEdge = rightEdge;
	}

	@Override
	public BLSpanQuery translate(QueryExecutionContext context) {
		return new SpanQueryEdge(clauses.get(0).translate(context), rightEdge);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TextPatternEdge) {
			return super.equals(obj) && ((TextPatternEdge)obj).rightEdge == rightEdge;
		}
		return false;
	}

	public boolean isRightEdge() {
		return rightEdge;
	}

	public String getElementName() {
		TextPattern cl = getClause();
		if (cl instanceof TextPatternTags) {
			return ((TextPatternTags)cl).getElementName();
		}
		return null;
	}

	public TextPattern getClause() {
		return clauses.get(0);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + (rightEdge ? 13 : 0);
	}

	@Deprecated
	@Override
	public String toString(QueryExecutionContext context) {
		return "EDGE(" + clauses.get(0).toString(context) + ", " + (rightEdge ? "R" : "L") + ")";
	}

	@Override
	public String toString() {
		return "EDGE(" + clauses.get(0).toString() + ", " + (rightEdge ? "R" : "L") + ")";
	}

}
