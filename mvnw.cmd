@REM Maven Wrapper startup script for Windows
@echo off
setlocal

if not "%JAVA_HOME%" == "" goto checkJHome
set "JAVA_HOME=C:\Program Files\Java\jdk-25"
:checkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto init
echo Error: JAVA_HOME is not set or invalid. >&2
echo Set JAVA_HOME to your JDK installation directory. >&2
exit /B 1

:init
set "MAVEN_PROJECTBASEDIR=%~dp0"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"

"%JAVA_HOME%\bin\java.exe" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
exit /B %ERRORLEVEL%
