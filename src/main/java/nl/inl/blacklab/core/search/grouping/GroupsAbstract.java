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
package nl.inl.blacklab.core.search.grouping;


/**
 * A number of groups of hits, grouped on the basis of a list of criteria.
 */
abstract class GroupsAbstract implements Groups {
	protected HitProperty criteria;

	public GroupsAbstract(HitProperty criteria) {
		this.criteria = criteria;
	}

	protected HitPropValue getGroupIdentity(int index) {
		return criteria.get(index);
	}

	@Override
	public HitProperty getGroupCriteria() {
		return criteria;
	}

}