package csc445.groupc.distauction.View;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import csc445.groupc.distauction.Communication.MessageReceiving;
import csc445.groupc.distauction.Communication.MessageSending;
import csc445.groupc.distauction.GameLogic.Bid;
import csc445.groupc.distauction.GameLogic.GameState;
import csc445.groupc.distauction.GameLogic.GameStep;
import csc445.groupc.distauction.GameLogic.Timeout;
import csc445.groupc.distauction.Paxos.Paxos;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

public class GameView {
    private JLabel bidLabel;
    private JButton bidButton;
    private JPanel playersPanel;
    public JPanel mainPanel;
    private GameState gameState;

    private final Phaser phaser;

    public GameView(ArrayList<String> usernames, int id, String multicastAddr) {
        phaser = new Phaser(1);

        String[] players = usernames.toArray(new String[0]);
        AtomicInteger prevGameRound = new AtomicInteger(1);
        gameState = new GameState(LocalDateTime.now(), players, (gs) -> {
            System.out.println(gs);
            updateUsers(gs.getPlayerScores());
            updateBid(gs.getMostRecentBid());

            if (gs.getRound() > prevGameRound.get()) {
                prevGameRound.set(gs.getRound());
                phaser.arrive();
            }
        });
        updateUsers(gameState.getPlayerScores());

        Paxos<GameStep> paxos = new Paxos<>(id, usernames.size(), (s) -> {
            gameState.applyStep(s);
        });
        paxos.run();

        onThread(() -> {
            try {
                MessageReceiving.run(multicastAddr, 5353, paxos.getReceivingQueue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        onThread(() -> {
            try {
                MessageSending.run(multicastAddr, 5354, 5353, paxos.getSendingQueue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        bidButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Bid Pressed");
                try {
                    paxos.proposeStep(gameState.generateRandomBid(usernames.get(id)));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });

        onThread(() -> {
            int waitingRound;
            while (true) {
                waitingRound = gameState.getRound();
                try {
                    phaser.awaitAdvanceInterruptibly(phaser.getPhase(), GameState.TIMEOUT_LENGTH, GameState.TIMEOUT_UNIT);
                } catch (Exception ex) {
                } finally {
                    try {
                        if (gameState.getRound() <= waitingRound) {
                            paxos.proposeStep(new Timeout(waitingRound));
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    public void updateUsers(HashMap<String, Integer> playerScores) {
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        //c.insets = new Insets(10,0,0,10);
        String[] usernames = playerScores.keySet().toArray(new String[0]);
        int x = 0;
        int y = 0;
        for (int i = 0; i < playerScores.size(); i++) {
            JLabel bidLabel = new JLabel();
            //bidLabel.setText("username($0)");
            bidLabel.setText(usernames[i] + "($" + playerScores.get(usernames[i]) + ")");
            bidLabel.setFont(bidLabel.getFont().deriveFont(18.0f));

            c.gridx = x;
            c.gridy = y;
            playersPanel.add(bidLabel, c);

            x++;
            if (x == 3) {
                y++;
                x = 0;
            }
        }
    }

    public void updateBid(Optional<Bid> bid) {
        if (bid.isPresent()) {
            bidLabel.setText(bid.get().getBidder() + " ($" + bid.get().getBidAmount() + ")");
        } else {
            bidLabel.setText("$0");
        }

    }

    private static void onThread(final Runnable runnable) {
        new Thread(runnable).start();
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
        mainPanel = new JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 2, new Insets(5, 5, 5, 5), -1, -1));
        mainPanel.setMinimumSize(new Dimension(400, 300));
        mainPanel.setPreferredSize(new Dimension(450, 300));
        final JLabel label1 = new JLabel();
        label1.setFont(new Font(label1.getFont().getName(), label1.getFont().getStyle(), 20));
        label1.setText("Current Bid:");
        mainPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bidLabel = new JLabel();
        bidLabel.setFont(new Font(bidLabel.getFont().getName(), bidLabel.getFont().getStyle(), 20));
        bidLabel.setHorizontalAlignment(0);
        bidLabel.setHorizontalTextPosition(0);
        bidLabel.setText("username: $0");
        mainPanel.add(bidLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bidButton = new JButton();
        bidButton.setFont(new Font(bidButton.getFont().getName(), bidButton.getFont().getStyle(), 20));
        bidButton.setLabel("Bid");
        bidButton.setText("Bid");
        mainPanel.add(bidButton, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        playersPanel = new JPanel();
        playersPanel.setLayout(new GridBagLayout());
        playersPanel.setFont(new Font(playersPanel.getFont().getName(), playersPanel.getFont().getStyle(), 20));
        mainPanel.add(playersPanel, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, 1, 1, null, new Dimension(400, 150), null, 0, false));
        playersPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-4521979)), null, TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(-1, 15), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }


//    public static void main(String[] args) {
//        JFrame frame = new JFrame("BidGame");
//        frame.setPreferredSize(new Dimension(450,300));
//        frame.setContentPane(new GameView().mainPanel);
//        frame.setLocationRelativeTo(null);
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//
//    }
}
