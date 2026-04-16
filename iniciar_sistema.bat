@echo off
echo ========================================
echo Sistema de Producao de Veiculos
echo ========================================
echo.
echo Iniciando sistema distribuido...
echo.
echo 1. Iniciando Fabrica...
start "Fabrica" executar_fabrica.bat
timeout /t 3 /nobreak >nul

echo 2. Iniciando Lojas...
start "Loja 1" executar_loja1.bat
timeout /t 2 /nobreak >nul
start "Loja 2" executar_loja2.bat
timeout /t 2 /nobreak >nul
start "Loja 3" executar_loja3.bat
timeout /t 2 /nobreak >nul

echo 3. Aguardando estabilizacao...
timeout /t 3 /nobreak >nul

echo 4. Iniciando Clientes...
start "Clientes" executar_clientes.bat

echo.
echo Sistema iniciado com sucesso!
echo.
echo Terminais abertos:
echo - Fabrica (Porta 5000)
echo - Loja 1 (Porta 6000)
echo - Loja 2 (Porta 6001)
echo - Loja 3 (Porta 6002)
echo - Clientes (20 threads)
echo.
pause
