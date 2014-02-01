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
package ch.eitchnet.utils.dbc;

import java.io.File;
import java.text.MessageFormat;

import ch.eitchnet.utils.helper.StringHelper;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 */
public enum DBC {

	PRE {

		@Override
		public void assertTrue(String msg, boolean value) {
			if (!value) {
				String ex = "Expected true, but was false: {0}"; //$NON-NLS-1$
				ex = MessageFormat.format(ex, msg);
				throw new DbcException(ex);
			}
		}

		@Override
		public void assertFalse(String msg, boolean value) {
			if (value) {
				String ex = "Expected false, but was true: {0}"; //$NON-NLS-1$
				ex = MessageFormat.format(ex, msg);
				throw new DbcException(ex);
			}
		}

		@Override
		public void assertNotEmpty(String msg, String value) {
			if (StringHelper.isEmpty(value)) {
				String ex = "Illegal empty value: {0}"; //$NON-NLS-1$
				ex = MessageFormat.format(ex, msg);
				throw new DbcException(ex);
			}
		}

		@Override
		public void assertNotNull(String msg, Object value) {
			if (value == null) {
				String ex = "Illegal null value: {0}"; //$NON-NLS-1$
				ex = MessageFormat.format(ex, msg);
				throw new DbcException(ex);
			}
		}

		@Override
		public void assertNull(String msg, Object value) {
			if (value != null) {
				String ex = "Illegal situation as value is not null: {0}"; //$NON-NLS-1$
				ex = MessageFormat.format(ex, msg);
				throw new DbcException(ex);
			}
		}

		@Override
		public void assertNotExists(String msg, File file) {
			if (file.exists()) {
				String ex = MessageFormat.format("Illegal situation as file ({0}) exists: {0}", file); //$NON-NLS-1$
				ex = MessageFormat.format(ex, msg);
				throw new DbcException(ex);
			}
		}

		@Override
		public void assertExists(String msg, File file) {
			if (!file.exists()) {
				String ex = MessageFormat.format("Illegal situation as file ({0}) does not exist: {0}", file); //$NON-NLS-1$
				ex = MessageFormat.format(ex, msg);
				throw new DbcException(ex);
			}
		}
	};

	public abstract void assertTrue(String msg, boolean value);

	public abstract void assertFalse(String msg, boolean value);

	public abstract void assertNotEmpty(String msg, String value);

	public abstract void assertNotNull(String msg, Object value);

	public abstract void assertNull(String msg, Object value);

	public abstract void assertNotExists(String msg, File file);

	public abstract void assertExists(String msg, File file);

	public class DbcException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public DbcException(String message) {
			super(message);
		}
	}
}
