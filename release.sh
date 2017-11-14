#!/bin/bash
# This script (for macOS) will release the plugin to the Jenkins update site.

export JAVA_HOME=`/usr/libexec/java_home -v 1.7`
export PATH=JAVA_HOME/bin:$PATH

mvn clean
mvn release:prepare release:perform -X