package Group9.agent.factories;

import Group9.agent.Guard2.Tactical;
import Group9.agent.RandomIntruderAgent;
import Interop.Agent.Guard;
import Interop.Agent.Intruder;

import java.util.ArrayList;
import java.util.List;

public class TacticalAgentFactory implements IAgentFactory{
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
            guards.add(new Tactical());
        }
        return guards;
    }
}
