
document.getElementById("loginForm")
    .addEventListener("submit", async function(event) {

    event.preventDefault();

    const username =
        document.getElementById("username").value.trim();

    const password =
        document.getElementById("password").value;

    const message = document.getElementById("message");

    message.style.color = "blue";
    message.textContent = "Logging in...";

    try {

        const response = await fetch("/api/auth/login", {
            method: "POST",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        const data = await response.json();

        if (response.ok && data.success) {

            message.style.color = "green";
            message.textContent = "Login successful!";

            if (data.role === "ADMIN") {
                window.location.href = "index.html";
            }
            else if (data.role === "FACULTY") {
                window.location.href = "faculty.html";
            }
            else if (data.role === "STUDENT") {
                window.location.href = "student.html";
            }

        } else {

            message.style.color = "red";
            message.textContent = data.message ||
                "Invalid username or password.";

        }

    } catch (error) {

        message.style.color = "red";
        message.textContent = "Unable to connect to server.";

        console.error(error);
    }
});
