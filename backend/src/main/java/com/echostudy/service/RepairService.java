package com.echostudy.service;

import com.echostudy.dto.AdminRepairHandleRequest;
import com.echostudy.dto.RepairRequest;
import com.echostudy.vo.RepairVO;

import java.time.LocalDate;
import java.util.List;

public interface RepairService {

    RepairVO create(RepairRequest request);

    List<RepairVO> myRepairs();

    RepairVO cancel(Long id);

    List<RepairVO> adminList(String status, String repairType, String repairLevel, Long roomId,
                             LocalDate startDate, LocalDate endDate, String keyword);

    RepairVO detail(Long id);

    RepairVO accept(Long id, AdminRepairHandleRequest request);

    RepairVO process(Long id, AdminRepairHandleRequest request);

    RepairVO reject(Long id, AdminRepairHandleRequest request);

    RepairVO finish(Long id, AdminRepairHandleRequest request);
}
