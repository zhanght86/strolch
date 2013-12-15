/*
 * Copyright 2013 Robert von Burg <eitch@eitchnet.ch>
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
 */
package ch.eitchnet.xmlpers.objref;

import java.io.File;
import java.text.MessageFormat;

import ch.eitchnet.xmlpers.api.PersistenceContext;
import ch.eitchnet.xmlpers.api.PersistenceTransaction;
import ch.eitchnet.xmlpers.impl.PathBuilder;

public class SubTypeRef extends ObjectRef {

	private final String type;
	private final String subType;

	public SubTypeRef(String realmName, String type, String subType) {
		super(realmName, RefNameCreator.createSubTypeName(realmName, type, subType));
		this.type = type;
		this.subType = subType;
	}

	@Override
	public String getType() {
		return this.type;
	}

	public String getSubType() {
		return this.subType;
	}

	@Override
	public boolean isRoot() {
		return false;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public ObjectRef getParent(PersistenceTransaction tx) {
		return tx.getRealm().getObjectRefCache().getTypeRef(this.type);
	}

	@Override
	public ObjectRef getChildIdRef(PersistenceTransaction tx, String id) {
		return tx.getRealm().getObjectRefCache().getIdOfSubTypeRef(this.type, this.subType, id);
	}

	@Override
	public ObjectRef getChildTypeRef(PersistenceTransaction tx, String type) {
		String msg = MessageFormat.format("A SubType has no child type: {0}", getName()); //$NON-NLS-1$
		throw new UnsupportedOperationException(msg);
	}

	@Override
	public File getPath(PathBuilder pathBuilder) {
		return pathBuilder.getSubTypePath(this.type, this.subType);
	}
	
	@Override
	public <T> PersistenceContext<T> createPersistenceContext(PersistenceTransaction tx) {
		String msg = MessageFormat.format("{0} is not a leaf and can thus not have a Persistence Context", getName()); //$NON-NLS-1$
		throw new UnsupportedOperationException(msg);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.realmName == null) ? 0 : this.realmName.hashCode());
		result = prime * result + ((this.subType == null) ? 0 : this.subType.hashCode());
		result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubTypeRef other = (SubTypeRef) obj;
		if (this.realmName == null) {
			if (other.realmName != null)
				return false;
		} else if (!this.realmName.equals(other.realmName))
			return false;
		if (this.subType == null) {
			if (other.subType != null)
				return false;
		} else if (!this.subType.equals(other.subType))
			return false;
		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;
		return true;
	}
}
