package com.example.ADAS_App;

import com.example.ADAS_App.entity.*;
import com.example.ADAS_App.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
/*
@Component
public class TestDataLoader implements CommandLineRunner {

    private final CompanyRepository companyRepo;
    private final AdminRepository adminRepo;
    private final CompanyDriverRepository companyDriverRepo;
    private final IndividualDriverRepository individualDriverRepo;
    private final EmergencyContactRepository emergencyContactRepo;
    private final AlertRepository alertRepo;
    private final EmotionRecordRepository emotionRecordRepo;
    private final SessionRepository sessionRepo;

    public TestDataLoader(CompanyRepository companyRepo, AdminRepository adminRepo,
                          CompanyDriverRepository companyDriverRepo, IndividualDriverRepository individualDriverRepo , EmergencyContactRepository emergencyContactRepo , AlertRepository alertRepo, EmotionRecordRepository emotionRecordRepo, SessionRepository sessionRepo) {
        this.companyRepo = companyRepo;
        this.adminRepo = adminRepo;
        this.companyDriverRepo = companyDriverRepo;
        this.individualDriverRepo = individualDriverRepo;
        this.emergencyContactRepo = emergencyContactRepo;
        this.emotionRecordRepo = emotionRecordRepo;
        this.alertRepo = alertRepo;
        this.sessionRepo = sessionRepo;
    }

    @Override
    public void run(String... args) {
        alertRepo.deleteAll();             // depends on users
        emotionRecordRepo.deleteAll();     // depends on users (drivers)
        sessionRepo.deleteAll();           // depends on users
        emergencyContactRepo.deleteAll();  // depends on individual drivers

           // depends on companies and users
        individualDriverRepo.deleteAll();  // depends on users
        adminRepo.deleteAll();             // depends on companies and users

        companyRepo.deleteAll();           // parent of company drivers and admins

// Finally delete base users if you have a generic User repository
// userRepo.deleteAll(); // only if you maintain a separate generic userRepo

        // Create a Company
        Company company = new Company();
        company.setName("FastLogistics");
        company.setIndustry("Transport");
        company.setAddress("123 Main Street");
        companyRepo.save(company);

        // Password encoder
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Create an Admin
        Admin admin = new Admin();
        admin.setName("Alice Johnson");
        admin.setEmail("admin@fastlogistics.com");
        admin.setPassword(encoder.encode("password123"));
        admin.setRole(Role.ADMIN);
        admin.setCompany(company);
        adminRepo.save(admin);

        // Create a Company Driver


        // Create an Individual Driver
        IndividualDriver driver2 = new IndividualDriver();
        driver2.setName("Emma Davis");
        driver2.setEmail("emma@example.com");
        driver2.setPassword(encoder.encode("driver456"));
        driver2.setRole(Role.DRIVER_INDEPENDENT);
        driver2.setLicenseNumber("DL-654321");
        driver2.setVehicleId("V-IND-01");
        individualDriverRepo.save(driver2);

        IndividualDriver driver3 = new IndividualDriver();
        driver3.setName("Honda Rachid");
        driver3.setEmail("honda@example.com");
        driver3.setPassword(encoder.encode("driver4567"));
        driver3.setRole(Role.DRIVER_INDEPENDENT);
        driver3.setLicenseNumber("DL-654351");
        driver3.setVehicleId("V-IND-21");
        individualDriverRepo.save(driver3);

        // Create Emergency Contacts
        EmergencyContact contact1 = new EmergencyContact();
        contact1.setName("David Smith");
        contact1.setPhone("+212600111222");
        contact1.setRelation("Brother");
        contact1.setDriver(driver2);

        EmergencyContact contact2 = new EmergencyContact();
        contact2.setName("Sarah Johnson");
        contact2.setPhone("+212600333444");
        contact2.setRelation("Friend");
        contact2.setDriver(driver2);

// Add contacts to driver
        driver2.setContacts(Arrays.asList(contact1, contact2));

// Save driver with contacts
        individualDriverRepo.save(driver2);

        // === Create a session for individual driver ===
        Session session = new Session();
        session.setDriver(driver2);
        session.setStartTime(LocalDateTime.now().minusHours(2));
        session.setEndTime(LocalDateTime.now());
        session.setAverageEmotion("Fatigue");
        sessionRepo.save(session);

// === Create an alert ===
        Alert alert = new Alert();
        alert.setDriver(driver2);
        alert.setType("Fatigue");
        alert.setMessage("Driver shows signs of fatigue. Immediate action recommended.");
        alertRepo.save(alert);

        // After saving drivers and contacts

        EmotionRecord record1 = new EmotionRecord();
        record1.setSession(session);
        record1.setEmotions(List.of("fatigue","sad","neutral"));
        record1.setConfidences(List.of(0.8,0.1,0.1));
        record1.setDetectedAt(LocalDateTime.now().minusHours(1));


        EmotionRecord record2 = new EmotionRecord();
        record2.setSession(session);
        record2.setEmotions(List.of("fatigue","sad","neutral"));
        record2.setConfidences(List.of(0.8,0.1,0.1));
        record2.setDetectedAt(LocalDateTime.now());


        emotionRecordRepo.saveAll(Arrays.asList(record1, record2));


        System.out.println("✅ Sample data inserted successfully!");
    }
}*/