import std/streams
import std/net
import std/strutils
import std/uri
import std/pegs
import std/os
import std/strformat
import std/sequtils
import std/httpcore

import pkg/zippy/tarballs

import ./types
import ./exceptions

proc echoAndRun*(command: string): void =
    
    echo &"> {command}"
    
    let exitCode: int = execShellCmd(command = command)
    assert exitCode == 0
    

proc writeStderr*(s: string, exitCode: int = -1): void =
    stderr.write(s & "\n")
    
    if exitCode >= 0:
        quit(exitCode)

proc writeStdout*(s: string, exitCode: int = -1): void =
    stdout.write(s & "\n")
    
    if exitCode >= 0:
        quit(exitCode)

proc getPrefixedArgument*(s: string): string =
    result = if len(s) > 1: "--" & s else: "-" & s

proc toHuman(bytes: int): string =
    var bt: float = float(bytes)
    
    for unit in ["", "Ki", "Mi", "Gi", "Ti", "Pi", "Ei", "Zi"]:
        if bt < 1024.0:
            return &"{bt:3.1f}{unit}B"
        bt = bt / 1024.0
    
    result = &"{bt:.1f}YiB"

func getRequestUri(self: Uri): string =
    ## Builds a raw request-URI from an Uri object
    
    if self.path.isEmptyOrWhitespace():
        result = "/"
    else:
        result = self.path
    
    if not self.query.isEmptyOrWhitespace():
        result = &"{result}?{self.query}"

func getPort(self: Uri): int =
    
    if not self.port.isEmptyOrWhitespace():
        return self.port.parseInt()
    
    case self.scheme:
    of "http":
        result = 80
    of "https":
        result = 443
    else:
        discard

func getHostname(self: Uri): string =
    
    let port: int = self.getPort()
    
    if (self.scheme == "http" and port != 80) or (self.scheme == "https" and port != 443):
        return &"{self.hostname}:{port}"
    
    result = self.hostname

proc downloadFile*(url, filename: string): void = 
    
    echo &"Downloading from {url} to {filename}"
    
    var thisUrl: string = url
    var currentRedirects: int
    
    let httpMaxRedirects: int = 13

    while true:
        if not (thisUrl.startsWith(prefix = "http") or thisUrl.startsWith(prefix = "https")):
            raise newException(
                exceptn = UnsupportedProtocolError,
                message = "Unrecognized URI or unsupported protocol"
            )
        
        let socket: Socket = newSocket(buffered = false)
        socket.setSockOpt(opt = OptNoDelay, value = true, level = IPPROTO_TCP.cint)

        var uri: Uri = thisUrl.parseUri()
        
        if uri.scheme == "https":
            newContext().wrapSocket(socket = socket)

        try:
            when (NimMajor, NimMinor) >= (1, 6):
                socket.connect(
                    address = uri.hostname,
                    port = Port(uri.getPort()),
                    timeout = 3000
                )
            else:
                socket.connect(
                    address = uri.hostname,
                    port = Port(uri.getPort())
                )
            socket.send(data = &"GET {uri.getRequestUri()} HTTP/1.0\nHost: {uri.hostname}\n\n")
        except Exception as e:
            socket.close()

            raise newException(
                exceptn = ConnectError,
                message = e.msg,
                parentException = e
            )

        var chunks: string

        # Start reading headers
        while not anyIt(["\r\n\r\n", "\n\n"], it in chunks):
            var data: string

            try:
                if socket.recv(data = data, size = 8192, timeout = 3000) == 0:
                    break

            except Exception as e:
                socket.close()

                raise newException(
                    exceptn = ReadError,
                    message = e.msg,
                    parentException = e
                )

            chunks.add(y = data)

        let parts: seq[string] = chunks.split(sep = "\r\n\r\n", maxsplit = 1)
        
        if len(parts) != 2:
            socket.close()
            
            raise newException(
                exceptn = RemoteProtocolError,
                message = "Invalid server response"
            )

        var headers: seq[(string, string)] = newSeq[(string, string)]()
        
        var
            httpVersion: HttpVersion
            statusCode: HttpCode
            statusMessage: string

        for (index, line) in parts[0].split(sep = "\r\n").pairs():
            # This index is not a header
            if index == 0:
                let parts: seq[string] = line.split(sep = " ", maxsplit = 2)
                
                if len(parts) != 3:
                    socket.close()
                    
                    raise newException(
                        exceptn = RemoteProtocolError,
                        message = &"Malformed server response"
                    )
                
                # Parse HTTP version
                let values: seq[string] = parts[0].split(sep = "/")
                
                if len(values) != 2:
                    socket.close()
                    
                    raise newException(
                        exceptn = RemoteProtocolError,
                        message = &"Malformed server response"
                    )
                
                if values[0] != "HTTP":
                    socket.close()
                    
                    raise newException(
                        exceptn = RemoteProtocolError,
                        message = &"Unknown protocol name: {values[0]}"
                    )
                
                case values[1]
                of "1.0":
                    httpVersion = HttpVer10
                of "1.1":
                    httpVersion = HttpVer11
                else:
                    socket.close()
                    
                    raise newException(
                        exceptn = RemoteProtocolError,
                        message = &"Unsupported protocol version: {values[1]}"
                    )
                
                # Parse status code
                case parts[1]
                of "100":
                    statusCode = Http100
                    statusMessage = "Continue"
                of "101":
                    statusCode = Http101
                    statusMessage = "Switching Protocols"
                of "200":
                    statusCode = Http200
                    statusMessage = "OK"
                of "201":
                    statusCode = Http201
                    statusMessage = "Created"
                of "202":
                    statusCode = Http202
                    statusMessage = "Accepted"
                of "203":
                    statusCode = Http203
                    statusMessage = "Non-Authoritative Information"
                of "204":
                    statusCode = Http204
                    statusMessage = "Non-Authoritative Information"
                of "205":
                    statusCode = Http205
                    statusMessage = "Reset Content"
                of "206":
                    statusCode = Http206
                    statusMessage = "Partial Content"
                of "300":
                    statusCode = Http300
                    statusMessage = "Multiple Choices"
                of "301":
                    statusCode = Http301
                    statusMessage = "Moved Permanently"
                of "302":
                    statusCode = Http302
                    statusMessage = "Found"
                of "303":
                    statusCode = Http303
                    statusMessage = "See Other"
                of "304":
                    statusCode = Http304
                    statusMessage = "Not Modified"
                of "305":
                    statusCode = Http305
                    statusMessage = "Use Proxy"
                of "307":
                    statusCode = Http307
                    statusMessage = "Temporary Redirect"
                of "400":
                    statusCode = Http400
                    statusMessage = "Bad Request"
                of "401":
                    statusCode = Http401
                    statusMessage = "Unauthorized"
                of "402":
                    when (NimMajor, NimMinor) < (1, 6):
                        statusCode = HttpCode(402)
                    else:
                        statusCode = Http402
                    statusMessage = "Payment Required"
                of "403":
                    statusCode = Http403
                    statusMessage = "Forbidden"
                of "404":
                    statusCode = Http404
                    statusMessage = "Not Found"
                of "405":
                    statusCode = Http405
                    statusMessage = "Method Not Allowed"
                of "406":
                    statusCode = Http406
                    statusMessage = "Not Acceptable"
                of "407":
                    statusCode = Http407
                    statusMessage = "Proxy Authentication Required"
                of "408":
                    statusCode = Http408
                    statusMessage = "Request Time-out"
                of "409":
                    statusCode = Http409
                    statusMessage = "Conflict"
                of "410":
                    statusCode = Http410
                    statusMessage = "Gone"
                of "411":
                    statusCode = Http411
                    statusMessage = "Length Required"
                of "412":
                    statusCode = Http412
                    statusMessage = "Precondition Failed"
                of "413":
                    statusCode = Http413
                    statusMessage = "Request Entity Too Large"
                of "414":
                    statusCode = Http414
                    statusMessage = "Request-URI Too Large"
                of "415":
                    statusCode = Http415
                    statusMessage = "Unsupported Media Type"
                of "416":
                    statusCode = Http416
                    statusMessage = "Requested range not satisfiable"
                of "417":
                    statusCode = Http417
                    statusMessage = "Expectation Failed"
                of "500":
                    statusCode = Http500
                    statusMessage = "Internal Server Error"
                of "501":
                    statusCode = Http501
                    statusMessage = "Not Implemented"
                of "502":
                    statusCode = Http502
                    statusMessage = "Bad Gateway"
                of "503":
                    statusCode = Http503
                    statusMessage = "Service Unavailable"
                of "504":
                    statusCode = Http504
                    statusMessage = "Gateway Time-out"
                of "505":
                    statusCode = Http505
                    statusMessage = "HTTP Version not supported"
                else:
                    socket.close()
                    
                    raise newException(
                        exceptn = RemoteProtocolError,
                        message = &"Unknown status code: {parts[1]}"
                    )
                
                continue

            var key, value: string
            (key, value) = line.split(sep = ": ", maxsplit = 1)

            headers.add((key, value))

        var response: Response = initResponse(
            httpVersion = httpVersion,
            statusCode = statusCode,
            statusMessage = statusMessage,
            headers = newHttpHeaders(headers),
            body = ""
        )

        var redirectLocation: string

        if response.statusCode.is3xx() and response.headers.hasKey(key = "Location"):
            redirectLocation = response.headers["Location"].toString()
        elif response.statusCode.is2xx() and response.headers.hasKey(key = "Content-Location"):
            redirectLocation = response.headers["Content-Location"].toString()

        if not redirectLocation.isEmptyOrWhitespace():
            redirectLocation = redirectLocation.replace(sub = " ", by = "%20")
            if not (redirectLocation.startsWith(prefix = "https://") or redirectLocation.startsWith(prefix = "http://")):
                if redirectLocation.startsWith(prefix = "//"):
                    redirectLocation = &"{uri.scheme}://" & redirectLocation.replacef(sub = peg"^ '/' *", by = "") 
                elif redirectLocation.startsWith(prefix = '/'):
                    redirectLocation = &"{uri.scheme}://" & uri.getHostname() & redirectLocation
                else:
                    redirectLocation = &"{uri.scheme}://" & uri.getHostname() & (if uri.path != "/": parentDir(uri.path) else: uri.path) & redirectLocation

            if redirectLocation == thisUrl:
                break

            inc currentRedirects
            
            if currentRedirects > httpMaxRedirects:
                raise newException(
                    exceptn = TooManyRedirectsError,
                    message = "Exceeded maximum allowed redirects"
                )
            
            thisUrl = redirectLocation
            
            continue

        if response.statusCode != 200.HttpCode:
            socket.close()
            
            raise newException(
                exceptn = ConnectError,
                message = &"Got unexpected status code: {(response.statusCode).int}"
            )
        
        let contentLength: int = (
            if response.headers.hasKey(key = "Content-Length"):
                response.headers["Content-Length"].toString().parseInt()
            else:
                0
        )
        
        let destinationFile: FileStream = newFileStream(filename = filename, mode = fmWrite)
        destinationFile.write(x = parts[1])

        # Start reading body
        while true:
            var data: string

            try:
                if socket.recv(data = data, size = 8192, timeout = 3000) == 0:
                    break
            except Exception as e:
                socket.close()

                raise newException(
                    exceptn = ReadError,
                    message = e.msg,
                    parentException = e
                )

            destinationFile.write(x = data)

            stdout.write &"\rProgress: {toHuman(destinationFile.getPosition())} / {toHuman(contentLength)}    \r"

        socket.close()
        
        let totalReceived: int = destinationFile.getPosition()
        destinationFile.close()
        
        if contentLength > 0 and totalReceived != contentLength:
            raise newException(
                exceptn = RemoteProtocolError,
                message = &"Server closed connection without sending complete message body (received {totalReceived} bytes, expected {contentLength})"
            )
        
        let
            name: string = splitFile(path = filename).name
            directory: string = getTempDir() / name

        echo &"Extracting from {filename} to {directory}..."

        extractAll(tarPath = filename, dest = directory)

        let
            source: string = toSeq(walkDirs(pattern = &"{directory}/*"))[0]
            dest: string = getCurrentDir().parentDir() / name

        echo &"Moving source directory from {source} to {dest}..."
        
        if dirExists(dir = dest):
            removeDir(dir = dest)
        
        moveDir(source = source, dest = dest)

        echo &"Removing {directory} from disk..."
        
        removeDir(dir = directory)
        
        echo &"Removing {filename} from disk..."
        
        removeFile(file = filename)
        
        return
