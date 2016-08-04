@echo off
echo If you are updating Forge, make sure you have modified build.properties with the new version string first!
pause
call gradlew.bat setupDecompWorkspace --refresh-dependencies
call gradlew.bat eclipse
echo.
echo.
echo.
echo Done!
pause
