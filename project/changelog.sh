#! /bin/sh

LIMIT="ac5f3cd1a1e794ec19b863bd6627ebd813b8bc4b"

git log --grep '^\[play-jdbc\]' --grep '^\[reactivemongo\]' --grep '^\[jdbc-driver\]' --grep '^\[jdbc-scala\]' --grep '^\[jdbc-java8\]' --grep '^\[studio\]' --grep '^\[scalac-plugin\]' "$LIMIT..HEAD" | grep -v '^Author:' | grep -v '^Date:' | perl -pe 's|^commit (.+)$|# \1|;s|^    ||;s|^\[([a-zA-Z0-9-]+)\]|\1|' | awk 'BEGIN { c = ""; d = 0; } { if (c != "" && $0 != "") { d = 1; m = $1; t = substr($0, index($0, " ")+1); printf("([%s](https://github.com/cchantep/acolyte/commit/%s) @ [%s](https://github.com/cchantep/acolyte/tree/master/%s)) %s\n", c, c, m, m, t); } if ($1 == "#") { c = $2; d = 0; } if (d == 1) { if (c != "") { c = ""; } else { printf("%s\n", $0); } } }'
