cd ..
deps\tools\system\7z.exe x -obin deps\tools\system\apache-ant-1.7.1-bin.zip
deps\tools\system\7z.exe x -obin deps\tools\containers\appengine-java-sdk-1.2.1.zip
cd build
..\bin\apache-ant-1.7.1\bin\ant.bat -f build-appengine.xml init