# Airline Reservation Management System (Java AWT + MySQL)

Desktop project made using Java AWT for all screens, MySQL (XAMPP) for data and JDBC for connection.
Tickets are made as PDF using Apache PDFBox and sent on email using Jakarta Mail (Gmail SMTP).

## Folder structure

`src` has all the .java files and `classes` has all the .class files (made by javac).

```
src/
    AirlineMain.java            -> first window (Admin / Customer)
    LoginFrame.java             -> login window for admin and customer
    DBConnection.java           -> MySQL connection (change port here)
    MessageBox.java             -> popup message and Yes/No box
    TicketService.java          -> PDF ticket and email
    AdminDashboard.java
    ManageFlightsFrame.java     -> view, search, delete flights
    FlightFormFrame.java        -> add and update flight
    ViewBookingsFrame.java      -> all bookings, search by PNR
    RegisterFrame.java
    CustomerDashboard.java
    AvailableFlightsFrame.java  -> all upcoming flights
    SeatSelectionFrame.java     -> seat selection window (max 6 seats)
    SeatMap.java                -> draws the aeroplane seat map (Canvas + Graphics)
    PassengerDetailsFrame.java  -> details of every passenger, books all with one PNR after payment
    PaymentFrame.java           -> shows total fare and QR code (ZXing), waits for payment
    PaymentServer.java          -> small HTTP backend (port 8085) for the payment website
    MyBookingsFrame.java        -> my bookings, cancel, get ticket
classes/                        -> compiled .class files
database/airline_db.sql         -> tables + sample data
lib/                            -> jar files (MySQL connector, PDFBox, Jakarta Mail, ZXing)
payment-website/                -> React + Vite demo payment website (opened from the QR code)
config.properties               -> Gmail settings for email
tickets/                        -> generated PDF tickets are saved here
```

## Setup

1. Install XAMPP, open XAMPP Control Panel and **Start Apache and MySQL**.
2. Open http://localhost/phpmyadmin -> **Import** tab -> choose `database/airline_db.sql` -> **Go**.
   This creates the `airline_db` database with sample flights.
3. Database settings are in `src/DBConnection.java` (port 3307, user `root`, empty password).
   If your XAMPP MySQL uses port 3306, change it there.
4. For email (optional):
   - Turn on 2-Step Verification on your Google account.
   - Create an App Password at https://myaccount.google.com/apppasswords
   - Put your Gmail in `mail.username` and the 16 letter app password in `mail.password`.
   - Do not upload `config.properties` to GitHub (it is in `.gitignore`).

## Run

Mac / Linux:
```
./run.sh
```
Windows: double click `run.bat`

Or in VS Code open the folder and run `src/AirlineMain.java` (the jars in `lib/` are already added in `.vscode/settings.json`).

## Login details (sample data)

| Role     | Username / Email | Password |
|----------|------------------|----------|
| Admin    | admin            | admin123 |
| Customer | rahul@gmail.com  | 1234     |

Sample flights are on 2026-12-10 (Mumbai -> Delhi), 2026-12-12 (Delhi -> Mumbai) and 2026-12-15 (Mumbai -> Bangalore).

## Features

**Admin:** Dashboard has Manage Flights and View Bookings.
Manage Flights has Add, View All, Search (by ID or flight number), Update and Delete flight.
Seats are created automatically and flight ID is auto increment.
Total seats can't be changed and the flight can't be deleted if it has active bookings.
View Bookings shows all bookings and can search a booking by PNR.

**Customer:** register, login, view all upcoming flights (can filter by From / To city),
book up to 6 seats at a time for family and friends (all passengers get one PNR),
enter details of every passenger, view my bookings, search by PNR,
cancel the ticket of one passenger, PDF ticket (with all passengers) and email.

## Demo QR Payment (NO REAL MONEY)

After passenger details, click **Proceed to Payment**. The payment window shows the total fare and a QR code.
Scan it with your phone -> the payment website opens -> click **Payment Done** or **Payment Failed**.
Seats are booked only after Payment Done. If payment fails nothing is booked, click Retry Payment for a new QR code.

**Setup (one time)**
1. If your database already exists, run `database/payments_table.sql` in phpMyAdmin (SQL tab).
2. Install Node.js, then in Terminal:
   ```
   cd payment-website
   npm install
   ```

**Every time**
1. Start MySQL in XAMPP.
2. Terminal 1 (website):
   ```
   cd payment-website
   npm run dev
   ```
3. Terminal 2 (Java app, also starts the payment server on port 8085):
   ```
   ./run.sh
   ```
4. Phone and laptop must be on the **same Wi-Fi**.
   If Mac asks "Allow incoming connections" for java / node, click **Allow**.
   The QR code uses the laptop's Wi-Fi IP automatically.

## Keyboard shortcuts

In every window, press **Ctrl + first letter** of a button to click it
(eg. Ctrl+A = Admin Login, Ctrl+S = Search, Ctrl+B = Back).
The code for this is in `main()` of `AirlineMain.java`.

## How booking works

Booking runs inside one JDBC transaction:
1. Lock the flight row (`SELECT ... FOR UPDATE`)
2. `UPDATE seats SET status='BOOKED' WHERE ... AND status='AVAILABLE'` - if 0 rows changed, someone else took the seat
3. Decrease `available_seats`
4. Insert booking with a random 6 character PNR (tries again if the PNR already exists)
5. Commit - if any step fails everything is rolled back

Cancelling also uses one transaction and only works if the status is still `CONFIRMED`,
so the seat count is never increased two times. If the email fails the booking is still saved
and the customer can press **Retry**.

## Libraries used (in lib folder)

- mysql-connector-j 8.4.0
- pdfbox-app 3.0.3
- jakarta.mail-api 2.1.3, angus-mail 2.0.3
- jakarta.activation-api 2.1.3, angus-activation 2.0.2
