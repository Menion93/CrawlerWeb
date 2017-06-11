package crawler;

import graph.GraphRepository;
import graph.NodePage;
import scorer.Scorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Andrea on 07/03/2017.
 */
public class Ant
{

    private List<Edge> path;
    private Scorer scorer;
    private Random randomGenerator;
    private double randomInitValue;
    private boolean cachePages;


    HashMap<Edge, Double > edge2trail;


    public Ant(Scorer scorer, HashMap<Edge, Double > edge2trail, double randomInitValue, int seed, boolean cachePages){
        this.path = new ArrayList<>();
        this.scorer = scorer;
        this.edge2trail = edge2trail;
        this.randomInitValue = randomInitValue;
        this.cachePages = cachePages;
        randomGenerator = new Random(seed);

    }


    public void AntCycle(NodePage startNode, HashMap<String,Evaluation> id2score, GraphRepository graphRepo, int numberOfStep) throws IOException {

        this.path = new ArrayList<>();

        NodePage currentNode = startNode;

        if(!id2score.containsKey(currentNode.getId()))
        {
            id2score.put(currentNode.getId(), new Evaluation(scorer.predictScore(currentNode), 0));
            if(!cachePages)
                currentNode.freeContentMemory();
        }

        for(int j=0; j<numberOfStep;j++)
        {
            List<NodePage> frontier = graphRepo.expandNode(currentNode);

            if (frontier == null)
                return;

            NodePage successorNode = selectNode(currentNode, frontier);

            while(path.contains(successorNode) && frontier.size() > 0){
                frontier.remove(successorNode);
                successorNode = selectNode(currentNode, frontier);
            }

            if(successorNode == null)
                return;

            if(path.contains(successorNode))

            if(!id2score.containsKey(successorNode.getId()))
            {
                id2score.put(successorNode.getId(), new Evaluation( scorer.predictScore(successorNode), j));
                if(!cachePages)
                    successorNode.freeContentMemory();
            }

            path.add(new Edge(currentNode.getId(), successorNode.getId()));

            currentNode = successorNode;
        }

    }

    // To do: select a node with probability according to the trail, or randomValue
    // if it is the last iteration
    private NodePage selectNode(NodePage currentNode, List<NodePage> frontier)
    {
        if(frontier.size() == 0)
            return null;

        double[] trailsProba = new double[frontier.size()];
        double sumTrails = 0;

        for(int i=0; i<frontier.size(); i++)
        {
            Edge e = new Edge(currentNode.getId(), frontier.get(i).getId());
            if(edge2trail.containsKey(e))
            {
                trailsProba[i] = edge2trail.get(e);
            }
            else
            {
                edge2trail.put(e, randomInitValue);
                trailsProba[i] = randomInitValue;
            }

            sumTrails += trailsProba[i];
        }

        // Normalize probability
        for(int i=0; i<trailsProba.length; i++)
        {
            trailsProba[i] /= sumTrails;
        }


        // Chose random a successor between those probability
        double nodeChoice = randomGenerator.nextDouble();
        double currentProba = 0;

        for(int i=0; i<trailsProba.length; i++)
        {
            currentProba += trailsProba[i];

            if(nodeChoice < currentProba)
                return frontier.get(i);
        }

        System.out.println("ERROR in the selection of the successor");
        return null;
    }

    public List<Edge> getPath() {
        return path;
    }
}
