package uk.gov.dwp;


import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DwpService {

    private static Logger logger = LoggerFactory.getLogger(DwpService.class);

    @Autowired
    private RestTemplate restTemplate;

    // London lat: 51 deg 30 min 26 sec N
    double CITY_LATITUDE = 51 + (30 / 60.0) + (26 / 60.0 / 60.0);
    // London lon: 0 deg 7 min 39 sec W
    double CITY_LONGITUDE = 0 - (7 / 60.0) - (39 / 60.0 / 60.0);

    @Value("${cityLat}")
    private String cityLatStr;

    @Value("${cityLon}")
    private String cityLonStr;

    @PostConstruct
    public void init() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("Nashorn");
        try {
            CITY_LATITUDE = (double) scriptEngine.eval(cityLatStr);
            CITY_LONGITUDE = (double) scriptEngine.eval(cityLonStr);
        } catch (ScriptException e) {
            logger.warn("Failed to parse configured coordinates. Reverting to default London ones.");
            CITY_LATITUDE = 51 + (30 / 60.0) + (26 / 60.0 / 60.0);
            CITY_LONGITUDE = 0 - (7 / 60.0) - (39 / 60.0 / 60.0);
        }
    }

    public List<Map<String, Object>> getUsersFromCity(String city)  {
        ResponseEntity<List<Map<String, Object>>> cityUsers = restTemplate.exchange(
                "https://bpdts-test-app.herokuapp.com/city/" + city + "/users",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        return cityUsers.getBody() == null ? Collections.emptyList() : cityUsers.getBody();
    }

    public List<Map<String, Object>> getUsersFromCoordinates(double distance)   {
        ResponseEntity<List<Map<String, Object>>> londonUsers = restTemplate.exchange(
                "https://bpdts-test-app.herokuapp.com/users",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        if (londonUsers.getBody() != null) {
            return londonUsers.getBody().stream()
                    .filter(user -> calculateDistanceFromCity(user) <= distance)
                    .collect(Collectors.toList());
        }
        else    {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getAllCityUsers(String city, double distance)   {
        final List<Map<String, Object>> usersFromCity = getUsersFromCity(city);
        final List<Map<String, Object>> usersFromCoordinates = getUsersFromCoordinates(distance);

        // Keep an index of the ids to avoid duplicates from the two APIs:
        Set<Integer> ids = new HashSet<>(usersFromCoordinates.size());

        final List<Map<String, Object>> result = new ArrayList<>(usersFromCoordinates.size());

        // Go through the results from the "/users" API, add them in the resulting list and keep their IDs:
        usersFromCoordinates.forEach(userFromCoordinates -> {
            ids.add((Integer) userFromCoordinates.get("id"));
            result.add(userFromCoordinates);
        });

        // Go through the results from the "/city" API and if they have not been returned from "/users" API, add them in the result:
        usersFromCity.stream()
                .filter(userFromCity -> !ids.contains(userFromCity.get("id")))
                .forEach(result::add);

        return result;
    }

    /* Implementation based on this analysis: https://stackoverflow.com/a/62367064/9534558 */
    private double calculateDistanceFromCity(Map<String, Object> user) {

        // there are 41 users with "string" coordinates instead of "doubles":
        double userLatitude = user.get("latitude") instanceof Double ? (double) user.get("latitude") : Double.parseDouble((String) user.get("latitude"));
        double userLongitude = user.get("longitude") instanceof Double ? (double) user.get("longitude") : Double.parseDouble((String) user.get("longitude"));

        GeodesicData result =
                Geodesic.WGS84.Inverse(CITY_LATITUDE, CITY_LONGITUDE, userLatitude, userLongitude);

        double distanceInMeters = result.s12;
        double distanceInMiles = distanceInMeters / 1609.34;
        return distanceInMiles;
    }

}
