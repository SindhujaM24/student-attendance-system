package com.example.attendance.controller;

import com.example.attendance.dto.MessageResponse;
import com.example.attendance.dto.StudentRequest;
import com.example.attendance.dto.StudentResponse;
import com.example.attendance.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public List<StudentResponse> list(@RequestParam(name = "search", required = false, defaultValue = "") String search) {
        return studentService.search(search);
    }

    @GetMapping("/{id}")
    public StudentResponse get(@PathVariable("id") Long id) {
        return studentService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse create(@Valid @RequestBody StudentRequest request) {
        return studentService.create(request);
    }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable("id") Long id, @Valid @RequestBody StudentRequest request) {
        return studentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable("id") Long id) {
        studentService.delete(id);
        return new MessageResponse("Student deleted successfully");
    }
}
