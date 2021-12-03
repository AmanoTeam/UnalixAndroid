import std/httpcore

type
    Response* {.final.} = object
        httpVersion*: HttpVersion
        statusCode*: HttpCode
        statusMessage*: string
        headers*: HttpHeaders
        body*: string

func initResponse*(
    httpVersion: HttpVersion,
    statusCode: HttpCode,
    statusMessage: string,
    headers: HttpHeaders,
    body: string
): Response =
    result.httpVersion = httpVersion
    result.statusCode = statusCode
    result.statusMessage = statusMessage
    result.headers = headers
    result.body = body    
