import os
import sys
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.lib.units import inch
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super().showPage()
        super().save()

    def draw_page_decorations(self, page_count):
        if self._pageNumber == 1:
            # Suppress headers and footers on cover page
            return

        self.saveState()
        self.setFont("Helvetica-Bold", 8)
        self.setFillColor(colors.HexColor("#4A5568"))

        # Running Header
        self.drawString(54, letter[1] - 36, "SKYWAY AIRLINES — College Project Defense & Viva Voce Guide")
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#718096"))
        self.drawRightString(letter[0] - 54, letter[1] - 36, "Java AWT • MySQL • PDFBox • ZXing")
        self.setStrokeColor(colors.HexColor("#CBD5E0"))
        self.setLineWidth(0.75)
        self.line(54, letter[1] - 42, letter[0] - 54, letter[1] - 42)

        # Running Footer
        page_str = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(letter[0] - 54, 34, page_str)
        self.drawString(54, 34, "Author: Avadhoot Savle | Prepared for Academic Project Viva & Evaluation")
        self.line(54, 46, letter[0] - 54, 46)
        self.restoreState()

def build_pdf(filename="SkyWay_Project_Explanation_Guide.pdf"):
    doc = SimpleDocTemplate(
        filename,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()

    primary_color = colors.HexColor("#0F2A4A")     # Deep navy
    secondary_color = colors.HexColor("#1A56DB")   # Accent blue
    text_dark = colors.HexColor("#1A202C")         # Charcoal body
    callout_bg = colors.HexColor("#EFF6FF")        # Soft blue box
    callout_border = colors.HexColor("#BFDBFE")
    warning_bg = colors.HexColor("#FFFBEB")
    warning_border = colors.HexColor("#FDE68A")

    title_style = ParagraphStyle(
        'CoverTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=26,
        leading=32,
        textColor=primary_color,
        alignment=1,
        spaceAfter=12
    )

    subtitle_style = ParagraphStyle(
        'CoverSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=12.5,
        leading=17,
        textColor=colors.HexColor("#4A5568"),
        alignment=1,
        spaceAfter=25
    )

    h1_style = ParagraphStyle(
        'SectionH1',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=15,
        leading=19,
        textColor=primary_color,
        spaceBefore=16,
        spaceAfter=8,
        keepWithNext=True
    )

    h2_style = ParagraphStyle(
        'SectionH2',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=11.5,
        leading=15,
        textColor=secondary_color,
        spaceBefore=12,
        spaceAfter=5,
        keepWithNext=True
    )

    h3_style = ParagraphStyle(
        'SectionH3',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=9.5,
        leading=13,
        textColor=colors.HexColor("#2D3748"),
        spaceBefore=8,
        spaceAfter=3,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'BodyDark',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=13,
        textColor=text_dark,
        spaceAfter=6
    )

    bullet_style = ParagraphStyle(
        'BulletText',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8.5,
        leading=12,
        textColor=text_dark,
        leftIndent=12,
        spaceAfter=3
    )

    callout_style = ParagraphStyle(
        'CalloutText',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8.5,
        leading=12,
        textColor=colors.HexColor("#1E40AF")
    )

    table_header_style = ParagraphStyle(
        'TableHeader',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8.5,
        leading=11,
        textColor=colors.white
    )

    table_cell_style = ParagraphStyle(
        'TableCell',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8,
        leading=11,
        textColor=text_dark
    )

    code_snip_style = ParagraphStyle(
        'CodeSnippet',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=7.5,
        leading=9.5,
        textColor=colors.HexColor("#1A202C")
    )

    story = []

    # =========================================================================
    # COVER PAGE
    # =========================================================================
    story.append(Spacer(1, 35))
    story.append(Paragraph("✈️ SKYWAY AIRLINES", ParagraphStyle('TopBadge', parent=title_style, fontSize=15, textColor=secondary_color, spaceAfter=8)))
    story.append(Paragraph("Airline Reservation &amp; Management System", title_style))
    story.append(Paragraph("Comprehensive Project Guide, File-by-File Code Walkthrough,<br/>Database Concurrency Architecture &amp; Viva Voce Defense Preparation", subtitle_style))
    story.append(HRFlowable(width="75%", thickness=2, color=secondary_color, spaceAfter=20, spaceBefore=5))

    cover_meta = [
        [Paragraph("<b>Domain &amp; Category:</b>", table_cell_style), Paragraph("Full-Stack Enterprise Desktop Application &amp; Mobile QR Gateway", table_cell_style)],
        [Paragraph("<b>Core Language &amp; GUI:</b>", table_cell_style), Paragraph("Java (JDK 25) • Java Abstract Window Toolkit (AWT) &amp; 2D Graphics", table_cell_style)],
        [Paragraph("<b>Relational Database:</b>", table_cell_style), Paragraph("MySQL Server (ACID Transactions, Row Locks via JDBC Driver 8.4)", table_cell_style)],
        [Paragraph("<b>Integrated Libraries:</b>", table_cell_style), Paragraph("Apache PDFBox 3.0, Jakarta Mail 2.1, Google ZXing 3.5, React/Vite", table_cell_style)],
        [Paragraph("<b>Student Presenter:</b>", table_cell_style), Paragraph("Avadhoot Savle", table_cell_style)],
        [Paragraph("<b>Target Evaluation:</b>", table_cell_style), Paragraph("College Project Submission, Code Inspection &amp; Viva Voce Examination", table_cell_style)],
    ]
    t_cover = Table(cover_meta, colWidths=[140, 310])
    t_cover.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#F8FAFC")),
        ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#CBD5E0")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#E2E8F0")),
        ('TOPPADDING', (0,0), (-1,-1), 6),
        ('BOTTOMPADDING', (0,0), (-1,-1), 6),
        ('LEFTPADDING', (0,0), (-1,-1), 10),
        ('RIGHTPADDING', (0,0), (-1,-1), 10),
    ]))
    story.append(t_cover)

    story.append(Spacer(1, 25))
    summary_box = [
        [Paragraph("<b>🎯 Objective of this Document:</b> This guide breaks down every module, algorithm, and line of code in the SkyWay system. It explains <i>what</i> each component is doing, <i>why</i> it was engineered that way, and <i>how</i> to present it convincingly to professors and external examiners during lab evaluation tomorrow.", callout_style)]
    ]
    t_summary = Table(summary_box, colWidths=[480])
    t_summary.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), callout_bg),
        ('BOX', (0,0), (-1,-1), 1, callout_border),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LEFTPADDING', (0,0), (-1,-1), 10),
        ('RIGHTPADDING', (0,0), (-1,-1), 10),
    ]))
    story.append(t_summary)
    story.append(PageBreak())

    # =========================================================================
    # SECTION 1: ARCHITECTURAL OVERVIEW & WORKFLOW
    # =========================================================================
    story.append(Paragraph("1. System Architecture &amp; End-to-End Workflow", h1_style))
    story.append(HRFlowable(width="100%", thickness=1, color=primary_color, spaceAfter=10))

    story.append(Paragraph(
        "<b>SkyWay</b> is an enterprise-grade desktop airline reservation application that marries classic low-level "
        "Java GUI development with modern distributed transaction patterns. The application avoids bloated third-party GUI "
        "frameworks in favor of native <b>Java AWT (Abstract Window Toolkit)</b>, combining it with high-performance 2D canvas "
        "rendering, a built-in micro HTTP web server, automated vector PDF boarding pass generation, and encrypted Gmail SMTP integration.",
        body_style
    ))

    arch_table_data = [
        [Paragraph("<b>Layer</b>", table_header_style), Paragraph("<b>Tech Stack</b>", table_header_style), Paragraph("<b>Responsibilities &amp; Capabilities</b>", table_header_style)],
        [
            Paragraph("<b>Presentation (GUI)</b>", table_cell_style),
            Paragraph("Java AWT (Frame, Canvas, Graphics), Custom SeatMap", table_cell_style),
            Paragraph("Desktop windows, coordinate-based aeroplane fuselage map, real-time seat availability color-coding, JVM-wide Ctrl+Key shortcuts.", table_cell_style)
        ],
        [
            Paragraph("<b>Business Logic</b>", table_cell_style),
            Paragraph("AirlineMain, FlightFormFrame, PassengerDetailsFrame, TicketService", table_cell_style),
            Paragraph("Flight scheduling, seat generation algorithms, fare calculation, 6-character PNR generation, PDF compilation.", table_cell_style)
        ],
        [
            Paragraph("<b>Mobile Gateway</b>", table_cell_style),
            Paragraph("ZXing QR Library, com.sun.net.httpserver.HttpServer, React/Vite", table_cell_style),
            Paragraph("Generates dynamic QR codes, detects laptop LAN IP, serves HTTP webhooks (port 8085) for mobile phone simulated checkout.", table_cell_style)
        ],
        [
            Paragraph("<b>Data &amp; Concurrency</b>", table_cell_style),
            Paragraph("MySQL 8/9, JDBC Connector 8.4, DBConnection", table_cell_style),
            Paragraph("ACID transaction boundary, pessimistic row-level locking (`SELECT ... FOR UPDATE`), 6 normalized tables in 3NF.", table_cell_style)
        ],
    ]
    t_arch = Table(arch_table_data, colWidths=[90, 160, 230])
    t_arch.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), primary_color),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E0")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#F8FAFC")]),
        ('TOPPADDING', (0,0), (-1,-1), 5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 5),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(t_arch)
    story.append(Spacer(1, 10))

    story.append(Paragraph("<b>End-to-End Operational Flow Diagram</b>", h2_style))

    flow_box = [
        [Paragraph(
            "<b>CUSTOMER BOOKING &amp; PAYMENT FLOW:</b><br/>"
            "<code>[Customer Login]</code> &rarr; <code>[AvailableFlightsFrame]</code> (Select Flight) &rarr; "
            "<code>[SeatSelectionFrame]</code> (Click on 2D Aeroplane Canvas) &rarr; "
            "<code>[PassengerDetailsFrame]</code> (Enter Name, Age, Gender) &rarr; "
            "<code>[PaymentFrame]</code> (Displays ZXing QR Code) &rarr; "
            "<b>[Phone Scans QR &rarr; React Webpage]</b> &rarr; Click 'Payment Done' &rarr; "
            "<b>[PaymentServer (Port 8085)]</b> receives webhook &rarr; "
            "<b>[MySQL ACID Transaction]</b> (Locks Flight &rarr; Marks Seats 'BOOKED' &rarr; Saves Booking) &rarr; "
            "<b>[TicketService]</b> (Apache PDFBox generates PDF &rarr; Jakarta Mail sends email).",
            callout_style
        )]
    ]
    t_flow = Table(flow_box, colWidths=[480])
    t_flow.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), callout_bg),
        ('BOX', (0,0), (-1,-1), 1, callout_border),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LEFTPADDING', (0,0), (-1,-1), 10),
        ('RIGHTPADDING', (0,0), (-1,-1), 10),
    ]))
    story.append(t_flow)
    story.append(Spacer(1, 14))

    # =========================================================================
    # SECTION 2: DATABASE ARCHITECTURE & CONCURRENCY
    # =========================================================================
    story.append(Paragraph("2. Database Design &amp; Concurrency Control", h1_style))
    story.append(HRFlowable(width="100%", thickness=1, color=primary_color, spaceAfter=10))

    story.append(Paragraph(
        "The relational database schema is normalized to 3NF to avoid update anomalies and guarantee ACID compliance. "
        "It consists of 6 primary tables in the <code>airline_db</code> database:",
        body_style
    ))

    db_tables_data = [
        [Paragraph("<b>Table</b>", table_header_style), Paragraph("<b>Keys &amp; Constraints</b>", table_header_style), Paragraph("<b>Columns &amp; Purpose</b>", table_header_style)],
        [
            Paragraph("<b>admins</b>", table_cell_style),
            Paragraph("PK: admin_id", table_cell_style),
            Paragraph("`username` (UNIQUE), `password`. Stores administrator login credentials.", table_cell_style)
        ],
        [
            Paragraph("<b>customers</b>", table_cell_style),
            Paragraph("PK: customer_id", table_cell_style),
            Paragraph("`name`, `email` (UNIQUE), `phone`, `password`, `created_at`. Registered passenger profiles.", table_cell_style)
        ],
        [
            Paragraph("<b>flights</b>", table_cell_style),
            Paragraph("PK: flight_id", table_cell_style),
            Paragraph("`flight_no`, `airline`, `source`, `destination`, `departure_time`, `arrival_time`, `price`, `total_seats`, `available_seats`.", table_cell_style)
        ],
        [
            Paragraph("<b>seats</b>", table_cell_style),
            Paragraph("PK: seat_id<br/>FK: flight_id", table_cell_style),
            Paragraph("`seat_number` (eg. 1A, 2C), `status` ('AVAILABLE', 'BOOKED'). Composite unique key on `(flight_id, seat_number)`.", table_cell_style)
        ],
        [
            Paragraph("<b>bookings</b>", table_cell_style),
            Paragraph("PK: booking_id<br/>FK: flight_id, customer_id", table_cell_style),
            Paragraph("`pnr` (6-char UNIQUE), `passenger_name`, `passenger_age`, `passenger_gender`, `seat_number`, `booking_date`, `status` ('CONFIRMED', 'CANCELLED').", table_cell_style)
        ],
        [
            Paragraph("<b>payments</b>", table_cell_style),
            Paragraph("PK: payment_id<br/>FK: flight_id, customer_id", table_cell_style),
            Paragraph("`payment_ref` (UNIQUE), `amount`, `status` ('PENDING', 'SUCCESS', 'FAILED'), `seats_csv`, `passengers_json`, `created_at`.", table_cell_style)
        ],
    ]
    t_db = Table(db_tables_data, colWidths=[80, 130, 270])
    t_db.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), primary_color),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E0")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#F8FAFC")]),
        ('TOPPADDING', (0,0), (-1,-1), 5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 5),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(t_db)
    story.append(Spacer(1, 10))

    story.append(Paragraph("<b>How Double-Booking is Prevented (Pessimistic Row-Level Locking)</b>", h2_style))
    story.append(Paragraph(
        "A critical challenge in airline booking systems is <b>race conditions</b> (two users clicking 'Pay' for the same seat simultaneously). "
        "SkyWay solves this through a 3-step defensive locking mechanism inside <code>PaymentFrame.java</code>:",
        body_style
    ))

    race_box = [
        [Paragraph(
            "<b>Step 1: Row Locking (`SELECT ... FOR UPDATE`):</b><br/>"
            "Inside an explicit transaction (<code>con.setAutoCommit(false)</code>), we execute:<br/>"
            "<code>PreparedStatement ps = con.prepareStatement(\"SELECT available_seats FROM flights WHERE flight_id = ? FOR UPDATE\");</code><br/>"
            "This forces MySQL InnoDB to place an exclusive write lock on that flight row. Any other concurrent booking thread trying to book on that flight is queued until our transaction commits or rolls back.<br/><br/>"
            "<b>Step 2: Atomic State Transition Check:</b><br/>"
            "We update each seat with a strict status check:<br/>"
            "<code>UPDATE seats SET status='BOOKED' WHERE flight_id=? AND seat_number=? AND status='AVAILABLE';</code><br/>"
            "If <code>ps.executeUpdate() == 0</code>, it means another thread claimed the seat a split second prior. The application immediately invokes <code>con.rollback()</code>, aborts the reservation, and alerts the user without corrupted data.<br/><br/>"
            "<b>Step 3: Atomic Final Commit:</b><br/>"
            "Only when all seats are locked, the booking records inserted, and the flight seat inventory decremented does <code>con.commit()</code> execute.",
            ParagraphStyle('LockStyle', parent=styles['Normal'], fontName='Helvetica', fontSize=8, leading=11.5, textColor=colors.HexColor("#92400E"))
        )]
    ]
    t_race = Table(race_box, colWidths=[480])
    t_race.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), warning_bg),
        ('BOX', (0,0), (-1,-1), 1, warning_border),
        ('TOPPADDING', (0,0), (-1,-1), 7),
        ('BOTTOMPADDING', (0,0), (-1,-1), 7),
        ('LEFTPADDING', (0,0), (-1,-1), 9),
        ('RIGHTPADDING', (0,0), (-1,-1), 9),
    ]))
    story.append(t_race)
    story.append(PageBreak())

    # =========================================================================
    # SECTION 3: FILE-BY-FILE COMPLETE CODE EXPLANATION
    # =========================================================================
    story.append(Paragraph("3. Complete File-by-File Code Walkthrough", h1_style))
    story.append(HRFlowable(width="100%", thickness=1, color=primary_color, spaceAfter=10))

    story.append(Paragraph(
        "Here is the granular, file-by-file breakdown of every Java source file in the application. "
        "Review each component's purpose, design, key methods, and presentation tips.",
        body_style
    ))

    def create_card(title, role, methods, tip):
        items = []
        items.append(Paragraph(f"<b>📄 {title}</b>", h2_style))
        items.append(Paragraph(f"<b>Role:</b> {role}", body_style))
        items.append(Paragraph("<b>Key Methods &amp; Implementation Logic:</b>", h3_style))
        for m_sig, m_body in methods:
            items.append(Paragraph(f"• <code><b>{m_sig}</b></code>: {m_body}", bullet_style))
        items.append(Spacer(1, 3))
        items.append(Paragraph(f"🎓 <b>Viva Voce Tip:</b> {tip}", callout_style))
        items.append(Spacer(1, 6))

        card_table = Table([[items]], colWidths=[480])
        card_table.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#F8FAFC")),
            ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#E2E8F0")),
            ('LINELEFT', (0,0), (0,-1), 3, secondary_color),
            ('TOPPADDING', (0,0), (-1,-1), 7),
            ('BOTTOMPADDING', (0,0), (-1,-1), 7),
            ('LEFTPADDING', (0,0), (-1,-1), 9),
            ('RIGHTPADDING', (0,0), (-1,-1), 9),
        ]))
        return KeepTogether([card_table, Spacer(1, 8)])

    # 1. DBConnection.java
    story.append(create_card(
        "src/DBConnection.java",
        "Database connectivity provider with auto-detection of active MySQL port.",
        [
            ("getConnection()", "Loads `com.mysql.cj.jdbc.Driver`. Attempts connection to port 3306 first; if unavailable, automatically tries port 3307 (standard XAMPP port). Caches the successful port in `workingPort`."),
            ("workingPort (static Integer)", "Stores the verified port in memory so subsequent queries throughout the session connect instantly without re-testing.")
        ],
        "Explain that hardcoding a single port breaks when moving between laptops with Homebrew MySQL (3306) and XAMPP (3307). Your code solves this with dynamic port discovery."
    ))

    # 2. AirlineMain.java
    story.append(create_card(
        "src/AirlineMain.java",
        "Application entry point; provides user role selection, global keyboard shortcuts, and payment server initialization.",
        [
            ("main(String[] args)", "Attaches a JVM-wide event listener to `Toolkit.getDefaultToolkit().addAWTEventListener(...)`. Starts `PaymentServer.start()` in the background, then instantiates the main frame."),
            ("eventDispatched(AWTEvent ev)", "Filters for `KeyEvent.KEY_PRESSED` with `isControlDown()`. When Ctrl+letter is pressed, it crawls the active window hierarchy and programmatically triggers the matching button's `ActionEvent`."),
            ("findButton(Container box, int key)", "Recursively searches nested panels inside the active window for any button whose label begins with the pressed character.")
        ],
        "Demonstrate pressing `Ctrl+A` or `Ctrl+C` to your professor without touching the mouse. It proves mastery of low-level Java AWT event pipelines."
    ))

    # 3. LoginFrame.java & RegisterFrame.java
    story.append(create_card(
        "src/LoginFrame.java & src/RegisterFrame.java",
        "Role-aware credential authentication and new customer account registration.",
        [
            ("LoginFrame(String role)", "Configures GUI labels for either 'Admin' or 'Customer'. Queries `admins` table or `customers` table accordingly."),
            ("actionPerformed(ActionEvent e)", "Reads username/email and password fields. Executes `SELECT * FROM ... WHERE ... = ? AND password = ?` via `PreparedStatement`. Upon customer authentication, constructs `CustomerDashboard(id, name)`."),
            ("RegisterFrame submit", "Validates that name is non-empty, email contains '@' and '.', and phone number consists of exactly 10 digits before executing `INSERT INTO customers`.")
        ],
        "Emphasize that `PreparedStatement` with parameterized placeholders (`?`) is used instead of string concatenation to guarantee immunity against SQL Injection."
    ))

    # 4. AdminDashboard.java & CustomerDashboard.java
    story.append(create_card(
        "src/AdminDashboard.java & src/CustomerDashboard.java",
        "Role-segregated navigation control centers.",
        [
            ("AdminDashboard()", "Provides navigation to `ManageFlightsFrame` and `ViewBookingsFrame`. Includes a safe session logout action."),
            ("CustomerDashboard(int id, String name)", "Displays personalized welcome banner ('Welcome, [Name]'). Directs user to `AvailableFlightsFrame` (booking) or `MyBookingsFrame` (booking history).")
        ],
        "Note how session variables (`customerId`, `customerName`) are maintained and forwarded through constructors to associate subsequent bookings with the logged-in user."
    ))

    # 5. ManageFlightsFrame.java & FlightFormFrame.java
    story.append(create_card(
        "src/ManageFlightsFrame.java & src/FlightFormFrame.java",
        "Admin flight scheduling and automatic seat inventory generator.",
        [
            ("loadFlights(String filter)", "Retrieves upcoming flights from MySQL, formatting flight number, airline, route, departure/arrival timestamps, fare, and seat count into an AWT `List`."),
            ("deleteFlight()", "Checks for active confirmed bookings before allowing flight deletion to prevent orphaned foreign keys."),
            ("saveFlight() in FlightFormFrame", "Inserts or updates flight record. If creating a new flight, it runs a batch insertion algorithm generating 30 seat rows (`1A` through `5F`) set to 'AVAILABLE'.")
        ],
        "Explain the seat generator: whenever an admin creates a flight, 30 distinct seat records are automatically generated in the `seats` table."
    ))

    # 6. AvailableFlightsFrame.java
    story.append(create_card(
        "src/AvailableFlightsFrame.java",
        "Customer flight discovery window with live inventory filters.",
        [
            ("searchFlights()", "Executes `SELECT * FROM flights WHERE departure_time >= NOW()` with optional source and destination text filters."),
            ("proceedToSeatSelection()", "Validates that a flight is highlighted and has `available_seats > 0`, then launches `SeatSelectionFrame` with the selected `flightId`.")
        ],
        "Mention that flights with departure times in the past are automatically filtered out using `departure_time >= NOW()`."
    ))

    story.append(PageBreak())

    # 7. SeatMap.java & SeatSelectionFrame.java (THE GRAPHICS HIGHLIGHT)
    story.append(create_card(
        "src/SeatMap.java & src/SeatSelectionFrame.java (Custom 2D Vector Aeroplane Engine)",
        "Interactive 2D graphical aeroplane fuselage canvas allowing direct seat selection.",
        [
            ("paint(Graphics g) in SeatMap", "Draws the aeroplane fuselage from scratch: curved aerodynamic nose (`drawArc`/`fillArc`), cockpit windows, aircraft wings, central aisle walkway, and seat bounding boxes."),
            ("Color-Coded Status Engine", "Green (`#2ECC71`) = Available; Red (`#E74C3C`) = Booked; Blue (`#3498DB`) = Selected by customer. Renders armrests, headrests, and alphanumeric seat codes (`1A`, `2B`)."),
            ("mouseClicked(MouseEvent e)", "Calculates hit-test collision: translates raw `(e.getX(), e.getY())` coordinates into matrix row and column. Enforces maximum 6 seats limit. Repaints canvas."),
            ("Selection Listener Callback", "Notifies `SeatSelectionFrame` to dynamically re-calculate and display total ticket fare in real time as seats are toggled.")
        ],
        "THIS IS YOUR STRONGEST SHOWCASE: Most student projects use basic checkboxes. Your project implements a custom 2D vector graphics engine on an AWT Canvas with coordinate collision detection."
    ))

    # 8. PassengerDetailsFrame.java
    story.append(create_card(
        "src/PassengerDetailsFrame.java",
        "Dynamic passenger manifest form generator and payment reference initializer.",
        [
            ("buildPassengerForms()", "Generates input rows (Name, Age, Gender Choice dropdown) matching the exact number of seats chosen in the seat map."),
            ("proceedToPayment()", "Generates unique payment reference (e.g., `PAY9K2X4B`). Inserts a pending record into `payments` table storing serialized passenger data, and transitions to `PaymentFrame`.")
        ],
        "Explain that storing passenger data in `payments` before booking ensures uncompleted checkouts don't pollute the primary `bookings` table."
    ))

    # 9. PaymentFrame.java & PaymentServer.java
    story.append(create_card(
        "src/PaymentFrame.java & src/PaymentServer.java (Mobile QR Payment Gateway)",
        "Embedded HTTP micro-server and dynamic QR code payment gateway bridging phone to desktop.",
        [
            ("generateQRCodeImage(String url)", "Uses Google ZXing (`core-3.5.3.jar`) `QRCodeWriter` to encode `http://<laptop-ip>:8085/?ref=PAY...` into a 220x220 pixel buffered image."),
            ("PaymentServer.start()", "Spawns an embedded HTTP server on port 8085 using Java's built-in `com.sun.net.httpserver.HttpServer`. Handles `/api/payment/details`, `/api/payment/success`, and `/api/payment/fail`."),
            ("getLaptopIp()", "Scans active network interfaces via `NetworkInterface.getNetworkInterfaces()` to detect the laptop's Wi-Fi LAN IP so phones on the same network can load the payment portal."),
            ("completeBookingTransaction()", "Executes the atomic ACID transaction: row lock -> updates seat status -> generates 6-char PNR -> inserts booking rows -> decrements flight seats -> commits -> calls TicketService.")
        ],
        "Highlight cross-device integration: the Java desktop app hosts its own HTTP server so an external phone can scan the screen and complete the transaction in real time."
    ))

    # 10. TicketService.java
    story.append(create_card(
        "src/TicketService.java (Automated PDF & SMTP Dispatcher)",
        "Vector PDF e-ticket compiler and asynchronous Gmail SMTP email dispatcher.",
        [
            ("generateTicketPDF(...)", "Uses Apache PDFBox 3.0 (`PDDocument`, `PDPage`, `PDPageContentStream`). Programmatically draws professional airline boarding passes with headers, route badges, PNR, passenger roster, and a verification QR code. Saves to `tickets/ticket_<PNR>.pdf`."),
            ("sendTicketEmail(...)", "Reads Gmail credentials from `config.properties`. Connects to `smtp.gmail.com:587` with TLS encryption via Jakarta Mail. Attaches the generated PDF to a MIME multipart email."),
            ("Async Execution", "Dispatches the email on a background worker thread (`new Thread(...).start()`) so the desktop GUI remains silky-smooth and never freezes.")
        ],
        "Point out the asynchronous threading: network operations like SMTP are offloaded to background threads to ensure optimal UI responsiveness."
    ))

    # 11. MyBookingsFrame.java & ViewBookingsFrame.java
    story.append(create_card(
        "src/MyBookingsFrame.java & src/ViewBookingsFrame.java",
        "Customer self-service reservation management and admin booking overview.",
        [
            ("cancelBooking(String pnr)", "Transactional cancellation: updates booking status to 'CANCELLED', resets seat status to 'AVAILABLE', and increments `available_seats` inside an atomic transaction."),
            ("openPDFTicket(String pnr)", "Uses `java.awt.Desktop.getDesktop().open(file)` to launch the generated PDF in the operating system's default viewer (Preview on Mac / Acrobat on Windows)."),
            ("resendTicketEmail()", "Re-invokes `TicketService` to re-transmit the e-ticket to the customer's registered email.")
        ],
        "Explain that cancellation uses transactional rollback: if updating the seat status fails, the cancellation is aborted to ensure database consistency."
    ))

    # 12. MessageBox.java
    story.append(create_card(
        "src/MessageBox.java",
        "Lightweight native modal dialog component.",
        [
            ("MessageBox(Frame parent, String msg)", "Modal dialog that blocks parent input until closed, displaying formatted alerts and error notices."),
            ("Confirmation Mode", "Provides Yes/No confirmation options returning a boolean result, used before destructive operations such as flight cancellation.")
        ],
        "Implemented using pure Java AWT primitives without requiring heavy Swing dependencies."
    ))

    story.append(PageBreak())

    # =========================================================================
    # SECTION 4: VIVA VOCE DEFENSE & EXAMINER Q&A CHEAT SHEET
    # =========================================================================
    story.append(Paragraph("4. Viva Voce Defense &amp; Examiner Q&amp;A Cheat Sheet", h1_style))
    story.append(HRFlowable(width="100%", thickness=1, color=primary_color, spaceAfter=10))

    story.append(Paragraph(
        "Review these 8 high-probability questions before your defense. Deliver the provided model answers "
        "to demonstrate mastery of software engineering concepts:",
        body_style
    ))

    qa_list = [
        (
            "Q1: Why did you choose Java AWT instead of Swing or JavaFX?",
            "Answer: AWT gives direct, low-level access to native GUI primitives and lightweight graphics rendering. By extending `java.awt.Canvas` and overriding `paint(Graphics g)`, we built a fully custom 2D vector aircraft seating map with coordinate hit-testing that demonstrates foundational computer graphics principles rather than relying on heavy pre-built widgets."
        ),
        (
            "Q2: How does your system guarantee that two passengers cannot book the same seat simultaneously?",
            "Answer: We implement Pessimistic Concurrency Control inside an ACID JDBC transaction. Before updating, we lock the flight row using `SELECT ... FOR UPDATE` in MySQL InnoDB. Then we execute an atomic update with a status predicate: `UPDATE seats SET status='BOOKED' WHERE flight_id=? AND seat_number=? AND status='AVAILABLE'`. If `executeUpdate()` returns 0, another user claimed it; our transaction rolls back immediately."
        ),
        (
            "Q3: How does the QR mobile payment function without an external cloud gateway?",
            "Answer: Our Java desktop app embeds a lightweight HTTP server (`com.sun.net.httpserver.HttpServer`) on port 8085. We use the ZXing library to encode the laptop's Wi-Fi IP and payment reference into a QR code. When scanned on a phone on the same Wi-Fi, the phone opens our React web portal and sends an HTTP webhook back to our embedded Java server."
        ),
        (
            "Q4: What is a PNR and how is it generated in your project?",
            "Answer: PNR stands for Passenger Name Record. It is a 6-character alphanumeric unique identifier (e.g., `RB4TX2`) used globally in aviation to index all travelers under a reservation. We generate random uppercase alphanumeric strings and verify their uniqueness in the `bookings` table before confirmation."
        ),
        (
            "Q5: Why did you use `PreparedStatement` instead of `Statement`?",
            "Answer: `PreparedStatement` pre-compiles SQL queries on the database engine and binds parameters separately using typed placeholders (`?`). This completely prevents SQL Injection attacks and improves execution performance through query plan reuse."
        ),
        (
            "Q6: How are email delivery failures handled without losing booking data?",
            "Answer: The MySQL transaction commits *before* email dispatch. The email routine in `TicketService.java` runs asynchronously in a background thread inside a `try-catch` block. If the SMTP server is unreachable, the booking remains safely saved in the database, the PDF is preserved on disk, and the user can click 'Resend Ticket' in their dashboard."
        ),
        (
            "Q7: What happens under the hood when a booking is cancelled?",
            "Answer: Cancellation executes inside an atomic JDBC transaction. The booking status is updated to `'CANCELLED'`, the seat in the `seats` table is set back to `'AVAILABLE'`, and the flight's `available_seats` count is incremented by 1. All changes commit together so seat inventory is never corrupted."
        ),
        (
            "Q8: What software architectural pattern does your database layer follow?",
            "Answer: It follows the Singleton/Factory Pattern in `DBConnection.java`. It maintains a static connection factory and caches the active port (3306 or 3307) so the entire application shares an optimized, resilient connection pipeline."
        )
    ]

    for q, a in qa_list:
        qa_data = [
            [Paragraph(f"<b>{q}</b>", ParagraphStyle('QStyle', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=9, leading=12, textColor=secondary_color))],
            [Paragraph(a, ParagraphStyle('AStyle', parent=styles['Normal'], fontName='Helvetica', fontSize=8.5, leading=11.5, textColor=text_dark))]
        ]
        t_qa = Table(qa_data, colWidths=[480])
        t_qa.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#F8FAFC")),
            ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E0")),
            ('TOPPADDING', (0,0), (-1,-1), 5),
            ('BOTTOMPADDING', (0,0), (-1,-1), 5),
            ('LEFTPADDING', (0,0), (-1,-1), 8),
            ('RIGHTPADDING', (0,0), (-1,-1), 8),
        ]))
        story.append(KeepTogether([t_qa, Spacer(1, 5)]))

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"SUCCESS: Generated {filename}")

if __name__ == "__main__":
    output_path = sys.argv[1] if len(sys.argv) > 1 else "SkyWay_Project_Explanation_Guide.pdf"
    build_pdf(output_path)
