type
    ConnectError* {.pure, inheritable.} = object of CatchableError
    UnsupportedProtocolError* {.final.} = object of ConnectError
    RemoteProtocolError* {.final.} = object of ConnectError
    ReadError* {.final.} = object of ConnectError
    TooManyRedirectsError* {.final.} = object of ConnectError
