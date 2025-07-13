package br.com.literalura;

import br.com.literalura.model.*;
import br.com.literalura.repository.AutorRepository;
import br.com.literalura.repository.LivroRepository;
import br.com.literalura.service.ConsumoAPI;
import br.com.literalura.service.ConverteDados;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Principal {

    private final Scanner leitura = new Scanner(System.in);
    private final String ENDERECO = "http://gutendex.com/books/?search=";
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;
    private ConverteDados converteDados = new ConverteDados();
    private List<LivroResponse> dadosLivros = new ArrayList<>();

    private List<Livro> livros = new ArrayList<>();

    public Principal(LivroRepository livroRepository, AutorRepository autorRepository) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
    }

    public void exibirMenu() {
        int opcao = -1;
        while (opcao != 0) {
            var menu = """
                    
                    |-----------------------------------|
                    |        **** LITERALURA ****       |
                    |-----------------------------------|
                    | (1) Buscar livro pelo título      |
                    | (2) Listar livros registrados     |
                    | (3) Listar autores registrados    |
                    | (4) Listar autores vivos por ano  |
                    | (5) Listar livros por idioma      |
                    | (0) Sair                          |
                    |-----------------------------------|
                    """;

            System.out.println(menu);
            System.out.print("Digite sua escolha: ");
            opcao = leitura.nextInt();
            leitura.nextLine();


            switch (opcao) {
                case 1 -> buscarLivro();
                case 2 -> listarLivros();
                case 3 -> listarAutores();
                case 4 -> listarAutoresVivos();
                case 5 -> listarLivrosPorIdioma();
                case 0 -> System.out.println("\nEncerrando a aplicação...\n");
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    private void buscarLivro() {
        System.out.print("Qual livro deseja buscar: ");
        var nomeLivro = leitura.nextLine();

        String json = consumoAPI.obterDados(ENDERECO + nomeLivro.replace(" ", "%20"));
        DadosResponse resposta = converteDados.obterDados(json, DadosResponse.class);
        List<LivroResponse> livrosEncontrados = resposta.results();

        if (livrosEncontrados.isEmpty()) {
            System.out.println("Livro não localizado.");
            return;
        }

        for (LivroResponse livroResponse : livrosEncontrados) {
            if (livroResponse.autores().isEmpty()) {
                System.out.println("Livro não registrado devido a falta de um autor.");
                continue;
            }

            AutorResponse autorResponse = livroResponse.autores().get(0);
            var nomeAutor = autorResponse.nome();

            Autor autor = autorRepository.findByNomeIgnoreCase(nomeAutor)
                    .orElse(null);

            if (autor == null) {
                Autor novoAutor = new Autor();
                novoAutor.setNome(nomeAutor);
                novoAutor.setAnoDeNascimento(autorResponse.anoDeNascimento());
                novoAutor.setAnoDeFalescimento(autorResponse.anoDeFalescimento());
                autor = autorRepository.save(novoAutor);
            }

            Livro livro = new Livro();
            livro.setTitulo(livroResponse.titulo());
            livro.setAutor(autor);
            livro.setIdioma(livroResponse.idioma().isEmpty() ? "desconhecido" : livroResponse.idioma().get(0));
            livro.setNumeroDownloads(livroResponse.numeroDownloads());

            livroRepository.save(livro);
            System.out.println("Livro salvo com suceeso.");
            System.out.println(livro);
        }
    }

    private void listarLivros() {
        System.out.println("\n** LIVROS BUSCADOS **");
        System.out.println("---------------------");

        livros = livroRepository.findAll();
        livros.stream()
                .sorted(Comparator.comparing(Livro::getTitulo))
                .forEach(System.out::println);
    }

    private void listarAutores() {
        List<Autor> autores = autorRepository.findAll();

        if (autores.isEmpty()) {
            System.out.println("Nenhum autor encontrado.");
            return;
        }

        System.out.println("\n** AUTORES REGISTRADOS **");
        System.out.println("-------------------------------");

       autores.stream()
               .forEach(autor -> {
                   String nascimento = (autor.getAnoDeNascimento() != null) ? autor.getAnoDeNascimento().toString() : "Desconhecido";
                   String falescimento = (autor.getAnoDeFalescimento() != null) ? autor.getAnoDeFalescimento().toString() : "Ainda vivo ou desconhecido";
                   System.out.println("Autor(a): " + autor.getNome() + " | Nascimento: " + nascimento + " | Falescimento: " + falescimento);
                   System.out.println("-------------------------------");
               });
    }

    private void listarAutoresVivos() {
    }

    private void listarLivrosPorIdioma() {
    }
}
