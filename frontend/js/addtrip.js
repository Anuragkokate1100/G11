const API_URL = "http://localhost:8080/api/trips";

const tripForm = document.getElementById("tripForm");
const saveBtn = document.getElementById("saveBtn");
const message = document.getElementById("message");


// Get token from localStorage
function getToken() {

    return (
        localStorage.getItem("token") ||
        localStorage.getItem("accessToken") ||
        localStorage.getItem("jwt") ||
        localStorage.getItem("authToken")
    );
}


// Show message
function showMessage(text, type) {

    message.textContent = text;

    message.className = type;
}


// Submit form
tripForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    saveBtn.disabled = true;
    saveBtn.textContent = "Saving...";

    message.style.display = "none";


    // Get values from form
    const sugarcaneOwnerName =
        document.getElementById("sugarcaneOwnerName").value.trim();

    const weight =
        document.getElementById("weight").value;

    const dieselRequired =
        document.getElementById("dieselRequired").value;

    const millName =
        document.getElementById("millName").value.trim();

    const tripDate =
        document.getElementById("tripDate").value;

    const tractorNumber =
        document.getElementById("tractorNumber").value.trim();


    // Create request body
    const tripData = {

        sugarcaneOwnerName: sugarcaneOwnerName,

        weight: Number(weight),

        dieselRequired: Number(dieselRequired),

        millName: millName,

        tripDate: tripDate
    };


    // Tractor number is optional
    // Backend will automatically use logged-in owner's tractor number
    if (tractorNumber !== "") {

        tripData.tractorNumber = tractorNumber;
    }


    try {

        const token = getToken();

        const headers = {
            "Content-Type": "application/json"
        };


        // Send JWT if available
        if (token) {

            headers["Authorization"] = "Bearer " + token;
        }


        // POST request to Spring Boot
        const response = await fetch(API_URL, {

            method: "POST",

            headers: headers,

            body: JSON.stringify(tripData)
        });


        const result = await response.json();


        if (!response.ok) {

            throw new Error(
                result.message || "Failed to save trip"
            );
        }


        // Success
        showMessage(
            "Trip saved successfully!",
            "success"
        );


        // Clear form
        tripForm.reset();


        // Automatically set today's date again
        setTodayDate();


    } catch (error) {

        console.error("Trip save error:", error);

        showMessage(
            error.message || "Something went wrong.",
            "error"
        );

    } finally {

        saveBtn.disabled = false;

        saveBtn.textContent = "Save Trip";
    }

});


// Set today's date
function setTodayDate() {

    const today = new Date();

    const year = today.getFullYear();

    const month = String(today.getMonth() + 1).padStart(2, "0");

    const day = String(today.getDate()).padStart(2, "0");

    document.getElementById("tripDate").value =
        `${year}-${month}-${day}`;
}


// Set today's date when page opens
setTodayDate();