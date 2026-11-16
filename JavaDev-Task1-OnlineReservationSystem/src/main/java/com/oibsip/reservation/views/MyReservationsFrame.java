
package com.oibsip.reservation.views;

import com.oibsip.reservation.data.models.Reservation;
import com.oibsip.reservation.data.registry.ReservationRegistryImpl;
import com.oibsip.reservation.services.ReservationService;
import com.oibsip.reservation.services.ReservationServiceImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MyReservationsFrame extends JFrame {

    private final String username;

    private final Color NAVY = new Color(20, 25, 55);
    private final Color PURPLE = new Color(105, 75, 190);
    private final Color LIGHT = new Color(246, 247, 251);

    private JTable reservationsTable;
    private DefaultTableModel tableModel;
    private JLabel emptyLabel;

    private final ReservationService reservationService;

    public MyReservationsFrame(String username) {
        this.username = username;

        reservationService = new ReservationServiceImpl(
                new ReservationRegistryImpl()
        );

        setTitle("My Reservations");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        buildInterface();
        setVisible(true);
    }

    private void buildInterface() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(LIGHT);

        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(createContent(), BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(25, 35, 25, 35));

        JPanel headingPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        headingPanel.setOpaque(false);

        JLabel heading = new JLabel("My Reservations");
        heading.setFont(new Font("Arial", Font.BOLD, 27));
        heading.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "View all your train bookings and journey details."
        );
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(new Color(210, 210, 225));

        headingPanel.add(heading);
        headingPanel.add(subtitle);

        JButton backButton = new JButton("Close");
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> dispose());

        header.add(headingPanel, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(LIGHT);
        content.setBorder(new EmptyBorder(30, 35, 30, 35));

        JPanel informationPanel = new JPanel(new BorderLayout());
        informationPanel.setOpaque(false);

        JLabel passengerLabel = new JLabel("Passenger: " + username);
        passengerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        passengerLabel.setForeground(NAVY);

        JLabel instructionLabel = new JLabel(
                "Your reservation history will appear below."
        );
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        instructionLabel.setForeground(Color.GRAY);

        JPanel informationText = new JPanel(new GridLayout(2, 1, 0, 7));
        informationText.setOpaque(false);
        informationText.add(passengerLabel);
        informationText.add(instructionLabel);

        informationPanel.add(informationText, BorderLayout.WEST);

        String[] columns = {
                "PNR",
                "Train Number",
                "Train Name",
                "Train Class",
                "Journey Date",
                "Departure",
                "Destination"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reservationsTable = new JTable(tableModel);
        reservationsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        reservationsTable.setRowHeight(35);
        reservationsTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        reservationsTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 12)
        );
        reservationsTable.getTableHeader().setBackground(NAVY);
        reservationsTable.getTableHeader().setForeground(Color.WHITE);
        reservationsTable.getTableHeader().setPreferredSize(
                new Dimension(0, 40)
        );

        JScrollPane scrollPane = new JScrollPane(reservationsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        emptyLabel = new JLabel(
                "No reservations to display yet.",
                SwingConstants.CENTER
        );
        emptyLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        emptyLabel.setForeground(Color.GRAY);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(emptyLabel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );
        bottomPanel.setOpaque(false);

        JButton refreshButton = new JButton("Refresh");
        styleButton(refreshButton);
        refreshButton.addActionListener(e -> loadReservations());

        bottomPanel.add(refreshButton);

        content.add(informationPanel, BorderLayout.NORTH);
        content.add(tablePanel, BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);

        loadReservations();

        return content;
    }

    private void loadReservations() {
        tableModel.setRowCount(0);

        try {
            List<Reservation> reservations =
                    reservationService.fetchReservations(username);

            if (reservations.isEmpty()) {
                emptyLabel.setText("No reservations to display yet.");
                emptyLabel.setVisible(true);
                return;
            }

            emptyLabel.setVisible(false);

            for (Reservation reservation : reservations) {
                Object[] row = {
                        reservation.getId(),
                        reservation.getTrainNumber(),
                        reservation.getTrainName(),
                        reservation.getClassType(),
                        reservation.getDateOfJourney(),
                        reservation.getSourceStation(),
                        reservation.getDestinationStation()
                };

                tableModel.addRow(row);
            }

        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load your reservations.\n"
                            + exception.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void styleButton(JButton button) {
        button.setBackground(PURPLE);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
