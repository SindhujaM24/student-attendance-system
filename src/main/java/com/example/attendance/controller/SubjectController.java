package com.example.attendance.controller;

import com.example.attendance.dto.MessageResponse;
import com.example.attendance.dto.SubjectRequest;
import com.example.attendance.entity.Subject;
import com.example.attendance.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public List<Subject> list() {
        return subjectService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Subject create(@Valid @RequestBody SubjectRequest request) {
        return subjectService.create(request);
    }

    @PutMapping("/{id}")
    public Subject update(@PathVariable("id") Long id, @Valid @RequestBody SubjectRequest request) {
        return subjectService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable("id") Long id) {
        subjectService.delete(id);
        return new MessageResponse("Subject deleted successfully");
    }
}
