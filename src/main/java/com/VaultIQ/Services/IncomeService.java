package com.VaultIQ.Services;

import com.VaultIQ.DTO.ExpenseDTO;
import com.VaultIQ.DTO.IncomeDTO;
import com.VaultIQ.Entity.CategoryEntity;
import com.VaultIQ.Entity.ExpenseEntity;
import com.VaultIQ.Entity.IncomeEntity;
import com.VaultIQ.Entity.ProfileEntity;
import com.VaultIQ.Repository.CategoryRepository;
import com.VaultIQ.Repository.IncomeRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class IncomeService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private IncomeRepository incomeRepository;
    @Autowired
    private ProfileServices profileServices;
    @Autowired
    private EmailService emailService;

    private IncomeEntity convertToEntity(IncomeDTO incomeDTO , ProfileEntity profile , CategoryEntity category){
        return IncomeEntity.builder()
                .name(incomeDTO.getName())
                .icon(incomeDTO.getIcon())
                .amount(incomeDTO.getAmount())
                .date(incomeDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private IncomeDTO convertToDto(IncomeEntity incomeEntity){
        return  IncomeDTO.builder()
                .id(incomeEntity.getId())
                .name(incomeEntity.getName())
                .icon(incomeEntity.getIcon())
                .categoryId(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getId() : null)
                .categoryName(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getName() : "N/A")
                .amount(incomeEntity.getAmount())
                .date(incomeEntity.getDate())
                .createdAt(incomeEntity.getCreatedAt())
                .updatedAt(incomeEntity.getUpdatedAt())
                .build();
    }

    @CacheEvict(value = "dashboardCache", allEntries = true)
    public  IncomeDTO addIncome(IncomeDTO incomeDTO){
        ProfileEntity profileEntity = profileServices.getCurrentProfile();
        CategoryEntity categoryEntity = categoryRepository.findById(incomeDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with "));
        IncomeEntity newIncome = convertToEntity(incomeDTO,profileEntity,categoryEntity);
        newIncome =incomeRepository.save(newIncome);
        return convertToDto(newIncome);
    }

    public List<IncomeDTO> getCurrentMonthIncomeOfCurrentUser(){
        ProfileEntity profile = profileServices.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDateTime startDate = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endDate = now.withDayOfMonth(now.lengthOfMonth()).atTime(23,59,59);
        List<IncomeEntity>incomeEntities = incomeRepository.findByProfileIdAndDateBetween(profile.getId(),startDate,endDate);
        return incomeEntities.stream().map(this::convertToDto).toList();
    }

    public List<IncomeDTO>getAllIncome(){
        ProfileEntity profile = profileServices.getCurrentProfile();
        List<IncomeEntity>getAllIncomeDetails = incomeRepository.findByProfileIdOrderByDateDesc(profile.getId());
        return getAllIncomeDetails.stream().map(this::convertToDto).toList();
    }

    //delete the expense
    @CacheEvict(value = "dashboardCache", allEntries = true)
    public void deleteIncome(Long incomeId){
        ProfileEntity profile = profileServices.getCurrentProfile();
        IncomeEntity incomeEntity = incomeRepository.findById(incomeId)
                .orElseThrow(()->new RuntimeException("income not found with this id " + incomeId));
        if(!incomeEntity.getProfile().getId().equals(profile.getId())){
            throw new RuntimeException("Unauthorized to delete this income");
        }
        incomeRepository.delete(incomeEntity);
    }

    // latest 5 income
    public List<IncomeDTO>getLatest5IncomeForCurrentUser(){
        ProfileEntity profile = profileServices.getCurrentProfile();
        List<IncomeEntity>list = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::convertToDto).toList();
    }

    //get total income
    public BigDecimal getTotalIncomeForCurrentUser(){
        ProfileEntity profile = profileServices.getCurrentProfile();
        BigDecimal totalAmount = incomeRepository.findTotalIncomeByProfileId(profile.getId()) ;
        return totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public List<IncomeDTO>filterIncome(LocalDate startDate , LocalDate endDate , String keyword , Sort sort){
        ProfileEntity profile = profileServices.getCurrentProfile();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<IncomeEntity>incomeList = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(),startDateTime,endDateTime,keyword,sort);
        return incomeList.stream().map(this::convertToDto).toList();
    }

    public byte[] exportAllIncomesToExcel() {
        ProfileEntity profile = profileServices.getCurrentProfile();
        List<IncomeEntity> incomeList = incomeRepository.findByProfileIdOrderByDateDesc(profile.getId());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Incomes");

            // Header row
            String[] headers = {"ID", "Name", "Category", "Amount", "Date", "Created At", "Updated At"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Data rows
            int rowIdx = 1;
            for (IncomeEntity income : incomeList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(income.getId());
                row.createCell(1).setCellValue(income.getName());
                row.createCell(2).setCellValue(
                        income.getCategory() != null ? income.getCategory().getName() : "N/A"
                );
                row.createCell(3).setCellValue(
                        income.getAmount() != null ? income.getAmount().doubleValue() : 0
                );
                row.createCell(4).setCellValue(income.getDate() != null ? income.getDate().toString() : "");
                row.createCell(5).setCellValue(income.getCreatedAt() != null ? income.getCreatedAt().toString() : "");
                row.createCell(6).setCellValue(income.getUpdatedAt() != null ? income.getUpdatedAt().toString() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel file", e);
        }
    }

    public void sendIncomeMail(){
        ProfileEntity profile = profileServices.getCurrentProfile();
        byte[] excelData = exportAllIncomesToExcel();

        String subject = "Your Monthly Income Statement – VaultIQ";

        String body = "<p>Dear <strong>" + profile.getFullname() + "</strong>,</p>" +
                "<p>Your income details are now available for your review. You can view them online at any time through your VaultIQ account.</p>" +
                "<p>For your convenience, please find attached your latest income statement in Excel format.</p>" ;

        emailService.sendMail(profile.getEmail() , subject , body , excelData , "Monthly_Income_Statement.xlsx");

    }

}
