package uk.gov.dwp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DwpServiceTest {

    private static final String sampleJsonStrForCoordinatesUsers = "[\n" +
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

    // one of the users above:
    private static final String sampleJsonStrForCityUsers = "[\n" +
            "  {\n" +
            "    \"id\": 266,\n" +
            "    \"first_name\": \"Ancell\",\n" +
            "    \"last_name\": \"Garnsworthy\",\n" +
            "    \"email\": \"agarnsworthy7d@seattletimes.com\",\n" +
            "    \"ip_address\": \"67.4.69.137\",\n" +
            "    \"latitude\": 51.6553959,\n" +
            "    \"longitude\": 0.0572553\n" +
            "  }\n" +
            "]";

    private List<Map<String, Object>> cityUsersObj;
    private List<Map<String, Object>> coordinatesUsersObj;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            cityUsersObj = mapper.readValue(sampleJsonStrForCityUsers, List.class);
            coordinatesUsersObj = mapper.readValue(sampleJsonStrForCoordinatesUsers, List.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @InjectMocks
    private DwpService dwpService;

    @Mock
    RestTemplate restTemplate;

    @Test
    void testUnionOfResults() {
        final double distance = 777;
        Mockito.when(restTemplate.exchange("https://bpdts-test-app.herokuapp.com/city/null/users",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Map<String, Object>>>() {}))
                .thenReturn(new ResponseEntity<>(cityUsersObj, HttpStatus.OK));

        Mockito.when(restTemplate.exchange("https://bpdts-test-app.herokuapp.com/users",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Map<String, Object>>>() {}))
                .thenReturn(new ResponseEntity<>(coordinatesUsersObj, HttpStatus.OK));

        final List<Map<String, Object>> allCityUsers = dwpService.getAllCityUsers(distance);
        // the common user from the two APIs should not be there
        assertEquals(2, allCityUsers.size(), "Wrong number of results");
    }

    @Test
    void testWithZeroDistance() {
        final double distance = 0;

        Mockito.when(restTemplate.exchange("https://bpdts-test-app.herokuapp.com/users",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Map<String, Object>>>() {}))
          .thenReturn(new ResponseEntity<>(coordinatesUsersObj, HttpStatus.OK));

        final List<Map<String, Object>> usersFromCoordinates = dwpService.getUsersFromCoordinates(distance);

        // no users have zero distance from the London coordinates
        assertEquals(0, usersFromCoordinates.size(), "Wrong number of results");
    }

    @Test
    void testWithMaxDistance() {
        final double distance = Double.MAX_VALUE;

        Mockito.when(restTemplate.exchange("https://bpdts-test-app.herokuapp.com/users",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Map<String, Object>>>() {}))
          .thenReturn(new ResponseEntity<>(coordinatesUsersObj, HttpStatus.OK));

        final List<Map<String, Object>> usersFromCoordinates = dwpService.getUsersFromCoordinates(distance);

        // all users should be returned
        assertEquals(coordinatesUsersObj.size(), usersFromCoordinates.size(), "Wrong number of results");
    }
}