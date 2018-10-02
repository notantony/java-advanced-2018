mkdir myout
javac -cp artifacts\JarImplementorTest.jar;artifacts\ImplementorTest.jar;..\lib\* -d myout java\ru\ifmo\rain\chekashev\implementor\Implementor.java
jar cmvf MANIFEST.MF ImplementorJar.jar -C myout ru -C myout info
rmdir \s\q myout
