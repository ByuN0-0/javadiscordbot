package me.byun.bot;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.*;

public class SlashBot extends ListenerAdapter {
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()){
            case "ping":
                ping(event);
                break;
            case "team":
                team(event);
                break;
            case "lotto":
                lotto(event);
                break;
            case "점메추":
            case "저메추":
                recommendFood(event);
                break;
            case "quiz":
                quiz(event);
                break;
            case "clear":
                clear(event);
                break;
            default:
                event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
        }
    }

    public void ping(SlashCommandInteractionEvent event){
        long time = System.currentTimeMillis();
        event.reply("Pong!").setEphemeral(false) // reply or acknowledge (ephemeral true == one on one message)
                .flatMap(v ->
                        event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)
                ).queue();
    }
    public void team(SlashCommandInteractionEvent event){
        String str = event.getOption("players").getAsString();
        String msg = "입력: " + str;
        String[] players = str.split(" ");
        if (players.length%2!=0){
            event.reply(msg+"\n짝수로 고쳐주십사와요~\n").queue();
            return;
        }
        List<String> playerList = new ArrayList<>();
        Collections.addAll(playerList, players);

        Collections.shuffle(playerList);

        List<String> team1 = new ArrayList<>();
        List<String> team2 = new ArrayList<>();

        for (int i = 0; i < playerList.size(); i++) {
            if (i % 2 == 0) {
                team1.add(playerList.get(i));

            } else {
                team2.add(playerList.get(i));
            }
        }
        StringBuilder result = new StringBuilder();
        result.append(msg+"\n");
        result.append("team 1: ").append(String.join(", ", team1)).append("\n");
        result.append("team 2: ").append(String.join(", ", team2));

        event.reply(result.toString()).setEphemeral(false).queue();
    }
    public void lotto(SlashCommandInteractionEvent event) {
        int[] num = new int[6];
        StringBuilder str = new StringBuilder();
        str.append("추천해줄 6개의 숫자는 ");

        Set<Integer> numberSet = new HashSet<>();
        Random random = new Random();

        while (numberSet.size() < 6) {
            int randomNumber = random.nextInt(45) + 1;
            numberSet.add(randomNumber);
        }

        int index = 0;
        for (int number : numberSet) {
            num[index] = number;
            index++;
        }

        insertionSort(num);

        for (int number : num) {
            str.append(number).append(", ");
        }

        str.delete(str.length() - 2, str.length());
        str.append("야");

        event.reply(str.toString()).setEphemeral(false).queue();
    }
    public void recommendFood(SlashCommandInteractionEvent event){
        String[] foods = {
                "피자", "햄버거", "스테이크", "돈까스", "파스타", "쌀국수", "샐러드", "마라탕", "스시", "타코", "카레", "라면", "떡볶이", "비빔밥", "김치찌개", "부대찌개", "삼계탕", "해장국", "볶음밥", "우동", "곱창", "닭갈비", "갈비찜", "냉면", "삼겹살", "굶어", "양꼬치", 
                "곱도리탕"
        };
        int randomIndex = (int) (Math.random() * foods.length);
        String recommendedFood = foods[randomIndex];
        String response = "추천 음식: " + recommendedFood;
        event.reply(response).setEphemeral(false).queue();
    }
    //Todo: quiz
    public void quiz(SlashCommandInteractionEvent event){
        String serverId = event.getGuild().getId();
        Quiz serverQuiz = Quiz.getInstance(serverId);
        serverQuiz.setChannel(event.getChannel());
        if(serverQuiz.checkQuiz()){
            event.reply("이미 진행중인 퀴즈가 있습니다.").setEphemeral(true).queue();
            return;
        }
        serverQuiz.startQuiz(serverId, event.getChannel());
        event.reply("퀴즈를 시작합니다!").setEphemeral(false).queue();
    }
    public void clear(SlashCommandInteractionEvent event) {
        int amount = event.getOption("amount").getAsInt();
        Member selfMember = event.getGuild().getSelfMember();
        if (selfMember.hasPermission((GuildChannel) event.getChannel(), Permission.MESSAGE_MANAGE)) {
            event.getChannel().getHistory().retrievePast(amount).queue(messages -> {
                for (Message message : messages) {
                    message.delete().queue();
                }
                event.reply("채팅을 " + amount + "개 삭제했습니다.").setEphemeral(false).queue();
            });
        }else {
            event.reply("채팅을 삭제할 권한이 없습니다.").setEphemeral(true).queue();
        }
    }
    public static void insertionSort(int[] arr) {
        int n = arr.length;

        for (int i = 1; i < n; i++) {
            int key = arr[i];
            int j = i - 1;

            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }
}
