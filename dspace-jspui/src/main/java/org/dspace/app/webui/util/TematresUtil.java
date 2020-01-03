/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONArray;

import org.dspace.app.webui.components.TematresTerm;
import org.dspace.app.webui.components.TematresTermDirectedGraph;


/**
 * This class provides a set of methods to load from
 * Tematres and to return proper html
 * 
 * @author Sinayra Pascoal Cotts Moreira
 * 
 */
public class TematresUtil
{
	// the log
    private final Logger log = Logger.getLogger(TematresUtil.class);

	// path to the Tematres service
    private final String TEMATRESUTIL_BASEURL
            = "http://localhost/tematres/vocab/services.php";

	//type of output to the tematres service
	private final String TEMATRESUTIL_OUTPUT
			= "output=json";

	//path images
	private final String PLUS_IMAGE = "/jspui/image/controlledvocabulary/p.gif";
	private final String FINAL_IMAGE = "/jspui/image/controlledvocabulary/f.gif";


    /**
     * Get Response from URL
     * 
     * @param args
     *            The GET args
     * @return a JSON data object
     */
    private JSONObject getResponseData(String args) 
    {
		int resp = 0;
		JSONObject data = null;
		try{
			String urlPath = TEMATRESUTIL_BASEURL + "?" + args + "&" + TEMATRESUTIL_OUTPUT;
			URL url = new URL(urlPath);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();

		    resp = connection.getResponseCode();

			if(resp == 200){
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				while((inputLine = in.readLine()) != null){
					response.append(inputLine);			
				}
				in.close();

				data = new JSONObject(response.toString());
			}
		}
		catch(Exception e){
			log.error("Exception", e);
		}

		return data;
    }
    

	/**
     * Set a Tematres term object from JSON
     * 
     */
	private void setTematresTerm(JSONObject JSONterm, TematresTerm term){
		
		for(String key : JSONterm.keySet()){
			Object value = JSONterm.get(key);
			switch(key){
				case "term_id":
					term.setId(Integer.parseInt(value.toString()));
					break;
				case "string":
					term.setName(value.toString());
					break;
				case "isMetaTerm":
					term.setIsMetaTerm(Integer.parseInt(value.toString()) == 1);
					break;
			}
		}			
	}
	
	/**
     * Create a directed Graph with terms
     * 
     * @return a graph with all relations
     */
	private TematresTermDirectedGraph createAllHierarchyRelations(HashMap<Integer, TematresTerm> terms){
		TematresTermDirectedGraph g = new TematresTermDirectedGraph();
		String args ="task=relationsSince&arg=2019-12-05";
		JSONObject data = getResponseData(args);
		JSONArray result = data.getJSONArray("result");

		Iterator<Object> it = result.iterator();
		while(it.hasNext()){
			JSONObject JSONelem = (JSONObject)it.next();
			TematresTerm starting = null, finishing = null;
			String relType = "";
			TematresTermDirectedGraph.NodeClass nStarting = null, nFinishing = null;

			for(String key : JSONelem.keySet()){
				Object value = JSONelem.get(key);
				String valueStr = value.toString();
				Integer id = 0;
				
				switch(key){
					case "lterm_id":
						id = Integer.parseInt(valueStr);
						if(terms.containsKey(id)){
							starting = terms.get(id);
						}
						break;
					case "rterm_id":
						id = Integer.parseInt(valueStr);
						if(terms.containsKey(id)){
							finishing = terms.get(id);
						}
						break;
					case "relType":
						relType = valueStr;
						break;		
				}
			}
			
			if(starting != null){
				nStarting = (g.containsParticularNode(starting) ? g.getParticularNode(starting) : g.addNode(starting));
			}
			if(finishing != null){
				nFinishing = (g.containsParticularNode(finishing) ? g.getParticularNode(finishing) : g.addNode(finishing));
			}
			
			if(nStarting != null && nFinishing != null){
				g.addEdges(nStarting, nFinishing, relType);
			}
			
		}
		
		return g;
	}
	
	/**
     * Create map of terms created in Tematres
     * 
     * @return a hashmap relating id with a term object
     */
	private HashMap<Integer, TematresTerm> getAllTerms(){
		HashMap<Integer, TematresTerm> terms = new HashMap<Integer, TematresTerm>();
		String args ="task=termsSince&arg=2019-12-05";
		JSONObject data = getResponseData(args);
		JSONObject result = data.getJSONObject("result");
		
		for(String id : result.keySet()){
			JSONObject JSONterm = (JSONObject)result.get(id);
			TematresTerm newTerm = new TematresTerm();
			
			setTematresTerm(JSONterm, newTerm);			
			
			terms.put(newTerm.getId(), newTerm);
		}
		
		return terms;
	}
	
	/**
     * Depth-first search from src setting each vertex to visited
     * 
     */
	private	void DFS(TematresTermDirectedGraph.NodeClass src){
		src.setVisited(true);
		
		Iterator<TematresTermDirectedGraph.EdgeContents> edgeSet = src.getEdges().iterator();
		while(edgeSet.hasNext()){
			TematresTermDirectedGraph.NodeClass n = edgeSet.next().getFinishingNode();
			if(!n.getVisited()){
				DFS(n);
			}
		}
	}

	/**
     * Find node that can reach all nodes
     * 
     * @return mother node
     */
	private TematresTermDirectedGraph.NodeClass findMother(TematresTermDirectedGraph g){
		Iterator<TematresTermDirectedGraph.NodeClass> it = null;
		TematresTermDirectedGraph.NodeClass mother = null;

		//setting every node to not visited
		g.setAllNodesNotVisited();
		
		//for each node
		it = g.getAllNodes().iterator();
		while(it.hasNext()){
			TematresTermDirectedGraph.NodeClass n = it.next();

			//if this node was not visited
			if(!n.getVisited()){
				DFS(n);	//visit and mark every node reachable from it
				mother = n; //and it must be the mother
			}
		}
		
		//reseting every node to not visited and try another DFS from the mother
		g.setAllNodesNotVisited();
		DFS(mother);
		
		//checking if every node was reached from mother
		it = g.getAllNodes().iterator();
		while(it.hasNext()){
			TematresTermDirectedGraph.NodeClass n = it.next();
			if(!n.getVisited()){
				mother = null; //in the end, there is other nodes not reachable from mother
				break;
			}	
		}

		return mother;
	}

	private String getHierarchyHTML(boolean hasHierarchy, TematresTerm term){
		String html = "";

		if(hasHierarchy){
			html += "<li>";
			html += "<img class=\"controlledvocabulary\" src=\"" + PLUS_IMAGE + "\" onclick=\"ec(this, '/jspui');\" alt=\"expand search term category\"/>";
			html += "<a id=\"" + term.getId() + "\" href=\"javascript:void(null);\" onclick=\"javascript: i(this);\" class=\"value\">" + term.getName() + "</a>";
			html += "<ul class=\"controlledvocabulary\">";		
		}
		else{
			html += "<li>";
			html += "<img class=\"dummyclass\" src=\"" + FINAL_IMAGE + "\" alt=\"search term\"/>";
			html += "<a id=\"" + term.getId() + "\" href=\"javascript:void(null);\" onclick=\"javascript: i(this);\" class=\"value\">" + term.getName() + "</a>";
			html += "</li>";
		}

		return html;
	}

	/**
     * Depth-first search from src rendering each vertex
     * 
     */

	private String DFSRenderHTMLHelper(TematresTermDirectedGraph g, TematresTermDirectedGraph.NodeClass src){
		Iterator<TematresTermDirectedGraph.EdgeContents> edgeSet = src.getEdges().iterator();
		String html = "";
		TematresTerm term = src.getTerm();
		boolean hasRendered = false;
		List<TematresTermDirectedGraph.NodeClass> relatedNodes = new ArrayList<TematresTermDirectedGraph.NodeClass>();
		List<TematresTermDirectedGraph.NodeClass> specificNodes = new ArrayList<TematresTermDirectedGraph.NodeClass>();

		if(!edgeSet.hasNext()){
			html += getHierarchyHTML(false, term);
			src.setVisited(true);
		}
		else{
			while(edgeSet.hasNext()){
				TematresTermDirectedGraph.EdgeContents relation = edgeSet.next();
				TematresTermDirectedGraph.NodeClass n = relation.getFinishingNode();

				if(!src.getVisited() && !(relation.getRelType().equals("TR"))){
					n.setAddedToList(true);
					specificNodes.add(n);
				}
				else{
					if(!n.getAddedToList() && !n.getVisited()){
						n.setAddedToList(true);
						relatedNodes.add(n);
					}
				}
			}
			if(!specificNodes.isEmpty()){
				Iterator<TematresTermDirectedGraph.NodeClass> it = specificNodes.iterator();
				
				html += getHierarchyHTML(true, term);
				src.setVisited(true);
				while(it.hasNext()){
					TematresTermDirectedGraph.NodeClass n = it.next();
					html += DFSRenderHTMLHelper(g, n);
				}
				html += "</ul>";
				html += "</li>";
			}
			if(!relatedNodes.isEmpty()){
				Iterator<TematresTermDirectedGraph.NodeClass> it = relatedNodes.iterator();
				while(it.hasNext()){
					TematresTermDirectedGraph.NodeClass n = it.next();
					html += DFSRenderHTMLHelper(g, n);

					if(!n.getVisited()){
						html += getHierarchyHTML(false, n.getTerm());
						n.setVisited(true);
					}
				}
			}
		}

		return html;
	}

	private	String DFSRenderHTML(TematresTermDirectedGraph g, TematresTermDirectedGraph.NodeClass src){
		g.setAllNodesNotVisited();
		String html = DFSRenderHTMLHelper(g, src);
		return html;
	}
	
	/**
     * Get all terms from Tematres and creates the HTML for each
     * 
     * @return a string to be rendered
     */
	public String renderAllTermsAsHTML(){
		String html = "";
		HashMap<Integer, TematresTerm> terms = getAllTerms();
		TematresTermDirectedGraph g = createAllHierarchyRelations(terms);
		TematresTermDirectedGraph.NodeClass motherNode = findMother(g);

		
		html += "<ul class=\"controlledvocabulary\">";

		html += DFSRenderHTML(g, motherNode);
		
		html += "</ul>";
		
		return html;
	}
	
	/**
     * Check if a term exists in Tematres
     * 
     * @return if there is a result for the search
     */
	public boolean isTematresSearch(String query){
		String args ="task=fetch&arg=" + query;
		JSONObject data = getResponseData(args);

		try{
			JSONObject result = data.getJSONObject("result");
			String id = result.keySet().iterator().next();
			JSONObject JSONterm = (JSONObject)result.get(id);
			if(JSONterm.isNull("term_id")){
				return false;			
			}
		}
		catch(Exception e){
			return false;		
		}

		return true;
	}

	/**
     * Get all related terms
     * 
     * @return a list of terms name
     */
	public List<String> getRelatedList(String query){
		List<String> names = new ArrayList<String>();

		String args ="task=fetch&arg=" + query;
		JSONObject data = getResponseData(args);
		JSONObject result = data.getJSONObject("result");
		TematresTerm term = new TematresTerm();

		String id = result.keySet().iterator().next();

		JSONObject JSONterm = (JSONObject)result.get(id);
		setTematresTerm(JSONterm, term);

		args = "task=fetchRelated&arg=" + term.getId();
		data = getResponseData(args);
		result = data.getJSONObject("result");

		for(String relatedId : result.keySet()){

			JSONterm = (JSONObject)result.get(relatedId);
			TematresTerm newTerm = new TematresTerm();
			
			setTematresTerm(JSONterm, newTerm);			
			
			names.add(newTerm.getName());
		}
		
		return names;
	}

}
