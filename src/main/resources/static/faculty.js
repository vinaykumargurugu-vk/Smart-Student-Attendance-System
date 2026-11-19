
document.addEventListener("DOMContentLoaded", () => {

    // Load initial data
    loadFacultyProfile();
    loadFacultySubjects();

    // Set today's date as default
    document.getElementById("attendanceDate").value =
        new Date().toLocaleDateString("en-CA");

    // Subject selection
    document.getElementById("subjectSelect")
        .addEventListener("change", function () {

            const subjectId = this.value;

            if (subjectId) {
                loadStudents(subjectId);
                loadAttendanceStudents(subjectId);
            } else {
                document.getElementById("studentsContainer").textContent =
                    "Select a subject to view students.";

                document.getElementById("attendanceContainer").textContent =
                    "Select a subject to mark attendance.";
            }
        });

    // Save attendance button
    document.getElementById("saveAttendanceBtn")
        .addEventListener("click", saveAttendance);

    // Logout button
    document.getElementById("logoutBtn")
        .addEventListener("click", logout);

});


// Helper to safely display database values
function escapeHTML(value) {

    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);

}


// 1. Load Faculty Profile
async function loadFacultyProfile() {

    const profileDiv = document.getElementById("facultyProfile");

    try {

        const response = await fetch("/api/faculty/me", {
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("Unable to load faculty profile.");
        }

        const faculty = await response.json();

        profileDiv.innerHTML = `
            <p><strong>Faculty ID:</strong> ${escapeHTML(faculty.faculty_id)}</p>
            <p><strong>Name:</strong> ${escapeHTML(faculty.name)}</p>
            <p><strong>Email:</strong> ${escapeHTML(faculty.email)}</p>
            <p><strong>Department:</strong> ${escapeHTML(faculty.department)}</p>
            <p><strong>Phone:</strong> ${escapeHTML(faculty.phone)}</p>
        `;

    } catch (error) {

        profileDiv.textContent = error.message;

    }

}


// 2. Load Assigned Subjects
async function loadFacultySubjects() {

    const subjectsDiv = document.getElementById("subjectsContainer");
    const subjectSelect = document.getElementById("subjectSelect");

    try {

        const response = await fetch("/api/faculty/subjects", {
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("Unable to load assigned subjects.");
        }

        const subjects = await response.json();

        if (subjects.length === 0) {

            subjectsDiv.textContent = "No subjects assigned.";

            return;
        }

        let table = `
            <table>
                <thead>
                    <tr>
                        <th>Subject ID</th>
                        <th>Subject Code</th>
                        <th>Subject Name</th>
                    </tr>
                </thead>
                <tbody>
        `;

        // Clear old dropdown options
        subjectSelect.innerHTML =
            '<option value="">Select a subject</option>';

        subjects.forEach(subject => {

            table += `
                <tr>
                    <td>${escapeHTML(subject.subject_id)}</td>
                    <td>${escapeHTML(subject.subject_code)}</td>
                    <td>${escapeHTML(subject.subject_name)}</td>
                </tr>
            `;

            const option = document.createElement("option");

            option.value = subject.subject_id;

            option.textContent =
                `${subject.subject_code} - ${subject.subject_name}`;

            subjectSelect.appendChild(option);

        });

        table += `
                </tbody>
            </table>
        `;

        subjectsDiv.innerHTML = table;

    } catch (error) {

        subjectsDiv.textContent = error.message;

    }

}


// 3. Load Students by Subject
async function loadStudents(subjectId) {

    const studentsDiv = document.getElementById("studentsContainer");

    studentsDiv.textContent = "Loading students...";

    try {

        const response = await fetch(
            `/api/faculty/students?subjectId=${encodeURIComponent(subjectId)}`,
            {
                credentials: "same-origin"
            }
        );

        if (!response.ok) {
            throw new Error("Unable to load enrolled students.");
        }

        const students = await response.json();

        if (students.length === 0) {

            studentsDiv.textContent =
                "No students enrolled in this subject.";

            return;
        }

        let table = `
            <table>
                <thead>
                    <tr>
                        <th>Student ID</th>
                        <th>Roll Number</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Department</th>
                    </tr>
                </thead>
                <tbody>
        `;

        students.forEach(student => {

            table += `
                <tr>
                    <td>${escapeHTML(student.student_id)}</td>
                    <td>${escapeHTML(student.roll_no)}</td>
                    <td>${escapeHTML(student.name)}</td>
                    <td>${escapeHTML(student.email)}</td>
                    <td>${escapeHTML(student.department)}</td>
                </tr>
            `;

        });

        table += `
                </tbody>
            </table>
        `;

        studentsDiv.innerHTML = table;

    } catch (error) {

        studentsDiv.textContent = error.message;

    }

}


// 4. Load Students for Attendance
async function loadAttendanceStudents(subjectId) {

    const container =
        document.getElementById("attendanceContainer");

    container.textContent = "Loading students...";

    try {

        const response = await fetch(
            `/api/faculty/students?subjectId=${encodeURIComponent(subjectId)}`,
            {
                credentials: "same-origin"
            }
        );

        if (!response.ok) {
            throw new Error("Unable to load attendance students.");
        }

        const students = await response.json();

        if (students.length === 0) {

            container.textContent = "No students enrolled.";

            return;
        }

        let table = `
            <table>
                <thead>
                    <tr>
                        <th>Roll Number</th>
                        <th>Student Name</th>
                        <th>Attendance Status</th>
                    </tr>
                </thead>
                <tbody>
        `;

        students.forEach(student => {

            table += `
                <tr>
                    <td>${escapeHTML(student.roll_no)}</td>

                    <td>${escapeHTML(student.name)}</td>

                    <td>
                        <select
                            class="attendance-status"
                            data-student-id="${escapeHTML(student.student_id)}">

                            <option value="PRESENT">PRESENT</option>

                            <option value="ABSENT">ABSENT</option>

                        </select>
                    </td>
                </tr>
            `;

        });

        table += `
                </tbody>
            </table>
        `;

        container.innerHTML = table;

    } catch (error) {

        container.textContent = error.message;

    }

}


// 5. Save Attendance
async function saveAttendance() {

    const subjectId =
        document.getElementById("subjectSelect").value;

    const attendanceDate =
        document.getElementById("attendanceDate").value;

    const message =
        document.getElementById("attendanceMessage");

    const button =
        document.getElementById("saveAttendanceBtn");


    // Validate subject
    if (!subjectId) {

        message.textContent = "Please select a subject.";

        return;
    }


    // Validate date
    if (!attendanceDate) {

        message.textContent = "Please select an attendance date.";

        return;
    }

    // Prevent future attendance dates
    if (attendanceDate > new Date().toLocaleDateString("en-CA")) {

        message.textContent = "Future attendance dates are not allowed.";

        return;
    }


    const statusElements =
        document.querySelectorAll(".attendance-status");


    if (statusElements.length === 0) {

        message.textContent = "No students available.";

        return;
    }


    button.disabled = true;

    message.textContent = "Saving attendance...";


    try {

        // Send attendance separately for each student
        const results = await Promise.all(

            Array.from(statusElements).map(async element => {

                const response = await fetch(
                    "/api/faculty/attendance",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type": "application/json"
                        },

                        credentials: "same-origin",

                        body: JSON.stringify({

                            studentId: Number(element.dataset.studentId),

                            subjectId: Number(subjectId),

                            attendanceDate: attendanceDate,

                            status: element.value

                        })
                    }
                );


                if (!response.ok) {

                    const errorText = await response.text();

                    throw new Error(
                        "Failed for student ID " +
                        element.dataset.studentId +
                        ". " + errorText
                    );
                }

                return response.json();

            })

        );


        message.textContent =
            results.length +
            " attendance records saved successfully!";


        message.style.color = "green";


    } catch (error) {

        message.textContent = error.message;

        message.style.color = "red";

    } finally {

        button.disabled = false;

    }

}


// 6. Logout
async function logout() {

    try {

        const response = await fetch("/api/auth/logout", {

            method: "POST",

            credentials: "same-origin"

        });

        if (!response.ok) {

            throw new Error("Logout failed.");

        }

        window.location.href = "login.html";

    } catch (error) {

        alert(error.message);

    }

}
