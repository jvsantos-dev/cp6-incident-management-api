package br.com.fiap.incidentmanagement.controller;

import br.com.fiap.incidentmanagement.dto.PageResponse;
import br.com.fiap.incidentmanagement.dto.SchoolClassDtos.SchoolClassResponse;
import br.com.fiap.incidentmanagement.dto.StudentDtos.*;
import br.com.fiap.incidentmanagement.model.Student;
import br.com.fiap.incidentmanagement.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/students")
@Tag(name = "Alunos")
@SecurityRequirement(name = "bearer-key")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    @Operation(summary = "Cadastra um aluno (RF02)")
    public ResponseEntity<StudentResponse> create(@RequestBody @Valid StudentRequest request,
                                                  UriComponentsBuilder uriBuilder) {
        StudentResponse student = studentService.create(request);
        return ResponseEntity.created(uriBuilder.path("/students/{id}").buildAndExpand(student.id()).toUri())
                .body(student);
    }

    @GetMapping
    @Operation(summary = "Lista os alunos ativos, com filtro opcional por nome (paginado)")
    public PageResponse<StudentResponse> list(@RequestParam(required = false) String name,
                                              @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return PageResponse.of(studentService.list(name, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um aluno pelo id")
    public StudentResponse findById(@PathVariable Long id) {
        return studentService.findById(id);
    }

    @GetMapping("/{id}/schoolclasses")
    @Operation(summary = "Lista as turmas em que o aluno está ou esteve matriculado")
    public List<SchoolClassResponse> listSchoolClasses(@PathVariable Long id) {
        return studentService.listSchoolClasses(id);
    }

    @PutMapping
    @Operation(summary = "Atualiza nome e data de nascimento do aluno")
    public StudentResponse update(@RequestBody @Valid StudentUpdateRequest request) {
        return studentService.update(request);
    }

    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Envia a foto do aluno (campo multipart \"image\")")
    public StudentResponse uploadImage(@PathVariable Long id, @RequestParam("image") MultipartFile image) {
        return studentService.uploadImage(id, image);
    }

    @GetMapping("/{id}/image")
    @Operation(summary = "Devolve a foto do aluno")
    public ResponseEntity<byte[]> image(@PathVariable Long id) {
        Student student = studentService.getWithImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(student.getImageContentType()))
                .body(student.getImage());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativa o aluno (exclusão lógica)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.inactivate(id);
        return ResponseEntity.noContent().build();
    }
}
