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
package li.strolch.xmlpers.test;

import static li.strolch.xmlpers.test.model.ModelBuilder.BOOK_ID;
import static li.strolch.xmlpers.test.model.ModelBuilder.assertBook;
import static li.strolch.xmlpers.test.model.ModelBuilder.assertBookUpdated;
import static li.strolch.xmlpers.test.model.ModelBuilder.createBook;
import static li.strolch.xmlpers.test.model.ModelBuilder.updateBook;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import li.strolch.xmlpers.api.IoMode;
import li.strolch.xmlpers.api.ObjectDao;
import li.strolch.xmlpers.api.PersistenceConstants;
import li.strolch.xmlpers.api.PersistenceTransaction;
import li.strolch.xmlpers.objref.IdOfTypeRef;
import li.strolch.xmlpers.objref.ObjectRef;
import li.strolch.xmlpers.objref.TypeRef;
import li.strolch.xmlpers.test.impl.TestConstants;
import li.strolch.xmlpers.test.model.Book;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 * 
 */
public class ObjectDaoBookTest extends AbstractPersistenceTest {

	private static final String BASEPATH = "target/db/ObjectDaoTest/"; //$NON-NLS-1$

	@Before
	public void before() {
		cleanPath(BASEPATH);
	}

	private void setup(IoMode ioMode) {
		Properties properties = new Properties();
		properties.setProperty(PersistenceConstants.PROP_BASEPATH, BASEPATH + ioMode.name());
		setup(properties);
	}

	@Test
	public void testCrudSax() {
		setup(IoMode.SAX);
		testCrud(IoMode.SAX);
	}

	@Test
	public void testCrudDom() {
		setup(IoMode.DOM);
		testCrud(IoMode.DOM);
	}

	private PersistenceTransaction freshTx(IoMode ioMode) {
		PersistenceTransaction tx = this.persistenceManager.openTx();
		tx.setIoMode(ioMode);
		return tx;
	}

	private void testCrud(IoMode ioMode) {

		ObjectDao objectDao;

		// create new book
		Book book = createBook();
		try (PersistenceTransaction tx = freshTx(ioMode);) {
			objectDao = tx.getObjectDao();
			objectDao.add(book);
		}

		// read book
		try (PersistenceTransaction tx = freshTx(ioMode);) {
			IdOfTypeRef bookRef = tx.getObjectRefCache()
					.getIdOfTypeRef(TestConstants.TYPE_BOOK, Long.toString(BOOK_ID));
			objectDao = tx.getObjectDao();
			book = objectDao.queryById(bookRef);
			assertBook(book);

			// modify book
			updateBook(book);
			objectDao.update(book);
		}

		// read modified book
		try (PersistenceTransaction tx = freshTx(ioMode);) {
			IdOfTypeRef bookRef = tx.getObjectRefCache()
					.getIdOfTypeRef(TestConstants.TYPE_BOOK, Long.toString(BOOK_ID));
			objectDao = tx.getObjectDao();
			book = objectDao.queryById(bookRef);
			assertBookUpdated(book);
		}

		// delete book
		try (PersistenceTransaction tx = freshTx(ioMode);) {
			objectDao = tx.getObjectDao();
			objectDao.remove(book);
		}

		// fail to read
		try (PersistenceTransaction tx = freshTx(ioMode);) {
			IdOfTypeRef bookRef = tx.getObjectRefCache()
					.getIdOfTypeRef(TestConstants.TYPE_BOOK, Long.toString(BOOK_ID));
			objectDao = tx.getObjectDao();
			book = objectDao.queryById(bookRef);
			assertNull(book);

			// and create again
			book = createBook();
			assertBook(book);
			objectDao.add(book);
		}
	}

	@Test
	public void testBulkSax() {
		setup(IoMode.SAX);
		testBulk(IoMode.SAX);
	}

	@Test
	public void testBulkDom() {
		setup(IoMode.DOM);
		testBulk(IoMode.DOM);
	}

	private void testBulk(IoMode ioMode) {

		// create a list of books
		List<Book> books = new ArrayList<>(10);
		for (int i = 0; i < 10; i++) {
			long id = i;
			String title = "Bulk Test Book. " + i; //$NON-NLS-1$
			String author = "Nick Hornby"; //$NON-NLS-1$
			String press = "Penguin Books"; //$NON-NLS-1$
			double price = 21.30;

			Book book = createBook(id, title, author, press, price);
			books.add(book);
		}

		// save all
		try (PersistenceTransaction tx = freshTx(ioMode);) {
			ObjectDao objectDao = tx.getObjectDao();
			objectDao.addAll(books);
			books.clear();
		}

		// query all
		try (PersistenceTransaction tx = freshTx(ioMode);) {
			TypeRef typeRef = tx.getObjectRefCache().getTypeRef(TestConstants.TYPE_BOOK);
			ObjectDao objectDao = tx.getObjectDao();
			books = objectDao.queryAll(typeRef);
			assertEquals("Expected to find 10 entries!", 10, books.size()); //$NON-NLS-1$

			// delete them all
			objectDao.removeAll(books);
		}

		// now query them again
		try (PersistenceTransaction tx = freshTx(ioMode);) {
			TypeRef typeRef = tx.getObjectRefCache().getTypeRef(TestConstants.TYPE_BOOK);
			ObjectDao objectDao = tx.getObjectDao();
			books = objectDao.queryAll(typeRef);
			assertEquals("Expected to find 0 entries!", 0, books.size()); //$NON-NLS-1$
		}
	}

	@Test
	public void shouldPersistById() {
		setup(IoMode.SAX);

		String classType = TestConstants.TYPE_BOOK;
		long id = System.currentTimeMillis();
		String title = "About a boy"; //$NON-NLS-1$
		String author = "Nick Hornby"; //$NON-NLS-1$
		String press = "Penguin Books"; //$NON-NLS-1$
		double price = 21.30;

		// create a book
		try (PersistenceTransaction tx = this.persistenceManager.openTx()) {
			Book book = createBook(id, title, author, press, price);
			tx.getObjectDao().add(book);
		}

		// read by id
		try (PersistenceTransaction tx = this.persistenceManager.openTx()) {
			ObjectRef objectRef = tx.getObjectRefCache().getIdOfTypeRef(classType, Long.toString(id));
			Book book = tx.getObjectDao().queryById(objectRef);
			assertNotNull("Expected to read book by ID", book); //$NON-NLS-1$
		}

		// delete by id
		try (PersistenceTransaction tx = this.persistenceManager.openTx()) {
			ObjectRef objectRef = tx.getObjectRefCache().getIdOfTypeRef(classType, Long.toString(id));
			tx.getObjectDao().removeById(objectRef);
		}

		// fail to read by id
		try (PersistenceTransaction tx = this.persistenceManager.openTx()) {
			ObjectRef objectRef = tx.getObjectRefCache().getIdOfTypeRef(classType, Long.toString(id));
			Book book = tx.getObjectDao().queryById(objectRef);
			assertNull("Expected that book was deleted by ID, thus can not be read anymore", book); //$NON-NLS-1$
		}
	}
}
