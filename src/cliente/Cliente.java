package cliente;

import modelo.Veiculo;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Cliente implements Runnable {
    
    private static final int[] PORTAS_LOJAS = {6000, 6001, 6002};
    private static final int CAPACIDADE_GARAGEM = 20;
    private static final String[] HOSTS_LOJAS = carregarHostsLojas();
    
    private int idCliente;
    private List<Veiculo> garagem;
    private Semaphore mutexGaragem;
    private Random random;
    
    public Cliente(int idCliente) {
        this.idCliente = idCliente;
        this.garagem = new ArrayList<>();
        this.mutexGaragem = new Semaphore(1);
        this.random = new Random();
    }
    
    @Override
    public void run() {
        System.out.println("Cliente " + idCliente + " iniciado");
        
        int numCompras = random.nextInt(5) + 1;
        
        for (int i = 0; i < numCompras; i++) {
            try {
                int idxLoja = random.nextInt(PORTAS_LOJAS.length);
                int portaLoja = PORTAS_LOJAS[idxLoja];
                String hostLoja = HOSTS_LOJAS.length > idxLoja ? HOSTS_LOJAS[idxLoja] : HOSTS_LOJAS[0];
                
                Socket socket = new Socket(hostLoja, portaLoja);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                
                out.writeObject("COMPRAR_VEICULO");
                out.writeInt(idCliente);
                out.flush();
                
                Veiculo veiculo = (Veiculo) in.readObject();
                
                mutexGaragem.acquire();
                if (garagem.size() < CAPACIDADE_GARAGEM) {
                    garagem.add(veiculo);
                    System.out.println("Cliente " + idCliente + " comprou veiculo " + veiculo.getId() + 
                                     " da loja " + veiculo.getIdLoja() + " (Total: " + garagem.size() + ")");
                }
                mutexGaragem.release();
                
                socket.close();
                
                Thread.sleep(random.nextInt(1000) + 500);
                
            } catch (Exception e) {
                System.out.println("Cliente " + idCliente + " erro ao comprar: " + e.getMessage());
            }
        }
        
        System.out.println("Cliente " + idCliente + " finalizou compras. Total: " + garagem.size() + " veiculos");
    }
    
    public static void main(String[] args) {
        System.out.println("Iniciando sistema de clientes...");
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<Thread> threads = new ArrayList<>();
        
        for (int i = 1; i <= 20; i++) {
            Thread thread = new Thread(new Cliente(i));
            threads.add(thread);
            thread.start();
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("Todos os clientes finalizaram suas compras");
    }

    private static String[] carregarHostsLojas() {
        String prop = System.getProperty("hosts.lojas");
        if (prop == null || prop.trim().isEmpty()) {
            prop = System.getProperty("host.lojas", "localhost");
        }
        String[] partes = prop.split(",");
        List<String> hosts = new ArrayList<>();
        for (String p : partes) {
            String h = p.trim();
            if (!h.isEmpty()) hosts.add(h);
        }
        if (hosts.isEmpty()) {
            return new String[] {"localhost"};
        }
        return hosts.toArray(new String[0]);
    }
}
