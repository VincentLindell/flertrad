@echo off

rem Simple wrapper for Make, to pass correct PATH to Make/GCC. Detects whether
rem PATH contains MSYS2 paths, and adds them if not. It is not enough to use
rem setlocal, as we need the path to be set in the surrounding shell as well
rem (otherwise the resulting binaries will not run)

set MSYSHOME=C:\msys64

rem This condition checks whether PATH does not contain the substring "\msys64\"
if "x%PATH:\msys64\=%"=="x%PATH%" set "PATH=%MSYSHOME%\usr\bin;%MSYSHOME%\usr\local\bin;%MSYSHOME%\ucrt64\bin;%PATH%"
make.exe %*

