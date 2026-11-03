package br.com.fiap.incidentmanagement.controller;

import br.com.fiap.incidentmanagement.dto.PageResponse;
import br.com.fiap.incidentmanagement.dto.SchoolClassDtos.*;
import br.com.fiap.incidentmanagement.dto.StudentDtos.StudentResponse;
import br.com.fiap.incidentmanagement.model.Semester;
import br.com.fiap.incidentmanagement.model.Shift;
import br.com.fiap.incidentmanagement.service.SchoolClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/schoolclasses")
@Tag(name = "Turmas")
@SecurityRequirement(name = "bearer-key")
public class SchoolClassController {

    private final SchoolClassService schoolClassService;

    public SchoolClassController(SchoolClassService schoolClassService) {
        this.schoolClassService = schoolClassService;
    }

    @PostMapping
    @Operation(summary = "Cadastra uma turma semestral (RF03)")
    public ResponseEntity<SchoolClassResponse> create(@RequestBody @Valid SchoolClassRequest request,
                                                      UriComponentsBuilder uriBuilder) {
        SchoolClassResponse schoolClass = schoolClassService.create(request);
        return ResponseEntity.created(uriBuilder.path("/schoolclasses/{id}").buildAndExpand(schoolClass.id()).toUri())
                .body(schoolClass);
    }

    @GetMapping
    @Operation(summary = "Lista as turmas ativas (paginado)")
    public PageResponse<SchoolClassResponse> list(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return PageResponse.of(schoolClassService.list(false, pageable));
    }

    @GetMapping("/canceled")
    @Operation(summary = "Lista as turmas canceladas (paginado)")
    public PageResponse<SchoolClassResponse> listCanceled(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return PageResponse.of(schoolClassService.list(true, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma turma pelo id")
    public SchoolClassResponse findById(@PathVariable Long id) {
        return schoolClassService.findById(id);
    }

    @GetMapping("/shifts")
    @Operation(summary = "Lista os turnos disponíveis")
    public List<String> shifts() {
        return Arrays.stream(Shift.values()).map(Enum::name).toList();
    }

    @GetMapping("/semesters")
    @Operation(summary = "Lista os semestres disponíveis")
    public List<String> semesters() {
        return Arrays.stream(Semester.values()).map(Enum::name).toList();
    }

    @PutMapping
    @Operation(summary = "Atualiza os dados da turma")
    public SchoolClassResponse update(@RequestBody @Valid SchoolClassUpdateRequest request) {
        return schoolClassService.update(request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela a turma (exclusão lógica)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        schoolClassService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/students")
    @Operation(summary = "Lista os alunos vinculados à turma")
    public List<StudentResponse> listStudents(@PathVariable Long id) {
        return schoolClassService.listStudents(id);
    }

    @PostMapping("/{id}/students/{studentId}")
    @Operation(summary = "Vincula um aluno à turma (RF04)")
    public SchoolClassResponse addStudent(@PathVariable Long id, @PathVariable Long studentId) {
        return schoolClassService.addStudent(id, studentId);
    }

    @DeleteMapping("/{id}/students/{studentId}")
    @Operation(summary = "Desvincula um aluno da turma")
    public SchoolClassResponse removeStudent(@PathVariable Long id, @PathVariable Long studentId) {
        return schoolClassService.removeStudent(id, studentId);
    }
}
