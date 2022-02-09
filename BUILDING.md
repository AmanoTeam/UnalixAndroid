### Requirements

- A Unix-like operating system
- [Nim compiler](https://nim-lang.org/install_unix.html) (`1.6.0` or higher is recommended)
- [Android NDK](https://developer.android.com/ndk/downloads) (`r23b` or higher is recommended)

### Instructions

Most of the core functionality of this application is provided by [Unalix-nim](https://github.com/AmanoTeam/Unalix-nim), which in thurn uses the PCRE and LibreSSL libraries.

Unalix has a CLI tool that can be used to compile all of it's dependencies, so you don't need to worry about manually downloading or building them.

#### Get source code

To checkout the latest commit, run the following command:

```bash
git clone --depth=1 https://github.com/AmanoTeam/UnalixAndroid
```

#### Install helper tool

We will use this helper tool to build all the dependencies required by Unalix.

Run the following commands:

```bash
cd ./app/src/main/jni/tool
nimble install
```

#### Build PCRE

To build the PCRE library, run the following commands:

```bash
./tool download pcre
./tool build pcre --architecture=arm
```

#### Build LibreSSL

To build the LibreSSL library, run the following commands:

```bash
./tool download libressl
./tool patch libressl
./tool build libressl --architecture=arm
```

#### Build JNI wrapper

To build the JNI wrapper, run the following command:

```bash
./tool build wrapper --architecture=arm
```

#### Build application

We recommend using JDK 11 or higher to build the application. Older versions might work, but they weren't tested.

Go to the root directory of the source code and run the following command:

```bash
./gradlew assembleRelease --no-daemon
```
