package Group9.experiments;

import Group9.Game;
import Group9.agent.factories.ShallowSpaceAgentFactory;
import Group9.map.parser.Parser;

public class SimpleExperimentMazeVsOpen {
    public static void main(String[] args) {
        Game game = new Game(
                Parser.parseFile("./src/main/java/Group9/experiments/mazy_experiment.map"),
                //Parser.parseFile("./src/main/java/Group9/experiments/open_unmazed_experiment.map"),
                new ShallowSpaceAgentFactory(),
                false
        );


        long time = System.currentTimeMillis();

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                game.run();
            }
        });

        t1.start();

        // Setting it up as a thread in case we might wan to query Game every X seconds to see how many intruders
        // have been captured, or ... something.

        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Winner: " + game.getWinner());
        System.out.println("time: " + (System.currentTimeMillis() - time));
    }
}
