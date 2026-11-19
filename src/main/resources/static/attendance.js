
const API_URL = "/api/attendance";

// Helper: safely display text in HTML
function escapeHTML(value) {
    return String(value ?? "").replace(/[&<>"']/g, char => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[char]);
}

// Helper: read JSON response
async function getResponseData(response) {
    return await response.json();
}

// 1. Mark Attendance
document.getElementById("attendanceForm").addEventListener("submit", async function(event) {
    event.preventDefault();

    const attendance = {
        roll_no: document.getElementById("roll_no").value.trim(),
        subject_code: document.getElementById("subject_code").value.trim(),
        attendance_date: document.getElementById("attendance_date").value,
        status: document.getElementById("status").value
    };

    try {
        const response = await fetch(API_URL, {
            method: "POST",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(attendance)
        });

        const result = await getResponseData(response);

        alert(result.message || result.error || "Attendance request completed.");

        if (response.ok) {
            document.getElementById("attendanceForm").reset();

            loadAttendance();
            loadPercentage();
            loadLowAttendance();
        }

    } catch (error) {
        alert("Unable to connect to the server!");
        console.error("Attendance error:", error);
    }
});


// 2. Display All Attendance Records
async function loadAttendance() {
    try {
        const response = await fetch(API_URL, {
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("Failed to load attendance records");
        }

        const records = await response.json();

        const table = document.getElementById("attendanceTable");
        table.innerHTML = "";

        records.forEach(record => {
            table.innerHTML += `
                <tr>
                    <td>${escapeHTML(record.roll_no)}</td>
                    <td>${escapeHTML(record.name)}</td>
                    <td>${escapeHTML(record.subject_code)}</td>
                    <td>${escapeHTML(record.attendance_date)}</td>
                    <td>${escapeHTML(record.status)}</td>
                </tr>
            `;
        });

    } catch (error) {
        console.error("Error loading attendance:", error);
    }
}


// 3. Display Attendance Percentage
async function loadPercentage() {
    try {
        const response = await fetch(API_URL + "/percentage", {
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("Failed to load attendance percentage");
        }

        const records = await response.json();

        const table = document.getElementById("percentageTable");
        table.innerHTML = "";

        records.forEach(record => {
            table.innerHTML += `
                <tr>
                    <td>${escapeHTML(record.roll_no)}</td>
                    <td>${escapeHTML(record.name)}</td>
                    <td>${escapeHTML(record.subject_code)}</td>
                    <td>${escapeHTML(record.total_classes)}</td>
                    <td>${escapeHTML(record.present_classes)}</td>
                    <td>${escapeHTML(record.attendance_percentage)}%</td>
                </tr>
            `;
        });

    } catch (error) {
        console.error("Error loading percentage:", error);
    }
}


// 4. Display Low Attendance Alerts
async function loadLowAttendance() {
    try {
        const response = await fetch(API_URL + "/low-attendance", {
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("Failed to load low attendance records");
        }

        const records = await response.json();

        const table = document.getElementById("lowAttendanceTable");
        table.innerHTML = "";

        if (records.length === 0) {
            table.innerHTML = `
                <tr>
                    <td colspan="4">No low attendance alerts found.</td>
                </tr>
            `;
            return;
        }

        records.forEach(record => {
            table.innerHTML += `
                <tr>
                    <td>${escapeHTML(record.roll_no)}</td>
                    <td>${escapeHTML(record.name)}</td>
                    <td>${escapeHTML(record.subject_code)}</td>
                    <td>${escapeHTML(record.attendance_percentage)}%</td>
                </tr>
            `;
        });

    } catch (error) {
        console.error("Error loading low attendance:", error);
    }
}


// 5. Load all attendance information when page opens
document.addEventListener("DOMContentLoaded", function() {
    loadAttendance();
    loadPercentage();
    loadLowAttendance();
});
