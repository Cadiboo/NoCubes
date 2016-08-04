@echo off
echo Building for release...
echo.
SETLOCAL
SET RELEASEMODE=true
call gradlew.bat clean
call gradlew.bat build
SET RELEASEMODE=
ENDLOCAL
echo.
echo Done!
pause
