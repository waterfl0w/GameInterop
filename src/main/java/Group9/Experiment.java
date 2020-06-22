package Group9;

import Group9.agent.RandomAgent;
import Group9.agent.RandomIntruderAgent;
import Group9.agent.factories.IAgentFactory;
import Group9.agent.gridbased.GridBased;
import Group9.agent.odyssey.GridMap;
import Group9.map.GameMap;
import Group9.map.parser.Parser;
import Interop.Agent.Guard;
import Interop.Agent.Intruder;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public class Experiment {

    public static void main(String[] args) {

        StringBuilder output = new StringBuilder();

        // test_2 | mazy | test_tunnel_objects

        final int n = 500;
        // Simple -> test_2.map
        // Mazy -> mazy.map
        // test_tunnel_objects.map
        File simple = new File("./src/main/java/Group9/map/maps/test_2.map");
        File mazy = new File("./src/main/java/Group9/map/maps/mazy.map");
        File test_tunnel_objects = new File("./src/main/java/Group9/map/maps/test_tunnel_objects.map");

        Class<? extends Intruder> intruderClass = RandomIntruderAgent.class;
        Class<? extends Guard> guardClass = GridBased.class;

        int intruderWins = 0;
        int guardWins = 0;

        GameMap gameMap = Parser.parseFile(test_tunnel_objects.getAbsolutePath());
        for (int i = 0; i < n; i++) {

            Game game = new Game(gameMap.clone(), new IAgentFactory() {
                @Override
                public List<Intruder> createIntruders(int amount) {
                    List<Intruder> intruders = new LinkedList<>();
                    for (int i = 0; i < amount; i++)
                    {
                        try {
                            intruders.add(intruderClass.getDeclaredConstructor().newInstance());
                        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    return intruders;
                }

                @Override
                public List<Guard> createGuards(int amount) {
                    List<Guard> guards = new LinkedList<>();
                    for (int i = 0; i < amount; i++)
                    {
                        try {
                            guards.add(guardClass.getDeclaredConstructor().newInstance());
                        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    return guards;
                }
            }, false);
            game.run();
            if(game.getWinner() == Game.Team.INTRUDERS)
            {
                intruderWins++;
            }
            else
            {
                guardWins++;
            }

            Game.Team winner = game.getWinner();
            output.append(String.format("%d,%d,%d,%d\n", winner == Game.Team.INTRUDERS ? 1 : 0, winner == Game.Team.GUARDS ? 1 : 0, game.intruderActions, game.guardActions));
            System.out.println(intruderWins + " - " + guardWins);

        }

        System.out.println(output);



    }

}
