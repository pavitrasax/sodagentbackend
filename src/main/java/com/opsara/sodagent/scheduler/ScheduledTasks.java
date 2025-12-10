// src/main/java/com/opsara/sodagent/scheduler/WhatsAppScheduler.java
package com.opsara.sodagent.scheduler;


import com.opsara.aaaservice.util.MSG91WhatsappUtil;
import com.opsara.aaaservice.util.URLGenerationUtil;
import com.opsara.sodagent.entities.RolloutUser;
import com.opsara.aaaservice.repositories.OrganisationRepository;
import com.opsara.sodagent.repositories.RolloutUserRepository;
import com.opsara.aaaservice.entities.Organisation;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    private final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private final OrganisationRepository organisationRepository;
    private final RolloutUserRepository rolloutUserRepository;
    private final URLGenerationUtil urlGenerationUtil;
    private final MSG91WhatsappUtil msg91WhatsappUtil;

    @PostConstruct
    public void init() {
        log.info("WhatsAppScheduler initialized. job=10:00 AM IST (Asia/Kolkata)");
    }

    // runs daily at 10:00 AM IST
    @Scheduled(cron = "0 0 10 * * ?", zone = "Asia/Kolkata")
    // for testing purpose, runs every minute
    // @Scheduled(cron = "0 */1 * * * ?", zone = "Asia/Kolkata")
    public void sendRemindersAt10amIst() {
        log.info("Scheduled job started: sendRemindersAt10amIst");
        List<Organisation> orgs = organisationRepository.findBySendWhatsappRemindersTrue();
        for (Organisation org : orgs) {
            Integer orgId = org.getId();
            try {
                List<RolloutUser> users = rolloutUserRepository.findByOrgId(orgId);
                for (RolloutUser user : users) {
                    try {
                        // TODO send message here
                        log.info("Sending whatsapp message to user id={} orgId={}", user.getMobileNumber(), orgId);

                        String sodaChecklistUrl = "fillsodchecklist?hashtoken=";
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy-00:00");
                        LocalDate today = LocalDate.now();
                        String dateString = today.format(formatter);

                        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
                        String formattedDate = today.format(outputFormatter);

                        String hash = "";
                        String mobile = user.getMobileNumber();
                        try {
                            hash = urlGenerationUtil.generateHash(mobile, "mobile", dateString, String.valueOf(orgId));
                        } catch (Exception e) {
                            //throw new RuntimeException(e);
                        }

                        String whatsappUtilResponse = msg91WhatsappUtil.sendGenericFillFormMessageNewTemplate(mobile,"User","sod form",formattedDate,sodaChecklistUrl+hash,user.getOrgId());

                    } catch (Exception ex) {
                        log.error("Failed to send WhatsApp to user id={} orgId={}", user.getMobileNumber(), orgId, ex);
                    }
                }
            } catch (Exception ex) {
                log.error("Failed to fetch or process users for orgId={}", orgId, ex);
            }
        }
        log.info("Scheduled job finished: sendRemindersAt10amIst");
    }
}