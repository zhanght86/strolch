package li.strolch.model;

import java.text.MessageFormat;
import java.util.Date;

import li.strolch.utils.dbc.DBC;
import li.strolch.utils.iso8601.ISO8601FormatFactory;

/**
 * <p>
 * Defines the version of a {@link StrolchRootElement}. The version of an object allows to store the history of changes
 * as an absolute change, not differential.
 * </p>
 * 
 * <p>
 * Versions have an integer value, which is incremented for each further version. Thus to retrieve the previous version,
 * decrement the objects current version. To find the next version increment the version.
 * </p>
 * 
 * <p>
 * A version has a flag <code>delete</code> which, if true, designates that this version was removed
 * </p>
 * 
 * <p>
 * A {@link Version} is immutable
 * </p>
 * 
 * @author Robert von Burg <eitch@eitchnet.ch>
 */
public class Version {

	private final Locator locator;
	private final int version;
	private final String createdBy;
	private final Date createdAt;
	private final boolean deleted;

	/**
	 * Creates a new version instance with the given values. The creation date is now.
	 * 
	 * @param version
	 *            the integer version which must be >= 0 and should be incremented for each new version of an object
	 * @param createdBy
	 *            the username of the creator of this object
	 */
	public Version(Locator locator, int version, String createdBy, boolean deleted) {
		DBC.PRE.assertTrue("Version must by >= 0", version >= 0);
		DBC.PRE.assertNotNull("locator must be set!", locator);
		DBC.PRE.assertNotNull("createdBy must be set!", createdBy);
		this.locator = locator;
		this.version = version;
		this.createdBy = createdBy;
		this.createdAt = new Date();
		this.deleted = deleted;
	}

	public Locator getLocator() {
		return this.locator;
	}

	/**
	 * Returns the integer version, which is >= 0
	 * 
	 * @return the version
	 */
	public int getVersion() {
		return this.version;
	}

	/**
	 * Returns the current version incremented
	 * 
	 * @return the current version incremented
	 */
	public int getNextVersion() {
		return this.version + 1;
	}

	/**
	 * Returns true if this version == 0
	 * 
	 * @return true if this version == 0
	 */
	public boolean isFirstVersion() {
		return this.version == 0;
	}

	/**
	 * Returns the current version decremented
	 * 
	 * @return the current version decremented
	 * 
	 * @throws IllegalStateException
	 *             if this version is already the first version
	 */
	public int getPreviousVersion() throws IllegalStateException {
		if (this.version == 0)
			throw new IllegalStateException(
					"This is the first version, no previous version available for " + this.locator);
		return this.version - 1;
	}

	/**
	 * Returns the username of the creator
	 * 
	 * @return the username of the creator
	 */
	public String getCreatedBy() {
		return this.createdBy;
	}

	/**
	 * Returns the date when this version was created
	 * 
	 * @return the date when this version was created
	 */
	public Date getCreatedAt() {
		return this.createdAt;
	}

	/**
	 * Returns true if this version was deleted, otherwise false
	 * 
	 * @return true if this version was deleted, otherwise false
	 */
	public boolean isDeleted() {
		return this.deleted;
	}

	/**
	 * Validates that the given argument is a newer version to this version
	 * 
	 * @param other
	 *            the other version to check
	 * 
	 * @throws IllegalArgumentException
	 *             if the given argument's locator is not equal to this version's locator
	 * @throws IllegalStateException
	 *             if the given argument is not the next version
	 */
	public void validateIsNewer(Version other) throws IllegalArgumentException, IllegalStateException {
		if (!this.locator.equals(other.locator)) {
			String msg = "Other version {0} is not for same object: {1}";
			throw new IllegalArgumentException(MessageFormat.format(msg, other, this.version));
		}

		if (this.version >= other.version) {
			String msg = "Other version: {0} is a newer version of this version: {1}";
			throw new IllegalArgumentException(MessageFormat.format(msg, other, this.version));
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Version [version=");
		builder.append(this.version);
		builder.append(", locator=");
		builder.append(this.locator);
		builder.append(", createdBy=");
		builder.append(this.createdBy);
		builder.append(", createdAt=");
		builder.append(ISO8601FormatFactory.getInstance().formatDate(this.createdAt));
		builder.append(", deleted=");
		builder.append(this.deleted);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Returns the next version, i.e. this version incremented by 1
	 * 
	 * @param username
	 *            the username to set
	 * @param deleted
	 *            the deleted flag to set
	 * 
	 * @return the next version
	 */
	public Version next(String username, boolean deleted) {
		return new Version(this.locator, getNextVersion(), username, deleted);
	}

	/**
	 * Sets the initial version = 0 for the given element which is also set to not deleted
	 * 
	 * @param element
	 *            the element for which to create a new version
	 * @param username
	 *            the username of the user who created this version of the object
	 */
	public static void setInitialVersionFor(StrolchRootElement element, String username) {
		Version version = new Version(element.getLocator(), 0, username, false);
		element.setVersion(version);
	}

	/**
	 * Sets a new version on the given element. If the element has no version yet, then the result will be version 0,
	 * otherwise the version will be an increment to the current version
	 * 
	 * @param element
	 *            the element for which to create a new version
	 * @param username
	 *            the username of the user who created this version of the object
	 * @param deleted
	 *            if true, then the version will be marked as deleted, i.e. this object was removed from the element
	 *            maps
	 */
	public static void updateVersionFor(StrolchRootElement element, String username, boolean deleted) {
		int v = element.getVersion() == null ? 0 : element.getVersion().getVersion() + 1;
		Version version = new Version(element.getLocator(), v, username, deleted);
		element.setVersion(version);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.deleted ? 1231 : 1237);
		result = prime * result + ((this.locator == null) ? 0 : this.locator.hashCode());
		result = prime * result + this.version;
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
		Version other = (Version) obj;
		if (this.deleted != other.deleted)
			return false;
		if (this.locator == null) {
			if (other.locator != null)
				return false;
		} else if (!this.locator.equals(other.locator))
			return false;
		if (this.version != other.version)
			return false;
		return true;
	}
}
