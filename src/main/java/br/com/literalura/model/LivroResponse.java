package br.com.literalura.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LivroResponse(
        @JsonAlias("title") String titulo,
        @JsonAlias("authors") List<AutorResponse> autores,
        @JsonAlias("languages") List<String> idioma,
        @JsonAlias("download_count") Integer numeroDownloads){
}
