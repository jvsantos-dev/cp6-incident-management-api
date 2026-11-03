package br.com.fiap.incidentmanagement.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ocorrencia")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ocorrencia")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_turma")
    private SchoolClass schoolClass;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_aluno")
    private Student student;

    /** Usuário (professor) que registrou a ocorrência. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario")
    private User user;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime registerDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_categoria")
    private Category category;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_tipo_ocorrencia")
    private IncidentType type;

    @Column(name = "descricao", nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status = IncidentStatus.WAITING;

    @Column(name = "data_atualizacao")
    private LocalDateTime updateDate;

    @Column(nullable = false)
    private boolean deleted = false;

    protected Incident() {
    }

    public Incident(SchoolClass schoolClass, Student student, User user, LocalDateTime registerDate,
                    Category category, IncidentType type, String description) {
        this.schoolClass = schoolClass;
        this.student = student;
        this.user = user;
        this.registerDate = registerDate;
        this.category = category;
        this.type = type;
        this.description = description;
        this.updateDate = registerDate;
    }

    public Long getId() { return id; }
    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public User getUser() { return user; }
    public LocalDateTime getRegisterDate() { return registerDate; }
    public void setRegisterDate(LocalDateTime registerDate) { this.registerDate = registerDate; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public IncidentType getType() { return type; }
    public void setType(IncidentType type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
    public LocalDateTime getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
