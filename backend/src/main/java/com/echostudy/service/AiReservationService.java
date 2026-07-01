package com.echostudy.service;

import com.echostudy.dto.AiTaskRequest;
import com.echostudy.entity.AiReservationTask;

import java.util.List;

public interface AiReservationService {

    AiReservationTask createTask(AiTaskRequest request);

    List<AiReservationTask> myTasks();

    List<AiReservationTask> allTasks();

    void executePendingTasks();
}
