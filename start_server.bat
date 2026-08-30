@echo off
title Presentation Clicker Laptop Server
cd /d "%~dp0"
echo ===================================================
echo   Starting Wear OS Presentation Clicker Server...
echo ===================================================
python server\server.py
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Server stopped or encountered an error.
    pause
)
