/**
 * TrackBill AI - Master Dashboard Application Logic
 */

// Global Chart Instances Cache for clean destruction
const chartInstances = {};

// Active Filter State
let currentOverviewFilter = 'ALL';
let currentReportFilter = 'ALL';

// Dashboard-wide record caches. These are intentionally loaded from the actual CRUD APIs
// so the overview always reflects the complete backend data, not only the latest 5 rows.
let dashboardAllRecords = { trips: [], labour: [], expenses: [], bills: [] };
let activeDashboardRecordTab = 'trips';

// Delete Handler State
let pendingDeleteAction = null;

document.addEventListener('DOMContentLoaded', () => {
    // 1. Ensure User is Authenticated
    Api.requireAuth();

    // 2. Initialize Navigation and Views
    initNavigation();

    // 3. Load Owner Profile
    loadOwnerProfile();

    // 4. Load Initial Dashboard Data
    loadDashboardData();

    // 5. Initialize Forms and Event Listeners
    initEventListeners();
});

/* ==========================================================================
   Navigation & View Switching
   ========================================================================== */
function initNavigation() {
    const navLinks = document.querySelectorAll('.sidebar-nav .nav-item-link');
    const sidebar = document.getElementById('sidebar');
    const toggleBtn = document.getElementById('btn-sidebar-toggle');

    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const view = link.getAttribute('data-view');
            switchView(view);

            // On mobile, close sidebar after clicking
            if (window.innerWidth <= 992) {
                sidebar.classList.remove('show');
            }
        });
    });

    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            sidebar.classList.toggle('show');
        });
    }

    // Handle hash navigation
    const checkHashRoute = () => {
        const hash = window.location.hash.replace('#', '').trim();
        const validViews = ['overview', 'labour', 'trips', 'expenses', 'billing', 'calculator', 'reports', 'ai', 'profile'];
        if (hash && validViews.includes(hash)) {
            switchView(hash, false);
        }
    };

    window.addEventListener('hashchange', checkHashRoute);
    checkHashRoute();

    // Logout
    const logout = () => {
        Api.clearAuth();
        showToast('Logged out successfully.', 'info');
        setTimeout(() => { window.location.href = 'tractorlogin.html'; }, 500);
    };
    document.getElementById('btn-logout')?.addEventListener('click', logout);
    document.getElementById('topbar-logout')?.addEventListener('click', logout);
}

function switchView(viewName, updateHash = true) {
    if (updateHash && window.location.hash !== `#${viewName}`) {
        window.location.hash = viewName;
    }
    // Update active nav link
    document.querySelectorAll('.sidebar-nav .nav-item-link').forEach(link => {
        if (link.getAttribute('data-view') === viewName) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });

    // Update visible section
    document.querySelectorAll('.dashboard-view').forEach(sec => {
        sec.classList.add('d-none');
        sec.classList.remove('active');
    });

    const targetSection = document.getElementById(`view-${viewName}`);
    if (targetSection) {
        targetSection.classList.remove('d-none');
        targetSection.classList.add('active', 'fade-in');
    }

    // Update Topbar Heading
    const headings = {
        'overview': 'Dashboard Overview',
        'labour': 'Labour & Worker Management',
        'trips': 'Sugarcane Transportation Trips',
        'expenses': 'Tractor Expense Tracking',
        'billing': 'Billing & Invoicing',
        'calculator': 'Koyta Labour Payment Calculator',
        'reports': 'Reports & Analytics',
        'ai': 'TrackBill AI Business Assistant',
        'profile': 'Tractor Owner Profile'
    };
    document.getElementById('topbar-heading').textContent = headings[viewName] || 'Dashboard';

    // Lazy load specific view data
    if (viewName === 'labour') loadLabourData();
    if (viewName === 'trips') loadTripsData();
    if (viewName === 'expenses') loadExpensesData();
    if (viewName === 'billing') loadBillingData();
    if (viewName === 'calculator') initCalculatorView();
    if (viewName === 'reports') loadReportData(currentReportFilter);
    if (viewName === 'profile') loadProfileForm();
    if (viewName === 'overview') loadDashboardData();
}

/* ==========================================================================
   Owner Profile Loading
   ========================================================================== */
async function loadOwnerProfile() {
    try {
        const res = await Api.get('/api/owner/profile');
        if (res.success && res.data) {
            const owner = res.data;
            Api.setOwner(owner);

            document.getElementById('owner-sidebar-name').textContent = owner.name || 'Tractor Owner';
            document.getElementById('owner-sidebar-tractor').innerHTML = `<i class="bi bi-speedometer2"></i> ${owner.tractorNumber || 'MH--'}`;
            document.getElementById('owner-avatar').textContent = (owner.name || 'T').charAt(0).toUpperCase();
            const villageEl = document.getElementById('topbar-village');
            if (villageEl) villageEl.textContent = `${owner.village || ''}, ${owner.district || 'Maharashtra'}`;
            document.getElementById('hero-welcome').textContent = `Welcome back, ${owner.name}!`;
            document.getElementById('hero-tractor-num').textContent = owner.tractorNumber || '-';
            document.getElementById('hero-trolley-count').textContent = owner.trollyCount !== undefined ? owner.trollyCount : '1';

            // Also set default tractor number on Trip modal
            const tripTractorInput = document.getElementById('trip-tractor-num');
            if (tripTractorInput) tripTractorInput.value = owner.tractorNumber || '';
        }
    } catch (e) {
        console.error('Failed to load owner profile:', e);
    }
}

/* ==========================================================================
   Module 1: Dashboard Overview & Visualizations
   ========================================================================== */
async function loadDashboardData() {
    try {
        // Load Statistics
        const statsRes = await Api.get(`/api/dashboard/stats?filter=${currentOverviewFilter}`);
        if (statsRes.success && statsRes.data) {
            renderDashboardStats(statsRes.data);
        }

        // Load Visualizations
        const vizRes = await Api.get(`/api/dashboard/visualizations?filter=${currentOverviewFilter}`);
        if (vizRes.success && vizRes.data) {
            renderDashboardCharts(vizRes.data);
        }

        // Load every CRUD record for the dashboard record center.
        await loadAllDashboardRecords();
    } catch (error) {
        showToast('Error loading dashboard analytics: ' + error.message, 'error');
    }
}

async function loadAllDashboardRecords() {
    try {
        const [tripsRes, labourRes, expensesRes, billsRes] = await Promise.all([
            Api.get('/api/trips'),
            Api.get('/api/labour'),
            Api.get('/api/expenses'),
            Api.get('/api/bills')
        ]);

        dashboardAllRecords.trips = tripsRes?.success && Array.isArray(tripsRes.data) ? tripsRes.data : [];
        dashboardAllRecords.labour = labourRes?.success && Array.isArray(labourRes.data) ? labourRes.data : [];
        dashboardAllRecords.expenses = expensesRes?.success && Array.isArray(expensesRes.data) ? expensesRes.data : [];
        dashboardAllRecords.bills = billsRes?.success && Array.isArray(billsRes.data) ? billsRes.data : [];

        // Keep module caches synchronized so edit/delete actions always use fresh data.
        tripsListCache = dashboardAllRecords.trips;
        labourListCache = dashboardAllRecords.labour;
        expensesListCache = dashboardAllRecords.expenses;
        billsListCache = dashboardAllRecords.bills;

        renderAllDashboardRecords();
    } catch (error) {
        console.error('Failed to load all dashboard records:', error);
        // Do not replace a working dashboard with an error just because one optional
        // record table failed. The module-specific screens will show their own error.
    }
}

function switchDashboardRecordTab(tab) {
    activeDashboardRecordTab = tab;
    document.querySelectorAll('.record-tab').forEach(button => {
        button.classList.toggle('active', button.dataset.recordTab === tab);
    });
    document.querySelectorAll('.dashboard-record-view').forEach(view => {
        view.classList.toggle('d-none', view.id !== `dashboard-records-${tab}`);
    });
}

function renderAllDashboardRecords() {
    const counts = {
        trips: dashboardAllRecords.trips.length,
        labour: dashboardAllRecords.labour.length,
        expenses: dashboardAllRecords.expenses.length,
        bills: dashboardAllRecords.bills.length
    };
    Object.entries(counts).forEach(([key, value]) => {
        const el = document.getElementById(`all-count-${key}`);
        if (el) el.textContent = value;
    });

    const trips = dashboardAllRecords.trips;
    document.getElementById('dashboard-records-trips').innerHTML = trips.length ? `
        <table class="dashboard-record-table">
            <thead><tr><th>Date</th><th>Farmer / Owner</th><th>Mill</th><th>Tractor</th><th>Weight</th><th>Diesel</th><th>Status</th><th>Actions</th></tr></thead>
            <tbody>${trips.map(t => `
                <tr>
                    <td>${formatDate(t.tripDate)}</td>
                    <td><strong>${escapeHtml(t.sugarcaneOwnerName)}</strong></td>
                    <td>${escapeHtml(t.millName)}</td>
                    <td>${escapeHtml(t.tractorNumber || '-')}</td>
                    <td>${formatWeight(t.weight)}</td>
                    <td>${Number(t.dieselRequired || 0).toFixed(1)} L</td>
                    <td>${t.isBilled ? '<span class="status-pill status-complete">Billed</span>' : '<span class="status-pill status-pending">Unbilled</span>'}</td>
                    <td class="dashboard-record-actions">
                        <button class="btn-action" title="Edit" onclick="openEditTripModal(${t.id})"><i class="bi bi-pencil"></i></button>
                        <button class="btn-action btn-action-delete" title="Delete" onclick="confirmDeleteTrip(${t.id})"><i class="bi bi-trash"></i></button>
                    </td>
                </tr>`).join('')}</tbody>
        </table>` : dashboardEmptyRecord('No trips recorded yet.', 'bi-truck');

    const labour = dashboardAllRecords.labour;
    document.getElementById('dashboard-records-labour').innerHTML = labour.length ? `
        <table class="dashboard-record-table">
            <thead><tr><th>Name</th><th>Village</th><th>Category</th><th>Type</th><th>Phone</th><th>Advance</th><th>Actions</th></tr></thead>
            <tbody>${labour.map(l => `
                <tr>
                    <td><strong>${escapeHtml(l.name)}</strong></td><td>${escapeHtml(l.village)}</td>
                    <td>${escapeHtml(l.category)}</td><td>${escapeHtml(l.type)}</td>
                    <td><a href="tel:${escapeHtml(l.phone)}">${escapeHtml(l.phone)}</a></td>
                    <td>${formatCurrency(l.advanceMoney)}</td>
                    <td class="dashboard-record-actions">
                        <button class="btn-action" title="Edit" onclick="openEditLabourModal(${l.id})"><i class="bi bi-pencil"></i></button>
                        <button class="btn-action btn-action-delete" title="Delete" onclick="confirmDeleteLabour(${l.id}, '${escapeHtml(l.name)}')"><i class="bi bi-trash"></i></button>
                    </td>
                </tr>`).join('')}</tbody>
        </table>` : dashboardEmptyRecord('No labour workers recorded yet.', 'bi-people');

    const expenses = dashboardAllRecords.expenses;
    document.getElementById('dashboard-records-expenses').innerHTML = expenses.length ? `
        <table class="dashboard-record-table">
            <thead><tr><th>Date</th><th>Category</th><th>Amount</th><th>Description</th><th>Actions</th></tr></thead>
            <tbody>${expenses.map(e => `
                <tr>
                    <td>${formatDate(e.expenseDate)}</td><td>${escapeHtml(e.expenseType)}</td>
                    <td><strong>${formatCurrency(e.amount)}</strong></td><td>${escapeHtml(e.description || '-')}</td>
                    <td class="dashboard-record-actions">
                        <button class="btn-action" title="Edit" onclick="openEditExpenseModal(${e.id})"><i class="bi bi-pencil"></i></button>
                        <button class="btn-action btn-action-delete" title="Delete" onclick="confirmDeleteExpense(${e.id})"><i class="bi bi-trash"></i></button>
                    </td>
                </tr>`).join('')}</tbody>
        </table>` : dashboardEmptyRecord('No expenses recorded yet.', 'bi-wallet2');

    const bills = dashboardAllRecords.bills;
    document.getElementById('dashboard-records-bills').innerHTML = bills.length ? `
        <table class="dashboard-record-table">
            <thead><tr><th>Date</th><th>Farmer / Owner</th><th>Trips</th><th>Weight</th><th>Rate/Ton</th><th>Total</th><th>Actions</th></tr></thead>
            <tbody>${bills.map(b => `
                <tr>
                    <td>${formatDate(b.billDate)}</td><td>${escapeHtml(b.sugarcaneOwnerName || '-')}</td>
                    <td>${Number(b.tripCount || 0)}</td><td>${formatWeight(b.totalWeight)}</td>
                    <td>${formatCurrency(b.ratePerTon)}</td><td><strong>${formatCurrency(b.totalAmount)}</strong></td>
                    <td class="dashboard-record-actions">
                        <button class="btn-action" title="View Invoice" onclick="viewBillInvoice(${b.id})"><i class="bi bi-eye"></i></button>
                        <button class="btn-action btn-action-delete" title="Delete Bill" onclick="confirmDeleteBill(${b.id})"><i class="bi bi-trash"></i></button>
                    </td>
                </tr>`).join('')}</tbody>
        </table>` : dashboardEmptyRecord('No bills generated yet.', 'bi-receipt');

    switchDashboardRecordTab(activeDashboardRecordTab);
}

function dashboardEmptyRecord(message, icon) {
    return `<div class="dashboard-record-empty"><i class="bi ${icon}"></i><div>${escapeHtml(message)}</div></div>`;
}

function renderDashboardStats(stats) {
    document.getElementById('stat-trips').textContent = stats.totalTrips || 0;
    document.getElementById('stat-weight').textContent = formatWeight(stats.totalWeight);
    document.getElementById('stat-revenue').textContent = formatCurrency(stats.totalRevenue);
    document.getElementById('stat-expenses').textContent = formatCurrency(stats.totalExpenses);

    const dieselValue = stats.totalDiesel ?? stats.totalDieselUsed ?? stats.dieselUsed ?? stats.totalDieselLitres ?? 0;
    const dieselElem = document.getElementById('stat-diesel');
    if (dieselElem) dieselElem.textContent = `${Number(dieselValue || 0).toLocaleString('en-IN', { maximumFractionDigits: 1 })} L`;

    const profitElem = document.getElementById('stat-profit');
    profitElem.textContent = formatCurrency(stats.netProfit);
    profitElem.className = stats.netProfit < 0 ? 'stat-value text-danger' : 'stat-value text-success';

    // Recent Trips Table - preserve compatibility with multiple backend field names.
    const tripsTbody = document.getElementById('recent-trips-tbody');
    if (stats.recentTrips && stats.recentTrips.length > 0) {
        tripsTbody.innerHTML = stats.recentTrips.map(t => {
            const diesel = t.dieselRequired ?? t.dieselUsed ?? t.diesel ?? 0;
            const revenue = t.revenue ?? t.amount ?? t.billAmount ?? t.totalAmount ?? t.transportationCharge ?? 0;
            const status = t.status || (t.isBilled ? 'Billed' : 'Pending');
            const statusClass = String(status).toLowerCase().includes('bill') || String(status).toLowerCase().includes('complete') ? 'status-complete' : 'status-pending';
            return `
                <tr>
                    <td>${formatDate(t.tripDate)}</td>
                    <td><strong>${escapeHtml(t.millName || '-')}</strong><small>${escapeHtml(t.sugarcaneOwnerName || '')}</small></td>
                    <td>${Number(t.weight || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })} Ton</td>
                    <td>${Number(diesel || 0).toLocaleString('en-IN', { maximumFractionDigits: 1 })} L</td>
                    <td>${formatCurrency(revenue)}</td>
                    <td><span class="status-pill ${statusClass}">${escapeHtml(status)}</span></td>
                </tr>
            `;
        }).join('');
    } else {
        tripsTbody.innerHTML = `<tr><td colspan="6" class="empty-table">No trip records available yet.</td></tr>`;
    }

    // Recent Expenses Table (hidden compatibility table in the overview).
    const expTbody = document.getElementById('recent-expenses-tbody');
    if (expTbody) {
        if (stats.recentExpenses && stats.recentExpenses.length > 0) {
            expTbody.innerHTML = stats.recentExpenses.map(e => `
                <tr><td>${formatDate(e.expenseDate)}</td><td>${escapeHtml(e.expenseType || '-')}</td><td>${formatCurrency(e.amount)}</td><td>${escapeHtml(e.description || '-')}</td></tr>
            `).join('');
        } else {
            expTbody.innerHTML = '<tr><td colspan="4">No expenses recorded yet.</td></tr>';
        }
    }
}

function renderDashboardCharts(charts) {
    // 1. Monthly Weight
    renderSingleChart('chart-weight', 'empty-weight', charts.monthlyWeight);

    // 2. Monthly Trips
    renderSingleChart('chart-trips', 'empty-trips', charts.monthlyTrips);

    // 3. Expense Category
    renderSingleChart('chart-expenses', 'empty-expenses', charts.expenseCategory);

    // 4. Revenue vs Expenses
    renderSingleChart('chart-revenue-expense', 'empty-rev-exp', charts.revenueVsExpense);

    // 5. Mill Distribution
    renderSingleChart('chart-mills', 'empty-mills', charts.millDistribution);

    // 6. Labour Distribution
    renderSingleChart('chart-labour', 'empty-labour', charts.labourDistribution);
}

function renderSingleChart(canvasId, emptyId, chartData) {
    const canvas = document.getElementById(canvasId);
    const emptyBox = document.getElementById(emptyId);

    if (!canvas) return;

    // Clean up existing instance to avoid duplicate rendering or memory leaks
    if (chartInstances[canvasId]) {
        chartInstances[canvasId].destroy();
        delete chartInstances[canvasId];
    }

    if (!chartData || !chartData.hasData || !chartData.labels || chartData.labels.length === 0) {
        canvas.classList.add('d-none');
        if (emptyBox) emptyBox.classList.remove('d-none');
        return;
    }

    canvas.classList.remove('d-none');
    if (emptyBox) emptyBox.classList.add('d-none');

    const ctx = canvas.getContext('2d');

    const config = {
        type: chartData.chartType || 'bar',
        data: {
            labels: chartData.labels,
            datasets: chartData.datasets.map(ds => ({
                label: ds.label,
                data: ds.data,
                backgroundColor: ds.backgroundColor,
                borderColor: ds.borderColor,
                borderWidth: 1.5,
                fill: ds.fill || false,
                tension: 0.35
            }))
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: chartData.chartType === 'doughnut' || chartData.datasets.length > 1,
                    position: 'bottom',
                    labels: {
                        boxWidth: 12,
                        padding: 14,
                        font: { family: 'Inter', size: 12 }
                    }
                },
                tooltip: {
                    padding: 10,
                    callbacks: {
                        label: function(context) {
                            let label = context.dataset.label || '';
                            if (label) label += ': ';
                            const val = context.parsed.y !== undefined ? context.parsed.y : context.parsed;
                            if (chartData.title && chartData.title.includes('₹')) {
                                return label + formatCurrency(val);
                            } else if (chartData.title && chartData.title.includes('Tons')) {
                                return label + formatWeight(val);
                            }
                            return label + val;
                        }
                    }
                }
            },
            scales: (chartData.chartType === 'doughnut' || chartData.chartType === 'pie') ? {} : {
                x: {
                    grid: { display: false },
                    ticks: { font: { family: 'Inter', size: 11 } }
                },
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(0,0,0,0.05)' },
                    ticks: { font: { family: 'Inter', size: 11 } }
                }
            }
        }
    };

    chartInstances[canvasId] = new Chart(ctx, config);
}

/* ==========================================================================
   Module 2: Labour Management
   ========================================================================== */
let labourListCache = [];

async function loadLabourData() {
    try {
        const res = await Api.get('/api/labour');
        if (res.success && res.data) {
            labourListCache = res.data;
            renderLabourTable(labourListCache);
            updateLabourCounters(labourListCache);
        }
    } catch (e) {
        showToast('Failed to load labour records: ' + e.message, 'error');
    }
}

function updateLabourCounters(labours) {
    document.getElementById('labour-stat-total').textContent = labours.length;
    const full = labours.filter(l => l.category === 'FULL_KOYTA').length;
    const half = labours.filter(l => l.category === 'HALF_KOYTA').length;
    const totalAdvance = labours.reduce((sum, l) => sum + (Number(l.advanceMoney) || 0), 0);

    document.getElementById('labour-stat-full').textContent = full;
    document.getElementById('labour-stat-half').textContent = half;
    document.getElementById('labour-stat-advance').textContent = formatCurrency(totalAdvance);
}

function renderLabourTable(labours) {
    const tbody = document.getElementById('labour-tbody');
    const emptyBox = document.getElementById('labour-empty-state');

    if (!labours || labours.length === 0) {
        tbody.innerHTML = '';
        emptyBox.classList.remove('d-none');
        return;
    }

    emptyBox.classList.add('d-none');
    tbody.innerHTML = labours.map(l => `
        <tr>
            <td class="fw-bold">${escapeHtml(l.name)}</td>
            <td>${escapeHtml(l.village)}</td>
            <td>
                <span class="${l.category === 'FULL_KOYTA' ? 'badge-koyta-full' : 'badge-koyta-half'}">
                    ${l.category === 'FULL_KOYTA' ? 'Full Koyta (पूर्ण)' : 'Half Koyta (अर्धा)'}
                </span>
            </td>
            <td><span class="badge-type">${l.type}</span></td>
            <td><a href="tel:${l.phone}" class="text-decoration-none text-muted"><i class="bi bi-telephone"></i> ${l.phone}</a></td>
            <td class="fw-bold text-danger">${formatCurrency(l.advanceMoney)}</td>
            <td class="text-end">
                <button class="btn-action" title="Edit Labour" onclick="openEditLabourModal(${l.id})">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn-action btn-action-delete" title="Delete Labour" onclick="confirmDeleteLabour(${l.id}, '${escapeHtml(l.name)}')">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function openAddLabourModal() {
    document.getElementById('modal-labour-title').textContent = 'Add Labour Worker';
    document.getElementById('labour-edit-id').value = '';
    document.getElementById('form-labour').reset();
    document.getElementById('labour-advance').value = '0';
    new bootstrap.Modal(document.getElementById('modal-labour')).show();
}

function openEditLabourModal(id) {
    const labour = labourListCache.find(l => l.id === id);
    if (!labour) return;

    document.getElementById('modal-labour-title').textContent = 'Edit Labour Worker';
    document.getElementById('labour-edit-id').value = labour.id;
    document.getElementById('labour-name').value = labour.name;
    document.getElementById('labour-village').value = labour.village;
    document.getElementById('labour-category').value = labour.category;
    document.getElementById('labour-type').value = labour.type;
    document.getElementById('labour-phone').value = labour.phone;
    document.getElementById('labour-advance').value = labour.advanceMoney || 0;

    new bootstrap.Modal(document.getElementById('modal-labour')).show();
}

function confirmDeleteLabour(id, name) {
    pendingDeleteAction = async () => {
        try {
            await Api.delete(`/api/labour/${id}`);
            showToast(`Labour ${name} deleted successfully.`, 'success');
            await loadAllDashboardRecords();
            loadLabourData();
            loadDashboardData();
        } catch (e) {
            showToast(e.message, 'error');
        }
    };
    document.getElementById('delete-confirm-msg').textContent = `Are you sure you want to delete labour worker "${name}"?`;
    new bootstrap.Modal(document.getElementById('modal-delete-confirm')).show();
}

/* ==========================================================================
   Module 3: Trips Management
   ========================================================================== */
let tripsListCache = [];

async function loadTripsData() {
    try {
        const res = await Api.get('/api/trips');
        if (res.success && res.data) {
            tripsListCache = res.data;
            renderTripsTable(tripsListCache);
        }
    } catch (e) {
        showToast('Failed to load trips: ' + e.message, 'error');
    }
}

function renderTripsTable(trips) {
    const tbody = document.getElementById('trips-tbody');
    const emptyBox = document.getElementById('trips-empty-state');

    if (!trips || trips.length === 0) {
        tbody.innerHTML = '';
        emptyBox.classList.remove('d-none');
        return;
    }

    emptyBox.classList.add('d-none');
    tbody.innerHTML = trips.map(t => `
        <tr>
            <td>${formatDate(t.tripDate)}</td>
            <td class="fw-bold">${escapeHtml(t.sugarcaneOwnerName)}</td>
            <td><i class="bi bi-buildings text-muted me-1"></i> ${escapeHtml(t.millName)}</td>
            <td><span class="badge bg-light text-dark border">${escapeHtml(t.tractorNumber)}</span></td>
            <td class="fw-bold text-success">${formatWeight(t.weight)}</td>
            <td>${Number(t.dieselRequired || 0).toFixed(1)} L</td>
            <td>
                ${t.isBilled ? '<span class="badge badge-billed"><i class="bi bi-check2"></i> Billed</span>' : '<span class="badge badge-unbilled"><i class="bi bi-hourglass-split"></i> Unbilled</span>'}
            </td>
            <td class="text-end">
                <button class="btn-action" title="Edit Trip" onclick="openEditTripModal(${t.id})">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn-action btn-action-delete" title="Delete Trip" onclick="confirmDeleteTrip(${t.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function openAddTripModal() {
    document.getElementById('modal-trip-title').textContent = 'Record Sugarcane Trip';
    document.getElementById('trip-edit-id').value = '';
    document.getElementById('form-trip').reset();

    const owner = Api.getOwner();
    document.getElementById('trip-tractor-num').value = owner.tractorNumber || '';
    document.getElementById('trip-date').value = new Date().toISOString().split('T')[0];

    new bootstrap.Modal(document.getElementById('modal-trip')).show();
}

function openEditTripModal(id) {
    const trip = tripsListCache.find(t => t.id === id);
    if (!trip) return;

    document.getElementById('modal-trip-title').textContent = 'Edit Sugarcane Trip';
    document.getElementById('trip-edit-id').value = trip.id;
    document.getElementById('trip-farmer-name').value = trip.sugarcaneOwnerName;
    document.getElementById('trip-mill-name').value = trip.millName;
    document.getElementById('trip-weight').value = trip.weight;
    document.getElementById('trip-diesel').value = trip.dieselRequired || 0;
    document.getElementById('trip-date').value = trip.tripDate;
    document.getElementById('trip-tractor-num').value = trip.tractorNumber;

    new bootstrap.Modal(document.getElementById('modal-trip')).show();
}

function confirmDeleteTrip(id) {
    pendingDeleteAction = async () => {
        try {
            await Api.delete(`/api/trips/${id}`);
            showToast('Trip record deleted successfully.', 'success');
            await loadAllDashboardRecords();
            loadTripsData();
            loadDashboardData();
        } catch (e) {
            showToast(e.message, 'error');
        }
    };
    document.getElementById('delete-confirm-msg').textContent = 'Are you sure you want to delete this trip record?';
    new bootstrap.Modal(document.getElementById('modal-delete-confirm')).show();
}

async function filterTripsByDate() {
    const start = document.getElementById('trips-filter-start').value;
    const end = document.getElementById('trips-filter-end').value;

    let url = '/api/trips';
    if (start || end) {
        url = `/api/trips/filter?start=${start || ''}&end=${end || ''}`;
    }

    try {
        const res = await Api.get(url);
        if (res.success && res.data) {
            tripsListCache = res.data;
            renderTripsTable(tripsListCache);
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}

/* ==========================================================================
   Module 4: Expenses Management
   ========================================================================== */
let expensesListCache = [];

async function loadExpensesData() {
    try {
        const res = await Api.get('/api/expenses');
        if (res.success && res.data) {
            expensesListCache = res.data;
            renderExpensesTable(expensesListCache);
        }
    } catch (e) {
        showToast('Failed to load expenses: ' + e.message, 'error');
    }
}

function renderExpensesTable(expenses) {
    const tbody = document.getElementById('expenses-tbody');
    const emptyBox = document.getElementById('expenses-empty-state');

    if (!expenses || expenses.length === 0) {
        tbody.innerHTML = '';
        emptyBox.classList.remove('d-none');
        return;
    }

    emptyBox.classList.add('d-none');
    tbody.innerHTML = expenses.map(e => `
        <tr>
            <td>${formatDate(e.expenseDate)}</td>
            <td><span class="badge badge-expense">${e.expenseType}</span></td>
            <td class="fw-bold text-danger">${formatCurrency(e.amount)}</td>
            <td>${escapeHtml(e.description || '-')}</td>
            <td class="text-end">
                <button class="btn-action" title="Edit Expense" onclick="openEditExpenseModal(${e.id})">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn-action btn-action-delete" title="Delete Expense" onclick="confirmDeleteExpense(${e.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function openAddExpenseModal() {
    document.getElementById('modal-expense-title').textContent = 'Record Tractor Expense';
    document.getElementById('expense-edit-id').value = '';
    document.getElementById('form-expense').reset();
    document.getElementById('expense-date').value = new Date().toISOString().split('T')[0];

    new bootstrap.Modal(document.getElementById('modal-expense')).show();
}

function openEditExpenseModal(id) {
    const exp = expensesListCache.find(e => e.id === id);
    if (!exp) return;

    document.getElementById('modal-expense-title').textContent = 'Edit Tractor Expense';
    document.getElementById('expense-edit-id').value = exp.id;
    document.getElementById('expense-category').value = exp.expenseType;
    document.getElementById('expense-amount').value = exp.amount;
    document.getElementById('expense-date').value = exp.expenseDate;
    document.getElementById('expense-desc').value = exp.description || '';

    new bootstrap.Modal(document.getElementById('modal-expense')).show();
}

function confirmDeleteExpense(id) {
    pendingDeleteAction = async () => {
        try {
            await Api.delete(`/api/expenses/${id}`);
            showToast('Expense record deleted successfully.', 'success');
            await loadAllDashboardRecords();
            loadExpensesData();
            loadDashboardData();
        } catch (e) {
            showToast(e.message, 'error');
        }
    };
    document.getElementById('delete-confirm-msg').textContent = 'Are you sure you want to delete this expense record?';
    new bootstrap.Modal(document.getElementById('modal-delete-confirm')).show();
}

/* ==========================================================================
   Module 5: Billing & Invoicing (Rate Per Ton Entry)
   ========================================================================== */
let unbilledTripsCache = [];
let billsListCache = [];
let activeInvoiceForEmail = null;

async function loadBillingData() {
    try {
        // Load Unbilled Trips
        const unbilledRes = await Api.get('/api/trips/unbilled');
        if (unbilledRes.success && unbilledRes.data) {
            unbilledTripsCache = unbilledRes.data;
            renderUnbilledTripsTable(unbilledTripsCache);
        }

        // Load Bill History
        const billsRes = await Api.get('/api/bills');
        if (billsRes.success && billsRes.data) {
            billsListCache = billsRes.data;
            renderBillsTable(billsListCache);
            populateInvoiceEmailSelect(billsListCache);
        }

        document.getElementById('bill-date').value = new Date().toISOString().split('T')[0];
    } catch (e) {
        showToast('Error loading billing records: ' + e.message, 'error');
    }
}

function renderUnbilledTripsTable(trips) {
    const tbody = document.getElementById('unbilled-trips-tbody');
    if (!trips || trips.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center py-3 text-muted">No unbilled trips found. All completed trips have been billed.</td></tr>`;
        recalcSelectedBillWeight();
        return;
    }

    tbody.innerHTML = trips.map(t => `
        <tr>
            <td>
                <input type="checkbox" class="trip-select-chk" value="${t.id}" data-weight="${t.weight}" onchange="recalcSelectedBillWeight()">
            </td>
            <td>${formatDate(t.tripDate)}</td>
            <td class="fw-semibold">${escapeHtml(t.sugarcaneOwnerName)}</td>
            <td><small class="text-muted">${escapeHtml(t.millName)}</small></td>
            <td><strong>${formatWeight(t.weight)}</strong></td>
        </tr>
    `).join('');

    recalcSelectedBillWeight();
}

function recalcSelectedBillWeight() {
    const checkboxes = document.querySelectorAll('.trip-select-chk:checked');
    let totalWeight = 0;

    checkboxes.forEach(chk => {
        totalWeight += parseFloat(chk.getAttribute('data-weight')) || 0;
    });

    document.getElementById('bill-calc-weight').value = totalWeight.toFixed(3);
    recalcLiveBillAmount();
}

function recalcLiveBillAmount() {
    const weight = parseFloat(document.getElementById('bill-calc-weight').value) || 0;
    const ratePerTon = parseFloat(document.getElementById('bill-rate-per-ton').value) || 0;

    const total = weight * ratePerTon;

    document.getElementById('bill-live-total').textContent = formatCurrency(total);
    document.getElementById('bill-formula-preview').textContent = `${weight.toFixed(3)} Tons × ₹${ratePerTon.toFixed(2)} = ${formatCurrency(total)}`;
}

async function saveGeneratedBill() {
    const checkboxes = document.querySelectorAll('.trip-select-chk:checked');
    const selectedTripIds = Array.from(checkboxes).map(c => parseInt(c.value, 10));

    const ratePerTon = parseFloat(document.getElementById('bill-rate-per-ton').value);
    const billDate = document.getElementById('bill-date').value;

    if (selectedTripIds.length === 0) {
        showToast('Please select at least one trip to generate a bill.', 'warning');
        return;
    }

    if (!ratePerTon || ratePerTon <= 0) {
        showToast('Please enter a valid Rate Per Ton (e.g. ₹350).', 'warning');
        document.getElementById('bill-rate-per-ton').focus();
        return;
    }

    try {
        const res = await Api.post('/api/bills', {
            selectedTripIds,
            ratePerTon,
            billDate
        });

        if (res.success && res.data) {
            showToast('Bill generated successfully!', 'success');
            document.getElementById('bill-rate-per-ton').value = '';
            loadBillingData();
            loadDashboardData();
            viewBillInvoice(res.data.id);
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}

function renderBillsTable(bills) {
    const tbody = document.getElementById('bills-tbody');
    const emptyBox = document.getElementById('bills-empty-state');

    if (!bills || bills.length === 0) {
        tbody.innerHTML = '';
        emptyBox.classList.remove('d-none');
        return;
    }

    emptyBox.classList.add('d-none');
    tbody.innerHTML = bills.map(b => `
        <tr>
            <td><strong>#BILL-${b.id}</strong></td>
            <td>${formatDate(b.billDate)}</td>
            <td class="fw-bold">${escapeHtml(b.sugarcaneOwnerName || 'Various Farmers')}</td>
            <td><span class="badge bg-light text-dark border">${b.tripCount} trips</span></td>
            <td>${formatWeight(b.totalWeight)}</td>
            <td>₹${Number(b.ratePerTon).toFixed(2)} / ton</td>
            <td class="fw-bold text-success fs-6">${formatCurrency(b.totalAmount)}</td>
            <td class="text-end">
                <button class="btn btn-sm btn-outline-primary me-1" onclick="viewBillInvoice(${b.id})">
                    <i class="bi bi-printer"></i> View & Print
                </button>
                <button class="btn-action btn-action-delete" title="Delete Bill" onclick="confirmDeleteBill(${b.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

async function viewBillInvoice(billId) {
    try {
        const res = await Api.get(`/api/bills/${billId}`);
        if (!res.success || !res.data) return;

        const bill = res.data;
        activeInvoiceForEmail = bill;
        const container = document.getElementById('invoice-render-box');

        container.innerHTML = `
            <div class="invoice-header">
                <div>
                    <h3 class="brand-font text-success mb-1">TrackBill AI Sugarcane Invoice</h3>
                    <p class="text-muted small mb-0">Tractor Transportation Receipt</p>
                </div>
                <div class="text-end">
                    <h5 class="fw-bold mb-0">Invoice #BILL-${bill.id}</h5>
                    <small class="text-muted">Date: ${formatDate(bill.billDate)}</small>
                </div>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-6">
                    <span class="text-muted small fw-bold text-uppercase d-block">Tractor Transporter:</span>
                    <strong class="fs-6">${escapeHtml(bill.ownerName || 'Tractor Owner')}</strong><br>
                    <span class="text-muted">Tractor Reg: <strong>${escapeHtml(bill.tractorNumber || '-')}</strong></span><br>
                    <span class="text-muted">Location: ${escapeHtml(bill.ownerVillage || '')}, Taluka: ${escapeHtml(bill.ownerTaluka || '')}, Dist: ${escapeHtml(bill.ownerDistrict || '')}</span>
                </div>
                <div class="col-6 text-end">
                    <span class="text-muted small fw-bold text-uppercase d-block">Billed To (Sugarcane Farmer / Mill):</span>
                    <strong class="fs-6">${escapeHtml(bill.sugarcaneOwnerName || 'Sugarcane Grower')}</strong><br>
                    <span class="text-muted">Trips Included: ${bill.tripCount} Trip deliveries</span>
                </div>
            </div>

            <table class="table table-bordered mb-4">
                <thead class="table-light">
                    <tr>
                        <th>Trip Delivery Details</th>
                        <th class="text-end">Weight (Tons)</th>
                        <th class="text-end">Rate / Ton (₹)</th>
                        <th class="text-end">Subtotal (₹)</th>
                    </tr>
                </thead>
                <tbody>
                    ${(bill.tripDetails && bill.tripDetails.length > 0) ? bill.tripDetails.map(td => `
                        <tr>
                            <td>
                                <strong>${escapeHtml(td.millName)}</strong><br>
                                <small class="text-muted">Date: ${formatDate(td.tripDate)} | Farmer: ${escapeHtml(td.sugarcaneOwnerName)}</small>
                            </td>
                            <td class="text-end">${Number(td.weight).toFixed(3)} Tons</td>
                            <td class="text-end">₹${Number(bill.ratePerTon).toFixed(2)}</td>
                            <td class="text-end fw-semibold">${formatCurrency(Number(td.weight) * Number(bill.ratePerTon))}</td>
                        </tr>
                    `).join('') : `
                        <tr>
                            <td>Sugarcane Transportation Haulage (${bill.tripCount} delivery trips)</td>
                            <td class="text-end">${Number(bill.totalWeight).toFixed(3)} Tons</td>
                            <td class="text-end">₹${Number(bill.ratePerTon).toFixed(2)}</td>
                            <td class="text-end fw-semibold">${formatCurrency(bill.totalAmount)}</td>
                        </tr>
                    `}
                    <tr class="table-success fw-bold fs-6">
                        <td colspan="3" class="text-end">Total Amount Payable:</td>
                        <td class="text-end text-success">${formatCurrency(bill.totalAmount)}</td>
                    </tr>
                </tbody>
            </table>

            <div class="mt-4 pt-3 border-top d-flex justify-content-between align-items-center">
                <small class="text-muted">Generated by TrackBill AI Sugarcane Billing System</small>
                <div class="text-center" style="min-width: 180px;">
                    <div style="height: 40px;"></div>
                    <span class="d-block border-top pt-1 small text-muted">Transporter Signature</span>
                </div>
            </div>
        `;

        new bootstrap.Modal(document.getElementById('modal-invoice')).show();
    } catch (e) {
        showToast('Error opening bill invoice: ' + e.message, 'error');
    }
}

function confirmDeleteBill(id) {
    pendingDeleteAction = async () => {
        try {
            await Api.delete(`/api/bills/${id}`);
            showToast('Bill deleted and associated trips returned to unbilled status.', 'success');
            await loadAllDashboardRecords();
            loadBillingData();
            loadDashboardData();
        } catch (e) {
            showToast(e.message, 'error');
        }
    };
    document.getElementById('delete-confirm-msg').textContent = 'Are you sure you want to delete this bill? Associated trips will be unlocked back to unbilled status.';
    new bootstrap.Modal(document.getElementById('modal-delete-confirm')).show();
}

/* ==========================================================================
   Module 6: Koyta Labour Payment Calculator (Special Business Rule)
   ========================================================================== */
let lastKoytaCalculationResponse = null;

async function initCalculatorView() {
    document.getElementById('calc-date-input').value = new Date().toISOString().split('T')[0];

    try {
        const res = await Api.get('/api/labour');
        if (res.success && res.data) {
            const container = document.getElementById('calc-workers-checkboxes');
            if (res.data.length === 0) {
                container.innerHTML = `<div class="text-muted small p-2">No workers registered yet. Please add Full Koyta / Half Koyta labourers in the Labour tab first.</div>`;
                return;
            }

            container.innerHTML = res.data.map(l => `
                <div class="col-md-4 col-sm-6">
                    <div class="form-check p-2 bg-white rounded border">
                        <input class="form-check-input calc-worker-chk ms-1" type="checkbox" value="${l.id}" id="calc-lab-${l.id}" checked>
                        <label class="form-check-label ms-2 small d-block" for="calc-lab-${l.id}">
                            <strong class="d-block">${escapeHtml(l.name)}</strong>
                            <span class="${l.category === 'FULL_KOYTA' ? 'badge-koyta-full' : 'badge-koyta-half'}" style="font-size: 10px;">
                                ${l.category === 'FULL_KOYTA' ? 'Full Koyta' : 'Half Koyta'}
                            </span>
                            <span class="text-muted ms-1" style="font-size: 11px;">Adv: ${formatCurrency(l.advanceMoney)}</span>
                        </label>
                    </div>
                </div>
            `).join('');
        }
    } catch (e) {
        showToast('Error loading active workers: ' + e.message, 'error');
    }
}

async function runKoytaCalculation() {
    const totalWeight = parseFloat(document.getElementById('calc-weight-input').value);
    const ratePerTon = parseFloat(document.getElementById('calc-rate-input').value);
    const paymentDate = document.getElementById('calc-date-input').value;

    const checkedBoxes = document.querySelectorAll('.calc-worker-chk:checked');
    const labourIds = Array.from(checkedBoxes).map(c => parseInt(c.value, 10));

    if (!totalWeight || totalWeight <= 0) {
        showToast('Please enter valid total sugarcane weight in tons.', 'warning');
        return;
    }
    if (!ratePerTon || ratePerTon <= 0) {
        showToast('Please enter labour rate per ton.', 'warning');
        return;
    }
    if (labourIds.length === 0) {
        showToast('Please select at least one active worker for calculation.', 'warning');
        return;
    }

    try {
        const res = await Api.post('/api/labour-payments/calculate', {
            totalWeight,
            ratePerTon,
            labourIds,
            paymentDate
        });

        if (res.success && res.data) {
            lastKoytaCalculationResponse = res.data;
            renderKoytaCalculationResults(res.data);
            showToast('Distribution calculated successfully using Koyta rules.', 'success');
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}

function renderKoytaCalculationResults(data) {
    document.getElementById('calc-results-card').classList.remove('d-none');

    document.getElementById('res-total-workers').textContent = data.totalWorkers;
    document.getElementById('res-workers-ratio').textContent = `${data.fullKoytaCount} Full / ${data.halfKoytaCount} Half`;
    document.getElementById('res-base-share').textContent = formatWeight(data.baseSharePerWorker);
    document.getElementById('res-full-weight').textContent = formatWeight(data.fullKoytaShareWeight);
    document.getElementById('res-half-weight').textContent = formatWeight(data.halfKoytaShareWeight);

    document.getElementById('res-total-gross').textContent = formatCurrency(data.totalCalculatedGross);
    document.getElementById('res-total-advance').textContent = formatCurrency(data.totalAdvanceDeducted);
    document.getElementById('res-total-net').textContent = formatCurrency(data.totalNetPayable);

    const tbody = document.getElementById('calc-breakdown-tbody');
    tbody.innerHTML = data.items.map(item => `
        <tr>
            <td class="fw-bold">${escapeHtml(item.labourName)}</td>
            <td>
                <span class="${item.category === 'FULL_KOYTA' ? 'badge-koyta-full' : 'badge-koyta-half'}">
                    ${item.category === 'FULL_KOYTA' ? 'Full Koyta' : 'Half Koyta'}
                </span>
            </td>
            <td><small class="text-muted">${item.type}</small></td>
            <td><strong>${formatWeight(item.assignedWeight)}</strong></td>
            <td>${formatCurrency(item.grossAmount)}</td>
            <td class="text-muted">${formatCurrency(item.currentAdvance)}</td>
            <td class="text-danger fw-semibold">-${formatCurrency(item.advanceDeducted)}</td>
            <td class="text-success fw-bold fs-6">${formatCurrency(item.finalAmount)}</td>
        </tr>
    `).join('');

    // Smooth scroll to results
    document.getElementById('calc-results-card').scrollIntoView({ behavior: 'smooth' });
}

async function saveKoytaPayments() {
    if (!lastKoytaCalculationResponse || !lastKoytaCalculationResponse.items) {
        showToast('Please calculate payments first.', 'warning');
        return;
    }

    const paymentDate = document.getElementById('calc-date-input').value;

    try {
        const res = await Api.post('/api/labour-payments/save', {
            paymentDate,
            items: lastKoytaCalculationResponse.items
        });

        if (res.success) {
            showToast('Labour payment slips saved and advance money deducted successfully!', 'success');
            initCalculatorView();
            loadLabourData();
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}

/* ==========================================================================
   Module 7: Reports & Analytics
   ========================================================================== */
async function loadReportData(filterType) {
    currentReportFilter = filterType;

    let url = `/api/reports?filter=${filterType}`;

    if (filterType === 'CUSTOM') {
        const start = document.getElementById('report-custom-start').value;
        const end = document.getElementById('report-custom-end').value;
        url += `&startDate=${start || ''}&endDate=${end || ''}`;
    }

    try {
        const res = await Api.get(url);
        if (res.success && res.data) {
            renderReportData(res.data);
        }
    } catch (e) {
        showToast('Failed to load report data: ' + e.message, 'error');
    }
}

function renderReportData(report) {
    document.getElementById('rep-trips').textContent = report.totalTrips;
    document.getElementById('rep-weight').textContent = formatWeight(report.totalWeight);
    document.getElementById('rep-revenue').textContent = formatCurrency(report.totalRevenue);
    document.getElementById('rep-diesel').textContent = formatCurrency(report.dieselExpenses);
    document.getElementById('rep-expenses').textContent = formatCurrency(report.totalExpenses);

    const profitElem = document.getElementById('rep-profit');
    profitElem.textContent = formatCurrency(report.netProfit);
    profitElem.className = report.netProfit < 0 ? 'stat-value text-danger' : 'stat-value text-success';

    // Update counts
    document.getElementById('rep-trips-count').textContent = report.trips ? report.trips.length : 0;
    document.getElementById('rep-expenses-count').textContent = report.expenses ? report.expenses.length : 0;
    document.getElementById('rep-bills-count').textContent = report.bills ? report.bills.length : 0;

    // Render Tabular Data
    const tripsTbody = document.getElementById('rep-tbody-trips');
    if (report.trips && report.trips.length > 0) {
        tripsTbody.innerHTML = report.trips.map(t => `
            <tr>
                <td>${formatDate(t.tripDate)}</td>
                <td class="fw-semibold">${escapeHtml(t.sugarcaneOwnerName)}</td>
                <td>${escapeHtml(t.millName)}</td>
                <td><strong>${formatWeight(t.weight)}</strong></td>
                <td>${Number(t.dieselRequired || 0).toFixed(1)} L</td>
            </tr>
        `).join('');
    } else {
        tripsTbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-3">No trips recorded for this period.</td></tr>`;
    }

    const expTbody = document.getElementById('rep-tbody-expenses');
    if (report.expenses && report.expenses.length > 0) {
        expTbody.innerHTML = report.expenses.map(e => `
            <tr>
                <td>${formatDate(e.expenseDate)}</td>
                <td><span class="badge badge-expense">${e.expenseType}</span></td>
                <td class="fw-bold text-danger">${formatCurrency(e.amount)}</td>
                <td>${escapeHtml(e.description || '-')}</td>
            </tr>
        `).join('');
    } else {
        expTbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted py-3">No expenses recorded for this period.</td></tr>`;
    }

    const billsTbody = document.getElementById('rep-tbody-bills');
    if (report.bills && report.bills.length > 0) {
        billsTbody.innerHTML = report.bills.map(b => `
            <tr>
                <td>#BILL-${b.id}</td>
                <td>${formatDate(b.billDate)}</td>
                <td>${formatWeight(b.totalWeight)}</td>
                <td>₹${Number(b.ratePerTon).toFixed(2)}</td>
                <td class="fw-bold text-success">${formatCurrency(b.totalAmount)}</td>
            </tr>
        `).join('');
    } else {
        billsTbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-3">No bills generated for this period.</td></tr>`;
    }
}

/* ==========================================================================
   Module 8: AI Assistant
   ========================================================================== */
function askAi(promptText) {
    document.getElementById('ai-prompt-input').value = promptText;
    sendAiMessage();
}

async function sendAiMessage() {
    const input = document.getElementById('ai-prompt-input');
    const message = input.value.trim();
    if (!message) return;

    input.value = '';

    const messagesBox = document.getElementById('ai-chat-messages');

    // Append user message
    const userBubble = document.createElement('div');
    userBubble.className = 'chat-bubble user';
    userBubble.textContent = message;
    messagesBox.appendChild(userBubble);

    // Append typing indicator
    const typingBubble = document.createElement('div');
    typingBubble.className = 'chat-bubble assistant';
    typingBubble.innerHTML = `<span class="spinner-grow spinner-grow-sm text-success"></span> Analyzing verified TrackBill metrics...`;
    messagesBox.appendChild(typingBubble);
    messagesBox.scrollTop = messagesBox.scrollHeight;

    try {
        const res = await Api.post('/api/ai/chat', { message });
        typingBubble.remove();

        const aiBubble = document.createElement('div');
        aiBubble.className = 'chat-bubble assistant';

        let replyHtml = escapeHtml(res.data ? res.data.reply : 'No response');
        // Simple Markdown bold to HTML
        replyHtml = replyHtml.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
        replyHtml = replyHtml.replace(/\n/g, '<br>');

        aiBubble.innerHTML = replyHtml;
        messagesBox.appendChild(aiBubble);

        if (res.data && (res.data.isGeminiConfigured || res.data.configured)) {
            document.getElementById('ai-status-badge').innerHTML = `<i class="bi bi-stars text-warning"></i> Powered by Gemini 3.5 Flash`;
        }
    } catch (e) {
        typingBubble.remove();
        const errBubble = document.createElement('div');
        errBubble.className = 'chat-bubble assistant text-danger';
        errBubble.textContent = 'Error querying AI assistant: ' + e.message;
        messagesBox.appendChild(errBubble);
    }

    messagesBox.scrollTop = messagesBox.scrollHeight;
}

/* ==========================================================================
   Module 9: Owner Profile Form
   ========================================================================== */
function loadProfileForm() {
    const owner = Api.getOwner();
    document.getElementById('prof-name').value = owner.name || '';
    document.getElementById('prof-email').value = owner.email || '';
    document.getElementById('prof-tractor').value = owner.tractorNumber || '';
    document.getElementById('prof-trolley').value = owner.trollyCount !== undefined ? owner.trollyCount : 1;
    document.getElementById('prof-village').value = owner.village || '';
    document.getElementById('prof-taluka').value = owner.taluka || '';
    document.getElementById('prof-district').value = owner.district || '';
    document.getElementById('prof-pincode').value = owner.pincode || '';
}

/* ==========================================================================
   Event Listeners & Form Handlers
   ========================================================================== */
function initEventListeners() {
    // Overview Filter Pills
    document.querySelectorAll('#overview-filter-pills .filter-pill').forEach(pill => {
        pill.addEventListener('click', () => {
            document.querySelectorAll('#overview-filter-pills .filter-pill').forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            currentOverviewFilter = pill.getAttribute('data-filter');
            loadDashboardData();
        });
    });

    // Report Filter Pills
    document.querySelectorAll('#report-filter-pills .filter-pill').forEach(pill => {
        pill.addEventListener('click', () => {
            document.querySelectorAll('#report-filter-pills .filter-pill').forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            loadReportData(pill.getAttribute('data-report-filter'));
        });
    });

    // Live search on Labour
    document.getElementById('labour-search-input')?.addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase().trim();
        const filtered = labourListCache.filter(l =>
            l.name.toLowerCase().includes(query) ||
            l.village.toLowerCase().includes(query) ||
            l.phone.includes(query)
        );
        renderLabourTable(filtered);
    });

    // Live search on Trips
    document.getElementById('trips-search-input')?.addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase().trim();
        const filtered = tripsListCache.filter(t =>
            t.sugarcaneOwnerName.toLowerCase().includes(query) ||
            t.millName.toLowerCase().includes(query) ||
            t.tractorNumber.toLowerCase().includes(query)
        );
        renderTripsTable(filtered);
    });

    // Live search on Expenses
    document.getElementById('expenses-search-input')?.addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase().trim();
        const filtered = expensesListCache.filter(exp =>
            (exp.description && exp.description.toLowerCase().includes(query)) ||
            exp.expenseType.toLowerCase().includes(query)
        );
        renderExpensesTable(filtered);
    });

    // Live search on Bills
    document.getElementById('bills-search-input')?.addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase().trim();
        const filtered = billsListCache.filter(b =>
            String(b.id).includes(query) ||
            (b.sugarcaneOwnerName && b.sugarcaneOwnerName.toLowerCase().includes(query))
        );
        renderBillsTable(filtered);
    });

    // Bill Rate Per Ton Input
    document.getElementById('bill-rate-per-ton')?.addEventListener('input', recalcLiveBillAmount);
    document.getElementById('invoice-email-form')?.addEventListener('submit', sendInvoiceEmail);

    // Select All Trips Toggle
    const chkAll = document.getElementById('chk-select-all-trips');
    if (chkAll) {
        chkAll.addEventListener('change', (e) => {
            document.querySelectorAll('.trip-select-chk').forEach(c => {
                c.checked = e.target.checked;
            });
            recalcSelectedBillWeight();
        });
    }

    document.getElementById('btn-select-all-trips')?.addEventListener('click', (e) => {
        e.preventDefault();
        const checkboxes = document.querySelectorAll('.trip-select-chk');
        const anyUnchecked = Array.from(checkboxes).some(c => !c.checked);
        checkboxes.forEach(c => c.checked = anyUnchecked);
        if (chkAll) chkAll.checked = anyUnchecked;
        recalcSelectedBillWeight();
    });

    // Select All Workers Toggle in Calculator
    document.getElementById('btn-calc-select-all')?.addEventListener('click', (e) => {
        e.preventDefault();
        const checkboxes = document.querySelectorAll('.calc-worker-chk');
        const anyUnchecked = Array.from(checkboxes).some(c => !c.checked);
        checkboxes.forEach(c => c.checked = anyUnchecked);
    });

    // Form Submissions
    // 1. Labour Form
    document.getElementById('form-labour')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const editId = document.getElementById('labour-edit-id').value;
        const payload = {
            name: document.getElementById('labour-name').value.trim(),
            village: document.getElementById('labour-village').value.trim(),
            category: document.getElementById('labour-category').value,
            type: document.getElementById('labour-type').value,
            phone: document.getElementById('labour-phone').value.trim(),
            advanceMoney: parseFloat(document.getElementById('labour-advance').value) || 0
        };

        try {
            if (editId) {
                await Api.put(`/api/labour/${editId}`, payload);
                showToast('Labour updated successfully.', 'success');
            } else {
                await Api.post('/api/labour', payload);
                showToast('Labour worker added successfully.', 'success');
            }
            bootstrap.Modal.getInstance(document.getElementById('modal-labour')).hide();
            await loadAllDashboardRecords();
            loadLabourData();
            loadDashboardData();
        } catch (err) {
            showToast(err.message, 'error');
        }
    });

    // 2. Trip Form
    document.getElementById('form-trip')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const editId = document.getElementById('trip-edit-id').value;
        const payload = {
            sugarcaneOwnerName: document.getElementById('trip-farmer-name').value.trim(),
            millName: document.getElementById('trip-mill-name').value.trim(),
            weight: parseFloat(document.getElementById('trip-weight').value),
            dieselRequired: parseFloat(document.getElementById('trip-diesel').value) || 0,
            tripDate: document.getElementById('trip-date').value,
            tractorNumber: document.getElementById('trip-tractor-num').value.trim()
        };

        try {
            if (editId) {
                await Api.put(`/api/trips/${editId}`, payload);
                showToast('Trip updated successfully.', 'success');
            } else {
                await Api.post('/api/trips', payload);
                showToast('Sugarcane trip recorded successfully.', 'success');
            }
            bootstrap.Modal.getInstance(document.getElementById('modal-trip')).hide();
            await loadAllDashboardRecords();
            loadTripsData();
            loadDashboardData();
        } catch (err) {
            showToast(err.message, 'error');
        }
    });

    // 3. Expense Form
    document.getElementById('form-expense')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const editId = document.getElementById('expense-edit-id').value;
        const payload = {
            expenseType: document.getElementById('expense-category').value,
            amount: parseFloat(document.getElementById('expense-amount').value),
            expenseDate: document.getElementById('expense-date').value,
            description: document.getElementById('expense-desc').value.trim()
        };

        try {
            if (editId) {
                await Api.put(`/api/expenses/${editId}`, payload);
                showToast('Expense updated successfully.', 'success');
            } else {
                await Api.post('/api/expenses', payload);
                showToast('Expense recorded successfully.', 'success');
            }
            bootstrap.Modal.getInstance(document.getElementById('modal-expense')).hide();
            await loadAllDashboardRecords();
            loadExpensesData();
            loadDashboardData();
        } catch (err) {
            showToast(err.message, 'error');
        }
    });

    // 4. Profile Form
    document.getElementById('profile-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            name: document.getElementById('prof-name').value.trim(),
            tractorNumber: document.getElementById('prof-tractor').value.trim().toUpperCase(),
            trollyCount: parseInt(document.getElementById('prof-trolley').value, 10),
            village: document.getElementById('prof-village').value.trim(),
            taluka: document.getElementById('prof-taluka').value.trim(),
            district: document.getElementById('prof-district').value.trim(),
            pincode: document.getElementById('prof-pincode').value.trim()
        };

        try {
            const res = await Api.put('/api/owner/profile', payload);
            if (res.success && res.data) {
                Api.setOwner(res.data);
                showToast('Profile updated successfully!', 'success');
                loadOwnerProfile();
            }
        } catch (err) {
            showToast(err.message, 'error');
        }
    });

    // Confirm Delete Action
    document.getElementById('btn-confirm-delete')?.addEventListener('click', async () => {
        if (pendingDeleteAction) {
            await pendingDeleteAction();
            pendingDeleteAction = null;
        }
        bootstrap.Modal.getInstance(document.getElementById('modal-delete-confirm')).hide();
    });
}

function showQuickAddModal() {
    openAddTripModal();
}

// Security Helper
function escapeHtml(text) {
    if (!text) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}


/* ==========================================================================
   Invoice email delivery
   ========================================================================== */
function populateInvoiceEmailSelect(bills) {
    const select = document.getElementById('email-bill-id');
    if (!select) return;
    const selected = select.value;
    select.innerHTML = '<option value="">Select invoice</option>' + (bills || []).map(b =>
        `<option value="${Number(b.id)}">#BILL-${Number(b.id)} · ${escapeHtml(b.sugarcaneOwnerName || 'Various Farmers')} · ${formatCurrency(b.totalAmount)}</option>`
    ).join('');
    if (selected && [...select.options].some(option => option.value === selected)) select.value = selected;
}

function prefillInvoiceEmailFromPreview() {
    if (!activeInvoiceForEmail) return;
    const select = document.getElementById('email-bill-id');
    if (select) {
        if (![...select.options].some(option => option.value === String(activeInvoiceForEmail.id))) {
            const option = document.createElement('option');
            option.value = String(activeInvoiceForEmail.id);
            option.textContent = `#BILL-${activeInvoiceForEmail.id}`;
            select.appendChild(option);
        }
        select.value = String(activeInvoiceForEmail.id);
    }
    const emailPanel = document.getElementById('invoice-email-form');
    bootstrap.Modal.getInstance(document.getElementById('modal-invoice'))?.hide();
    switchView('billing');
    emailPanel?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    document.getElementById('invoice-recipient-email')?.focus({ preventScroll: true });
}

async function sendInvoiceEmail(event) {
    event.preventDefault();
    const billId = Number(document.getElementById('email-bill-id')?.value);
    const to = document.getElementById('invoice-recipient-email')?.value.trim();
    const status = document.getElementById('invoice-email-status');
    const button = document.getElementById('btn-send-invoice-email');
    if (!billId || !to) return;
    status.className = 'small text-muted';
    status.textContent = 'Preparing invoice email…';
    button.disabled = true;
    try {
        const response = await Api.get(`/api/bills/${billId}`);
        if (!response.success || !response.data) throw new Error('Could not load the selected invoice.');
        const bill = response.data;
        const result = await Api.post('/api/email/invoice', {
            to,
            billId: bill.id,
            billDate: bill.billDate,
            ownerName: bill.ownerName || 'Tractor Owner',
            tractorNumber: bill.tractorNumber || '-',
            customerName: bill.sugarcaneOwnerName || 'Sugarcane Grower',
            tripCount: bill.tripCount || 0,
            totalWeight: Number(bill.totalWeight || 0),
            ratePerTon: Number(bill.ratePerTon || 0),
            totalAmount: Number(bill.totalAmount || 0)
        });
        status.className = 'small text-success';
        status.textContent = result.message || `Invoice #BILL-${billId} sent to ${to}.`;
        showToast('Invoice email sent successfully.', 'success');
    } catch (error) {
        status.className = 'small text-danger';
        status.textContent = error.message || 'Unable to send invoice email. Check SMTP configuration.';
        showToast(status.textContent, 'error');
    } finally {
        button.disabled = false;
    }
}
