package uk.gov.dwp;

import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = DwpApplication.class)
@AutoConfigureMockMvc
class IntegrationTests {

    @Autowired
    private DwpController dwpController;
    @Autowired
    private MockMvc mvc;

    @Test
    void testApiCalls() throws Exception {

        MvcResult mvcResult = mvc.perform(get("/dwp/users?distance={distance}", 0)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email", containsString("@")))
                .andReturn();
        JSONArray jsonArray = new JSONArray(mvcResult.getResponse().getContentAsString());
        final int zeroDistanceUsersSize = jsonArray.length();

        mvcResult = mvc.perform(get("/dwp/users?distance={distance}", 50)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email", containsString("@")))
                .andReturn();
        jsonArray = new JSONArray(mvcResult.getResponse().getContentAsString());
        final int fiftyMilesUsersSize = jsonArray.length();

        mvcResult = mvc.perform(get("/dwp/users?distance={distance}", Double.MAX_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email", containsString("@")))
                .andReturn();
        jsonArray = new JSONArray(mvcResult.getResponse().getContentAsString());
        final int allUsersSize = jsonArray.length();

        assertTrue(allUsersSize > fiftyMilesUsersSize && fiftyMilesUsersSize > zeroDistanceUsersSize,
                "The distance parameter is not working.");

    }

}
