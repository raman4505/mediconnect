@echo off
setlocal
cd /d "%~dp0"

if not exist "target\mediconnect-1.0.0.jar" (
  if exist ".tools\apache-maven-3.9.16\bin\mvn.cmd" (
    call ".tools\apache-maven-3.9.16\bin\mvn.cmd" -q -DskipTests package
  ) else (
    call mvn -q -DskipTests package
  )
  if errorlevel 1 (
    echo Build failed. Install Java 17+ and Maven, then run this launcher again.
    pause
    exit /b 1
  )
)

set "JAVA_CMD=java"
if defined JAVA_HOME set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
start "MediConnect server" /min "%JAVA_CMD%" -jar "target\mediconnect-1.0.0.jar"
timeout /t 6 /nobreak >nul
start "" "http://localhost:8080"
echo MediConnect is starting at http://localhost:8080
echo Close the MediConnect server window to stop the website.
pause
