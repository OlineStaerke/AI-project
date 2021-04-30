import java.security.cert.CertificateRevokedException;
import java.util.*;

public class MaPPAlgorithm {


    public static void MaPPVanilla(State state) throws InterruptedException {

        for(Agent agent : state.agents.values()){
            // Finds initial plan with BFS
            agent.planGoals(state, new LinkedHashSet());

        }

        boolean goalIsReached = false;

        // TODO: Check if node.equals works (it might be pointing towards reference in memory, not actual value@Mathias
        // Each iteration is a processing of 1 step
        // Follows Algo 1, has "Progression step" and "repositioning step" merged to improve speed.

        ArrayList<Agent> agentsInOrder = new ArrayList<>(state.agents.values());
        Collections.sort(agentsInOrder,new Agent.CustomComparator());
        while(!goalIsReached){
            System.err.println();


            // Copy of agents which are then sorted w.r.t. priority. Must be done dynamically, as order can change

            //
            //
            Thread.sleep(1000);

            System.err.println("-----------------------------------");
            System.err.println(state.occupiedNodes);

            System.err.println("Agents in order :"+agentsInOrder);


            //agentsInOrder = new ArrayList<>(newAgentsInOrder);
            agentsInOrder = new ArrayList<>(state.agents.values());
            Collections.sort(agentsInOrder,new Agent.CustomComparator());

            for(Agent agent : agentsInOrder) {

                agent.subgoals.UpdateGoals();

                System.err.println("//////////////////////");
                System.err.println(agent);
                System.err.println("MAINPLAN:"+agent.mainPlan.plan);
                System.err.println("IN GOAL "+agent.isInGoal());



                if ((agent.mainPlan.plan.size()> 0) && (state.blankPlan.size()<=0||agent.blank||(!state.agentConflicts.contains(agent) && !agent.position.isTunnel))) {


                    String wantedMove= agent.mainPlan.plan.get(0);



                    // wantedMove = position: Stay.



                    if (agent.attached_box!=null && wantedMove.equals(agent.attached_box.position.NodeId)) {
                        agent.ExecuteMove(agent,state,state.stringToNode.get(wantedMove), false);
                    }
                    else if (wantedMove.equals(agent.position.NodeId) ) {
                        agent.ExecuteMove(agent,state,state.stringToNode.get(wantedMove),false);

                    }



                    // Agent wants to move into an occupied cell
                    else if (state.occupiedNodes.containsKey(wantedMove)) {
                        System.out.println("CONFLICT! I am agent:"+agent.ID);
                        // Bring Blank and move
                        var occupyingObject = state.occupiedNodes.get(wantedMove);


                        System.err.println("!! I want: "+ wantedMove+" !! Occypied by :"+ occupyingObject);


                            // Agent is blocking
                            if (occupyingObject instanceof Agent) {
                                var occupyingAgent = (Agent) occupyingObject;

                                if (state.stringToNode.get(wantedMove).isTunnel) {
                                    state.agentConflicts.add(occupyingAgent);
                                }

                                if (occupyingAgent.conflicts == agent) {
                                    LinkedHashSet visited = new LinkedHashSet(state.occupiedNodes.keySet());
                                    visited.remove(agent.position.NodeId);
                                    occupyingAgent.planPi(state, visited);
                                    occupyingAgent.blank = true;
                                } else {
                                    occupyingAgent.blank = true;
                                    occupyingAgent.conflicts = agent;

                                    agent.blank = false;

                                    occupyingAgent.bringBlank(state,  occupyingAgent);
                                }

                            }
                            // Box is blocking
                            else{
                                var occupyingBox = (Box) occupyingObject;

                                if (state.stringToNode.get(wantedMove).isTunnel) {
                                    state.agentConflicts.add(occupyingBox.owner);
                                }

                                // Your own box is blocking :(
                                // Maybe handle seperatly? idk
                                if (state.NameToColor.get(occupyingBox.ID).equals(state.NameToColor.get(agent.ID))){
                                } else{

                                    occupyingBox.owner.blank = true;
                                    occupyingBox.owner.conflicts = agent;
                                    System.err.println("OWNER: " + occupyingBox.owner.ID);


                                    occupyingBox.bringBlank(state,  occupyingBox.owner);
                                    System.err.println("PLAN2: " + agent.mainPlan.plan);

                                }

                            }

                            // Do nothing, (NoOP). So the agent waits if he cannot enter a cell, or he has tried to make someone blank.
                            agent.ExecuteMove(agent,state,agent.position, true);




                    }
                    // Empty cell
                    else if (!state.occupiedNodes.containsKey(wantedMove)) {

                        agent.ExecuteMove(agent,state, state.stringToNode.get(wantedMove), false);
                    } else {

                        agent.ExecuteMove(agent,state,agent.position, true);
                    }
                }
                    else {
                        //agent.setAgentsFree(state);

                        agent.ExecuteMove(agent,state,agent.position, true);


                        // Agent is not in goal, proceed with next subgoal
                    /***
                        if (!agent.isInGoal()) {
                            for (Box B : agent.boxes) {
                                if (!B.isInGoal()) {
                                    agent.mainPlan.createPlan(state.map, agent.position.NodeId,
                                            B.position.NodeId, new LinkedHashSet<>());
                                    break;
                                }
                            }
                            // All boxes are in goals, go to finish.
                            if (agent.mainPlan.plan.size() == 0) {
                                agent.planPi(state.map);
                            }
                        }
***/

                    }

                     //Update the subgoals
                    agent.subgoals.UpdateGoals();

                for(Agent AA : agentsInOrder) {
                    System.err.println("PLANANAN:  " + AA.ID + "  " + AA.mainPlan.plan);

                }
                }


            // Boxes are automatically checked in agent.isInGoal
            state.UpdateOccupiedNodes();



            goalIsReached = true;
            Boolean noroutes = true;
            Boolean anyAgentBlank = false;
            for(Agent agent : agentsInOrder) {



                if (!agent.isInGoal()) {
                    goalIsReached = false;
                }
                if (agent.blank) {
                    anyAgentBlank = true;
                }
                if (agent.mainPlan.plan.size()>0) {
                    noroutes = false;
                }

            }

            if (noroutes) {
                for(Agent agent : agentsInOrder) {


                   if (!agent.isInSubGoal()) {

                     
                       if (state.agentConflicts.size()>0) {
                           agent.blank = true;

                           LinkedHashSet visited = new LinkedHashSet(state.occupiedNodes.keySet());
                           visited.remove(agent.position.NodeId);

                           System.err.println("PLAN PI");
                           agent.planPi(state, visited);
                           System.err.println("PLAN3: " + agent.mainPlan.plan);

                           //state.agentConflicts.remove(agent);
                           // if (agent.mainPlan.plan.size()>0) {
                           //  state.blankPlan = new ArrayList<>(agent.mainPlan.plan);
                           //if (anyAgentBlank) break;
                           break;
                           //}
                       }
                       else {
                           if (!agent.isInGoal()) {
                           LinkedHashSet visited = new LinkedHashSet();
                           visited.remove(agent.position.NodeId);
                           agent.planPi(state, visited);
                               System.err.println("PLAN10: " + agent.mainPlan.plan);


                           }
                       }
                   }
                }

            }

        }

    }


}
