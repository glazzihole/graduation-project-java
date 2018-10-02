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

import org.apache.lucene.document.Document;

import nl.inl.blacklab.core.externalstorage.ContentStore;
import nl.inl.blacklab.core.index.complex.ComplexFieldUtil;

/**
 * Defines a way to access the original indexed content.
 */
public class ContentAccessor {
	protected String fieldName;

	private ContentStore contentStore;

	private String contentIdField = null;

	public ContentAccessor(String fieldName, ContentStore contentStore) {
		contentIdField = ComplexFieldUtil.contentIdField(ComplexFieldUtil.getBaseName(fieldName));
		this.contentStore = contentStore;
	}

	public String getFieldName() {
		return fieldName;
	}

	public ContentStore getContentStore() {
		return contentStore;
	}

	public ContentAccessor(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * Get substrings from a document.
	 *
	 * Note: if start and end are both -1 for a certain substring, the whole document is returned.
	 *
	 * @param d
	 *            the Lucene document (contains the file name)
	 * @param start
	 *            start positions of the substrings.
	 *            -1 means start of document.
	 * @param end
	 *            end positions of the substrings.
	 *            -1 means end of document.
	 * @return the requested substrings from this document
	 */
	public String[] getSubstringsFromDocument(Document d, int[] start, int[] end) {
		int contentId = getContentId(d);
		return getSubstringsFromDocument(contentId, start, end);
	}

	private int getContentId(Document d) {
		String contentIdStr = d.get(contentIdField);
		if (contentIdStr == null) {
            throw new RuntimeException("Lucene document has no content id: " + d);
        }
		int contentId = Integer.parseInt(contentIdStr);
		return contentId;
	}

	/**
	 * Get substrings from a document.
	 *
	 * Note: if start and end are both -1 for a certain substring, the whole document is returned.
	 *
	 * @param contentId
	 *            the content id
	 * @param start
	 *            start positions of the substrings.
	 *            -1 means start of document.
	 * @param end
	 *            end positions of the substrings.
	 *            -1 means end of document.
	 * @return the requested substrings from this document
	 */
	public String[] getSubstringsFromDocument(int contentId, int[] start, int[] end) {
		return contentStore.retrieveParts(contentId, start, end);
	}

	public void delete(Document d) {
		delete(getContentId(d));
	}

	private void delete(int contentId) {
		contentStore.delete(contentId);
	}

	public void close() {
		contentStore.close();
	}

}
