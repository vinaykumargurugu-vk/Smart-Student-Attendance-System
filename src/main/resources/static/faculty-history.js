
document.addEventListener("DOMContentLoaded", () => {

    const subjectSelect = document.getElementById("historySubject");
    const dateInput = document.getElementById("historyDate");
    const viewButton = document.getElementById("viewHistoryBtn");
    const tableBody = document.getElementById("historyTableBody");
    const message = document.getElementById("historyMessage");

    // Set today's date
    const today = new Date();
    const localDate = new Date(
        today.getTime() - today.getTimezoneOffset() * 60000
    ).toISOString().split("T")[0];

    dateInput.value = localDate;

    // Load faculty assigned subjects
    async function loadHistorySubjects() {

        try {
            const response = await fetch("/api/faculty/subjects", {
                credentials: "same-origin"
            });

            if (!response.ok) {
                throw new Error("Unable to load subjects");
            }

            const subjects = await response.json();

            subjectSelect.innerHTML =
                '<option value="">Select Subject</option>';

            subjects.forEach(subject => {

                const option = document.createElement("option");

                option.value = subject.subject_id;

                option.textContent =
                    subject.subject_code + " - " + subject.subject_name;

                subjectSelect.appendChild(option);
            });

        } catch (error) {
            message.textContent = error.message;
        }
    }

    // Load attendance history
    async function loadAttendanceHistory() {

        const subjectId = subjectSelect.value;
        const attendanceDate = dateInput.value;

        if (!subjectId || !attendanceDate) {
            message.textContent = "Please select a subject and date.";
            return;
        }

        message.textContent = "Loading attendance history...";

        tableBody.innerHTML = "";

        try {

            const url =
                `/api/faculty/attendance/history?subjectId=${encodeURIComponent(subjectId)}&attendanceDate=${encodeURIComponent(attendanceDate)}`;

            const response = await fetch(url, {
                method: "GET",
                credentials: "same-origin"
            });

            if (!response.ok) {
                throw new Error("Failed to load attendance history.");
            }

            const records = await response.json();

            if (records.length === 0) {

                tableBody.innerHTML = `
                    <tr>
                        <td colspan="6">No attendance records found.</td>
                    </tr>
                `;

                message.textContent = "No records available for this date.";
                return;
            }

            records.forEach(record => {

                const row = document.createElement("tr");

                const values = [
                    record.roll_no,
                    record.name,
                    record.subject_code,
                    record.subject_name,
                    record.attendance_date,
                    record.status
                ];

                values.forEach(value => {
                    const cell = document.createElement("td");
                    cell.textContent = value ?? "";
                    row.appendChild(cell);
                });

                tableBody.appendChild(row);
            });

            message.textContent =
                `${records.length} attendance record(s) found.`;

        } catch (error) {

            message.textContent = error.message;

            tableBody.innerHTML = `
                <tr>
                    <td colspan="6">Unable to load attendance records.</td>
                </tr>
            `;
        }
    }

    viewButton.addEventListener("click", loadAttendanceHistory);

    loadHistorySubjects();

});
