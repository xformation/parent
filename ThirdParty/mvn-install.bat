ECHO OFF
REM This file contains all the unresolved dependencies
call mvn install:install-file -Dfile=oak-pojosr-1.6.0.jar -DgroupId=org.apache.jackrabbit -DartifactId=oak-pojosr -Dversion=1.6.0 -Dpackaging=jar -DgeneratePom=true


REM call mvn install:install-file -Dfile=oak-standalone-1.6.0.jar -DgroupId=org.apache.jackrabbit -DartifactId=oak-standalone -Dversion=1.6.0 -Dpackaging=jar -DgeneratePom=true
REM call mvn install:install-file -Dfile=oak-blob-1.6.0.jar -DgroupId=org.apache.jackrabbit -DartifactId=oak-blob -Dversion=1.6.0 -Dpackaging=jar -DgeneratePom=true
REM call mvn install:install-file -Dfile=oak-commons-1.6.0.jar -DgroupId=org.apache.jackrabbit -DartifactId=oak-commons -Dversion=1.6.0 -Dpackaging=jar -DgeneratePom=true
REM call mvn install:install-file -Dfile=oak-core-1.6.0.jar -DgroupId=org.apache.jackrabbit -DartifactId=oak-core -Dversion=1.6.0 -Dpackaging=jar -DgeneratePom=true
REM call mvn install:install-file -Dfile=oak-jcr-1.6.0.jar -DgroupId=org.apache.jackrabbit -DartifactId=oak-jcr -Dversion=1.6.0 -Dpackaging=jar -DgeneratePom=true
REM call mvn install:install-file -Dfile=oak-lucene-1.6.0.jar -DgroupId=org.apache.jackrabbit -DartifactId=oak-lucene -Dversion=1.6.0 -Dpackaging=jar -DgeneratePom=true
REM call mvn install:install-file -Dfile=oak-segment-1.6.0.jar -DgroupId=org.apache.jackrabbit -DartifactId=oak-segment -Dversion=1.6.0 -Dpackaging=jar -DgeneratePom=true
REM call mvn install:install-file -Dfile=oak-segment-tar-1.6.0.jar -DgroupId=org.apache.jackrabbit -DartifactId=oak-segment-tar -Dversion=1.6.0 -Dpackaging=jar -DgeneratePom=true
