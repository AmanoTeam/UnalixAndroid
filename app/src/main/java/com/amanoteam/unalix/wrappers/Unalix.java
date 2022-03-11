package com.amanoteam.unalix.wrappers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class Unalix {

	static {
		System.loadLibrary("unalix_jni");
	}

	private boolean ignoreReferralMarketing = false;
	private boolean ignoreRules = false;
	private boolean ignoreExceptions = false;
	private boolean ignoreRawRules = false;
	private boolean ignoreRedirections = false;
	private boolean skipBlocked = false;
	private int timeout = 3000;
	private int maxRedirects = 13;

	private native String clearUrl(
			final String url,
			final boolean ignoreReferralMarketing,
			final boolean ignoreRules,
			final boolean ignoreExceptions,
			final boolean ignoreRawRules,
			final boolean ignoreRedirections,
			final boolean skipBlocked
	);

	public String clearUrl(final String url) {
		return this.clearUrl(
				url,
				this.ignoreReferralMarketing,
				this.ignoreRules,
				this.ignoreExceptions,
				this.ignoreRawRules,
				this.ignoreRedirections,
				this.skipBlocked
		);
	}

	private native String unshortUrl(
			final String url,
			final boolean ignoreReferralMarketing,
			final boolean ignoreRules,
			final boolean ignoreExceptions,
			final boolean ignoreRawRules,
			final boolean ignoreRedirections,
			final boolean skipBlocked,
			final int timeout,
			final int maxRedirects
	);

	public String unshortUrl(final String url) {
		return this.unshortUrl(
				url,
				this.ignoreReferralMarketing,
				this.ignoreRules,
				this.ignoreExceptions,
				this.ignoreRawRules,
				this.ignoreRedirections,
				this.skipBlocked,
				this.timeout,
				this.maxRedirects
		);
	}

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

	private void setSkipBlocked(final boolean value) {
		this.skipBlocked = value;
	}

	private void setTimeout(final int value) {
		this.timeout = value;
	}

	private void setMaxRedirects(final int value) {
		this.maxRedirects = value;
	}

	public void setFromPreferences(final Context context) {

		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		final boolean ignoreReferralMarketing = settings.getBoolean("ignoreReferralMarketing", false);
		setIgnoreReferralMarketing(ignoreReferralMarketing);

		final boolean ignoreRules = settings.getBoolean("ignoreRules", false);
		setIgnoreRules(ignoreRules);

		final boolean ignoreExceptions = settings.getBoolean("ignoreExceptions", false);
		setIgnoreExceptions(ignoreExceptions);

		final boolean ignoreRawRules = settings.getBoolean("ignoreRawRules", false);
		setIgnoreRawRules(ignoreRawRules);

		final boolean ignoreRedirections = settings.getBoolean("ignoreRedirections", false);
		setIgnoreRedirections(ignoreRedirections);

		final boolean skipBlocked = settings.getBoolean("skipBlocked", false);
		setSkipBlocked(skipBlocked);

		final int timeout = Integer.parseInt(settings.getString("timeout", "3000"));
		setTimeout(timeout);

		final int maxRedirects = Integer.parseInt(settings.getString("maxRedirects", "13"));
		setMaxRedirects(maxRedirects);

	}

}