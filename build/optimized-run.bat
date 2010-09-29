bin\apache-ant-1.7.1\bin\ant -f build\build-appengine.xml step-datanucleus-enhance
bin\apache-ant-1.7.1\bin\ant -f build\build-appengine.xml step-stage-prepare
# Assume ${temp.dir} in build.xml file set to /tmp/webapp, in ${user.dir}/local.build.properties
bin\apache-ant-1.7.1\bin\appengine-java-sdk-1.3.6/bin/dev_appserver.sh --port=9999 %TEMP%\webapp\appengine-stage