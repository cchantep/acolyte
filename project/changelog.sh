#! /bin/sh

#LIMIT="f33f9c82f4e64bc3b83c9f64ab82319b47af873e"
LIMIT="924ef57d29285ad49c8674d76ceee97630a25c1a"

echo "## $1\n"

git log --grep '^\[core\]' --grep '^\[scala\]' --grep '^\[studio\]' "$LIMIT..HEAD" | grep -v '^Author:' | grep -v '^Date:' | perl -pe 's|^commit (.+)$|# \1|;s|^    ||;s|^\[([a-zA-Z0-9]+)\]|\1|' | awk 'BEGIN { c = ""; d = 0; } { if (c != "" && $0 != "") { d = 1; m = substr($0, 1, index($0, " ")-1); t = substr($0, index($0, " ")+1); printf("([%s](https://github.com/cchantep/acolyte/commit/%s) @ [%s](https://github.com/cchantep/acolyte/tree/master/%s)) %s\n", c, c, m, m, t); } if ($1 == "#") { c = $2; d = 0; } if (d == 1) { if (c != "") { c = ""; } else { printf("%s\n", $0); } } }'
