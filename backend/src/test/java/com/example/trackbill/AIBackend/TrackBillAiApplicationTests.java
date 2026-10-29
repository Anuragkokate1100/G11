package com.example.trackbill.AIBackend;

import com.example.trackbill.AIBackend.dto.*;
import com.example.trackbill.AIBackend.exception.BadRequestException;
import com.example.trackbill.AIBackend.model.*;
import com.example.trackbill.AIBackend.repository.TractorOwnerRepository;
import com.example.trackbill.AIBackend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TrackBillAiApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private LabourService labourService;

    @Autowired
    private TripService tripService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private BillingService billingService;

    @Autowired
    private LabourPaymentService labourPaymentService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private InvoiceEmailService invoiceEmailService;

    @Autowired
    private TractorOwnerRepository ownerRepository;

    private TractorOwner ownerA;
    private TractorOwner ownerB;

    @BeforeEach
    void setUp() {
        String unique = UUID.randomUUID().toString().substring(0, 8);

        // Register Owner A
        RegisterRequest reqA = new RegisterRequest(
                "Sanjay Patil",
                "MH 12 SP 1111",
                2,
                "sanjay." + unique + "@trackbill.in",
                "Sanjay@123",
                "Baramati",
                "Baramati",
                "Pune",
                "413102"
        );
        AuthResponse resA = authService.register(reqA);
        ownerA = ownerRepository.findById(resA.getOwner().getId()).orElseThrow();

        // Register Owner B (for isolation tests)
        RegisterRequest reqB = new RegisterRequest(
                "Vikas Deshmukh",
                "MH 14 VD 2222",
                1,
                "vikas." + unique + "@trackbill.in",
                "Vikas@123",
                "Indapur",
                "Indapur",
                "Pune",
                "413106"
        );
        AuthResponse resB = authService.register(reqB);
        ownerB = ownerRepository.findById(resB.getOwner().getId()).orElseThrow();
    }

    private void authenticateAs(TractorOwner owner) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                owner,
                null,
                Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Step 2 Test: Registration, BCrypt Hashing, No Password Leak & Duplicate Email")
    void testRegistrationAndAuthentication() {
        // 1. Password must be hashed with BCrypt
        assertTrue(ownerA.getPassword().startsWith("$2a$") || ownerA.getPassword().startsWith("$2b$"));
        assertNotEquals("Sanjay@123", ownerA.getPassword());

        // 2. Login verification
        LoginRequest loginReq = new LoginRequest(ownerA.getEmail(), "Sanjay@123");
        AuthResponse loginRes = authService.login(loginReq);
        assertTrue(loginRes.isSuccess());
        assertNotNull(loginRes.getToken());
        assertEquals("Sanjay Patil", loginRes.getOwner().getName());

        // 3. Invalid credentials must throw
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(new LoginRequest(ownerA.getEmail(), "WrongPassword"));
        });

        // 4. Duplicate email registration must be rejected
        RegisterRequest dupReq = new RegisterRequest(
                "Another Sanjay",
                "MH 12 AS 9999",
                1,
                ownerA.getEmail(), // same email
                "Password123",
                "Baramati",
                "Baramati",
                "Pune",
                "413102"
        );
        assertThrows(BadRequestException.class, () -> authService.register(dupReq));
    }

    @Test
    @DisplayName("Step 3 Test: Labour CRUD, Search & Owner Isolation")
    void testLabourModule() {
        authenticateAs(ownerA);

        // Create Labour for Owner A
        LabourRequest labReq1 = new LabourRequest(
                "Dnyaneshwar Shinde",
                "Shirur",
                LabourCategory.FULL_KOYTA,
                "9876543210",
                new BigDecimal("5000.00"),
                LabourType.LABOUR
        );
        LabourResponse lab1 = labourService.createLabour(labReq1);
        assertEquals("Dnyaneshwar Shinde", lab1.getName());
        assertEquals(LabourCategory.FULL_KOYTA, lab1.getCategory());

        // Search Labour
        List<LabourResponse> searchResults = labourService.searchLabour("Shinde");
        assertEquals(1, searchResults.size());

        // Authenticate as Owner B -> Owner B must NOT see Owner A's labour
        authenticateAs(ownerB);
        List<LabourResponse> ownerBLabour = labourService.getAllLabour();
        assertTrue(ownerBLabour.isEmpty());

        // Owner B cannot delete Owner A's labour
        assertThrows(Exception.class, () -> labourService.deleteLabour(lab1.getId()));
    }

    @Test
    @DisplayName("Step 4 Test: Trip CRUD, Validation, Default Tractor Number & Date Filter")
    void testTripModule() {
        authenticateAs(ownerA);

        // Create Trip without tractor number -> must default to owner's registered tractor
        TripRequest tripReq = new TripRequest(
                "Ramesh Jadhav",
                new BigDecimal("32.50"),
                new BigDecimal("15.00"),
                "Sahyadri SSK",
                LocalDate.now(),
                null // empty tractor number
        );
        TripResponse trip = tripService.createTrip(tripReq);
        assertEquals("MH 12 SP 1111", trip.getTractorNumber());
        assertEquals("Ramesh Jadhav", trip.getSugarcaneOwnerName());
        assertFalse(trip.getIsBilled());

        // Search trips
        List<TripResponse> search = tripService.searchTrips("Sahyadri");
        assertEquals(1, search.size());

        // Owner B isolation
        authenticateAs(ownerB);
        List<TripResponse> ownerBTrips = tripService.getAllTrips();
        assertTrue(ownerBTrips.isEmpty());
    }

    @Test
    @DisplayName("Step 5 Test: Expense Module CRUD & Categories")
    void testExpenseModule() {
        authenticateAs(ownerA);

        ExpenseRequest expReq = new ExpenseRequest(
                ExpenseType.DIESEL,
                new BigDecimal("2500.00"),
                LocalDate.now(),
                "25 Liters Diesel"
        );
        ExpenseResponse exp = expenseService.createExpense(expReq);
        assertEquals(ExpenseType.DIESEL, exp.getExpenseType());
        assertEquals(new BigDecimal("2500.00"), exp.getAmount());

        List<ExpenseResponse> list = expenseService.getAllExpenses();
        assertEquals(1, list.size());

        // Owner B isolation
        authenticateAs(ownerB);
        assertTrue(expenseService.getAllExpenses().isEmpty());
    }

    @Test
    @DisplayName("Step 6 Test: Billing Calculation (Trip 1 = 55.4 tons, Trip 2 = 44.6 tons -> Total 100 tons * ₹350 = ₹35,000) & Invalid Cases")
    void testBillingCalculation() {
        authenticateAs(ownerA);

        // Create 2 trips: 55.4 tons + 44.6 tons = 100 tons
        TripResponse t1 = tripService.createTrip(new TripRequest("Dattatray Shinde", new BigDecimal("55.400"), new BigDecimal("18.50"), "Shri Datta SSK", LocalDate.now(), null));
        TripResponse t2 = tripService.createTrip(new TripRequest("Hanumant Pawar", new BigDecimal("44.600"), new BigDecimal("15.00"), "Shri Datta SSK", LocalDate.now(), null));

        // Create Bill with Rate Per Ton = 350.00
        BillRequest billReq = new BillRequest(
                List.of(t1.getId(), t2.getId()),
                null,
                new BigDecimal("350.00"),
                LocalDate.now(),
                "Combined Sugarcane Bill"
        );
        BillResponse bill = billingService.createBill(billReq);

        // Assert backend calculated total amount: 100 * 350 = 35000.00
        assertEquals(0, new BigDecimal("100.000").compareTo(bill.getTotalWeight()));
        assertEquals(new BigDecimal("350.00"), bill.getRatePerTon());
        assertEquals(new BigDecimal("35000.00"), bill.getTotalAmount());
        assertEquals(2, bill.getTripCount());
        assertNotNull(bill.getOwnerName());
        assertEquals(ownerA.getName(), bill.getOwnerName());

        // Verify that trips are now marked as billed
        TripResponse updatedT1 = tripService.getTripById(t1.getId());
        assertTrue(updatedT1.getIsBilled());
        assertEquals(bill.getId(), updatedT1.getBillingId());

        // Also test getAllBills() and verify owner metadata is present without LazyInitializationException
        List<BillResponse> allBills = billingService.getAllBills();
        assertFalse(allBills.isEmpty());
        assertEquals(ownerA.getName(), allBills.get(0).getOwnerName());

        // --- TEST INVALID CASES ---

        // 1. Missing rate (null)
        assertThrows(BadRequestException.class, () -> {
            billingService.createBill(new BillRequest(List.of(t1.getId()), null, null, LocalDate.now(), "Test"));
        });

        // 2. Negative rate
        assertThrows(BadRequestException.class, () -> {
            billingService.createBill(new BillRequest(List.of(t1.getId()), null, new BigDecimal("-350.00"), LocalDate.now(), "Test"));
        });

        // 3. Zero rate
        assertThrows(BadRequestException.class, () -> {
            billingService.createBill(new BillRequest(List.of(t1.getId()), null, BigDecimal.ZERO, LocalDate.now(), "Test"));
        });

        // 4. No trips and no manual weight
        assertThrows(BadRequestException.class, () -> {
            billingService.createBill(new BillRequest(Collections.emptyList(), null, new BigDecimal("350.00"), LocalDate.now(), "Test"));
        });

        // 5. Invalid trip ID (does not exist)
        assertThrows(BadRequestException.class, () -> {
            billingService.createBill(new BillRequest(List.of(999999L), null, new BigDecimal("350.00"), LocalDate.now(), "Test"));
        });

        // 6. Another owner's trip (cross-owner trip attempt)
        authenticateAs(ownerB);
        TripResponse tripOwnerB = tripService.createTrip(new TripRequest("Farmer B", new BigDecimal("30.00"), BigDecimal.ZERO, "Mill B", LocalDate.now(), null));

        authenticateAs(ownerA);
        // Owner A attempts to bill Owner B's trip: must be rejected!
        assertThrows(BadRequestException.class, () -> {
            billingService.createBill(new BillRequest(List.of(tripOwnerB.getId()), null, new BigDecimal("350.00"), LocalDate.now(), "Cross Owner Attempt"));
        });

        // 7. Duplicate billing of already billed trip (t1 is already billed)
        assertThrows(BadRequestException.class, () -> {
            billingService.createBill(new BillRequest(List.of(t1.getId()), null, new BigDecimal("350.00"), LocalDate.now(), "Duplicate Attempt"));
        });
    }

    @Test
    @DisplayName("Step 7 Test: Labour Payment Calculation (100 Tons, 9 Full Koyta, 1 Half Koyta)")
    void testKoytaLabourCalculationAlgorithm() {
        authenticateAs(ownerA);

        // Create 9 Full Koyta and 1 Half Koyta workers
        List<Long> workerIds = new ArrayList<>();

        for (int i = 1; i <= 9; i++) {
            LabourResponse full = labourService.createLabour(new LabourRequest(
                    "Full Worker " + i,
                    "Village " + i,
                    LabourCategory.FULL_KOYTA,
                    "900000000" + i,
                    new BigDecimal("1000.00"),
                    LabourType.LABOUR
            ));
            workerIds.add(full.getId());
        }

        LabourResponse half = labourService.createLabour(new LabourRequest(
                "Half Worker 1",
                "Village 10",
                LabourCategory.HALF_KOYTA,
                "9000000010",
                new BigDecimal("500.00"),
                LabourType.LABOUR
        ));
        workerIds.add(half.getId());

        // Run calculation: 100 tons at ₹350/ton
        LabourPaymentCalculationRequest req = new LabourPaymentCalculationRequest(
                new BigDecimal("100.00"),
                new BigDecimal("350.00"),
                workerIds,
                LocalDate.now(),
                null
        );

        LabourPaymentCalculationResponse res = labourPaymentService.calculatePayments(req);

        // Verified Mathematical Rule (Section 19):
        // Total workers = 10
        // Base share = 100 / 10 = 10 tons
        // Half Koyta gets 0.5 * 10 = 5.0 tons
        // Full Koyta initial = 10 tons
        // Remaining 5 tons redistributed among 9 Full = 5/9 = ~0.5556 tons
        // Final Full Koyta = 10 + 0.556 = ~10.556 tons
        assertEquals(10, res.getTotalWorkers());
        assertEquals(9, res.getFullKoytaCount());
        assertEquals(1, res.getHalfKoytaCount());
        assertEquals(new BigDecimal("10.000"), res.getBaseSharePerWorker());
        assertEquals(new BigDecimal("5.000"), res.getHalfKoytaShareWeight());
        assertEquals(new BigDecimal("10.556"), res.getFullKoytaShareWeight());

        // Sum check: 9 * 10.556 + 1 * 5.000 = 95.004 + 5.000 = 100.004 (rounded to 100.00)
        BigDecimal sumAssigned = res.getItems().stream()
                .map(LabourPaymentItemDto::getAssignedWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("100.004"), sumAssigned);

        // Check advance deduction
        for (LabourPaymentItemDto item : res.getItems()) {
            assertTrue(item.getGrossAmount().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(item.getAdvanceDeducted().compareTo(BigDecimal.ZERO) > 0);
            assertEquals(item.getGrossAmount().subtract(item.getAdvanceDeducted()), item.getFinalAmount());
        }
    }

    @Test
    @DisplayName("Step 9 Test: Dashboard Statistics & Visualizations Data Integrity")
    void testDashboardAndReports() {
        authenticateAs(ownerA);

        // Record a trip and an expense
        tripService.createTrip(new TripRequest("Farmer Test", new BigDecimal("50.00"), new BigDecimal("10.00"), "Someshwar SSK", LocalDate.now(), null));
        expenseService.createExpense(new ExpenseRequest(ExpenseType.DIESEL, new BigDecimal("1500.00"), LocalDate.now(), "Fuel"));

        // Fetch dashboard statistics
        DashboardStatsResponse stats = dashboardService.getDashboardStats("ALL");
        assertEquals("Sanjay Patil", stats.getOwnerName());
        assertEquals("MH 12 SP 1111", stats.getTractorNumber());
        assertEquals(1, stats.getTotalTrips());
        assertEquals(new BigDecimal("50.00"), stats.getTotalWeight());
        assertEquals(new BigDecimal("1500.00"), stats.getTotalExpenses());

        // Fetch visualizations
        var charts = dashboardService.getDashboardVisualizations("ALL");
        assertNotNull(charts.get("monthlyWeight"));
        assertTrue(charts.get("monthlyWeight").isHasData());
        assertNotNull(charts.get("expenseCategory"));
        assertTrue(charts.get("expenseCategory").isHasData());

        // Fetch report
        ReportSummaryResponse report = reportService.generateReport("ALL", null, null);
        assertEquals(1, report.getTotalTrips());
        assertEquals(new BigDecimal("50.00"), report.getTotalWeight());
        assertEquals(new BigDecimal("1500.00"), report.getTotalExpenses());
    }

    @Test
    @DisplayName("Step 8 Test: Send Invoice Email Service")
    void testSendInvoiceEmail() {
        authenticateAs(ownerA);

        InvoiceEmailRequest emailReq = new InvoiceEmailRequest();
        emailReq.setBillId(101L);
        emailReq.setTo("customer@example.com");
        emailReq.setCustomerName("Ramesh Khot");
        emailReq.setOwnerName(ownerA.getName());
        emailReq.setTractorNumber(ownerA.getTractorNumber());
        emailReq.setTripCount(2);
        emailReq.setTotalWeight(new BigDecimal("55.50"));
        emailReq.setRatePerTon(new BigDecimal("350.00"));
        emailReq.setTotalAmount(new BigDecimal("19425.00"));
        emailReq.setBillDate("2026-10-29");

        var response = invoiceEmailService.sendInvoice(emailReq);
        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertEquals("customer@example.com", response.get("recipient"));
        assertEquals(101L, response.get("billId"));

        var history = invoiceEmailService.getSentEmailHistory();
        assertFalse(history.isEmpty());
        assertEquals("customer@example.com", history.get(0).get("recipient"));
    }
}
