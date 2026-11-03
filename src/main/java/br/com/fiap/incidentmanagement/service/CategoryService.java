package br.com.fiap.incidentmanagement.service;

import br.com.fiap.incidentmanagement.dto.CategoryDtos.*;
import br.com.fiap.incidentmanagement.exception.BusinessException;
import br.com.fiap.incidentmanagement.exception.NotFoundException;
import br.com.fiap.incidentmanagement.model.Category;
import br.com.fiap.incidentmanagement.model.IncidentType;
import br.com.fiap.incidentmanagement.repository.CategoryRepository;
import br.com.fiap.incidentmanagement.repository.IncidentRepository;
import br.com.fiap.incidentmanagement.repository.IncidentTypeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RF05 e RF06 - categorias e tipos de ocorrência.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final IncidentTypeRepository typeRepository;
    private final IncidentRepository incidentRepository;

    public CategoryService(CategoryRepository categoryRepository, IncidentTypeRepository typeRepository,
                           IncidentRepository incidentRepository) {
        this.categoryRepository = categoryRepository;
        this.typeRepository = typeRepository;
        this.incidentRepository = incidentRepository;
    }

    // ---------- categorias ----------

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Já existe uma categoria com esse nome.");
        }
        return CategoryResponse.from(categoryRepository.save(new Category(request.name())));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll(Sort.by("name")).stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findCategory(Long id) {
        return CategoryResponse.from(getCategory(id));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = getCategory(id);
        categoryRepository.findByNameIgnoreCase(request.name())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessException("Já existe uma categoria com esse nome.");
                });
        category.setName(request.name());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategory(id);
        if (typeRepository.existsByCategoryId(id)) {
            throw new BusinessException("A categoria possui tipos de ocorrência vinculados e não pode ser excluída.");
        }
        categoryRepository.delete(category);
    }

    // ---------- tipos ----------

    @Transactional
    public IncidentTypeResponse createType(IncidentTypeRequest request) {
        Category category = getCategory(request.categoryId());
        if (typeRepository.findByNameIgnoreCaseAndCategoryId(request.name(), category.getId()).isPresent()) {
            throw new BusinessException("Já existe um tipo com esse nome nesta categoria.");
        }
        return IncidentTypeResponse.from(typeRepository.save(new IncidentType(request.name(), category)));
    }

    @Transactional(readOnly = true)
    public List<IncidentTypeResponse> listTypes(Long categoryId) {
        List<IncidentType> types = categoryId == null
                ? typeRepository.findAll(Sort.by("category.name", "name"))
                : typeRepository.findAllByCategoryIdOrderByName(categoryId);
        return types.stream().map(IncidentTypeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public IncidentTypeResponse findType(Long id) {
        return IncidentTypeResponse.from(getType(id));
    }

    @Transactional
    public IncidentTypeResponse updateType(Long id, IncidentTypeRequest request) {
        IncidentType type = getType(id);
        Category category = getCategory(request.categoryId());
        typeRepository.findByNameIgnoreCaseAndCategoryId(request.name(), category.getId())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessException("Já existe um tipo com esse nome nesta categoria.");
                });
        type.setName(request.name());
        type.setCategory(category);
        return IncidentTypeResponse.from(type);
    }

    @Transactional
    public void deleteType(Long id) {
        IncidentType type = getType(id);
        if (incidentRepository.existsByTypeId(id)) {
            throw new BusinessException("O tipo já foi usado em ocorrências e não pode ser excluído.");
        }
        typeRepository.delete(type);
    }

    // ---------- consultas usadas no registro de ocorrência ----------

    @Transactional(readOnly = true)
    public List<String> categoryNames() {
        return categoryRepository.findAll(Sort.by("name")).stream().map(Category::getName).toList();
    }

    @Transactional(readOnly = true)
    public List<String> typeNames(String categoryName) {
        return typeRepository.findAllByCategoryNameIgnoreCaseOrderByName(categoryName).stream()
                .map(IncidentType::getName)
                .toList();
    }

    Category getCategoryByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new BusinessException("Categoria inexistente: " + name));
    }

    IncidentType getTypeByName(String name, Category category) {
        return typeRepository.findByNameIgnoreCaseAndCategoryId(name, category.getId())
                .orElseThrow(() -> new BusinessException(
                        "Tipo de ocorrência '" + name + "' não pertence à categoria " + category.getName()));
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoria não encontrada: " + id));
    }

    private IncidentType getType(Long id) {
        return typeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de ocorrência não encontrado: " + id));
    }
}
