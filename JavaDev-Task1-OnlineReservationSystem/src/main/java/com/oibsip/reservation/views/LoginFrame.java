
package com.oibsip.reservation.views;

import com.oibsip.reservation.data.registry.PassengerRegistryImpl;
import com.oibsip.reservation.dtos.request.LoginPassengerRequest;
import com.oibsip.reservation.dtos.request.RegisterPassengerRequest;
import com.oibsip.reservation.dtos.response.LoginPassengerResponse;
import com.oibsip.reservation.dtos.response.RegisterPassengerResponse;
import com.oibsip.reservation.services.AuthService;
import com.oibsip.reservation.services.AuthServiceImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final AuthService authService;

    private JTextField usernameField;
    private JPasswordField passwordField;

    private JTextField registerUsernameField;
    private JTextField registerEmailField;
    private JPasswordField registerPasswordField;
    private JPasswordField confirmPasswordField;

    private static final Color NAVY = new Color(20, 30, 75);
    private static final Color PURPLE = new Color(91, 45, 190);
    private static final Color BACKGROUND = new Color(246, 245, 252);
    private static final Color TEXT = new Color(35, 42, 75);
    private static final Color MUTED = new Color(120, 125, 150);

    private static final Font TITLE_FONT =
            new Font("SansSerif", Font.BOLD, 28);

    private static final Font NORMAL_FONT =
            new Font("SansSerif", Font.PLAIN, 15);

    public LoginFrame() {

        authService = new AuthServiceImpl(new PassengerRegistryImpl());

        setTitle("Train Reservation System");
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createRegistrationPanel(), "REGISTER");

        JPanel rootPanel = new JPanel(new BorderLayout());

        rootPanel.add(createBrandPanel(), BorderLayout.WEST);
        rootPanel.add(mainPanel, BorderLayout.CENTER);

        rootPanel.setBackground(BACKGROUND);

        add(rootPanel);

        setVisible(true);
    }


    private JPanel createBrandPanel() {

        JPanel panel = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();

                GradientPaint gradient = new GradientPaint(
                        0, 0, NAVY,
                        getWidth(), getHeight(), PURPLE
                );

                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };

        panel.setPreferredSize(new Dimension(390, 700));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(65, 35, 45, 35));

        JLabel trainIcon = new JLabel("🚆");
        trainIcon.setFont(new Font("SansSerif", Font.PLAIN, 65));
        trainIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heading = new JLabel("Train Reservation");
        heading.setFont(new Font("SansSerif", Font.BOLD, 28));
        heading.setForeground(Color.WHITE);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel system = new JLabel("System");
        system.setFont(new Font("SansSerif", Font.BOLD, 32));
        system.setForeground(new Color(190, 160, 255));
        system.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel line = new JPanel();
        line.setBackground(new Color(155, 105, 255));
        line.setMaximumSize(new Dimension(230, 3));
        line.setPreferredSize(new Dimension(230, 3));
        line.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Book. Travel. Explore.");
        tagline.setFont(new Font("SansSerif", Font.ITALIC, 18));
        tagline.setForeground(Color.WHITE);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel description = new JLabel(
                "<html><center>Experience convenient and reliable<br>" +
                        "train reservations with us.</center></html>"
        );

        description.setFont(NORMAL_FONT);
        description.setForeground(new Color(220, 215, 245));
        description.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel footer = new JLabel("Your Journey, Our Priority");
        footer.setFont(new Font("SansSerif", Font.ITALIC, 14));
        footer.setForeground(new Color(205, 190, 255));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(trainIcon);
        panel.add(Box.createVerticalStrut(25));
        panel.add(heading);
        panel.add(Box.createVerticalStrut(5));
        panel.add(system);
        panel.add(Box.createVerticalStrut(25));
        panel.add(line);
        panel.add(Box.createVerticalStrut(25));
        panel.add(tagline);
        panel.add(Box.createVerticalStrut(25));
        panel.add(description);
        panel.add(Box.createVerticalGlue());
        panel.add(footer);

        return panel;
    }


    private JPanel createLoginPanel() {

        JPanel panel = createFormCard();

        JPanel header = createHeader(
                "👤",
                "Welcome Back",
                "Login to continue your journey"
        );

        JPanel content = createFormContent();

        usernameField = createTextField();
        passwordField = createPasswordField();

        JButton loginButton = createPrimaryButton("Login");

        JButton registerButton = createTextButton(
                "Don't have an account? Register"
        );

        addFormField(content, "Username", usernameField, 0);
        addFormField(content, "Password", passwordField, 1);

        GridBagConstraints gbc = createConstraints();

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 10, 0);

        content.add(loginButton, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(5, 0, 0, 0);

        content.add(registerButton, gbc);

        loginButton.addActionListener(e -> {

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            handleLogin(username, password);
        });

        registerButton.addActionListener(e ->
                cardLayout.show(mainPanel, "REGISTER")
        );

        panel.add(header, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BACKGROUND);
        outer.setBorder(new EmptyBorder(65, 65, 65, 65));
        outer.add(panel, BorderLayout.CENTER);

        return outer;
    }


    private JPanel createRegistrationPanel() {

        JPanel panel = createFormCard();

        JPanel header = createHeader(
                "✎",
                "Create Account",
                "Register to start your journey"
        );

        JPanel content = createFormContent();

        registerUsernameField = createTextField();
        registerEmailField = createTextField();
        registerPasswordField = createPasswordField();
        confirmPasswordField = createPasswordField();

        JButton registerButton = createPrimaryButton("Create Account");

        JButton backButton = createTextButton(
                "Already have an account? Login"
        );

        addFormField(content, "Username", registerUsernameField, 0);
        addFormField(content, "Email Address", registerEmailField, 1);
        addFormField(content, "Password", registerPasswordField, 2);
        addFormField(content, "Confirm Password", confirmPasswordField, 3);

        GridBagConstraints gbc = createConstraints();

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 10, 0);

        content.add(registerButton, gbc);

        gbc.gridy = 9;
        gbc.insets = new Insets(5, 0, 0, 0);

        content.add(backButton, gbc);

        registerButton.addActionListener(e -> {

            String username = registerUsernameField.getText().trim();
            String email = registerEmailField.getText().trim();

            String password =
                    new String(registerPasswordField.getPassword());

            String confirmPassword =
                    new String(confirmPasswordField.getPassword());

            handleRegistration(
                    username,
                    email,
                    password,
                    confirmPassword
            );
        });

        backButton.addActionListener(e ->
                cardLayout.show(mainPanel, "LOGIN")
        );

        panel.add(header, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BACKGROUND);
        outer.setBorder(new EmptyBorder(40, 65, 40, 65));
        outer.add(panel, BorderLayout.CENTER);

        return outer;
    }


    private JPanel createFormCard() {

        JPanel panel = new JPanel(new BorderLayout(0, 20));

        panel.setBackground(Color.WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(230, 225, 245), 1
                ),
                new EmptyBorder(25, 35, 25, 35)
        ));

        return panel;
    }

    private JPanel createHeader(
            String icon,
            String heading,
            String subtitle) {

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.BOLD, 35));
        iconLabel.setForeground(PURPLE);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(TITLE_FONT);
        headingLabel.setForeground(NAVY);
        headingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(NORMAL_FONT);
        subtitleLabel.setForeground(MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(iconLabel);
        header.add(Box.createVerticalStrut(12));
        header.add(headingLabel);
        header.add(Box.createVerticalStrut(10));
        header.add(subtitleLabel);
        header.add(Box.createVerticalStrut(20));

        return header;
    }

    private JPanel createFormContent() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(5, 0, 0, 0));

        return panel;
    }

    private JTextField createTextField() {

        JTextField field = new JTextField(20);
        styleTextField(field);

        return field;
    }

    private JPasswordField createPasswordField() {

        JPasswordField field = new JPasswordField(20);
        styleTextField(field);

        return field;
    }

    private void styleTextField(JTextField field) {

        field.setFont(NORMAL_FONT);
        field.setForeground(TEXT);
        field.setBackground(new Color(250, 249, 255));
        field.setCaretColor(PURPLE);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(215, 210, 240), 1
                ),
                new EmptyBorder(12, 14, 12, 14)
        ));

        field.setPreferredSize(new Dimension(300, 46));
    }

    private JButton createPrimaryButton(String text) {

        JButton button = new JButton(text);

        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(PURPLE);

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(300, 48));

        button.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(70, 30, 155));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(PURPLE);
            }
        });

        return button;
    }

    private JButton createTextButton(String text) {

        JButton button = new JButton(text);

        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setForeground(PURPLE);
        button.setBackground(Color.WHITE);

        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }


    private GridBagConstraints createConstraints() {

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 5, 0);

        return gbc;
    }

    private void addFormField(
            JPanel panel,
            String labelText,
            JTextField field,
            int row) {

        GridBagConstraints gbc = createConstraints();

        JLabel label = new JLabel(labelText);

        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(TEXT);

        gbc.gridx = 0;
        gbc.gridy = row * 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(7, 0, 3, 0);

        panel.add(label, gbc);

        gbc.gridy = (row * 2) + 1;
        gbc.insets = new Insets(3, 0, 10, 0);

        panel.add(field, gbc);
    }


    private void handleLogin(String username, String password) {

        if (username.isBlank() || password.isBlank()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please fill in all fields"
            );

            return;
        }

        try {

            LoginPassengerRequest loginRequest =
                    new LoginPassengerRequest();

            loginRequest.setUsername(username);
            loginRequest.setPassword(password);

            LoginPassengerResponse response =
                    authService.login(loginRequest);

            JOptionPane.showMessageDialog(
                    this,
                    response.getMessage()
            );

            dispose();
            new HomeFrame(username);

        } catch (Exception exception) {

            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    private void handleRegistration(
            String username,
            String email,
            String password,
            String confirmPassword) {

        if (username.isBlank() || email.isBlank()
                || password.isBlank() || confirmPassword.isBlank()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please fill in all fields"
            );

            return;
        }

        if (!password.equals(confirmPassword)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Passwords do not match!"
            );

            return;
        }

        try {

            RegisterPassengerRequest registerRequest =
                    new RegisterPassengerRequest();

            registerRequest.setUsername(username);
            registerRequest.setEmail(email);
            registerRequest.setPassword(password);

            RegisterPassengerResponse response =
                    authService.register(registerRequest);

            JOptionPane.showMessageDialog(
                    this,
                    response.getMessage()
            );

            cardLayout.show(mainPanel, "LOGIN");

        } catch (Exception exception) {

            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

}
