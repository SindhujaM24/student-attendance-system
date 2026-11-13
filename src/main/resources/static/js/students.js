let searchTimer;

(async function () {
    const user = await initPage('ADMIN', 'students');
    if (!user) return;

    document.getElementById('searchInput').addEventListener('input', () => {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(loadStudents, 300);   // wait until the user stops typing
    });
    document.getElementById('searchForm').addEventListener('submit', event => {
        event.preventDefault();
        loadStudents();
    });
    document.getElementById('studentTableBody').addEventListener('click', onTableClick);

    loadStudents();
})();

async function loadStudents() {
    const keyword = document.getElementById('searchInput').value.trim();
    const body = document.getElementById('studentTableBody');
    try {
        const students = await api('/api/students?search=' + encodeURIComponent(keyword));
        document.getElementById('studentCount').textContent = students.length + ' student(s)';
        body.innerHTML = students.length
            ? students.map(rowHtml).join('')
            : '<tr><td colspan="8" class="text-center text-muted py-4">No students found.</td></tr>';
    } catch (error) {
        showAlert('danger', error.message);
    }
}

function rowHtml(s) {
    return `
        <tr>
            <td>${escapeHtml(s.studentCode)}</td>
            <td>${escapeHtml(s.name)}</td>
            <td>${escapeHtml(s.email)}</td>
            <td>${escapeHtml(s.phone)}</td>
            <td>${escapeHtml(s.department)}</td>
            <td>${s.year}</td>
            <td>${escapeHtml(s.section)}</td>
            <td class="text-nowrap">
                <a class="btn btn-sm btn-outline-primary" href="add-student.html?id=${s.id}">Edit</a>
                <button class="btn btn-sm btn-outline-danger" data-id="${s.id}" data-name="${escapeHtml(s.name)}">Delete</button>
            </td>
        </tr>`;
}

async function onTableClick(event) {
    const button = event.target.closest('button[data-id]');
    if (!button) return;

    const name = button.dataset.name;
    if (!confirm(`Delete ${name}? Their attendance records and login will also be deleted.`)) return;

    try {
        await api('/api/students/' + button.dataset.id, { method: 'DELETE' });
        showAlert('success', `${name} was deleted successfully`);
        loadStudents();
    } catch (error) {
        showAlert('danger', error.message);
    }
}
