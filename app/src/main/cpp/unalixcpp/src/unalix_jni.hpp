#include <jni.h>

extern "C"

JNIEXPORT jstring JNICALL Java_com_amanoteam_unalix_wrappers_Unalix_clearUrl
	(
		JNIEnv* env,
		jobject obj,
		jstring url,
		jboolean ignoreReferralMarketing,
		jboolean ignoreRules,
		jboolean ignoreExceptions,
		jboolean ignoreRawRules,
		jboolean ignoreRedirections,
		jboolean skipBlocked
	);

extern "C"

JNIEXPORT jstring JNICALL Java_com_amanoteam_unalix_wrappers_Unalix_unshortUrl
	(
		JNIEnv* env,
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
		jstring proxy_username,
		jstring proxy_password
	);
