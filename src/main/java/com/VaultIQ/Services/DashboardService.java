package com.VaultIQ.Services;

import com.VaultIQ.DTO.ExpenseDTO;
import com.VaultIQ.DTO.IncomeDTO;
import com.VaultIQ.DTO.RecentTransactionDTO;
import com.VaultIQ.Entity.ProfileEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class DashboardService {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private IncomeService incomeService;

//    @Cacheable(value = "dashboardCache", key = "#profileId")
    public Map<String , Object> getDashboardData(Long profileId){

        log.info("Redis cache miss, Fetching from database");
        Map<String , Object> returnValue = new LinkedHashMap<>();

        List<IncomeDTO> latestIncomes =
                incomeService.getLatest5IncomeForCurrentUser();

        List<ExpenseDTO> latestExpenses =
                expenseService.getLatest5ExpenseForCurrentUser();

        List<RecentTransactionDTO> recentTransactionList = Stream.concat(
                latestIncomes.stream().map(income ->
                        RecentTransactionDTO.builder()
                                .id(income.getId())
                                .profileId(profileId)
                                .icon(income.getIcon())
                                .name(income.getName())
                                .amount(income.getAmount())
                                .date(income.getDate())
                                .createdAt(income.getCreatedAt())
                                .updatedAt(income.getUpdatedAt())
                                .type("income")
                                .build()),
                latestExpenses.stream().map(expense ->
                        RecentTransactionDTO.builder()
                                .id(expense.getId())
                                .profileId(profileId)
                                .icon(expense.getIcon())
                                .name(expense.getName())
                                .amount(expense.getAmount())
                                .date(expense.getDate())
                                .createdAt(expense.getCreatedAt())
                                .updatedAt(expense.getUpdatedAt())
                                .type("expense")
                                .build())
        ).sorted((a, b) -> {
            int cmp = b.getDate().compareTo(a.getDate());
            if (cmp == 0 && a.getCreatedAt() != null && b.getCreatedAt() != null) {
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            }
            return cmp;
        }).collect(Collectors.toList());

        returnValue.put("totalBalance",
                incomeService.getTotalIncomeForCurrentUser()
                        .subtract(expenseService.getTotalExpenseForCurrentUser()));

        returnValue.put("totalIncome",
                incomeService.getTotalIncomeForCurrentUser());

        returnValue.put("totalExpense",
                expenseService.getTotalExpenseForCurrentUser());

        returnValue.put("recent5Income", latestIncomes);
        returnValue.put("recent5Expense", latestExpenses);
        returnValue.put("recentTransactions", recentTransactionList);

        return returnValue;
    }
}