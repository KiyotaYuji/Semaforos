#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/src"
javac -d ../bin modelo/Veiculo.java
javac -d ../bin -cp ../bin fabrica/Fabrica.java
javac -d ../bin -cp ../bin loja/Loja.java
javac -d ../bin -cp ../bin cliente/Cliente.java
echo "Compilacao concluida"
