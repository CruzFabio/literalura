package br.com.literalura;

import br.com.literalura.model.*;
import br.com.literalura.repository.AutorRepository;
import br.com.literalura.repository.LivroRepository;
import br.com.literalura.service.ConsumoAPI;
import br.com.literalura.service.ConverteDados;

import java.util.*;

public class Principal {

    private final Scanner leitura = new Scanner(System.in);
    private final String ENDERECO = "http://gutendex.com/books/?search=";
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;
    private ConverteDados converteDados = new ConverteDados();

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
            opcao = lerNumeroInteiro(leitura, "Digite sua escolha: ");

            switch (opcao) {
                case 1 -> buscarLivro();
                case 2 -> listarLivros();
                case 3 -> listarAutores();
                case 4 -> listarAutoresVivosPorAno();
                case 5 -> listarLivrosPorIdioma();
                case 0 -> System.out.println("\nEncerrando a aplicação...\n");
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    private void buscarLivro() {

        var nomeLivro = lerTexto(leitura, "Qual livro deseja buscar: ");

        String json = consumoAPI.obterDados(ENDERECO + nomeLivro.replace(" ", "%20"));
        DadosResponse resposta = converteDados.obterDados(json, DadosResponse.class);
        List<LivroResponse> livrosEncontrados = resposta.results();

        if (livrosEncontrados.isEmpty()) {
            System.out.println("Livro não localizado.");
            return;
        }

        System.out.println("\nLivros encontrados:");
        for (int i = 0; i < livrosEncontrados.size(); i++) {
            var livro = livrosEncontrados.get(i);
            var idioma = livro.idioma().isEmpty() ? "desconhecido" : livro.idioma().get(0);
            System.out.printf("%d - %s (%s) - Downloads: %d%n", i + 1, livro.titulo(), idioma, livro.numeroDownloads());
        }

        int escolha = lerNumeroInteiro(leitura, "Escolha o número do livro que deseja salvar: ") - 1;

        if (escolha < 0 || escolha >= livrosEncontrados.size()) {
            System.out.println("Opção inválida.");
            return;
        }

        LivroResponse livroResponse = livrosEncontrados.get(escolha);

        if (livroResponse.autores().isEmpty()) {
            System.out.println("Livro não registrado devido à falta de um autor.");
            return;
        }

        AutorResponse autorResponse = livroResponse.autores().get(0);
        var nomeAutor = autorResponse.nome();

        Autor autor = autorRepository.findByNomeIgnoreCase(nomeAutor)
                .orElseGet(() -> {
                    Autor novoAutor = new Autor();
                    novoAutor.setNome(nomeAutor);
                    novoAutor.setAnoDeNascimento(autorResponse.anoDeNascimento());
                    novoAutor.setAnoDeFalecimento(autorResponse.anoDeFalecimento());
                    return autorRepository.save(novoAutor);
                });

        Livro livro = new Livro();
        livro.setTitulo(livroResponse.titulo());
        livro.setAutor(autor);
        livro.setIdioma(livroResponse.idioma().isEmpty() ? "desconhecido" : livroResponse.idioma().get(0));
        livro.setNumeroDownloads(livroResponse.numeroDownloads());

        livroRepository.save(livro);
        System.out.println("Livro salvo com suceeso.");
        System.out.println(livro);
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

        autores.forEach(autor -> {
            System.out.println("Nome: " + autor.getNome());

            String nascimento = (autor.getAnoDeNascimento() != null) ? autor.getAnoDeNascimento().toString() : "Desconhecido";
            String falecimento = (autor.getAnoDeFalecimento() != null) ? autor.getAnoDeFalecimento().toString() : "Ainda vivo ou desconhecido";

            System.out.println("Ano de nascimento: " + nascimento);
            System.out.println("Ano de falecimento: " + falecimento);
            System.out.println("Livros publicados:");

            List<Livro> livros = autor.getLivros();
            if (livros == null || livros.isEmpty()) {
                System.out.println("- Nenhum livro cadastrado");
            } else {
                livros.forEach(livro -> System.out.println("- " + livro.getTitulo()));
            }
            System.out.println("-------------------------------");
        });
    }

    private void listarAutoresVivosPorAno() {
        var anoPesquisado = lerNumeroInteiro(leitura, "Insira o ano que deseja pesquisar: ");

        List<Autor> autores = autorRepository.findAll();

        System.out.println("\nAutores vivos no ano de " + anoPesquisado + ":");

        List<Autor> autoresVivosNoAno = autores.stream()
                .filter(autor -> autor.getAnoDeNascimento() != null && autor.getAnoDeNascimento() <= anoPesquisado)
                .filter(autor -> autor.getAnoDeFalecimento() == null || autor.getAnoDeFalecimento() > anoPesquisado)
                .toList();

        if (autoresVivosNoAno.isEmpty()) {
            System.out.println("\nNenhum autor encontrado vivo no ano de " + anoPesquisado);
            return;
        } else {
            autoresVivosNoAno.forEach(autor -> {
                System.out.println(autor.getNome() + " nasceu: " + autor.getAnoDeNascimento()
                        + ", morreu: " + (autor.getAnoDeFalecimento() == null ?
                        "Ainda vivo ou desconhecido" : autor.getAnoDeFalecimento()));
            });
        }

    }

    private void listarLivrosPorIdioma() {
        System.out.println("\n** BUSCAR LIVROS POR IDIOMA **");
        System.out.println("-----------------------------");

        Map<String, String> idiomasComNomes = Map.of(
                "de", "Deutsch",
                "en", "English",
                "es", "Espanhol",
                "fr", "Francês",
                "pt", "Português"
        );

        List<Livro> livros = livroRepository.findAll();

        List<String> idiomasDisponiveis = livros.stream()
                .map(Livro::getIdioma)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .distinct()
                .sorted()
                .toList();

        if (idiomasDisponiveis.isEmpty()) {
            System.out.println("Nenhum idioma encontrado no sistema.");
            return;
        }

        System.out.println("\nIdiomas disponíveis:");
        idiomasDisponiveis.forEach(idioma -> {
            var nomeCompleto = idiomasComNomes.getOrDefault(idioma, "Idioma desconhecido");
            System.out.println("- " + idioma + " (" + nomeCompleto + ")");
        });

        String idiomaEscolhido;
        do {
            idiomaEscolhido = lerTexto(leitura, "\nEscolha o código do idioma listado acima (ex: en, pt): ")
                    .toLowerCase();

            if (!idiomasDisponiveis.contains(idiomaEscolhido)) {
                System.out.println("\n⚠ Idioma inválido. Tente novamente.");
            }
        } while (!idiomasDisponiveis.contains(idiomaEscolhido));

        final String idiomaSelecionado = idiomaEscolhido;

        List<Livro> livrosPorIdioma = livros.stream()
                .filter(livro -> livro.getIdioma() != null &&
                        livro.getIdioma().equalsIgnoreCase(idiomaSelecionado))
                .toList();

        var nomeCompletoEscolhido = idiomasComNomes.getOrDefault(idiomaEscolhido, "Idioma desconhecido.");

        System.out.println("\nLivros no idioma '" + idiomaEscolhido + "' (" + nomeCompletoEscolhido + ")");
        System.out.println("---------------------------------");
        livrosPorIdioma.forEach(System.out::println);
    }

    // Método utilitário para validar a entrada de números inteiros.
    public int lerNumeroInteiro(Scanner leitura, String mensagem) {
        int numero = 0;
        boolean valido = false;

        while (!valido) {
            try {
                System.out.print(mensagem);
                numero = leitura.nextInt();
                leitura.nextLine();
                valido = true;
            } catch (InputMismatchException e) {
                System.out.println("\n⚠ Atenção: digite um número válido!\n");
                leitura.nextLine();
            }
        }
        return numero;
    }

    // Método utilitário para validar a entrada de texto
    public String lerTexto(Scanner leitura, String mensagem) {
        String texto;

        do {
            System.out.print(mensagem);
            texto = leitura.nextLine().trim();
            if (texto.isEmpty()) {
                System.out.println("⚠ Atenção: o texto não pode ser vazio!");
            }
        } while (texto.isEmpty());
        return texto;
    }
}