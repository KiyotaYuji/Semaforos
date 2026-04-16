package loja;

import modelo.Veiculo;
import java.util.concurrent.Semaphore;
import java.io.*;
import java.net.*;

public class Loja {
    
    private static final int CAPACIDADE_ESTEIRA = 50;
    private static final String HOST_FABRICA = System.getProperty("host.fabrica", "localhost");
    private static final int PORTA_FABRICA = Integer.parseInt(System.getProperty("porta.fabrica", "5000"));
    
    private int idLoja;
    private int portaLoja;
    
    private Semaphore espacoEsteira;
    private Semaphore veiculosDisponiveis;
    private Semaphore mutexEsteira;
    
    private Veiculo[] esteira;
    private int posicaoInsercao;
    private int posicaoRemocao;
    
    private Socket socketFabrica;
    private ObjectOutputStream outFabrica;
    private ObjectInputStream inFabrica;
    
    private PrintWriter logRecebimento;
    private PrintWriter logVenda;
    
    public Loja(int idLoja, int portaLoja) throws IOException {
        this.idLoja = idLoja;
        this.portaLoja = portaLoja;
        
        espacoEsteira = new Semaphore(CAPACIDADE_ESTEIRA);
        veiculosDisponiveis = new Semaphore(0);
        mutexEsteira = new Semaphore(1);
        
        esteira = new Veiculo[CAPACIDADE_ESTEIRA];
        posicaoInsercao = 0;
        posicaoRemocao = 0;
        
        logRecebimento = new PrintWriter(new FileWriter("log_recebimento_loja_" + idLoja + ".txt"), true);
        logVenda = new PrintWriter(new FileWriter("log_venda_loja_" + idLoja + ".txt"), true);
        
        conectarFabrica();
        iniciarReposicao();
        iniciarServidor();
    }
    
    private void conectarFabrica() throws IOException {
        socketFabrica = new Socket(HOST_FABRICA, PORTA_FABRICA);
        outFabrica = new ObjectOutputStream(socketFabrica.getOutputStream());
        inFabrica = new ObjectInputStream(socketFabrica.getInputStream());
        System.out.println("Loja " + idLoja + " conectada à fábrica");
    }
    
    private void iniciarReposicao() {
        new Thread(() -> {
            while (true) {
                try {
                    espacoEsteira.acquire();
                    
                    mutexEsteira.acquire();
                    int posicao = posicaoInsercao;
                    mutexEsteira.release();
                    
                    outFabrica.writeObject("SOLICITAR_VEICULO");
                    outFabrica.writeInt(idLoja);
                    outFabrica.writeInt(posicao);
                    outFabrica.flush();
                    
                    Veiculo veiculo = (Veiculo) inFabrica.readObject();
                    
                    mutexEsteira.acquire();
                    esteira[posicaoInsercao] = veiculo;
                    posicaoInsercao = (posicaoInsercao + 1) % CAPACIDADE_ESTEIRA;
                    mutexEsteira.release();
                    
                    veiculosDisponiveis.release();
                    
                    logRecebimento.println(veiculo.getLogVendaLoja());
                    System.out.println("LOJA " + idLoja + " RECEBEU: " + veiculo.getLogVendaLoja());
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }
    
    private void iniciarServidor() {
        new Thread(() -> {
            try (ServerSocket servidor = new ServerSocket(portaLoja)) {
                System.out.println("Loja " + idLoja + " iniciada na porta " + portaLoja);
                while (true) {
                    Socket clienteSocket = servidor.accept();
                    new Thread(new AtendimentoCliente(clienteSocket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    class AtendimentoCliente implements Runnable {
        private Socket socket;
        
        public AtendimentoCliente(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                
                while (true) {
                    String comando = (String) in.readObject();
                    
                    if ("COMPRAR_VEICULO".equals(comando)) {
                        int idCliente = in.readInt();
                        
                        veiculosDisponiveis.acquire();
                        
                        mutexEsteira.acquire();
                        Veiculo veiculo = esteira[posicaoRemocao];
                        esteira[posicaoRemocao] = null;
                        posicaoRemocao = (posicaoRemocao + 1) % CAPACIDADE_ESTEIRA;
                        mutexEsteira.release();
                        
                        espacoEsteira.release();
                        
                        logVenda.println("Cliente " + idCliente + " comprou: " + veiculo.getLogVendaLoja());
                        System.out.println("LOJA " + idLoja + " VENDEU para Cliente " + idCliente + ": " + veiculo);
                        
                        out.writeObject(veiculo);
                        out.flush();
                    }
                }
                
            } catch (Exception e) {
                System.out.println("Cliente desconectado da loja " + idLoja);
            }
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Uso: java loja.Loja <idLoja> <porta>");
            return;
        }
        
        try {
            int idLoja = Integer.parseInt(args[0]);
            int porta = Integer.parseInt(args[1]);
            new Loja(idLoja, porta);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
