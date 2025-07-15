package br.com.literalura.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AutorResponse (
        @JsonAlias("name") String nome,
        @JsonAlias("birth_year") Integer anoDeNascimento,
        @JsonAlias("death_year") Integer anoDeFalecimento) {
}
