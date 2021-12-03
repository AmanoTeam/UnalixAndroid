import std/parseopt
import std/strutils
import std/os
import std/sugar
import std/strformat

import ./utils

const
    toolVersion: string = "0.1"
    helpMessage: string = "Usage: tool ACTION <LIBRARY-NAME> [PARAMETERS]..."
    repository: string = staticExec(command = "git config --get remote.origin.url")
    commit: string = staticExec(command = "git rev-parse --short HEAD")
    versionInfo: string = &"tool v{toolVersion} ({repository}@{commit})\n" &
        &"Compiled for {hostOS} ({hostCPU}) using Nim {NimVersion} " &
        &"({CompileDate}, {CompileTime})\n"

setControlCHook(
    hook = proc(): void {.noconv.} =
        quit(0)
)

var parser: OptParser = initOptParser()

var arguments: seq[string] = newSeq[string]()

var architecture: string

while true:
    parser.next()

    case parser.kind
    of cmdEnd:
        break
    of cmdShortOption, cmdLongOption:
        case parser.key
        of "version", "v":
            writeStdout(s = versionInfo, exitCode = 0)
        of "help", "h":
            writeStdout(s = helpMessage, exitCode = 0)
        of "a", "architecture":
            architecture = parser.val
            
            if architecture.isEmptyOrWhitespace():
                writeStderr(s = &"fatal: missing required value for argument: --architecture", exitCode = 1)
            
            if architecture notin ["arm", "arm64", "i386", "amd64"]:
                writeStderr(s = &"fatal: unsupported build architecture: {architecture}", exitCode = 1)
        else:
            writeStderr(s = &"faltal: unrecognized argument: " & getPrefixedArgument(parser.key), exitCode = 1)
    of cmdArgument:
        if len(arguments) == 2:
            writeStderr(s = "faltal: too many arguments", exitCode = 1)
        
        arguments.add(parser.key)

if len(arguments) > 2:
    writeStderr(s = "faltal: too many arguments", exitCode = 1)
elif len(arguments) < 2:
    writeStderr(s = "faltal: argument list too short", exitCode = 1)

let
    command: string = arguments[0]
    target: string = arguments[1]

case command
of "download":
    case target
    of "pcre":
        downloadFile(
            url = "https://megalink.dl.sourceforge.net/project/pcre/pcre/8.45/pcre-8.45.tar.gz",
            filename = getTempDir() / "pcre.tgz"
        )
    of "libressl":
        downloadFile(
            url = "https://cdn.openbsd.org/pub/OpenBSD/LibreSSL/libressl-3.4.2.tar.gz",
            filename = getTempDir() / "libressl.tgz"
        )
    else:
        writeStderr(
            s = &"faltal: unknown library name: {target}",
            exitCode = 1
        )
of "patch":
    case target
    of "libressl":
        if not dirExists(dir = "../libressl"):
            writeStderr(
                s = "faltal: no source directory found: did you forget to run './tool download libressl'?",
                exitCode = 1
            )

        echoAndRun(command = "patch --force --strip=0 --input=../patches/libressl/crypto-x509-by_dir.c.patch --directory=../libressl")
    else:
        writeStderr(
            s = &"faltal: unknown library name: {target}",
            exitCode = 1
        )
of "build":
    let toolchain: string = getEnv(key = "ANDROID_NDK")
    
    if toolchain.isEmptyOrWhitespace():
        writeStderr(
            s = "faltal: ANDROID_NDK is not defined",
            exitCode = 1
        )
    
    if not dirExists(dir = toolchain):
        writeStderr(
            s = "faltal: ANDROID_NDK points to an invalid location",
            exitCode = 1
        )
    
    if architecture.isEmptyOrWhitespace():
        writeStderr(
            s = "faltal: missing required argument: -a/--architecture",
            exitCode = 1
        )
    
    var
        CC, CXX, HOST, JNI_LIBS: string
    
    case architecture
    of "arm":
        CC = toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/armv7a-linux-androideabi21-clang"
        CXX = toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/armv7a-linux-androideabi21-clang++"
        HOST = "armv7a-linux-androideabi"
        JNI_LIBS = absolutePath(path = "../../" / "jniLibs/armeabi-v7a")
    of "arm64":
        CC = toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android21-clang"
        CXX = toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android21-clang++"
        HOST = "aarch64-linux-android"
        JNI_LIBS = absolutePath(path = "../../" / "jniLibs/arm64-v8a")
    of "i386":
        CC = toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/i686-linux-android21-clang"
        CXX = toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/i686-linux-android21-clang++"
        HOST = "i686-linux-android"
        JNI_LIBS = absolutePath(path = "../../" / "jniLibs/x86")
    of "amd64":
        CC = toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/x86_64-linux-android21-clang"
        CXX = toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/x86_64-linux-android21-clang++"
        HOST = "x86_64-linux-android"
        JNI_LIBS = absolutePath(path = "../../" / "jniLibs/x86_64")
    else:
        discard
    
    let flags: seq[(string, string)] = @[
        ("CC", CC),
        ("CXX", CXX),
        ("AR", toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-ar"),
        ("AS", toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-as"),
        ("LD", toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/ld"),
        ("LIPO", toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-lipo"),
        ("RANLIB", toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-ranlib"),
        ("OBJCOPY", toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-objcopy"),
        ("OBJDUMP", toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-objdump"),
        ("STRIP", toolchain / "toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-strip"),
        ("CFLAGS", "-s -DNDEBUG -Ofast -w -Wfatal-errors -fpic -flto=full -D__ANDROID__ -D__ANDROID_API__=21"),
        ("CCFLAGS", "-s -DNDEBUG -Ofast -w -Wfatal-errors -fpic -flto=full -D__ANDROID__ -D__ANDROID_API__=21"),
        ("CXXFLAGS", "-s -DNDEBUG -Ofast -w -Wfatal-errors -fpic -flto=full -D__ANDROID__ -D__ANDROID_API__=21"),
    ]
    
    let CONFIGURE_FLAGS: string = (
        block: collect newSeq: (for (key, value) in flags: &"{key}='{value}'")
    ).join(sep = " ")
    
    case target
    of "pcre":
        if not dirExists(dir = "../pcre"):
            writeStderr(
                s = "faltal: no source directory found: did you forget to run './tool download pcre'?",
                exitCode = 1
            )
        
        setCurrentDir(newDir = "../pcre")
        
        if fileExists(filename = "config.status"):
            echoAndRun(command = "make distclean")
        
        echoAndRun(command = &"./configure --silent --host='{HOST}' {CONFIGURE_FLAGS}")
        echoAndRun(command = "make --jobs --silent")
        
        moveFile(source = "./" / ".libs/libpcre.so", dest = JNI_LIBS / "libpcre.so")
        
        echoAndRun(command = "make distclean --silent")
    of "libressl":
        if not dirExists(dir = "../libressl"):
            writeStderr(
                s = "faltal: no source directory found: did you forget to run './tool download libressl'?",
                exitCode = 1
            )
        
        setCurrentDir(newDir = "../libressl")
        
        if fileExists(filename = "config.status"):
            echoAndRun(command = "make distclean")
        
        echoAndRun(command = &"./configure --silent --host='{HOST}' {CONFIGURE_FLAGS}")
        echoAndRun(command = "make --jobs --silent")

        moveFile(source = expandFilename(filename = "./" / "crypto/.libs/libcrypto.so"), dest = JNI_LIBS / "libcrypto.so")
        moveFile(source = expandFilename(filename = "./" / "ssl/.libs/libssl.so"), dest = JNI_LIBS / "libssl.so")
        
        echoAndRun(command = "make distclean --silent")
    of "wrapper":
        if not dirExists(dir = "../wrapper"):
            writeStderr(s = "faltal: no source directory found", exitCode = 1)
        
        setCurrentDir(newDir = "../wrapper")
        
        echoAndRun(command = "nimble install --accept")
        
        let nimCompilerFlags: seq[(string, string)] = @[
            ("clang.exe", CC),
            ("clang.linkerexe", CC),
            ("os", "android"),
            ("cpu", architecture),
            ("define", "release"),
            ("define", "strip"),
            ("define", "danger"),
            ("define", "ssl"),
            ("define", "libressl"),
            ("define", "noSignalHandler"),
            ("panics", "on"),
            ("passC", "-Ofast"),
            ("passC", "-flto=full"),
            ("gc", "refc"),
            ("out", JNI_LIBS / "libunalix_jni.so")
        ]
        
        let compilerFlags: string = (
            block: collect newSeq: (for (key, value) in nimCompilerFlags: &"--{key}:'{value}'")
        ).join(sep = " ")
    
        echoAndRun(command = &"nim compile {compilerFlags} './src/wrapper.nim'")
    else:
        writeStderr(
            s = &"faltal: unknown library name: {target}",
            exitCode = 1
        )
else:
    writeStderr(
        s = &"faltal: unknown command name: {command}",
        exitCode = 1
    )
