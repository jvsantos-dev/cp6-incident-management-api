package br.com.fiap.incidentmanagement.repository;

import br.com.fiap.incidentmanagement.model.SchoolClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    Page<SchoolClass> findAllByCanceled(boolean canceled, Pageable pageable);
}
