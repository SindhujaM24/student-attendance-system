const editId = new URLSearchParams(window.location.search).get('id');
const FIELDS = ['studentCode', 'name', 'email', 'phone', 'department', 'year', 'section'];

(async function () {
    const user = await initPage('ADMIN', 'students');
    if (!user) return;

    fillSelect('department', DEPARTMENTS, 'Select department');
    fillSelect('year', YEARS, 'Select year');
    fillSelect('section', SECTIONS, 'Select section');

    if (editId) {
        document.getElementById('formTitle').textContent = 'Edit student';
        document.getElementById('submitBtn').textContent = 'Update student';
        document.getElementById('passwordNote').classList.add('d-none');
        try {
            const student = await api('/api/students/' + editId);
            FIELDS.forEach(field => { document.getElementById(field).value = student[field]; });
        } catch (error) {
            showAlert('danger', error.message);
        }
    }
    document.getElementById('studentForm').addEventListener('submit', saveStudent);
})();

function readForm() {
    const data = {};
    FIELDS.forEach(field => { data[field] = document.getElementById(field).value.trim(); });
    return data;
}

/** Returns true when all fields are valid. Shows a message under each invalid field. */
function validateForm(data) {
    const rules = {
        studentCode: v => v ? '' : 'Student ID is required',
        name: v => v ? '' : 'Name is required',
        email: v => !v ? 'Email is required' : (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) ? '' : 'Enter a valid email address'),
        phone: v => !v ? 'Phone number is required' : (/^[6-9]\d{9}$/.test(v) ? '' : 'Enter a valid 10-digit mobile number'),
        department: v => v ? '' : 'Select a department',
        year: v => v ? '' : 'Select a year',
        section: v => v ? '' : 'Select a section'
    };
    let valid = true;
    FIELDS.forEach(field => {
        const input = document.getElementById(field);
        const message = rules[field](data[field]);
        input.classList.toggle('is-invalid', message !== '');
        input.parentElement.querySelector('.invalid-feedback').textContent = message;
        if (message) valid = false;
    });
    return valid;
}

async function saveStudent(event) {
    event.preventDefault();
    const data = readForm();
    if (!validateForm(data)) return;
    data.year = Number(data.year);

    const button = document.getElementById('submitBtn');
    button.disabled = true;
    try {
        await api(editId ? '/api/students/' + editId : '/api/students',
                  { method: editId ? 'PUT' : 'POST', body: data });
        setFlash('success', editId ? 'Student updated successfully' : 'Student added successfully');
        window.location.href = 'students.html';
    } catch (error) {
        showAlert('danger', error.message);   // e.g. duplicate Student ID / email
        button.disabled = false;
    }
}
