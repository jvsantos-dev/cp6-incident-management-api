package br.com.fiap.incidentmanagement.service;

import br.com.fiap.incidentmanagement.dto.UserDtos.*;
import br.com.fiap.incidentmanagement.exception.BusinessException;
import br.com.fiap.incidentmanagement.exception.NotFoundException;
import br.com.fiap.incidentmanagement.model.User;
import br.com.fiap.incidentmanagement.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByLogin(request.login())) {
            throw new BusinessException("Já existe um usuário com o login informado.");
        }
        User user = new User(request.name(), request.login(), passwordEncoder.encode(request.password()),
                request.role());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAllByActiveTrue(pageable).map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(get(id));
    }

    @Transactional
    public UserResponse update(UserUpdateRequest request) {
        User user = get(request.id());
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.login() != null && !request.login().isBlank() && !request.login().equals(user.getLogin())) {
            if (userRepository.existsByLogin(request.login())) {
                throw new BusinessException("Já existe um usuário com o login informado.");
            }
            user.setLogin(request.login());
        }
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateRole(RoleUpdateRequest request) {
        User user = get(request.id());
        user.setRole(request.role());
        return UserResponse.from(user);
    }

    /**
     * Cada usuário só pode alterar a própria senha.
     */
    @Transactional
    public void updatePassword(PasswordUpdateRequest request, User authenticated) {
        if (!authenticated.getId().equals(request.id())) {
            throw new AccessDeniedException("Só é permitido alterar a própria senha.");
        }
        User user = get(request.id());
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException("Senha atual incorreta.");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    /** Exclusão lógica: o usuário é inativado e não consegue mais fazer login. */
    @Transactional
    public void inactivate(Long id) {
        get(id).setActive(false);
    }

    private User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + id));
    }
}
