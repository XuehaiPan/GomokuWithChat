package Gomoku.Chat;

import Gomoku.Client;

import javax.swing.*;
import java.awt.*;

public class ChatPanel extends JPanel {
    private final Client client;
    private final JTextArea historyTextArea;
    private final JButton sendButton;
    private final JTextField inputTextField;
    private final JScrollPane historyScrollPane;
    private String incident;
    
    
    public ChatPanel(Client client) {
        this.client = client;
        historyTextArea = new JTextArea();
        sendButton = new JButton("发送");
        inputTextField = new JTextField();
        historyScrollPane = new JScrollPane(historyTextArea);
        
        initLayout();
        initActionListeners();
    }
    
    
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        initLayout();
    }
    
    
    private void initLayout() {
        int width = getWidth(), height = getHeight();
        removeAll();
        setLayout(null);
        historyScrollPane.setBounds(0, 0, width, 8 * height / 9);
        inputTextField.setBounds(0, 8 * height / 9, 4 * width / 5, height - 8 * height / 9);
        sendButton.setBounds(4 * width / 5, 8 * height / 9, width - 4 * width / 5, height - 8 * height / 9);
        historyTextArea.setLineWrap(false);
        historyTextArea.setEditable(false);
        historyScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        historyScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(historyScrollPane);
        add(inputTextField);
        add(sendButton);
        FontMetrics fontMetrics = historyTextArea.getFontMetrics(historyTextArea.getFont());
        StringBuilder builder = new StringBuilder();
        while (fontMetrics.stringWidth(builder.toString()) <= 3 * width / 5)
            builder.append(" ");
        builder.deleteCharAt(builder.length() - 1);
        incident = builder.toString();
    }
    
    
    private void initActionListeners() {
        sendButton.addActionListener(e -> {
            String text = inputTextField.getText();
            if (!text.equals("")) {
                inputTextField.setText("");
                addMessageFromThisSide(text);
                client.sendChatText(text);
            }
        });
    }
    
    
    private void addLine(String line) {
        historyTextArea.append(line);
        historyTextArea.append("\n");
    }
    
    
    private void addMessage(String text, String incident, int bound) {
        FontMetrics fontMetrics = historyTextArea.getFontMetrics(historyTextArea.getFont());
        StringBuilder line = new StringBuilder(incident);
        line.append(text);
        if (fontMetrics.stringWidth(line.toString()) < bound) {
            if (!incident.equals("")) {
                while (fontMetrics.stringWidth(line.toString()) <= bound)
                    line.insert(0, " ");
                line.deleteCharAt(0);
            }
            addLine(line.toString());
        }
        else {
            StringBuilder input = new StringBuilder(text);
            line = new StringBuilder(incident);
            for (int i = 0; i < input.length(); ++i) {
                line.append(input.charAt(i));
                if (fontMetrics.stringWidth(line.toString()) > bound) {
                    line.deleteCharAt(line.length() - 1);
                    input.delete(0, i - 1);
                    i = 0;
                    addLine(line.toString());
                    line = new StringBuilder(incident);
                }
                else if (i == input.length() - 1)
                    addLine(line.toString());
            }
        }
    }
    
    
    public void addMessageFromThisSide(String chatText) {
        addMessage(chatText, this.incident, getWidth() - 2 * historyScrollPane.getVerticalScrollBar().getWidth());
    }
    
    
    public void addMessageFromOtherSide(String chatText) {
        addMessage(chatText, "", 2 * getWidth() / 5 - historyScrollPane.getVerticalScrollBar().getWidth());
    }
}