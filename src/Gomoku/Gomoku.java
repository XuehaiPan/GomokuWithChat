/**
 * @author 潘学海
 */

package Gomoku;

import Gomoku.Chat.ChatPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public class Gomoku extends JFrame {
    private final Client client;
    private final Display display;
    private final JButton retractButton;
    private final JButton newGameButton;
    private final JButton showRuleButton;
    private final ChatPanel chatPanel;
    
    public static final String swap2Rule = "一. 假先方在棋盘任意下三手（二黑一白），假后方有三种选择：\n" +
                                           "     1. 选黑。\n" +
                                           "     2. 选白。\n" +
                                           "     3. 下四、五两手（一黑一白），再假先方选择黑或白。\n" +
                                           "二. 黑白双方轮流落子。\n" +
                                           "三. 首选在横、竖、斜方向上成五（连续五个己方棋子）者为胜。\n" +
                                           "四. 超过五子以上不算赢也不算输。";
    
    
    public Gomoku(Client client) {
        super("五子棋");
        this.client = client;
        display = new Display(60, 60, client);
        setContentPane(display);
        
        newGameButton = new JButton("新游戏");
        retractButton = new JButton("悔棋");
        showRuleButton = new JButton("游戏规则");
        chatPanel = new ChatPanel(client);
        
        initLayout();
        
        initActionListeners();
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    
    private void initActionListeners() {
        retractButton.setEnabled(false);
        newGameButton.addActionListener(e -> {
            if (!display.isGameStarted())
                client.inquireToNewGame();
            else
                client.admitDefeat();
        });
        retractButton.addActionListener(e -> client.inquireToRetractStone());
        showRuleButton.addActionListener(e -> JOptionPane.showMessageDialog(this, swap2Rule, "Swap2 规则", JOptionPane.INFORMATION_MESSAGE));
        display.addGameStartedChangeListener(evt -> {
            if ((Boolean) evt.getNewValue())
                newGameButton.setText("认输");
            else
                newGameButton.setText("新游戏");
            retractButton.setEnabled(display.canRetractStone());
        });
        display.addHistorySizeChangeListener(evt -> retractButton.setEnabled(display.canRetractStone()));
        display.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }
            
            
            @Override
            public void mousePressed(MouseEvent e) {
                display.inquireToPutStoneFromMouse(e.getX(), e.getY());
            }
            
            
            @Override
            public void mouseReleased(MouseEvent e) {
            }
            
            
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            
            
            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }
    
    
    private void initLayout() {
        Font font = new Font(Font.DIALOG, Font.PLAIN, 3 * Display.sideLength / 4);
        newGameButton.setFont(font);
        retractButton.setFont(font);
        showRuleButton.setFont(font);
        
        display.setLayout(null);
        setSize(display.getBoardBoundXR() + 15 * Display.sideLength, 700);
        newGameButton.setBounds(display.getBoardBoundXR() + 2 * Display.sideLength, display.getBoardBoundYU() + 2 * Display.sideLength, 4 * Display.sideLength, 2 * Display.sideLength);
        retractButton.setBounds(display.getBoardBoundXR() + 6 * Display.sideLength, display.getBoardBoundYU() + 2 * Display.sideLength, 4 * Display.sideLength, 2 * Display.sideLength);
        showRuleButton.setBounds(display.getBoardBoundXR() + 10 * Display.sideLength, display.getBoardBoundYU() + 2 * Display.sideLength, 4 * Display.sideLength, 2 * Display.sideLength);
        chatPanel.setBounds(display.getBoardBoundXR() + 2 * Display.sideLength, display.getBoardBoundYU() + 4 * Display.sideLength + Display.sideLength / 2, 12 * Display.sideLength, 9 * Display.sideLength);
        display.add(newGameButton);
        display.add(retractButton);
        display.add(showRuleButton);
        display.add(chatPanel);
        
        setResizable(false);
    }
    
    
    /**
     * 新建游戏并刷新界面
     *
     * @param playerNumber 本方玩家号
     */
    public void newGame(int playerNumber) {
        display.newGame(playerNumber);
    }
    
    
    /**
     * 结束游戏但不刷新界面
     *
     * @param winnerNumber     胜者的玩家号
     * @param indexOfRowStones 连珠的棋子编号
     * @param rowStones        连珠的棋子
     */
    public void gameOver(int winnerNumber, List<Integer> indexOfRowStones, List<Stone> rowStones) {
        display.gameOver(winnerNumber, indexOfRowStones, rowStones);
    }
    
    
    /**
     * 执行落子
     *
     * @param stone         落子的 stone
     * @param previousStone 落子的 stone 的前一个 stone，若没有则传入 null。
     * @param historySize   落子完成后棋盘上的棋子数
     */
    public void putStone(Stone stone, Stone previousStone, int historySize) {
        display.putStone(stone, previousStone, historySize);
    }
    
    
    /**
     * 执行悔棋
     *
     * @param stone         被移走的 stone
     * @param previousStone 被移走的 stone 的前一个 stone，因为可以悔棋时棋盘上至少有 4 个棋子，必然是非 null。
     * @param historySize   悔棋完成后棋盘上的棋子数
     */
    public void retractStone(Stone stone, Stone previousStone, int historySize) {
        display.retractStone(stone, previousStone, historySize);
    }
    
    
    /**
     * 设置玩家执子颜色
     *
     * @param playerStoneType   玩家执子颜色
     * @param presetStoneNumber 预先放置的棋子数
     */
    public void setPlayerStoneType(StoneType playerStoneType, int presetStoneNumber) {
        display.setPlayerStoneType(playerStoneType, presetStoneNumber);
    }
    
    
    /**
     * 在聊天面板上添加对方发来的消息
     *
     * @param chatText 对方发来的消息
     */
    public void addMessageFromOtherSide(String chatText) {
        chatPanel.addMessageFromOtherSide(chatText);
    }
}