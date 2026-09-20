@echo off
setlocal enabledelayedexpansion
title Android Wireless Debugging Connector - BitChord

:: Locate ADB executable
set "ADB=adb"
where adb >nul 2>nul
if %errorlevel% neq 0 (
    if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
        set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
    ) else (
        echo [ERROR] adb.exe not found in PATH or Android SDK folder.
        echo Please ensure Android SDK Platform-Tools are installed.
        pause
        exit /b 1
    )
)

:: Cache file for last used IP
set "LAST_IP_FILE=%~dp0.last_adb_ip"
set "LAST_IP=192.168.29.5"
if exist "%LAST_IP_FILE%" (
    set /p LAST_IP=<"%LAST_IP_FILE%"
)

:MENU
cls
echo ================================================================
echo             BITCHORD - WIRELESS DEBUGGING CONNECTOR
echo ================================================================
echo.
echo Make sure your phone and PC are connected to the SAME Wi-Fi network!
echo Phone: Settings -^> Developer options -^> Wireless debugging (ON)
echo.

:: Check for auto-discovered mDNS devices
set "MDNS_FOUND="
for /f "tokens=1,2,3" %%A in ('"%ADB%" mdns services 2^>nul') do (
    if "%%B"=="_adb-tls-connect._tcp" (
        set "MDNS_FOUND=%%C"
    )
)

if not "!MDNS_FOUND!"=="" (
    echo [*] Discovered Device via Wi-Fi: !MDNS_FOUND!
    echo.
    echo   [0] AUTO-CONNECT to !MDNS_FOUND! ^(Default - Press Enter^)
    echo   [1] Enter IP and Port manually
) else (
    echo   [1] Quick Connect with IP:Port ^(Default - Press Enter^)
)
echo   [2] Pair New Device ^(Android 11+ 6-digit code^)
echo   [3] Switch from USB to Wireless ^(adb tcpip 5555^)
echo   [4] Check Connected Devices ^(adb devices -l^)
echo   [5] Disconnect all wireless devices
echo   [6] Restart ADB Server
echo   [7] Install ^& Launch BitChord on Device ^(gradlew installDevDebug^)
echo   [8] Exit
echo ================================================================
echo.

if not "!MDNS_FOUND!"=="" (
    set /p CHOICE="Select option [0-8] (Default is 0): "
    if "!CHOICE!"=="" set "CHOICE=0"
) else (
    set /p CHOICE="Select option [1-8] (Default is 1): "
    if "!CHOICE!"=="" set "CHOICE=1"
)

if "%CHOICE%"=="0" goto AUTOCONNECT
if "%CHOICE%"=="1" goto CONNECT
if "%CHOICE%"=="2" goto PAIR
if "%CHOICE%"=="3" goto TCPIP
if "%CHOICE%"=="4" goto DEVICES
if "%CHOICE%"=="5" goto DISCONNECT
if "%CHOICE%"=="6" goto RESTART_ADB
if "%CHOICE%"=="7" goto INSTALL_APP
if "%CHOICE%"=="8" exit /b 0
goto MENU

:AUTOCONNECT
cls
echo ================================================================
echo              AUTO-CONNECTING TO DISCOVERED DEVICE
echo ================================================================
echo.
if "!MDNS_FOUND!"=="" (
    echo No device was auto-detected. Switching to manual connect...
    timeout /t 2 >nul
    goto CONNECT
)
echo Target: !MDNS_FOUND!
echo.
"%ADB%" connect !MDNS_FOUND!
echo.
echo Connected Devices:
"%ADB%" devices -l
echo.
for /f "tokens=1 delims=:" %%a in ("!MDNS_FOUND!") do (
    echo %%a>"%LAST_IP_FILE%"
)
echo Press any key to return to menu...
pause >nul
goto MENU

:CONNECT
cls
echo ================================================================
echo                     [1] CONNECT WITH IP:PORT
echo ================================================================
echo.
echo On your phone, look at the main "Wireless debugging" screen:
echo Find: "IP address & Port" (e.g. 192.168.1.5:41235)
echo.
set /p INPUT_ADDR="Enter IP:Port or IP [Default: %LAST_IP%]: "
if "%INPUT_ADDR%"=="" set "INPUT_ADDR=%LAST_IP%"

:: Check if user included port with colon
echo %INPUT_ADDR% | findstr ":" >nul
if %errorlevel% equ 0 (
    set "TARGET_ADDR=%INPUT_ADDR%"
    for /f "tokens=1 delims=:" %%a in ("%INPUT_ADDR%") do (
        echo %%a>"%LAST_IP_FILE%"
    )
) else (
    set "TARGET_IP=%INPUT_ADDR%"
    echo !TARGET_IP!>"%LAST_IP_FILE%"
    set /p TARGET_PORT="Enter Port (from phone screen): "
    if "!TARGET_PORT!"=="" (
        echo [ERROR] Port is required for Android Wireless Debugging.
        pause
        goto MENU
    )
    set "TARGET_ADDR=!TARGET_IP!:!TARGET_PORT!"
)

echo.
echo Connecting to %TARGET_ADDR%...
"%ADB%" connect %TARGET_ADDR%
echo.
echo Connected Devices:
"%ADB%" devices -l
echo.
pause
goto MENU

:PAIR
cls
echo ================================================================
echo             [2] PAIR NEW DEVICE (Android 11+)
echo ================================================================
echo.
echo On your phone:
echo 1. In "Wireless debugging", tap "Pair device with pairing code".
echo 2. A dialog will pop up showing:
echo    - "Wi-Fi pairing code" (6 digits)
echo    - "IP address & Port"  (NOTE: pairing port is different from connect port!)
echo.
set /p PAIR_ADDR="Enter Pairing IP:Port (from popup dialog): "
if "%PAIR_ADDR%"=="" (
    echo [ERROR] Pairing address cannot be empty.
    pause
    goto MENU
)
set /p PAIR_CODE="Enter 6-digit Pairing Code: "
if "%PAIR_CODE%"=="" (
    echo [ERROR] Pairing code cannot be empty.
    pause
    goto MENU
)

echo.
echo Pairing with %PAIR_ADDR% using code %PAIR_CODE%...
"%ADB%" pair %PAIR_ADDR% %PAIR_CODE%
if %errorlevel% neq 0 (
    echo.
    echo [WARNING] Pairing failed. Check that phone is on same Wi-Fi and code is correct.
    pause
    goto MENU
)

echo.
echo [SUCCESS] Device successfully paired!
echo.
echo Now close the pairing popup on your phone.
echo Look at the MAIN "Wireless debugging" screen for the CONNECT Port.
echo.
for /f "tokens=1 delims=:" %%a in ("%PAIR_ADDR%") do set "DEV_IP=%%a"
echo Device IP is: %DEV_IP%
echo %DEV_IP%>"%LAST_IP_FILE%"
set /p CONNECT_PORT="Enter the CONNECT Port (from main Wireless debugging screen): "
if not "%CONNECT_PORT%"=="" (
    echo.
    echo Connecting to %DEV_IP%:%CONNECT_PORT%...
    "%ADB%" connect %DEV_IP%:%CONNECT_PORT%
    echo.
    "%ADB%" devices -l
)
echo.
pause
goto MENU

:TCPIP
cls
echo ================================================================
echo        [3] SWITCH FROM USB TO WIRELESS (PORT 5555)
echo ================================================================
echo.
echo 1. Connect your phone via USB cable first.
echo 2. Make sure USB Debugging is allowed.
echo.
pause
echo.
echo Restarting ADB in TCP/IP mode on port 5555...
"%ADB%" tcpip 5555
if %errorlevel% neq 0 (
    echo [ERROR] Could not switch to TCP/IP. Is your phone connected via USB?
    pause
    goto MENU
)
echo.
echo You can unplug the USB cable now.
set /p PHONE_IP="Enter your phone's Wi-Fi IP address [Default: %LAST_IP%]: "
if "%PHONE_IP%"=="" set "PHONE_IP=%LAST_IP%"
echo %PHONE_IP%>"%LAST_IP_FILE%"
echo Connecting to %PHONE_IP%:5555...
"%ADB%" connect %PHONE_IP%:5555
echo.
"%ADB%" devices -l
echo.
pause
goto MENU

:DEVICES
cls
echo ================================================================
echo                    CONNECTED ADB DEVICES
echo ================================================================
echo.
"%ADB%" devices -l
echo.
pause
goto MENU

:DISCONNECT
cls
echo Disconnecting all ADB devices...
"%ADB%" disconnect
echo Done.
echo.
"%ADB%" devices -l
echo.
pause
goto MENU

:RESTART_ADB
cls
echo Restarting ADB Server...
"%ADB%" kill-server
"%ADB%" start-server
echo Done.
echo.
pause
goto MENU

:INSTALL_APP
cls
echo ================================================================
echo              INSTALL & RUN BITCHORD ON DEVICE
echo ================================================================
echo.
echo Checking connected devices...
"%ADB%" devices | findstr /R "device$" >nul
if %errorlevel% neq 0 (
    echo [ERROR] No authorized device is currently connected.
    echo Please connect your device first using option 0 or 1.
    pause
    goto MENU
)
echo Device detected! Building and installing devDebug build...
echo.
cd /d "%~dp0"
call gradlew.bat :app:installDevDebug
if %errorlevel% equ 0 (
    echo.
    echo [SUCCESS] App installed! Launching BitChord...
    "%ADB%" shell am start -n com.music.bitchord/com.music.bitchord.MainActivity
) else (
    echo.
    echo [ERROR] Build or installation failed. Check gradle output above.
)
echo.
pause
goto MENU
