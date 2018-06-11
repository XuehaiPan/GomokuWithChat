package Gomoku.Timer;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

public class TimerPanel extends JPanel {
    private int hour, min, sec;
    private JLabel hourLabel, minLabel, secLabel;
    private StartManager start;
    
    
    public TimerPanel() {
        super();
        hourLabel = new JLabel("00");
        secLabel = new JLabel("00");
        minLabel = new JLabel("00");
        initLayout();
    }
    
    
    private void initLayout() {
        int width = getWidth(), height = getHeight();
        removeAll();
        setLayout(null);
        Font numberFont = new Font("Consolas", Font.PLAIN, height / 2);
        
        // hour
        hourLabel.setBounds(0, 0, width / 4, height);
        hourLabel.setFont(numberFont);
        hourLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // colon
        JLabel colonLabel1 = new JLabel(":");
        colonLabel1.setBounds(width / 4, 0, width / 8, height);
        colonLabel1.setFont(numberFont);
        colonLabel1.setHorizontalAlignment(JLabel.CENTER);
        
        // minute
        minLabel.setBounds(3 * width / 8, 0, width / 4, height);
        minLabel.setFont(numberFont);
        minLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // colon
        JLabel colonLabel2 = new JLabel(":");
        colonLabel2.setBounds(5 * width / 8, 0, width / 8, height);
        colonLabel2.setFont(numberFont);
        colonLabel2.setHorizontalAlignment(JLabel.CENTER);
        
        // second
        secLabel.setBounds(3 * width / 4, 0, width / 4, height);
        secLabel.setFont(numberFont);
        secLabel.setHorizontalAlignment(JLabel.CENTER);
        
        add(hourLabel);
        add(colonLabel1);
        add(minLabel);
        add(colonLabel2);
        add(secLabel);
    }
    
    
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        initLayout();
    }
    
    
    public void start() {  // startManager or continue
        start = new StartManager(this);
        start.start();
    }
    
    
    public void pause() {
        new PauseManager(start).start();
    }
    
    
    public void stop() {
        // refresh data
        setTime(0, 0, 0);
        showTime();
        new PauseManager(start).start();
    }
    
    
    public void showTime() {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumIntegerDigits(2);
        this.hourLabel.setText(numberFormat.format(hour));
        this.minLabel.setText(numberFormat.format(min));
        this.secLabel.setText(numberFormat.format(sec));
    }
    
    
    public void setTime(int hour, int min, int sec) {
        this.hour = hour;
        this.min = min;
        this.sec = sec;
    }
    
    
    public int getHour() {
        return hour;
    }
    
    
    public int getMin() {
        return min;
    }
    
    
    public int getSec() {
        return sec;
    }
    
    
    private static class StartManager extends Thread {
        TimerPanel timerPanel;
        
        
        public StartManager(TimerPanel timerPanel) {
            this.timerPanel = timerPanel;
        }
        
        
        @Override
        public void run() {
            int sec, min, hour;
            sec = timerPanel.getSec();
            min = timerPanel.getMin();
            hour = timerPanel.getHour();
            while (true) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    break;  // break loop
                }
                ++sec;
                if (sec >= 60) {
                    ++min;
                    sec = 0;
                    if (min >= 60) {
                        ++hour;
                        min = 0;
                        if (min >= 24) {
                            hour = 0;
                        }
                    }
                }
                timerPanel.setTime(hour, min, sec);
                timerPanel.showTime();
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