package br.com.fiap.incidentmanagement.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "aluno")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aluno")
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String name;

    @Column(name = "data_nascimento")
    private LocalDate birthDate;

    @Lob
    @Column(name = "foto")
    private byte[] image;

    @Column(name = "foto_content_type", length = 50)
    private String imageContentType;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany(mappedBy = "students")
    private Set<SchoolClass> schoolClasses = new HashSet<>();

    protected Student() {
    }

    public Student(String name, LocalDate birthDate) {
        this.name = name;
        this.birthDate = birthDate;
    }

    public boolean hasImage() {
        return image != null && image.length > 0;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
    public String getImageContentType() { return imageContentType; }
    public void setImageContentType(String imageContentType) { this.imageContentType = imageContentType; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Set<SchoolClass> getSchoolClasses() { return schoolClasses; }
}
