call deploy.bat

REM Assume ${temp.dir} is set to %TEMP%\webapp, in ${user.dir}\local.build.properties
bin\appengine-java-sdk-1.3.2\bin\appcfg.cmd --email=dominique.derrien@gmail.com update %TEMP%\webapp\appengine-stage