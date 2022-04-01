package com.amanoteam.unalix.wrappers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

public class Unalix {

	final private static boolean DEFAULT_IGNORE_REFERRAL_MARKETING = false;
	final private static boolean DEFAULT_IGNORE_RULES = false;
	final private static boolean DEFAULT_IGNORE_EXCEPTIONS = false;
	final private static boolean DEFAULT_IGNORE_RAWRULES = false;
	final private static boolean DEFAULT_IGNORE_REDIRECTIONS = false;
	final private static boolean DEFAULT_SKIP_BLOCKED = false;

	final private static int DEFAULT_TIMEOUT = 3;
	final private static int DEFAULT_MAX_REDIRECTS = 13;
	final private static String DEFAULT_USER_AGENT = "UnalixAndroid (+https://github.com/AmanoTeam/UnalixAndroid)";

	final private static String DEFAULT_DNS = "";
	
	final private static String DEFAULT_PROXY = "";
	final private static String DEFAULT_PROXY_USERNAME = "";
	final private static String DEFAULT_PROXY_PASSWORD = "";
	
	static {
		System.loadLibrary("unalix_jni");
	}
	
	private boolean ignoreReferralMarketing;
	private boolean ignoreRules;
	private boolean ignoreExceptions;
	private boolean ignoreRawRules;
	private boolean ignoreRedirections;
	private boolean skipBlocked;
	
	private int timeout;
	private int maxRedirects;
	private String userAgent;
	
	private String dns;
	
	private String proxy;
	private String proxyUsername;
	private String proxyPassword;
	
	public Unalix() {
		setIgnoreReferralMarketing(DEFAULT_IGNORE_REFERRAL_MARKETING);
		setIgnoreRules(DEFAULT_IGNORE_RULES);
		setIgnoreExceptions(DEFAULT_IGNORE_EXCEPTIONS);
		setIgnoreRawRules(DEFAULT_IGNORE_RAWRULES);
		setIgnoreRedirections(DEFAULT_IGNORE_REDIRECTIONS);
		setSkipBlocked(DEFAULT_SKIP_BLOCKED);
		
		setTimeout(DEFAULT_TIMEOUT);
		setMaxRedirects(DEFAULT_MAX_REDIRECTS);
		setUserAgent(DEFAULT_USER_AGENT);
		
		setDns(DEFAULT_DNS);
		
		setProxy(DEFAULT_PROXY);
		setProxyUsername(DEFAULT_PROXY_USERNAME);
		setProxyPassword(DEFAULT_PROXY_PASSWORD);
	}

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
			final String userAgent,
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
				this.userAgent,
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
	
	private void setUserAgent(final String value) {
		this.userAgent = value;
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
		
		final String userAgent = preferences.getString("userAgent", "");
		final String customUserAgent = preferences.getString("customUserAgent", "");
		
		if (userAgent.equals("default")) {
			setUserAgent(DEFAULT_USER_AGENT);
		} else {
			setUserAgent((userAgent.equals("custom")) ? customUserAgent : userAgent);
		}
		
		final String dns = preferences.getString("dns", "");
		final String customDns = preferences.getString("customDns", "");
		
		if (dns.equals("follow_system")) {
			setDns(DEFAULT_DNS);
		} else {
			setDns((dns.equals("custom")) ? customDns : dns);
		}
		
		final boolean socks5Proxy = preferences.getBoolean("socks5Proxy", false);
		
		if (socks5Proxy) {
			final String proxyAddress = preferences.getString("proxyAddress", "");
			final int proxyPort = Integer.parseInt(preferences.getString("proxyPort", "8081"));
			
			if (TextUtils.isEmpty(proxyAddress)) {
				setProxy(DEFAULT_PROXY);
			} else {
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
		} else {
			setProxy(DEFAULT_PROXY);
			setProxyUsername(DEFAULT_PROXY_USERNAME);
			setProxyPassword(DEFAULT_PROXY_PASSWORD);
		}
		
	}
}