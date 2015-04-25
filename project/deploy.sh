#! /bin/sh

REPO="https://oss.sonatype.org/service/local/staging/deploy/maven2/"

VERSION="$1"
KEY="$2"
PASS="$3"

function deploy {
  BASE="$1"
  POM="$BASE.pom"
  FILES="$BASE.jar $BASE-javadoc.jar:javadoc $BASE-sources.jar:sources"

  for FILE in $FILES; do
    JAR=`echo "$FILE" | cut -d ':' -f 1`
    CLASSIFIER=`echo "$FILE" | cut -d ':' -f 2`

    if [ ! "$CLASSIFIER" = "$JAR" ]; then
      ARG="-Dclassifier=$CLASSIFIER"
    else
      ARG=""
    fi

    expect << EOF
set timeout 300
spawn mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=$POM -Dfile=$JAR $ARG -Durl=$REPO -DrepositoryId=sonatype-nexus-staging
expect "GPG Passphrase:"
send "$PASS\r"
expect "BUILD SUCCESS"
expect eof
EOF
  done
}

JAVA_MODULES="jdbc-driver"
EXTRA_JAVA_MODULES="jdbc-java8"
SCALA_MODULES="jdbc-scala scalac-plugin reactive-mongo"
SCALA_VERSIONS="2.10 2.11"
BASES=""

for M in $JAVA_MODULES; do
  BASES="$BASES $M/target/$M-$VERSION"
done

for M in $EXTRA_JAVA_MODULES; do
  B="$M/target/$M-$VERSION"
  if [ -r "$B.jar" ]; then
    BASES="$BASES $B"
  fi
done

for V in $SCALA_VERSIONS; do
  for M in $SCALA_MODULES; do
    BASES="$BASES $M/target/scala-$V/$M"_$V-$VERSION
  done
done

for B in $BASES; do
  deploy "$B"
done
