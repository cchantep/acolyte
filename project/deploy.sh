#! /bin/sh

REPO="https://oss.sonatype.org/service/local/staging/deploy/maven2/"

VERSION="$1"
KEY="$2"

if [ $# -lt 2 ]; then
  echo "Usage: $0 version gpg-key"
  exit 1
fi

echo "Password: "
read -s PASS

function deploy {
  BASE="$1"
  POM="$BASE.pom"
  FILES="$BASE.jar $BASE-javadoc.jar:javadoc $BASE-sources.jar:sources"

  expect <<EOF
spawn mvn gpg:sign-and-deploy-file -DuniqueVersion=false -Dkeyname=$KEY -DpomFile=$POM -Dfile=$BASE.jar -Djavadoc=$BASE-javadoc.jar -Dsources=$BASE-sources.jar $ARG -Durl=$REPO -DrepositoryId=sonatype-nexus-staging  
log_user 0
expect "GPG Passphrase:"
send "$PASS\r"
log_user 1
expect "BUILD SUCCESS"
expect eof
EOF
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
