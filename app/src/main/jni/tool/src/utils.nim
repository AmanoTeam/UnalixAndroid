import std/strutils
import std/os
import std/sequtils
import std/httpclient

import pkg/zippy/tarballs

proc `~`*(command: string): void =
    
    echo "> $1" % [command]
    
    let exitCode: int = execShellCmd(command = command)
    assert exitCode == 0
    

proc `!`*(err: string): void =
    stderr.write(err & "\n")
    quit(1)

proc writeStdout*(s: string, exitCode: int = -1): void =
    stdout.write(s & "\n")
    
    if exitCode >= 0:
        quit(exitCode)

proc getPrefixedArgument*(s: string): string =
    result = if len(s) > 1: "--" & s else: "-" & s

proc downloadTarball*(url, filename: string): void =

    let client: HttpClient = newHttpClient(timeout = 5000)

    echo "Downloading from $1 to $2" % [url, filename]

    let content: string = client.getContent(url = url)
    writeFile(filename = filename, content = content)

    let
        name: string = splitFile(path = filename).name
        directory: string = getTempDir() / name

    echo "Extracting from $1 to $2" % [filename, directory]

    extractAll(tarPath = filename, dest = directory)

    let directories: seq[string] = toSeq(iter = walkDirs(pattern = "$1/*" % [directory]))

    let
        source: string = directories[0]
        dest: string = "../" / name

    echo "Moving source directory from $1 to $2..." % [source, dest]

    if dirExists(dir = dest):
        removeDir(dir = dest)

    moveDir(source = source, dest = dest)

    echo "Removing $1 from disk..." % [directory]

    removeDir(dir = directory)

    echo "Removing $1 from disk..." % [filename]

    removeFile(file = filename)
