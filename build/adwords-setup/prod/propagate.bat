@echo off
set startTime=%time%

REM
REM Define the output folder path
REM
set outDir=C:\Jetty730\webapps\root
REM set outDir=C:\Steven\Apache\root

REM
REM Propagate the template one city at a time
REM
propagate_templates.py -c Montreal -p "H2Y 1C6" -o %outDir% -s
propagate_templates.py -c Laval    -p "H7V 3Z4" -o %outDir% -s

REM
REM Copy the companion files
REM
copy CarDealers\*.* %outDir%\CarDealers
copy Automobiles\*.* %outDir%\Automobiles

echo.
echo -- Started at:  %startTime%
echo -- Finished at: %time%
echo.