#! /bin/sh

set -e

SCRIPT_DIR=`dirname $0 | sed -e "s|^\./|$PWD/|"`
SBT_VER="$1"

export SBT_OPTS="-Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"

export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export PATH="$JAVA_HOME/bin:$PATH"

# Sonatype staging (avoid Central sync delay)
perl -pe "s|resolvers |resolvers in ThisBuild += \"Sonatype Staging\" at \"https://oss.sonatype.org/content/repositories/staging/\",\r\nresolvers |" < "$SCRIPT_DIR/../build.sbt" > /tmp/build.sbt && mv /tmp/build.sbt "$SCRIPT_DIR/../build.sbt"

SBT_JAR="$HOME/.sbt/launchers/$SBT_VER/sbt-launch.jar"

java $SBT_OPTS -jar "$SBT_JAR" compile || exit 1

echo "[INFO] Code snippets validated"

rm -rf target project vendor && \
  bundle exec jekyll build || exit 2

echo "[INFO] Spell checking"
./node_modules/markdown-spellcheck/bin/mdspell -r --en-gb -n `find . -not -path '*/node_modules/*' -type f -name '*.md' | perl -pe 's|^\./||;s|[A-Za-z0-9.-]+|*|g' | sort -u | sed -e 's/$/.md/'` '!**/node_modules/**/*.md' '!**/vendor/**/*.md' || exit 3

echo "[INFO] Documentation built"
