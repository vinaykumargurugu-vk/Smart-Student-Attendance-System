
// 1. Get Total Students
async function loadTotalStudents() {
    try {
        const response = await fetch("/api/dashboard/total-students");
        const data = await response.json();

        document.getElementById("totalStudents").textContent = data;
    } catch (error) {
        document.getElementById("totalStudents").textContent = "Error";
        console.error(error);
    }
}

// 2. Get Average Attendance
async function loadAverageAttendance() {
    try {
        const response = await fetch("/api/dashboard/average-attendance");
        const data = await response.json();

        document.getElementById("averageAttendance").textContent = data + "%";
    } catch (error) {
        document.getElementById("averageAttendance").textContent = "Error";
        console.error(error);
    }
}

// 3. Get Average Performance
async function loadAveragePerformance() {
    try {
        const response = await fetch("/api/dashboard/average-performance");
        const data = await response.json();

        document.getElementById("averagePerformance").textContent = data + "%";
    } catch (error) {
        document.getElementById("averagePerformance").textContent = "Error";
        console.error(error);
    }
}

// 4. Get Top 5 Performers
async function loadTopPerformers() {
    try {
        const response = await fetch("/api/dashboard/top-performers");
        const students = await response.json();

        const table = document.getElementById("topPerformers");
        table.innerHTML = "";

        if (students.length === 0) {
            table.innerHTML = "<tr><td colspan='3'>No records found</td></tr>";
            return;
        }

        students.forEach(student => {
            const row = `
                <tr>
                    <td>${student.roll_no}</td>
                    <td>${student.name}</td>
                    <td>${student.percentage}%</td>
                </tr>
            `;

            table.innerHTML += row;
        });

    } catch (error) {
        console.error(error);
        document.getElementById("topPerformers").innerHTML =
            "<tr><td colspan='3'>Unable to load data</td></tr>";
    }
}

// 5. Get Low Attendance Alerts
async function loadAttendanceAlerts() {
    try {
        const response = await fetch("/api/dashboard/low-attendance");
        const students = await response.json();

        const container = document.getElementById("attendanceAlerts");
        container.innerHTML = "";

        if (students.length === 0) {
            container.textContent = "No low attendance alerts.";
            return;
        }

        students.forEach(student => {
            container.innerHTML += `
                <p>${student.name} (${student.roll_no})
                - ${student.attendance_percentage}%</p>
            `;
        });

    } catch (error) {
        console.error(error);
        document.getElementById("attendanceAlerts").textContent =
            "Unable to load alerts.";
    }
}

// 6. Get Academic Alerts
async function loadAcademicAlerts() {
    try {
        const response = await fetch("/api/dashboard/academic-alerts");
        const students = await response.json();

        const container = document.getElementById("academicAlerts");
        container.innerHTML = "";

        if (students.length === 0) {
            container.textContent = "No academic alerts.";
            return;
        }

        students.forEach(student => {
            container.innerHTML += `
                <p>${student.name} (${student.roll_no})
                - ${student.percentage}%</p>
            `;
        });

    } catch (error) {
        console.error(error);
        document.getElementById("academicAlerts").textContent =
            "Unable to load alerts.";
    }
}

// 7. Get Total Attendance Records
async function loadTotalAttendance() {
    try {
        const response = await fetch("/api/dashboard/total-attendance-records");
        const data = await response.json();

        document.getElementById("totalAttendance").textContent = data;
    } catch (error) {
        document.getElementById("totalAttendance").textContent = "Error";
        console.error(error);
    }
}

// Load all dashboard information
function loadDashboard() {
    loadTotalStudents();
    loadAverageAttendance();
    loadAveragePerformance();
    loadTopPerformers();
    loadAttendanceAlerts();
    loadAcademicAlerts();
    loadTotalAttendance();
}

// Run when the page loads
document.addEventListener("DOMContentLoaded", loadDashboard);
