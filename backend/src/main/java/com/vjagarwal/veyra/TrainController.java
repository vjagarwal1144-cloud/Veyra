package com.vjagarwal.veyra;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trains")
public class TrainController {
    private final TrainService service;
    public TrainController(TrainService service) { this.service = service; }

    @GetMapping("/{number}")
    public Object track(@PathVariable String number) {
        return service.find(number);
    }
}
