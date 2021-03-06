package csc445.groupc.distauction.View;

import csc445.groupc.distauction.GameLogic.GameStep;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

public class LoginView {
    private static final String GAME_INFO_FILE_PREFIX = ".gameinfo_";

    public static JFrame frame;
    private JPanel loginPanel;
    private JButton hostButton;
    private JButton joinButton;
    private JTextField usernameField;
    private JButton rejoinButton;

    private final String gameInfoFilePath;
    private final File gameIntoFile;

    public LoginView() {
        String hostname = "";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        gameInfoFilePath = GAME_INFO_FILE_PREFIX + hostname;
        gameIntoFile = new File(gameInfoFilePath);

        if (!gameIntoFile.exists()) {
            rejoinButton.setVisible(false);
        }

        hostButton.addActionListener(new BtnClicked());
        joinButton.addActionListener(new BtnClicked());
        rejoinButton.addActionListener(new BtnClicked());
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setMinimumSize(new Dimension(800, 600));
        loginPanel.setPreferredSize(new Dimension(400, 300));
        final JLabel label1 = new JLabel();
        label1.setOpaque(true);
        label1.setText("Please Enter your username:");
        label1.setVerifyInputWhenFocusTarget(true);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(label1, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        loginPanel.add(spacer2, gbc);
        usernameField = new JTextField();
        usernameField.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(usernameField, gbc);
        hostButton = new JButton();
        hostButton.setMinimumSize(new Dimension(99, 32));
        hostButton.setPreferredSize(new Dimension(99, 60));
        hostButton.setSelected(false);
        hostButton.setText("Host Game");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(hostButton, gbc);
        joinButton = new JButton();
        joinButton.setPreferredSize(new Dimension(95, 60));
        joinButton.setText("Join Game");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(joinButton, gbc);
        rejoinButton = new JButton();
        rejoinButton.setText("Rejoin Game");
        rejoinButton.setToolTipText("Rejoins the previously played game");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        loginPanel.add(rejoinButton, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return loginPanel;
    }

    private class BtnClicked implements ActionListener {
        String username;

        @Override
        public void actionPerformed(ActionEvent e) {
            this.username = usernameField.getText();

            if (e.getSource().equals(hostButton)) {
                System.out.println("Host Button");

                if (gameIntoFile.exists()) {
                    gameIntoFile.delete();
                }

                //switch panel to host
                frame.setContentPane(new HostView(username, gameInfoFilePath).hostPanel);
                frame.pack();
            } else if (e.getSource().equals(joinButton)) {
                System.out.println("Join Button");

                if (gameIntoFile.exists()) {
                    gameIntoFile.delete();
                }

                //switch panel to join
                frame.setContentPane(new JoinView(username, gameInfoFilePath).joinPanel);
                frame.pack();
            } else if (e.getSource().equals(rejoinButton)) {
                try {
                    final Scanner sc = new Scanner(gameIntoFile);

                    final String[] players = sc.nextLine().split(",");
                    final int id = sc.nextInt();
                    sc.nextLine();
                    final String multicastGroup = sc.nextLine();
                    sc.nextLine();

                    final ArrayList<GameStep> previousSteps = new ArrayList<>();
                    while (sc.hasNextLine()) {
                        previousSteps.add(GameStep.fromString(sc.nextLine()));
                    }

                    frame.setContentPane(new GameView(players,
                            id, multicastGroup, gameInfoFilePath, Optional.of(previousSteps)).mainPanel);
                    frame.pack();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
            System.out.println("Username: " + username);
        }
    }

    public static void main() {
        frame = new JFrame("BidGame");
        frame.setContentPane(new LoginView().loginPanel);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

}
