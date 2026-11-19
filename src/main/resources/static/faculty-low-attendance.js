
document.addEventListener("DOMContentLoaded", () => {

    const subjectSelect =
        document.getElementById("lowAttendanceSubject");

    const refreshButton =
        document.getElementById("refreshLowAttendanceBtn");

    const message =
        document.getElementById("lowAttendanceMessage");

    const tableBody =
        document.getElementById("lowAttendanceTableBody");


    // 1. Load assigned subjects
    async function loadSubjects() {

        try {

            const response = await fetch(
                "/api/faculty/subjects",
                { credentials: "same-origin" }
            );

            if (!response.ok) {
                throw new Error("Unable to load subjects.");
            }

            const subjects = await response.json();

            subjectSelect.innerHTML =
                '<option value="">Select Subject</option>';

            subjects.forEach(subject => {

                const option = document.createElement("option");

                option.value = subject.subject_id;

                option.textContent =
                    subject.subject_code + " - " +
                    subject.subject_name;

                subjectSelect.appendChild(option);

            });

            if (subjects.length > 0) {

                subjectSelect.value =
                    String(subjects[0].subject_id);

                loadLowAttendance();

            } else {

                message.textContent =
                    "No subjects assigned.";

            }

        } catch (error) {

            message.textContent = error.message;

        }
    }


    // 2. Load low attendance students
    async function loadLowAttendance() {

        const subjectId = subjectSelect.value;

        if (!subjectId) {

            message.textContent =
                "Please select a subject.";

            return;
        }

        message.textContent =
            "Loading low attendance report...";

        tableBody.innerHTML = "";

        refreshButton.disabled = true;

        try {

            const response = await fetch(
                `/api/faculty/attendance/low-attendance?subjectId=${encodeURIComponent(subjectId)}`,
                { credentials: "same-origin" }
            );

            if (!response.ok) {
                throw new Error(
                    "Unable to load low attendance report."
                );
            }

            const students = await response.json();

            if (students.length === 0) {

                tableBody.innerHTML = `
                    <tr>
                        <td colspan="7">
                            No students have attendance below 75%.
                        </td>
                    </tr>
                `;

                message.textContent =
                    "No low attendance students found.";

                return;
            }

            students.forEach(student => {

                const row = document.createElement("tr");

                const values = [
                    student.roll_no,
                    student.name,
                    student.total_classes,
                    student.classes_attended,
                    student.classes_missed,
                    Number(student.attendance_percentage)
                        .toFixed(2) + "%"
                ];

                values.forEach(value => {

                    const cell = document.createElement("td");

                    cell.textContent = value ?? "";

                    row.appendChild(cell);

                });

                // Attendance warning
                const statusCell = document.createElement("td");

                const badge = document.createElement("span");

                badge.className = "low-attendance-badge";
                badge.textContent = "Below 75%";

                statusCell.appendChild(badge);

                row.appendChild(statusCell);

                tableBody.appendChild(row);

            });

            message.textContent =
                `${students.length} student(s) require attendance improvement.`;

        } catch (error) {

            message.textContent = error.message;

            tableBody.innerHTML = `
                <tr>
                    <td colspan="7">
                        Unable to load attendance report.
                    </td>
                </tr>
            `;

        } finally {

            refreshButton.disabled = false;

        }
    }


    // 3. Refresh report
    refreshButton.addEventListener(
        "click",
        loadLowAttendance
    );


    // 4. Change subject
    subjectSelect.addEventListener(
        "change",
        loadLowAttendance
    );


    // 5. Initial loading
    loadSubjects();

});
