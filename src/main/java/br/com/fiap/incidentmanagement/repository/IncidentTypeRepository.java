package br.com.fiap.incidentmanagement.repository;

import br.com.fiap.incidentmanagement.model.IncidentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidentTypeRepository extends JpaRepository<IncidentType, Long> {

    List<IncidentType> findAllByCategoryIdOrderByName(Long categoryId);

    List<IncidentType> findAllByCategoryNameIgnoreCaseOrderByName(String categoryName);

    Optional<IncidentType> findByNameIgnoreCaseAndCategoryId(String name, Long categoryId);

    boolean existsByCategoryId(Long categoryId);
}
