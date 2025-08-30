package com.opsara.sodagent.services;


import com.opsara.sodagent.entities.OrganisationChecklist;
import com.opsara.sodagent.entities.UserChecklistData;
import com.opsara.sodagent.entities.WhatsappUser;
import com.opsara.sodagent.repositories.OrganisationChecklistRepository;
import com.opsara.sodagent.repositories.UserChecklistDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import com.opsara.sodagent.repositories.WhatsappUserRepository;
import com.opsara.sodagent.entities.WhatsappUser;


@Service
public class SODAgentService {

    @Autowired
    private OrganisationChecklistRepository checklistRepository;

    @Autowired
    private UserChecklistDataRepository userChecklistDataRepository;

    @Autowired
    private WhatsappUserRepository whatsappUserRepository;

    @Transactional
    public OrganisationChecklist saveChecklist(Integer orgId, String checklistJson) {
        // Set all previous versions to inactive

        // Fetch all checklists for the org only once
        List<OrganisationChecklist> checklists = checklistRepository.findByOrgId(orgId);

        // Set all previous versions to inactive
        checklists.forEach(c -> {
            if (Boolean.TRUE.equals(c.getIsActive())) {
                c.setIsActive(false);
                checklistRepository.save(c);
            }
        });

// Get latest version number
        Integer maxVersion = checklists.stream()
                .map(OrganisationChecklist::getVersion)
                .max(Integer::compareTo)
                .orElse(0);

        OrganisationChecklist newChecklist = new OrganisationChecklist();
        newChecklist.setOrgId(orgId);
        newChecklist.setVersion(maxVersion + 1);
        newChecklist.setChecklistJson(checklistJson);
        newChecklist.setCreatedAt(LocalDateTime.now());
        newChecklist.setIsActive(true);

        return checklistRepository.save(newChecklist);
    }




    @Transactional
    public OrganisationChecklist fetchLatestActiveChecklist(Integer orgId) {
        List<OrganisationChecklist> checklists = checklistRepository
                .findByOrgIdAndIsActiveOrderByCreatedAtDesc(orgId, true);
        // 2. Take the latest record
        if (checklists.isEmpty()) {
            return null; // or throw an exception if preferred
        }
        OrganisationChecklist latestChecklist = checklists.get(0);
        return latestChecklist;
    }

    public UserChecklistData saveUserChecklistData(
            String userCredential,
            String userCredentialType,
            String filledForPeriod,
            Long organisationChecklistId,
            String dataJson
    ) {
        OrganisationChecklist organisationChecklist = checklistRepository.findById(organisationChecklistId)
                .orElseThrow(() -> new IllegalArgumentException("OrganisationChecklist not found"));

        UserChecklistData userChecklistData = new UserChecklistData();
        userChecklistData.setUserCredential(userCredential);
        userChecklistData.setUserCredentialType(userCredentialType);
        userChecklistData.setFilledForPeriod(filledForPeriod);
        userChecklistData.setOrganisationChecklist(organisationChecklist);
        userChecklistData.setDataJson(dataJson);
        userChecklistData.setCreatedAt(LocalDateTime.now());

        return userChecklistDataRepository.save(userChecklistData);
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


    @Transactional
    public WhatsappUser saveWhatsappUser(String mobileNumber, String whatsAppUserName) {
        if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Mobile number is required");
        }
        WhatsappUser user = new WhatsappUser();
        user.setMobileNumber(mobileNumber);
        user.setName(whatsAppUserName);
        user.setCreatedAt(LocalDateTime.now());
        return whatsappUserRepository.save(user);
    }

}



