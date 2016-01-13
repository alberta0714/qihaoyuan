rd /S /Q target
rd /S /Q .settings
rd /S /Q .myeclipse
del .classpath
del .project
del .mymetadata
cd src/main/webapp/WEB-INF
rd /S /Q classes
cd ../../../../
mvn clean 