document.getElementById('loginForm').addEventListener('submit', async (event) => {
    event.preventDefault();

    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const username = usernameInput.value.trim();
    const password = passwordInput.value;
    const role = document.querySelector('input[name="role"]:checked').value;

    // simple validation
    usernameInput.classList.toggle('is-invalid', username === '');
    passwordInput.classList.toggle('is-invalid', password === '');
    if (username === '' || password === '') return;

    const button = document.getElementById('loginBtn');
    button.disabled = true;
    button.textContent = 'Signing in...';
    try {
        const user = await api('/api/auth/login', { method: 'POST', body: { username, password, role } });
        window.location.href = user.role === 'ADMIN' ? 'dashboard.html' : 'student-dashboard.html';
    } catch (error) {
        showAlert('danger', error.message);
    } finally {
        button.disabled = false;
        button.textContent = 'Login';
    }
});
