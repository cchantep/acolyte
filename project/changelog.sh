#! /bin/sh

LIMIT="9c261f107338c4a9d4364d89540f8965cb46c71f"

echo "## $1\n"

git log --grep '^\[reactivemongo\]' --grep '^\[jdbc-driver\]' --grep '^\[jdbc-scala\]' --grep '^\[studio\]' --grep '^\[scalac-plugin\]' "$LIMIT..HEAD" | grep -v '^Author:' | grep -v '^Date:' | perl -pe 's|^commit (.+)$|# \1|;s|^    ||;s|^\[([a-zA-Z0-9]+)\]|\1|' | awk 'BEGIN { c = ""; d = 0; } { if (c != "" && $0 != "") { d = 1; m = substr($1, 2, length($1)-2); t = substr($0, index($0, " ")+1); printf("([%s](https://github.com/cchantep/acolyte/commit/%s) @ [%s](https://github.com/cchantep/acolyte/tree/master/%s)) %s\n", c, c, m, m, t); } if ($1 == "#") { c = $2; d = 0; } if (d == 1) { if (c != "") { c = ""; } else { printf("%s\n", $0); } } }'
