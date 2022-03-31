#include <string>

#include <jni.h>

#include "unalix.hpp"
#include "unalix_jni.hpp"

jstring Java_com_amanoteam_unalix_wrappers_Unalix_clearUrl (
	JNIEnv *env,
	jobject obj,
	jstring url,
	jboolean ignoreReferralMarketing,
	jboolean ignoreRules,
	jboolean ignoreExceptions,
	jboolean ignoreRawRules,
	jboolean ignoreRedirections,
	jboolean skipBlocked
) {
	
	const char *url_ = env -> GetStringUTFChars(url, NULL);
	
	const std::string result = clear_url(
		url_,
		(bool) ignoreReferralMarketing,
		(bool) ignoreRules,
		(bool) ignoreExceptions,
		(bool) ignoreRawRules,
		(bool) ignoreRedirections,
		(bool) skipBlocked
	);
	
	env -> ReleaseStringUTFChars(url, url_);
	
	return env -> NewStringUTF(result.c_str());
}

jstring Java_com_amanoteam_unalix_wrappers_Unalix_unshortUrl (
	JNIEnv *env,
	jobject obj,
	jstring url,
	jboolean ignoreReferralMarketing,
	jboolean ignoreRules,
	jboolean ignoreExceptions,
	jboolean ignoreRawRules,
	jboolean ignoreRedirections,
	jboolean skipBlocked,
	jint timeout,
	jint maxRedirects,
	jstring userAgent,
	jstring dns,
	jstring proxy,
	jstring proxyUsername,
	jstring proxyPassword
) {
	
	const char *url_ = env -> GetStringUTFChars(url, NULL);
	const char *userAgent_ = env -> GetStringUTFChars(userAgent, NULL);
	const char *dns_ = env -> GetStringUTFChars(dns, NULL);
	const char *proxy_ = env -> GetStringUTFChars(proxy, NULL);
	const char *proxyUsername_ = env -> GetStringUTFChars(proxyUsername, NULL);
	const char *proxyPassword_ = env -> GetStringUTFChars(proxyPassword, NULL);
	
	std::string result;
	
	try {
	 	result = unshort_url(
			url_,
			(bool) ignoreReferralMarketing,
			(bool) ignoreRules,
			(bool) ignoreExceptions,
			(bool) ignoreRawRules,
			(bool) ignoreRedirections,
			(bool) skipBlocked,
			(int) timeout,
			(int) maxRedirects,
			userAgent_,
			dns_,
			proxy_,
			proxyUsername_,
			proxyPassword_
		);
	} catch (const UnalixException e) {
		result = e.get_url();
	}
	
	env -> ReleaseStringUTFChars(url, url_);
	env -> ReleaseStringUTFChars(userAgent, userAgent_);
	env -> ReleaseStringUTFChars(dns, dns_);
	env -> ReleaseStringUTFChars(proxy, proxy_);
	env -> ReleaseStringUTFChars(proxyUsername, proxyUsername_);
	env -> ReleaseStringUTFChars(proxyPassword, proxyPassword_);
	
	return env -> NewStringUTF(result.c_str());
}