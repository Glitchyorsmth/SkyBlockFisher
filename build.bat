@echo off
set JAVA_HOME=C:\Program Files\Zulu\zulu-21
set MODS=C:\Users\glitc\AppData\Roaming\ModrinthApp\profiles\1.21.5 Foraging\mods
echo Using Java 21 from %JAVA_HOME%
echo Building SkyBlockFisher...
cd /d "%~dp0"
call gradlew.bat build
if %errorlevel% neq 0 (
    echo BUILD FAILED
    pause
    exit /b 1
)

echo Removing old versions from mods folder...
del /q "%MODS%\SkyBlockFisher-*.jar" 2>nul
del /q "%MODS%\SkyBlockFisher-*.jar.old" 2>nul

echo Copying new version...
for /f "tokens=*" %%f in ('dir /b /o-d build\libs\SkyBlockFisher-*.jar 2^>nul') do (
    copy /y "build\libs\%%f" "%MODS%\%%f"
    echo Copied %%f
    goto :done_copy
)
:done_copy

echo Done! Restart Minecraft to use the new version.
pause
