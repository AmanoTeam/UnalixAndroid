package com.amanoteam.libunalix;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.amanoteam.libunalix.exceptions.UnalixException;
import androidx.preference.PreferenceManager;

public class LibUnalix {

	final private static boolean DEFAULT_IGNORE_REFERRAL_MARKETING = false;
	final private static boolean DEFAULT_IGNORE_RULES = false;
	final private static boolean DEFAULT_IGNORE_EXCEPTIONS = false;
	final private static boolean DEFAULT_IGNORE_RAWRULES = false;
	final private static boolean DEFAULT_IGNORE_REDIRECTIONS = false;
	final private static boolean DEFAULT_STRIP_EMPTY = false;
	final private static boolean DEFAULT_STRIP_DUPLICATES = false;

	final private static int DEFAULT_TIMEOUT = 3;
	final private static String DEFAULT_USER_AGENT = "UnalixAndroid (+https://github.com/AmanoTeam/UnalixAndroid)";

	static {
		System.loadLibrary("unalix_jni");
	}

	private boolean ignoreReferralMarketing;
	private boolean ignoreRules;
	private boolean ignoreExceptions;
	private boolean ignoreRawRules;
	private boolean ignoreRedirections;
	private boolean stripEmpty;
	private boolean stripDuplicates;

	private int timeout;
	private String userAgent;


	public LibUnalix(final Context context) {
		setFromPreferences(context);
	}

	private native String cleanUrl(
			final String url,
			final boolean ignoreReferralMarketing,
			final boolean ignoreRules,
			final boolean ignoreExceptions,
			final boolean ignoreRawRules,
			final boolean ignoreRedirections,
			final boolean stripEmpty,
			final boolean stripDuplicates
	);

	public String cleanUrl(final String url) {
		return this.cleanUrl(
				url,
				this.ignoreReferralMarketing,
				this.ignoreRules,
				this.ignoreExceptions,
				this.ignoreRawRules,
				this.ignoreRedirections,
				this.stripEmpty,
				this.stripDuplicates
		);
	}

	private native String unshortUrl(
		final String url,
		final boolean ignoreReferralMarketing,
		final boolean ignoreRules,
		final boolean ignoreExceptions,
		final boolean ignoreRawRules,
		final boolean ignoreRedirections,
		final boolean stripEmpty,
		final boolean stripDuplicates,
		final String userAgent,
		final int timeout
	);

	public String unshortUrl(final String url) {
		return this.unshortUrl(
			url,
			this.ignoreReferralMarketing,
			this.ignoreRules,
			this.ignoreExceptions,
			this.ignoreRawRules,
			this.ignoreRedirections,
			this.stripEmpty,
			this.stripDuplicates,
			this.userAgent,
			this.timeout
		);
	}

	public native boolean rulesetCheckUpdate(
		final String filename,
		final String url
	) throws UnalixException;
	
	public native boolean rulesetUpdate(
		final String filename,
		final String url,
		final String sha256_url,
		final String temporary_directory
	) throws UnalixException;

	private void setIgnoreReferralMarketing(final boolean value) {
		this.ignoreReferralMarketing = value;
	}

	private void setIgnoreRules(final boolean value) {
		this.ignoreRules = value;
	}

	private void setIgnoreExceptions(final boolean value) {
		this.ignoreExceptions = value;
	}

	private void setIgnoreRawRules(final boolean value) {
		this.ignoreRawRules = value;
	}

	private void setIgnoreRedirections(final boolean value) {
		this.ignoreRedirections = value;
	}

	private void setStripEmpty(final boolean value) {
		this.stripEmpty = value;
	}
	
	private void setStripDuplicates(final boolean value) {
		this.stripDuplicates = value;
	}

	private void setUserAgent(final String value) {
		this.userAgent = value;
	}
	
	private void setTimeout(final int value) {
		this.timeout = value;
	}

	public void setFromPreferences(final Context context) {

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

		final boolean ignoreReferralMarketing = preferences.getBoolean("ignoreReferralMarketing", false);
		setIgnoreReferralMarketing(ignoreReferralMarketing);

		final boolean ignoreRules = preferences.getBoolean("ignoreRules", false);
		setIgnoreRules(ignoreRules);

		final boolean ignoreExceptions = preferences.getBoolean("ignoreExceptions", false);
		setIgnoreExceptions(ignoreExceptions);

		final boolean ignoreRawRules = preferences.getBoolean("ignoreRawRules", false);
		setIgnoreRawRules(ignoreRawRules);

		final boolean ignoreRedirections = preferences.getBoolean("ignoreRedirections", false);
		setIgnoreRedirections(ignoreRedirections);

		final boolean stripEmpty = preferences.getBoolean("stripEmpty", false);
		setStripEmpty(stripEmpty);
		
		final boolean stripDuplicates = preferences.getBoolean("stripDuplicates", false);
		setStripDuplicates(stripDuplicates);

		final int timeout = Integer.parseInt(preferences.getString("timeout", "3"));
		setTimeout(timeout);

		final String userAgent = preferences.getString("userAgent", "");
		final String customUserAgent = preferences.getString("customUserAgent", "");

		if (userAgent.equals("default")) {
			setUserAgent(DEFAULT_USER_AGENT);
		} else {
			setUserAgent((userAgent.equals("custom")) ? customUserAgent : userAgent);
		}

	}
}
