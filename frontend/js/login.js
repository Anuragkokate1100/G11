/**
 * TrackBill AI - Login Controller Script
 */

document.addEventListener('DOMContentLoaded', () => {
    // If already logged in, redirect to dashboard
    if (Api.isAuthenticated()) {
        window.location.href = localStorage.getItem('trackbill_role') === 'FARMER' ? 'farmerhome.html' : 'tractorhome.html';
        return;
    }

    const form = document.getElementById('login-form');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const toggleBtn = document.getElementById('toggle-password');
    const toggleIcon = document.getElementById('toggle-icon');
    const loginAlert = document.getElementById('login-alert');
    const btnLogin = document.getElementById('btn-login');
    const btnText = document.getElementById('btn-text');
    const btnSpinner = document.getElementById('btn-spinner');
    const btnDemo = document.getElementById('btn-demo-fill');
    let selectedRole = 'owner';
    const loginParams = new URLSearchParams(window.location.search);
    const registerLink = document.getElementById('register-link');
    document.querySelectorAll('.role-card').forEach(card => {
        card.addEventListener('click', () => {
            document.querySelectorAll('.role-card').forEach(item => {
                item.classList.remove('selected');
                item.setAttribute('aria-pressed', 'false');
                item.querySelector('.role-check')?.remove();
            });
            card.classList.add('selected');
            card.setAttribute('aria-pressed', 'true');
            selectedRole = card.dataset.role || 'owner';
            if (registerLink) registerLink.href = selectedRole === 'farmer' ? 'farmer-register.html' : 'register.html';
            document.querySelector('.reference-login-title p').textContent = selectedRole === 'farmer' ? 'Login to manage your farm and crops' : 'Login to continue managing your tractor business';
            const check = document.createElement('span');
            check.className = 'role-check';
            check.innerHTML = '<i class="bi bi-check-lg"></i>';
            card.appendChild(check);
        });
    });

    if (loginParams.get('role') === 'farmer') {
        document.querySelector('.role-card[data-role="farmer"]')?.click();
        if (loginParams.get('email')) emailInput.value = loginParams.get('email');
    }

    // Toggle password visibility
    toggleBtn.addEventListener('click', () => {
        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            toggleIcon.className = 'bi bi-eye-slash';
        } else {
            passwordInput.type = 'password';
            toggleIcon.className = 'bi bi-eye';
        }
    });

    // Handle Quick Demo Fill
    btnDemo.addEventListener('click', async () => {
        if (selectedRole === 'farmer') { showError('Demo autofill is available for the Tractor Owner account only. Register a farmer account first.'); return; }
        emailInput.value = 'sanjay.patil@trackbill.in';
        passwordInput.value = 'Sanjay@123';
        loginAlert.classList.add('d-none');
    });

    // Handle Form Submit
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        loginAlert.classList.add('d-none');

        const email = emailInput.value.trim();
        const password = passwordInput.value.trim();

        if (!email || !password) {
            showError('Please fill in both email and password.');
            return;
        }

        setLoading(true);

        try {
            const res = selectedRole === 'farmer'
                ? await Api.post('/api/auth/farmer/login', { email, password })
                : await Api.post('/api/auth/login', { email, password });

            if (res.success && res.data && res.data.token) {
                Api.setToken(res.data.token);
                if (selectedRole === 'farmer') {
                    Api.setFarmer(res.data.profile);
                    localStorage.setItem('trackbill_role', 'FARMER');
                } else {
                    Api.setOwner(res.data.owner);
                    localStorage.setItem('trackbill_role', 'TRACTOR_OWNER');
                }
                showToast('Login successful! Redirecting to dashboard...', 'success');
                setTimeout(() => {
                    window.location.href = selectedRole === 'farmer' ? 'farmerhome.html' : 'tractorhome.html';
                }, 700);
            } else {
                showError(res.message || 'Login failed.');
            }
        } catch (error) {
            // If demo account does not exist, let's try auto-registering the demo account!
            if (selectedRole === 'owner' && email === 'sanjay.patil@trackbill.in' && error.message.includes('Invalid email or password')) {
                try {
                    const regRes = await Api.post('/api/auth/register', {
                        name: 'Sanjay Patil',
                        tractorNumber: 'MH 12 AB 3456',
                        trollyCount: 2,
                        email: 'sanjay.patil@trackbill.in',
                        password: 'Sanjay@123',
                        village: 'Baramati',
                        taluka: 'Baramati',
                        district: 'Pune',
                        pincode: '413102'
                    });

                    if (regRes.success && regRes.data && regRes.data.token) {
                        Api.setToken(regRes.data.token);
                        Api.setOwner(regRes.data.owner);
                        showToast('Demo owner account created and logged in!', 'success');
                        setTimeout(() => {
                            window.location.href = 'tractorhome.html';
                        }, 700);
                        return;
                    }
                } catch (regError) {
                    showError(regError.message || error.message);
                }
            } else {
                showError(error.message || 'An error occurred during login.');
            }
        } finally {
            setLoading(false);
        }
    });

    function showError(msg) {
        loginAlert.textContent = msg;
        loginAlert.classList.remove('d-none');
    }

    function setLoading(isLoading) {
        btnLogin.disabled = isLoading;
        if (isLoading) {
            btnText.classList.add('d-none');
            btnSpinner.classList.remove('d-none');
        } else {
            btnText.classList.remove('d-none');
            btnSpinner.classList.add('d-none');
        }
    }
});
