
const API_URL = "/api/performance";

let studentChart = null;
let subjectChart = null;
let gradeChart = null;

// Fetch API data
async function fetchData(endpoint) {
    const response = await fetch(API_URL + endpoint, {
        credentials: "same-origin"
    });

    if (!response.ok) {
        throw new Error("API request failed: " + response.status);
    }

    return await response.json();
}

// Display table records
function displayRecords(data, tableId, showSubject = false) {
    const tableBody = document.getElementById(tableId);
    tableBody.innerHTML = "";

    if (!data || data.length === 0) {
        const columns = showSubject ? 7 : 6;
        tableBody.innerHTML =
            `<tr><td colspan="${columns}">No records found!</td></tr>`;
        return;
    }

    data.forEach(record => {
        const row = document.createElement("tr");

        const cells = [
            record.roll_no,
            record.name
        ];

        if (showSubject) {
            cells.push(record.subject_name);
        }

        cells.push(
            record.total_obtained,
            record.total_maximum,
            record.percentage + "%",
            record.grade
        );

        cells.forEach(value => {
            const cell = document.createElement("td");
            cell.textContent = value ?? "";
            row.appendChild(cell);
        });

        tableBody.appendChild(row);
    });
}

// 1. Performance Summary
async function loadPerformanceSummary() {
    try {
        const data = await fetchData("/summary");

        document.getElementById("totalStudents").textContent =
            data.total_students_evaluated ?? 0;

        document.getElementById("averagePercentage").textContent =
            (data.average_percentage ?? 0) + "%";

        document.getElementById("highestPercentage").textContent =
            (data.highest_percentage ?? 0) + "%";

        document.getElementById("lowPerformers").textContent =
            data.low_performers_count ?? 0;

        document.getElementById("topStudentName").textContent =
            data.top_student_name ?? "N/A";

        document.getElementById("topStudentRoll").textContent =
            "Roll Number: " + (data.top_student_roll_no ?? "N/A");

    } catch (error) {
        console.error("Error loading summary:", error);
    }
}

// 2. Top 5 Performers
async function loadTopPerformers() {
    try {
        const data = await fetchData("/top-performers");
        const tableBody = document.getElementById("topPerformersTable");

        tableBody.innerHTML = "";

        if (!data || data.length === 0) {
            tableBody.innerHTML =
                "<tr><td colspan='7'>No performers found!</td></tr>";
            return;
        }

        data.forEach((record, index) => {
            const row = document.createElement("tr");

            const values = [
                index + 1,
                record.roll_no,
                record.name,
                record.total_obtained,
                record.total_maximum,
                record.percentage + "%",
                record.grade
            ];

            values.forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value ?? "";
                row.appendChild(cell);
            });

            tableBody.appendChild(row);
        });

    } catch (error) {
        console.error("Error loading top performers:", error);
    }
}

// 3. Overall Performance
async function loadOverallPerformance() {
    try {
        const data = await fetchData("/overall");
        displayRecords(data, "overallTable");
    } catch (error) {
        console.error("Error loading overall performance:", error);
    }
}

// 4. Subject-wise Performance
async function loadSubjectPerformance() {
    try {
        const data = await fetchData("/subject-wise");
        displayRecords(data, "subjectTable", true);
    } catch (error) {
        console.error("Error loading subject performance:", error);
    }
}

// 5. Low Performance
async function loadLowPerformance() {
    try {
        const data = await fetchData("/low-performance");
        displayRecords(data, "lowPerformanceTable");
    } catch (error) {
        console.error("Error loading low performance:", error);
    }
}

// 6. Create all three charts
async function loadPerformanceCharts() {
    try {
        const [overallData, subjectData] = await Promise.all([
            fetchData("/overall"),
            fetchData("/subject-wise")
        ]);

        if (!overallData || !subjectData) {
            return;
        }

        // Student-wise bar chart
        if (studentChart) {
            studentChart.destroy();
        }

        studentChart = new Chart(
            document.getElementById("studentPerformanceChart"),
            {
                type: "bar",
                data: {
                    labels: overallData.map(student => student.roll_no),
                    datasets: [{
                        label: "Percentage",
                        data: overallData.map(student =>
                            Number(student.percentage)
                        ),
                        backgroundColor: "#4e79a7",
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: {
                            beginAtZero: true,
                            max: 100,
                            title: {
                                display: true,
                                text: "Percentage (%)"
                            }
                        }
                    }
                }
            }
        );

        // Subject-wise average bar chart
        const subjectTotals = {};

        subjectData.forEach(record => {
            const subject = record.subject_name;
            const percentage = Number(record.percentage);

            if (!subjectTotals[subject]) {
                subjectTotals[subject] = {
                    total: 0,
                    count: 0
                };
            }

            subjectTotals[subject].total += percentage;
            subjectTotals[subject].count++;
        });

        const subjectLabels = Object.keys(subjectTotals);

        const subjectAverages = subjectLabels.map(subject =>
            Number(
                (subjectTotals[subject].total /
                subjectTotals[subject].count).toFixed(2)
            )
        );

        if (subjectChart) {
            subjectChart.destroy();
        }

        subjectChart = new Chart(
            document.getElementById("subjectPerformanceChart"),
            {
                type: "bar",
                data: {
                    labels: subjectLabels,
                    datasets: [{
                        label: "Average Percentage",
                        data: subjectAverages,
                        backgroundColor: "#59a14f",
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: {
                            beginAtZero: true,
                            max: 100,
                            title: {
                                display: true,
                                text: "Average Percentage (%)"
                            }
                        }
                    }
                }
            }
        );

        // Grade distribution pie chart
        const gradeCounts = {
            "A+": 0,
            "A": 0,
            "B": 0,
            "C": 0,
            "D": 0,
            "E": 0,
            "F": 0
        };

        overallData.forEach(student => {
            const grade = student.grade;

            if (Object.prototype.hasOwnProperty.call(gradeCounts, grade)) {
                gradeCounts[grade]++;
            }
        });

        if (gradeChart) {
            gradeChart.destroy();
        }

        gradeChart = new Chart(
            document.getElementById("gradeDistributionChart"),
            {
                type: "pie",
                data: {
                    labels: Object.keys(gradeCounts),
                    datasets: [{
                        label: "Number of Students",
                        data: Object.values(gradeCounts),
                        backgroundColor: [
                            "#59a14f",
                            "#4e79a7",
                            "#f28e2b",
                            "#e15759",
                            "#b07aa1",
                            "#76b7b2",
                            "#9c755f"
                        ]
                    }]
                },
                options: {
                    responsive: true
                }
            }
        );

    } catch (error) {
        console.error("Error loading performance charts:", error);
    }
}

// 7. Load dashboard
document.addEventListener("DOMContentLoaded", function() {
    loadPerformanceSummary();
    loadTopPerformers();
    loadOverallPerformance();
    loadSubjectPerformance();
    loadLowPerformance();
    loadPerformanceCharts();
});
