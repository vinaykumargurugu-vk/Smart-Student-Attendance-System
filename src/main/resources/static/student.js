
document.addEventListener("DOMContentLoaded", () => {

    loadStudentProfile();
    loadStudentAttendance();
    loadStudentPerformance();
    loadStudentMarks();
    loadSubjectPerformanceChart();

    document.getElementById("logoutBtn")
        .addEventListener("click", logout);

});

// Helper function
function displayValue(value) {

    return value === null || value === undefined
        ? "-"
        : value;

}

// 1. Load Student Profile
async function loadStudentProfile() {

    const profileDiv = document.getElementById("profile");

    try {

        const response = await fetch("/api/student/me", {
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("Unable to load student profile");
        }

        const student = await response.json();

        profileDiv.replaceChildren();

        const fields = [
            ["Student ID", student.student_id],
            ["Roll Number", student.roll_no],
            ["Name", student.name],
            ["Email", student.email],
            ["Department", student.department],
            ["Year", student.year_of_study]
        ];

        fields.forEach(([label, value]) => {

            const paragraph = document.createElement("p");

            const strong = document.createElement("strong");

            strong.textContent = label + ": ";

            paragraph.append(
                strong,
                document.createTextNode(displayValue(value))
            );

            profileDiv.appendChild(paragraph);

        });

    } catch (error) {

        profileDiv.textContent = error.message;

    }

}

// 2. Load Student Attendance
async function loadStudentAttendance() {

    const attendanceDiv = document.getElementById("attendance");

    try {

        const response = await fetch("/api/student/attendance", {
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("Unable to load attendance");
        }

        const records = await response.json();

        if (records.length === 0) {

            attendanceDiv.textContent =
                "No attendance records found.";

            return;

        }

        const table = document.createElement("table");

        const thead = document.createElement("thead");

        const headerRow = document.createElement("tr");

        [
            "Subject Code",
            "Subject Name",
            "Total Classes",
            "Present",
            "Attendance %"
        ].forEach(header => {

            const th = document.createElement("th");

            th.textContent = header;

            headerRow.appendChild(th);

        });

        thead.appendChild(headerRow);

        const tbody = document.createElement("tbody");

        records.forEach(record => {

            const row = document.createElement("tr");

            const values = [
                record.subject_code,
                record.subject_name,
                record.total_classes,
                record.present_classes ?? 0,
                (record.attendance_percentage ?? 0) + "%"
            ];

            values.forEach(value => {

                const td = document.createElement("td");

                td.textContent = displayValue(value);

                row.appendChild(td);

            });

            tbody.appendChild(row);

        });

        table.append(thead, tbody);

        attendanceDiv.replaceChildren(table);

    } catch (error) {

        attendanceDiv.textContent = error.message;

    }

}

// 3. Load Student Overall Performance
async function loadStudentPerformance() {

    const message = document.getElementById("performanceMessage");

    try {

        const response = await fetch("/api/student/performance", {
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("Unable to load academic performance");
        }

        const performance = await response.json();

        const totalExams = Number(performance.total_exams) || 0;

        const obtainedMarks =
            Number(performance.total_obtained_marks) || 0;

        const maxMarks =
            Number(performance.total_max_marks) || 0;

        const percentage =
            Number(performance.overall_percentage) || 0;

        const safePercentage = Math.min(
            100,
            Math.max(0, percentage)
        );

        // Update Performance Cards
        document.getElementById("totalExams").textContent =
            totalExams;

        document.getElementById("totalObtainedMarks").textContent =
            obtainedMarks;

        document.getElementById("totalMaxMarks").textContent =
            maxMarks;

        document.getElementById("overallPercentage").textContent =
            percentage.toFixed(2) + "%";

        // Update Progress Bar
        document.getElementById("progressPercentage").textContent =
            percentage.toFixed(2) + "%";

        document.getElementById("performanceProgress").style.width =
            safePercentage + "%";

        // Calculate Academic Status
        let status;
        let remark;

        if (totalExams === 0 || maxMarks === 0) {

            status = "No Results Available";

            remark =
                "Your exam results have not been published yet.";

        } else if (percentage >= 90) {

            status = "Excellent Performance";

            remark =
                "Outstanding work! Keep maintaining your performance.";

        } else if (percentage >= 75) {

            status = "Very Good Performance";

            remark =
                "Great job! Continue improving your skills.";

        } else if (percentage >= 60) {

            status = "Good Performance";

            remark =
                "Good progress. Keep working towards higher marks.";

        } else if (percentage >= 40) {

            status = "Average Performance";

            remark =
                "You can improve with consistent practice.";

        } else {

            status = "Needs Improvement";

            remark =
                "Focus on your subjects and practice regularly.";

        }

        document.getElementById("performanceStatus").textContent =
            status;

        document.getElementById("performanceRemark").textContent =
            remark;

        message.textContent = "";

    } catch (error) {

        message.textContent = error.message;

    }

}

// 4. Load Student Exam-wise Marks
async function loadStudentMarks() {

    const tableBody = document.getElementById("marksTableBody");

    const message = document.getElementById("marksMessage");

    try {

        const response = await fetch("/api/student/marks", {
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("Unable to load exam marks");
        }

        const records = await response.json();

        tableBody.replaceChildren();

        if (records.length === 0) {

            const row = document.createElement("tr");

            const cell = document.createElement("td");

            cell.colSpan = 6;

            cell.textContent = "No exam marks found.";

            row.appendChild(cell);

            tableBody.appendChild(row);

            message.textContent = "";

            return;

        }

        records.forEach(record => {

            const row = document.createElement("tr");

            const values = [
                record.subject_code,
                record.subject_name,
                record.exam_name,
                record.max_marks,
                record.obtained_marks,
                (record.percentage ?? 0) + "%"
            ];

            values.forEach(value => {

                const cell = document.createElement("td");

                cell.textContent = displayValue(value);

                row.appendChild(cell);

            });

            tableBody.appendChild(row);

        });

        message.textContent = "";

    } catch (error) {

        message.textContent = error.message;

        tableBody.replaceChildren();

        const row = document.createElement("tr");

        const cell = document.createElement("td");

        cell.colSpan = 6;

        cell.textContent = "Unable to display marks.";

        row.appendChild(cell);

        tableBody.appendChild(row);

    }

}

// 5. Load Subject-wise Performance Chart
async function loadSubjectPerformanceChart() {

    const chartDiv = document.getElementById(
        "subjectPerformanceChart"
    );

    const message = document.getElementById(
        "subjectChartMessage"
    );

    try {

        const response = await fetch("/api/student/marks", {
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("Unable to load subject performance");
        }

        const records = await response.json();

        chartDiv.replaceChildren();

        if (records.length === 0) {

            chartDiv.textContent =
                "No subject performance data available.";

            message.textContent = "";

            return;

        }

        // Group marks by subject
        const subjectData = {};

        records.forEach(record => {

            const subjectCode = record.subject_code;

            const subjectName = record.subject_name;

            if (!subjectData[subjectCode]) {

                subjectData[subjectCode] = {

                    name: subjectName,

                    obtainedMarks: 0,

                    maxMarks: 0

                };

            }

            subjectData[subjectCode].obtainedMarks +=
                Number(record.obtained_marks) || 0;

            subjectData[subjectCode].maxMarks +=
                Number(record.max_marks) || 0;

        });

        // Create subject performance bars
        Object.values(subjectData).forEach(subject => {

            const percentage = subject.maxMarks > 0
                ? (subject.obtainedMarks / subject.maxMarks) * 100
                : 0;

            const safePercentage = Math.min(
                100,
                Math.max(0, percentage)
            );

            // Main Container
            const subjectContainer =
                document.createElement("div");

            subjectContainer.className = "subject-chart-item";

            // Subject Information
            const info = document.createElement("div");

            info.className = "subject-chart-info";

            const name = document.createElement("span");

            name.textContent = subject.name;

            const score = document.createElement("strong");

            score.textContent = percentage.toFixed(2) + "%";

            info.append(name, score);

            // Progress Bar
            const progressBar =
                document.createElement("div");

            progressBar.className = "subject-progress-bar";

            const progressFill =
                document.createElement("div");

            progressFill.className = "subject-progress-fill";

            progressFill.style.width =
                safePercentage + "%";

            progressBar.appendChild(progressFill);

            // Marks Details
            const details = document.createElement("p");

            details.className = "subject-chart-details";

            details.textContent =
                `${subject.obtainedMarks} / ${subject.maxMarks} marks`;

            subjectContainer.append(
                info,
                progressBar,
                details
            );

            chartDiv.appendChild(subjectContainer);

        });

        message.textContent = "";

    } catch (error) {

        chartDiv.textContent =
            "Unable to display subject performance.";

        message.textContent = error.message;

    }

}

// 6. Student Logout
async function logout() {

    try {

        const response = await fetch("/api/auth/logout", {

            method: "POST",

            credentials: "same-origin"

        });

        if (!response.ok) {
            throw new Error("Logout failed");
        }

        window.location.href = "login.html";

    } catch (error) {

        alert(error.message);

    }

}
