#! /bin/bash

SCRIPT_DIR=`dirname $0`

source "$SCRIPT_DIR/jvmopts.sh"

cat > /dev/stdout <<EOF
- JVM options: $JVM_OPTS
EOF

export JVM_OPTS

if [ "$TRAVIS_SCALA_VERSION" = "2.10.5" -a `javac -version 2>&1 | grep 1.7 | wc -l` -eq 1 ]; then
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

sbt ++$TRAVIS_SCALA_VERSION 'testOnly'
