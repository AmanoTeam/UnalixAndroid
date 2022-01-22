import std/asyncdispatch
import std/net

import pkg/jnim
import pkg/unalix

proc clearUrl(
    env: ptr JNIEnv,
    obj: jobject,
    url: jstring,
    ignoreReferralMarketing: jboolean,
    ignoreRules: jboolean,
    ignoreExceptions: jboolean,
    ignoreRawRules: jboolean,
    ignoreRedirections: jboolean,
    skipBlocked: jboolean,
    stripDuplicates: jboolean,
    stripEmpty: jboolean
): jstring {.cdecl, exportc: "Java_com_amanoteam_unalix_wrappers_Unalix_clearUrl", dynlib.} =

    result = env.NewStringUTF(
        env = env,
        s = clearUrl(
            url = $env.GetStringUTFChars(env = env, s = url, isCopy = nil),
            ignoreReferralMarketing = bool(ignoreReferralMarketing),
            ignoreRules = bool(ignoreRules),
            ignoreExceptions = bool(ignoreExceptions),
            ignoreRawRules = bool(ignoreRawRules),
            ignoreRedirections = bool(ignoreRedirections),
            skipBlocked = bool(skipBlocked),
            stripDuplicates = bool(stripDuplicates),
            stripEmpty = bool(stripEmpty)
        ).cstring
    )

proc unshortUrl(
    env: ptr JNIEnv,
    obj: jobject,
    url: jstring,
    ignoreReferralMarketing: jboolean,
    ignoreRules: jboolean,
    ignoreExceptions: jboolean,
    ignoreRawRules: jboolean,
    ignoreRedirections: jboolean,
    skipBlocked: jboolean,
    stripDuplicates: jboolean,
    stripEmpty: jboolean,
    parseDocuments: jboolean,
    timeout: jint,
    maxRedirects: jint
): jstring {.cdecl, exportc: "Java_com_amanoteam_unalix_wrappers_Unalix_unshortUrl", dynlib.} =
    
    let sslContext: SslContext = newContext(caDir = "/system/etc/security/cacerts")
    
    try:
        return env.NewStringUTF(
            env = env,
            s = (
                waitFor aunshortUrl(
                    url = $env.GetStringUTFChars(env = env, s = url, isCopy = nil),
                    ignoreReferralMarketing = bool(ignoreReferralMarketing),
                    ignoreRules = bool(ignoreRules),
                    ignoreExceptions = bool(ignoreExceptions),
                    ignoreRawRules = bool(ignoreRawRules),
                    ignoreRedirections = bool(ignoreRedirections),
                    skipBlocked = bool(skipBlocked),
                    stripDuplicates = bool(stripDuplicates),
                    stripEmpty = bool(stripEmpty),
                    parseDocuments = bool(parseDocuments),
                    timeout = int(timeout),
                    maxRedirects = int(maxRedirects),
                    sslContext = sslContext
                )
            ).cstring
        )
    except ConnectError as e:
        return env.NewStringUTF(env = env, s = (e.url).cstring)
