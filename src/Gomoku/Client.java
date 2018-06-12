package Gomoku;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

public class Client extends AbstractSocket {
    private final Gomoku gomoku;
    private final Socket client;
    private final LinkedBlockingDeque<byte[]> messageQueue; // 消息队列，用于存储接收到的消息。
    
    
    public Client(Socket client) {
        this.client = client;
        gomoku = new Gomoku(this);
        messageQueue = new LinkedBlockingDeque<byte[]>();
        
        initService();
    }
    
    
    private void initService() {
        Thread receiveFromServer = new Thread(this::receiveFromServer);
        
        Thread service = new Thread(() -> {
            while (true) {
                if (messageQueue.isEmpty())
                    continue;
                else {
                    byte[] message = messageQueue.pop();
                    handleMessage(message);
                }
            }
        });
        
        receiveFromServer.start();
        service.start();
    }
    
    
    public void sendToServer(byte[] message) {
        try {
            sendPacket(client.getOutputStream(), message);
        }
        catch (IOException ignored) {
        }
    }
    
    
    private void receiveFromServer() {
        while (!client.isClosed()) {
            try {
                InputStream is = client.getInputStream();
                byte[] message = receivePacket(is);
                printMessage(message);
                messageQueue.add(message);
            }
            catch (IOException ignored) {
            }
        }
    }
    
    
    public void setClientId(int clientId) {
        socketId = clientId;
        gomoku.setTitle("五子棋 " + clientId);
    }
    
    
    /**
     * /**
     * server 向双方 client 发送新建游戏命令
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = NEW_GAME
     * @implNote server 不可能接收到这个消息
     */
    @Override
    protected void handleNewGame(byte[] message) {
        Object[] messageArgs = unpackNewGame(message);
        int playerNumber = (Integer) messageArgs[0]; // 从 message 解析 playerNumber
        gomoku.newGame(playerNumber);
    }
    
    
    /**
     * client 请求新建游戏，server 直接转发对方 client。
     * client 弹出窗口，让用户选择是否开始。
     * server 直接转发对方 client
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = INQUIRE_TO_NEW_GAME
     */
    @Override
    protected void handleInquireToNewGame(byte[] message) {
        String[] options = {"同意", "拒绝"};
        int state = JOptionPane.showOptionDialog(gomoku,
                                                 "对方请求新建游戏",
                                                 "",
                                                 JOptionPane.YES_NO_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null,
                                                 options,
                                                 options[0]);
        byte[] newMessage = packMessage(state == JOptionPane.YES_OPTION ? ACCEPT_TO_NEW_GAME : REJECT_TO_NEW_GAME, null);
        sendToServer(newMessage);
    }
    
    
    /**
     * client 同意新建游戏，server 新建游戏，并向双方 client 发送新建游戏命令。
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = ACCEPT_TO_NEW_GAME
     * @implNote client 不可能接收到这个消息
     */
    @Override
    protected void handleAcceptToNewGame(byte[] message) {
    }
    
    
    /**
     * client 拒绝新建游戏，server 直接转发对方 client。
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = REJECT_TO_NEW_GAME
     */
    @Override
    protected void handleRejectToNewGame(byte[] message) {
        JOptionPane.showMessageDialog(gomoku, "对方拒绝新建游戏", "", JOptionPane.INFORMATION_MESSAGE);
    }
    
    
    /**
     * server 向双方 client 发送游戏结束命令
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = GAME_OVER
     * @implNote server 不可能接收到这个消息
     */
    @Override
    protected void handleGameOver(byte[] message) {
        Object[] messageArgs = unpackGameOver(message);
        int winnerNumber = (Integer) messageArgs[0]; // 从 message 解析 (winnerNumber, indexOfRowStones, rowStones)
        List<Integer> indexOfRowStones = (ArrayList<Integer>) messageArgs[1];
        List<Stone> rowStones = (ArrayList<Stone>) messageArgs[2];
        gomoku.gameOver(winnerNumber, indexOfRowStones, rowStones);
    }
    
    
    /**
     * client 认输，server 结束游戏，server 接收后向双方 client 发送游戏结束命令。
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = ADMIT_DEFEAT
     * @implNote client 不可能接收到这个消息
     */
    @Override
    protected void handleAdmitDefeat(byte[] message) {
    }
    
    
    /**
     * server 向双方 client 发送落子命令
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = PUT_STONE
     * @implNote server 不可能接收到这个消息
     */
    @Override
    protected void handlePutStone(byte[] message) {
        Object[] messageArgs = unpackPutStone(message);
        Stone stone = (Stone) messageArgs[0]; // 从 message 解析 (stone, previousStone, historySize)
        Stone previousStone = (Stone) messageArgs[1];
        int historySize = (Integer) messageArgs[2];
        gomoku.putStone(stone, previousStone, historySize);
    }
    
    
    /**
     * client 请求落子，server 进行处理，若可以落子则向双方 client 发送落子命令。
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = INQUIRE_TO_PUT_STONE
     * @implNote client 不可能接收到这个消息
     */
    @Override
    protected void handleInquireToPutStone(byte[] message) {
    }
    
    
    /**
     * server 向双方 client 发送悔棋命令
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = RETRACT_STONE
     * @implNote server 不可能接收到这个消息
     */
    @Override
    protected void handleRetractStone(byte[] message) {
        Object[] messageArgs = unpackRetractStone(message);
        Stone stone = (Stone) messageArgs[0]; // 从 message 解析 (stone, previousStone, historySize)
        Stone previousStone = (Stone) messageArgs[1];
        int historySize = (Integer) messageArgs[2];
        gomoku.retractStone(stone, previousStone, historySize);
    }
    
    
    /**
     * client 请求悔棋，server 直接转发对方 client。
     * client 弹出窗口，让用户选择是否同意。
     * server 直接转发对方 client
     *
     * @param message 报文
     *
     * @implNote messageType = INQUIRE_TO_RETRACT_STONE
     */
    @Override
    protected void handleInquireToRetractStone(byte[] message) {
        String[] options = {"同意", "拒绝"};
        int state = JOptionPane.showOptionDialog(gomoku,
                                                 "对方请求悔棋",
                                                 "",
                                                 JOptionPane.YES_NO_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null,
                                                 options,
                                                 options[0]);
        byte[] newMessage = packMessage(state == JOptionPane.YES_OPTION ? ACCEPT_TO_RETRACT_STONE : REJECT_TO_RETRACT_STONE, null);
        sendToServer(newMessage);
    }
    
    
    /**
     * client 同意悔棋，server 悔棋，并向双方 client 发送悔棋命令。
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = ACCEPT_TO_RETRACT_STONE
     * @implNote client 不可能接收到这个消息
     */
    @Override
    protected void handleAcceptToRetractStone(byte[] message) {
    }
    
    
    /**
     * client 拒绝悔棋，server 直接转发对方 client。
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = REJECT_TO_RETRACT_STONE
     */
    @Override
    protected void handleRejectToRetractStone(byte[] message) {
        JOptionPane.showMessageDialog(gomoku, "对方拒绝悔棋", "", JOptionPane.INFORMATION_MESSAGE);
    }
    
    
    /**
     * client 选择执子颜色
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = CHOOSE_PLAYER_COLOR
     * @implNote client 不可能接收到这个消息
     */
    @Override
    protected void handleChoosePlayerColor(byte[] message) {
    }
    
    
    /**
     * server 指定玩家执子颜色
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = SET_PLAYER_COLOR
     * @implNote server 不可能接收到这个消息
     */
    @Override
    protected void handleSetPlayerColor(byte[] message) {
        Object[] messageArgs = unpackSetPlayerColor(message);
        StoneType playerStoneType = (StoneType) messageArgs[0]; // 从 message 解析 (playerStoneType, playerNumber)
        int presetStoneNumber = (Integer) messageArgs[1];
        gomoku.setPlayerStoneType(playerStoneType, presetStoneNumber);
    }
    
    
    /**
     * client 发送聊天消息，server 直接转发对方 client。
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = CHAT_TEXT
     */
    @Override
    protected void handleChatText(byte[] message) {
        Object[] messageArgs = unpackChatText(message);
        String chatText = (String) messageArgs[0]; // 从 message 解析 chatText
        gomoku.addMessageFromOtherSide(chatText);
    }
    
    
    public void inquireToNewGame() {
        byte[] message = packMessage(INQUIRE_TO_NEW_GAME, null);
        sendToServer(message);
    }
    
    
    public void admitDefeat() {
        byte[] message = packMessage(ADMIT_DEFEAT, null);
        sendToServer(message);
    }
    
    
    public void inquireToPutStone(int i, int j) {
        byte[] message = packInquireToPutStone(i, j);
        sendToServer(message);
    }
    
    
    public void choosePlayerColor(int state) {
        byte[] message = packChoosePlayerColor(state);
        sendToServer(message);
    }
    
    
    public void inquireToRetractStone() {
        byte[] message = packMessage(INQUIRE_TO_RETRACT_STONE, null);
        sendToServer(message);
    }
    
    
    public void sendChatText(String chatText) {
        byte[] message = packChatText(chatText);
        sendToServer(message);
    }
}

