package br.com.fiap.incidentmanagement.controller;

import br.com.fiap.incidentmanagement.dto.IncidentDtos.*;
import br.com.fiap.incidentmanagement.dto.PageResponse;
import br.com.fiap.incidentmanagement.model.IncidentStatus;
import br.com.fiap.incidentmanagement.model.User;
import br.com.fiap.incidentmanagement.service.CategoryService;
import br.com.fiap.incidentmanagement.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/incidents")
@Tag(name = "Ocorrências")
@SecurityRequirement(name = "bearer-key")
public class IncidentController {

    private final IncidentService incidentService;
    private final CategoryService categoryService;

    public IncidentController(IncidentService incidentService, CategoryService categoryService) {
        this.incidentService = incidentService;
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(summary = "Registra uma ocorrência (RF07 - perfil PROFESSOR)")
    public ResponseEntity<IncidentResponse> create(@RequestBody @Valid IncidentRequest request,
                                                   @AuthenticationPrincipal User author,
                                                   UriComponentsBuilder uriBuilder) {
        IncidentResponse incident = incidentService.create(request, author);
        return ResponseEntity.created(uriBuilder.path("/incidents/{id}").buildAndExpand(incident.id()).toUri())
                .body(incident);
    }

    @GetMapping
    @Operation(summary = "Consulta ocorrências com filtros por aluno, turma, categoria, tipo e período (RF08)")
    public PageResponse<IncidentResponse> search(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long schoolClassId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return PageResponse.of(incidentService.search(studentId, schoolClassId, category, type, startDate, endDate,
                includeDeleted, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma ocorrência (RF09)")
    public IncidentResponse findById(@PathVariable Long id) {
        return incidentService.findById(id);
    }

    @GetMapping("/students/{studentId}/history")
    @Operation(summary = "Histórico completo de ocorrências do aluno em todas as turmas")
    public List<IncidentResponse> history(@PathVariable Long studentId) {
        return incidentService.history(studentId);
    }

    @GetMapping("/status")
    @Operation(summary = "Lista os status possíveis de uma ocorrência")
    public List<StatusResponse> statuses() {
        return Arrays.stream(IncidentStatus.values()).map(StatusResponse::from).toList();
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Lista ocorrências por situação: active, waiting, progressing, solved, unsolved ou closed")
    public PageResponse<IncidentResponse> listByStatus(@PathVariable String status,
                                                       @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return PageResponse.of(incidentService.listByStatus(status, pageable));
    }

    @PatchMapping("/status")
    @Operation(summary = "Altera o status de uma ocorrência (COORDENADOR / ANALISTA)")
    public IncidentResponse updateStatus(@RequestBody @Valid StatusUpdateRequest request) {
        return incidentService.updateStatus(request);
    }

    @GetMapping("/categories")
    @Operation(summary = "Nomes das categorias disponíveis para o registro")
    public List<String> categories() {
        return categoryService.categoryNames();
    }

    @GetMapping("/types/{category}")
    @Operation(summary = "Nomes dos tipos de ocorrência da categoria informada")
    public List<String> types(@PathVariable String category) {
        return categoryService.typeNames(category);
    }

    @GetMapping("/summary")
    @Operation(summary = "Dashboard: totais por status, categoria, tipo, turma, período e ranking de reincidentes")
    public IncidentSummary summary() {
        return incidentService.summary();
    }

    @PutMapping
    @Operation(summary = "Altera uma ocorrência própria (perfil PROFESSOR)")
    public IncidentResponse update(@RequestBody @Valid IncidentUpdateRequest request,
                                   @AuthenticationPrincipal User authenticated) {
        return incidentService.update(request, authenticated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui (logicamente) uma ocorrência própria (perfil PROFESSOR)")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User authenticated) {
        incidentService.delete(id, authenticated);
        return ResponseEntity.noContent().build();
    }
}
