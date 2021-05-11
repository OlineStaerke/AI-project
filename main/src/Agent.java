import java.util.*;

public class Agent extends Object {

    ArrayList<Box> boxes = new ArrayList<>();
    int distance_to_goal = 100;
    Boolean blank = false;
    public SubGoals.SubGoal currentGoal;
    public SubGoals.SubGoal nextGoal;
    public SubGoals subgoals;
    Box attached_box;


    public Agent(Node node, String ID) {
        finalPlan = new ArrayList<>();
        finalPlan.add(node);
        finalPlanString = new ArrayList<>();
        finalPlanString.add(node.getNodeId());
        position = node;
        this.ID = ID;
        currentGoal = null;
        this.Taken = false;
        this.planToGoal = new ArrayList<>();


    }

 //Compare to find the agent who is furthest away from goal, by looking at the plan size.
    public static class CustomComparator implements Comparator<Agent> {
        @Override
        public int compare(Agent o1, Agent o2) {
            Integer o2_value = 0;
            Integer o1_value = 0;
            //Computes of another agents goal is on the agent path. If it is, their value should be smaller.

            if(o1.nextGoal!=null && o2.nextGoal!=null) {

                for (String goal : o1.nextGoal.Obj.Goal) {
                    if (o2.nextGoal.Obj.planToGoal.contains(goal)) {
                        o2_value+= 100;
                    }
                }

                for (String goal : o2.nextGoal.Obj.Goal) {
                    if (o1.nextGoal.Obj.planToGoal.contains(goal)) {
                        o1_value+= 100;
                    }
                }

            }

            if (o1.subgoals.ExistsBlankGoal()) {
                o1_value+= 2000;
            }
            if (o2.subgoals.ExistsBlankGoal()) {
                o2_value+= 2000;
            }


            //The one with longest plan can move first, if other things are 0.

            Integer comparevalue = o2.mainPlan.plan.size() + o2_value;
            return (comparevalue).compareTo(((Integer) o1.mainPlan.plan.size())+o1_value);
        }
    }


    public ArrayList<Node> getFinalPlan(){
        return this.finalPlan;
    }




    public void ExecuteMove(Agent agent,State state, Boolean NoOp) throws InterruptedException {

            Node wantedMove;
            if (NoOp) {
                wantedMove = agent.position;
            }
            else {
                wantedMove = state.stringToNode.get(agent.mainPlan.plan.get(0));
            }

            for (Box b : boxes) {

                if (b!=attached_box && b.currentowner.ID.equals(agent.ID)) {

                    b.finalPlan.add(b.position);
                    b.finalPlanString.add(b.position.NodeId);
                }

            }
            position = wantedMove;
            finalPlan.add(wantedMove);
            finalPlanString.add(wantedMove.getNodeId());

            state.occupiedNodes.put(position.NodeId, this);

            if (mainPlan.plan.size()>0 && !NoOp) {
                mainPlan.plan.remove(0);
            }




            if (attached_box!=null) {
                Node wantedMoveBox;
                if (attached_box.mainPlan.plan.size()==0 || NoOp) {
                    wantedMoveBox = attached_box.position;


                }
                else {
                    wantedMoveBox = state.stringToNode.get(attached_box.mainPlan.plan.get(0));
                    }

                attached_box.finalPlan.add(wantedMoveBox);
                attached_box.finalPlanString.add(wantedMoveBox.NodeId);

                if (attached_box.mainPlan.plan.size() > 0 && !NoOp) {
                    attached_box.mainPlan.plan.remove(0);
                }
                state.occupiedNodes.put(wantedMoveBox.NodeId, attached_box);
                attached_box.position = wantedMoveBox;

                if (attached_box.mainPlan.plan.size()==0 && agent.currentGoal.gType.equals(SubGoals.GoalType.BoxBlanked)) {
                    agent.subgoals.UpdatedBlanked((Box) agent.currentGoal.Obj, true);

                }




            }



            if (blank) {

                if (state.blankPlan.size()>0) {
                    state.blankPlan.remove(0);
                }

                //The conflicts has been solved

                if (mainPlan.plan.size()==0 && conflicts!=null ) {

                    blank = false;
                    conflicts.blank = true;

                    // THIS LINE OF CODE RUINS STUFF WITH BOXES LETS TRY TO FIX IT!!

                    if(!conflicts.isInGoal()) {
                        if (conflicts.conflicts!=null && conflicts.currentGoal.gType!= SubGoals.GoalType.BoxBlanked && conflicts.mainPlan.plan.size() ==0) {

                            conflicts.bringBlank(state, conflicts);
                            //conflicts.planPi(state,new LinkedHashSet());

                        }
                        else {
                            if (conflicts.mainPlan.plan.size()==0){
                            //conflicts.planPi(state,new LinkedHashSet());
                            }
                        }
                    }
                    if (conflicts.conflicts == agent) {
                        conflicts.conflicts = null;
                        conflicts = null;
                    }

                }

            }


        }



    @Override
    boolean isInGoal() {
        return subgoals.InGoal();

    }

    boolean attachedBox(State state) {
        if (attached_box==null) {return false;}
        return (state.map.getAdjacent(position.NodeId).contains(attached_box.position.NodeId));
    }

    boolean isInSubGoal() {
        if (Goal.size()==0) {
            return true;
        }
        else {
            return (Goal.contains(position.NodeId));
        }
    }

    public void planGoals(State state, LinkedHashSet visited) throws InterruptedException {
        subgoals = new SubGoals(boxes, this, state);
        planPi(state, visited);
    }


    public void planPi(State state,LinkedHashSet visited) throws InterruptedException {

        mainPlan.plan = new ArrayList<>();
            if (currentGoal != null) (currentGoal.Obj).Taken = false;
            var SG = subgoals.ExtractNextGoal(currentGoal);

            currentGoal = SG;


            if (currentGoal != null) {
                currentGoal.Obj.Taken = true;
                switch (currentGoal.gType) {
                    case BoxBlanked:
                        // If this is 1:1 with BoxToGoal case, remove the code (dupliacte code)
                        if ((state.map.getAdjacent(position.NodeId)).contains(SG.Obj.position.NodeId)) {


                            var neighs = state.map.map.get(SG.Obj.position.NodeId);
                            attached_box = (Box) SG.Obj;
                            ((Box) SG.Obj).currentowner = this;

                            mainPlan.createPlanWithBox(state, this, null, (Box) SG.Obj);


                        } else {

                            attached_box = null;
                            mainPlan.createPlan(state, position.NodeId, state.map.getAdjacent(SG.Obj.position.NodeId), visited);
                            ((Box) SG.Obj).currentowner = this;
                        }
                        break;

                    case BoxToGoal:
                        // code block

                        if ((state.map.getAdjacent(position.NodeId)).contains(SG.Obj.position.NodeId)) {

                            attached_box = (Box) SG.Obj;
                            mainPlan.createPlanWithBox(state, this, SG.Obj.Goal, (Box) SG.Obj);
                            ((Box) SG.Obj).currentowner = this;



                        } else {

                            attached_box = null;
                            mainPlan.createPlan(state, position.NodeId, state.map.getAdjacent(SG.Obj.position.NodeId), visited);
                            ((Box) SG.Obj).currentowner = this;
                        }
                        break;

                    case AgentToGoal:
                        List<String> goalListAgent = new ArrayList<>();
                        goalListAgent.addAll(SG.Obj.Goal);
                        mainPlan.createPlan(state, position.NodeId, goalListAgent, visited);
                        attached_box = null;
                        planToGoal = new ArrayList<>(mainPlan.plan);
                        break;
                    case AgentBlanked:
                        bringBlank(state,conflicts);

                }

                distance_to_goal = mainPlan.plan.size();


            }
            else {
                attached_box = null;

            }
        }


    // Must update the new position of blanked agent
    public void bringBlank(State state, Agent agent) throws InterruptedException {

        //!state.occupiedNodes.containsKey(mainPlan.plan.get(0))
        blank = true;
        if ( mainPlan.plan.size()!=0 && (!state.occupiedNodes.containsKey(mainPlan.plan.get(0)))){
            state.blankPlan = new ArrayList<>(mainPlan.plan);
            return;
        }

        if ( attached_box!= null && mainPlan.plan.size()!=0 ){

            //If the first move is its own position, look one further
            String wantedMoveAgent = mainPlan.plan.get(0);
            String wantedMoveBox = attached_box.mainPlan.plan.get(0);

            Boolean check_wantedMoveAgent = (!state.occupiedNodes.containsKey(wantedMoveAgent)) || wantedMoveAgent ==attached_box.position.NodeId;
            Boolean check_wantedMoveBox = (!state.occupiedNodes.containsKey(wantedMoveBox)) || wantedMoveBox ==position.NodeId ;
            if(check_wantedMoveAgent && check_wantedMoveBox) {

                state.blankPlan = new ArrayList<>(mainPlan.plan);
                return;
            }
        }

        if (attached_box!=null && (conflicts.mainPlan.plan.contains(attached_box.position.NodeId))) {
            subgoals.UpdatedBlanked(attached_box,false);
            planPi(state,new LinkedHashSet());
            //mainPlan.createPlanWithBox(state, this, null, attached_box);
        }
        else {

            mainPlan.createAltPaths(state, agent);
            attached_box = null;
        }

    }




}



