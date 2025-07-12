package br.com.literalura.repository;

import br.com.literalura.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LivroRepository extends JpaRepository<Livro, Long> {
    List<Livro> findByTituloContainingIgnoreCase(String nomeLivro);
    List<Livro> findByIdioma(String idioma);

}
