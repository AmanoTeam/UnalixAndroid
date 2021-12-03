# Package

version       = "0.1"
author        = "SnwMds"
description   = "A new awesome nimble package"
license       = "LGPL-3.0-or-later"
srcDir        = "src"
bin           = @["tool"]


# Dependencies

requires "nim >= 1.6.0"
requires "https://github.com/SnwMds/zippy#fix-permissions2"
