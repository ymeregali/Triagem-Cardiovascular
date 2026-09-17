import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

enum Genero {
    HOMEM, MULHER
}

class Paciente {
    private String nome;
    private int idade;
    private Genero genero;
    private double imc;
    private boolean fuma;
    private boolean praticaExercicios;
    
    // Dados clínicos opcionais
    private int pressaoSistolica;
    private boolean pressaoTratada;
    private int colesterolTotal;
    private int hdl;
    private boolean modoSimplificado;

    // Construtor para Modo Simplificado (Baseado em IMC e hábitos)
    public Paciente(String nome, int idade, Genero genero, double imc, boolean fuma, boolean praticaExercicios) {
        this.nome = nome;
        this.idade = idade;
        this.genero = genero;
        this.imc = imc;
        this.fuma = fuma;
        this.praticaExercicios = praticaExercicios;
        this.modoSimplificado = true;
    }

    // Construtor para Modo Completo (Framingham)
    public Paciente(String nome, int idade, Genero genero, double imc, boolean fuma, boolean praticaExercicios,
                    int pressaoSistolica, boolean pressaoTratada, int colesterolTotal, int hdl) {
        this.nome = nome;
        this.idade = idade;
        this.genero = genero;
        this.imc = imc;
        this.fuma = fuma;
        this.praticaExercicios = praticaExercicios;
        this.pressaoSistolica = pressaoSistolica;
        this.pressaoTratada = pressaoTratada;
        this.colesterolTotal = colesterolTotal;
        this.hdl = hdl;
        this.modoSimplificado = false;
    }

    public String getNome() { return nome; }
    public int getIdade() { return idade; }
    public Genero getGenero() { return genero; }
    public double getImc() { return imc; }
    public boolean isFuma() { return fuma; }
    public boolean isPraticaExercicios() { return praticaExercicios; }
    public int getPressaoSistolica() { return pressaoSistolica; }
    public boolean isPressaoTratada() { return pressaoTratada; }
    public int getColesterolTotal() { return colesterolTotal; }
    public int getHdl() { return hdl; }
    public boolean isModoSimplificado() { return modoSimplificado; }
}

class AvaliadorCardio {

    // --- TABELAS FRAMINGHAM (MODO COMPLETO) ---
    private int calcularPontosIdade(int idade, Genero g) {
        if (idade < 35) return g == Genero.HOMEM ? -9 : -7;
        if (idade < 40) return g == Genero.HOMEM ? -4 : -3;
        if (idade < 45) return 0;
        if (idade < 50) return 3;
        if (idade < 55) return 6;
        if (idade < 60) return 8;
        if (idade < 65) return g == Genero.HOMEM ? 10 : 12;
        if (idade < 70) return g == Genero.HOMEM ? 11 : 12;
        if (idade < 75) return g == Genero.HOMEM ? 12 : 14;
        return g == Genero.HOMEM ? 13 : 16;
    }

    private int calcularPontosColesterol(int col, int idade, Genero g) {
        if (col < 160) return 0;
        if (g == Genero.HOMEM) {
            if (col < 200) return (idade < 40) ? 4 : (idade < 50) ? 3 : (idade < 60) ? 2 : (idade < 70) ? 1 : 0;
            if (col < 240) return (idade < 40) ? 7 : (idade < 50) ? 5 : (idade < 60) ? 3 : (idade < 70) ? 1 : 0;
            if (col < 280) return (idade < 40) ? 9 : (idade < 50) ? 6 : (idade < 60) ? 4 : (idade < 70) ? 2 : 1;
            return (idade < 40) ? 11 : (idade < 50) ? 8 : (idade < 60) ? 5 : (idade < 70) ? 3 : 1;
        } else {
            if (col < 200) return (idade < 40) ? 4 : (idade < 50) ? 3 : (idade < 60) ? 2 : (idade < 70) ? 1 : 1;
            if (col < 240) return (idade < 40) ? 8 : (idade < 50) ? 6 : (idade < 60) ? 4 : (idade < 70) ? 2 : 1;
            if (col < 280) return (idade < 40) ? 11 : (idade < 50) ? 8 : (idade < 60) ? 5 : (idade < 70) ? 3 : 2;
            return (idade < 40) ? 13 : (idade < 50) ? 10 : (idade < 60) ? 7 : (idade < 70) ? 4 : 2;
        }
    }

    private int calcularPontosFumo(boolean fuma, int idade, Genero g) {
        if (!fuma) return 0;
        if (g == Genero.HOMEM) {
            return (idade < 40) ? 8 : (idade < 50) ? 5 : (idade < 60) ? 3 : (idade < 70) ? 1 : 1;
        } else {
            return (idade < 40) ? 9 : (idade < 50) ? 7 : (idade < 60) ? 4 : (idade < 70) ? 2 : 1;
        }
    }

    private int calcularPontosHDL(int hdl) {
        if (hdl >= 60) return -1;
        if (hdl >= 50) return 0;
        if (hdl >= 40) return 1;
        return 2;
    }

    private int calcularPontosPAS(int pas, boolean tratada, Genero g) {
        if (pas < 120) return 0;
        if (g == Genero.HOMEM) {
            if (pas < 130) return tratada ? 1 : 0;
            if (pas < 140) return tratada ? 2 : 1;
            if (pas < 160) return tratada ? 2 : 1;
            return tratada ? 3 : 2;
        } else {
            if (pas < 130) return tratada ? 3 : 1;
            if (pas < 140) return tratada ? 4 : 2;
            if (pas < 160) return tratada ? 5 : 3;
            return tratada ? 6 : 4;
        }
    }

    // --- AVALIAÇÃO SIMPLIFICADA (BASEADA EM IMC E ESTILO DE VIDA) ---
    private int calcularPontosIMC(double imc) {
        if (imc < 25.0) return 0;      // Eutrófico / Normal
        if (imc < 30.0) return 2;      // Sobrepeso
        if (imc < 35.0) return 4;      // Obesidade Grau I
        return 6;                      // Obesidade Grau II/III
    }

    public ResultadoAvaliacao avaliar(Paciente p) {
        int pontos = 0;
        String classificacaoRisco;
        String alertaCor;

        if (p.isModoSimplificado()) {
            // Lógica do Caminho Alternativo (IMC + Idade + Hábitos)
            pontos += (p.getIdade() >= 45) ? 3 : 0;
            pontos += calcularPontosIMC(p.getImc());
            if (p.isFuma()) pontos += 4;
            if (!p.isPraticaExercicios()) pontos += 2;

            if (pontos <= 2) {
                classificacaoRisco = "Risco Estimado Baixo (Baseado em IMC e Hábitos)";
                alertaCor = "Verde";
            } else if (pontos <= 5) {
                classificacaoRisco = "Risco Estimado Moderado (Atenção ao IMC/Hábitos)";
                alertaCor = "Amarelo";
            } else {
                classificacaoRisco = "Risco Estimado Elevado (Necessita Exames Clínicos)";
                alertaCor = "Vermelho";
            }
        } else {
            // Lógica do Framingham Completo
            pontos += calcularPontosIdade(p.getIdade(), p.getGenero());
            pontos += calcularPontosColesterol(p.getColesterolTotal(), p.getIdade(), p.getGenero());
            pontos += calcularPontosFumo(p.isFuma(), p.getIdade(), p.getGenero());
            pontos += calcularPontosHDL(p.getHdl());
            pontos += calcularPontosPAS(p.getPressaoSistolica(), p.isPressaoTratada(), p.getGenero());

            if (pontos < 10) {
                classificacaoRisco = "Baixo Risco (< 10% em 10 anos)";
                alertaCor = "Verde";
            } else if (pontos <= 14) {
                classificacaoRisco = "Risco Moderado (10% a 20%)";
                alertaCor = "Amarelo";
            } else {
                classificacaoRisco = "Alto Risco (> 20%)";
                alertaCor = "Vermelho";
            }
        }

        List<String> recomendacoes = gerarRecomendacoes(p);
        return new ResultadoAvaliacao(p, pontos, classificacaoRisco, alertaCor, recomendacoes);
    }

    private List<String> gerarRecomendacoes(Paciente p) {
        List<String> dicas = new ArrayList<>();
        if (p.isModoSimplificado()) {
            dicas.add("- NOTA: Esta foi uma simulação rápida baseada no IMC. Recomendamos aferir a pressão e realizar exames de sangue.");
        }
        if (p.getImc() >= 25.0) dicas.add("- Buscar orientação nutricional e readequação do IMC.");
        if (!p.isPraticaExercicios()) dicas.add("- Iniciar atividades físicas regulares (mínimo 150 min/semana).");
        if (p.isFuma()) dicas.add("- Cessação do tabagismo.");

        if (dicas.isEmpty()) {
            dicas.add("- Mantenha seus bons hábitos de vida e exames preventivos em dia.");
        }
        return dicas;
    }
}

class ResultadoAvaliacao {
    private final Paciente paciente;
    private final int pontos;
    private final String classificacaoRisco;
    private final String alertaCor;
    private final List<String> recomendacoes;

    public ResultadoAvaliacao(Paciente paciente, int pontos, String classificacaoRisco, String alertaCor, List<String> recomendacoes) {
        this.paciente = paciente;
        this.pontos = pontos;
        this.classificacaoRisco = classificacaoRisco;
        this.alertaCor = alertaCor;
        this.recomendacoes = recomendacoes;
    }

    public void exibir() {
        System.out.println("\n=======================================================");
        System.out.println("            RESULTADO DA SIMULAÇÃO DE RISCO");
        System.out.println("=======================================================");
        System.out.println("Paciente: " + paciente.getNome() + " | Idade: " + paciente.getIdade() + " anos");
        System.out.println("Tipo de Avaliação: " + (paciente.isModoSimplificado() ? "Triagem Antropométrica (IMC + Estilo de Vida)" : "Escore Clínico de Framingham"));
        System.out.println("IMC Atual: " + String.format("%.1f", paciente.getImc()));
        System.out.println("Pontuação do Modelo: " + pontos + " pontos");
        System.out.println("Classificação de Risco: " + classificacaoRisco);
        System.out.println("Status: [" + alertaCor + "]");
        System.out.println("-------------------------------------------------------");
        System.out.println("Orientações de Saúde:");
        for (String dica : recomendacoes) {
            System.out.println(dica);
        }
        System.out.println("\n-------------------------------------------------------");
        System.out.println("Aviso: Esta ferramenta possui caráter educativo/simulativo.");
        System.out.println("Não substitui avaliações médicas e diagnósticos formais.");
        System.out.println("=======================================================\n");
    }
}

public class Main {

    private static int lerInteiroComValidacao(Scanner scanner, String mensagem, int min, int max) {
        int valor = 0;
        boolean valido = false;
        while (!valido) {
            System.out.print(mensagem);
            try {
                valor = scanner.nextInt();
                if (valor >= min && valor <= max) {
                    valido = true;
                } else {
                    System.out.println(" [ERRO] Digite um número entre " + min + " e " + max + ".");
                }
            } catch (InputMismatchException e) {
                System.out.println(" [ERRO] Entrada inválida. Digite apenas números.");
                scanner.next();
            }
        }
        return valor;
    }

    private static double lerDoubleComValidacao(Scanner scanner, String mensagem, double min, double max) {
        double valor = 0;
        boolean valido = false;
        while (!valido) {
            System.out.print(mensagem);
            try {
                String entrada = scanner.next().replace(',', '.');
                valor = Double.parseDouble(entrada);
                if (valor >= min && valor <= max) {
                    valido = true;
                } else {
                    System.out.println(" [ERRO] Digite um número entre " + min + " e " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println(" [ERRO] Entrada inválida (exemplo aceito: 24.5).");
            }
        }
        return valor;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=======================================================");
        System.out.println("     SISTEMA BIOTECNOLÓGICO DE TRIAGEM CARDÍACA");
        System.out.println("=======================================================");

        System.out.print("Digite o nome do paciente: ");
        String nome = scanner.nextLine();

        int idade = lerInteiroComValidacao(scanner, "Digite a idade (20 a 79 anos): ", 20, 79);
        int opcaoGenero = lerInteiroComValidacao(scanner, "Gênero biológico (1 para Masculino, 2 para Feminino): ", 1, 2);
        Genero genero = (opcaoGenero == 1) ? Genero.HOMEM : Genero.MULHER;

        // Escolha do caminho pelo usuário
        System.out.println("\n--- MENU DE AVALIAÇÃO ---");
        System.out.println("1 - Possuo exames recentes (Pressão Arterial e Colesterol)");
        System.out.println("2 - Não sei meus exames (Usar avaliação rápida por IMC e Hábitos)");
        int escolhaCaminho = lerInteiroComValidacao(scanner, "Escolha uma opção (1 ou 2): ", 1, 2);

        // O IMC é solicitado em ambos os caminhos
        double imc = lerDoubleComValidacao(scanner, "Digite o IMC (ex: 23.5 ou 28.0): ", 10.0, 60.0);
        boolean fuma = lerInteiroComValidacao(scanner, "Fuma atualmente? (1 para Sim, 0 para Não): ", 0, 1) == 1;
        boolean exercicios = lerInteiroComValidacao(scanner, "Pratica ao menos 150 min de exercícios por semana? (1 para Sim, 0 para Não): ", 0, 1) == 1;

        Paciente paciente;

        if (escolhaCaminho == 1) {
            // Caminho Completo (Framingham)
            int pas = lerInteiroComValidacao(scanner, "Pressão Arterial Sistólica (ex: 120, 140): ", 80, 240);
            boolean pressaoTratada = lerInteiroComValidacao(scanner, "Usa remédio para pressão? (1 para Sim, 0 para Não): ", 0, 1) == 1;
            int colesterolTotal = lerInteiroComValidacao(scanner, "Colesterol Total (mg/dL): ", 100, 500);
            int hdl = lerInteiroComValidacao(scanner, "HDL Colesterol (mg/dL): ", 10, 120);

            paciente = new Paciente(nome, idade, genero, imc, fuma, exercicios, pas, pressaoTratada, colesterolTotal, hdl);
        } else {
            // Caminho Alternativo (IMC + Hábitos)
            paciente = new Paciente(nome, idade, genero, imc, fuma, exercicios);
        }

        AvaliadorCardio avaliador = new AvaliadorCardio();
        ResultadoAvaliacao resultado = avaliador.avaliar(paciente);
        resultado.exibir();

        scanner.close();
    }
}
