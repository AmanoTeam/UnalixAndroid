### Instructions

Most of the core functionality of this application is provided by [Unalix-nim](https://github.com/AmanoTeam/Unalix-nim), which in thurn uses the PCRE and LibreSSL libraries.

Unalix has a CLI tool that can be used to compile all of it's dependencies, so you don't need to worry about manually downloading or building them.

#### Requirements

- A Unix-like operating system
- [Nim compiler](https://nim-lang.org/install_unix.html) (version `1.6.0` or higher is recommended)
- [Android NDK](https://developer.android.com/ndk/downloads) (version `r23b` or higher is recommended)
- [JDK](https://adoptium.net/) (version `11` or higher is required)

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
nimble install --accept
```

#### Build PCRE

To build the PCRE library, run the following commands:

```bash
./tool download pcre
./tool build pcre --architecture=arm
./tool build pcre --architecture=arm64
./tool build pcre --architecture=i386
./tool build pcre --architecture=amd64
```

#### Build LibreSSL

To build the LibreSSL library, run the following commands:

```bash
./tool download libressl
./tool patch libressl
./tool build libressl --architecture=arm
./tool build libressl --architecture=arm64
./tool build libressl --architecture=i386
./tool build libressl --architecture=amd64
```

#### Build JNI wrapper

To build the JNI wrapper, run the following commands:

```bash
./tool build wrapper --architecture=arm
./tool build wrapper --architecture=arm64
./tool build wrapper --architecture=i386
./tool build wrapper --architecture=amd64
```

#### Build application

Go to the root directory of the source code and run the following command:

```bash
./gradlew assembleRelease --no-daemon
```
