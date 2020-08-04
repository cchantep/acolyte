---
layout: page
title: Build
---

Acolyte can be built from these sources using SBT (0.12.2+): `sbt publish`

[![TravisCI](https://secure.travis-ci.org/cchantep/acolyte.png?branch=master)](http://travis-ci.org/cchantep/acolyte) [![CircleCI](https://circleci.com/gh/cchantep/acolyte.svg?style=svg)](https://circleci.com/gh/cchantep/acolyte)

## Documentation

Documentation is generated using Maven 3: `mvn -f site.xml site`

## Deploy

To local repository:

```
mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-driver/target/jdbc-driver-$VERSION.pom -Dfile=jdbc-driver/target/jdbc-driver-$VERSION.jar -Durl=file://$REPOPATH

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-scala/target/scala-2.10/jdbc-scala_2.10-$VERSION.pom -Dfile=jdbc-scala/target/scala-2.10/jdbc-scala_2.10-$VERSION.jar -Durl=file://$REPOPATH

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=scalac-plugin/target/scala-2.10/scalac-plugin_2.10-$VERSION.pom -Dfile=scalac-plugin/target/scala-2.10/scalac-plugin_2.10-$VERSION.jar -Durl=file://$REPOPATH
```

At Sonatype:

```
export REPO="https://oss.sonatype.org/service/local/staging/deploy/maven2/"
# or https://oss.sonatype.org/content/repositories/snapshots/

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-driver/target/jdbc-driver-$VERSION.pom -Dfile=jdbc-driver/target/jdbc-driver-$VERSION.jar -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-driver/target/jdbc-driver-$VERSION.pom -Dfile=jdbc-driver/target/jdbc-driver-$VERSION-javadoc.jar -Dclassifier=javadoc -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-driver/target/jdbc-driver-$VERSION.pom -Dfile=jdbc-driver/target/jdbc-driver-$VERSION-sources.jar -Dclassifier=sources -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

export SCALAVER="2.10"

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-scala/target/scala-$SCALAVER/jdbc-scala_$SCALAVER-$VERSION.pom -Dfile=jdbc-scala/target/scala-$SCALAVER/jdbc-scala_$SCALAVER-$VERSION.jar -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-scala/target/scala-$SCALAVER/jdbc-scala_$SCALAVER-$VERSION.pom -Dfile=jdbc-scala/target/scala-$SCALAVER/jdbc-scala_$SCALAVER-$VERSION-javadoc.jar -Dclassifier=javadoc -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-scala/target/scala-$SCALAVER/jdbc-scala_$SCALAVER-$VERSION.pom -Dfile=jdbc-scala/target/scala-$SCALAVER/jdbc-scala_$SCALAVER-$VERSION-sources.jar -Dclassifier=sources -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=scalac-plugin/target/scala-$SCALAVER/scalac-plugin_$SCALAVER-$VERSION.pom -Dfile=scalac-plugin/target/scala-$SCALAVER/scalac-plugin_$SCALAVER-$VERSION.jar -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=scalac-plugin/target/scala-$SCALAVER/scalac-plugin_$SCALAVER-$VERSION.pom -Dfile=scalac-plugin/target/scala-$SCALAVER/scalac-plugin_$SCALAVER-$VERSION-javadoc.jar -Dclassifier=javadoc -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=scalac-plugin/target/scala-$SCALAVER/scalac-plugin_$SCALAVER-$VERSION.pom -Dfile=scalac-plugin/target/scala-$SCALAVER/scalac-plugin_$SCALAVER-$VERSION-sources.jar -Dclassifier=sources -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=reactive-mongo/target/scala-$SCALAVER/reactive-mongo_$SCALAVER-$VERSION.pom -Dfile=reactive-mongo/target/scala-$SCALAVER/reactive-mongo_$SCALAVER-$VERSION.jar -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=reactive-mongo/target/scala-$SCALAVER/reactive-mongo_$SCALAVER-$VERSION.pom -Dfile=reactive-mongo/target/scala-$SCALAVER/reactive-mongo_$SCALAVER-$VERSION-javadoc.jar -Dclassifier=javadoc -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=reactive-mongo/target/scala-$SCALAVER/reactive-mongo_$SCALAVER-$VERSION.pom -Dfile=reactive-mongo/target/scala-$SCALAVER/reactive-mongo_$SCALAVER-$VERSION-sources.jar -Dclassifier=sources -Durl=$REPO -DrepositoryId=sonatype-nexus-staging
```

Authentication should be configured in `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <!-- ... -->

    <server>
      <id>sonatype-nexus-staging</id>
      <username>your-jira-id</username>
      <password>your-jira-pwd</password>
    </server>
  </servers>
  <!-- ... -->
</settings>
```
