@echo off
echo Compilando o sistema...
cd src
javac -d ..\bin modelo\Veiculo.java
javac -d ..\bin -cp ..\bin fabrica\Fabrica.java
javac -d ..\bin -cp ..\bin loja\Loja.java
javac -d ..\bin -cp ..\bin cliente\Cliente.java
cd ..
echo Compilacao concluida!
pause
