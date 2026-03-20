package com.VaultIQ.Controller;

import com.VaultIQ.DTO.ExpenseDTO;
import com.VaultIQ.Entity.ExpenseEntity;
import com.VaultIQ.Services.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDTO>addExpense(@RequestBody ExpenseDTO expenseDTO){
        ExpenseDTO savedDto = expenseService.addExpense(expenseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @GetMapping("/month")
    public ResponseEntity<List<ExpenseDTO>>getCurrentMonthExpenses(){
        List<ExpenseDTO>expenses = expenseService.getCurrentMonthExpenseOfCurrentUser();
        return ResponseEntity.ok(expenses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>deleteExpense(@PathVariable Long id){
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>>getAllExpense(){
        List<ExpenseDTO>getAllExpenseDetail = expenseService.getAllExpense();
        return ResponseEntity.ok(getAllExpenseDetail);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportIncomesToExcel() {
        byte[] excelData = expenseService.exportAllExpensesToExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expense.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @PostMapping("/send-email")
    public ResponseEntity<String> sendIncomeStatement() {
        expenseService.sendExpenseMail();
        return ResponseEntity.ok("Income statement email sent successfully.");
    }
}
