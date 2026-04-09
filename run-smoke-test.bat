@echo off
setlocal

set "JAVA_HOME_BIN=C:\Users\Lena\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.10.7-hotspot\bin"

if not exist "%JAVA_HOME_BIN%\java.exe" (
    echo Java 21 runtime not found at:
    echo %JAVA_HOME_BIN%\java.exe
    exit /b 1
)

"%JAVA_HOME_BIN%\java.exe" -cp bin byow.Core.WindowSmokeTest

exit /b %errorlevel%
