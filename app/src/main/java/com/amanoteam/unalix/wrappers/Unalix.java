package com.amanoteam.unalix.wrappers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

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
	
	private int timeout = 3;
	private int maxRedirects = 13;
	
	private String dns = "";
	
	private String proxy = "";
	private String proxyUsername = "";
	private String proxyPassword = "";

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
			final int maxRedirects,
			final String dns,
			final String proxy,
			final String proxyUsername,
			final String proxyPassword
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
				this.maxRedirects,
				this.dns,
				this.proxy,
				this.proxyUsername,
				this.proxyPassword
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

	private void setDns(final String value) {
		this.dns = value;
	}
	
	private void setProxy(final String value) {
		this.proxy = value;
	}
	
	private void setProxyUsername(final String value) {
		this.proxyUsername = value;
	}
	
	private void setProxyPassword(final String value) {
		this.proxyPassword = value;
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

		final boolean skipBlocked = preferences.getBoolean("skipBlocked", false);
		setSkipBlocked(skipBlocked);

		final int timeout = Integer.parseInt(preferences.getString("timeout", "3"));
		setTimeout(timeout);

		final int maxRedirects = Integer.parseInt(preferences.getString("maxRedirects", "13"));
		setMaxRedirects(maxRedirects);
		
		final String dns = preferences.getString("dns", "");
		final String customDns = preferences.getString("customDns", "");
		
		if (dns.equals("follow_system")) {
			setDns("");
		} else if (dns.equals("custom")) {
			if (!customDns.equals("")) {
				setDns(customDns);
			}
		} else {
			setDns(dns);
		}
		
		final boolean socks5Proxy = preferences.getBoolean("socks5Proxy", false);
		
		if (socks5Proxy) {
			final String proxyAddress = preferences.getString("proxyAddress", "");
			final int proxyPort = Integer.parseInt(preferences.getString("proxyPort", "8081"));
			
			if (!TextUtils.isEmpty(proxyAddress)) {
				final String proxy = String.format("socks5://%s:%d", proxyAddress, proxyPort);
				setProxy(proxy);
				
				final boolean proxyAuthentication = preferences.getBoolean("proxyAuthentication", false);
				
				if (proxyAuthentication) {
					final String proxyUsername = preferences.getString("proxyUsername", "");
					setProxyUsername(proxyUsername);
					
					final String proxyPassword = preferences.getString("proxyPassword", "");
					setProxyPassword(proxyPassword);
				}
			}
		}
		
	}
}