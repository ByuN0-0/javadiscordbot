package me.byun.bot;

import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Quiz {
    private static Map<String, Quiz> quizMap = new HashMap<>();
    private String serverId;
    private String userAnswer;
    private String previousAnswer;
    private String lastUser;
    private boolean isQuizStarted;
    private MessageChannelUnion channel;

    private Quiz(String serverId){
        this.serverId = serverId;
        this.isQuizStarted = false;
        this.userAnswer = null;
        this.previousAnswer = null;
        this.lastUser = null;
    }
    public static synchronized Quiz getInstance(String serverId){
        if (!quizMap.containsKey(serverId)) {
            Quiz quiz = new Quiz(serverId);
            quizMap.put(serverId, quiz);
        }
        return quizMap.get(serverId);
    }
    public void startQuiz(String serverId, MessageChannelUnion channel){
        this.channel = channel;
        if (!quizMap.containsKey(serverId)) {
            Quiz serverQuiz = new Quiz(serverId);
            quizMap.put(serverId, serverQuiz);
        }
        this.isQuizStarted = true;
        quizCycle();
    }
    public void endQuiz(String serverId){
        quizMap.remove(serverId);
        this.isQuizStarted = false;
    }
    public boolean checkQuiz(){
        return isQuizStarted;
    }
    public void setUserAnswer(String userAnswer, MessageChannelUnion channel, String lastUser){
        this.userAnswer = userAnswer;
        this.channel = channel;
        this.lastUser = lastUser;
    }
    public void quizCycle() {
        String question = "한국의 수도는?";
        String answer = "서울";
        AtomicInteger tIndex = new AtomicInteger(100);

        Thread quizThread = new Thread(() -> {
            while (isQuizStarted) {
                int currentIndex = tIndex.getAndAdd(-1);
                if (currentIndex <= 0) {
                    tIndex.set(100);
                }
                channel.sendMessage("남은 시간: " + currentIndex ).queue();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if (currentIndex == 100) {
                    channel.sendMessage(question).queue();
                    userAnswer = null;
                    previousAnswer = null;
                    // 문제 출제 & answer 변경
                }
                if (!Objects.equals(userAnswer, previousAnswer)) {
                    if (userAnswer != null && userAnswer.equalsIgnoreCase(answer)) {
                        channel.sendMessage(lastUser + "님 정답").queue();
                        continue;
                    }
                    previousAnswer = userAnswer;
                }
            }
        });

        quizThread.start();
    }
    /*public void quizCycle() {
        String question = "한국의 수도는?";
        String answer = "서울";
        AtomicInteger tIndex = new AtomicInteger(100);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            int currentIndex = tIndex.getAndAdd(-5);
            if (currentIndex % 10 == 0) {
                channel.sendMessage("남은 시간: " + currentIndex / 10 + "초").queue();
            }
            if (currentIndex == 100) {
                channel.sendMessage(question).queue();
                userAnswer = null;
                previousAnswer = null;
                // 문제 출제 & answer 변경
            }
            if (!Objects.equals(userAnswer, previousAnswer)) {
                if (userAnswer != null && userAnswer.equalsIgnoreCase(answer)) {
                    channel.sendMessage(lastUser + "님 정답").queue();
                }
                previousAnswer = userAnswer;
            }
            if (!isQuizStarted) {
                executor.shutdown();
            }
            if (currentIndex <= 0) {
                tIndex.set(100);
            }
        }, 0, 500, TimeUnit.MILLISECONDS);  // 0.5초마다 userAnswer를 확인하도록 설정
    }*/
    /*public void quizCycle(){
        String question = "한국의 수도는?";
        String answer = "서울";
        AtomicInteger tIndex = new AtomicInteger(100);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int currentIndex = tIndex.getAndAdd(-5);
                if(currentIndex%10==0){
                    channel.sendMessage("남은 시간: "+currentIndex/10+"초").queue();
                }
                if(currentIndex==100){
                    channel.sendMessage(question).queue();
                    userAnswer = null;
                    previousAnswer = null;
                    //answer = newAnswer;
                    //문제 출제 & answer 변경
                }
                if (!Objects.equals(userAnswer, previousAnswer)) {
                    if (userAnswer != null && userAnswer.equalsIgnoreCase(answer)) {
                        channel.sendMessage(lastUser+"님 정답").queue();
                    }
                    previousAnswer = userAnswer;
                }
                if(!isQuizStarted){ cancel(); }
                if(currentIndex<=0){
                    tIndex.set(100);
                }
            }
        }, 0, 500);  // 0.5초마다 userAnswer를 확인하도록 설정
    }*/
}