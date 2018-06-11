package Gomoku;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.ArrayList;

public abstract class AbstractSocket {
    protected int socketId; // socket 编号
    public static final int headLength = 6; // 报文头长度
    public static final int NEW_GAME = 0;                 // server 向双方 client 发送新建游戏命令
    public static final int INQUIRE_TO_NEW_GAME = 1;      // client 请求新建游戏，server 直接转发对方 client。
    public static final int ACCEPT_TO_NEW_GAME = 2;       // client 同意新建游戏，server 新建游戏，并向双方 client 发送新建游戏命令。
    public static final int REJECT_TO_NEW_GAME = 3;       // client 拒绝新建游戏，server 直接转发对方 client。
    public static final int GAME_OVER = 4;                // server 向双方 client 发送游戏结束命令
    public static final int ADMIT_DEFEAT = 5;             // client 认输，server 结束游戏，server 接收后向双方 client 发送游戏结束命令。
    public static final int PUT_STONE = 6;                // server 向双方 client 发送落子命令
    public static final int INQUIRE_TO_PUT_STONE = 7;     // client 请求落子，server 进行处理，若可以落子则向双方 client 发送落子命令。
    public static final int RETRACT_STONE = 8;            // server 向双方 client 发送悔棋命令
    public static final int INQUIRE_TO_RETRACT_STONE = 9; // client 请求悔棋，server 直接转发对方 client。
    public static final int ACCEPT_TO_RETRACT_STONE = 10; // client 同意悔棋，server 悔棋，并向双方 client 发送悔棋命令。
    public static final int REJECT_TO_RETRACT_STONE = 11; // client 拒绝悔棋，server 直接转发对方 client。
    public static final int CHOOSE_PLAYER_COLOR = 12;     // client 选择执子颜色
    public static final int SET_PLAYER_COLOR = 13;        // server 指定玩家执子颜色
    public static final int CHAT_TEXT = 14;               // client 发送聊天消息，server 直接转发对方 client。
    
    
    /**
     * 打印报文内容
     *
     * @param message 接收到的报文
     */
    public void printMessage(byte[] message) {
        int srcSocketId = parseSocketId(message);
        StringBuilder builder = new StringBuilder();
        if (socketId == 0)
            builder.append("server");
        else
            builder.append("client").append(socketId);
        builder.append(" receive from ");
        if (srcSocketId == 0)
            builder.append("server");
        else
            builder.append("client").append(srcSocketId);
        builder.append(": { ");
        boolean flag = false;
        for (byte b : message) {
            if (flag)
                builder.append(", ");
            flag = true;
            builder.append(b);
        }
        builder.append(" }");
        System.out.println(builder.toString());
    }
    
    
    /**
     * 获取 Socket 编号
     */
    public int getSocketId() {
        return socketId;
    }
    
    
    /**
     * 设置 Socket 编号
     */
    public void setSocketId(int socketId) {
        this.socketId = socketId;
    }
    
    
    /**
     * 获取报文长度
     *
     * @param head 接收到的报文头
     */
    public static int getMessageLength(byte[] head) {
        return ((head[1] << 24) & 0xFF) + ((head[2] << 16) & 0xFF) + ((head[3] << 8) & 0xFF) + (head[4] & 0xFF);
    }
    
    
    /**
     * 从输入流接收报文
     *
     * @param is 输入流
     */
    public static byte[] receivePackage(InputStream is) throws IOException {
        byte[] headBuffer = new byte[headLength];
        is.read(headBuffer);
        byte[] restBuffer = new byte[getMessageLength(headBuffer)];
        is.read(restBuffer);
        byte[] message = new byte[headLength + restBuffer.length];
        System.arraycopy(headBuffer, 0, message, 0, headLength);
        System.arraycopy(restBuffer, 0, message, headLength, restBuffer.length);
        return message;
    }
    
    
    /**
     * 向输出流发送报文
     *
     * @param os      输出流
     * @param message 待发送的报文
     */
    public static void sendPackage(OutputStream os, byte[] message) {
        while (true) {
            try {
                os.write(message);
                os.flush();
                break;
            }
            catch (IOException ignored) {
            }
        }
    }
    
    
    /**
     * 解析接收到的报文类型
     *
     * @param message 接收到的报文
     */
    protected static int parseMessageType(byte[] message) {
        return message[headLength - 1];
    }
    
    
    /**
     * 解析报文的源 Socket 编号
     *
     * @param message 接收到的报文
     */
    protected static int parseSocketId(byte[] message) {
        return message[0];
    }
    
    
    /**
     * 处理接收到的报文
     *
     * @param message 接收到的报文
     */
    protected void handleMessage(byte[] message) {
        int messageType = parseMessageType(message);
        switch (messageType) {
            case NEW_GAME:                 // server 向双方 client 发送新建游戏命令
                handleNewGame(message);
                break;
            case INQUIRE_TO_NEW_GAME:      // client 请求新建游戏，server 直接转发对方 client。
                handleInquireToNewGame(message);
                break;
            case ACCEPT_TO_NEW_GAME:       // client 同意新建游戏，server 新建游戏，并向双方 client 发送新建游戏命令。
                handleAcceptToNewGame(message);
                break;
            case REJECT_TO_NEW_GAME:       // client 拒绝新建游戏，server 直接转发对方 client。
                handleRejectToNewGame(message);
                break;
            case GAME_OVER:                // server 向双方 client 发送游戏结束命令
                handleGameOver(message);
                break;
            case ADMIT_DEFEAT:             // client 认输，server 结束游戏，server 接收后向双方 client 发送游戏结束命令。
                handleAdmitDefeat(message);
                break;
            case PUT_STONE:                // server 向双方 client 发送落子命令
                handlePutStone(message);
                break;
            case INQUIRE_TO_PUT_STONE:     // client 请求落子，server 进行处理，若可以落子则向双方 client 发送落子命令。
                handleInquireToPutStone(message);
                break;
            case RETRACT_STONE:            // server 向双方 client 发送悔棋命令
                handleRetractStone(message);
                break;
            case INQUIRE_TO_RETRACT_STONE: // client 请求悔棋，server 直接转发对方 client。
                handleInquireToRetractStone(message);
                break;
            case ACCEPT_TO_RETRACT_STONE:  // client 同意悔棋，server 悔棋，并向双方 client 发送悔棋命令。
                handleAcceptToRetractStone(message);
                break;
            case REJECT_TO_RETRACT_STONE:  // client 拒绝悔棋，server 直接转发对方 client。
                handleRejectToRetractStone(message);
                break;
            case CHOOSE_PLAYER_COLOR:      // client 选择执子颜色
                handleChoosePlayerColor(message);
                break;
            case SET_PLAYER_COLOR:         // server 指定玩家执子颜色
                handleSetPlayerColor(message);
                break;
            case CHAT_TEXT:
                handleChatText(message);
                break;
        }
    }
    
    
    /**
     * server 向双方 client 发送新建游戏命令
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = NEW_GAME
     * @implNote server 不可能接收到这个消息
     */
    protected abstract void handleNewGame(byte[] message);
    
    /**
     * client 请求新建游戏，server 直接转发对方 client。
     * client 弹出窗口，让用户选择是否开始。
     * server 直接转发对方 client
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = INQUIRE_TO_NEW_GAME
     */
    protected abstract void handleInquireToNewGame(byte[] message);
    
    /**
     * client 同意新建游戏，server 新建游戏，并向双方 client 发送新建游戏命令。
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = ACCEPT_TO_NEW_GAME
     * @implNote client 不可能接收到这个消息
     */
    protected abstract void handleAcceptToNewGame(byte[] message);
    
    /**
     * client 拒绝新建游戏，server 直接转发对方 client。
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = REJECT_TO_NEW_GAME
     */
    protected abstract void handleRejectToNewGame(byte[] message);
    
    /**
     * server 向双方 client 发送游戏结束命令
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = GAME_OVER
     * @implNote server 不可能接收到这个消息
     */
    protected abstract void handleGameOver(byte[] message);
    
    /**
     * client 认输，server 结束游戏，server 接收后向双方 client 发送游戏结束命令。
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = ADMIT_DEFEAT
     * @implNote client 不可能接收到这个消息
     */
    protected abstract void handleAdmitDefeat(byte[] message);
    
    /**
     * server 向双方 client 发送落子命令
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = PUT_STONE
     * @implNote server 不可能接收到这个消息
     */
    protected abstract void handlePutStone(byte[] message);
    
    /**
     * client 请求落子，server 进行处理，若可以落子则向双方 client 发送落子命令。
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = INQUIRE_TO_PUT_STONE
     * @implNote client 不可能接收到这个消息
     */
    protected abstract void handleInquireToPutStone(byte[] message);
    
    /**
     * server 向双方 client 发送悔棋命令
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = RETRACT_STONE
     * @implNote server 不可能接收到这个消息
     */
    protected abstract void handleRetractStone(byte[] message);
    
    /**
     * client 请求悔棋，server 直接转发对方 client。
     * client 弹出窗口，让用户选择是否同意。
     * server 直接转发对方 client
     *
     * @param message 报文
     *
     * @implNote messageType = INQUIRE_TO_RETRACT_STONE
     */
    protected abstract void handleInquireToRetractStone(byte[] message);
    
    /**
     * client 同意悔棋，server 悔棋，并向双方 client 发送悔棋命令。
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = ACCEPT_TO_RETRACT_STONE
     * @implNote client 不可能接收到这个消息
     */
    protected abstract void handleAcceptToRetractStone(byte[] message);
    
    /**
     * client 拒绝悔棋，server 直接转发对方 client。
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = REJECT_TO_RETRACT_STONE
     */
    protected abstract void handleRejectToRetractStone(byte[] message);
    
    /**
     * client 选择执子颜色
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = CHOOSE_PLAYER_COLOR
     * @implNote client 不可能接收到这个消息
     */
    protected abstract void handleChoosePlayerColor(byte[] message);
    
    /**
     * server 指定玩家执子颜色
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = SET_PLAYER_COLOR
     * @implNote server 不可能接收到这个消息
     */
    protected abstract void handleSetPlayerColor(byte[] message);
    
    /**
     * client 发送聊天消息，server 直接转发对方 client。
     *
     * @param message 接收到的报文
     *
     * @implNote messageType = CHAT_TEXT
     */
    protected abstract void handleChatText(byte[] message);
    
    
    /**
     * 向待发送的报文添加报文头
     *
     * @param messageType 向待发送的报文的报文类型
     * @param message     向待发送的报文内容（未加入报文头）
     */
    protected byte[] packMessage(int messageType, byte[] message) {
        if (message == null)
            message = new byte[0];
        byte[] packedMessage = new byte[headLength + message.length];
        System.arraycopy(message, 0, packedMessage, headLength, message.length);
        packedMessage[0] = (byte) socketId;
        int length = message.length;
        packedMessage[1] = (byte) ((length >>> 24) & 0xFF);
        packedMessage[2] = (byte) ((length >>> 16) & 0xFF);
        packedMessage[3] = (byte) ((length >>> 8) & 0xFF);
        packedMessage[4] = (byte) (length & 0xFF);
        packedMessage[5] = (byte) messageType;
        return packedMessage;
    }
    
    
    /**
     * 打包新建游戏报文
     *
     * @param playerNumber 玩家编号
     *
     * @implNote @messageType NEW_GAME
     * @implNote @messageArg  playerNumber 玩家编号
     */
    protected byte[] packNewGame(int playerNumber) {
        byte[] message = {(byte) playerNumber};
        return packMessage(NEW_GAME, message);
    }
    
    
    /**
     * 拆包新建游戏报文
     *
     * @implNote @messageType NEW_GAME
     * @implNote @messageArg  playerNumber 玩家编号
     */
    protected Object[] unpackNewGame(byte[] message) {
        int playerNumber = message[headLength];
        return new Object[]{playerNumber};
    }
    
    
    /**
     * 打包结束游戏报文
     *
     * @param winnerNumber     胜者编号
     * @param indexOfRowStones 连珠的棋子编号
     * @param rowStones        连珠的棋子
     *
     * @implNote @messageType GAME_OVER
     * @implNote @messageArg  winnerNumber     胜者编号
     * @implNote @messageArg  indexOfRowStones 连珠的棋子编号
     * @implNote @messageArg  rowStones        连珠的棋子
     */
    protected byte[] packGameOver(int winnerNumber, List<Integer> indexOfRowStones, List<Stone> rowStones) {
        int rowStoneNumber = rowStones.size();
        byte[] message = new byte[3 + 3 * rowStoneNumber];
        message[0] = (byte) winnerNumber;
        message[1] = (byte) rowStoneNumber;
        if (rowStoneNumber > 0) {
            message[2] = (byte) (rowStones.get(0).getType() == StoneType.BLACK ? 1 : 2);
            for (int index = 0; index < rowStoneNumber; ++index)
                message[3 + index] = indexOfRowStones.get(index).byteValue();
            for (int index = 0; index < rowStoneNumber; ++index) {
                message[3 + rowStoneNumber + 2 * index] = (byte) rowStones.get(index).getI();
                message[3 + rowStoneNumber + 2 * index + 1] = (byte) rowStones.get(index).getJ();
            }
        }
        return packMessage(GAME_OVER, message);
    }
    
    
    /**
     * 拆包结束游戏报文
     *
     * @implNote @messageType GAME_OVER
     * @implNote @messageArg  winnerNumber     胜者编号
     * @implNote @messageArg  indexOfRowStones 连珠的棋子编号
     * @implNote @messageArg  rowStones        连珠的棋子
     */
    protected Object[] unpackGameOver(byte[] message) {
        int winnerNumber = message[headLength];
        int rowStoneNumber = message[headLength + 1];
        StoneType stoneType = (message[headLength + 2] == 1 ? StoneType.BLACK : StoneType.WHITE);
        List<Integer> indexOfRowStones = new ArrayList<Integer>();
        List<Stone> rowStones = new ArrayList<Stone>();
        for (int index = 0; index < rowStoneNumber; ++index)
            indexOfRowStones.add((int) message[headLength + 3 + index]);
        for (int index = 0; index < rowStoneNumber; ++index) {
            try {
                int i = message[headLength + 3 + rowStoneNumber + 2 * index];
                int j = message[headLength + 3 + rowStoneNumber + 2 * index + 1];
                rowStones.add(new Stone(i, j, stoneType));
            }
            catch (StoneOutOfBoardRangeException ignored) {
            }
        }
        return new Object[]{winnerNumber, indexOfRowStones, rowStones};
    }
    
    
    /**
     * 打包落子报文
     *
     * @param stone         落子的 stone
     * @param previousStone 落子的 stone 的前一个 stone，若没有则传入 null。
     * @param historySize   落子完成后棋盘上的棋子数
     *
     * @implNote @messageType PUT_STONE
     * @implNote @messageArg  stone         落子的 stone
     * @implNote @messageArg  previousStone 落子的 stone 的前一个 stone，若没有则传入 null。
     * @implNote @messageArg  historySize   落子完成后棋盘上的棋子数
     */
    protected byte[] packPutStone(Stone stone, Stone previousStone, int historySize) {
        byte[] message = new byte[7];
        message[0] = (byte) stone.getI();
        message[1] = (byte) stone.getJ();
        message[2] = (byte) (stone.getType() == StoneType.BLACK ? 1 : 2);
        if (previousStone != null) {
            message[3] = (byte) previousStone.getI();
            message[4] = (byte) previousStone.getJ();
            message[5] = (byte) (previousStone.getType() == StoneType.BLACK ? 1 : 2);
        }
        else
            message[3] = message[4] = message[5] = 0;
        message[6] = (byte) historySize;
        return packMessage(PUT_STONE, message);
    }
    
    
    /**
     * 拆包落子报文
     *
     * @implNote @messageType PUT_STONE
     * @implNote @messageArg  stone         落子的 stone
     * @implNote @messageArg  previousStone 落子的 stone 的前一个 stone，若没有则传入 null。
     * @implNote @messageArg  historySize   落子完成后棋盘上的棋子数
     */
    protected Object[] unpackPutStone(byte[] message) {
        Stone stone = null, previousStone = null;
        try {
            stone = new Stone(message[headLength], message[headLength + 1], (message[headLength + 2] == 1 ? StoneType.BLACK : StoneType.WHITE));
            previousStone = new Stone(message[headLength + 3], message[headLength + 4], (message[headLength + 5] == 1 ? StoneType.BLACK : StoneType.WHITE));
        }
        catch (StoneOutOfBoardRangeException ignored) {
        }
        int historySize = message[headLength + 6];
        return new Object[]{stone, previousStone, historySize};
    }
    
    
    /**
     * 打包请求落子报文
     *
     * @param i 落子的 stone 的棋盘格点横坐标
     * @param j 落子的 stone 的棋盘格点纵坐标
     *
     * @implNote @messageType INQUIRE_TO_PUT_STONE
     * @implNote @messageArg  i 落子的 stone 的棋盘格点横坐标
     * @implNote @messageArg  j 落子的 stone 的棋盘格点纵坐标
     */
    protected byte[] packInquireToPutStone(int i, int j) {
        byte[] message = {(byte) i, (byte) j};
        message = packMessage(INQUIRE_TO_PUT_STONE, message);
        return message;
    }
    
    
    /**
     * 拆包请求落子报文
     *
     * @implNote @messageType INQUIRE_TO_PUT_STONE
     * @implNote @messageArg  i 落子的 stone 的棋盘格点横坐标
     * @implNote @messageArg  j 落子的 stone 的棋盘格点纵坐标
     */
    protected Object[] unpackInquireToPutStone(byte[] message) {
        int i = message[headLength];
        int j = message[headLength + 1];
        return new Object[]{i, j};
    }
    
    
    /**
     * 打包悔棋报文
     *
     * @param stone         被移走的 stone
     * @param previousStone 被移走的 stone 的前一个 stone，因为可以悔棋时棋盘上至少有 4 个棋子，必然是非 null。
     * @param historySize   悔棋完成后棋盘上的棋子数
     *
     * @implNote @messageType RETRACT_STONE
     * @implNote @messageArg  stone         被移走的 stone
     * @implNote @messageArg  previousStone 被移走的 stone 的前一个 stone，因为可以悔棋时棋盘上至少有 4 个棋子，必然是非 null。
     * @implNote @messageArg  historySize   悔棋完成后棋盘上的棋子数
     */
    protected byte[] packRetractStone(Stone stone, Stone previousStone, int historySize) {
        byte[] putStoneMessage = packPutStone(stone, previousStone, historySize);
        putStoneMessage[headLength - 1] = (byte) RETRACT_STONE;
        return putStoneMessage;
    }
    
    
    /**
     * 拆包悔棋报文
     *
     * @implNote @messageType RETRACT_STONE
     * @implNote @messageArg  stone         被移走的 stone
     * @implNote @messageArg  previousStone 被移走的 stone 的前一个 stone，因为可以悔棋时棋盘上至少有 4 个棋子，必然是非 null。
     * @implNote @messageArg  historySize   悔棋完成后棋盘上的棋子数
     */
    protected Object[] unpackRetractStone(byte[] message) {
        return unpackPutStone(message);
    }
    
    
    /**
     * 打包选择执子颜色报文
     *
     * @param state 按钮按键
     *
     * @implNote @messageType CHOOSE_PLAYER_COLOR
     * @implNote @messageArg  state 按钮按键
     */
    protected byte[] packChoosePlayerColor(int state) {
        byte[] message = {(byte) state};
        return packMessage(CHOOSE_PLAYER_COLOR, message);
    }
    
    
    /**
     * 拆包选择执子颜色报文
     *
     * @implNote @messageType CHOOSE_PLAYER_COLOR
     * @implNote @messageArg  state 按钮按键
     */
    protected Object[] unpackChoosePlayerColor(byte[] message) {
        int state = message[headLength];
        return new Object[]{state};
    }
    
    
    /**
     * 打包设置执子颜色报文
     *
     * @param stoneType         玩家棋子类型
     * @param presetStoneNumber 预先放置的棋子数
     *
     * @implNote @messageType SET_PLAYER_COLOR
     * @implNote @messageArg  playerStoneType   玩家棋子类型
     * @implNote @messageArg  presetStoneNumber 预先放置的棋子数
     */
    protected byte[] packSetPlayerColor(StoneType stoneType, int presetStoneNumber) {
        byte[] message = {0, (byte) presetStoneNumber};
        if (stoneType == StoneType.BLACK)
            message[0] = 1;
        else if (stoneType == StoneType.WHITE)
            message[0] = 2;
        return packMessage(SET_PLAYER_COLOR, message);
    }
    
    
    /**
     * 拆包设置执子颜色报文
     *
     * @implNote @messageType SET_PLAYER_COLOR
     * @implNote @messageArg  playerStoneType   玩家棋子类型
     * @implNote @messageArg  presetStoneNumber 预先放置的棋子数
     */
    protected Object[] unpackSetPlayerColor(byte[] message) {
        StoneType stoneType = StoneType.SPACE;
        if (message[headLength] == 1)
            stoneType = StoneType.BLACK;
        else if (message[headLength] == 2)
            stoneType = StoneType.WHITE;
        int presetStoneNumber = message[headLength + 1];
        return new Object[]{stoneType, presetStoneNumber};
    }
    
    
    /**
     * 打包聊天消息报文
     *
     * @param chatText 聊天消息
     *
     * @implNote @messageType CHAT_TEXT
     * @implNote @messageArg  chatText 聊天消息
     */
    protected byte[] packChatText(String chatText) {
        return packMessage(CHAT_TEXT, chatText.getBytes());
    }
    
    
    /**
     * 拆包聊天消息报文
     *
     * @implNote @messageType CHAT_TEXT
     * @implNote @messageArg  chatText 聊天消息
     */
    protected Object[] unpackChatText(byte[] message) {
        String chatText = new String(message, headLength, message.length - headLength);
        return new Object[]{chatText};
    }
}




