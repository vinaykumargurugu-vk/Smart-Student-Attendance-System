
document.addEventListener("DOMContentLoaded", () => {

    const subjectSelect = document.getElementById("marksSubject");
    const examSelect = document.getElementById("marksExam");
    const tableBody = document.getElementById("marksTableBody");
    const message = document.getElementById("marksMessage");

    function showMessage(text, success = false) {
        message.textContent = text;
        message.style.color = success ? "green" : "red";
    }

    // 1. Load assigned subjects
    async function loadSubjects() {

        try {
            const response = await fetch("/api/faculty/subjects", {
                credentials: "same-origin"
            });

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
                    `${subject.subject_code} - ${subject.subject_name}`;

                subjectSelect.appendChild(option);
            });

        } catch (error) {
            showMessage(error.message);
        }
    }

    // 2. Load exams by subject
    async function loadExams(subjectId) {

        examSelect.innerHTML =
            '<option value="">Select Exam</option>';

        examSelect.disabled = true;
        tableBody.innerHTML =
            '<tr><td colspan="5">Select an exam.</td></tr>';

        if (!subjectId) return;

        try {
            const response = await fetch(
                `/api/faculty/exams?subjectId=${encodeURIComponent(subjectId)}`,
                { credentials: "same-origin" }
            );

            if (!response.ok) {
                throw new Error("Unable to load exams.");
            }

            const exams = await response.json();

            exams.forEach(exam => {
                const option = document.createElement("option");

                option.value = exam.exam_id;
                option.textContent =
                    `${exam.exam_name} (Maximum: ${exam.max_marks})`;

                examSelect.appendChild(option);
            });

            examSelect.disabled = exams.length === 0;

            if (exams.length === 0) {
                showMessage("No exams found for this subject.");
            } else {
                showMessage("Exams loaded successfully.", true);
            }

        } catch (error) {
            showMessage(error.message);
        }
    }

    // 3. Load students and existing marks
    async function loadExamStudents(examId) {

        tableBody.innerHTML =
            '<tr><td colspan="5">Loading students...</td></tr>';

        if (!examId) return;

        try {
            const response = await fetch(
                `/api/faculty/exam/students?examId=${encodeURIComponent(examId)}`,
                { credentials: "same-origin" }
            );

            if (!response.ok) {
                throw new Error("Unable to load exam students.");
            }

            const students = await response.json();

            tableBody.innerHTML = "";

            if (students.length === 0) {
                tableBody.innerHTML =
                    '<tr><td colspan="5">No students found.</td></tr>';
                return;
            }

            students.forEach(student => {

                const row = document.createElement("tr");

                const rollCell = document.createElement("td");
                rollCell.textContent = student.roll_no;

                const nameCell = document.createElement("td");
                nameCell.textContent = student.name;

                const maxCell = document.createElement("td");
                maxCell.textContent = student.max_marks;

                const marksCell = document.createElement("td");
                const input = document.createElement("input");

                input.type = "number";
                input.min = "0";
                input.max = student.max_marks;
                input.step = "0.01";
                input.value = student.obtained_marks ?? "";
                input.className = "obtained-marks";

                marksCell.appendChild(input);

                const actionCell = document.createElement("td");
                const button = document.createElement("button");

                button.textContent = "Save";
                button.type = "button";

                button.addEventListener("click", () => {
                    saveMarks(student, input, button);
                });

                actionCell.appendChild(button);

                row.append(
                    rollCell,
                    nameCell,
                    maxCell,
                    marksCell,
                    actionCell
                );

                tableBody.appendChild(row);
            });

            showMessage("Students and existing marks loaded.", true);

        } catch (error) {
            tableBody.innerHTML =
                '<tr><td colspan="5">Unable to load students.</td></tr>';

            showMessage(error.message);
        }
    }

    // 4. Save or update marks
    async function saveMarks(student, input, button) {

        const obtainedMarks = Number(input.value);
        const maxMarks = Number(student.max_marks);

        if (
            input.value.trim() === "" ||
            !Number.isFinite(obtainedMarks) ||
            obtainedMarks < 0 ||
            obtainedMarks > maxMarks
        ) {
            showMessage(`Enter marks between 0 and ${maxMarks}.`);
            return;
        }

        button.disabled = true;
        showMessage("Saving marks...");

        try {
            const response = await fetch("/api/faculty/marks", {

                method: "POST",

                credentials: "same-origin",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    studentId: student.student_id,
                    examId: student.exam_id,
                    obtainedMarks: obtainedMarks
                })
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(
                    result.error ||
                    result.message ||
                    "Unable to save marks."
                );
            }

            showMessage(
                `Marks saved successfully for ${student.name}.`,
                true
            );

        } catch (error) {
            showMessage(error.message);

        } finally {
            button.disabled = false;
        }
    }

    // 5. Subject selection
    subjectSelect.addEventListener("change", () => {
        loadExams(subjectSelect.value);
    });

    // 6. Exam selection
    examSelect.addEventListener("change", () => {
        loadExamStudents(examSelect.value);
    });

    // Initial loading
    loadSubjects();

});
