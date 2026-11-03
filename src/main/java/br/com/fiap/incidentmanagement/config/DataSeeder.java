package br.com.fiap.incidentmanagement.config;

import br.com.fiap.incidentmanagement.model.*;
import br.com.fiap.incidentmanagement.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Popula o banco com dados de exemplo na primeira execução (banco vazio).
 * Todos os usuários iniciais usam a senha 123456.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final CategoryRepository categoryRepository;
    private final IncidentTypeRepository typeRepository;
    private final IncidentRepository incidentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, StudentRepository studentRepository,
                      SchoolClassRepository schoolClassRepository, CategoryRepository categoryRepository,
                      IncidentTypeRepository typeRepository, IncidentRepository incidentRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.categoryRepository = categoryRepository;
        this.typeRepository = typeRepository;
        this.incidentRepository = incidentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        String password = passwordEncoder.encode("123456");
        User professor = userRepository.save(new User("Ana Professora", "professor", password, Role.PROFESSOR));
        userRepository.save(new User("Bruno Professor", "professor2", password, Role.PROFESSOR));
        userRepository.save(new User("Carla Analista", "analista", password, Role.ANALISTA));
        userRepository.save(new User("Diego Coordenador", "coordenador", password, Role.COORDENADOR));
        userRepository.save(new User("Elisa Administrativa", "admin", password, Role.ADMINISTRATIVO));

        Map<String, List<String>> catalog = Map.of(
                "Disciplinar", List.of("Indisciplina", "Agressão", "Uso de celular"),
                "Pedagógica", List.of("Rendimento", "Falta de tarefa"),
                "Administrativa", List.of("Atraso", "Falta", "Uniforme"));
        catalog.forEach((categoryName, types) -> {
            Category category = categoryRepository.save(new Category(categoryName));
            types.forEach(type -> typeRepository.save(new IncidentType(type, category)));
        });

        SchoolClass classA = schoolClassRepository.save(new SchoolClass("1A", Shift.MANHA, 2026, Semester.SEGUNDO));
        SchoolClass classB = schoolClassRepository.save(new SchoolClass("2B", Shift.TARDE, 2026, Semester.SEGUNDO));
        Student joao = studentRepository.save(new Student("João Silva", LocalDate.of(2012, 3, 10)));
        Student maria = studentRepository.save(new Student("Maria Souza", LocalDate.of(2011, 7, 22)));
        Student pedro = studentRepository.save(new Student("Pedro Lima", LocalDate.of(2012, 1, 5)));
        classA.getStudents().addAll(List.of(joao, maria));
        classB.getStudents().addAll(List.of(maria, pedro));

        Category disciplinar = categoryRepository.findByNameIgnoreCase("Disciplinar").orElseThrow();
        Category administrativa = categoryRepository.findByNameIgnoreCase("Administrativa").orElseThrow();
        IncidentType indisciplina = typeRepository
                .findByNameIgnoreCaseAndCategoryId("Indisciplina", disciplinar.getId()).orElseThrow();
        IncidentType atraso = typeRepository
                .findByNameIgnoreCaseAndCategoryId("Atraso", administrativa.getId()).orElseThrow();

        incidentRepository.save(new Incident(classA, joao, professor, LocalDateTime.now().minusDays(3),
                disciplinar, indisciplina, "Conversou durante a avaliação mesmo após advertência."));
        incidentRepository.save(new Incident(classB, maria, professor, LocalDateTime.now().minusDays(1),
                administrativa, atraso, "Chegou 30 minutos após o início da aula."));

        log.info("Dados iniciais criados. Logins: professor, professor2, analista, coordenador, admin (senha 123456)");
    }
}
