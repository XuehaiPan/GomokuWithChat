package Gomoku.Timer;

import Gomoku.Display;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

public class CountDownPanel extends JPanel {
    private Display display;
    private int remainingSec;
    public final static int maximumTime = 15;
    public final static int minimumTime = 5;
    private JLabel secLabel;
    private StartManager startManager;
    
    
    public CountDownPanel(Display display) {
        this.display = display;
        secLabel = new JLabel("");
        remainingSec = maximumTime;
        initLayout();
    }
    
    
    private void initLayout() {
        int width = getWidth(), height = getHeight();
        removeAll();
        setLayout(null);
        Font numberFont = new Font("Consolas", Font.BOLD, 3 * height / 4);
        secLabel.setBounds(0, 0, width, height);
        secLabel.setFont(numberFont);
        secLabel.setHorizontalAlignment(JLabel.CENTER);
        
        add(secLabel);
    }
    
    
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        initLayout();
    }
    
    
    public void start() {
        startManager = new StartManager(this);
        startManager.start();
    }
    
    
    public void pause() {
        new PauseManager(startManager).start();
    }
    
    
    public void stop() {
        new PauseManager(startManager).start();
        setTime(maximumTime);
        secLabel.setText("");
    }
    
    
    public void showTime() {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumIntegerDigits(2);
        secLabel.setForeground(remainingSec > minimumTime ? Color.BLACK : Color.RED);
        secLabel.setText(numberFormat.format(remainingSec));
    }
    
    
    public void setTime(int sec) {
        this.remainingSec = sec;
    }
    
    
    public int getSec() {
        return remainingSec;
    }
    
    
    private static class StartManager extends Thread {
        private CountDownPanel countDownPanel;
        
        
        public StartManager(CountDownPanel countDownPanel) {
            this.countDownPanel = countDownPanel;
        }
        
        
        @Override
        public void run() {
            int remainSec;
            remainSec = countDownPanel.getSec();
            countDownPanel.showTime();
            
            while (true) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    break;
                }
                --remainSec;
                if (remainSec < 0 && countDownPanel.display.getPlayerNumber() == countDownPanel.display.getNextPlayerNumber()) {
                    new Thread(() -> countDownPanel.display.admitDefeat()).start();    // time over!
                    break;
                }
                countDownPanel.setTime(remainSec);
                countDownPanel.showTime();
            }
        }
    }
    
    
    private static class PauseManager extends Thread {
        StartManager startManager;
        
        
        public PauseManager(StartManager startManager) {
            this.startManager = startManager;
        }
        
        
        @Override
        public void run() {
            try {
                startManager.interrupt();
            }
            catch (NullPointerException ignored) {
            }
        }
    }
}