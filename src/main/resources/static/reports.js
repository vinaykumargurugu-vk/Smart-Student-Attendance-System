
// ==========================================
// REPORTS ANALYTICS - GLOBAL DATA
// ==========================================

let studentReportData = [];
let attendanceReportData = [];
let marksReportData = [];
let subjectReportData = [];

let reportAttendanceChart = null;
let reportMarksChart = null;
let reportSubjectChart = null;


// ==========================================
// COMMON API FUNCTION
// ==========================================

async function fetchReport(url) {

    const response = await fetch(url, {
        method: "GET",
        credentials: "same-origin"
    });

    if (!response.ok) {
        throw new Error(
            `Request failed: ${response.status} ${response.statusText}`
        );
    }

    return await response.json();
}


// ==========================================
// COMMON TABLE DISPLAY FUNCTION
// ==========================================

function displayReport(data, tableId, columns) {

    const tableBody = document.getElementById(tableId);

    if (!tableBody) {
        console.error("Table not found:", tableId);
        return;
    }

    tableBody.innerHTML = "";

    if (!Array.isArray(data) || data.length === 0) {

        tableBody.innerHTML = `
            <tr>
                <td colspan="${columns.length}">
                    No records found!
                </td>
            </tr>
        `;

        return;
    }

    data.forEach(record => {

        const row = document.createElement("tr");

        columns.forEach(column => {

            const cell = document.createElement("td");

            cell.textContent = record[column] ?? "-";

            row.appendChild(cell);
        });

        tableBody.appendChild(row);
    });
}


// ==========================================
// 1. STUDENT ACADEMIC REPORT
// ==========================================

async function loadStudentReport() {

    const rollNo = document
        .getElementById("reportRollNo")
        .value.trim();

    if (!rollNo) {
        alert("Please enter student roll number!");
        return;
    }

    try {

        const data = await fetchReport(
            `/api/reports/student/${encodeURIComponent(rollNo)}`
        );

        if (!Array.isArray(data)) {
            throw new Error("Invalid student report response");
        }

        studentReportData = data;

        displayReport(data, "studentReportTable", [
            "roll_no",
            "name",
            "department",
            "total_classes",
            "present_classes",
            "attendance_percentage",
            "total_obtained",
            "total_maximum",
            "marks_percentage"
        ]);

        if (data.length === 0) {
            alert("No report found for this student!");
        }

    } catch (error) {

        console.error("Student report error:", error);

        alert("Unable to load student report!");
    }
}


// ==========================================
// 2. ATTENDANCE SUMMARY
// ==========================================

async function loadAttendanceSummary() {

    try {

        const data = await fetchReport(
            "/api/reports/attendance-summary"
        );

        attendanceReportData = data;

        displayReport(data, "attendanceReportTable", [
            "roll_no",
            "name",
            "subject_name",
            "total_classes",
            "present_classes",
            "attendance_percentage"
        ]);

    } catch (error) {

        console.error("Attendance summary error:", error);

        alert("Unable to load attendance summary!");
    }
}


// ==========================================
// 3. MARKS SUMMARY
// ==========================================

async function loadMarksSummary() {

    try {

        const data = await fetchReport(
            "/api/reports/marks-summary"
        );

        marksReportData = data;

        displayReport(data, "marksReportTable", [
            "roll_no",
            "name",
            "total_obtained",
            "total_maximum",
            "percentage"
        ]);

    } catch (error) {

        console.error("Marks summary error:", error);

        alert("Unable to load marks summary!");
    }
}


// ==========================================
// 4. SUBJECT-WISE SUMMARY
// ==========================================

async function loadSubjectSummary() {

    try {

        const data = await fetchReport(
            "/api/reports/subject-summary"
        );

        subjectReportData = data;

        displayReport(data, "subjectReportTable", [
            "subject_code",
            "subject_name",
            "total_marks_records",
            "average_percentage"
        ]);

    } catch (error) {

        console.error("Subject summary error:", error);

        alert("Unable to load subject summary!");
    }
}


// ==========================================
// 5. DOWNLOAD CSV
// ==========================================

function downloadCSV(type) {

    let data = [];
    let fileName = "report.csv";

    if (type === "student") {

        data = studentReportData;
        fileName = "student-report.csv";

    } else if (type === "attendance") {

        data = attendanceReportData;
        fileName = "attendance-report.csv";

    } else if (type === "marks") {

        data = marksReportData;
        fileName = "marks-report.csv";

    } else if (type === "subject") {

        data = subjectReportData;
        fileName = "subject-report.csv";
    }

    if (!data || data.length === 0) {

        alert("Please generate the report before downloading!");

        return;
    }

    const headers = Object.keys(data[0]);

    const csvRows = [

        headers.map(value => `"${value}"`).join(","),

        ...data.map(record => {

            return headers.map(header => {

                const value = record[header] ?? "";

                return `"${String(value).replace(/"/g, '""')}"`;

            }).join(",");
        })
    ];

    const csvContent = "\uFEFF" + csvRows.join("\n");

    const blob = new Blob([csvContent], {
        type: "text/csv;charset=utf-8;"
    });

    const url = URL.createObjectURL(blob);

    const link = document.createElement("a");

    link.href = url;
    link.download = fileName;

    document.body.appendChild(link);

    link.click();

    document.body.removeChild(link);

    URL.revokeObjectURL(url);
}


// ==========================================
// 6. DOWNLOAD STUDENT REPORT
// ==========================================

function downloadStudentReport() {

    downloadCSV("student");
}


// ==========================================
// 7. PRINT / SAVE STUDENT REPORT AS PDF
// ==========================================

function printStudentReport() {

    if (studentReportData.length === 0) {

        alert("Please generate a student report first!");

        return;
    }

    window.print();
}


// ==========================================
// 8. REPORT SUMMARY CARDS
// ==========================================

async function loadReportSummary() {

    try {

        const [
            attendanceData,
            marksData,
            subjectData
        ] = await Promise.all([

            fetchReport("/api/reports/attendance-summary"),

            fetchReport("/api/reports/marks-summary"),

            fetchReport("/api/reports/subject-summary")

        ]);

        attendanceReportData = attendanceData;
        marksReportData = marksData;
        subjectReportData = subjectData;

        // Count unique students from marks records
        const uniqueStudents = new Set(
            marksData.map(student => student.roll_no)
        );

        // Average attendance
        const averageAttendance = attendanceData.length > 0

            ? attendanceData.reduce(
                (sum, item) =>
                    sum + Number(item.attendance_percentage || 0),
                0
            ) / attendanceData.length

            : 0;

        // Average marks
        const averageMarks = marksData.length > 0

            ? marksData.reduce(
                (sum, item) =>
                    sum + Number(item.percentage || 0),
                0
            ) / marksData.length

            : 0;

        document.getElementById(
            "reportTotalStudents"
        ).textContent = uniqueStudents.size;

        document.getElementById(
            "reportAverageAttendance"
        ).textContent = averageAttendance.toFixed(2) + "%";

        document.getElementById(
            "reportAverageMarks"
        ).textContent = averageMarks.toFixed(2) + "%";

        document.getElementById(
            "reportTotalSubjects"
        ).textContent = subjectData.length;

    } catch (error) {

        console.error("Report summary error:", error);
    }
}


// ==========================================
// 9. STUDENT ATTENDANCE ANALYSIS CHART
// ==========================================

function createAttendanceChart(attendanceData) {

    const canvas = document.getElementById(
        "reportAttendanceChart"
    );

    if (!canvas) {
        console.error("Attendance chart canvas not found");
        return;
    }

    if (reportAttendanceChart) {
        reportAttendanceChart.destroy();
    }

    // Calculate average attendance per student
    const attendanceMap = {};

    attendanceData.forEach(record => {

        const rollNo = record.roll_no;

        if (!attendanceMap[rollNo]) {

            attendanceMap[rollNo] = {
                total: 0,
                count: 0
            };
        }

        attendanceMap[rollNo].total += Number(
            record.attendance_percentage || 0
        );

        attendanceMap[rollNo].count++;
    });

    const labels = Object.keys(attendanceMap);

    const values = labels.map(rollNo => {

        const student = attendanceMap[rollNo];

        return Number(
            (student.total / student.count).toFixed(2)
        );
    });

    reportAttendanceChart = new Chart(canvas, {

        type: "bar",

        data: {

            labels: labels,

            datasets: [{

                label: "Attendance Percentage",

                data: values,

                backgroundColor: "#4e79a7",

                borderRadius: 5
            }]
        },

        options: {

            responsive: true,

            maintainAspectRatio: false,

            plugins: {

                legend: {
                    display: true
                },

                title: {
                    display: true,
                    text: "Student Attendance Percentage"
                }
            },

            scales: {

                y: {

                    beginAtZero: true,

                    max: 100,

                    title: {
                        display: true,
                        text: "Attendance Percentage (%)"
                    }
                },

                x: {

                    title: {
                        display: true,
                        text: "Student Roll Number"
                    }
                }
            }
        }
    });
}


// ==========================================
// 10. STUDENT MARKS ANALYSIS CHART
// ==========================================

function createMarksChart(marksData) {

    const canvas = document.getElementById(
        "reportMarksChart"
    );

    if (!canvas) {
        console.error("Marks chart canvas not found");
        return;
    }

    if (reportMarksChart) {
        reportMarksChart.destroy();
    }

    reportMarksChart = new Chart(canvas, {

        type: "bar",

        data: {

            labels: marksData.map(record => record.roll_no),

            datasets: [{

                label: "Marks Percentage",

                data: marksData.map(record =>
                    Number(record.percentage || 0)
                ),

                backgroundColor: "#59a14f",

                borderRadius: 5
            }]
        },

        options: {

            responsive: true,

            maintainAspectRatio: false,

            plugins: {

                legend: {
                    display: true
                },

                title: {
                    display: true,
                    text: "Student Marks Percentage"
                }
            },

            scales: {

                y: {

                    beginAtZero: true,

                    max: 100,

                    title: {
                        display: true,
                        text: "Marks Percentage (%)"
                    }
                },

                x: {

                    title: {
                        display: true,
                        text: "Student Roll Number"
                    }
                }
            }
        }
    });
}


// ==========================================
// 11. SUBJECT PERFORMANCE ANALYSIS CHART
// ==========================================

function createSubjectChart(subjectData) {

    const canvas = document.getElementById(
        "reportSubjectChart"
    );

    if (!canvas) {
        console.error("Subject chart canvas not found");
        return;
    }

    if (reportSubjectChart) {
        reportSubjectChart.destroy();
    }

    reportSubjectChart = new Chart(canvas, {

        type: "bar",

        data: {

            labels: subjectData.map(record =>
                record.subject_name
            ),

            datasets: [{

                label: "Average Subject Percentage",

                data: subjectData.map(record =>
                    Number(record.average_percentage || 0)
                ),

                backgroundColor: "#f28e2b",

                borderRadius: 5
            }]
        },

        options: {

            responsive: true,

            maintainAspectRatio: false,

            plugins: {

                legend: {
                    display: true
                },

                title: {
                    display: true,
                    text: "Subject-wise Performance"
                }
            },

            scales: {

                y: {

                    beginAtZero: true,

                    max: 100,

                    title: {
                        display: true,
                        text: "Average Percentage (%)"
                    }
                },

                x: {

                    title: {
                        display: true,
                        text: "Subjects"
                    }
                }
            }
        }
    });
}


// ==========================================
// 12. LOAD ALL REPORT CHARTS
// ==========================================

async function loadReportCharts() {

    if (typeof Chart === "undefined") {

        console.error(
            "Chart.js is not loaded. Check your internet connection or CDN."
        );

        alert(
            "Chart.js could not load. Please check your internet connection."
        );

        return;
    }

    try {

        const [
            attendanceData,
            marksData,
            subjectData
        ] = await Promise.all([

            fetchReport("/api/reports/attendance-summary"),

            fetchReport("/api/reports/marks-summary"),

            fetchReport("/api/reports/subject-summary")

        ]);

        // Create the three charts
        createAttendanceChart(attendanceData);

        createMarksChart(marksData);

        createSubjectChart(subjectData);

    } catch (error) {

        console.error("Reports chart error:", error);

        alert(
            "Unable to load reports charts. Check the API responses."
        );
    }
}


// ==========================================
// 13. INITIALIZE REPORTS DASHBOARD
// ==========================================

document.addEventListener("DOMContentLoaded", function () {

    console.log("Reports Dashboard initialized");

    loadReportSummary();

    loadReportCharts();

});
