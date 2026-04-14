@echo off
setlocal

call :set_repo_root || exit /b 1
call :find_javac || exit /b 1

pushd "%REPO_ROOT%" || exit /b 1

if not exist "bin" mkdir bin

"%JAVAC_CMD%" -encoding UTF-8 -d bin ^
    edu\princeton\cs\introcs\StdDraw.java ^
    byow\TileEngine\TERenderer.java ^
    byow\TileEngine\TETile.java ^
    byow\TileEngine\Tileset.java ^
    byow\Core\Position.java ^
    byow\Core\RandomUtils.java ^
    byow\Core\Room.java ^
    byow\Core\World.java ^
    byow\InputDemo\InputSource.java ^
    byow\InputDemo\StringInputDevice.java ^
    byow\Core\Engine.java ^
    byow\Core\Launcher.java ^
    byow\Core\WindowSmokeTest.java ^
    byow\Core\Main.java

set "BUILD_RESULT=%errorlevel%"
popd
exit /b %BUILD_RESULT%

:set_repo_root
set "REPO_ROOT=%~dp0"
if exist "%REPO_ROOT%byow\Core\Main.java" exit /b 0
if exist "%REPO_ROOT%..\byow\Core\Main.java" (
    for %%I in ("%REPO_ROOT%..") do set "REPO_ROOT=%%~fI\"
    exit /b 0
)
echo Could not find the repo root from %~dp0
exit /b 1

:find_javac
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" (
    set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
    exit /b 0
)
for %%I in (javac.exe) do set "JAVAC_CMD=%%~$PATH:I"
if defined JAVAC_CMD exit /b 0
echo Java compiler not found.
echo Install JDK 21 or newer, then set JAVA_HOME or add javac.exe to PATH.
exit /b 1
