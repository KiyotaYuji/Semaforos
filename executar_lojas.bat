@echo off
cd /d "%~dp0"
set HOST_FABRICA=192.168.64.100
set PORTA_FABRICA=5000
cd bin
start "Loja 1" cmd /k java -Dhost.fabrica=%HOST_FABRICA% -Dporta.fabrica=%PORTA_FABRICA% loja.Loja 1 6000
start "Loja 2" cmd /k java -Dhost.fabrica=%HOST_FABRICA% -Dporta.fabrica=%PORTA_FABRICA% loja.Loja 2 6001
start "Loja 3" cmd /k java -Dhost.fabrica=%HOST_FABRICA% -Dporta.fabrica=%PORTA_FABRICA% loja.Loja 3 6002
