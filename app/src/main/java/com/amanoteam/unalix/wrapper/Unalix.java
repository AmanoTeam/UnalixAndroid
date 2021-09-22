package com.amanoteam.this.wrapper;

import com.sun.jna.Native;
import com.sun.jna.Library;

public class Unalix {

	private boolean ignoreReferralMarketing = false;
	private boolean ignoreRules = false;
	private boolean ignoreExceptions = false;
	private boolean ignoreRawRules = false;
	private boolean ignoreRedirections = false;
	private boolean skipBlocked = false;
	private boolean stripDuplicates = false;
	private boolean stripEmpty = false;
	
	private String httpMethod = "GET";
	private boolean parseDocuments = false;
	private int httpMaxRedirects = 13;
	private int httpTimeout = 3000;
	private int httpMaxFetchSize = 1024 * 1024;
	private int httpMaxRetries = 0;
	
	public Unalix setIgnoreReferralMarketing(final boolean value) {
		this.ignoreReferralMarketing = value;
		return this;
	}

	public Unalix setIgnoreRules(final boolean value) {
		this.ignoreRules = value;
		return this;
	}

	public Unalix setIgnoreExceptions(final boolean value) {
		this.ignoreExceptions = value;
		return this;
	}

	public Unalix setRawRules(final boolean value) {
		this.ignoreRawRules = value;
		return this;
	}

	public Unalix setIgnoreRedirections(final boolean value) {
		this.ignoreRedirections = value;
		return this;
	}

	public Unalix setSkipBlocked(final boolean value) {
		this.skipBlocked = value;
		return this;
	}

	public Unalix setStripDuplicates(final boolean value) {
		this.stripDuplicates = value;
		return this;
	}

	public Unalix setStripEmpty(final boolean value) {
		this.stripEmpty = value;
		return this;
	}
	
	public Unalix setHttpMethod(final String value) {
		this.httpMethod = value;
		return this;
	}
	
	public Unalix setParseDocuments(final boolean value) {
		this.parseDocuments = value;
		return this;
	}
	
	public Unalix setHttpMaxRedirects(final int value) {
		this.httpMaxRedirects = value;
		return this;
	}
	
	public Unalix setHttpTimeout(final int value) {
		this.httpTimeout = value;
		return this;
	}
	
	public Unalix setHttpMaxFetchSize(final int value) {
		this.httpMaxFetchSize = value;
		return this;
	}
	
	public Unalix setHttpMaxRetries(final int value) {
		this.httpMaxRetries = value;
		return this;
	}
	
	public interface UnalixCLibrary extends Library {
		final UnalixCLibrary instance = (UnalixCLibrary) Native.load("unalix", UnalixCLibrary.class);

		void NimMain();

		String clearUrl(
			String url,
			boolean ignoreReferralMarketing,
			boolean ignoreRules,
			boolean ignoreExceptions,
			boolean ignoreRawRules,
			boolean ignoreRedirections,
			boolean skipBlocked,
			boolean stripDuplicates,
			boolean stripEmpty
		);
		
		String unshortUrl(
			String url,
			boolean ignoreReferralMarketing,
			boolean ignoreRules,
			boolean ignoreExceptions,
			boolean ignoreRawRules,
			boolean ignoreRedirections,
			boolean skipBlocked,
			boolean stripDuplicates,
			boolean stripEmpty,
			String httpMethod,
			boolean parseDocuments,
			int httpMaxRedirects,
			int httpTimeout,
			int httpMaxFetchSize,
			int httpMaxRetries
		);
		
	}
	
	public String clearUrl(final String url) {
		return UnalixCLibrary.instance.clearUrl(
			url,
			this.ignoreReferralMarketing,
			this.ignoreRules,
			this.ignoreExceptions,
			this.ignoreRawRules,
			this.ignoreRedirections,
			this.skipBlocked,
			this.stripDuplicates,
			this.stripEmpty
		);
	}
	
	public String unshortUrl(final String url) {
		return UnalixCLibrary.instance.unshortUrl(
			url,
			this.ignoreReferralMarketing,
			this.ignoreRules,
			this.ignoreExceptions,
			this.ignoreRawRules,
			this.ignoreRedirections,
			this.skipBlocked,
			this.stripDuplicates,
			this.stripEmpty,
			this.httpMethod,
			this.parseDocuments,
			this.httpMaxRedirects,
			this.httpTimeout,
			this.httpMaxFetchSize,
			this.httpMaxRetries
		);
	}
	
	public void setFromPreferences(final SharedPreferences preferences);
		
		this.setIgnoreReferralMarketing(
			preferences.getBoolean("ignoreReferralMarketing", false)
		);
		
		this.setIgnoreRules(
			preferences.getBoolean("ignoreRules", false)
		);
		
		this.setIgnoreExceptions(
			preferences.getBoolean("ignoreExceptions", false)
		);
		
		this.setRawRules(
			preferences.getBoolean("ignoreRawRules", false)
		);
		
		this.setIgnoreRedirections(
			preferences.getBoolean("ignoreRedirections", false)
		);
		
		this.setSkipBlocked(
			preferences.getBoolean("skipBlocked", false)
		);
		
		this.setStripDuplicates(
			preferences.getBoolean("stripDuplicates", false)
		);
		
		this.setStripEmpty(
			preferences.getBoolean("stripEmpty", false)
		);
		
		this.setHttpMethod(
			preferences.getString("httpMethod", "GET")
		);
		
		this.setParseDocuments(
			preferences.getBoolean("parseDocuments", false)
		);
		
		this.setHttpMaxRedirects(
			Integer.valueOf(preferences.getString("httpMaxRedirects", "13"))
		);
		
		this.setHttpTimeout(
			Integer.valueOf(preferences.getString("httpTimeout", "3000"))
		);
		
		this.setHttpMaxFetchSize(
			Integer.valueOf(preferences.getString("httpMaxFetchSize", String.valueOf(1024 * 1024)))
		);
		
		this.setHttpMaxRetries(
			Integer.valueOf(preferences.getString("httpMaxRetries", "0"))
		);
	}
	
	public Unalix() {
		UnalixCLibrary.instance.NimMain();
	};
	
 }
