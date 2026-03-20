package com.VaultIQ.Controller;

import com.VaultIQ.DTO.ExpenseDTO;
import com.VaultIQ.DTO.FilterDTO;
import com.VaultIQ.DTO.IncomeDTO;
import com.VaultIQ.Services.ExpenseService;
import com.VaultIQ.Services.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1.0/filter")
public class FilterController {

    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private IncomeService incomeService;

    @PostMapping
    public ResponseEntity<?>filterTransaction(@RequestBody FilterDTO filterDTO){
        LocalDate startDate = filterDTO.getStartDate() != null ? filterDTO.getStartDate() : LocalDate.MIN;
        LocalDate endDate = filterDTO.getEndDate() != null ? filterDTO.getEndDate() : LocalDate.now();
        String keyword = filterDTO.getKeyword() != null ? filterDTO.getKeyword() : "";
        String sortFields = filterDTO.getSortField() != null ? filterDTO.getSortField() : "date" ;
        Sort.Direction direction = "desc".equalsIgnoreCase(filterDTO.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction , sortFields);

        if("income".equalsIgnoreCase(filterDTO.getType())){
            List<IncomeDTO> incomes =  incomeService.filterIncome(startDate, endDate , keyword , sort);
            return ResponseEntity.ok(incomes);
        }else if("expense".equalsIgnoreCase(filterDTO.getType())){
            List<ExpenseDTO> expenses = expenseService.filterExpense(startDate , endDate , keyword , sort);
            return ResponseEntity.ok(expenses);
        }else{
            return ResponseEntity.badRequest().body("Invalid Request , Type Must be 'Income' and 'Expense'");
        }
    }
}
