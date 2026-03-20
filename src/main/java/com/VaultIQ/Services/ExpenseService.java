package com.VaultIQ.Services;

import com.VaultIQ.DTO.ExpenseDTO;
import com.VaultIQ.Entity.CategoryEntity;
import com.VaultIQ.Entity.ExpenseEntity;
import com.VaultIQ.Entity.ProfileEntity;
import com.VaultIQ.Repository.CategoryRepository;
import com.VaultIQ.Repository.ExpenseRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ExpenseService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private ProfileServices profileServices;
    @Autowired
    private EmailService emailService;

    private ExpenseEntity convertToEntity(ExpenseDTO expenseDTO , ProfileEntity profile , CategoryEntity category){
        return ExpenseEntity.builder()
                .name(expenseDTO.getName())
                .icon(expenseDTO.getIcon())
                .amount(expenseDTO.getAmount())
                .date(expenseDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private ExpenseDTO convertToDto(ExpenseEntity expenseEntity){
        return  ExpenseDTO.builder()
                .id(expenseEntity.getId())
                .name(expenseEntity.getName())
                .icon(expenseEntity.getIcon())
                .categoryId(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getId() : null)
                .categoryName(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getName() : "N/A")
                .amount(expenseEntity.getAmount())
                .date(expenseEntity.getDate())
                .createdAt(expenseEntity.getCreatedAt())
                .updatedAt(expenseEntity.getUpdatedAt())
                .build();
    }

    @CacheEvict(value = "dashboardCache", allEntries = true)
    public  ExpenseDTO addExpense(ExpenseDTO expenseDTO){
        ProfileEntity profileEntity = profileServices.getCurrentProfile();
        CategoryEntity categoryEntity = categoryRepository.findById(expenseDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with "));
        ExpenseEntity newExpense = convertToEntity(expenseDTO,profileEntity,categoryEntity);
        newExpense = expenseRepository.save(newExpense);
        return convertToDto(newExpense);
    }

    // all the expenses of the current month
    public List<ExpenseDTO>getCurrentMonthExpenseOfCurrentUser(){
        ProfileEntity profile = profileServices.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDateTime startDate = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endDate = now.withDayOfMonth(now.lengthOfMonth()).atTime(23,59,59);
        List<ExpenseEntity>expenseEntities  = expenseRepository.findByProfileIdAndDateBetween(profile.getId(),startDate,endDate);
        return expenseEntities.stream().map(this::convertToDto).toList();
    }

    //delete the expense
    @CacheEvict(value = "dashboardCache", allEntries = true)
    public void deleteExpense(Long expenseId){
        ProfileEntity profile = profileServices.getCurrentProfile();
        ExpenseEntity expenseEntity = expenseRepository.findById(expenseId)
                .orElseThrow(()->new RuntimeException("Expense not found with this id " + expenseId));
        if(! expenseEntity.getProfile().getId().equals(profile.getId())){
            throw new RuntimeException("Unauthorized to delete this expense");
        }
        expenseRepository.delete(expenseEntity);
    }

    // latest 5 expenses
    public List<ExpenseDTO>getLatest5ExpenseForCurrentUser(){
        ProfileEntity profile = profileServices.getCurrentProfile();
        List<ExpenseEntity>list = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::convertToDto).toList();
    }

    //get total expense
    public BigDecimal getTotalExpenseForCurrentUser(){
        ProfileEntity profile = profileServices.getCurrentProfile();
        BigDecimal totalAmount = expenseRepository.findTotalExpenseByProfileId(profile.getId()) ;
        return totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public List<ExpenseDTO>filterExpense(LocalDate startDate , LocalDate endDate , String keyword , Sort sort){
        ProfileEntity profile = profileServices.getCurrentProfile();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<ExpenseEntity>expenseList = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(),startDateTime,endDateTime,keyword,sort);
        return expenseList.stream().map(this::convertToDto).toList();
    }

    public List<ExpenseDTO>getExpensesForUserOnDate(Long profileId , LocalDate date){
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX); // 23:59:59.999999999
        List<ExpenseEntity> expenseList = expenseRepository.findByProfileIdAndDateBetween(profileId, startOfDay, endOfDay);
        return expenseList.stream().map(this::convertToDto).toList();
    }

    public List<ExpenseDTO>getAllExpense(){
        ProfileEntity profile = profileServices.getCurrentProfile();
        List<ExpenseEntity>getAllExpenseDetails = expenseRepository.findByProfileIdOrderByDateDesc(profile.getId());
        return getAllExpenseDetails.stream().map(this::convertToDto).toList();
    }

    public byte[] exportAllExpensesToExcel() {
        ProfileEntity profile = profileServices.getCurrentProfile();
        List<ExpenseEntity> expenseList = expenseRepository.findByProfileIdOrderByDateDesc(profile.getId());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Expenses");

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
            for (ExpenseEntity expense : expenseList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(expense.getId());
                row.createCell(1).setCellValue(expense.getName());
                row.createCell(2).setCellValue(
                        expense.getCategory() != null ? expense.getCategory().getName() : "N/A"
                );
                row.createCell(3).setCellValue(
                        expense.getAmount() != null ? expense.getAmount().doubleValue() : 0
                );
                row.createCell(4).setCellValue(expense.getDate() != null ? expense.getDate().toString() : "");
                row.createCell(5).setCellValue(expense.getCreatedAt() != null ? expense.getCreatedAt().toString() : "");
                row.createCell(6).setCellValue(expense.getUpdatedAt() != null ? expense.getUpdatedAt().toString() : "");
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

    public void sendExpenseMail(){
        ProfileEntity profile = profileServices.getCurrentProfile();
        byte[] excelData = exportAllExpensesToExcel();

        String subject = "Your Monthly Expense Statement – VaultIQ";

        String body = "<p>Dear <strong>" + profile.getFullname() + "</strong>,</p>" +
                "<p>Your Expense details are now available for your review. You can view them online at any time through your VaultIQ account.</p>" +
                "<p>For your convenience, please find attached your latest income statement in Excel format.</p>" ;

        emailService.sendMail(profile.getEmail() , subject , body , excelData , "Monthly_Expense_Statement.xlsx");
    }
}
