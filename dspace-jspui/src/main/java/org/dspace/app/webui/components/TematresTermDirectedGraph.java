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

/**
 * Class for relationships between Tematres terms
 * 
 * @author Sinayra Pascoal Cotts Moreira
 *
 */
public class TematresTermDirectedGraph {
	// contains set of nodes
	private HashSet<NodeClass> nodes = new HashSet<TematresTermDirectedGraph.NodeClass>();

	// set of directed edges
	private HashSet<edgeContents> edges = new HashSet<TematresTermDirectedGraph.edgeContents>();

	// maps a nodeId -> node
	private HashMap<TematresTerm, NodeClass> nodeMap = new HashMap<TematresTerm, TematresTermDirectedGraph.NodeClass>();

	/**
	 * @param graph
	 * @param nodeToAdd Adds a node to the graph. Node is added to the HashMap
	 *                  nodeMap of the graph,node is added to HashSet nodes in graph
	 */
	public NodeClass addNode(TematresTerm nodeTerm) {
		NodeClass nodeToAdd = new NodeClass(nodeTerm);
		this.nodes.add(nodeToAdd);
		this.nodeMap.put(nodeTerm, nodeToAdd);

		return nodeToAdd;
	}

	public void addNode(NodeClass nodeToAdd) {
		this.nodes.add(nodeToAdd);
		this.nodeMap.put(nodeToAdd.term, nodeToAdd);
	}

	public void addEdges(NodeClass startingNode, NodeClass finishingNode) {
		edgeContents edge = new edgeContents(startingNode, finishingNode);
		this.edges.add(edge);
		startingNode.getEdges().add(edge);
		this.nodes.add(startingNode);
	}

	public HashMap<TematresTerm, NodeClass> getNodeMap() {
		return nodeMap;
	}

	public void setNodeMap(HashMap<TematresTerm, NodeClass> nodeMap) {
		this.nodeMap = nodeMap;
	}

	public NodeClass getParticularNode(TematresTerm term) {
		if (this.nodeMap.containsKey(term)){
			return this.nodeMap.get(term);
		}

		return null;
	}

	public boolean containsParticularNode(TematresTerm term) {
		return this.nodeMap.containsKey(term);
	}

	public HashSet<NodeClass> getAllNodes() {
		return this.nodes;
	}

	public HashSet<edgeContents> getEdges() {
		return this.edges;
	}


	// node class consists of node name a.k.a id of node ,set of directed edges
	// a.k.a arcs

	public static class NodeClass {

		private TematresTerm term;
		private boolean visited;
		private HashSet<edgeContents> edges;

		public NodeClass(TematresTerm term) {
			this.term = term;
			this.edges = new HashSet<TematresTermDirectedGraph.edgeContents>();
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

		public HashSet<edgeContents> getEdges() {
			return this.edges;
		}
		
		public void setVisited(boolean visited){
		    this.visited = visited;
		}

	}

	// definition of arc a.k.a edge
	public static class edgeContents {
		private NodeClass startingNode;
		private NodeClass finishingNode;

		public edgeContents(NodeClass startingNode, NodeClass finishingNode) {
			this.startingNode = startingNode;
			this.finishingNode = finishingNode;
		}
		
		public NodeClass getStartingNode() {
			return this.startingNode;
		}

		public NodeClass getFinishingNode() {
			return this.finishingNode;
		}

		@Override
		public boolean equals(Object obj) {
			edgeContents e = (edgeContents) obj;
			return e.finishingNode == finishingNode && e.startingNode == startingNode;
		}

	}
}
