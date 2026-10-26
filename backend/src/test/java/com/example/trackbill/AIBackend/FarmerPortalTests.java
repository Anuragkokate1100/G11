package com.example.trackbill.AIBackend;

import com.example.trackbill.AIBackend.controller.FarmCropController;
import com.example.trackbill.AIBackend.controller.FarmerNotificationController;
import com.example.trackbill.AIBackend.controller.FarmerReportsController;
import com.example.trackbill.AIBackend.controller.FarmerTransactionController;
import com.example.trackbill.AIBackend.dto.*;
import com.example.trackbill.AIBackend.exception.BadRequestException;
import com.example.trackbill.AIBackend.model.Farmer;
import com.example.trackbill.AIBackend.model.FarmerNotification;
import com.example.trackbill.AIBackend.repository.FarmerNotificationRepository;
import com.example.trackbill.AIBackend.repository.FarmerRepository;
import com.example.trackbill.AIBackend.service.FarmerAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FarmerPortalTests {

    @Autowired
    private FarmerAuthService farmerAuthService;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private FarmerTransactionController transactionController;

    @Autowired
    private FarmCropController cropController;

    @Autowired
    private FarmerNotificationController notificationController;

    @Autowired
    private FarmerNotificationRepository notificationRepository;

    @Autowired
    private FarmerReportsController reportsController;

    private Farmer farmerA;
    private Farmer farmerB;
    private UsernamePasswordAuthenticationToken authA;
    private UsernamePasswordAuthenticationToken authB;

    @BeforeEach
    void setUp() {
        String unique = UUID.randomUUID().toString().substring(0, 8);

        FarmerRegisterRequest reqA = new FarmerRegisterRequest();
        reqA.setName("Ramesh Patil");
        reqA.setEmail("ramesh." + unique + "@farmer.in");
        reqA.setPhone("9822" + unique.substring(0, 6));
        reqA.setPassword("Ramesh@123");
        reqA.setVillage("Shirur");
        reqA.setTaluka("Shirur");
        reqA.setDistrict("Pune");
        reqA.setState("Maharashtra");
        reqA.setPincode("412210");

        farmerAuthService.register(reqA);
        farmerA = farmerRepository.findByEmail(reqA.getEmail().toLowerCase()).orElseThrow();
        authA = new UsernamePasswordAuthenticationToken(farmerA, null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_FARMER")));

        FarmerRegisterRequest reqB = new FarmerRegisterRequest();
        reqB.setName("Ganesh Shinde");
        reqB.setEmail("ganesh." + unique + "@farmer.in");
        reqB.setPhone("9833" + unique.substring(0, 6));
        reqB.setPassword("Ganesh@123");
        reqB.setVillage("Haveli");
        reqB.setTaluka("Haveli");
        reqB.setDistrict("Pune");
        reqB.setState("Maharashtra");
        reqB.setPincode("412201");

        farmerAuthService.register(reqB);
        farmerB = farmerRepository.findByEmail(reqB.getEmail().toLowerCase()).orElseThrow();
        authB = new UsernamePasswordAuthenticationToken(farmerB, null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_FARMER")));
    }

    @Test
    @DisplayName("Farmer Registration & Login Verification")
    void testFarmerAuth() {
        assertTrue(farmerA.getPassword().startsWith("$2a$") || farmerA.getPassword().startsWith("$2b$"));
        assertNotEquals("Ramesh@123", farmerA.getPassword());

        LoginRequest loginReq = new LoginRequest(farmerA.getEmail(), "Ramesh@123");
        FarmerAuthResponse loginRes = farmerAuthService.login(loginReq);
        assertTrue(loginRes.isSuccess());
        assertNotNull(loginRes.getToken());
        assertEquals("Ramesh Patil", loginRes.getProfile().getName());

        assertThrows(BadCredentialsException.class, () ->
                farmerAuthService.login(new LoginRequest(farmerA.getEmail(), "WrongPassword")));

        // Duplicate email
        FarmerRegisterRequest dupEmailReq = new FarmerRegisterRequest();
        dupEmailReq.setName("Duplicate Farmer");
        dupEmailReq.setEmail(farmerA.getEmail());
        dupEmailReq.setPhone("9999999999");
        dupEmailReq.setPassword("Password@123");
        dupEmailReq.setVillage("V");
        dupEmailReq.setTaluka("T");
        dupEmailReq.setDistrict("D");
        assertThrows(BadRequestException.class, () -> farmerAuthService.register(dupEmailReq));
    }

    @Test
    @DisplayName("Farmer Transactions: CRUD, Filtering, Summary & Isolation")
    void testFarmerTransactions() {
        // 1. Create Income Transaction
        FarmerTransactionRequest incomeReq = new FarmerTransactionRequest();
        incomeReq.setTransactionType("INCOME");
        incomeReq.setCategory("Crop Sale");
        incomeReq.setAmount(new BigDecimal("50000.00"));
        incomeReq.setTransactionDate(LocalDate.now());
        incomeReq.setDescription("Sugarcane harvest sold to mill");
        incomeReq.setFarmOrCrop("Sugarcane Plot 1");

        Map<String, Object> createdIncome = transactionController.create(incomeReq, authA);
        assertNotNull(createdIncome.get("id"));
        assertEquals("INCOME", createdIncome.get("transactionType"));
        assertEquals(new BigDecimal("50000.00"), createdIncome.get("amount"));
        Long incomeId = (Long) createdIncome.get("id");

        // 2. Create Expense Transaction
        FarmerTransactionRequest expReq = new FarmerTransactionRequest();
        expReq.setTransactionType("EXPENSE");
        expReq.setCategory("Fertilizer");
        expReq.setAmount(new BigDecimal("12000.00"));
        expReq.setTransactionDate(LocalDate.now());
        expReq.setDescription("Urea and DAP fertilizers");
        expReq.setFarmOrCrop("Sugarcane Plot 1");

        Map<String, Object> createdExpense = transactionController.create(expReq, authA);
        assertNotNull(createdExpense.get("id"));
        Long expenseId = (Long) createdExpense.get("id");

        // 3. List transactions for Farmer A
        List<Map<String, Object>> allA = transactionController.list(authA, null, null, null);
        assertEquals(2, allA.size());

        List<Map<String, Object>> incomeOnly = transactionController.list(authA, "INCOME", null, null);
        assertEquals(1, incomeOnly.size());
        assertEquals("Crop Sale", incomeOnly.get(0).get("category"));

        List<Map<String, Object>> expenseOnly = transactionController.list(authA, "EXPENSE", null, null);
        assertEquals(1, expenseOnly.size());

        // 4. Summary calculation
        Map<String, Object> summary = transactionController.summary(authA, null, null);
        assertEquals(new BigDecimal("50000.00"), summary.get("income"));
        assertEquals(new BigDecimal("12000.00"), summary.get("expenses"));
        assertEquals(new BigDecimal("38000.00"), summary.get("netBalance"));
        assertEquals(2, summary.get("count"));

        // 5. Update transaction
        FarmerTransactionRequest updateReq = new FarmerTransactionRequest();
        updateReq.setTransactionType("EXPENSE");
        updateReq.setCategory("Fertilizer & Pesticide");
        updateReq.setAmount(new BigDecimal("15000.00"));
        updateReq.setTransactionDate(LocalDate.now());
        updateReq.setDescription("Updated fertilizers and spray");
        updateReq.setFarmOrCrop("Sugarcane Plot 1");

        Map<String, Object> updated = transactionController.update(expenseId, updateReq, authA);
        assertEquals(new BigDecimal("15000.00"), updated.get("amount"));
        assertEquals("Fertilizer & Pesticide", updated.get("category"));

        // 6. Farmer B Isolation Check: Farmer B cannot see or manipulate Farmer A's transactions
        List<Map<String, Object>> allB = transactionController.list(authB, null, null, null);
        assertTrue(allB.isEmpty());

        assertThrows(ResponseStatusException.class, () ->
                transactionController.update(incomeId, updateReq, authB));

        assertThrows(ResponseStatusException.class, () ->
                transactionController.delete(incomeId, authB));

        // 7. Delete transaction by Farmer A
        transactionController.delete(expenseId, authA);
        List<Map<String, Object>> afterDelete = transactionController.list(authA, null, null, null);
        assertEquals(1, afterDelete.size());
    }

    @Test
    @DisplayName("Farmer Crop Management & Isolation")
    void testFarmerCrops() {
        FarmCropRequest req = new FarmCropRequest();
        req.setCropName("Sugarcane (Co 86032)");
        req.setLandArea(new BigDecimal("5.50"));
        req.setAreaUnit("acre");
        req.setSowingDate(LocalDate.now().minusMonths(3));
        req.setExpectedHarvestDate(LocalDate.now().plusMonths(9));
        req.setStatus("GROWING");

        FarmCropResponse created = cropController.create(req, authA).getBody().getData();
        assertNotNull(created.getId());
        assertEquals("Sugarcane (Co 86032)", created.getCropName());
        assertEquals(new BigDecimal("5.50"), created.getLandArea());

        List<FarmCropResponse> cropsA = cropController.list(authA).getBody().getData();
        assertEquals(1, cropsA.size());

        // Isolation: Farmer B has no crops and cannot access Farmer A's crop
        List<FarmCropResponse> cropsB = cropController.list(authB).getBody().getData();
        assertTrue(cropsB.isEmpty());

        assertThrows(ResponseStatusException.class, () ->
                cropController.delete(created.getId(), authB));

        cropController.delete(created.getId(), authA);
        assertTrue(cropController.list(authA).getBody().getData().isEmpty());
    }

    @Test
    @DisplayName("Farmer Notifications: Unread Count & Mark Read")
    void testFarmerNotifications() {
        FarmerNotification n1 = new FarmerNotification();
        n1.setFarmer(farmerA);
        n1.setTitle("Test Notification 1");
        n1.setMessage("Message 1");
        n1.setEventType("TEST");
        notificationRepository.save(n1);

        FarmerNotification n2 = new FarmerNotification();
        n2.setFarmer(farmerA);
        n2.setTitle("Test Notification 2");
        n2.setMessage("Message 2");
        n2.setEventType("TEST");
        notificationRepository.save(n2);

        Map<String, Object> listRes = notificationController.list(authA);
        assertEquals(2L, listRes.get("unreadCount"));

        notificationController.markRead(n1.getId(), authA);
        Map<String, Object> listAfterOne = notificationController.list(authA);
        assertEquals(1L, listAfterOne.get("unreadCount"));

        notificationController.markAllRead(authA);
        Map<String, Object> listAfterAll = notificationController.list(authA);
        assertEquals(0L, listAfterAll.get("unreadCount"));
    }

    @Test
    @DisplayName("Farmer Reports: Monthly Report Calculation")
    void testFarmerReports() {
        FarmerTransactionRequest inc = new FarmerTransactionRequest();
        inc.setTransactionType("INCOME");
        inc.setCategory("Dairy");
        inc.setAmount(new BigDecimal("20000.00"));
        inc.setTransactionDate(LocalDate.now());
        transactionController.create(inc, authA);

        FarmerTransactionRequest exp = new FarmerTransactionRequest();
        exp.setTransactionType("EXPENSE");
        exp.setCategory("Fodder");
        exp.setAmount(new BigDecimal("5000.00"));
        exp.setTransactionDate(LocalDate.now());
        transactionController.create(exp, authA);

        Map<String, Object> monthly = reportsController.monthly(authA, null, null);
        assertNotNull(monthly.get("monthly"));
        assertEquals(new BigDecimal("20000.00"), monthly.get("income"));
        assertEquals(new BigDecimal("5000.00"), monthly.get("expenses"));
        assertEquals(new BigDecimal("15000.00"), monthly.get("netBalance"));
    }
}
