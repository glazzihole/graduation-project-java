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
/**
 *
 */
package nl.inl.blacklab.core.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;

import nl.inl.blacklab.core.filter.RemoveAllAccentsFilter;
import nl.inl.blacklab.core.index.complex.ComplexFieldUtil;

/**
 * Analyzer that doesn't tokenize but returns a single token.
 *
 * Has the option of analyzing case-/accent-sensitive or -insensitive, depending on the field name.
 */
public final class BLNonTokenizingAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer source = new BLNonTokenizer();
		TokenStream filter = source;
		boolean caseSensitive = ComplexFieldUtil.isCaseSensitive(fieldName);
		if (!caseSensitive)
		{
			filter = new LowerCaseFilter(filter);// lowercase all
		}
		boolean diacSensitive = ComplexFieldUtil.isDiacriticsSensitive(fieldName);
		if (!diacSensitive)
		{
			filter = new RemoveAllAccentsFilter(filter); // remove accents
		}
		return new TokenStreamComponents(source, filter);
	}

}
