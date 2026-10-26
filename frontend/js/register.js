/**
 * TrackBill AI - Tractor Owner Registration Script
 */

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('register-form');
    const nameInput = document.getElementById('name');
    const tractorNumberInput = document.getElementById('tractorNumber');
    const trollyCountInput = document.getElementById('trollyCount');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const toggleBtn = document.getElementById('toggle-password');
    const toggleIcon = document.getElementById('toggle-icon');
    const villageInput = document.getElementById('village');
    const talukaInput = document.getElementById('taluka');
    const districtInput = document.getElementById('district');
    const pincodeInput = document.getElementById('pincode');
    const registerAlert = document.getElementById('register-alert');
    const btnRegister = document.getElementById('btn-register');
    const btnText = document.getElementById('btn-text');
    const btnSpinner = document.getElementById('btn-spinner');

    // Password visibility toggle
    toggleBtn.addEventListener('click', () => {
        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            toggleIcon.className = 'bi bi-eye-slash';
        } else {
            passwordInput.type = 'password';
            toggleIcon.className = 'bi bi-eye';
        }
    });

    // Auto-uppercase tractor number
    tractorNumberInput.addEventListener('input', (e) => {
        e.target.value = e.target.value.toUpperCase();
    });

    // Submit form
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        registerAlert.classList.add('d-none');

        const name = nameInput.value.trim();
        const tractorNumber = tractorNumberInput.value.trim().toUpperCase();
        const trollyCount = parseInt(trollyCountInput.value, 10);
        const email = emailInput.value.trim().toLowerCase();
        const password = passwordInput.value.trim();
        const village = villageInput.value.trim();
        const taluka = talukaInput.value.trim();
        const district = districtInput.value.trim();
        const pincode = pincodeInput.value.trim();

        // Client-side validations
        if (!name || !tractorNumber || isNaN(trollyCount) || !email || !password || !village || !taluka || !district || !pincode) {
            showError('Please fill out all required fields.');
            return;
        }

        if (trollyCount < 0) {
            showError('Trolley count must be 0 or greater.');
            return;
        }

        if (password.length < 6) {
            showError('Password must be at least 6 characters long.');
            return;
        }

        if (!/^[1-9][0-9]{5}$/.test(pincode)) {
            showError('Pincode must be a valid 6-digit Indian postal code.');
            return;
        }

        setLoading(true);

        try {
            const res = await Api.post('/api/auth/register', {
                name,
                tractorNumber,
                trollyCount,
                email,
                password,
                village,
                taluka,
                district,
                pincode
            });

            if (res.success && res.data && res.data.token) {
                Api.setToken(res.data.token);
                Api.setOwner(res.data.owner);
                showToast('Registration successful! Redirecting to your dashboard...', 'success');
                setTimeout(() => {
                    window.location.href = 'tractorhome.html';
                }, 800);
            } else {
                showError(res.message || 'Registration failed.');
            }
        } catch (error) {
            showError(error.message || 'Registration failed. Please check your information and try again.');
        } finally {
            setLoading(false);
        }
    });

    function showError(msg) {
        registerAlert.textContent = msg;
        registerAlert.classList.remove('d-none');
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function setLoading(isLoading) {
        btnRegister.disabled = isLoading;
        if (isLoading) {
            btnText.classList.add('d-none');
            btnSpinner.classList.remove('d-none');
        } else {
            btnText.classList.remove('d-none');
            btnSpinner.classList.add('d-none');
        }
    }
});
