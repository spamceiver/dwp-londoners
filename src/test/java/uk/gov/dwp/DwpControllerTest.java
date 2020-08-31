package uk.gov.dwp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(DwpController.class)
class DwpControllerTest {

    private static final String sampleJsonStr = "[\n" +
            "  {\n" +
            "    \"id\": 266,\n" +
            "    \"first_name\": \"Ancell\",\n" +
            "    \"last_name\": \"Garnsworthy\",\n" +
            "    \"email\": \"agarnsworthy7d@seattletimes.com\",\n" +
            "    \"ip_address\": \"67.4.69.137\",\n" +
            "    \"latitude\": 51.6553959,\n" +
            "    \"longitude\": 0.0572553\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": 322,\n" +
            "    \"first_name\": \"Hugo\",\n" +
            "    \"last_name\": \"Lynd\",\n" +
            "    \"email\": \"hlynd8x@merriam-webster.com\",\n" +
            "    \"ip_address\": \"109.0.153.166\",\n" +
            "    \"latitude\": 51.6710832,\n" +
            "    \"longitude\": 0.8078532\n" +
            "  }\n" +
            "]";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DwpService dwpService;

    private List<Map<String, Object>> jsonObj;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            jsonObj = mapper.readValue(sampleJsonStr, List.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private DwpController controller;

    @Test
    public void contextLoads() {
        assertThat(controller).isNotNull();
    }

    @Test
    void getUsers() throws Exception {
        final int distance = 10;
        when(dwpService.getAllCityUsers(distance))
                .thenReturn(jsonObj);
        mockMvc.perform(get("/dwp/users?distance={distance}", distance))
                .andExpect(status().isOk())
                .andExpect(content().json(sampleJsonStr));
    }

    @Test
    void getUsersWithException() throws Exception {
        final int anyDistance = 10;
        when(dwpService.getAllCityUsers(any(Double.class)))
                .thenThrow(new RuntimeException("some runtime exception"));
        mockMvc.perform(get("/dwp/users?city=distance={distance}", anyDistance))
                .andExpect(status().is5xxServerError());
    }

}