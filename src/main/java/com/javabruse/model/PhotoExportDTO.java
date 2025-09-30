package com.javabruse.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class PhotoExportDTO {
    @ExcelProperty("фото")
    private String name;

    @ExcelProperty("Размер")
    private String fileSize;

    @ExcelProperty("Ссылка через сервис")
    private String src;

    @ExcelProperty("Номер на карте")
    private int number;

    @ExcelProperty("Адрес")
    private String address;

    @ExcelProperty("Широта")
    private Double latitude;

    @ExcelProperty("Долгота")
    private Double longitude;
}
