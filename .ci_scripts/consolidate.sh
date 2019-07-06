#! /usr/bin/env bash

for F in `grep -rl 'java\$lang.html' _site`
do
  #echo $F
  sed -e 's/java\$lang.html/#java\$lang/g' < "$F" > "$F.tmp" && mv "$F.tmp" "$F"
done

echo '[INFO] Generated HTML normalized (for wget compat)'

wget -nv -e robots=off -Dlocalhost --follow-tags=a -r \
     --spider http://localhost:4000
RES=$?

rm -rf 'localhost:4000'

echo "[INFO] Documentation checked for broken links ($RES)"

pkill -9 -f jekyll

if [ $RES -ne 0 ]; then
  exit $RES
fi
