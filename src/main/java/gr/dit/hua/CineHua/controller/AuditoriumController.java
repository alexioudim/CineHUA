package gr.dit.hua.CineHua.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.dit.hua.CineHua.entity.Auditorium;
import gr.dit.hua.CineHua.service.AuditoriumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auditorium")
public class AuditoriumController {

    @Autowired
    private AuditoriumService auditoriumService;

    @PostMapping("/new")
    public ResponseEntity<String> createAuditorium() {

        Auditorium auditorium = new Auditorium();
        auditorium.setName("Test Room 2");
        auditorium.setRows(13);
        auditorium.setColumns(12);

        try {
            auditoriumService.createAuditorium(auditorium);
            return ResponseEntity.ok("Auditorium created:" + auditorium.getName());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("JsonProcessingException" + e.getMessage());
        }
    }

}
