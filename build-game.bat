@echo off
setlocal

set "JAVA_HOME_BIN=C:\Users\Lena\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.10.7-hotspot\bin"

if not exist "%JAVA_HOME_BIN%\javac.exe" (
    echo Java 21 compiler not found at:
    echo %JAVA_HOME_BIN%\javac.exe
    exit /b 1
)

if not exist "bin" mkdir bin

"%JAVA_HOME_BIN%\javac.exe" -encoding UTF-8 -d bin ^
    edu\princeton\cs\introcs\StdDraw.java ^
    byow\TileEngine\TERenderer.java ^
    byow\TileEngine\TETile.java ^
    byow\TileEngine\Tileset.java ^
    byow\lab12\Position.java ^
    byow\lab12\Hexagon.java ^
    byow\lab12\Tessellation.java ^
    byow\Core\RandomUtils.java ^
    byow\Core\Room.java ^
    byow\Core\World.java ^
    byow\InputDemo\InputSource.java ^
    byow\InputDemo\StringInputDevice.java ^
    byow\Core\Engine.java ^
    byow\Core\Launcher.java ^
    byow\Core\WindowSmokeTest.java ^
    byow\Core\Main.java

exit /b %errorlevel%
