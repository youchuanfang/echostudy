package com.echostudy.controller;

import com.echostudy.common.Result;
import com.echostudy.dto.AiTaskRequest;
import com.echostudy.entity.AiReservationTask;
import com.echostudy.service.AiReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AiReservationController {

    private final AiReservationService aiReservationService;

    @PostMapping("/api/student/ai-tasks")
    public Result<AiReservationTask> create(@Valid @RequestBody AiTaskRequest request) {
        return Result.ok(aiReservationService.createTask(request));
    }

    @GetMapping("/api/student/ai-tasks/my")
    public Result<List<AiReservationTask>> myTasks() {
        return Result.ok(aiReservationService.myTasks());
    }

    @GetMapping("/api/admin/ai-tasks")
    public Result<List<AiReservationTask>> allTasks() {
        return Result.ok(aiReservationService.allTasks());
    }
}
