package br.com.fiap.incidentmanagement.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "turma")
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turma")
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "turno", nullable = false, length = 10)
    private Shift shift;

    @Column(name = "ano", nullable = false)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "semestre", nullable = false, length = 10)
    private Semester semester;

    @Column(nullable = false)
    private boolean canceled = false;

    /** Relacionamento N:M entre ALUNO e TURMA. */
    @ManyToMany
    @JoinTable(name = "aluno_turma",
            joinColumns = @JoinColumn(name = "id_turma"),
            inverseJoinColumns = @JoinColumn(name = "id_aluno"))
    private Set<Student> students = new HashSet<>();

    protected SchoolClass() {
    }

    public SchoolClass(String name, Shift shift, Integer year, Semester semester) {
        this.name = name;
        this.shift = shift;
        this.year = year;
        this.semester = semester;
    }

    public boolean hasStudent(Long studentId) {
        return students.stream().anyMatch(s -> s.getId().equals(studentId));
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Shift getShift() { return shift; }
    public void setShift(Shift shift) { this.shift = shift; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Semester getSemester() { return semester; }
    public void setSemester(Semester semester) { this.semester = semester; }
    public boolean isCanceled() { return canceled; }
    public void setCanceled(boolean canceled) { this.canceled = canceled; }
    public Set<Student> getStudents() { return students; }
}
