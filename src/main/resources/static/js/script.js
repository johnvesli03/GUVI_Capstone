document.addEventListener("DOMContentLoaded", function () {
    const travelDateInput = document.getElementById("travelDateInput");
    const addPassengerBtn = document.getElementById("addPassenger");
    const passengerContainer = document.getElementById("passengerContainer");
    const passengerSection = document.getElementById("passengerSection");

    let bookedSeats = {
        Seater: [],
        Sleeper: []
    };

    let selectedSeatsInSession = {
        Seater: new Set(),
        Sleeper: new Set()
    };

    let seatCounts = {
        Seater: 0,
        Sleeper: 0
    };

    let passengerCount = 0;

    travelDateInput.addEventListener("change", async () => {
        const date = travelDateInput.value;
        if (!date || !busId) return;

        try {
            const res = await fetch(`/booked-seats/${busId}?travelDate=${date}`);
            const data = await res.json();

            bookedSeats.Seater = data.Seater || [];
            bookedSeats.Sleeper = data.Sleeper || [];

            seatCounts.Seater = parseInt(document.body.getAttribute("data-seater")) || 10;
            seatCounts.Sleeper = parseInt(document.body.getAttribute("data-sleeper")) || 5;

            selectedSeatsInSession.Seater.clear();
            selectedSeatsInSession.Sleeper.clear();

            passengerSection.style.display = "block";
            passengerContainer.innerHTML = "";
            passengerCount = 0;
            addPassenger();
        } catch (err) {
            alert("Could not load seat info. Try again.");
        }
    });

    function addPassenger() {
        passengerCount++;
        const div = document.createElement("div");
        div.classList.add("passenger-group");

        div.innerHTML = `
            <hr>
            <label>Name</label><input name="passengerName" required>
            <label>Age</label><input name="passengerAge" type="number" required>
            <label>Gender</label>
            <select name="passengerGender" required>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
                <option value="Other">Other</option>
            </select>
            <label>Seat Type</label>
            <select name="seatType" class="seat-type-selector" data-index="${passengerCount}" required>
                <option value="Seater">Seater</option>
                <option value="Sleeper">Sleeper</option>
            </select>

            <h4>Select Seat</h4>
            <div class="seat-layout" id="layout-${passengerCount}"></div>
            <input type="hidden" name="seatNumber" id="selectedSeat-${passengerCount}">
        `;

        passengerContainer.appendChild(div);
        attachSeatTypeListener(passengerCount);
        renderSeats(passengerCount, "Seater");
    }

    function attachSeatTypeListener(index) {
        const selector = document.querySelector(`select[data-index="${index}"]`);
        selector.addEventListener("change", (e) => {
            renderSeats(index, e.target.value);
        });
    }

    function renderSeats(index, type) {
        const layout = document.getElementById(`layout-${index}`);
        const selectedInput = document.getElementById(`selectedSeat-${index}`);
        layout.innerHTML = "";

        const total = seatCounts[type] || 10;

        for (let i = 1; i <= total; i++) {
            const btn = document.createElement("button");
            btn.innerText = i;
            btn.classList.add("seat");
            btn.dataset.seatNumber = i;

            const alreadyBooked = bookedSeats[type].includes(i);
            const alreadySelectedByOthers = selectedSeatsInSession[type].has(i);

            if (alreadyBooked || alreadySelectedByOthers) {
                btn.disabled = true;
                btn.classList.add("booked");
            }

            btn.addEventListener("click", function (e) {
                e.preventDefault();
                if (btn.disabled) return;

                const previous = parseInt(selectedInput.value);
                if (previous && selectedSeatsInSession[type].has(previous)) {
                    selectedSeatsInSession[type].delete(previous);
                }

                layout.querySelectorAll(".seat").forEach(b => b.classList.remove("selected"));
                btn.classList.add("selected");

                const seatNum = parseInt(btn.dataset.seatNumber);
                selectedSeatsInSession[type].add(seatNum);
                selectedInput.value = seatNum;

                refreshAllSeatLayouts();
            });

            layout.appendChild(btn);
        }

        layout.classList.toggle("sleeper-layout", type === "Sleeper");
    }

    function refreshAllSeatLayouts() {
        const allLayouts = passengerContainer.querySelectorAll(".passenger-group");
        allLayouts.forEach((group, idx) => {
            const index = idx + 1;
            const selector = group.querySelector("select[name='seatType']");
            const type = selector.value;
            renderSeats(index, type);
        });
    }

    addPassengerBtn.addEventListener("click", () => {
        addPassenger();
    });
});

// -------------------- Razorpay Integration + Booking + Conflict Handling --------------------
function initiatePayment() {
    const travelDate = document.getElementById("travelDateInput").value;
    if (!travelDate) {
        alert("Please select a travel date");
        return;
    }

    fetch("/api/payment/create-order", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            amount: 500,
            currency: "INR",
            receipt: "bus_" + new Date().getTime()
        })
    })
    .then(res => res.json())
    .then(order => {
        const options = {
            key: 'rzp_test_38I5IEufjhiOFj',
            amount: order.amount,
            currency: order.currency,
            name: "Bus Ticket Booking",
            description: "Payment for ticket",
            order_id: order.id,
            handler: function (response) {
                const travelDate = document.getElementById("travelDateInput").value;
                const form = document.getElementById("bookingForm");

                const formData = new FormData(form);
              formData.set("travelDate", travelDate);
                formData.append("paymentId", response.razorpay_payment_id);
                formData.append("orderId", order.id);
                formData.append("receipt", order.receipt);

                fetch(form.action, {
                    method: "POST",
                    body: formData
                })
                .then(async (res) => {
                    if (!res.ok) {
                        const errData = await res.json();
                        alert("❌ " + (errData.error || "Booking failed."));
                        return;
                    }
                    const data = await res.json();
                    window.location.href = `/confirmation?bookingId=${data.bookingId}&status=success`;
                })
                .catch(err => {
                    console.error(err);
                    alert("Something went wrong during booking.");
                });
            },
            theme: { color: "#3399cc" }
        };
        const rzp = new Razorpay(options);
        rzp.open();
    })
    .catch(err => {
        alert("❌ Payment failed.");
        console.error(err);
    });
}
