package br.com.fiap.incidentmanagement.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_ocorrencia")
public class IncidentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_ocorrencia")
    private Long id;

    @Column(name = "nome", nullable = false, length = 60)
    private String name;

    /** Uma categoria possui muitos tipos de ocorrência (1:N). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_categoria")
    private Category category;

    protected IncidentType() {
    }

    public IncidentType(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}
