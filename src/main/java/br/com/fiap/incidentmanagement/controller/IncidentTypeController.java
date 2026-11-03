package br.com.fiap.incidentmanagement.controller;

import br.com.fiap.incidentmanagement.dto.CategoryDtos.IncidentTypeRequest;
import br.com.fiap.incidentmanagement.dto.CategoryDtos.IncidentTypeResponse;
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
@RequestMapping("/types")
@Tag(name = "Tipos de ocorrência")
@SecurityRequirement(name = "bearer-key")
public class IncidentTypeController {

    private final CategoryService categoryService;

    public IncidentTypeController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(summary = "Cadastra um tipo de ocorrência vinculado a uma categoria (RF06)")
    public ResponseEntity<IncidentTypeResponse> create(@RequestBody @Valid IncidentTypeRequest request,
                                                       UriComponentsBuilder uriBuilder) {
        IncidentTypeResponse type = categoryService.createType(request);
        return ResponseEntity.created(uriBuilder.path("/types/{id}").buildAndExpand(type.id()).toUri()).body(type);
    }

    @GetMapping
    @Operation(summary = "Lista os tipos de ocorrência, com filtro opcional por categoria")
    public List<IncidentTypeResponse> list(@RequestParam(required = false) Long categoryId) {
        return categoryService.listTypes(categoryId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um tipo de ocorrência pelo id")
    public IncidentTypeResponse findById(@PathVariable Long id) {
        return categoryService.findType(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um tipo de ocorrência")
    public IncidentTypeResponse update(@PathVariable Long id, @RequestBody @Valid IncidentTypeRequest request) {
        return categoryService.updateType(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um tipo de ocorrência ainda não utilizado")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.deleteType(id);
        return ResponseEntity.noContent().build();
    }
}
