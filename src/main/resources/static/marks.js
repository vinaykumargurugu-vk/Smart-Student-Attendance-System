
const API_URL = "/api/marks";

// Helper function to prevent HTML injection
function escapeHTML(value) {
    return String(value ?? "").replace(/[&<>"']/g, char => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[char]);
}

// Display marks in the table
function displayMarks(data) {
    const tableBody = document.getElementById("marksTableBody");

    tableBody.innerHTML = "";

    if (!data || data.length === 0) {
        tableBody.innerHTML =
            "<tr><td colspan='7'>No marks found!</td></tr>";
        return;
    }

    data.forEach(mark => {
        const row = `
            <tr>
                <td>${escapeHTML(mark.roll_no)}</td>
                <td>${escapeHTML(mark.name)}</td>
                <td>${escapeHTML(mark.subject_name)}</td>
                <td>${escapeHTML(mark.exam_name)}</td>
                <td>${escapeHTML(mark.obtained_marks)}</td>
                <td>${escapeHTML(mark.max_marks)}</td>
                <td>${escapeHTML(mark.percentage)}%</td>
            </tr>
        `;

        tableBody.innerHTML += row;
    });
}


// 1. Load all student marks
async function loadAllMarks() {
    try {
        const response = await fetch(API_URL, {
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("Unable to load marks");
        }

        const data = await response.json();
        displayMarks(data);

    } catch (error) {
        console.error("Error loading marks:", error);
    }
}


// 2. Add student marks
document.getElementById("marksForm").addEventListener("submit", async function(event) {
    event.preventDefault();

    const marksData = {
        roll_no: document.getElementById("rollNo").value.trim(),
        exam_id: document.getElementById("examId").value,
        obtained_marks: document.getElementById("obtainedMarks").value
    };

    try {
        const response = await fetch(API_URL, {
            method: "POST",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(marksData)
        });

        const result = await response.json();

        const message = document.getElementById("message");

        message.innerText =
            result.message || result.error || "Request completed.";

        if (response.ok) {
            document.getElementById("marksForm").reset();
            loadAllMarks();
        }

    } catch (error) {
        document.getElementById("message").innerText =
            "Unable to connect to the server.";

        console.error("Error adding marks:", error);
    }
});


// 3. Search marks using student roll number
async function searchMarks() {
    const rollNo = document.getElementById("searchRollNo").value.trim();

    if (rollNo === "") {
        alert("Please enter student roll number!");
        return;
    }

    try {
        const response = await fetch(
            `${API_URL}/student/${encodeURIComponent(rollNo)}`,
            {
                credentials: "same-origin"
            }
        );

        if (!response.ok) {
            throw new Error("Unable to search marks");
        }

        const data = await response.json();
        displayMarks(data);

    } catch (error) {
        console.error("Error searching marks:", error);
    }
}


// 4. Load marks when page opens
document.addEventListener("DOMContentLoaded", loadAllMarks);
