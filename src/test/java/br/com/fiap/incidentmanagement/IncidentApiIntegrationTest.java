package br.com.fiap.incidentmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa os fluxos principais sobre os dados iniciais do DataSeeder
 * (turma 1 = 1A com João(1) e Maria(2); turma 2 = 2B com Maria(2) e Pedro(3)).
 */
@SpringBootTest
@AutoConfigureMockMvc
class IncidentApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private String token(String login) throws Exception {
        String body = mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + login + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(body).get("tokenJWT").asText();
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"professor\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithoutTokenReturns401() throws Exception {
        mvc.perform(get("/incidents")).andExpect(status().isUnauthorized());
    }

    @Test
    void professorRegistersUpdatesAndDeletesOwnIncident() throws Exception {
        String professor = token("professor");
        String json = """
                {"idSchoolClass": 1, "idStudent": 1, "registerDate": "01/11/2026 - 10:30",
                 "category": "Pedagógica", "type": "Rendimento", "description": "Não entregou o trabalho."}""";

        String created = mvc.perform(post("/incidents").header("Authorization", professor)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentName").value("João Silva"))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(created).get("id").asLong();

        mvc.perform(put("/incidents").header("Authorization", professor).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": " + id + ", \"description\": \"Descrição revisada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Descrição revisada"));

        // outro professor pode consultar, mas não alterar nem excluir
        String otherProfessor = token("professor2");
        mvc.perform(get("/incidents/" + id).header("Authorization", otherProfessor)).andExpect(status().isOk());
        mvc.perform(delete("/incidents/" + id).header("Authorization", otherProfessor))
                .andExpect(status().isForbidden());

        mvc.perform(delete("/incidents/" + id).header("Authorization", professor))
                .andExpect(status().isNoContent());
        mvc.perform(get("/incidents/" + id).header("Authorization", professor))
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    void studentMustBelongToSelectedClass() throws Exception {
        String json = """
                {"idSchoolClass": 1, "idStudent": 3, "category": "Disciplinar",
                 "type": "Indisciplina", "description": "Teste"}""";
        mvc.perform(post("/incidents").header("Authorization", token("professor"))
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void typeMustBelongToCategory() throws Exception {
        String json = """
                {"idSchoolClass": 1, "idStudent": 1, "category": "Disciplinar",
                 "type": "Atraso", "description": "Teste"}""";
        mvc.perform(post("/incidents").header("Authorization", token("professor"))
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void onlyProfessorCanRegisterIncidents() throws Exception {
        String json = """
                {"idSchoolClass": 1, "idStudent": 1, "category": "Disciplinar",
                 "type": "Indisciplina", "description": "Teste"}""";
        mvc.perform(post("/incidents").header("Authorization", token("analista"))
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminManagesClassesAndLinksStudents() throws Exception {
        String admin = token("admin");
        String created = mvc.perform(post("/schoolclasses").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"3C\",\"shift\":\"NOITE\",\"year\":2027,\"semester\":\"PRIMEIRO\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long classId = mapper.readTree(created).get("id").asLong();

        mvc.perform(post("/schoolclasses/" + classId + "/students/1").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").value(1));
        mvc.perform(post("/schoolclasses/" + classId + "/students/1").header("Authorization", admin))
                .andExpect(status().isBadRequest());

        // professor não pode cadastrar turmas
        mvc.perform(post("/schoolclasses").header("Authorization", token("professor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"shift\":\"NOITE\",\"year\":2027,\"semester\":\"PRIMEIRO\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void coordinatorSeesDashboardButProfessorDoesNot() throws Exception {
        mvc.perform(get("/incidents/summary").header("Authorization", token("coordenador")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.byCategory").isArray())
                .andExpect(jsonPath("$.bySchooclass").isArray())
                .andExpect(jsonPath("$.byStudent").isArray())
                .andExpect(jsonPath("$.byPeriod").isArray());

        mvc.perform(get("/incidents/summary").header("Authorization", token("professor")))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchFiltersByStudentAndPeriod() throws Exception {
        mvc.perform(get("/incidents").header("Authorization", token("analista"))
                        .param("studentId", "2").param("startDate", "2000-01-01").param("endDate", "2100-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].studentName").value("Maria Souza"));
    }
}
