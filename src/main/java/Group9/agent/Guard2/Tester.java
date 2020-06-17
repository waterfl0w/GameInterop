package Group9.agent.Guard2;

import Group9.Game;
import Group9.agent.RandomAgent;
import Group9.agent.RandomIntruderAgent;
import Group9.agent.factories.DefaultAgentFactory;
import Group9.map.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;


public class Tester {

    public static void main(String[] args) throws IOException {
        int games = 1;//Number of Games
        int IntruderWin = 0;
        int GuardWin = 0;
        double GuardPercentage = 0;
        double IntruderPercentage = 0;

        File file = new File("Experiment_1.txt");
        PrintWriter document = new PrintWriter(new FileWriter(file, true));

        for (int i = 0; i < games; i++) {
            Game game = new Game(Parser.parseFile("./src/Group9/map/maps/Mazy.map"), new DefaultAgentFactory(), false);
            game.run();
            if (game.getWinner().equals(Game.Team.GUARDS)) {
                GuardWin++;
                GuardPercentage = (GuardWin * 100) / games;
            } else {
                IntruderWin++;
                IntruderPercentage = (IntruderWin * 100) / games;
            }

        }

        document.println("Number of games : " + games);
        document.println("Number of games guard won: " + GuardWin);
        document.println("Number of games intruder won: " + IntruderWin);
        document.println("Guard win percentage :  " + GuardPercentage + "%");
        document.println("Intruder win percentage: " + IntruderPercentage + "%");
        document.close();
        System.out.println("DONE");

    }

}