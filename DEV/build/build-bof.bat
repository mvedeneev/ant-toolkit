set CLASSPATH=%ANT_HOME%\lib\ant.jar;%CLASSPATH%
del build-bof.log
ant -f build-bof.xml -l build-bof.log