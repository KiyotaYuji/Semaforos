# Sistema Distribuído de Produção e Comercialização de Veículos

## Visão Geral

Sistema didático em Java que simula uma cadeia de produção e comercialização de veículos utilizando programação concorrente e arquitetura distribuída. O sistema implementa sincronização exclusivamente com **Semaphores** (java.util.concurrent.Semaphore) e comunicação via **Sockets**.

## Arquitetura do Sistema

### 1. Fábrica (Servidor - Porta 5000)
- **Estoque**: Capacidade de 500 peças controlada por Semaphore
- **Esteira de Distribuição**: Suporta 5 solicitações simultâneas
- **4 Estações de Produção**: Cada uma com 5 funcionários
- **Problema dos Filósofos**: Funcionários precisam de 2 ferramentas adjacentes para produzir
- **Esteira Circular**: Buffer circular com 40 posições para veículos prontos
- **Logs**: Gera log_producao_fabrica.txt e log_venda_fabrica.txt

### 2. Lojas (3 instâncias - Portas 6000, 6001, 6002)
- Clientes da fábrica (conectam via Socket)
- Servidores para clientes finais
- **Esteira Circular**: Buffer de 50 veículos
- **Logs**: Gera log_recebimento_loja_X.txt e log_venda_loja_X.txt

### 3. Clientes (20 threads)
- Escolhem lojas aleatoriamente
- Compram de 1 a 5 veículos cada
- Armazenam em garagem local (capacidade 20)

## Como Executar

### Passo 1: Compilar
```bash
compilar.bat
```

### Passo 2: Iniciar os componentes (nesta ordem)

1. **Iniciar a Fábrica** (Terminal 1):
```bash
executar_fabrica.bat
```

2. **Iniciar as 3 Lojas** (Terminais 2, 3 e 4):
```bash
executar_loja1.bat
executar_loja2.bat
executar_loja3.bat
```

3. **Iniciar os Clientes** (Terminal 5):
```bash
executar_clientes.bat
```

## Estrutura de Dados do Veículo

Cada veículo carrega toda a sua cadeia produtiva:
- ID sequencial
- Cor (alternância RGB)
- Tipo (SUV/SEDAN aleatório)
- ID da Estação
- ID do Funcionário
- Posição na Esteira da Fábrica
- ID da Loja (ao ser vendido)
- Posição na Esteira da Loja

## Sincronização com Semaphores

### Problema dos Filósofos (Fábrica)
- **Ferramentas**: 5 Semaphores binários por estação
- **Limitador**: Semaphore com 4 permits (N-1) previne deadlock
- **Ordem**: Funcionários pegam ferramentas em ordem crescente de ID

### Estoque e Esteiras
- **estoqueDisponivel**: Semaphore(500) - controla peças disponíveis
- **esteiraDistribuicao**: Semaphore(5) - limita acesso simultâneo
- **espacoEsteiraCircular**: Semaphore(40) - controla espaço no buffer
- **mutexEsteiraCircular**: Semaphore(1) - exclusão mútua para inserção/remoção

### Lojas
- **espacoEsteira**: Semaphore(50) - controla espaço disponível
- **veiculosDisponiveis**: Semaphore(0) - sinaliza veículos prontos para venda
- **mutexEsteira**: Semaphore(1) - protege operações críticas

## Prevenção de Deadlock e Starvation

1. **Ordem de Aquisição**: Ferramentas adquiridas em ordem crescente
2. **Limitador N-1**: Máximo de 4 funcionários simultâneos por estação
3. **Fairness**: Semaphores em modo justo (FIFO implícito)
4. **Timeouts**: Sistema aguarda disponibilidade sem polling ativo

## Logs Gerados

- `log_producao_fabrica.txt`: Todos os veículos produzidos
- `log_venda_fabrica.txt`: Veículos vendidos para lojas
- `log_recebimento_loja_1.txt`: Veículos recebidos pela Loja 1
- `log_venda_loja_1.txt`: Vendas da Loja 1 para clientes
- (Similar para Lojas 2 e 3)

## Observações Técnicas

- **Código Limpo**: Sem comentários embutidos para apresentação acadêmica
- **Sincronização**: 100% via Semaphore (sem synchronized/ReentrantLock)
- **Distribuição**: Comunicação via Sockets TCP/IP
- **Serialização**: Objetos Veiculo transmitidos via ObjectOutputStream
- **Thread-Safety**: Todas as estruturas compartilhadas protegidas por Semaphores
