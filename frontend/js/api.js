/**
 * TrackBill AI - Frontend API Client & Utilities
 */

const API_BASE_URL = (window.location.port === '8080') ? window.location.origin : 'http://localhost:8080';

const Api = {
    getToken() {
        return localStorage.getItem('trackbill_token');
    },

    setToken(token) {
        localStorage.setItem('trackbill_token', token);
    },

    getOwner() {
        try {
            return JSON.parse(localStorage.getItem('trackbill_owner') || '{}');
        } catch (e) {
            return {};
        }
    },

    setOwner(owner) {
        localStorage.setItem('trackbill_owner', JSON.stringify(owner));
    },

    getFarmer() {
        try { return JSON.parse(localStorage.getItem('trackbill_farmer') || '{}'); } catch (e) { return {}; }
    },

    setFarmer(farmer) { localStorage.setItem('trackbill_farmer', JSON.stringify(farmer)); },

    clearAuth() {
        localStorage.removeItem('trackbill_token');
        localStorage.removeItem('trackbill_owner');
        localStorage.removeItem('trackbill_farmer');
        localStorage.removeItem('trackbill_role');
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    requireAuth() {
        if (!this.isAuthenticated()) {
            window.location.href = 'tractorlogin.html';
        }
    },

    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...(options.headers || {})
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(url, {
                ...options,
                headers
            });

            if (response.status === 401) {
                // If unauthorized, redirect to login
                this.clearAuth();
                if (!window.location.pathname.includes('tractorlogin.html') && !window.location.pathname.includes('register.html')) {
                    window.location.href = 'tractorlogin.html';
                }
                throw new Error('Session expired or unauthorized. Please login again.');
            }

            const raw = response.status === 204 ? '' : await response.text();
            let data = null;
            if (raw) {
                try { data = JSON.parse(raw); }
                catch (_) { data = { message: raw }; }
            }

            if (!response.ok) {
                const message = data?.message || `Request failed with status ${response.status}`;
                throw new Error(message);
            }

            return data;
        } catch (error) {
            console.error(`API Error on ${endpoint}:`, error);
            throw error;
        }
    },

    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },

    post(endpoint, body) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    },

    put(endpoint, body) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(body)
        });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
};

// Utilities & Formatters
function formatCurrency(amount) {
    if (amount === null || amount === undefined || isNaN(amount)) return '₹0.00';
    return '₹' + Number(amount).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatWeight(tons) {
    if (tons === null || tons === undefined || isNaN(tons)) return '0.000 Tons';
    return Number(tons).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 3 }) + ' Tons';
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    try {
        const date = new Date(dateStr);
        return date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
    } catch (e) {
        return dateStr;
    }
}

// Toast Notifications
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `custom-toast toast-${type}`;

    let icon = 'bi-info-circle-fill';
    if (type === 'success') icon = 'bi-check-circle-fill text-success';
    if (type === 'error') icon = 'bi-exclamation-triangle-fill text-danger';
    if (type === 'warning') icon = 'bi-exclamation-circle-fill text-warning';

    toast.innerHTML = `
        <i class="bi ${icon} fs-5"></i>
        <div class="flex-grow-1">${message}</div>
        <button type="button" class="btn-close btn-sm" onclick="this.parentElement.remove()"></button>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        if (toast.parentElement) {
            toast.remove();
        }
    }, 4500);
}
