#!/bin/sh
# Maven Wrapper startup script for Unix
MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"

if [ -z "$JAVA_HOME" ]; then
  JAVACMD="$(command -v java)"
else
  JAVACMD="$JAVA_HOME/bin/java"
fi

if [ ! -x "$JAVACMD" ] && [ ! -f "$JAVACMD" ]; then
  echo "Error: JAVA_HOME is not set and 'java' is not in PATH." >&2
  exit 1
fi

exec "$JAVACMD" $MAVEN_OPTS \
  -classpath "$WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
