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
	
	const char *str = env -> GetStringUTFChars(url, NULL);
	
	const std::string result = clear_url(
		str,
		(bool) ignoreReferralMarketing,
		(bool) ignoreRules,
		(bool) ignoreExceptions,
		(bool) ignoreRawRules,
		(bool) ignoreRedirections,
		(bool) skipBlocked
	);
	
	env -> ReleaseStringUTFChars(url, str);
	
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
	jint maxRedirects
) {
	
	const char *str = env -> GetStringUTFChars(url, NULL);
	std::string result;
	
	try {
	 	result = unshort_url(
			str,
			(bool) ignoreReferralMarketing,
			(bool) ignoreRules,
			(bool) ignoreExceptions,
			(bool) ignoreRawRules,
			(bool) ignoreRedirections,
			(bool) skipBlocked,
			(int) timeout,
			(int) maxRedirects
		);
	} catch (const UnalixException e) {
		result = e.get_url();
	}
	
	env -> ReleaseStringUTFChars(url, str);
	
	return env -> NewStringUTF(result.c_str());
}