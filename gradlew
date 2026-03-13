#!/bin/bash
# Simple gradlew script
APP_HOME=$(dirname "$0")
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec java -cp "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
