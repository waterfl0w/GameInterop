package Group9.agent.factories;

import Group9.agent.RandomIntruderAgent;
import Group9.agent.gridbased.GridBased;
import Interop.Agent.Guard;
import Interop.Agent.Intruder;

import java.util.ArrayList;
import java.util.List;

public class GridBasedFactory implements IAgentFactory {

    @Override
    public List<Intruder> createIntruders(int amount) {
        List<Intruder> intruders = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            intruders.add(new RandomIntruderAgent());
        }
        return intruders;
    }

    @Override
    public List<Guard> createGuards(int amount) {
        List<Guard> guards = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            guards.add(new GridBased());
        }
        return guards;
    }
}
