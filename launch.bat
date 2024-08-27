@echo off
start /b /min "" cmd /c "runserver.bat"
start /b /min "" cmd /c "node server.js"
start /b /min "" cmd /c "python topoprojection.py"
start /b /min "" "C:\XboxGames\Minecraft Launcher\Content\Minecraft.exe"
exit /b 0
