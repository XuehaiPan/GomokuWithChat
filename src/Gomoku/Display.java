/**
 * @author 潘学海
 */

package Gomoku;

import Gomoku.Timer.CountDownPanel;
import Gomoku.Timer.TimeManager;
import Gomoku.Timer.TimerPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Display extends JPanel {
    private final int boardBoundXL, boardBoundXR, boardBoundYU, boardBoundYD, boardCenterX, boardCenterY;
    private final int[] stoneCenterX;
    private final int[] stoneCenterY;
    private final JLabel messageLabel;
    private final DataChangeSupport<Boolean> gameStartedChangeSupport; // 表示游戏是否开始
    private final DataChangeSupport<Integer> historySizeChangeSupport; // 棋盘上的棋子个数
    private final Client client; // 本方客户端
    private int presetStoneNumber; // 预先放置的棋子数
    private StoneType playerStoneType; // 本方玩家执子颜色
    private int playerNumber; // 本方玩家号
    private final TimerPanel timerPanel;
    private final CountDownPanel countDownPanel;
    private final TimeManager timeManager;
    
    public static final int sideLength = 40;
    public static final int starRadius = 5;
    public static final int stoneRadius = 18;
    private static final Color backgroundColor = new Color(244, 240, 220);
    private static final Color black = new Color(32, 32, 32);
    private static final Color white = new Color(220, 220, 220);
    private static final Color gray = new Color(160, 160, 160);
    private static final Font indexFont = new Font(Font.DIALOG, Font.PLAIN, 3 * stoneRadius / 4);
    
    
    /**
     * Display 构造函数
     *
     * @param x      棋盘 (1, 1) 格点在面板上的横坐标
     * @param y      棋盘 (1, 1) 格点在面板上的纵坐标
     * @param client 客户端
     */
    public Display(int x, int y, Client client) {
        super();
        this.client = client;
        gameStartedChangeSupport = new DataChangeSupport<Boolean>(this, "gameStarted", false);
        historySizeChangeSupport = new DataChangeSupport<Integer>(this, "historySize", 0);
        
        messageLabel = new JLabel("");
        boardBoundXL = x;
        boardBoundYU = y;
        stoneCenterX = new int[Board.n + 2];
        stoneCenterY = new int[Board.n + 2];
        for (int i = 0; i <= Board.n + 1; ++i) {
            stoneCenterX[i] = boardBoundXL + sideLength * (i - 1);
            stoneCenterY[i] = boardBoundYU + sideLength * (i - 1);
        }
        boardBoundXR = stoneCenterX[Board.n];
        boardBoundYD = stoneCenterY[Board.n];
        boardCenterX = stoneCenterX[8];
        boardCenterY = stoneCenterY[8];
        
        timerPanel = new TimerPanel();
        countDownPanel = new CountDownPanel(this);
        timeManager = new TimeManager(countDownPanel, timerPanel);
        
        initLayout();
        
        reset();
    }
    
    
    private void initLayout() {
        messageLabel.setBounds(boardBoundXR + 7 * sideLength / 2, boardBoundYU, 7 * sideLength, sideLength);
        messageLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, sideLength / 2));
        timerPanel.setBounds(boardBoundXR + 10 * Display.sideLength, boardBoundYU + 14 * Display.sideLength, 4 * Display.sideLength, Display.sideLength);
        countDownPanel.setBounds(boardBoundXR + 12 * Display.sideLength, boardBoundYU - Display.sideLength / 2, 2 * Display.sideLength, 2 * Display.sideLength);
        
        add(messageLabel);
        add(timerPanel);
        add(countDownPanel);
    }
    
    
    /**
     * 新建游戏并刷新界面
     *
     * @param playerNumber 本方玩家号
     */
    public void newGame(int playerNumber) {
        reset();
        this.playerNumber = playerNumber;
        gameStartedChangeSupport.setValue(true);
        Graphics2D g2D = (Graphics2D) getGraphics();
        paintBoard(g2D);
        paintPlayer(g2D);
        timeManager.OnNewGame();
    }
    
    
    /**
     * 重置但不刷新界面
     */
    public void reset() {
        historySizeChangeSupport.setValue(0);
        gameStartedChangeSupport.setValue(false);
        presetStoneNumber = 5;
        playerStoneType = StoneType.SPACE;
        timeManager.OnReset();
    }
    
    
    /**
     * 结束游戏但不刷新界面
     *
     * @param winnerNumber     胜者的玩家号
     * @param indexOfRowStones 连珠的棋子编号
     * @param rowStones        连珠的棋子
     */
    public void gameOver(int winnerNumber, List<Integer> indexOfRowStones, List<Stone> rowStones) {
        timeManager.OnGameOver();
        reset();
        Graphics2D g2D = (Graphics2D) getGraphics();
        int rowStoneNumber = rowStones.size();
        for (int i = 0; i < rowStoneNumber; ++i) {
            try {
                paintStoneWithIndex(g2D, rowStones.get(i), indexOfRowStones.get(i), true);
            }
            catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }
        String message;
        if (winnerNumber == 1 || winnerNumber == 2) {
            message = (winnerNumber == playerNumber ? "本方" : "对方") + "玩家胜利";
        }
        else
            message = "平局";
        messageLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "游戏结束", JOptionPane.INFORMATION_MESSAGE);
    }
    
    
    /**
     * 认输
     */
    public void admitDefeat() {
        timeManager.OnAdmitDefeat();
        client.admitDefeat();
    }
    
    
    /**
     * 选择执子颜色
     */
    public void choosePlayerColor() {
        timeManager.OnDialog();
        if (getHistorySize() == 3) {
            String message = (playerNumber == 2 ? "本方" : "对方") + "玩家选择执子颜色";
            messageLabel.setText(message);
            if (playerNumber == 2) {
                String[] options = {"执黑", "执白", "继续"};
                int state = JOptionPane.showOptionDialog(this,
                                                         message,
                                                         "",
                                                         JOptionPane.YES_NO_CANCEL_OPTION,
                                                         JOptionPane.QUESTION_MESSAGE,
                                                         null,
                                                         options,
                                                         options[0]);
                /**
                 * 向 server 发送按键选择
                 * @messageType CHOOSE_PLAYER_COLOR
                 * @arg state 按键选择
                 */
                client.choosePlayerColor(state);
            }
        }
        else if (!isPlayerColorChosen() && getHistorySize() == 5) {
            String message = (playerNumber == 1 ? "本方" : "对方") + "玩家选择执子颜色";
            messageLabel.setText(message);
            if (playerNumber == 1) {
                String[] options = {"执黑", "执白"};
                int state = JOptionPane.showOptionDialog(this,
                                                         message,
                                                         "",
                                                         JOptionPane.YES_NO_OPTION,
                                                         JOptionPane.QUESTION_MESSAGE,
                                                         null,
                                                         options,
                                                         options[0]);
                /**
                 * 向 server 发送按键选择
                 * @messageType CHOOSE_PLAYER_COLOR
                 * @arg state 按键选择
                 */
                client.choosePlayerColor(state);
            }
        }
    }
    
    
    /**
     * 执行落子
     *
     * @param stone         落子的 stone
     * @param previousStone 落子的 stone 的前一个 stone，若没有则传入 null。
     * @param historySize   落子完成后棋盘上的棋子数
     */
    public void putStone(Stone stone, Stone previousStone, int historySize) {
        timeManager.OnPutStone();
        Graphics2D g2D = (Graphics2D) getGraphics();
        if (previousStone != null)
            paintStoneWithIndex(g2D, previousStone, historySize - 2, false);
        paintStoneWithIndex(g2D, stone, historySize - 1, true);
        setHistorySize(historySize);
        if (!isPlayerColorChosen())
            choosePlayerColor();
        else
            paintPlayer(g2D);
    }
    
    
    /**
     * 执行悔棋
     *
     * @param stone         被移走的 stone
     * @param previousStone 被移走的 stone 的前一个 stone，因为可以悔棋时棋盘上至少有 4 个棋子，必然是非 null。
     * @param historySize   悔棋完成后棋盘上的棋子数
     */
    public void retractStone(Stone stone, Stone previousStone, int historySize) {
        timeManager.OnRetractStone();
        Graphics2D g2D = (Graphics2D) getGraphics();
        eraseStone(g2D, stone.getI(), stone.getJ());
        paintStoneIndex(g2D, previousStone, historySize - 1, true);
        setHistorySize(historySize);
        paintPlayer(g2D);
    }
    
    
    /**
     * 向 server 请求落子
     *
     * @param x 鼠标在面板上点击的横坐标
     * @param y 鼠标在面板上点击的纵坐标
     */
    public void inquireToPutStoneFromMouse(int x, int y) {
        try {
            int i = getIFromX(x), j = getJFromY(y);
            int xGrid = getXFromI(i), yGrid = getYFromJ(j);
            if ((x - xGrid) * (x - xGrid) + (y - yGrid) * (y - yGrid) < stoneRadius * stoneRadius) {
                client.inquireToPutStone(i, j);
            }
        }
        catch (StoneOutOfBoardRangeException ignored) {
        }
    }
    
    
    public int getPlayerNumber() {
        return playerNumber;
    }
    
    
    /**
     * 获取棋盘上的棋子数
     */
    public int getHistorySize() {
        return historySizeChangeSupport.getValue();
    }
    
    
    /**
     * 设置棋盘上的棋子数
     *
     * @param historySize 棋盘上的棋子数
     */
    public void setHistorySize(int historySize) {
        historySizeChangeSupport.setValue(historySize);
    }
    
    
    /**
     * 设置玩家执子颜色
     *
     * @param playerStoneType   玩家执子颜色
     * @param presetStoneNumber 预先放置的棋子数
     */
    public void setPlayerStoneType(StoneType playerStoneType, int presetStoneNumber) {
        this.playerStoneType = playerStoneType;
        this.presetStoneNumber = presetStoneNumber;
        System.out.printf("%d %d\n", client.socketId, presetStoneNumber);
        paintPlayer((Graphics2D) getGraphics());
        timeManager.OnDialogClose();
        if (isPlayerColorChosen())
            JOptionPane.showMessageDialog(this, "本方执" + (playerStoneType == StoneType.BLACK ? "黑" : "白") + " 对方执" + (playerStoneType == StoneType.BLACK ? "白" : "黑"), "", JOptionPane.INFORMATION_MESSAGE);
    }
    
    
    /**
     * 游戏是否开始
     */
    public boolean isGameStarted() {
        return gameStartedChangeSupport.getValue();
    }
    
    
    /**
     * 玩家棋子颜色是否已选择
     */
    public boolean isPlayerColorChosen() {
        return (playerStoneType != StoneType.SPACE);
    }
    
    
    /**
     * 下一个执行落子的玩家的玩家号（就是当前回合的玩家号）
     */
    public int getNextPlayerNumber() {
        if (isPlayerColorChosen())
            return (playerStoneType == getNextStoneType() ? playerNumber : 3 - playerNumber);
        else
            return (getHistorySize() < 3 ? 1 : 2);
    }
    
    
    /**
     * 下一个棋子颜色（就是当前回合的落子颜色）
     */
    public StoneType getNextStoneType() {
        return (getHistorySize() % 2 == 0 ? StoneType.BLACK : StoneType.WHITE);
    }
    
    
    /**
     * 是否可以悔棋
     * 棋盘上的棋子数大于预先放置的棋子数，且当前为对方回合时，才可以悔棋。
     */
    public boolean canRetractStone() {
        if (isGameStarted() && getNextPlayerNumber() != playerNumber)
            return (getHistorySize() > presetStoneNumber);
        else
            return false;
    }
    
    
    /**
     * 向 gameStartedChangeSupport 添加 PropertyChangeListener
     * 当游戏开始或结束时触发事件
     *
     * @param listener 待添加的 PropertyChangeListener
     */
    public void addGameStartedChangeListener(PropertyChangeListener listener) {
        gameStartedChangeSupport.addPropertyChangeListener(listener);
    }
    
    
    /**
     * 向 historySizeChangeSupport 添加 PropertyChangeListener
     * 当棋盘上棋子数目发生变化时触发事件
     *
     * @param listener 待添加的 PropertyChangeListener
     */
    public void addHistorySizeChangeListener(PropertyChangeListener listener) {
        historySizeChangeSupport.addPropertyChangeListener(listener);
    }
    
    
    /**
     * 绘制面板
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        paintBoard((Graphics2D) g);
    }
    
    
    /**
     * 绘制棋盘
     */
    private void paintBoard(Graphics2D g2D) {
        g2D.setColor(backgroundColor);
        g2D.fillRect(stoneCenterX[0], stoneCenterY[0], sideLength * (Board.n + 1), sideLength * (Board.n + 1));
        g2D.setColor(Color.BLACK);
        g2D.setStroke(new BasicStroke(5.0f));
        g2D.drawLine(stoneCenterX[0], stoneCenterY[0], stoneCenterX[Board.n + 1], stoneCenterY[0]);
        g2D.drawLine(stoneCenterX[0], stoneCenterY[Board.n + 1], stoneCenterX[Board.n + 1], stoneCenterY[Board.n + 1]);
        g2D.drawLine(stoneCenterX[0], stoneCenterY[0], stoneCenterX[0], stoneCenterY[Board.n + 1]);
        g2D.drawLine(stoneCenterX[Board.n + 1], stoneCenterY[0], stoneCenterX[Board.n + 1], stoneCenterY[Board.n + 1]);
        g2D.setStroke(new BasicStroke(2.0f));
        for (int i = 1; i <= Board.n; ++i) {
            g2D.drawLine(stoneCenterX[i], boardBoundYU, stoneCenterX[i], boardBoundYD);
            g2D.drawLine(boardBoundXL, stoneCenterY[i], boardBoundXR, stoneCenterY[i]);
        }
        fillCircle(g2D, 8, 8, starRadius);
        fillCircle(g2D, 4, 4, starRadius);
        fillCircle(g2D, 4, 12, starRadius);
        fillCircle(g2D, 12, 4, starRadius);
        fillCircle(g2D, 12, 12, starRadius);
    }
    
    
    /**
     * 擦除棋盘上的棋子
     *
     * @param i 棋子对应棋盘格点的横坐标
     * @param j 棋子对应棋盘格点的纵坐标
     */
    private void eraseStone(Graphics2D g2D, int i, int j) {
        try {
            int centerX = getXFromI(i);
            int centerY = getYFromJ(j);
            g2D.setColor(backgroundColor);
            g2D.fillRect(centerX - sideLength / 2, centerY - sideLength / 2, sideLength, sideLength);
            g2D.setColor(Color.BLACK);
            g2D.setStroke(new BasicStroke(2.0f));
            if (i > 1)
                g2D.drawLine(centerX - sideLength / 2, centerY, centerX, centerY);
            if (i < Board.n)
                g2D.drawLine(centerX, centerY, centerX + sideLength / 2, centerY);
            if (j > 1)
                g2D.drawLine(centerX, centerY - sideLength / 2, centerX, centerY);
            if (j < Board.n)
                g2D.drawLine(centerX, centerY, centerX, centerY + sideLength / 2);
            
            if (isStar(i, j))
                fillCircle(g2D, i, j, starRadius);
        }
        catch (StoneOutOfBoardRangeException ignored) {
        }
    }
    
    
    /**
     * 在棋盘上绘制棋子
     *
     * @param stone 被绘制的棋子
     */
    private void paintStone(Graphics2D g2D, Stone stone) {
        if (stone.getType() != StoneType.SPACE) {
            g2D.setStroke(new BasicStroke(1.0f));
            g2D.setColor(gray);
            drawCircle(g2D, stone.getI(), stone.getJ(), stoneRadius);
            g2D.setColor(getColorFromType(stone.getType()));
            fillCircle(g2D, stone.getI(), stone.getJ(), stoneRadius);
        }
    }
    
    
    /**
     * 在棋盘上绘制棋子编号
     *
     * @param stone 被绘制的棋子
     * @param index 棋子编号（实际显示的是 (index + 1)）
     * @param color 棋子编号颜色
     */
    private void paintStoneIndex(Graphics2D g2D, Stone stone, int index, Color color) {
        g2D.setColor(color);
        try {
            drawCenteredString(g2D, Integer.toString(index + 1), getXFromI(stone.getI()), getYFromJ(stone.getJ()));
        }
        catch (StoneOutOfBoardRangeException ignored) {
        }
    }
    
    
    /**
     * 在棋盘上绘制棋子编号
     *
     * @param stone     被绘制的棋子
     * @param index     棋子编号（实际显示的是 (index + 1)）
     * @param highlight 编号是否高亮，若高亮则显示红色，否则显示与棋子颜色相反的颜色。
     */
    private void paintStoneIndex(Graphics2D g2D, Stone stone, int index, boolean highlight) {
        g2D.setFont(indexFont);
        if (highlight)
            paintStoneIndex(g2D, stone, index, Color.RED);
        else
            paintStoneIndex(g2D, stone, index, getOppositeColorFromType(stone.getType()));
    }
    
    
    /**
     * 在棋盘上绘制棋子及其编号
     *
     * @param stone     被绘制的棋子
     * @param index     棋子编号（实际显示的是 (index + 1)）
     * @param highlight 编号是否高亮，若高亮则显示红色，否则显示与棋子颜色相反的颜色。
     */
    private void paintStoneWithIndex(Graphics2D g2D, Stone stone, int index, boolean highlight) {
        if (stone.getType() != StoneType.SPACE) {
            paintStone(g2D, stone);
            paintStoneIndex(g2D, stone, index, highlight);
        }
    }
    
    
    /**
     * 在通知区域（在棋盘外）绘制下一个棋子颜色（就是当前回合的落子颜色）
     */
    private void paintNextStoneColor(Graphics2D g2D) {
        g2D.setStroke(new BasicStroke(1.0f));
        g2D.setColor(gray);
        g2D.drawOval(boardBoundXR + 5 * sideLength / 2 - stoneRadius, boardBoundYU + sideLength / 2 - stoneRadius, 2 * stoneRadius, 2 * stoneRadius);
        g2D.setColor(getColorFromType(getNextStoneType()));
        g2D.fillOval(boardBoundXR + 5 * sideLength / 2 - stoneRadius, boardBoundYU + sideLength / 2 - stoneRadius, 2 * stoneRadius, 2 * stoneRadius);
    }
    
    
    /**
     * 在通知区域（在棋盘外）绘制下一个棋子颜色（就是当前回合的落子颜色），并显示通知。
     *
     * @param message 通知内容
     */
    private void paintMessage(Graphics2D g2D, String message) {
        paintNextStoneColor(g2D);
        messageLabel.setText(message);
    }
    
    
    /**
     * 在通知区域（在棋盘外）绘制下一个棋子颜色（就是当前回合的落子颜色），并显示下一个执行落子的玩家的玩家号（就是当前回合的玩家号）。
     */
    private void paintPlayer(Graphics2D g2D) {
        String message = (playerNumber == getNextPlayerNumber() ? "本方玩家" : "对方玩家") + (getNextStoneType() == StoneType.BLACK ? "执黑" : "执白");
        paintMessage(g2D, message);
    }
    
    
    /**
     * 在棋盘以格点为圆心画圆（填充）
     *
     * @param i 棋盘格点横坐标
     * @param j 棋盘格点纵坐标
     * @param r 圆半径
     */
    private void fillCircle(Graphics2D g2D, int i, int j, int r) {
        g2D.fillOval(stoneCenterX[i] - r, stoneCenterY[j] - r, 2 * r, 2 * r);
    }
    
    
    /**
     * 在棋盘以格点为圆心画圆（非填充）
     *
     * @param i 棋盘格点横坐标
     * @param j 棋盘格点纵坐标
     * @param r 圆半径
     */
    private void drawCircle(Graphics2D g2D, int i, int j, int r) {
        g2D.drawOval(stoneCenterX[i] - r, stoneCenterY[j] - r, 2 * r, 2 * r);
    }
    
    
    /**
     * 在面板上显示字符串
     *
     * @param text 被显示的字符串
     * @param x    显示的字符串的中心横坐标
     * @param y    显示的字符串的中心纵坐标
     */
    private void drawCenteredString(Graphics2D g2D, String text, int x, int y) {
        FontMetrics metrics = g2D.getFontMetrics();
        x -= metrics.stringWidth(text) / 2;
        y -= metrics.getHeight() / 2 - metrics.getAscent();
        g2D.drawString(text, x, y);
    }
    
    
    /**
     * 判断该格点是否是星（棋盘中心左上左下右上右下的黑点）
     *
     * @param i 棋盘格点横坐标
     * @param j 棋盘格点横坐标
     */
    public static boolean isStar(int i, int j) {
        return ((i == 8 && j == 8) ||
                (i == 4 && j == 4) ||
                (i == 4 && j == 12) ||
                (i == 12 && j == 4) ||
                (i == 12 && j == 12));
    }
    
    
    /**
     * 获取棋盘左边界横坐标（不包括棋盘周边的边框）
     */
    public int getBoardBoundXL() {
        return boardBoundXL;
    }
    
    
    /**
     * 获取棋盘右边界横坐标（不包括棋盘周边的边框）
     */
    public int getBoardBoundXR() {
        return boardBoundXR;
    }
    
    
    /**
     * 获取棋盘上边界纵坐标（不包括棋盘周边的边框）
     */
    public int getBoardBoundYU() {
        return boardBoundYU;
    }
    
    
    /**
     * 获取棋盘下边界纵坐标（不包括棋盘周边的边框）
     */
    public int getBoardBoundYD() {
        return boardBoundYD;
    }
    
    
    /**
     * 获取棋盘中心横坐标
     */
    public int getBoardCenterX() {
        return boardCenterX;
    }
    
    
    /**
     * 获取棋盘中心纵坐标
     */
    public int getBoardCenterY() {
        return boardCenterY;
    }
    
    
    /**
     * 将棋盘格点横坐标转换为面板横坐标
     *
     * @param i 棋盘格点横坐标
     */
    public int getXFromI(int i) throws StoneOutOfBoardRangeException {
        try {
            return stoneCenterX[i];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new StoneOutOfBoardRangeException();
        }
    }
    
    
    /**
     * 将棋盘格点纵坐标转换为面板纵坐标
     *
     * @param j 棋盘格点纵坐标
     */
    public int getYFromJ(int j) throws StoneOutOfBoardRangeException {
        try {
            return stoneCenterY[j];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new StoneOutOfBoardRangeException();
        }
    }
    
    
    /**
     * 将面板横坐标转换为距离其最近的棋盘格点横坐标
     *
     * @param x 面板横坐标
     */
    public int getIFromX(int x) {
        return Math.round(((float) (x - boardBoundXL)) / sideLength) + 1;
    }
    
    
    /**
     * 将面板纵坐标转换为距离其最近的棋盘格点纵坐标
     *
     * @param y 面板纵坐标
     */
    public int getJFromY(int y) {
        return Math.round(((float) (y - boardBoundYU)) / sideLength) + 1;
    }
    
    
    /**
     * 棋子颜色转换为其在界面上显示的颜色
     *
     * @param type 棋子颜色
     */
    public static Color getColorFromType(StoneType type) {
        return (type == StoneType.BLACK ? black : white);
    }
    
    
    /**
     * 棋子颜色转换为其在界面上显示的相反颜色
     *
     * @param type 棋子颜色
     */
    public static Color getOppositeColorFromType(StoneType type) {
        return (type != StoneType.BLACK ? black : white);
    }
}


class DataChangeSupport<T> extends PropertyChangeSupport {
    private T value;
    private String propertyName;
    
    
    public DataChangeSupport(Object source, String propertyName, T initialValue) {
        super(source);
        this.propertyName = propertyName;
        value = initialValue;
    }
    
    
    public T getValue() {
        return value;
    }
    
    
    public void setValue(T newValue) {
        T oldValue = value;
        value = newValue;
        firePropertyChange(propertyName, oldValue, newValue);
    }
}