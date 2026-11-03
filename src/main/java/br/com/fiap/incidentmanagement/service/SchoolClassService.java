package br.com.fiap.incidentmanagement.service;

import br.com.fiap.incidentmanagement.dto.SchoolClassDtos.*;
import br.com.fiap.incidentmanagement.dto.StudentDtos.StudentResponse;
import br.com.fiap.incidentmanagement.exception.BusinessException;
import br.com.fiap.incidentmanagement.exception.NotFoundException;
import br.com.fiap.incidentmanagement.model.SchoolClass;
import br.com.fiap.incidentmanagement.model.Student;
import br.com.fiap.incidentmanagement.repository.SchoolClassRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final StudentService studentService;

    public SchoolClassService(SchoolClassRepository schoolClassRepository, StudentService studentService) {
        this.schoolClassRepository = schoolClassRepository;
        this.studentService = studentService;
    }

    @Transactional
    public SchoolClassResponse create(SchoolClassRequest request) {
        SchoolClass schoolClass = new SchoolClass(request.name(), request.shift(), request.year(), request.semester());
        return SchoolClassResponse.from(schoolClassRepository.save(schoolClass));
    }

    @Transactional(readOnly = true)
    public Page<SchoolClassResponse> list(boolean canceled, Pageable pageable) {
        return schoolClassRepository.findAllByCanceled(canceled, pageable).map(SchoolClassResponse::from);
    }

    @Transactional(readOnly = true)
    public SchoolClassResponse findById(Long id) {
        return SchoolClassResponse.from(get(id));
    }

    @Transactional
    public SchoolClassResponse update(SchoolClassUpdateRequest request) {
        SchoolClass schoolClass = get(request.id());
        if (request.name() != null && !request.name().isBlank()) {
            schoolClass.setName(request.name());
        }
        if (request.shift() != null) {
            schoolClass.setShift(request.shift());
        }
        if (request.year() != null) {
            schoolClass.setYear(request.year());
        }
        if (request.semester() != null) {
            schoolClass.setSemester(request.semester());
        }
        return SchoolClassResponse.from(schoolClass);
    }

    /** Exclusão lógica: a turma é marcada como cancelada. */
    @Transactional
    public void cancel(Long id) {
        get(id).setCanceled(true);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> listStudents(Long id) {
        return get(id).getStudents().stream()
                .filter(Student::isActive)
                .sorted(Comparator.comparing(Student::getName))
                .map(StudentResponse::from)
                .toList();
    }

    /** RF04 - Vincular alunos às turmas. */
    @Transactional
    public SchoolClassResponse addStudent(Long id, Long studentId) {
        SchoolClass schoolClass = get(id);
        if (schoolClass.isCanceled()) {
            throw new BusinessException("Não é possível vincular alunos a uma turma cancelada.");
        }
        Student student = studentService.get(studentId);
        if (!student.isActive()) {
            throw new BusinessException("Não é possível vincular um aluno inativo.");
        }
        if (schoolClass.hasStudent(studentId)) {
            throw new BusinessException("O aluno já está vinculado a esta turma.");
        }
        schoolClass.getStudents().add(student);
        return SchoolClassResponse.from(schoolClass);
    }

    @Transactional
    public SchoolClassResponse removeStudent(Long id, Long studentId) {
        SchoolClass schoolClass = get(id);
        if (!schoolClass.getStudents().removeIf(s -> s.getId().equals(studentId))) {
            throw new NotFoundException("O aluno " + studentId + " não está vinculado à turma " + id);
        }
        return SchoolClassResponse.from(schoolClass);
    }

    SchoolClass get(Long id) {
        return schoolClassRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Turma não encontrada: " + id));
    }
}
