package gr.dit.hua.CineHua.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.dit.hua.CineHua.dto.request.AuditoriumRequest;
import gr.dit.hua.CineHua.entity.Auditorium;
import gr.dit.hua.CineHua.service.AuditoriumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auditorium")
public class AuditoriumController {

    @Autowired
    private AuditoriumService auditoriumService;

    @GetMapping("/view")
    public ResponseEntity<List<Auditorium>> getAll() {

        return ResponseEntity.ok(auditoriumService.getAll());
    }
    @PostMapping("/new")
    public ResponseEntity<String> createAuditorium(@RequestBody AuditoriumRequest auditoriumDTO) {

        Auditorium auditorium = new Auditorium();
        auditorium.setName(auditoriumDTO.getName());
        auditorium.setRows(auditoriumDTO.getRows());
        auditorium.setColumns(auditoriumDTO.getColumns());

        try {
            auditoriumService.createAuditorium(auditorium);
            return ResponseEntity.ok("Auditorium created: " + auditorium.getName());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("JsonProcessingException" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteAuditorium(@PathVariable Long id) {
        try {
            String name = auditoriumService.findAuditoriumById(id).getName();
            auditoriumService.deleteAuditorium(id);
            return ResponseEntity.ok("Auditorium deleted:" + name);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("JsonProcessingException" + e.getMessage());
        }

    }

}
