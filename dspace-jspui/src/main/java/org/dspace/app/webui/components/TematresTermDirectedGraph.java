/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.components;

import org.dspace.app.webui.components.TematresTerm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Class for relationships between Tematres terms
 * 
 * @author Sinayra Pascoal Cotts Moreira
 *
 */
public class TematresTermDirectedGraph {
	// contains set of nodes
	private HashSet<NodeClass> nodes = new HashSet<NodeClass>();

	// set of directed edges
	private HashSet<EdgeContents> edges = new HashSet<EdgeContents>();

	// maps a nodeId -> node
	private HashMap<Integer, NodeClass> nodeMap = new HashMap<Integer, NodeClass>();

	/**
	 * @param graph
	 * @param nodeToAdd Adds a node to the graph. Node is added to the HashMap
	 *                  nodeMap of the graph,node is added to HashSet nodes in graph
	 */
	public NodeClass addNode(TematresTerm nodeTerm) {
		NodeClass nodeToAdd = new NodeClass(nodeTerm);
		this.nodes.add(nodeToAdd);
		this.nodeMap.put(nodeTerm.getId(), nodeToAdd);

		return nodeToAdd;
	}

	public void addNode(NodeClass nodeToAdd) {
		this.nodes.add(nodeToAdd);
		this.nodeMap.put(nodeToAdd.term.getId(), nodeToAdd);
	}

	public void addEdges(NodeClass startingNode, NodeClass finishingNode, String relType) {
		EdgeContents edge = new EdgeContents(startingNode, finishingNode, relType);
		this.edges.add(edge);
		startingNode.getEdges().add(edge);
		this.nodes.add(startingNode);
	}

	public HashMap<Integer, NodeClass> getNodeMap() {
		return nodeMap;
	}

	public void setNodeMap(HashMap<Integer, NodeClass> nodeMap) {
		this.nodeMap = nodeMap;
	}

	public NodeClass getParticularNode(TematresTerm term) {
		if (this.nodeMap.containsKey(term.getId())){
			return this.nodeMap.get(term.getId());
		}

		return null;
	}

	public boolean containsParticularNode(TematresTerm term) {
		return this.nodeMap.containsKey(term.getId());
	}

	public HashSet<NodeClass> getAllNodes() {
		return this.nodes;
	}

	public HashSet<EdgeContents> getEdges() {
		return this.edges;
	}

	public void setAllNodesNotVisited(){
		Iterator<NodeClass> it = this.nodes.iterator();
		
		//setting every node to not visited
		while(it.hasNext()){
			NodeClass n = it.next();
			n.setVisited(false);
			n.setAddedToList(false);
		}
	}


	// node class consists of node name a.k.a id of node ,set of directed edges
	// a.k.a arcs

	public static class NodeClass {

		private TematresTerm term;
		private boolean visited;
		private boolean addedToList;
		private HashSet<EdgeContents> edges;

		public NodeClass(TematresTerm term) {
			this.term = term;
			this.edges = new HashSet<EdgeContents>();
		}

		// copy constructor
		public NodeClass(NodeClass anotherNode) {
			this.term = anotherNode.term;
			this.edges = anotherNode.edges;
		}

		public TematresTerm getTerm() {
			return this.term;
		}
		
		public boolean getVisited() {
			return this.visited;
		}

		public boolean getAddedToList() {
			return this.addedToList;
		}


		public HashSet<EdgeContents> getEdges() {
			return this.edges;
		}
		
		public void setVisited(boolean visited){
		    this.visited = visited;
		}

		public void setAddedToList(boolean addedToList){
		    this.addedToList = addedToList;
		}

		@Override
		public boolean equals(Object obj) {
			NodeClass t = (NodeClass) obj;
			return t.term == this.term;
		}

	}

	// definition of arc a.k.a edge
	public static class EdgeContents {
		private NodeClass startingNode;
		private NodeClass finishingNode;
		private String relType;

		public EdgeContents(NodeClass startingNode, NodeClass finishingNode, String relType) {
			this.startingNode = startingNode;
			this.finishingNode = finishingNode;
			this.relType = relType;
		}
		
		public NodeClass getStartingNode() {
			return this.startingNode;
		}

		public NodeClass getFinishingNode() {
			return this.finishingNode;
		}

		public String getRelType() {
			return this.relType;
		}

		@Override
		public boolean equals(Object obj) {
			EdgeContents e = (EdgeContents) obj;
			return e.finishingNode == this.finishingNode && e.startingNode == this.startingNode;
		}

	}
}
