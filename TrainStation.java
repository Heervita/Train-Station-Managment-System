import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Train Station Management System
 * Single file — compile & run:
 *   javac -cp ".;mysql-connector-java.jar" TrainStation.java
 *   java  -cp ".;mysql-connector-java.jar;." TrainStation
 *
 * Change DB_PASS below to your MySQL password.
 * Run schema.sql once to create the database.
 */
public class TrainStation extends JFrame {

    static final String db_password = "password";
    // ── DB CONFIG ─────────────────────────────────────────────
    static final String DB_URL  = "jdbc:mysql://localhost:3306/train_station?useSSL=false&serverTimezone=UTC";
    static final String DB_USER = "root";
    static final String DB_PASS = "Mysql@2006";   // <-- CHANGE THIS

    // ── COLOURS ───────────────────────────────────────────────
    static final Color BG       = new Color(0x1e1e2e);
    static final Color PANEL    = new Color(0x27273a);
    static final Color ACCENT   = new Color(0xf0a500);
    static final Color FG       = new Color(0xe0e0e0);
    static final Color MUTED    = new Color(0x888899);
    static final Color SUCCESS  = new Color(0x3fb950);
    static final Color DANGER   = new Color(0xf85149);

    static Connection conn;

    // ── ENTRY POINT ───────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new TrainStation().setVisible(true));
    }

    public TrainStation() {
        setTitle("🚆 Train Station Management");
        setSize(1100, 680);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        connectDB();
        showLogin();
    }

    // ══════════════════════════════════════════════════════════
    // DATABASE
    // ══════════════════════════════════════════════════════════
    static void connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "DB Error: " + e.getMessage() + "\n\nCheck DB_PASS and run schema.sql first.",
                "Connection Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    static ResultSet query(String sql, Object... p) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < p.length; i++) ps.setObject(i + 1, p[i]);
        return ps.executeQuery();
    }

    static int update(String sql, Object... p) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < p.length; i++) ps.setObject(i + 1, p[i]);
            return ps.executeUpdate();
        }
    }

    // ══════════════════════════════════════════════════════════
    // LOGIN SCREEN
    // ══════════════════════════════════════════════════════════
    void showLogin() {
        getContentPane().removeAll();
        JPanel root = bg(new JPanel(new GridBagLayout()));

        JPanel card = bg(new JPanel());
        card.setBackground(PANEL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        JLabel title = new JLabel("🚆  Train Station", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(ACCENT);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Management System", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(MUTED);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        JTextField userField = field("Username");
        JPasswordField passField = new JPasswordField(20);
        styleField(passField);

        JButton loginBtn = btn("Sign In", ACCENT, Color.BLACK);
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel status = new JLabel(" ", SwingConstants.CENTER);
        status.setForeground(DANGER);
        status.setAlignmentX(CENTER_ALIGNMENT);

        JLabel hint = new JLabel("admin / admin123", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(MUTED);
        hint.setAlignmentX(CENTER_ALIGNMENT);

        loginBtn.addActionListener(e -> {
            try {
                ResultSet rs = query(
                    "SELECT * FROM admin_users WHERE username=? AND password_hash=?",
                    userField.getText().trim(), new String(passField.getPassword()));
                if (rs.next()) showMain(rs.getString("full_name"));
                else { status.setText("Wrong credentials"); passField.setText(""); }
            } catch (Exception ex) { status.setText("DB error: " + ex.getMessage()); }
        });
        passField.addActionListener(e -> loginBtn.doClick());

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));
        card.add(lbl("Username")); card.add(Box.createVerticalStrut(4));
        card.add(userField);
        card.add(Box.createVerticalStrut(12));
        card.add(lbl("Password")); card.add(Box.createVerticalStrut(4));
        card.add(passField);
        card.add(Box.createVerticalStrut(20));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(status);
        card.add(Box.createVerticalStrut(10));
        card.add(hint);

        root.add(card);
        getContentPane().add(root);
        revalidate(); repaint();
    }

    // ══════════════════════════════════════════════════════════
    // MAIN APP (tabbed)
    // ══════════════════════════════════════════════════════════
    void showMain(String name) {
        getContentPane().removeAll();
        JPanel root = bg(new JPanel(new BorderLayout()));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(PANEL);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x444455)),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        JLabel titleLbl = new JLabel("🚆  Train Station Management System");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(ACCENT);
        JLabel userLbl = new JLabel("● " + name);
        userLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLbl.setForeground(SUCCESS);
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setForeground(DANGER); logoutBtn.setBackground(PANEL);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> showLogin());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(PANEL);
        right.add(userLbl); right.add(logoutBtn);
        topBar.add(titleLbl, BorderLayout.WEST);
        topBar.add(right, BorderLayout.EAST);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG);
        tabs.setForeground(FG);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tabs.addTab("📊 Dashboard",  dashboardPanel());
        tabs.addTab("🚂 Trains",     trainsPanel());
        tabs.addTab("📅 Schedules",  schedulesPanel());
        tabs.addTab("👤 Passengers", passengersPanel());
        tabs.addTab("🎫 Tickets",    ticketsPanel());

        root.add(topBar, BorderLayout.NORTH);
        root.add(tabs,   BorderLayout.CENTER);
        getContentPane().add(root);
        revalidate(); repaint();
    }

    // ══════════════════════════════════════════════════════════
    // DASHBOARD
    // ══════════════════════════════════════════════════════════
    JPanel dashboardPanel() {
        JPanel p = bg(new JPanel(new BorderLayout(0, 16)));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Stat cards
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setBackground(BG);
        cards.add(statCard("Active Trains",    count("SELECT COUNT(*) FROM trains WHERE status='ACTIVE'"),    ACCENT));
        cards.add(statCard("Today Schedules",  count("SELECT COUNT(*) FROM schedules WHERE DATE(departure_time)=CURDATE()"), SUCCESS));
        cards.add(statCard("Total Passengers", count("SELECT COUNT(*) FROM passengers"), new Color(0x58a6ff)));
        cards.add(statCard("Tickets Booked",   count("SELECT COUNT(*) FROM tickets WHERE status='BOOKED'"),  new Color(0xbc8cff)));

        // Today's schedule table
        String[] cols = {"Train", "Origin", "Destination", "Departure", "Platform", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        try {
            ResultSet rs = query(
                "SELECT t.train_number, s.origin, s.destination, s.departure_time, s.platform_no, s.status " +
                "FROM schedules s JOIN trains t ON s.train_id=t.train_id " +
                "WHERE DATE(s.departure_time)=CURDATE() ORDER BY s.departure_time");
            while (rs.next())
                model.addRow(new Object[]{ rs.getString(1), rs.getString(2), rs.getString(3),
                    fmtTime(rs.getString(4)), rs.getString(5), rs.getString(6) });
        } catch (Exception ignored) {}

        JTable tbl = styledTable(model);
        JLabel heading = new JLabel("Today's Departures");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(ACCENT);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel bottom = bg(new JPanel(new BorderLayout(0, 6)));
        bottom.add(heading, BorderLayout.NORTH);
        bottom.add(scrollPane(tbl), BorderLayout.CENTER);

        p.add(cards,  BorderLayout.NORTH);
        p.add(bottom, BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════
    // TRAINS
    // ══════════════════════════════════════════════════════════
    JPanel trainsPanel() {
        String[] cols = {"ID", "Number", "Name", "Type", "Seats", "Available", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable tbl = styledTable(model);
        Runnable load = () -> {
            model.setRowCount(0);
            try {
                ResultSet rs = query("SELECT * FROM trains ORDER BY train_number");
                while (rs.next())
                    model.addRow(new Object[]{ rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getInt(5), rs.getInt(6), rs.getString(7) });
            } catch (Exception ignored) {}
        };
        load.run();

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setBackground(BG);
        JButton addBtn = btn("+ Add", ACCENT, Color.BLACK);
        JButton delBtn = btn("Delete", DANGER, Color.WHITE);

        addBtn.addActionListener(e -> {
            // Simple dialog
            JTextField num  = field("e.g. TRN-010");
            JTextField name = field("e.g. Express One");
            JComboBox<String> type = combo("EXPRESS","LOCAL","FREIGHT","INTERCITY");
            JTextField seats = field("500");
            JComboBox<String> status = combo("ACTIVE","INACTIVE","MAINTENANCE");

            Object[] msg = { "Train Number:", num, "Train Name:", name,
                             "Type:", type, "Total Seats:", seats, "Status:", status };
            int r = JOptionPane.showConfirmDialog(this, msg, "Add Train", JOptionPane.OK_CANCEL_OPTION);
            if (r == JOptionPane.OK_OPTION) {
                try {
                    int s = Integer.parseInt(seats.getText().trim());
                    update("INSERT INTO trains(train_number,train_name,train_type,total_seats,available_seats,status) VALUES(?,?,?,?,?,?)",
                        num.getText().trim(), name.getText().trim(), type.getSelectedItem(), s, s, status.getSelectedItem());
                    load.run();
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            }
        });

        delBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a train first."); return; }
            int id = (int) model.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Delete this train?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try { update("DELETE FROM trains WHERE train_id=?", id); load.run(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            }
        });

        btns.add(addBtn); btns.add(delBtn);
        return crudPanel("Train Management", btns, tbl);
    }

    // ══════════════════════════════════════════════════════════
    // SCHEDULES
    // ══════════════════════════════════════════════════════════
    JPanel schedulesPanel() {
        String[] cols = {"ID", "Train", "Origin", "Destination", "Departure", "Arrival", "Platform", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable tbl = styledTable(model);
        Runnable load = () -> {
            model.setRowCount(0);
            try {
                ResultSet rs = query(
                    "SELECT s.schedule_id, t.train_number, s.origin, s.destination, " +
                    "s.departure_time, s.arrival_time, s.platform_no, s.status " +
                    "FROM schedules s JOIN trains t ON s.train_id=t.train_id ORDER BY s.departure_time DESC");
                while (rs.next())
                    model.addRow(new Object[]{ rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), fmtTime(rs.getString(5)), fmtTime(rs.getString(6)),
                        rs.getString(7), rs.getString(8) });
            } catch (Exception ignored) {}
        };
        load.run();

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setBackground(BG);
        JButton addBtn    = btn("+ Add", ACCENT, Color.BLACK);
        JButton statusBtn = btn("Update Status", new Color(0x444466), FG);
        JButton delBtn    = btn("Delete", DANGER, Color.WHITE);

        addBtn.addActionListener(e -> {
            // Build train dropdown from DB
            JComboBox<String> trainBox = new JComboBox<>();
            trainBox.setBackground(PANEL); trainBox.setForeground(FG);
            try {
                ResultSet rs = query("SELECT train_id, train_number, train_name FROM trains WHERE status='ACTIVE'");
                while (rs.next())
                    trainBox.addItem(rs.getInt(1) + " | " + rs.getString(2) + " - " + rs.getString(3));
            } catch (Exception ignored) {}

            JTextField origin  = field("e.g. Delhi");
            JTextField dest    = field("e.g. Mumbai");
            JTextField dep     = field("2025-06-15 10:00:00");
            JTextField arr     = field("2025-06-15 18:00:00");
            JTextField platNo  = field("1");

            Object[] msg = { "Train:", trainBox, "Origin:", origin, "Destination:", dest,
                             "Departure (YYYY-MM-DD HH:MM:SS):", dep,
                             "Arrival   (YYYY-MM-DD HH:MM:SS):", arr,
                             "Platform No:", platNo };
            int r = JOptionPane.showConfirmDialog(this, msg, "Add Schedule", JOptionPane.OK_CANCEL_OPTION);
            if (r == JOptionPane.OK_OPTION && trainBox.getSelectedItem() != null) {
                try {
                    int tid = Integer.parseInt(((String) trainBox.getSelectedItem()).split("\\|")[0].trim());
                    update("INSERT INTO schedules(train_id,origin,destination,departure_time,arrival_time,platform_no,status) VALUES(?,?,?,?,?,?,'SCHEDULED')",
                        tid, origin.getText().trim(), dest.getText().trim(),
                        dep.getText().trim(), arr.getText().trim(), platNo.getText().trim());
                    load.run();
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            }
        });

        statusBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a schedule first."); return; }
            int id = (int) model.getValueAt(row, 0);
            JComboBox<String> newStatus = combo("SCHEDULED","DEPARTED","ARRIVED","DELAYED","CANCELLED");
            JTextField delay = field("0");
            Object[] msg = { "New Status:", newStatus, "Delay (minutes):", delay };
            int r = JOptionPane.showConfirmDialog(this, msg, "Update Status", JOptionPane.OK_CANCEL_OPTION);
            if (r == JOptionPane.OK_OPTION) {
                try {
                    update("UPDATE schedules SET status=?, delay_minutes=? WHERE schedule_id=?",
                        newStatus.getSelectedItem(), Integer.parseInt(delay.getText().trim()), id);
                    load.run();
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            }
        });

        delBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a schedule first."); return; }
            int id = (int) model.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Delete?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try { update("DELETE FROM schedules WHERE schedule_id=?", id); load.run(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            }
        });

        btns.add(addBtn); btns.add(statusBtn); btns.add(delBtn);
        return crudPanel("Train Schedules", btns, tbl);
    }

    // ══════════════════════════════════════════════════════════
    // PASSENGERS
    // ══════════════════════════════════════════════════════════
    JPanel passengersPanel() {
        String[] cols = {"ID", "First Name", "Last Name", "Email", "Phone", "ID Number"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable tbl = styledTable(model);
        Runnable load = () -> {
            model.setRowCount(0);
            try {
                ResultSet rs = query("SELECT passenger_id,first_name,last_name,email,phone,id_number FROM passengers ORDER BY last_name");
                while (rs.next())
                    model.addRow(new Object[]{ rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6) });
            } catch (Exception ignored) {}
        };
        load.run();

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setBackground(BG);
        JButton addBtn = btn("+ Add", ACCENT, Color.BLACK);
        JButton delBtn = btn("Delete", DANGER, Color.WHITE);

        addBtn.addActionListener(e -> {
            JTextField first = field("First name");
            JTextField last  = field("Last name");
            JTextField email = field("email@example.com");
            JTextField phone = field("+91...");
            JTextField idNum = field("Passport / Aadhaar");
            Object[] msg = { "First Name:", first, "Last Name:", last,
                             "Email:", email, "Phone:", phone, "ID Number:", idNum };
            int r = JOptionPane.showConfirmDialog(this, msg, "Add Passenger", JOptionPane.OK_CANCEL_OPTION);
            if (r == JOptionPane.OK_OPTION) {
                try {
                    update("INSERT INTO passengers(first_name,last_name,email,phone,id_number) VALUES(?,?,?,?,?)",
                        first.getText().trim(), last.getText().trim(), email.getText().trim(),
                        phone.getText().trim(), idNum.getText().trim());
                    load.run();
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            }
        });

        delBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a passenger."); return; }
            int id = (int) model.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Delete passenger?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try { update("DELETE FROM passengers WHERE passenger_id=?", id); load.run(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            }
        });

        btns.add(addBtn); btns.add(delBtn);
        return crudPanel("Passenger Management", btns, tbl);
    }

    // ══════════════════════════════════════════════════════════
    // TICKETS
    // ══════════════════════════════════════════════════════════
    JPanel ticketsPanel() {
        String[] cols = {"ID", "Ticket No.", "Passenger", "Train", "Route", "Class", "Price", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable tbl = styledTable(model);
        Runnable load = () -> {
            model.setRowCount(0);
            try {
                ResultSet rs = query(
                    "SELECT tk.ticket_id, tk.ticket_number, " +
                    "CONCAT(p.first_name,' ',p.last_name), tr.train_name, " +
                    "CONCAT(s.origin,' → ',s.destination), tk.ticket_class, tk.price, tk.status " +
                    "FROM tickets tk " +
                    "JOIN passengers p ON tk.passenger_id=p.passenger_id " +
                    "JOIN schedules  s ON tk.schedule_id=s.schedule_id " +
                    "JOIN trains    tr ON s.train_id=tr.train_id " +
                    "ORDER BY tk.booking_date DESC");
                while (rs.next())
                    model.addRow(new Object[]{ rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6),
                        String.format("$%.2f", rs.getDouble(7)), rs.getString(8) });
            } catch (Exception ignored) {}
        };
        load.run();

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setBackground(BG);
        JButton bookBtn   = btn("+ Book", ACCENT, Color.BLACK);
        JButton cancelBtn = btn("Cancel Ticket", DANGER, Color.WHITE);

        bookBtn.addActionListener(e -> {
            JComboBox<String> passBox = new JComboBox<>();
            JComboBox<String> schedBox = new JComboBox<>();
            passBox.setBackground(PANEL); passBox.setForeground(FG);
            schedBox.setBackground(PANEL); schedBox.setForeground(FG);
            try {
                ResultSet rp = query("SELECT passenger_id, CONCAT(first_name,' ',last_name), id_number FROM passengers");
                while (rp.next()) passBox.addItem(rp.getInt(1) + " | " + rp.getString(2) + " (" + rp.getString(3) + ")");
                ResultSet rs = query(
                    "SELECT s.schedule_id, t.train_number, s.origin, s.destination, s.departure_time " +
                    "FROM schedules s JOIN trains t ON s.train_id=t.train_id WHERE s.status='SCHEDULED'");
                while (rs.next())
                    schedBox.addItem(rs.getInt(1) + " | " + rs.getString(2) + " " +
                        rs.getString(3) + "→" + rs.getString(4) + " " + fmtTime(rs.getString(5)));
            } catch (Exception ignored) {}

            JComboBox<String> classBox = combo("ECONOMY","BUSINESS","FIRST");
            JTextField price = field("50.00");
            classBox.addActionListener(ev -> {
                switch ((String) classBox.getSelectedItem()) {
                    case "ECONOMY":  price.setText("50.00");  break;
                    case "BUSINESS": price.setText("120.00"); break;
                    case "FIRST":    price.setText("250.00"); break;
                }
            });

            Object[] msg = { "Passenger:", passBox, "Schedule:", schedBox, "Class:", classBox, "Price ($):", price };
            int r = JOptionPane.showConfirmDialog(this, msg, "Book Ticket", JOptionPane.OK_CANCEL_OPTION);
            if (r == JOptionPane.OK_OPTION && passBox.getSelectedItem() != null && schedBox.getSelectedItem() != null) {
                try {
                    int pid = Integer.parseInt(((String) passBox.getSelectedItem()).split("\\|")[0].trim());
                    int sid = Integer.parseInt(((String) schedBox.getSelectedItem()).split("\\|")[0].trim());
                    String tnum = "TKT-" + System.currentTimeMillis();
                    update("INSERT INTO tickets(ticket_number,passenger_id,schedule_id,ticket_class,price,status,payment_method) VALUES(?,?,?,?,?,'BOOKED','CASH')",
                        tnum, pid, sid, classBox.getSelectedItem(), Double.parseDouble(price.getText().trim()));
                    JOptionPane.showMessageDialog(this, "Ticket booked: " + tnum, "Success", JOptionPane.INFORMATION_MESSAGE);
                    load.run();
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            }
        });

        cancelBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a ticket."); return; }
            int id = (int) model.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Cancel this ticket?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try { update("UPDATE tickets SET status='CANCELLED' WHERE ticket_id=?", id); load.run(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            }
        });

        btns.add(bookBtn); btns.add(cancelBtn);
        return crudPanel("Ticket Booking", btns, tbl);
    }

    // ══════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════
    JPanel crudPanel(String heading, JPanel btns, JTable tbl) {
        JPanel p = bg(new JPanel(new BorderLayout(0, 10)));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JLabel h = new JLabel(heading);
        h.setFont(new Font("Segoe UI", Font.BOLD, 16));
        h.setForeground(ACCENT);
        h.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        JPanel top = bg(new JPanel(new BorderLayout(0, 6)));
        top.add(h, BorderLayout.NORTH);
        top.add(btns, BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);
        p.add(scrollPane(tbl), BorderLayout.CENTER);
        return p;
    }

    JPanel statCard(String title, int value, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        JLabel t = new JLabel(title); t.setFont(new Font("Segoe UI", Font.PLAIN, 11)); t.setForeground(MUTED);
        JLabel v = new JLabel(String.valueOf(value)); v.setFont(new Font("Segoe UI", Font.BOLD, 30)); v.setForeground(accent);
        card.add(t); card.add(Box.createVerticalStrut(4)); card.add(v);
        return card;
    }

    int count(String sql) {
        try { ResultSet rs = query(sql); if (rs.next()) return rs.getInt(1); } catch (Exception ignored) {}
        return 0;
    }

    JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(PANEL);
        t.setForeground(FG);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(28);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(new Color(0x3d3d55));
        t.setSelectionForeground(FG);
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(0x1a1a2e));
        h.setForeground(ACCENT);
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setReorderingAllowed(false);
        return t;
    }

    JScrollPane scrollPane(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.getViewport().setBackground(PANEL);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0x444455)));
        return sp;
    }

    JTextField field(String placeholder) {
        JTextField f = new JTextField(placeholder, 20);
        styleField(f);
        return f;
    }

    void styleField(JTextField f) {
        f.setBackground(new Color(0x2a2a3d));
        f.setForeground(FG);
        f.setCaretColor(ACCENT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x444455)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
    }

    JComboBox<String> combo(String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setBackground(new Color(0x2a2a3d)); c.setForeground(FG);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return c;
    }

    JButton btn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(fg);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setForeground(MUTED);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    JPanel bg(JPanel p) { p.setBackground(BG); return p; }

    String fmtTime(String dt) {
        if (dt == null) return "—";
        return dt.length() >= 16 ? dt.substring(0, 16) : dt;
    }
}
