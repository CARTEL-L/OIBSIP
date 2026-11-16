
package com.oibsip.reservation.views;

import com.oibsip.reservation.data.models.Reservation;
import com.oibsip.reservation.data.models.Station;
import com.oibsip.reservation.data.models.Train;
import com.oibsip.reservation.data.models.TrainClass;
import com.oibsip.reservation.data.models.TrainName;
import com.oibsip.reservation.data.registry.ReservationRegistryImpl;
import com.oibsip.reservation.dtos.request.BookTicketRequest;
import com.oibsip.reservation.dtos.response.BookTicketResponse;
import com.oibsip.reservation.services.ReservationService;
import com.oibsip.reservation.services.ReservationServiceImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class ReservationFrame extends JFrame {

    private final String username;

    private final Color NAVY = new Color(20, 25, 55);
    private final Color PURPLE = new Color(105, 75, 190);
    private final Color LIGHT = new Color(246, 247, 251);

    private JTextField passengerNameField;
    private JTextField trainNumberField;
    private JTextField trainNameField;
    private JTextField journeyDateField;

    private JComboBox<TrainClass> trainClassBox;
    private JComboBox<Station> departureBox;
    private JComboBox<Station> destinationBox;

    private final Map<Long, TrainName> trainMappings = new HashMap<>();

    private final ReservationService reservationService;

    public ReservationFrame(String username) {
        this.username = username;

        reservationService = new ReservationServiceImpl(
                new ReservationRegistryImpl()
        );

        initializeTrainMappings();

        setTitle("Book a Train");
        setSize(850, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        buildInterface();

        setVisible(true);
    }

    private void initializeTrainMappings() {

        TrainName[] trainNames = TrainName.values();

        for (int i = 0; i < trainNames.length; i++) {

            long startNumber = 1001L + (i * 1000L);
            long endNumber = startNumber + 999L;

            for (long number = startNumber; number <= endNumber; number++) {
                trainMappings.put(number, trainNames[i]);
            }
        }
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(25, 35, 25, 35));

        JLabel heading = new JLabel("Book a Train");
        heading.setFont(new Font("Arial", Font.BOLD, 27));
        heading.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Enter your journey details to make a reservation."
        );
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(new Color(210, 210, 225));

        JPanel headingPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        headingPanel.setOpaque(false);
        headingPanel.add(heading);
        headingPanel.add(subtitle);

        JButton closeButton = new JButton("Close");
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());

        header.add(headingPanel, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);

        return header;
    }

    private void buildInterface() {

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(LIGHT);

        mainPanel.add(createHeader(), BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(30, 45, 30, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        passengerNameField = new JTextField(username);
        passengerNameField.setEditable(false);

        trainNumberField = new JTextField();

        trainNameField = new JTextField();
        trainNameField.setEditable(false);

        trainClassBox = new JComboBox<>(TrainClass.values());

        departureBox = new JComboBox<>(Station.values());
        destinationBox = new JComboBox<>(Station.values());

        journeyDateField = new JTextField();
        journeyDateField.setToolTipText("Enter date in YYYY-MM-DD format");

        int row = 0;

        addFormField(formPanel, gbc, row++, "Passenger Name",
                passengerNameField);

        addFormField(formPanel, gbc, row++, "Train Number",
                trainNumberField);

        addFormField(formPanel, gbc, row++, "Train Name",
                trainNameField);

        addFormField(formPanel, gbc, row++, "Train Class",
                trainClassBox);

        addFormField(formPanel, gbc, row++, "Journey Date",
                journeyDateField);

        addFormField(formPanel, gbc, row++, "Departure",
                departureBox);

        addFormField(formPanel, gbc, row++, "Destination",
                destinationBox);

        JButton findTrainButton = new JButton("Find Train");
        styleButton(findTrainButton);
        findTrainButton.addActionListener(e -> findTrain());

        gbc.gridx = 1;
        gbc.gridy = row++;
        formPanel.add(findTrainButton, gbc);

        JButton bookButton = new JButton("Book Train");
        styleButton(bookButton);
        bookButton.addActionListener(e -> bookTrain());

        gbc.gridx = 1;
        gbc.gridy = row;
        formPanel.add(bookButton, gbc);

        trainNumberField.addActionListener(e -> findTrain());

        mainPanel.add(formPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void addFormField(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            JComponent component
    ) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.4;

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Arial", Font.BOLD, 14));
        fieldLabel.setForeground(NAVY);

        panel.add(fieldLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        component.setPreferredSize(new Dimension(300, 38));

        panel.add(component, gbc);
    }

    private void findTrain() {

        String trainNumberText = trainNumberField.getText().trim();

        if (trainNumberText.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a train number.",
                    "Missing Train Number",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {

            long number = Long.parseLong(trainNumberText);

            TrainName trainName = findTrainName(number);

            if (trainName == null) {

                trainNameField.setText("");

                JOptionPane.showMessageDialog(
                        this,
                        "Invalid train number. Enter a number between 1001 and "
                                + (1000L + (TrainName.values().length * 1000L)),
                        "Train Not Found",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            trainNameField.setText(trainName.toString());

        } catch (NumberFormatException exception) {

            trainNameField.setText("");

            JOptionPane.showMessageDialog(
                    this,
                    "Train number must contain digits only.",
                    "Invalid Train Number",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private TrainName findTrainName(long trainNumber) {
        return trainMappings.get(trainNumber);
    }

    private void bookTrain() {

        String passengerName = passengerNameField.getText().trim();
        String trainNumberText = trainNumberField.getText().trim();
        String trainNameText = trainNameField.getText().trim();
        String journeyDateText = journeyDateField.getText().trim();

        if (passengerName.isEmpty()
                || trainNumberText.isEmpty()
                || trainNameText.isEmpty()
                || journeyDateText.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please complete all the fields.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            long trainNumber = Long.parseLong(trainNumberText);

            TrainName trainName = findTrainName(trainNumber);

            if (trainName == null
                    || !trainName.toString().equals(trainNameText)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid train number and find the train first.",
                        "Invalid Train",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            LocalDate journeyDate = LocalDate.parse(journeyDateText);

            if (journeyDate.isBefore(LocalDate.now())) {

                JOptionPane.showMessageDialog(
                        this,
                        "Journey date cannot be in the past.",
                        "Invalid Date",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            Station departure = (Station) departureBox.getSelectedItem();
            Station destination = (Station) destinationBox.getSelectedItem();

            if (departure == destination) {

                JOptionPane.showMessageDialog(
                        this,
                        "Departure and destination cannot be the same.",
                        "Invalid Journey",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            TrainClass trainClass =
                    (TrainClass) trainClassBox.getSelectedItem();

            Train train = new Train();
            train.setTrainNumber(trainNumber);
            train.setTrainName(trainName.toString());

            BookTicketRequest request = new BookTicketRequest();

            request.setPassengerName(passengerName);
            request.setTrainNumber(trainNumber);
            request.setTrainName(train);
            request.setClassType(trainClass);
            request.setDateOfJourney(journeyDate);
            request.setSourceStation(departure);
            request.setDestinationStation(destination);

            BookTicketResponse response =
                    reservationService.bookTickets(request);

            String message = "Reservation Successful!\n\n"
                    + "PNR: " + response.getPNR() + "\n"
                    + "Passenger: " + passengerName + "\n"
                    + "Train Number: " + response.getTrainNumber() + "\n"
                    + "Train Name: " + trainName + "\n"
                    + "Train Class: " + trainClass + "\n"
                    + "Journey Date: " + response.getDateOfJourney() + "\n"
                    + "Departure: " + departure + "\n"
                    + "Destination: " + response.getDestinationStation();

            JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Booking Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            dispose();

        } catch (NumberFormatException exception) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid train number.",
                    "Invalid Train Number",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (DateTimeParseException exception) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter the journey date in YYYY-MM-DD format.",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception exception) {

            JOptionPane.showMessageDialog(
                    this,
                    "Booking failed: " + exception.getMessage(),
                    "Booking Error",
                    JOptionPane.ERROR_MESSAGE
            );

            exception.printStackTrace();
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
