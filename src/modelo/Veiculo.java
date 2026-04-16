package modelo;

import java.io.Serializable;

public class Veiculo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String cor;
    private String tipo;
    private int idEstacao;
    private int idFuncionario;
    private int posicaoEsteiraFabrica;
    private Integer idLoja;
    private Integer posicaoEsteiraLoja;
    
    public Veiculo(int id, String cor, String tipo, int idEstacao, int idFuncionario, int posicaoEsteiraFabrica) {
        this.id = id;
        this.cor = cor;
        this.tipo = tipo;
        this.idEstacao = idEstacao;
        this.idFuncionario = idFuncionario;
        this.posicaoEsteiraFabrica = posicaoEsteiraFabrica;
    }
    
    public void setDadosLoja(int idLoja, int posicaoEsteiraLoja) {
        this.idLoja = idLoja;
        this.posicaoEsteiraLoja = posicaoEsteiraLoja;
    }
    
    public int getId() {
        return id;
    }
    
    public String getCor() {
        return cor;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public int getIdEstacao() {
        return idEstacao;
    }
    
    public int getIdFuncionario() {
        return idFuncionario;
    }
    
    public int getPosicaoEsteiraFabrica() {
        return posicaoEsteiraFabrica;
    }
    
    public Integer getIdLoja() {
        return idLoja;
    }
    
    public Integer getPosicaoEsteiraLoja() {
        return posicaoEsteiraLoja;
    }
    
    public String getLogProducao() {
        return String.format("Veiculo ID: %d | Cor: %s | Tipo: %s | Estacao: %d | Funcionario: %d | Pos_Esteira_Fabrica: %d",
            id, cor, tipo, idEstacao, idFuncionario, posicaoEsteiraFabrica);
    }
    
    public String getLogVendaLoja() {
        return String.format("%s | Loja: %d | Pos_Esteira_Loja: %d",
            getLogProducao(), idLoja, posicaoEsteiraLoja);
    }
    
    @Override
    public String toString() {
        if (idLoja != null) {
            return getLogVendaLoja();
        }
        return getLogProducao();
    }
}
