
document.addEventListener("DOMContentLoaded", () => {

    const subjectSelect =
        document.getElementById("summarySubject");

    const refreshButton =
        document.getElementById("refreshSummaryBtn");

    const message =
        document.getElementById("summaryMessage");

    const totalStudents =
        document.getElementById("totalStudents");

    const presentStudents =
        document.getElementById("presentStudents");

    const absentStudents =
        document.getElementById("absentStudents");

    const attendancePercentage =
        document.getElementById("attendancePercentage");


    // 1. Load faculty assigned subjects
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

                loadSummary();
            } else {
                message.textContent =
                    "No subjects are assigned to you.";
            }

        } catch (error) {

            message.textContent = error.message;

        }
    }


    // 2. Load attendance summary
    async function loadSummary() {

        const subjectId = subjectSelect.value;

        if (!subjectId) {
            message.textContent =
                "Please select a subject.";
            return;
        }

        message.textContent =
            "Loading attendance summary...";

        refreshButton.disabled = true;

        try {

            const response = await fetch(
                `/api/faculty/attendance/summary?subjectId=${encodeURIComponent(subjectId)}`,
                { credentials: "same-origin" }
            );

            if (!response.ok) {
                throw new Error(
                    "Unable to load attendance summary."
                );
            }

            const data = await response.json();

            totalStudents.textContent =
                data.total_students ?? 0;

            presentStudents.textContent =
                data.present_students ?? 0;

            absentStudents.textContent =
                data.absent_students ?? 0;

            attendancePercentage.textContent =
                Number(data.attendance_percentage ?? 0)
                    .toFixed(2) + "%";

            message.textContent =
                "Attendance summary loaded successfully.";

        } catch (error) {

            message.textContent = error.message;

        } finally {

            refreshButton.disabled = false;

        }
    }


    // 3. Refresh button
    refreshButton.addEventListener(
        "click",
        loadSummary
    );

    // 4. Load summary when subject changes
    subjectSelect.addEventListener(
        "change",
        loadSummary
    );

    // 5. Initial loading
    loadSubjects();

});
