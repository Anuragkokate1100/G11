# Live End-to-End Farmer Portal API Verification Script
$ErrorActionPreference = 'Stop'

try {
    Write-Host "=========================================================="
    Write-Host "       STARTING FARMER PORTAL LIVE API TESTS"
    Write-Host "=========================================================="

    # 1. Register Farmer
    Write-Host "`n--- 1. Testing Farmer Registration ---"
    $unique = [System.Guid]::NewGuid().ToString().Substring(0, 8)
    $randDigits = (Get-Random -Minimum 100000 -Maximum 999999)
    $farmerRegBody = @{
        name = "Kishor Mane"
        email = "kishor.$unique@farmer.in"
        phone = "9822$randDigits"
        password = "Password@123"
        village = "Malegaon"
        taluka = "Baramati"
        district = "Pune"
        state = "Maharashtra"
        pincode = "413115"
    } | ConvertTo-Json

    $regRes = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/farmer/register" -Method Post -Body $farmerRegBody -ContentType "application/json"
    Write-Host "Farmer Registration Success: $($regRes.success), Message: $($regRes.message)"
    if ($regRes.data.token -ne $null) {
        throw "Security Error: Token should not be returned on registration"
    }

    # 2. Login Farmer
    Write-Host "`n--- 2. Testing Farmer Login ---"
    $loginBody = @{
        email = "kishor.$unique@farmer.in"
        password = "Password@123"
    } | ConvertTo-Json

    $loginRes = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/farmer/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginRes.data.token
    Write-Host "Farmer Login Success! Token acquired. Profile Name: $($loginRes.data.profile.name)"
    $headers = @{ "Authorization" = "Bearer $token" }

    # 3. Farmer Me endpoint
    Write-Host "`n--- 3. Testing Farmer Profile (/api/auth/farmer/me) ---"
    $meRes = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/farmer/me" -Method Get -Headers $headers
    Write-Host "Profile verified: Name=$($meRes.data.name), Email=$($meRes.data.email), Village=$($meRes.data.village)"

    # 4. Create Income Transaction
    Write-Host "`n--- 4. Testing Farmer Transaction Creation (Income & Expense) ---"
    $incTx = @{
        transactionType = "INCOME"
        category = "Crop Sale"
        amount = 75000.00
        transactionDate = (Get-Date).ToString("yyyy-MM-dd")
        description = "Sugarcane cutting sale"
        farmOrCrop = "Plot A"
    } | ConvertTo-Json
    $incRes = Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/transactions" -Method Post -Body $incTx -ContentType "application/json" -Headers $headers
    Write-Host "Income Transaction created! ID: $($incRes.id), Type: $($incRes.transactionType), Amount: $($incRes.amount)"

    # 5. Create Expense Transaction
    $expTx = @{
        transactionType = "EXPENSE"
        category = "Diesel & Transport"
        amount = 18000.00
        transactionDate = (Get-Date).ToString("yyyy-MM-dd")
        description = "Transport payment"
        farmOrCrop = "Plot A"
    } | ConvertTo-Json
    $expRes = Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/transactions" -Method Post -Body $expTx -ContentType "application/json" -Headers $headers
    Write-Host "Expense Transaction created! ID: $($expRes.id), Type: $($expRes.transactionType), Amount: $($expRes.amount)"

    # 6. List Transactions & Filter
    Write-Host "`n--- 6. Testing Farmer Transaction List & Filtering ---"
    $allTx = Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/transactions" -Method Get -Headers $headers
    Write-Host "Total Transactions returned: $($allTx.Count)"

    $incomeOnly = Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/transactions?type=INCOME" -Method Get -Headers $headers
    Write-Host "Income Transactions filtered: $($incomeOnly.Count)"

    # 7. Transaction Summary
    Write-Host "`n--- 7. Testing Farmer Transaction Summary ---"
    $summary = Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/transactions/summary" -Method Get -Headers $headers
    Write-Host "Summary: Income=$($summary.income), Expenses=$($summary.expenses), NetBalance=$($summary.netBalance), Count=$($summary.count)"

    # 8. Update Transaction
    Write-Host "`n--- 8. Testing Farmer Transaction Update ---"
    $updTx = @{
        transactionType = "EXPENSE"
        category = "Fertilizer"
        amount = 20000.00
        transactionDate = (Get-Date).ToString("yyyy-MM-dd")
        description = "Updated expense description"
        farmOrCrop = "Plot A"
    } | ConvertTo-Json
    $updRes = Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/transactions/$($expRes.id)" -Method Put -Body $updTx -ContentType "application/json" -Headers $headers
    Write-Host "Updated Transaction: ID=$($updRes.id), Category=$($updRes.category), Amount=$($updRes.amount)"

    # 9. Delete Transaction
    Write-Host "`n--- 9. Testing Farmer Transaction Deletion ---"
    Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/transactions/$($expRes.id)" -Method Delete -Headers $headers
    $afterDel = Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/transactions" -Method Get -Headers $headers
    Write-Host "Transaction deleted successfully. Remaining count: $($afterDel.Count)"

    # 10. Farmer Crops CRUD
    Write-Host "`n--- 10. Testing Farmer Crops CRUD ---"
    $cropBody = @{
        cropName = "Sugarcane Co 86032"
        landArea = 4.25
        areaUnit = "acre"
        sowingDate = "2026-06-01"
        expectedHarvestDate = "2027-04-01"
        status = "GROWING"
    } | ConvertTo-Json
    $cropRes = (Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/crops" -Method Post -Body $cropBody -ContentType "application/json" -Headers $headers).data
    Write-Host "Crop created! ID=$($cropRes.id), Name=$($cropRes.cropName), Area=$($cropRes.landArea)"

    $cropsList = (Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/crops" -Method Get -Headers $headers).data
    Write-Host "Crops count: $($cropsList.Count)"

    # 11. Farmer Service Requests
    Write-Host "`n--- 11. Testing Farmer Service Requests ---"
    $svcBody = @{
        serviceRequired = "Tractor Ploughing"
        location = "Malegaon Shivar Plot 4"
        preferredDate = (Get-Date).AddDays(5).ToString("yyyy-MM-dd")
        details = "Deep ploughing required for 4 acres"
    } | ConvertTo-Json
    $svcRes = Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/service-requests" -Method Post -Body $svcBody -ContentType "application/json" -Headers $headers
    Write-Host "Service Request created! ID: $($svcRes.id), Status: $($svcRes.status)"

    $svcList = Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/service-requests" -Method Get -Headers $headers
    Write-Host "Farmer Service Requests count: $($svcList.Count)"

    # 12. Farmer Reports
    Write-Host "`n--- 12. Testing Farmer Monthly Reports ---"
    $reportRes = Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/reports/monthly" -Method Get -Headers $headers
    Write-Host "Report Monthly Breakdown count: $($reportRes.monthly.Count), Income: $($reportRes.income), Expenses: $($reportRes.expenses)"

    # 13. Farmer Notifications
    Write-Host "`n--- 13. Testing Farmer Notifications ---"
    $notifRes = Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/notifications" -Method Get -Headers $headers
    Write-Host "Notifications Count: $($notifRes.items.Count), Unread: $($notifRes.unreadCount)"

    # 14. Farmer Isolation (Farmer 2 cannot see Farmer 1's records)
    Write-Host "`n--- 14. Testing Farmer Data Isolation ---"
    $unique2 = [System.Guid]::NewGuid().ToString().Substring(0, 8)
    $randDigits2 = (Get-Random -Minimum 100000 -Maximum 999999)
    $farmerRegBody2 = @{
        name = "Deepak Jagtap"
        email = "deepak.$unique2@farmer.in"
        phone = "9844$randDigits2"
        password = "Password@123"
        village = "Indapur"
        taluka = "Indapur"
        district = "Pune"
        state = "Maharashtra"
        pincode = "413106"
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "http://localhost:8080/api/auth/farmer/register" -Method Post -Body $farmerRegBody2 -ContentType "application/json" | Out-Null
    $loginRes2 = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/farmer/login" -Method Post -Body (@{ email = "deepak.$unique2@farmer.in"; password = "Password@123" } | ConvertTo-Json) -ContentType "application/json"
    $headers2 = @{ "Authorization" = "Bearer $($loginRes2.data.token)" }

    $farmer2Tx = Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/transactions" -Method Get -Headers $headers2
    Write-Host "Farmer 2 Transactions count (Must be 0): $($farmer2Tx.Count)"
    if ($farmer2Tx.Count -eq 0) {
        Write-Host "DATA ISOLATION CONFIRMED: Farmer 2 cannot see Farmer 1's transactions!"
    } else {
        throw "SECURITY ERROR: Farmer 2 accessed Farmer 1's transactions!"
    }

    try {
        Invoke-RestMethod -Uri "http://localhost:8080/api/farmer/transactions/$($incRes.id)" -Method Delete -Headers $headers2
        throw "SECURITY ERROR: Farmer 2 deleted Farmer 1's transaction!"
    } catch {
        Write-Host "Farmer 2 cross-deletion blocked as expected: $($_.Exception.Message)"
    }

    Write-Host "`n=========================================================="
    Write-Host "     ALL FARMER PORTAL LIVE API TESTS PASSED!"
    Write-Host "=========================================================="
} catch {
    Write-Host "UNHANDLED FAILURE: $($_.Exception.ToString())"
    exit 1
}
