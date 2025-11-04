package com.opsara.sodagent.services;


import com.opsara.sodagent.entities.RolloutUser;
import com.opsara.sodagent.repositories.RolloutUserRepository;
import com.opsara.sodagent.dto.ProblematicCheckpoint;
import com.opsara.sodagent.entities.OrganisationChecklist;
import com.opsara.sodagent.entities.UserChecklistData;
import com.opsara.sodagent.repositories.OrganisationChecklistRepository;
import com.opsara.sodagent.repositories.UserChecklistDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;


@Service
public class SODAgentService {

    private static final Logger logger = LoggerFactory.getLogger(SODAgentService.class);

    @Autowired
    private OrganisationChecklistRepository checklistRepository;

    @Autowired
    private UserChecklistDataRepository userChecklistDataRepository;

    @Autowired
    private RolloutUserRepository rolloutUserRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Transactional
    public OrganisationChecklist saveChecklist(Integer orgId, String checklistJson) {
        // Set all previous versions to inactive

        // Fetch all checklists for the org only once
        List<OrganisationChecklist> checklists = checklistRepository.findByOrgId(orgId);
        final Integer[] maxStatus = {2};
        // Set all previous versions to inactive
        checklists.forEach(c -> {
            if (Boolean.TRUE.equals(c.getIsActive())) {
                c.setIsActive(false);
                maxStatus[0] = c.getStatus() > maxStatus[0] ? c.getStatus() : maxStatus[0];
                checklistRepository.save(c);
            }
        });

        if (maxStatus[0] >= 3) {
            maxStatus[0] = 5; // If there was already a status 2, set new to 3
        }
// Get latest version number
        Integer maxVersion = checklists.stream().map(OrganisationChecklist::getVersion).max(Integer::compareTo).orElse(0);

        OrganisationChecklist newChecklist = new OrganisationChecklist();
        newChecklist.setOrgId(orgId);
        newChecklist.setVersion(maxVersion + 1);
        newChecklist.setChecklistJson(checklistJson);
        newChecklist.setCreatedAt(LocalDateTime.now());
        newChecklist.setStatus(maxStatus[0]);
        newChecklist.setIsActive(true);

        return checklistRepository.save(newChecklist);
    }


    @Transactional
    public OrganisationChecklist fetchLatestActiveChecklist(Integer orgId) {
        List<OrganisationChecklist> checklists = checklistRepository.findByOrgIdAndIsActiveOrderByCreatedAtDesc(orgId, true);
        // 2. Take the latest record
        if (checklists.isEmpty()) {
            return null; // or throw an exception if preferred
        }
        OrganisationChecklist latestChecklist = checklists.get(0);
        return latestChecklist;
    }

    public UserChecklistData saveUserChecklistData(String userCredential, String userCredentialType, String filledForPeriod, String storeId, Long organisationChecklistId, String dataJson) {
        OrganisationChecklist organisationChecklist = checklistRepository.findById(organisationChecklistId).orElseThrow(() -> new IllegalArgumentException("OrganisationChecklist not found"));


        var existingOpt = userChecklistDataRepository.findByUserCredentialAndUserCredentialTypeAndFilledForPeriodAndOrganisationChecklist_Id(userCredential, userCredentialType, filledForPeriod, organisationChecklistId);

        if (existingOpt.isPresent()) {
            UserChecklistData existing = existingOpt.get();
            existing.setDataJson(dataJson);
            return userChecklistDataRepository.save(existing);
        } else {
            logger.info("No existing UserChecklistData found. Creating new record.");
            UserChecklistData userChecklistData = new UserChecklistData();
            userChecklistData.setUserCredential(userCredential);
            userChecklistData.setUserCredentialType(userCredentialType);
            userChecklistData.setFilledForPeriod(filledForPeriod);
            userChecklistData.setOrganisationChecklist(organisationChecklist);
            userChecklistData.setDataJson(dataJson);
            userChecklistData.setCreatedAt(LocalDateTime.now());
            userChecklistData.setFilledForPeriodTs(LocalDateTime.parse(filledForPeriod, DateTimeFormatter.ofPattern("ddMMyyyy-HH:mm")));
            if(storeId != null && !storeId.isEmpty()) {
                try {
                    userChecklistData.setStoreId(Integer.parseInt(storeId));
                } catch (NumberFormatException e) {
                    logger.warn("Invalid storeId format: {}", storeId);
                }
            }

            return userChecklistDataRepository.save(userChecklistData);
        }

    }

    public boolean existsChecklistForOrganisation(Integer orgId) {
        return checklistRepository.existsByOrgId(orgId);
    }


    @Transactional
    public void markChecklistStatusAsTwo(Integer orgId) {
        checklistRepository.markStatusAsTwo(orgId);
    }

    public void saveChecklist(OrganisationChecklist checklist) {
        checklistRepository.save(checklist);
    }


    public List<ProblematicCheckpoint> giveMostProblematicCheckPoints(int orgId) {
        String sql = """
                SELECT
                  jsonb_data ->> 'question' AS question,
                  COUNT(*) AS no_count
                FROM
                  sodagent.user_checklist_data ucd
                  INNER JOIN sodagent.rollout_users wu
                    ON ucd.user_credential = wu.mobile_number
                  , jsonb_array_elements(ucd.data_json -> 'checklist_responses') AS jsonb_data
                WHERE
                  jsonb_data ->> 'answer' = 'No'
                  AND wu.organisation_id = ?
                GROUP BY
                  question
                ORDER BY
                  no_count DESC
                LIMIT 3
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ProblematicCheckpoint(rs.getString("question"), rs.getInt("no_count")), orgId);
    }


    public List<UserChecklistData> getUserChecklistDataBetweenDatesAndOrg(Integer organisationId, LocalDateTime fromDate, LocalDateTime toDate) {
        return userChecklistDataRepository.findByOrganisationChecklist_OrgIdAndFilledForPeriodTsBetween(organisationId, fromDate, toDate);
    }

    public List<String> getUserChecklistDataFilledForPeriodAndOrg(String today, Integer orgId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        LocalDateTime startOfDay = LocalDate.parse(today, formatter).atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<UserChecklistData> dataList = userChecklistDataRepository.findByOrganisationChecklist_OrgIdAndFilledForPeriodTsBetween(orgId, startOfDay, endOfDay);

        return dataList.stream().map(UserChecklistData::getUserCredential).toList();
    }


    public RolloutUser saveOrUpdateRolloutUser(String mobile, String name, Integer orgId) {

        if (mobile == null || mobile.trim().isEmpty()) {
            throw new IllegalArgumentException("Mobile number is required");
        }


        // I am consistantly saving mobile number as +2547xxxxxxx format in DB. So refining input to same format before searching.

        String refined = mobile.trim().replaceFirst("^0+", "");
        if (!refined.startsWith("+")) {
            refined = "+" + refined;
        }


        RolloutUser existingUser = rolloutUserRepository.findByMobileNumberAndOrgId(refined, orgId).orElse(null);

        if (existingUser != null) {
            // Just ignore. Do nothing
            logger.info("found a Roll out User record. Ignoring insert and returning existing record.");
            return existingUser;
        } else {
            RolloutUser rolloutUser = new RolloutUser();
            rolloutUser.setMobileNumber(mobile);
            rolloutUser.setName(name);
            rolloutUser.setOrgId(orgId);
            rolloutUser.setCreatedAt(LocalDateTime.now());
            return rolloutUserRepository.save(rolloutUser);
        }
    }

    public List<String> getAllRolloutUserCredentialsByOrgId(Integer organisationId) {
        List<RolloutUser> users = rolloutUserRepository.findByOrgId(organisationId);
        return users.stream().map(RolloutUser::getMobileNumber).toList();

    }

    public List<RolloutUser> getAllRolloutUsersByOrgId(Integer organisationId) {
        return rolloutUserRepository.findByOrgId(organisationId);
    }

    public List<RolloutUser> getAllRolloutUsersByMobileNUmber(String mobileNumber) {
        return rolloutUserRepository.findByMobileNumber(mobileNumber);
    }

    // java
    public List<UserChecklistData> fetchUserChecklistDataBetweenDates(String userCredentials, LocalDateTime fromDateTime, LocalDateTime toDateTime) {

        return userChecklistDataRepository.findByUserCredentialAndFilledForPeriodTsBetween(userCredentials, fromDateTime, toDateTime);

    }

    public Optional<UserChecklistData> findUserChecklistData(String userCredential, String userCredentialType, String filledForPeriod, Integer organisationId) {
        try {
            return userChecklistDataRepository.findFirstByUserCredentialAndUserCredentialTypeAndFilledForPeriodAndOrganisationChecklist_OrgIdOrderByOrganisationChecklist_VersionDescCreatedAtDesc(userCredential, userCredentialType, filledForPeriod, organisationId);
        } catch (Exception e) {
            logger.error("Error fetching UserChecklistData for userCredential={}, orgId={}", userCredential, organisationId, e);
            return Optional.empty();
        }
    }
}



