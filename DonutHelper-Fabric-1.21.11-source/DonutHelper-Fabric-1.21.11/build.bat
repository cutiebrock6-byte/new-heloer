@echo off
setlocal
cd /d "%~dp0"
echo DonutHelper - Minecraft 1.21.11 Fabric build
echo.
where gradle >nul 2>nul
if errorlevel 1 (
  echo ERROR: Gradle was not found on PATH.
  echo Install Gradle 9.x, then reopen this window:
  echo https://gradle.org/install/
  echo.
  echo Java 21 is also required: https://adoptium.net/
  pause
  exit /b 1
)
gradle --version
if errorlevel 1 goto fail
gradle clean build
if errorlevel 1 goto fail
echo.
echo Build complete. Your mod JAR is in:
echo %CD%\build\libs\donuthelper-1.0.0.jar
pause
exit /b 0
:fail
echo.
echo Build failed. Check the messages above.
pause
exit /b 1
