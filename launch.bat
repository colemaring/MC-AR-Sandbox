@echo off
start "" "runserver.bat"
node "server.js"
python topoprojection.py
