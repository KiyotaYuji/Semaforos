package fabrica;

import modelo.Veiculo;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.net.*;
import java.util.*;

public class Fabrica {
    
    private static final int CAPACIDADE_ESTOQUE = 500;
    private static final int CAPACIDADE_ESTEIRA_DISTRIBUICAO = 5;
    private static final int CAPACIDADE_ESTEIRA_CIRCULAR = 40;
    private static final int NUM_ESTACOES = 4;
    private static final int NUM_FUNCIONARIOS_POR_ESTACAO = 5;
    private static final int PORTA_SERVIDOR = 5000;
    
    private Semaphore estoqueDisponivel;
    private Semaphore esteiraDistribuicao;
    private Semaphore espacoEsteiraCircular;
    private Semaphore mutexEsteiraCircular;
    private Semaphore mutexProducao;
    private Semaphore veiculosNaEsteira;
    
    private Veiculo[] esteiraCircular;
    private int posicaoInsercao;
    private int posicaoRemocao;
    
    private AtomicInteger contadorVeiculos;
    private String[] cores = {"Vermelho", "Verde", "Azul"};
    private String[] tipos = {"SUV", "SEDAN"};
    private int indiceCor;
    
    private PrintWriter logProducao;
    private PrintWriter logVenda;
    
    public Fabrica() throws IOException {
        estoqueDisponivel = new Semaphore(CAPACIDADE_ESTOQUE);
        esteiraDistribuicao = new Semaphore(CAPACIDADE_ESTEIRA_DISTRIBUICAO);
        espacoEsteiraCircular = new Semaphore(CAPACIDADE_ESTEIRA_CIRCULAR);
        mutexEsteiraCircular = new Semaphore(1);
        mutexProducao = new Semaphore(1);
        veiculosNaEsteira = new Semaphore(0);
        
        esteiraCircular = new Veiculo[CAPACIDADE_ESTEIRA_CIRCULAR];
        posicaoInsercao = 0;
        posicaoRemocao = 0;
        
        contadorVeiculos = new AtomicInteger(0);
        indiceCor = 0;
        
        logProducao = new PrintWriter(new FileWriter("log_producao_fabrica.txt"), true);
        logVenda = new PrintWriter(new FileWriter("log_venda_fabrica.txt"), true);
        
        iniciarProducao();
        iniciarServidor();
    }
    
    private void iniciarProducao() {
        for (int i = 0; i < NUM_ESTACOES; i++) {
            new Estacao(i).iniciar();
        }
    }
    
    private void iniciarServidor() {
        new Thread(() -> {
            try (ServerSocket servidor = new ServerSocket(PORTA_SERVIDOR)) {
                System.out.println("Fabrica iniciada na porta " + PORTA_SERVIDOR);
                while (true) {
                    Socket clienteSocket = servidor.accept();
                    new Thread(new AtendimentoLoja(clienteSocket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private String getProximaCor() throws InterruptedException {
        mutexProducao.acquire();
        String cor = cores[indiceCor];
        indiceCor = (indiceCor + 1) % cores.length;
        mutexProducao.release();
        return cor;
    }
    
    private String getTipoAleatorio() {
        return tipos[new Random().nextInt(tipos.length)];
    }
    
    private void adicionarVeiculoEsteira(Veiculo veiculo) throws InterruptedException {
        espacoEsteiraCircular.acquire();
        mutexEsteiraCircular.acquire();
        
        veiculo = new Veiculo(veiculo.getId(), veiculo.getCor(), veiculo.getTipo(),
                             veiculo.getIdEstacao(), veiculo.getIdFuncionario(), posicaoInsercao);
        esteiraCircular[posicaoInsercao] = veiculo;
        
        logProducao.println(veiculo.getLogProducao());
        System.out.println("PRODUCAO: " + veiculo.getLogProducao());
        
        posicaoInsercao = (posicaoInsercao + 1) % CAPACIDADE_ESTEIRA_CIRCULAR;
        
        mutexEsteiraCircular.release();
        veiculosNaEsteira.release();
    }
    
    private Veiculo removerVeiculoEsteira() throws InterruptedException {
        veiculosNaEsteira.acquire();
        mutexEsteiraCircular.acquire();
        
        Veiculo veiculo = esteiraCircular[posicaoRemocao];
        esteiraCircular[posicaoRemocao] = null;
        posicaoRemocao = (posicaoRemocao + 1) % CAPACIDADE_ESTEIRA_CIRCULAR;
        
        mutexEsteiraCircular.release();
        espacoEsteiraCircular.release();
        
        return veiculo;
    }
    
    class Estacao {
        private int idEstacao;
        private Semaphore[] ferramentas;
        private Semaphore limiteFuncionarios;
        
        public Estacao(int idEstacao) {
            this.idEstacao = idEstacao;
            this.ferramentas = new Semaphore[NUM_FUNCIONARIOS_POR_ESTACAO];
            for (int i = 0; i < NUM_FUNCIONARIOS_POR_ESTACAO; i++) {
                ferramentas[i] = new Semaphore(1);
            }
            this.limiteFuncionarios = new Semaphore(NUM_FUNCIONARIOS_POR_ESTACAO - 1);
        }
        
        public void iniciar() {
            for (int i = 0; i < NUM_FUNCIONARIOS_POR_ESTACAO; i++) {
                new Thread(new Funcionario(i)).start();
            }
        }
        
        class Funcionario implements Runnable {
            private int idFuncionario;
            
            public Funcionario(int idFuncionario) {
                this.idFuncionario = idFuncionario;
            }
            
            @Override
            public void run() {
                while (true) {
                    try {
                        estoqueDisponivel.acquire();
                        esteiraDistribuicao.acquire();
                        
                        limiteFuncionarios.acquire();
                        
                        int ferramentaEsq = idFuncionario;
                        int ferramentaDir = (idFuncionario + 1) % NUM_FUNCIONARIOS_POR_ESTACAO;
                        
                        if (ferramentaEsq < ferramentaDir) {
                            ferramentas[ferramentaEsq].acquire();
                            ferramentas[ferramentaDir].acquire();
                        } else {
                            ferramentas[ferramentaDir].acquire();
                            ferramentas[ferramentaEsq].acquire();
                        }
                        
                        int idVeiculo = contadorVeiculos.incrementAndGet();
                        String cor = getProximaCor();
                        String tipo = getTipoAleatorio();
                        
                        Veiculo veiculo = new Veiculo(idVeiculo, cor, tipo, idEstacao, idFuncionario, -1);
                        
                        Thread.sleep(100);
                        
                        adicionarVeiculoEsteira(veiculo);
                        
                        ferramentas[ferramentaEsq].release();
                        ferramentas[ferramentaDir].release();
                        limiteFuncionarios.release();
                        
                        esteiraDistribuicao.release();
                        
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }
    
    class AtendimentoLoja implements Runnable {
        private Socket socket;
        
        public AtendimentoLoja(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                
                while (true) {
                    String comando = (String) in.readObject();
                    
                    if ("SOLICITAR_VEICULO".equals(comando)) {
                        int idLoja = in.readInt();
                        int posicaoLoja = in.readInt();
                        
                        Veiculo veiculo = removerVeiculoEsteira();
                        
                        veiculo.setDadosLoja(idLoja, posicaoLoja);
                        
                        logVenda.println(veiculo.getLogVendaLoja());
                        System.out.println("VENDA: " + veiculo.getLogVendaLoja());
                        
                        out.writeObject(veiculo);
                        out.flush();
                    }
                }
                
            } catch (Exception e) {
                System.out.println("Loja desconectada");
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            new Fabrica();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
