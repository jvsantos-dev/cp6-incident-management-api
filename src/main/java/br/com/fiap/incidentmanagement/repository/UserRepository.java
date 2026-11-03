package br.com.fiap.incidentmanagement.repository;

import br.com.fiap.incidentmanagement.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String login);

    boolean existsByLogin(String login);

    Page<User> findAllByActiveTrue(Pageable pageable);
}
