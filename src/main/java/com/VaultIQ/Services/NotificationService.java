package com.VaultIQ.Services;


import com.VaultIQ.DTO.ExpenseDTO;
import com.VaultIQ.Entity.ProfileEntity;
import com.VaultIQ.Repository.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@Slf4j
public class NotificationService {

    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ExpenseService expenseService;

    @Value("${vaultiq.frontend.url}")
    private String frontedUrl;

//    @Scheduled(cron = "0 * * * * *" , zone = "IST")
    @Scheduled(cron = "0 0 22 * * *" , zone = "IST")
    public Void sendDailyIncomeExpenseReminder(){
//        log.info("Job started : sendDailyIncomeExpenseReminder()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for(ProfileEntity profile :  profiles){
//            System.out.println(profile.getEmail());
            String body = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                    ".container { background-color: #ffffff; max-width: 600px; margin: 40px auto; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }" +
                    ".greeting { font-size: 18px; color: #333333; }" +
                    ".message { font-size: 16px; color: #555555; margin-top: 20px; }" +
                    ".cta { margin-top: 30px; display: inline-block; background-color: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; }" +
                    ".footer { font-size: 12px; color: #999999; margin-top: 30px; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "<div class='greeting'>Hi " + profile.getFullname() + ",</div>" +
                    "<div class='message'>" +
                    "This is your friendly reminder to log your income and expenses for today in <strong>VaultIQ</strong>. Keeping track daily helps you build financial clarity and control!" +
                    "</div>" +
                    "<a href='" + frontedUrl + "' class='cta'>Add Income & Expenses</a>" +
                    "<div class='footer'>Thank you for using VaultIQ 💰</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
            emailService.sendMail(profile.getEmail() , "Friendly Reminder: Track today's income & expenses in VaultIQ" , body);
        }
        return null;
    }

//    @Scheduled(cron = "0 * * * * *", zone = "Asia/Kolkata")
    @Scheduled(cron = "0 0 20 * * *", zone = "Asia/Kolkata")
    public Void sendDailyIncomeExpenseSummary() {
//        log.info("Job started : sendDailyIncomeExpenseSummary()");
        List<ProfileEntity> profiles = profileRepository.findAll();

        for (ProfileEntity profile : profiles) {
            List<ExpenseDTO> todayExpense = expenseService.getExpensesForUserOnDate(profile.getId(), LocalDate.now());

            if (!todayExpense.isEmpty()) {
                StringBuilder table = new StringBuilder();

                table.append("<table style='font-family: Arial, sans-serif; border-collapse: collapse; width: 100%; max-width: 600px;'>");
                table.append("<caption style='font-size: 18px; margin-bottom: 10px; font-weight: bold;'>Today's Expense Summary</caption>");
                table.append("<tr>")
                        .append("<th style='border: 1px solid #ddd; padding: 8px; background-color: #4CAF50; color: white;'>Sr.No</th>")
                        .append("<th style='border: 1px solid #ddd; padding: 8px; background-color: #4CAF50; color: white;'>Name</th>")
                        .append("<th style='border: 1px solid #ddd; padding: 8px; background-color: #4CAF50; color: white;'>Amount</th>")
                        .append("<th style='border: 1px solid #ddd; padding: 8px; background-color: #4CAF50; color: white;'>Category</th>")
                        .append("</tr>");

                int i = 1;
                for (ExpenseDTO expenseDTO : todayExpense) {
                    table.append("<tr style='background-color:").append(i % 2 == 0 ? "#f2f2f2" : "#ffffff").append(";'>");
                    table.append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(i++).append("</td>");
                    table.append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(expenseDTO.getName()).append("</td>");
                    table.append("<td style='border: 1px solid #ddd; padding: 8px;'>₹ ").append(expenseDTO.getAmount()).append("</td>");
                    table.append("<td style='border: 1px solid #ddd; padding: 8px;'>")
                            .append(expenseDTO.getCategoryId() != null ? expenseDTO.getCategoryName() : "N/A").append("</td>");
                    table.append("</tr>");
                }

                table.append("</table>");

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d-MMMM-yyyy");
                String formattedDate = LocalDate.now().format(dateFormatter);

                String body = """
                <div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; padding: 20px;'>
                    <h2 style='color: #2E8B57;'>Hello %s,</h2>
                    <p>Here's your <strong>expense summary</strong> for today <strong>%s</strong>:</p>
                    %s
                    <p style='margin-top: 20px;'>Keep tracking and managing your finances smartly with <strong>VaultIQ</strong>!</p>
                    <p>Best regards,<br><strong>VaultIQ Team</strong></p>
                </div>
                """.formatted(profile.getFullname(), formattedDate, table);

                emailService.sendMail(profile.getEmail(), "Your Daily Expense Summary – VaultIQ", body);
            }
        }
        return null;
    }


}
