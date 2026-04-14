@echo off
setlocal

call :set_repo_root || exit /b 1
call :find_java || exit /b 1

pushd "%REPO_ROOT%" || exit /b 1
"%JAVA_CMD%" -cp bin byow.Core.WindowSmokeTest
set "TEST_RESULT=%errorlevel%"
popd
exit /b %TEST_RESULT%

:set_repo_root
set "REPO_ROOT=%~dp0"
if exist "%REPO_ROOT%byow\Core\Main.java" exit /b 0
if exist "%REPO_ROOT%..\byow\Core\Main.java" (
    for %%I in ("%REPO_ROOT%..") do set "REPO_ROOT=%%~fI\"
    exit /b 0
)
echo Could not find the repo root from %~dp0
exit /b 1

:find_java
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" (
    set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    exit /b 0
)
for %%I in (java.exe) do set "JAVA_CMD=%%~$PATH:I"
if defined JAVA_CMD exit /b 0
echo Java runtime not found.
echo Install JDK 21 or newer, then set JAVA_HOME or add java.exe to PATH.
exit /b 1
