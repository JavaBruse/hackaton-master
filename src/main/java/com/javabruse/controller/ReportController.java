package com.javabruse.controller;

import com.alibaba.excel.EasyExcel;
import com.javabruse.model.PhotoExportDTO;
import com.javabruse.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/report")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Вышрузить отчет по задаче в формате XLSX")
    @GetMapping("/XLSX/{taskId}")
    public void exportToXLSX(@PathVariable UUID taskId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=report_" + taskId + ".xlsx");

        List<PhotoExportDTO> exportData = reportService.getReportForExport(taskId, userUUID);

        EasyExcel.write(response.getOutputStream(), PhotoExportDTO.class)
                .sheet("Photos Report")
                .doWrite(exportData);
    }
}
