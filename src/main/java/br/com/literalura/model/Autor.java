package br.com.literalura.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "autores")
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private Integer anoDeNascimento;
    private Integer anoDeFalescimento;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Livro> livros = new ArrayList<>();


    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getAnoDeNascimento() {
        return anoDeNascimento;
    }

    public void setAnoDeNascimento(Integer anoDeNascimento) {
        this.anoDeNascimento = anoDeNascimento;
    }

    public Integer getAnoDeFalescimento() {
        return anoDeFalescimento;
    }

    public void setAnoDeFalescimento(Integer anoDeFalescimento) {
        this.anoDeFalescimento = anoDeFalescimento;
    }

    @Override
    public String toString() {
        return nome + " (" + anoDeNascimento + " - " + anoDeFalescimento + ")";
    }

}
