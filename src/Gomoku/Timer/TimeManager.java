package Gomoku.Timer;

public class TimeManager {
    private CountDownPanel countDownPanel;
    private TimerPanel timerPanel;
    
    
    public TimeManager(CountDownPanel countDownPanel, TimerPanel timerPanel) {
        this.countDownPanel = countDownPanel;
        this.timerPanel = timerPanel;
    }
    
    
    public void onNewGame() {
        countDownPanel.start();
        timerPanel.stop();
        timerPanel.start();
    }
    
    
    public void onGameOver() {
        countDownPanel.stop();
        timerPanel.pause();
    }
    
    
    public void onReset() {
        countDownPanel.stop();
        timerPanel.stop();
    }
    
    
    public void onPutStone() {
        countDownPanel.stop();
        countDownPanel.start();
    }
    
    
    public void onRetractStone() {
        countDownPanel.stop();
        countDownPanel.start();
    }
    
    
    public void onAdmitDefeat() {
        countDownPanel.pause();
        timerPanel.pause();
    }
    
    
    public void onDialog() {
        countDownPanel.pause();
        timerPanel.pause();
    }
    
    
    public void onDialogClose() {
        countDownPanel.start();
        timerPanel.start();
    }
}