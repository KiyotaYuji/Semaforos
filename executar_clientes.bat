@echo off
cd /d "%~dp0"
rem Um host por loja (na ordem das portas 6000,6001,6002)
rem 
set HOSTS_LOJAS=192.168.64.121,192.168.64.121,192.168.64.121
cd bin
java -Dhosts.lojas=%HOSTS_LOJAS% cliente.Cliente
pause
