package Group9.agent.factories;

import Group9.agent.RandomAgent;
import Group9.agent.testintruder.TestIntruder;
import Interop.Agent.Guard;
import Interop.Agent.Intruder;

import java.util.ArrayList;
import java.util.List;

public class TestIntruderFactory implements IAgentFactory {
    @Override
    public List<Intruder> createIntruders(int amount) {
        List<Intruder> intruders = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            intruders.add(new TestIntruder());
        }
        return intruders;
    }

    @Override
    public List<Guard> createGuards(int amount) {
        List<Guard> guards = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            guards.add(new RandomAgent());
        }
        return guards;
    }
}
