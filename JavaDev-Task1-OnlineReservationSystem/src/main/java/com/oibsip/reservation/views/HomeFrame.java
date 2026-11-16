
package com.oibsip.reservation.views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HomeFrame extends JFrame {

    private final String username;

    private final Color NAVY = new Color(20, 25, 55);
    private final Color PURPLE = new Color(105, 75, 190);
    private final Color LIGHT = new Color(246, 247, 251);
    private final Color TEXT = new Color(55, 60, 80);

    public HomeFrame(String username) {
        this.username = username;

        setTitle("Train Reservation System - Dashboard");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        buildInterface();
        setVisible(true);
    }

    private void buildInterface() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(LIGHT);

        mainPanel.add(createSidebar(), BorderLayout.WEST);
        mainPanel.add(createDashboard(), BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, 750));
        sidebar.setBackground(NAVY);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(35, 20, 25, 20));

        JLabel logo = new JLabel("TRAIN BOOKING");
        logo.setFont(new Font("Arial", Font.BOLD, 21));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("RESERVATION SYSTEM");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 10));
        subtitle.setForeground(new Color(180, 180, 205));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(subtitle);
        sidebar.add(Box.createVerticalStrut(55));

        addSidebarButton(sidebar, "Dashboard", true, () -> {});
        addSidebarButton(sidebar, "Book a Train", false, this::openReservationFrame);
        addSidebarButton(sidebar, "My Reservations", false, this::openMyReservations);
        addSidebarButton(sidebar, "Cancel Reservation", false,
                this::openCancelReservation);
        addSidebarButton(sidebar, "My Profile", false,
                () -> showComingSoon("My Profile"));

        sidebar.add(Box.createVerticalGlue());

        addSidebarButton(sidebar, "Logout", false, this::logout);

        return sidebar;
    }

    private void addSidebarButton(
            JPanel sidebar,
            String text,
            boolean selected,
            Runnable action
    ) {
        JButton button = new JButton(text);

        button.setMaximumSize(new Dimension(210, 48));
        button.setPreferredSize(new Dimension(210, 48));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(0, 18, 0, 10));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (selected) {
            button.setBackground(PURPLE);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(NAVY);
            button.setForeground(new Color(215, 215, 230));
        }

        button.addActionListener(e -> action.run());

        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(12));
    }

    private JPanel createDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout(0, 25));
        dashboard.setBackground(LIGHT);
        dashboard.setBorder(new EmptyBorder(35, 40, 35, 40));

        JLabel welcome = new JLabel("Welcome, " + username);
        welcome.setFont(new Font("Arial", Font.BOLD, 28));
        welcome.setForeground(NAVY);

        JLabel description = new JLabel(
                "Manage your train journeys from one place."
        );
        description.setFont(new Font("Arial", Font.PLAIN, 14));
        description.setForeground(TEXT);

        JPanel welcomePanel = new JPanel(new GridLayout(2, 1, 0, 8));
        welcomePanel.setOpaque(false);
        welcomePanel.add(welcome);
        welcomePanel.add(description);

        dashboard.add(welcomePanel, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel banner = createBanner();
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionTitle = new JLabel("What would you like to do?");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 21));
        sectionTitle.setForeground(NAVY);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(banner);
        content.add(Box.createVerticalStrut(35));
        content.add(sectionTitle);
        content.add(Box.createVerticalStrut(20));

        JPanel cards = new JPanel(new GridLayout(2, 2, 20, 20));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(850, 330));
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);

        cards.add(createActionCard(
                "Book a Train",
                "Search for a train and reserve your seat.",
                "Book Now",
                this::openReservationFrame
        ));

        cards.add(createActionCard(
                "My Reservations",
                "View your existing train reservations.",
                "View Reservations",
                this::openMyReservations
        ));

        cards.add(createActionCard(
                "Cancel Reservation",
                "Cancel an existing reservation using your PNR.",
                "Cancel Booking",
                this::openCancelReservation
        ));

        cards.add(createActionCard(
                "My Profile",
                "View and manage your account information.",
                "View Profile",
                () -> showComingSoon("My Profile")
        ));

        content.add(cards);
        dashboard.add(content, BorderLayout.CENTER);

        return dashboard;
    }

    private JPanel createBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(NAVY);
        banner.setBorder(new EmptyBorder(30, 35, 30, 35));
        banner.setPreferredSize(new Dimension(850, 155));
        banner.setMaximumSize(new Dimension(850, 155));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        textPanel.setOpaque(false);

        JLabel title = new JLabel("Your Journey Starts Here");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel description = new JLabel(
                "Book your train tickets quickly and conveniently."
        );
        description.setFont(new Font("Arial", Font.PLAIN, 14));
        description.setForeground(new Color(215, 215, 230));

        textPanel.add(title);
        textPanel.add(description);

        banner.add(textPanel, BorderLayout.CENTER);
        return banner;
    }

    private JPanel createActionCard(
            String title,
            String description,
            String buttonText,
            Runnable action
    ) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 17));
        titleLabel.setForeground(NAVY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descriptionLabel = new JLabel(
                "<html><div style='width:230px;'>"
                        + description
                        + "</div></html>"
        );
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        descriptionLabel.setForeground(TEXT);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton button = new JButton(buttonText);
        button.setBackground(PURPLE);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> action.run());

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(descriptionLabel);
        card.add(Box.createVerticalGlue());
        card.add(button);

        return card;
    }

    private void openReservationFrame() {
        new ReservationFrame(username);
    }

    private void openMyReservations() {
        new MyReservationsFrame(username);
    }

    private void openCancelReservation() {
        new com.oibsip.reservation.frames.CancelReservationFrame(username);
    }

    private void showComingSoon(String feature) {
        JOptionPane.showMessageDialog(
                this,
                feature + " screen will be implemented next.",
                "Coming Soon",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame();
        }
    }
}
