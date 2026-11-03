package br.com.fiap.incidentmanagement.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria_ocorrencia")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long id;

    @Column(name = "nome", nullable = false, unique = true, length = 60)
    private String name;

    protected Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
