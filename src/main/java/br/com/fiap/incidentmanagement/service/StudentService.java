package br.com.fiap.incidentmanagement.service;

import br.com.fiap.incidentmanagement.dto.SchoolClassDtos.SchoolClassResponse;
import br.com.fiap.incidentmanagement.dto.StudentDtos.*;
import br.com.fiap.incidentmanagement.exception.BusinessException;
import br.com.fiap.incidentmanagement.exception.NotFoundException;
import br.com.fiap.incidentmanagement.model.Student;
import br.com.fiap.incidentmanagement.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        return StudentResponse.from(studentRepository.save(new Student(request.name(), request.birthDate())));
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> list(String name, Pageable pageable) {
        Page<Student> page = (name == null || name.isBlank())
                ? studentRepository.findAllByActiveTrue(pageable)
                : studentRepository.findAllByActiveTrueAndNameContainingIgnoreCase(name, pageable);
        return page.map(StudentResponse::from);
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        return StudentResponse.from(get(id));
    }

    @Transactional
    public StudentResponse update(StudentUpdateRequest request) {
        Student student = get(request.id());
        if (request.name() != null && !request.name().isBlank()) {
            student.setName(request.name());
        }
        if (request.birthDate() != null) {
            student.setBirthDate(request.birthDate());
        }
        return StudentResponse.from(student);
    }

    /** Exclusão lógica: o aluno é inativado e o histórico de ocorrências é preservado. */
    @Transactional
    public void inactivate(Long id) {
        get(id).setActive(false);
    }

    @Transactional
    public StudentResponse uploadImage(Long id, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException("Nenhuma imagem enviada.");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("O arquivo enviado precisa ser uma imagem.");
        }
        Student student = get(id);
        try {
            student.setImage(image.getBytes());
        } catch (IOException e) {
            throw new BusinessException("Não foi possível ler a imagem enviada.");
        }
        student.setImageContentType(contentType);
        return StudentResponse.from(student);
    }

    @Transactional(readOnly = true)
    public Student getWithImage(Long id) {
        Student student = get(id);
        if (!student.hasImage()) {
            throw new NotFoundException("Aluno sem foto cadastrada: " + id);
        }
        return student;
    }

    @Transactional(readOnly = true)
    public List<SchoolClassResponse> listSchoolClasses(Long id) {
        return get(id).getSchoolClasses().stream()
                .sorted(Comparator.comparing(c -> c.getId()))
                .map(SchoolClassResponse::from)
                .toList();
    }

    Student get(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Aluno não encontrado: " + id));
    }
}
