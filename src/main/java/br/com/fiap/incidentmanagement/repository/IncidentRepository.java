package br.com.fiap.incidentmanagement.repository;

import br.com.fiap.incidentmanagement.dto.KeyValue;
import br.com.fiap.incidentmanagement.model.Incident;
import br.com.fiap.incidentmanagement.model.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    Page<Incident> findAllByDeletedFalseAndStatusIn(Collection<IncidentStatus> status, Pageable pageable);

    List<Incident> findAllByStudentIdAndDeletedFalseOrderByRegisterDateDesc(Long studentId);

    long countByDeletedFalseAndStatus(IncidentStatus status);

    long countByDeletedFalseAndStatusIn(Collection<IncidentStatus> status);

    boolean existsBySchoolClassId(Long schoolClassId);

    boolean existsByTypeId(Long typeId);

    @Query("""
            select new br.com.fiap.incidentmanagement.dto.KeyValue(i.category.name, count(i))
            from Incident i where i.deleted = false
            group by i.category.name order by count(i) desc""")
    List<KeyValue> countByCategory();

    @Query("""
            select new br.com.fiap.incidentmanagement.dto.KeyValue(i.type.name, count(i))
            from Incident i where i.deleted = false
            group by i.type.name order by count(i) desc""")
    List<KeyValue> countByType();

    @Query("""
            select new br.com.fiap.incidentmanagement.dto.KeyValue(i.schoolClass.name, count(i))
            from Incident i where i.deleted = false
            group by i.schoolClass.name order by count(i) desc""")
    List<KeyValue> countBySchoolClass();

    /** Ranking de alunos reincidentes. */
    @Query("""
            select new br.com.fiap.incidentmanagement.dto.KeyValue(i.student.name, count(i))
            from Incident i where i.deleted = false
            group by i.student.id, i.student.name order by count(i) desc""")
    List<KeyValue> rankingByStudent(Pageable pageable);

    @Query("select i.registerDate from Incident i where i.deleted = false")
    List<LocalDateTime> findAllRegisterDates();
}
