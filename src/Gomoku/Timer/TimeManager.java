package Gomoku.Timer;

public class TimeManager {
    private CountDownPanel countDownPanel;
    private TimerPanel timerPanel;
    
    
    public TimeManager(CountDownPanel countDownPanel, TimerPanel timerPanel) {
        this.countDownPanel = countDownPanel;
        this.timerPanel = timerPanel;
    }
    
    
    public void OnNewGame() {
        countDownPanel.start();
        timerPanel.stop();
        timerPanel.start();
    }
    
    
    public void OnGameOver() {
        countDownPanel.stop();
        timerPanel.pause();
    }
    
    
    public void OnReset() {
        countDownPanel.stop();
        timerPanel.stop();
    }
    
    
    public void OnPutStone() {
        countDownPanel.stop();
        countDownPanel.start();
    }
    
    
    public void OnRetractStone() {
        countDownPanel.stop();
        countDownPanel.start();
    }
    
    
    public void OnAdmitDefeat() {
        countDownPanel.pause();
        timerPanel.pause();
    }
    
    
    public void OnDialog() {
        countDownPanel.pause();
        timerPanel.pause();
    }
    
    
    public void OnDialogClose() {
        countDownPanel.start();
        timerPanel.start();
    }
}