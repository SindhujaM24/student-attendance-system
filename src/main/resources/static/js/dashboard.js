(async function () {
    const user = await initPage('ADMIN', 'dashboard');
    if (!user) return;

    try {
        const [stats, lowList] = await Promise.all([
            api('/api/reports/dashboard'),
            api('/api/reports/low-attendance')
        ]);

        document.getElementById('totalStudents').textContent = stats.totalStudents;
        document.getElementById('totalSubjects').textContent = stats.totalSubjects;

        const marked = stats.todayMarked;
        document.getElementById('todayAttendance').textContent = marked === 0 ? 'Not marked' : `${stats.todayPresent} / ${marked}`;
        document.getElementById('todayNote').textContent = marked === 0 ? 'No attendance saved today' : 'Present / records marked today';

        document.getElementById('lowCount').textContent = stats.studentsBelowMinimum;
        document.getElementById('lowLabel').textContent = `Below ${stats.minimumPercentage}% attendance`;

        const body = document.getElementById('lowTableBody');
        if (lowList.length === 0) {
            body.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">All students meet the attendance requirement.</td></tr>';
            return;
        }
        body.innerHTML = lowList.slice(0, 8).map(row => `
            <tr>
                <td>${escapeHtml(row.studentName)} <small class="text-muted">(${escapeHtml(row.studentCode)})</small></td>
                <td>${escapeHtml(row.subjectName)}</td>
                <td>${row.present} / ${row.totalClasses}</td>
                <td>${percentBadge(row.percentage, true)}</td>
            </tr>`).join('');
    } catch (error) {
        showAlert('danger', error.message);
    }
})();
