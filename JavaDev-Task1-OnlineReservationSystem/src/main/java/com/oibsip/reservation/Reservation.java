package com.oibsip.reservation;

import com.oibsip.reservation.views.LoginFrame;

import javax.swing.*;

public class Reservation {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
