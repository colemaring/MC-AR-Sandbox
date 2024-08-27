@echo off
start /b /min "" cmd /c "runserver.bat"
start /b /min "" cmd /c "node server.js"
start /b /min "" cmd /c "python topoprojection.py"
exit /b 0
