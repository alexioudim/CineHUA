package gr.dit.hua.CineHua.controller;

import gr.dit.hua.CineHua.entity.SeatAvailability;
import gr.dit.hua.CineHua.entity.Ticket;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingController {

    List<SeatAvailability> cart = new ArrayList();

    public ResponseEntity<T> addToCart(){

    }

}
