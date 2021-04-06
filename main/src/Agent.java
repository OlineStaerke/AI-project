import java.util.ArrayList;
import java.util.HashMap;

public class Agent extends Object {
    Plan mainPlan;
    AlternativePlan altPlans;
    ArrayList<Node> finalPlan;
    Node[] privateZone;
    Node initialState;


    public Agent(Node node){
        this.initialState = node;
        // The finalPlan (output plan) of an agent must always contain the initial node)
        finalPlan = new ArrayList<>();
        finalPlan.add(node);
        altPlans = new AlternativePlan();
    }

    public void planAltPaths() {}

    public void planPi(Map map) {
        mainPlan.createPlan(map, position, Goal);
    }

    public void ExecuteMove(State state, Node wantedMove) {
        state.occupiedNodes.remove(position.NodeId, this);
        position = wantedMove;
        finalPlan.add(wantedMove);
        state.occupiedNodes.put(position.NodeId, this);
        mainPlan.plan.remove(0);
    }

    // Must update the new position of blanked agent
    public void bringBlank() {}

    // Tries to reposition
    // An agent can reposition iff: Agent is not on pi. Agent has not moved this iteration.
    public void reposition(){
        if(finalPlan.size() >= 2 &&
            finalPlan.get(finalPlan.size()-1).equals(finalPlan.get(finalPlan.size()-2)) &&
            !mainPlan.plan.contains(position)){

            // Idea: use alt paths. Append alternative path in front of mainPlan, s.t. the agent will start executing.
            // Reuse an alt path or create a new one
            if (!altPlans.altPaths.containsKey(position))
                planAltPaths();
            mainPlan.plan.addAll(0, altPlans.altPaths.get(position).plan);

        }
    }


}
