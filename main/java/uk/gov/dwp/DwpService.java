package uk.gov.dwp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import net.sf.geographiclib.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DwpService {

    private static Logger logger = LoggerFactory.getLogger(DwpService.class);
    RestTemplate restTemplate;

    // 51 deg 30 min 26 sec N
    final static double LondonLat = 51 + (30 / 60.0) + (26 / 60.0 / 60.0);
    // 0 deg 7 min 39 sec W
    final static double LondonLon = 0 - (7 / 60.0) - (39 / 60.0 / 60.0);

    public DwpService() {
        this.restTemplate = new RestTemplate();
    }

    public List<Map<String, Object>> getUsersFromCity()   {
        ResponseEntity<List<Map<String, Object>>> londonUsers = restTemplate.exchange(
                "https://bpdts-test-app.herokuapp.com/city/London/users",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        return londonUsers.getBody() == null ? Collections.emptyList() : londonUsers.getBody();
    }

    public List<Map<String, Object>> getUsersFromCoordinates(double distance)   {
        ResponseEntity<List<Map<String, Object>>> londonUsers = restTemplate.exchange(
                "https://bpdts-test-app.herokuapp.com/users",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        if (londonUsers.getBody() != null) {
            return londonUsers.getBody().stream()
                    .filter(user -> calculateDistanceFromLondon(user) <= distance)
                    .collect(Collectors.toList());
        }
        else    {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getAllLondonUsers(double distance)   {
        final List<Map<String, Object>> usersFromLondon = getUsersFromCity();
        final List<Map<String, Object>> usersFromGreaterLondonArea = getUsersFromCoordinates(distance);
        Set<Integer> ids = new HashSet<>(usersFromGreaterLondonArea.size());
        final List<Map<String, Object>> result = new ArrayList<>(usersFromGreaterLondonArea.size());
        usersFromGreaterLondonArea.forEach(userFromGreaterLondon -> {
            ids.add((Integer) userFromGreaterLondon.get("id"));
            result.add(userFromGreaterLondon);
        });
        usersFromLondon.stream()
                .filter(userFromLondon -> !ids.contains(userFromLondon.get("id")))
                .forEach(result::add);
        return result;
    }

    /*
    Implementation based on this analysis: https://stackoverflow.com/a/62367064/9534558
     */
    private double calculateDistanceFromLondon(Map<String, Object> user) {

        // there are 41 users with "string" coordinates instead of "doubles":
        double latitude = user.get("latitude") instanceof Double ? (double) user.get("latitude") : Double.parseDouble((String) user.get("latitude"));
        double longitude = user.get("longitude") instanceof Double ? (double) user.get("longitude") : Double.parseDouble((String) user.get("longitude"));

        GeodesicData result =
                Geodesic.WGS84.Inverse(LondonLat, LondonLon, latitude, longitude);

        double distanceInMeters = result.s12;
        double distanceInMiles = distanceInMeters / 1609.34;
        return distanceInMiles;
    }

}
