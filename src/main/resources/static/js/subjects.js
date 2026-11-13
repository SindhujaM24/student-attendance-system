let subjectModal;
let editingId = null;      // null = adding a new subject
let subjectsById = {};

(async function () {
    const user = await initPage('ADMIN', 'subjects');
    if (!user) return;

    subjectModal = new bootstrap.Modal(document.getElementById('subjectModal'));
    fillSelect('subjectDepartment', DEPARTMENTS, 'Select department');
    fillSelect('subjectSemester', SEMESTERS, 'Select semester');

    document.getElementById('addSubjectBtn').addEventListener('click', () => openModal(null));
    document.getElementById('subjectForm').addEventListener('submit', saveSubject);
    document.getElementById('subjectTableBody').addEventListener('click', onTableClick);
    loadSubjects();
})();

async function loadSubjects() {
    const body = document.getElementById('subjectTableBody');
    try {
        const subjects = await api('/api/subjects');
        subjectsById = {};
        subjects.forEach(s => { subjectsById[s.id] = s; });
        body.innerHTML = subjects.length ? subjects.map(s => `
            <tr>
                <td>${escapeHtml(s.subjectCode)}</td>
                <td>${escapeHtml(s.name)}</td>
                <td>${escapeHtml(s.department)}</td>
                <td>${s.semester}</td>
                <td class="text-nowrap">
                    <button class="btn btn-sm btn-outline-primary" data-action="edit" data-id="${s.id}">Edit</button>
                    <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${s.id}">Delete</button>
                </td>
            </tr>`).join('')
            : '<tr><td colspan="5" class="text-center text-muted py-4">No subjects yet. Click "Add subject" to create one.</td></tr>';
    } catch (error) {
        showAlert('danger', error.message);
    }
}

function openModal(subject) {
    editingId = subject ? subject.id : null;
    document.getElementById('modalTitle').textContent = subject ? 'Edit subject' : 'Add subject';
    document.getElementById('modalAlert').innerHTML = '';
    document.getElementById('subjectCode').value = subject ? subject.subjectCode : '';
    document.getElementById('subjectName').value = subject ? subject.name : '';
    document.getElementById('subjectDepartment').value = subject ? subject.department : '';
    document.getElementById('subjectSemester').value = subject ? subject.semester : '';
    ['subjectCode', 'subjectName', 'subjectDepartment', 'subjectSemester']
        .forEach(id => document.getElementById(id).classList.remove('is-invalid'));
    subjectModal.show();
}

function onTableClick(event) {
    const button = event.target.closest('button[data-id]');
    if (!button) return;
    const subject = subjectsById[button.dataset.id];
    if (button.dataset.action === 'edit') {
        openModal(subject);
    } else {
        deleteSubject(subject);
    }
}

function validate(data) {
    const rules = {
        subjectCode: data.subjectCode ? '' : 'Subject ID is required',
        subjectName: data.name ? '' : 'Subject name is required',
        subjectDepartment: data.department ? '' : 'Select a department',
        subjectSemester: data.semester ? '' : 'Select a semester'
    };
    let valid = true;
    Object.keys(rules).forEach(id => {
        const input = document.getElementById(id);
        input.classList.toggle('is-invalid', rules[id] !== '');
        input.parentElement.querySelector('.invalid-feedback').textContent = rules[id];
        if (rules[id]) valid = false;
    });
    return valid;
}

async function saveSubject(event) {
    event.preventDefault();
    const data = {
        subjectCode: document.getElementById('subjectCode').value.trim(),
        name: document.getElementById('subjectName').value.trim(),
        department: document.getElementById('subjectDepartment').value,
        semester: document.getElementById('subjectSemester').value
    };
    if (!validate(data)) return;
    data.semester = Number(data.semester);

    try {
        await api(editingId ? '/api/subjects/' + editingId : '/api/subjects',
                  { method: editingId ? 'PUT' : 'POST', body: data });
        subjectModal.hide();
        showAlert('success', editingId ? 'Subject updated successfully' : 'Subject added successfully');
        loadSubjects();
    } catch (error) {
        showAlert('danger', error.message, 'modalAlert');   // e.g. duplicate Subject ID
    }
}

async function deleteSubject(subject) {
    if (!confirm(`Delete ${subject.name}? All attendance records of this subject will also be deleted.`)) return;
    try {
        await api('/api/subjects/' + subject.id, { method: 'DELETE' });
        showAlert('success', 'Subject deleted successfully');
        loadSubjects();
    } catch (error) {
        showAlert('danger', error.message);
    }
}
