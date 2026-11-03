package br.com.fiap.incidentmanagement.controller;

import br.com.fiap.incidentmanagement.dto.CategoryDtos.CategoryRequest;
import br.com.fiap.incidentmanagement.dto.CategoryDtos.CategoryResponse;
import br.com.fiap.incidentmanagement.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categorias de ocorrência")
@SecurityRequirement(name = "bearer-key")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(summary = "Cadastra uma categoria (RF05)")
    public ResponseEntity<CategoryResponse> create(@RequestBody @Valid CategoryRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.created(uriBuilder.path("/categories/{id}").buildAndExpand(category.id()).toUri())
                .body(category);
    }

    @GetMapping
    @Operation(summary = "Lista as categorias")
    public List<CategoryResponse> list() {
        return categoryService.listCategories();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma categoria pelo id")
    public CategoryResponse findById(@PathVariable Long id) {
        return categoryService.findCategory(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Renomeia uma categoria")
    public CategoryResponse update(@PathVariable Long id, @RequestBody @Valid CategoryRequest request) {
        return categoryService.updateCategory(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma categoria sem tipos vinculados")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
