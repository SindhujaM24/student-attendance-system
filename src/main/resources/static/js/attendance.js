let loaded = null;   // { subjectId, date, rows } - what is currently on screen

(async function () {
    const user = await initPage('ADMIN', 'attendance');
    if (!user) return;

    const dateInput = document.getElementById('date');
    dateInput.value = todayString();
    dateInput.max = todayString();          // future dates are not allowed

    fillSelect('year', YEARS, 'Year');
    fillSelect('section', SECTIONS, 'Sec');

    try {
        const subjects = await api('/api/subjects');
        const select = document.getElementById('subject');
        select.innerHTML = '<option value="">Select subject</option>' + subjects.map(s =>
            `<option value="${s.id}">${escapeHtml(s.subjectCode)} - ${escapeHtml(s.name)} (${escapeHtml(s.department)})</option>`).join('');
    } catch (error) {
        showAlert('danger', error.message);
    }

    document.getElementById('filterForm').addEventListener('submit', loadSheet);
    document.getElementById('saveBtn').addEventListener('click', saveAttendance);
    document.getElementById('allPresentBtn').addEventListener('click', () => setAll('PRESENT'));
    document.getElementById('allAbsentBtn').addEventListener('click', () => setAll('ABSENT'));
    document.getElementById('sheetBody').addEventListener('change', updateCount);
})();

async function loadSheet(event) {
    event.preventDefault();
    const date = document.getElementById('date').value;
    const subjectSelect = document.getElementById('subject');
    const subjectId = subjectSelect.value;
    const year = document.getElementById('year').value;
    const section = document.getElementById('section').value;

    if (!date || !subjectId || !year || !section) {
        showAlert('warning', 'Please select date, subject, year and section.');
        return;
    }

    try {
        const rows = await api(`/api/attendance/sheet?subjectId=${subjectId}&date=${date}&year=${year}&section=${section}`);
        if (rows.length === 0) {
            document.getElementById('sheetCard').classList.add('d-none');
            loaded = null;
            showAlert('warning', 'No students found for this subject, year and section.');
            return;
        }
        loaded = { subjectId: Number(subjectId), date, rows };
        const subjectText = subjectSelect.options[subjectSelect.selectedIndex].text;
        document.getElementById('sheetTitle').textContent = `${subjectText} | Year ${year} - Section ${section} | ${formatDate(date)}`;
        document.getElementById('alertBox').innerHTML = '';
        renderSheet();
    } catch (error) {
        showAlert('danger', error.message);
    }
}

function renderSheet() {
    // students not marked yet start as Present (teacher only changes the absentees)
    document.getElementById('sheetBody').innerHTML = loaded.rows.map(row => {
        const id = row.studentId;
        const absent = row.status === 'ABSENT';
        return `
            <tr>
                <td>${escapeHtml(row.studentCode)}</td>
                <td>${escapeHtml(row.studentName)}</td>
                <td>
                    <input type="radio" class="btn-check" name="status-${id}" id="present-${id}" value="PRESENT" ${absent ? '' : 'checked'}>
                    <label class="btn btn-sm btn-outline-success" for="present-${id}">Present</label>
                    <input type="radio" class="btn-check" name="status-${id}" id="absent-${id}" value="ABSENT" ${absent ? 'checked' : ''}>
                    <label class="btn btn-sm btn-outline-danger" for="absent-${id}">Absent</label>
                </td>
            </tr>`;
    }).join('');

    const alreadySaved = loaded.rows.some(row => row.status !== null);
    document.getElementById('editNotice').classList.toggle('d-none', !alreadySaved);
    document.getElementById('sheetCard').classList.remove('d-none');
    updateCount();
}

function setAll(status) {
    document.querySelectorAll(`#sheetBody input[value="${status}"]`).forEach(radio => { radio.checked = true; });
    updateCount();
}

function updateCount() {
    if (!loaded) return;
    const absent = document.querySelectorAll('#sheetBody input[value="ABSENT"]:checked').length;
    const total = loaded.rows.length;
    document.getElementById('countText').textContent = `${total - absent} present, ${absent} absent (total ${total})`;
}

async function saveAttendance() {
    if (!loaded || loaded.rows.length === 0) {
        showAlert('danger', 'No students loaded. Load a class before saving.');
        return;
    }
    const records = loaded.rows.map(row => ({
        studentId: row.studentId,
        status: document.querySelector(`input[name="status-${row.studentId}"]:checked`).value
    }));

    const button = document.getElementById('saveBtn');
    button.disabled = true;
    try {
        const result = await api('/api/attendance', {
            method: 'POST',
            body: { subjectId: loaded.subjectId, date: loaded.date, records }
        });
        showAlert('success', result.message);
        loaded.rows = loaded.rows.map((row, i) => ({ ...row, status: records[i].status }));
        document.getElementById('editNotice').classList.remove('d-none');
    } catch (error) {
        showAlert('danger', error.message);
    } finally {
        button.disabled = false;
    }
}
