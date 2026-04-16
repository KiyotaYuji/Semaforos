@echo off
title Loja 1
cd /d "%~dp0"
set HOST_FABRICA=192.168.64.100
set PORTA_FABRICA=5000
cd bin
java -Dhost.fabrica=%HOST_FABRICA% -Dporta.fabrica=%PORTA_FABRICA% loja.Loja 1 6000
pause
