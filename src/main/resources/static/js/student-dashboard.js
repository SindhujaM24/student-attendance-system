(async function () {
    const user = await initPage('STUDENT', null);
    if (!user) return;
    document.getElementById('logoutBtn').addEventListener('click', logout);

    try {
        const [profile, attendance] = await Promise.all([
            api('/api/me/profile'),
            api('/api/me/attendance')
        ]);
        renderProfile(profile);
        renderOverall(attendance);
        renderSubjects(attendance.subjects);
        renderRecent(attendance.recent);
    } catch (error) {
        showAlert('danger', error.message);
    }
})();

function renderProfile(p) {
    const rows = [
        ['Name', p.name],
        ['Student ID', p.studentCode],
        ['Department', p.department],
        ['Year / Section', `Year ${p.year} - Section ${p.section}`],
        ['Email', p.email],
        ['Phone', p.phone]
    ];
    document.getElementById('profileBox').innerHTML = rows.map(([key, value]) =>
        `<div class="profile-row"><span class="key">${key}</span><span>${escapeHtml(value)}</span></div>`).join('');
}

function renderOverall(a) {
    if (a.totalClasses === 0) {
        document.getElementById('overallPercent').textContent = '-';
        document.getElementById('overallText').textContent = 'No attendance records yet.';
        return;
    }
    const low = a.overallPercentage < a.minimumPercentage;
    document.getElementById('overallPercent').textContent = a.overallPercentage.toFixed(1) + '%';
    document.getElementById('overallText').textContent = `${a.present} present out of ${a.totalClasses} classes (${a.absent} absent)`;

    const bar = document.getElementById('overallBar');
    bar.style.width = Math.min(a.overallPercentage, 100) + '%';
    bar.classList.add(low ? 'bg-danger' : 'bg-success');

    const lowSubjects = a.subjects.filter(s => s.low).map(s => s.subjectName);
    let message = '';
    if (low) {
        message = `Your overall attendance is below ${a.minimumPercentage}%.`;
    } else if (lowSubjects.length) {
        message = `Overall attendance is fine, but these subjects are below ${a.minimumPercentage}%: ${lowSubjects.join(', ')}.`;
    }
    if (message) {
        document.getElementById('warningBox').innerHTML = `<div class="alert alert-warning mb-0">\u26A0 ${escapeHtml(message)}</div>`;
    }
}

function renderSubjects(subjects) {
    document.getElementById('subjectBody').innerHTML = subjects.length ? subjects.map(s => `
        <tr class="${s.low ? 'table-danger' : ''}">
            <td>${escapeHtml(s.subjectName)}</td>
            <td>${s.totalClasses}</td>
            <td>${s.present}</td>
            <td>${s.absent}</td>
            <td>${percentBadge(s.percentage, s.low)}</td>
        </tr>`).join('')
        : '<tr><td colspan="5" class="text-center text-muted py-4">No attendance has been marked yet.</td></tr>';
}

function renderRecent(records) {
    document.getElementById('recentBody').innerHTML = records.length ? records.map(r => `
        <tr>
            <td>${formatDate(r.date)}</td>
            <td>${escapeHtml(r.subjectName)}</td>
            <td>${statusBadge(r.status)}</td>
        </tr>`).join('')
        : '<tr><td colspan="3" class="text-center text-muted py-4">No records yet.</td></tr>';
}
