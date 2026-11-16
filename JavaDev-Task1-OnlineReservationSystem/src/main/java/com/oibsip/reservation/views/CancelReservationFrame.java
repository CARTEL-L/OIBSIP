
package com.oibsip.reservation.frames;

import com.oibsip.reservation.data.models.Reservation;
import com.oibsip.reservation.data.registry.ReservationRegistryImpl;
import com.oibsip.reservation.dtos.request.CancelBookingRequest;
import com.oibsip.reservation.services.ReservationServiceImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CancelReservationFrame extends JFrame {

    private final String username;
    private final ReservationServiceImpl reservationService;

    private static final Color NAVY = new Color(20, 25, 60);
    private static final Color PURPLE = new Color(105, 65, 190);
    private static final Color LIGHT = new Color(245, 246, 250);
    private static final Color RED = new Color(190, 45, 55);

    private JTextField pnrField;

    private JPanel contentPanel;
    private JPanel detailsPanel;
    private JPanel cancellationPanel;

    private JLabel passengerNameLabel;
    private JLabel pnrLabel;
    private JLabel trainNumberLabel;
    private JLabel trainNameLabel;
    private JLabel classTypeLabel;
    private JLabel journeyDateLabel;
    private JLabel sourceStationLabel;
    private JLabel destinationStationLabel;

    private Reservation currentReservation;

    public CancelReservationFrame(String username) {
        this.username = username;

        reservationService =
                new ReservationServiceImpl(new ReservationRegistryImpl());

        setTitle("Cancel Reservation");
        setSize(650, 700);
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

        contentPanel = createContent();

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setBorder(null);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(NAVY);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel("Cancel Reservation");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 25));

        JLabel subtitle = new JLabel(
                "Retrieve your booking and cancel your ticket"
        );
        subtitle.setForeground(new Color(210, 210, 225));
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));

        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitle);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel();
        content.setBackground(LIGHT);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(25, 30, 30, 30));

        content.add(createSearchPanel());
        content.add(Box.createVerticalStrut(20));

        content.add(createDetailsPanel());
        content.add(Box.createVerticalStrut(20));

        content.add(createCancellationPanel());

        return content;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 235)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel heading = new JLabel("Enter Your PNR Number");
        heading.setFont(new Font("Arial", Font.BOLD, 16));
        heading.setForeground(NAVY);

        pnrField = new JTextField();
        pnrField.setFont(new Font("Arial", Font.PLAIN, 14));
        pnrField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton fetchButton = new JButton("Fetch Booking");
        styleButton(fetchButton, PURPLE);
        fetchButton.addActionListener(e -> fetchBooking());

        panel.add(heading);
        panel.add(Box.createVerticalStrut(12));
        panel.add(pnrField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(fetchButton);

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        return panel;
    }

    private JPanel createDetailsPanel() {
        detailsPanel = new JPanel(new GridLayout(0, 2, 12, 15));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 235)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        passengerNameLabel = new JLabel();
        pnrLabel = new JLabel();
        trainNumberLabel = new JLabel();
        trainNameLabel = new JLabel();
        classTypeLabel = new JLabel();
        journeyDateLabel = new JLabel();
        sourceStationLabel = new JLabel();
        destinationStationLabel = new JLabel();

        addDetailRow("Passenger Name:", passengerNameLabel);
        addDetailRow("PNR Number:", pnrLabel);
        addDetailRow("Train Number:", trainNumberLabel);
        addDetailRow("Train Name:", trainNameLabel);
        addDetailRow("Train Class:", classTypeLabel);
        addDetailRow("Journey Date:", journeyDateLabel);
        addDetailRow("Departure:", sourceStationLabel);
        addDetailRow("Destination:", destinationStationLabel);

        detailsPanel.setVisible(false);
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 400)
        );

        return detailsPanel;
    }

    private void addDetailRow(String heading, JLabel value) {
        JLabel title = new JLabel(heading);
        title.setFont(new Font("Arial", Font.BOLD, 13));
        title.setForeground(NAVY);

        value.setFont(new Font("Arial", Font.PLAIN, 13));
        value.setForeground(Color.DARK_GRAY);

        detailsPanel.add(title);
        detailsPanel.add(value);
    }

    private JPanel createCancellationPanel() {
        cancellationPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 10)
        );
        cancellationPanel.setBackground(LIGHT);

        JButton cancelButton = new JButton("Confirm Cancellation");
        styleButton(cancelButton, RED);

        cancelButton.addActionListener(e -> confirmCancellation());

        cancellationPanel.add(cancelButton);

        cancellationPanel.setVisible(false);
        cancellationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancellationPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 80)
        );

        return cancellationPanel;
    }

    private void fetchBooking() {
        String pnr = pnrField.getText().trim();

        if (pnr.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your PNR number.",
                    "Missing PNR",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            CancelBookingRequest request = new CancelBookingRequest();
            request.setPNR(pnr);

            Reservation reservation =
                    reservationService.fetchTicket(request);

            if (reservation == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "No booking was found with this PNR number.",
                        "Booking Not Found",
                        JOptionPane.INFORMATION_MESSAGE
                );

                hideBookingDetails();
                return;
            }

            if (!username.equalsIgnoreCase(
                    reservation.getPassengerName())) {

                JOptionPane.showMessageDialog(
                        this,
                        "This booking does not belong to your account.",
                        "Access Denied",
                        JOptionPane.WARNING_MESSAGE
                );

                hideBookingDetails();
                return;
            }

            currentReservation = reservation;
            displayBookingDetails(reservation);

        } catch (Exception exception) {
            exception.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to fetch booking: " + exception.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void displayBookingDetails(Reservation reservation) {
        passengerNameLabel.setText(reservation.getPassengerName());
        pnrLabel.setText(reservation.getId());
        trainNumberLabel.setText(
                String.valueOf(reservation.getTrainNumber())
        );
        trainNameLabel.setText(
                String.valueOf(reservation.getTrainName())
        );
        classTypeLabel.setText(
                String.valueOf(reservation.getClassType())
        );
        journeyDateLabel.setText(
                String.valueOf(reservation.getDateOfJourney())
        );
        sourceStationLabel.setText(
                String.valueOf(reservation.getSourceStation())
        );
        destinationStationLabel.setText(
                String.valueOf(reservation.getDestinationStation())
        );

        detailsPanel.setVisible(true);
        cancellationPanel.setVisible(true);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void confirmCancellation() {
        if (currentReservation == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel this booking?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            cancelReservation();
        }
    }

    private void cancelReservation() {
        try {
            CancelBookingRequest request = new CancelBookingRequest();
            request.setPNR(currentReservation.getId());

            reservationService.cancelBooking(request);

            JOptionPane.showMessageDialog(
                    this,
                    "Your booking has been cancelled successfully.",
                    "Cancellation Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            resetForm();

        } catch (Exception exception) {
            exception.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to cancel booking: " + exception.getMessage(),
                    "Cancellation Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void hideBookingDetails() {
        currentReservation = null;

        detailsPanel.setVisible(false);
        cancellationPanel.setVisible(false);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void resetForm() {
        pnrField.setText("");
        hideBookingDetails();
    }

    private void styleButton(JButton button, Color background) {
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 20, 12, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
