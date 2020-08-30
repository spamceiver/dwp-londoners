package uk.gov.dwp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class IntegrationTests {

    @Autowired
    private DwpController dwpController;


    @Test
    void testApiCalls() {

        // zero miles from DummyCity
        final ResponseEntity<List<Map<String, Object>>> zeroUsers = dwpController.getUsers(0, "DummyCity");
        assertTrue(zeroUsers.getBody() != null && zeroUsers.getBody().isEmpty(), "Should return zero results");

        // zero miles from London
        final ResponseEntity<List<Map<String, Object>>> londonUsers = dwpController.getUsers(0, "London");
        assertTrue(londonUsers.getBody() != null && londonUsers.getBody().size() > 0, "Should return some results");

        // 50 miles from London
        final ResponseEntity<List<Map<String, Object>>> fiftyMilesFromLondonUsers = dwpController.getUsers(50, "London");
        assertTrue(fiftyMilesFromLondonUsers.getBody() != null
                && fiftyMilesFromLondonUsers.getBody().size() > 0
                && fiftyMilesFromLondonUsers.getBody().size() > londonUsers.getBody().size(), "Should return more results than before");

        // all users
        final ResponseEntity<List<Map<String, Object>>> allUsers = dwpController.getUsers(Double.MAX_VALUE, "London");
        assertTrue(allUsers.getBody() != null
                && allUsers.getBody().size() > 0
                && allUsers.getBody().size() > fiftyMilesFromLondonUsers.getBody().size(), "Should return more results then before");
    }

}
