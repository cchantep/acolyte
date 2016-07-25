#! /bin/sh

LIMIT="59f24680d55250b50dd7f1de4c98c7c8d263a46e"

git log --grep '^\[play-jdbc\]' --grep '^\[reactivemongo\]' --grep '^\[jdbc-driver\]' --grep '^\[jdbc-scala\]' --grep '^\[jdbc-java8\]' --grep '^\[studio\]' --grep '^\[scalac-plugin\]' "$LIMIT..HEAD" | grep -v '^Author:' | grep -v '^Date:' | perl -pe 's|^commit (.+)$|# \1|;s|^    ||;s|^\[([a-zA-Z0-9-]+)\]|\1|' | awk 'BEGIN { c = ""; d = 0; } { if (c != "" && $0 != "") { d = 1; m = $1; t = substr($0, index($0, " ")+1); printf("([%s](https://github.com/cchantep/acolyte/commit/%s) @ [%s](https://github.com/cchantep/acolyte/tree/master/%s)) %s\n", c, c, m, m, t); } if ($1 == "#") { c = $2; d = 0; } if (d == 1) { if (c != "") { c = ""; } else { printf("%s\n", $0); } } }'
