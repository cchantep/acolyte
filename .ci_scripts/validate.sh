#! /usr/bin/env bash

set -e

SCRIPT_DIR=`dirname $0`

if [ "v$SCALA_VERSION" = "v2.13.15" ]; then
    echo "[INFO] Check the source format and backward compatibility"

    sbt ";++$SCALA_VERSION ;error ;scalafixAll -check ;scalafmtCheckAll" || (cat >> /dev/stdout <<EOF
ERROR: Scalafmt check failed, see differences above.
To fix, format your sources using 'sbt scalafmtAll' before submitting a pull request.
Additionally, please squash your commits (eg, use git commit --amend) if you're going to update this pull request.
EOF
        false
    )
fi

SBT_CMDS="testQuick doc"

sbt ++$SCALA_VERSION $SBT_CMDS
