@echo off
start /b /min "" cmd /c "runserver.bat"
start /b /min "" cmd /c "node server.js"
start /b /min "" cmd /c "python topoprojection.py"
start /b /min "" "C:\ProgramData\Microsoft\Windows\Start Menu\Programs\Minecraft Launcher\Minecraft Launcher.lnk"
exit /b 0
