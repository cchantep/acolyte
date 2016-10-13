#! /bin/sh

set -e

SCRIPT_DIR=`dirname $0 | sed -e "s|^\./|$PWD/|"`
SBT_VER="$1"

export SBT_OPTS="-Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"

export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export PATH="$JAVA_HOME/bin:$HOME/.gem/ruby/2.2.0/bin:$PATH"
export GEM_PATH="$HOME/.gem/ruby/2.2.0:$GEM_PATH"

# Sonatype staging (avoid Central sync delay)
perl -pe "s|resolvers |resolvers in ThisBuild += \"Sonatype Staging\" at \"https://oss.sonatype.org/content/repositories/staging/\",\r\nresolvers |" < "$SCRIPT_DIR/../build.sbt" > /tmp/build.sbt && mv /tmp/build.sbt "$SCRIPT_DIR/../build.sbt"

SBT_JAR="$HOME/.sbt/launchers/$SBT_VER/sbt-launch.jar"

(java $SBT_OPTS -jar "$SBT_JAR" compile && \
  bundle exec jekyll build) || exit 2

echo "# Spell checking"
./node_modules/markdown-spellcheck/bin/mdspell -r --en-gb -n `find . -not -path '*/node_modules/*' -type f -name '*.md' -print` '!**/node_modules/**/*.md' '!**/vendor/**/*.md' || exit 3

echo "# Documentation built"

bundle exec jekyll serve --detach
echo "# Jekyll local server started"

for F in `grep -rl 'java\$lang.html' _site`
do
  echo $F && sed -e 's/java\$lang.html/#java\$lang/g' < "$F" > "$F.tmp" && mv "$F.tmp" "$F"
done

echo "# Generated HTML normalized (for wget compat)"

wget -nv -e robots=off -Dlocalhost --follow-tags=a -r --spider http://localhost:4000
RES=$?

echo "# Documentation checked for broken links"

rm -rf 'localhost:4000'
pkill -f jekyll

if [ $RES -ne 0 ]; then
  exit $RES
fi

echo "# Indexing to Algolia"
bundle exec jekyll algolia push || (
    echo "!! fails to push to Algolia"
    false
)
