#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Maven Start Up Batch script
#
# Required ENV vars:
# JAVA_HOME - location of a JDK home dir
#
# Optional ENV vars
# M2_HOME - location of maven2's installed home dir
# MAVEN_BATCH_ECHO - set to 'on' to enable the echoing of the batch commands
# MAVEN_BATCH_PAUSE - set to 'on' to wait for a keystroke before ending
# MAVEN_OPTS - parameters passed to the Java VM when running Maven
#     e.g. to debug Maven itself, use
# set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
# ----------------------------------------------------------------------------

JAVACMD="$JAVA_HOME/bin/java"

CLASS_PATH=${MAVEN_PROJECTBASEDIR}/.mvn/wrapper/maven-wrapper.jar

if [ ! -e "$CLASS_PATH" ]; then
    echo "Error: Could not find or load main class org.apache.maven.wrapper.MavenWrapperMain"
    echo "Please ensure you have the .mvn/wrapper/maven-wrapper.jar file or that the wrapper downloads it."
fi

exec "$JAVACMD" \
  $MAVEN_OPTS \
  -classpath "$CLASS_PATH" \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"