let searchTimer;

(async function () {
    const user = await initPage('ADMIN', 'reports');
    if (!user) return;

    try {
        const subjects = await api('/api/subjects');
        document.getElementById('subjectFilter').innerHTML = '<option value="">All subjects</option>' +
            subjects.map(s => `<option value="${s.id}">${escapeHtml(s.name)}</option>`).join('');
    } catch (error) {
        showAlert('danger', error.message);
    }

    document.getElementById('subjectFilter').addEventListener('change', loadReport);
    document.getElementById('searchInput').addEventListener('input', () => {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(loadReport, 300);
    });

    loadReport();
    loadLowAttendance();
})();

async function loadReport() {
    const params = new URLSearchParams();
    const subjectId = document.getElementById('subjectFilter').value;
    const search = document.getElementById('searchInput').value.trim();
    if (subjectId) params.set('subjectId', subjectId);
    if (search) params.set('search', search);

    try {
        const rows = await api('/api/reports/attendance?' + params.toString());
        document.getElementById('rowCount').textContent = rows.length + ' record(s)';
        document.getElementById('reportBody').innerHTML = rows.length ? rows.map(row => `
            <tr class="${row.low ? 'table-danger' : ''}">
                <td>${escapeHtml(row.studentCode)}</td>
                <td>${escapeHtml(row.studentName)}</td>
                <td>${escapeHtml(row.subjectName)}</td>
                <td>${row.totalClasses}</td>
                <td>${row.present}</td>
                <td>${row.absent}</td>
                <td>${percentBadge(row.percentage, row.low)}</td>
            </tr>`).join('')
            : '<tr><td colspan="7" class="text-center text-muted py-4">No attendance records found. Mark attendance first.</td></tr>';
    } catch (error) {
        showAlert('danger', error.message);
    }
}

async function loadLowAttendance() {
    try {
        const rows = await api('/api/reports/low-attendance');
        document.getElementById('lowBody').innerHTML = rows.length ? rows.map(row => `
            <tr>
                <td>${escapeHtml(row.studentName)} <small class="text-muted">(${escapeHtml(row.studentCode)})</small></td>
                <td>${escapeHtml(row.subjectName)}</td>
                <td>${row.present} / ${row.totalClasses}</td>
                <td>${percentBadge(row.percentage, true)}</td>
            </tr>`).join('')
            : '<tr><td colspan="4" class="text-center text-muted py-4">No student is below the attendance requirement.</td></tr>';
    } catch (error) {
        showAlert('danger', error.message);
    }
}
