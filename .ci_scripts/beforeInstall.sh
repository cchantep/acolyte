#! /bin/bash

if [ -d "$HOME/.local/bin" ]; then
  export PATH="$HOME/.local/bin:$PATH"
fi

gem update
gem install bundler || exit 1
bundle install || exit 2
pip install --user Pygments || exit 3
npm i markdown-spellcheck -u || exit 4

SBT_VER="$1"
SBT_LAUNCHER_HOME="$HOME/.sbt/launchers/$SBT_VER"
SBT_LAUNCHER_JAR="$SBT_LAUNCHER_HOME/sbt-launch.jar"

if [ ! -r "$SBT_LAUNCHER_JAR" ]; then
  mkdir -p $SBT_LAUNCHER_HOME
  curl -L -o "$SBT_LAUNCHER_JAR" "https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/$SBT_VER/sbt-launch-$SBT_VER.jar"
else
  echo -n "SBT already set up: "
  ls -v -1 "$SBT_LAUNCHER_JAR"
fi
