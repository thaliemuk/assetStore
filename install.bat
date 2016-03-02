@echo off

:: build frontend
call xcopy frontend backend\src\main\webapp\ /S /E /Y

:: build backend
cd backend
call mvn clean install -X
cd ..

:: installation in Tomcat
:: getting the tomcat directory to install too
set APP_CLASSES_FILES=backend\target\interactions\WEB-INF\classes
FOR /F "eol=; tokens=1* delims==" %%A IN (%APP_CLASSES_FILES%\application.properties) DO (IF "%%A" == "installation.dir" set INSTALL_DIR=%%B)
echo installation dir: %INSTALL_DIR%
set TOMCAT_DIR=%INSTALL_DIR%\tomcat
echo tomcat dir: %TOMCAT_DIR%
set WEBAPPS_DIR=%TOMCAT_DIR%\webapps
echo webapps dir: %WEBAPPS_DIR%
IF EXIST %WEBAPPS_DIR%\assetStore (call RD /S /Q %WEBAPPS_DIR%\assetStore)
IF EXIST %WEBAPPS_DIR%\assetStore.war (call RD /S /Q %WEBAPPS_DIR%\assetStore.war)

:: copy the filesContext.xml into Tomcat's conf directory
call xcopy %APP_CLASSES_FILES%\filesContext.xml %TOMCAT_DIR%\conf\Catalina\localhost /Y

:: deploy the WAR file into tomcat webapps directory
cd backend\target
call rename interactions.war assetStore.war
cd ..\..\
call xcopy backend\target\assetStore.war %WEBAPPS_DIR% /Y