#! /usr/bin/env bash

set -e

SCRIPT_DIR=`dirname $0`

if [ "$TRAVIS_SCALA_VERSION" = "2.10.7" -a `javac -version 2>&1 | grep 1.7 | wc -l` -eq 1 ]; then
    echo "[INFO] Check the source format and backward compatibility"

    sbt ++$TRAVIS_SCALA_VERSION scalariformFormat test:scalariformFormat > /dev/null
    git diff --exit-code || (cat >> /dev/stdout <<EOF
ERROR: Scalariform check failed, see differences above.
To fix, format your sources using sbt scalariformFormat test:scalariformFormat before submitting a pull request.
Additionally, please squash your commits (eg, use git commit --amend) if you're going to update this pull request.
EOF
        false
    )

    sbt ++$TRAVIS_SCALA_VERSION ";error ;mimaReportBinaryIssues" || exit 2
fi

sbt ++$TRAVIS_SCALA_VERSION ';clean ;package'

PROJECTS="scalac-plugin jdbc-driver jdbc-java8 jdbc-scala"
PROJECTS="$PROJECTS reactive-mongo play-jdbc play-reactive-mongo studio"

# Memory workaround; TODO: fix
for P in $PROJECTS; do
  echo "# $P"
  find "$SCRIPT_DIR/../scalac-plugin/target" -type f -name '*.jar' -print  
  sbt ++$TRAVIS_SCALA_VERSION "$P/testQuick"
done
