set CLASSPATH=%ANT_HOME%\lib\ant.jar;%CLASSPATH%
del build-ant-toolkit.log
ant -f build-ant-toolkit.xml -l build-ant-toolkit.log