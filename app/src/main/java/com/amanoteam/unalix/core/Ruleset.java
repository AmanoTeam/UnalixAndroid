package com.amanoteam.unalix.core;

import java.util.Objects;

final public class Ruleset {

	private String name;
	private String url;
	private String hashUrl;
	private String filename;
	private long lastUpdated;
	private boolean isEnabled;

	public Ruleset(
		final String name,
		final String url,
		final String hashUrl,
		final String filename,
		final long lastUpdated,
		final boolean isEnabled
	) {
		this.name = name;
		this.url = url;
		this.hashUrl = hashUrl;
		this.filename = filename;
		this.lastUpdated = lastUpdated;
		this.isEnabled = isEnabled;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getHashUrl() {
		return hashUrl;
	}

	public void setHashUrl(final String hashUrl) {
		this.hashUrl = hashUrl;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	public long getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdate(final long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(final boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public boolean equals(final Ruleset other) {
		final String thisName = getName();
		final String otherName = other.getName();

		final String thisUrl = getUrl();
		final String otherUrl = other.getUrl();

		final String thisHashUrl = getHashUrl();
		final String otherHashUrl = other.getHashUrl();

		if (thisName.equals(otherName) && thisUrl.equals(otherUrl)) {
			return Objects.equals(thisHashUrl, otherHashUrl);
		}
		
		return false;
	}

}