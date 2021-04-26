import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Objects;

public class Box extends Object {

    Agent owner;
    String problemnode;


    public Box(Node node, char id){
        this.position = node;
        this.ID = id;
    }

    public void bringBlank(State state, Map map, Agent otherAgent, String problem_node){
        // Things that Box.bringBlank should do:
        // Create a new path (like agent.bringBlank)

        // Signal to the owner agent, that this box should be moved (extend their mainplan)

    }

    @Override
    boolean isInGoal() {
        if (Objects.isNull(Goal)) return true;
        return Goal.NodeId.equals(position.NodeId);
    }

    @Override
    void planPi(Map map) {
        mainPlan.createPlan(map, position.NodeId, Goal.NodeId,new LinkedHashSet<>());


    }

    @Override
    void bringBlank(State state, Map map, Agent otherAgent, String problem_node, Agent agent) {

    }

    @Override
    boolean passedProblem() {
        if(problemnode == null) {
            return true;
        }
        else return false;
    }
}
