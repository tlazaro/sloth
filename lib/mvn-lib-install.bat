mvn install:install-file -Dfile=lwjgl.jar -DgroupId=lwjgl -DartifactId=lwjgl -Dversion=2.7.1 -Dpackaging=jar
mvn install:install-file -Dfile=lwjgl_util.jar -DgroupId=lwjgl -DartifactId=lwjgl-util -Dversion=2.7.1 -Dpackaging=jar
mvn install:install-file -Dfile=lwjgl-2.7.1-natives-linux.jar -DgroupId=lwjgl -DartifactId=lwjgl -Dclassifier=natives-linux -Dversion=2.7.1 -Dpackaging=jar
mvn install:install-file -Dfile=lwjgl-2.7.1-natives-macosx.jar -DgroupId=lwjgl -DartifactId=lwjgl -Dclassifier=natives-macosx -Dversion=2.7.1 -Dpackaging=jar
mvn install:install-file -Dfile=lwjgl-2.7.1-natives-win.jar -DgroupId=lwjgl -DartifactId=lwjgl -Dclassifier=natives-win -Dversion=2.7.1 -Dpackaging=jar


