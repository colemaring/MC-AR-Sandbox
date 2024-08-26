@echo off
start /min "" "runserver.bat"
start /min "" node server.js
start /min "" python topoprojection.py
