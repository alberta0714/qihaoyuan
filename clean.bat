#mvn clean 
#mvn eclipse:clean

rd /S /Q .myeclipse
del .classpath
del .mymetadata
del .project

rd /S /Q target
cd src/main/webapp/WEB-INF
rd /S /Q classes
cd ../../../../

