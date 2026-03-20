package com.VaultIQ.Controller;

import com.VaultIQ.DTO.ExpenseDTO;
import com.VaultIQ.DTO.IncomeDTO;
import com.VaultIQ.Services.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/incomes")
public class IncomeController {

    @Autowired
    private IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeDTO> addIncome(@RequestBody IncomeDTO incomeDTO){
        IncomeDTO savedDto = incomeService.addIncome(incomeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @GetMapping("/month")
    public ResponseEntity<List<IncomeDTO>>getCurrentMonthIncomes(){
        List<IncomeDTO>incomes = incomeService.getCurrentMonthIncomeOfCurrentUser();
        return ResponseEntity.ok(incomes);
    }

    @GetMapping
    public ResponseEntity<List<IncomeDTO>>getAllIncomeDetails(){
        List<IncomeDTO>incomes = incomeService.getAllIncome();
        return ResponseEntity.ok(incomes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>deleteIncome(@PathVariable Long id){
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportIncomesToExcel() {
        byte[] excelData = incomeService.exportAllIncomesToExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=incomes.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @PostMapping("/send-email")
    public ResponseEntity<String> sendIncomeStatement() {
        incomeService.sendIncomeMail();
        return ResponseEntity.ok("Income statement email sent successfully.");
    }
}
