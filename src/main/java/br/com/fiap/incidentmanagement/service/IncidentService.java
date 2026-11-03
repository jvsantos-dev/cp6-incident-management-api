package br.com.fiap.incidentmanagement.service;

import br.com.fiap.incidentmanagement.dto.IncidentDtos.*;
import br.com.fiap.incidentmanagement.dto.KeyValue;
import br.com.fiap.incidentmanagement.exception.BusinessException;
import br.com.fiap.incidentmanagement.exception.NotFoundException;
import br.com.fiap.incidentmanagement.model.*;
import br.com.fiap.incidentmanagement.repository.IncidentRepository;
import br.com.fiap.incidentmanagement.repository.IncidentSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class IncidentService {

    /** Campos de ordenação aceitos vindos do frontend, traduzidos para o caminho na entidade. */
    private static final Map<String, String> SORT_FIELDS = Map.of(
            "category", "category.name",
            "type", "type.name",
            "schoolClassName", "schoolClass.name",
            "studentName", "student.name");

    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("MM/yyyy");

    private final IncidentRepository incidentRepository;
    private final SchoolClassService schoolClassService;
    private final StudentService studentService;
    private final CategoryService categoryService;

    public IncidentService(IncidentRepository incidentRepository, SchoolClassService schoolClassService,
                           StudentService studentService, CategoryService categoryService) {
        this.incidentRepository = incidentRepository;
        this.schoolClassService = schoolClassService;
        this.studentService = studentService;
        this.categoryService = categoryService;
    }

    /** RF07 - Registrar ocorrência. */
    @Transactional
    public IncidentResponse create(IncidentRequest request, User author) {
        SchoolClass schoolClass = schoolClassService.get(request.idSchoolClass());
        Student student = studentService.get(request.idStudent());
        validateStudentInClass(schoolClass, student);
        Category category = categoryService.getCategoryByName(request.category());
        IncidentType type = categoryService.getTypeByName(request.type(), category);
        LocalDateTime registerDate = request.registerDate() != null ? request.registerDate() : LocalDateTime.now();
        if (registerDate.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new BusinessException("A data da ocorrência não pode estar no futuro.");
        }

        Incident incident = new Incident(schoolClass, student, author, registerDate, category, type,
                request.description());
        return IncidentResponse.from(incidentRepository.save(incident));
    }

    /** RF08 - Consultar ocorrências com filtros por aluno, turma, categoria, tipo e período. */
    @Transactional(readOnly = true)
    public Page<IncidentResponse> search(Long studentId, Long schoolClassId, String category, String type,
                                         LocalDate startDate, LocalDate endDate, boolean includeDeleted,
                                         Pageable pageable) {
        var spec = IncidentSpecifications.filter(studentId, schoolClassId, category, type, startDate, endDate,
                includeDeleted);
        return incidentRepository.findAll(spec, normalize(pageable)).map(IncidentResponse::from);
    }

    /** Lista por situação: active (em aberto), waiting, progressing, solved, unsolved ou closed. */
    @Transactional(readOnly = true)
    public Page<IncidentResponse> listByStatus(String status, Pageable pageable) {
        List<IncidentStatus> statuses = "active".equalsIgnoreCase(status)
                ? IncidentStatus.OPEN
                : List.of(IncidentStatus.fromValue(status));
        return incidentRepository.findAllByDeletedFalseAndStatusIn(statuses, normalize(pageable))
                .map(IncidentResponse::from);
    }

    /** RF09 - Detalhar ocorrência. */
    @Transactional(readOnly = true)
    public IncidentResponse findById(Long id) {
        return IncidentResponse.from(get(id));
    }

    /** Histórico completo do aluno, contemplando todas as turmas em que esteve matriculado. */
    @Transactional(readOnly = true)
    public List<IncidentResponse> history(Long studentId) {
        studentService.get(studentId);
        return incidentRepository.findAllByStudentIdAndDeletedFalseOrderByRegisterDateDesc(studentId).stream()
                .map(IncidentResponse::from)
                .toList();
    }

    @Transactional
    public IncidentResponse update(IncidentUpdateRequest request, User authenticated) {
        Incident incident = getOwned(request.id(), authenticated);

        SchoolClass schoolClass = request.idSchoolClass() != null
                ? schoolClassService.get(request.idSchoolClass()) : incident.getSchoolClass();
        Student student = request.idStudent() != null
                ? studentService.get(request.idStudent()) : incident.getStudent();
        if (!schoolClass.getId().equals(incident.getSchoolClass().getId())
                || !student.getId().equals(incident.getStudent().getId())) {
            validateStudentInClass(schoolClass, student);
        }

        Category category = hasText(request.category())
                ? categoryService.getCategoryByName(request.category()) : incident.getCategory();
        String typeName = hasText(request.type()) ? request.type() : incident.getType().getName();
        IncidentType type = categoryService.getTypeByName(typeName, category);

        incident.setSchoolClass(schoolClass);
        incident.setStudent(student);
        incident.setCategory(category);
        incident.setType(type);
        if (request.registerDate() != null) {
            incident.setRegisterDate(request.registerDate());
        }
        if (hasText(request.description())) {
            incident.setDescription(request.description());
        }
        incident.setUpdateDate(LocalDateTime.now());
        return IncidentResponse.from(incident);
    }

    @Transactional
    public IncidentResponse updateStatus(StatusUpdateRequest request) {
        Incident incident = get(request.id());
        if (incident.isDeleted()) {
            throw new BusinessException("Não é possível alterar o status de uma ocorrência excluída.");
        }
        incident.setStatus(request.status());
        incident.setUpdateDate(request.updateDate() != null ? request.updateDate() : LocalDateTime.now());
        return IncidentResponse.from(incident);
    }

    /** Exclusão lógica, permitida apenas ao professor autor da ocorrência. */
    @Transactional
    public void delete(Long id, User authenticated) {
        Incident incident = getOwned(id, authenticated);
        incident.setDeleted(true);
        incident.setUpdateDate(LocalDateTime.now());
    }

    /** Dados do dashboard do coordenador e relatórios (RF13). */
    @Transactional(readOnly = true)
    public IncidentSummary summary() {
        Map<YearMonth, Long> byMonth = incidentRepository.findAllRegisterDates().stream()
                .collect(Collectors.groupingBy(YearMonth::from, TreeMap::new, Collectors.counting()));
        List<KeyValue> byPeriod = byMonth.entrySet().stream()
                .map(e -> new KeyValue(e.getKey().format(PERIOD_FORMAT), e.getValue()))
                .toList();

        return new IncidentSummary(
                incidentRepository.countByDeletedFalseAndStatusIn(IncidentStatus.OPEN),
                incidentRepository.countByDeletedFalseAndStatus(IncidentStatus.WAITING),
                incidentRepository.countByDeletedFalseAndStatus(IncidentStatus.PROGRESSING),
                incidentRepository.countByDeletedFalseAndStatus(IncidentStatus.SOLVED),
                incidentRepository.countByDeletedFalseAndStatus(IncidentStatus.UNSOLVED),
                incidentRepository.countByDeletedFalseAndStatus(IncidentStatus.CLOSED),
                incidentRepository.countByCategory(),
                incidentRepository.countByType(),
                incidentRepository.countBySchoolClass(),
                incidentRepository.rankingByStudent(PageRequest.of(0, 10)),
                byPeriod);
    }

    private void validateStudentInClass(SchoolClass schoolClass, Student student) {
        if (schoolClass.isCanceled()) {
            throw new BusinessException("A turma " + schoolClass.getName() + " está cancelada.");
        }
        if (!student.isActive()) {
            throw new BusinessException("O aluno " + student.getName() + " está inativo.");
        }
        if (!schoolClass.hasStudent(student.getId())) {
            throw new BusinessException("O aluno " + student.getName() + " não pertence à turma "
                    + schoolClass.getName() + ".");
        }
    }

    private Incident getOwned(Long id, User authenticated) {
        Incident incident = get(id);
        if (!incident.getUser().getId().equals(authenticated.getId())) {
            throw new AccessDeniedException("O professor só pode alterar ou excluir as próprias ocorrências.");
        }
        if (incident.isDeleted()) {
            throw new BusinessException("A ocorrência já foi excluída.");
        }
        return incident;
    }

    private Incident get(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ocorrência não encontrada: " + id));
    }

    private static Pageable normalize(Pageable pageable) {
        Sort sort = Sort.by(pageable.getSort().stream()
                .map(order -> order.withProperty(SORT_FIELDS.getOrDefault(order.getProperty(), order.getProperty())))
                .toList());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
