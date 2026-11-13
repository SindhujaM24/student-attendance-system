/* Shared helpers used by every page */

const DEPARTMENTS = ['CSE', 'IT', 'ECE', 'EEE', 'MECH', 'CIVIL'];
const YEARS = [1, 2, 3, 4];
const SECTIONS = ['A', 'B', 'C', 'D'];
const SEMESTERS = [1, 2, 3, 4, 5, 6, 7, 8];

const ADMIN_MENU = [
    { key: 'dashboard', label: 'Dashboard', href: 'dashboard.html' },
    { key: 'students', label: 'Students', href: 'students.html' },
    { key: 'subjects', label: 'Subjects', href: 'subjects.html' },
    { key: 'attendance', label: 'Attendance', href: 'attendance.html' },
    { key: 'reports', label: 'Reports', href: 'attendance-report.html' }
];

/** Calls the Spring Boot REST API. Returns parsed JSON or throws Error(message). */
async function api(url, options = {}) {
    const config = { method: 'GET', credentials: 'same-origin', headers: {}, ...options };
    if (config.body !== undefined) {
        config.body = JSON.stringify(config.body);
        config.headers['Content-Type'] = 'application/json';
    }

    let response;
    try {
        response = await fetch(url, config);
    } catch (networkError) {
        throw new Error('Cannot connect to the server. Is the Spring Boot application running?');
    }

    let data = null;
    const text = await response.text();
    if (text) {
        try { data = JSON.parse(text); } catch (parseError) { data = null; }
    }

    if (!response.ok) {
        const onLoginPage = window.location.pathname.endsWith('login.html');
        if (response.status === 401 && !onLoginPage) {
            window.location.href = 'login.html';
        }
        throw new Error((data && data.message) || 'Something went wrong. Please try again.');
    }
    return data;
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, ch => (
        { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch]
    ));
}

/** Shows a Bootstrap alert inside <div id="alertBox">. type: success | danger | warning | info */
function showAlert(type, message, boxId = 'alertBox') {
    const box = document.getElementById(boxId);
    if (!box) return;
    box.innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${escapeHtml(message)}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>`;
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

/** A flash message survives one page change (for example: "Student added" after redirect). */
function setFlash(type, message) {
    sessionStorage.setItem('flash', JSON.stringify({ type, message }));
}

function showFlash() {
    const raw = sessionStorage.getItem('flash');
    if (!raw) return;
    sessionStorage.removeItem('flash');
    const flash = JSON.parse(raw);
    showAlert(flash.type, flash.message);
}

/** Fills a <select> with options. Pass values like ['CSE','IT'] or [1,2,3]. */
function fillSelect(selectId, values, placeholder) {
    const select = document.getElementById(selectId);
    const first = placeholder ? `<option value="">${escapeHtml(placeholder)}</option>` : '';
    select.innerHTML = first + values.map(v => `<option value="${escapeHtml(v)}">${escapeHtml(v)}</option>`).join('');
}

function todayString() {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function formatDate(isoDate) {
    return new Date(isoDate + 'T00:00:00').toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function percentBadge(percentage, low) {
    const colour = low ? 'text-bg-danger' : 'text-bg-success';
    return `<span class="badge ${colour}">${low ? '\u26A0 ' : ''}${Number(percentage).toFixed(1)}%</span>`;
}

function statusBadge(status) {
    return status === 'PRESENT'
        ? '<span class="badge text-bg-success">Present</span>'
        : '<span class="badge text-bg-danger">Absent</span>';
}

function renderSidebar(activeKey) {
    const links = ADMIN_MENU.map(item =>
        `<a href="${item.href}" class="${item.key === activeKey ? 'active' : ''}">${item.label}</a>`
    ).join('');

    document.getElementById('sidebar').innerHTML = `
        <div class="sidebar-brand">
            <div class="title">Attendance Manager</div>
            <div class="subtitle">College administration</div>
        </div>
        <nav class="sidebar-nav">
            ${links}
            <a href="#" id="logoutLink">Logout</a>
        </nav>`;
    document.getElementById('logoutLink').addEventListener('click', event => {
        event.preventDefault();
        logout();
    });
}

async function logout() {
    try { await api('/api/auth/logout', { method: 'POST' }); } catch (error) { /* ignore */ }
    window.location.href = 'login.html';
}

/**
 * Call at the start of every protected page.
 * Checks the logged-in user's role, draws the sidebar (admin pages) and shows flash messages.
 * Returns the user, or null if the browser is being redirected.
 */
async function initPage(requiredRole, activeMenu) {
    try {
        const user = await api('/api/auth/me');
        if (user.role !== requiredRole) {
            window.location.href = user.role === 'ADMIN' ? 'dashboard.html' : 'student-dashboard.html';
            return null;
        }
        if (requiredRole === 'ADMIN') renderSidebar(activeMenu);
        const nameBox = document.getElementById('userName');
        if (nameBox) nameBox.textContent = user.fullName;
        showFlash();
        return user;
    } catch (error) {
        window.location.href = 'login.html';
        return null;
    }
}
