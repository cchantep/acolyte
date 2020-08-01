#! /usr/bin/env bash

set -e

SCRIPT_DIR=`dirname $0`

if [ "v$SCALA_VERSION" = "v2.12.11" ]; then
    echo "[INFO] Check the source format and backward compatibility"

    sbt ++$SCALA_VERSION scalariformFormat test:scalariformFormat > /dev/null
    git diff --exit-code || (cat >> /dev/stdout <<EOF
ERROR: Scalariform check failed, see differences above.
To fix, format your sources using sbt scalariformFormat test:scalariformFormat before submitting a pull request.
Additionally, please squash your commits (eg, use git commit --amend) if you're going to update this pull request.
EOF
        false
    )
fi

SBT_CMDS="testQuick doc"

if [ "v$SCALA_VERSION" = "v2.12.11" ]; then
  SBT_CMDS="$SBT_CMDS scapegoat"
fi

sbt ++$SCALA_VERSION $SBT_CMDS
