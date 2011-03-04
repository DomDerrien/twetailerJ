@echo off
set startTime=%time%

REM
REM Define the output folder path
REM
set webDir="C:/Jetty730/webapps/root"
set csvDir=.

REM
REM Propagate the template one city at a time
REM
main.py -w %webDir% -c %csvDir% -s

echo.
echo -- Started at:  %startTime%
echo -- Finished at: %time%
echo.