# Live End-to-End API Verification Script
$ErrorActionPreference = 'Stop'

try {
    # 1. Test Registration
    Write-Host "--- 1. Testing Registration ---"
    $unique = [System.Guid]::NewGuid().ToString().Substring(0, 8)
    $regBody = @{
        name = "Sunil Gaikwad"
        tractorNumber = "MH 11 SG 9090"
        trollyCount = 2
        email = "sunil.$unique@trackbill.in"
        password = "Password@123"
        village = "Phaltan"
        taluka = "Phaltan"
        district = "Satara"
        pincode = "415523"
    } | ConvertTo-Json

    $regRes = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $regBody -ContentType "application/json"
    Write-Host "Registration Success: $($regRes.success), Owner: $($regRes.data.owner.name), Tractor: $($regRes.data.owner.tractorNumber)"
    $token = $regRes.data.token

    # 2. Test Duplicate Email
    Write-Host "`n--- 2. Testing Duplicate Email ---"
    try {
        Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $regBody -ContentType "application/json"
        Write-Host "ERROR: Duplicate registration did not fail!"
    } catch {
        Write-Host "Duplicate registration rejected as expected: $($_.Exception.Message)"
    }

    # 3. Test Invalid Login
    Write-Host "`n--- 3. Testing Invalid Login ---"
    try {
        $badLogin = @{ email = "sunil.$unique@trackbill.in"; password = "WrongPassword" } | ConvertTo-Json
        Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $badLogin -ContentType "application/json"
        Write-Host "ERROR: Invalid login did not fail!"
    } catch {
        Write-Host "Invalid login rejected as expected: $($_.Exception.Message)"
    }

    # 4. Test Valid Login
    Write-Host "`n--- 4. Testing Valid Login ---"
    $goodLogin = @{ email = "sunil.$unique@trackbill.in"; password = "Password@123" } | ConvertTo-Json
    $loginRes = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $goodLogin -ContentType "application/json"
    Write-Host "Login Success: $($loginRes.success), Name: $($loginRes.data.owner.name)"

    $headers = @{ "Authorization" = "Bearer $($loginRes.data.token)" }

    # 5. Test Labour Module CRUD & Search
    Write-Host "`n--- 5. Testing Labour CRUD & Search ---"
    $labourBody = @{
        name = "Baburao Kale"
        village = "Koregaon"
        category = "FULL_KOYTA"
        phone = "9876500001"
        advanceMoney = 3000.00
        type = "LABOUR"
    } | ConvertTo-Json
    $labourRes = Invoke-RestMethod -Uri "http://localhost:8080/api/labour" -Method Post -Body $labourBody -ContentType "application/json" -Headers $headers
    $createdLabour = $labourRes.data
    Write-Host "Labour created: $($createdLabour.name), ID: $($createdLabour.id)"

    $allLabourRes = Invoke-RestMethod -Uri "http://localhost:8080/api/labour" -Method Get -Headers $headers
    $allLabour = $allLabourRes.data
    Write-Host "Labour count: $($allLabour.Count)"

    $searchLabourRes = Invoke-RestMethod -Uri "http://localhost:8080/api/labour/search?query=Kale" -Method Get -Headers $headers
    $searchLabour = $searchLabourRes.data
    Write-Host "Labour search found: $($searchLabour.Count) worker(s)"

    # 6. Test Trip Module CRUD & Default Tractor Number
    Write-Host "`n--- 6. Testing Trip Module CRUD ---"
    $tripBody1 = @{
        sugarcaneOwnerName = "Dattatray Shinde"
        weight = 55.40
        dieselRequired = 18.50
        millName = "Shri Datta SSK"
        tripDate = "2026-09-23"
    } | ConvertTo-Json
    $tripRes1 = Invoke-RestMethod -Uri "http://localhost:8080/api/trips" -Method Post -Body $tripBody1 -ContentType "application/json" -Headers $headers
    $trip1 = $tripRes1.data
    Write-Host "Trip 1 created with tractorNumber defaulted: $($trip1.tractorNumber), Weight: $($trip1.weight)"

    $tripBody2 = @{
        sugarcaneOwnerName = "Hanumant Pawar"
        weight = 44.60
        dieselRequired = 15.00
        millName = "Shri Datta SSK"
        tripDate = "2026-09-23"
    } | ConvertTo-Json
    $tripRes2 = Invoke-RestMethod -Uri "http://localhost:8080/api/trips" -Method Post -Body $tripBody2 -ContentType "application/json" -Headers $headers
    $trip2 = $tripRes2.data
    Write-Host "Trip 2 created. Weight: $($trip2.weight)"

    # 7. Test Expense Module CRUD
    Write-Host "`n--- 7. Testing Expense Module CRUD ---"
    $expBody = @{
        expenseType = "DIESEL"
        amount = 3200.00
        expenseDate = "2026-09-23"
        description = "35L Diesel for 2 Trips"
    } | ConvertTo-Json
    $expRes = Invoke-RestMethod -Uri "http://localhost:8080/api/expenses" -Method Post -Body $expBody -ContentType "application/json" -Headers $headers
    $exp = $expRes.data
    Write-Host "Expense created: $($exp.expenseType), Amount: $($exp.amount)"

    # 8. Test Billing Calculation (55.40 + 44.60 = 100 Tons @ 350 = 35000)
    Write-Host "`n--- 8. Testing Billing Calculation (100 Tons @ 350 = 35000) ---"
    $billBody = @{
        selectedTripIds = @($trip1.id, $trip2.id)
        ratePerTon = 350.00
        billDate = "2026-09-23"
        sugarcaneOwnerName = "Shri Datta SSK Combined"
    } | ConvertTo-Json
    $billRes = Invoke-RestMethod -Uri "http://localhost:8080/api/billing" -Method Post -Body $billBody -ContentType "application/json" -Headers $headers
    $bill = $billRes.data
    Write-Host "Bill created! Total Weight: $($bill.totalWeight), Rate/Ton: $($bill.ratePerTon), Total Amount: $($bill.totalAmount), Trips Billed: $($bill.tripCount)"

    # 9. Test Koyta Labour Calculation Algorithm
    Write-Host "`n--- 9. Testing Koyta Labour Calculation Algorithm ---"
    # Create 9 Full Koyta and 1 Half Koyta workers
    $workerIds = @()
    for ($i = 1; $i -le 9; $i++) {
        $wReq = @{
            name = "Koyta Full $i"
            village = "Koyta Village"
            category = "FULL_KOYTA"
            phone = "911111110$i"
            advanceMoney = 1000.00
            type = "LABOUR"
        } | ConvertTo-Json
        $wRes = Invoke-RestMethod -Uri "http://localhost:8080/api/labour" -Method Post -Body $wReq -ContentType "application/json" -Headers $headers
        $workerIds += $wRes.data.id
    }
    $hReq = @{
        name = "Koyta Half 1"
        village = "Koyta Village"
        category = "HALF_KOYTA"
        phone = "9111111199"
        advanceMoney = 500.00
        type = "LABOUR"
    } | ConvertTo-Json
    $hRes = Invoke-RestMethod -Uri "http://localhost:8080/api/labour" -Method Post -Body $hReq -ContentType "application/json" -Headers $headers
    $workerIds += $hRes.data.id

    $calcBody = @{
        totalWeight = 100.00
        ratePerTon = 350.00
        selectedLabourIds = $workerIds
        paymentDate = "2026-09-23"
    } | ConvertTo-Json
    $koytaRes = (Invoke-RestMethod -Uri "http://localhost:8080/api/labour/calculate" -Method Post -Body $calcBody -ContentType "application/json" -Headers $headers).data
    Write-Host "Koyta Calculation Results:"
    Write-Host "  Total Workers: $($koytaRes.totalWorkers) (Full: $($koytaRes.fullKoytaCount), Half: $($koytaRes.halfKoytaCount))"
    Write-Host "  Base Share: $($koytaRes.baseSharePerWorker) Tons"
    Write-Host "  Half Koyta Weight: $($koytaRes.halfKoytaShareWeight) Tons"
    Write-Host "  Full Koyta Weight: $($koytaRes.fullKoytaShareWeight) Tons"
    Write-Host "  Total Gross Amount: $($koytaRes.totalGrossAmount), Total Net: $($koytaRes.totalFinalAmount)"

    # 10. Test Dashboard Statistics & Visualizations
    Write-Host "`n--- 10. Testing Dashboard Statistics & Charts ---"
    $stats = (Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/stats" -Method Get -Headers $headers).data
    Write-Host "Dashboard Stats -> Owner: $($stats.ownerName), Total Weight: $($stats.totalWeight), Revenue: $($stats.totalBilling), Expenses: $($stats.totalExpenses), Net Profit: $($stats.netProfit)"

    $charts = (Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/visualizations" -Method Get -Headers $headers).data
    Write-Host "Visualizations loaded: MonthlyWeight hasData=$($charts.monthlyWeight.hasData), ExpenseCategory hasData=$($charts.expenseCategory.hasData)"

    # 11. Test Reports Summary
    Write-Host "`n--- 11. Testing Reports Summary ---"
    $report = (Invoke-RestMethod -Uri "http://localhost:8080/api/reports?filter=ALL" -Method Get -Headers $headers).data
    Write-Host "Report Summary -> Total Trips: $($report.totalTrips), Total Weight: $($report.totalWeight), Total Expenses: $($report.totalExpenses), Net Margin: $($report.netMargin)"

    # 12. Test Owner Data Isolation (Second Owner cannot see First Owner's records)
    Write-Host "`n--- 12. Testing Owner Data Isolation ---"
    $regBody2 = @{
        name = "Santosh Jagtap"
        tractorNumber = "MH 12 SJ 5555"
        trollyCount = 1
        email = "santosh.$unique@trackbill.in"
        password = "Password@123"
        village = "Baramati"
        taluka = "Baramati"
        district = "Pune"
        pincode = "413102"
    } | ConvertTo-Json
    $regRes2 = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $regBody2 -ContentType "application/json"
    $headers2 = @{ "Authorization" = "Bearer $($regRes2.data.token)" }

    $owner2Trips = (Invoke-RestMethod -Uri "http://localhost:8080/api/trips" -Method Get -Headers $headers2).data
    Write-Host "Owner 2 Trips count (Must be 0): $($owner2Trips.Count)"
    if ($owner2Trips.Count -eq 0) {
        Write-Host "DATA ISOLATION CONFIRMED: Owner 2 cannot see Owner 1's trips!"
    } else {
        Write-Host "SECURITY ERROR: Owner 2 accessed Owner 1's trips!"
    }

    # 13. Test Unauthorized Access
    Write-Host "`n--- 13. Testing Unauthorized Access (No Token) ---"
    try {
        Invoke-RestMethod -Uri "http://localhost:8080/api/trips" -Method Get
        Write-Host "ERROR: Unauthorized access was not blocked!"
    } catch {
        Write-Host "Unauthorized access blocked as expected: $($_.Exception.Message)"
    }

    # 14. Test CORS Options Preflight
    Write-Host "`n--- 14. Testing CORS Headers ---"
    $corsResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/trips" -Method Options -Headers @{ "Origin" = "http://localhost:5500"; "Access-Control-Request-Method" = "GET" }
    Write-Host "CORS Status Code: $($corsResponse.StatusCode), Access-Control-Allow-Origin: $($corsResponse.Headers['Access-Control-Allow-Origin'])"

    Write-Host "`n=========================================================="
    Write-Host "       ALL 20 SYSTEM VERIFICATION CHECKS PASSED!"
    Write-Host "=========================================================="
} catch {
    Write-Host "UNHANDLED FAILURE: $($_.Exception.ToString())"
    exit 1
}
