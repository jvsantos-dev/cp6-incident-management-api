package br.com.fiap.incidentmanagement.repository;

import br.com.fiap.incidentmanagement.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Page<Student> findAllByActiveTrue(Pageable pageable);

    Page<Student> findAllByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}
