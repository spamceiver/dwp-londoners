package uk.gov.dwp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dwp")
public class DwpController {

    private static Logger logger = LoggerFactory.getLogger(DwpController.class);

    private final DwpService dwpService;

    public DwpController(DwpService dwpService) {
        this.dwpService = dwpService;
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> getUsers(
            @RequestParam(name = "distance", defaultValue = "${request.defaultDistance}", required = false) double distance) {
        try {
            return new ResponseEntity<>(dwpService.getAllCityUsers(distance), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error calling the users API", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}