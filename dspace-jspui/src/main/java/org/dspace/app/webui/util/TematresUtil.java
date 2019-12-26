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
     * Mark hierarchy nodes from graph
     * 
     */
	private void markAllHierarchyRelations(TematresTermDirectedGraph g){
		
	}

	/**
     * Set a Tematres term object from JSON
     * 
     */
	private void setTematresTerm(JSONObject JSONterm, TematresTerm term){
		
		for(String key : JSONterm.keySet()){
			System.out.println("key: " + key);
			Object value = JSONterm.get(key);
			System.out.println("value: " + value.toString());				
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

		
		System.out.println("<CREATE ALL HIERARCHY RELATIONS>");

		Iterator<Object> it = result.iterator();
		while(it.hasNext()){
			JSONObject JSONelem = (JSONObject)it.next();
			TematresTerm starting = null, finishing = null;
			TematresTermDirectedGraph.NodeClass nStarting = null, nFinishing = null;

			for(String key : JSONelem.keySet()){
				System.out.println("key: " + key);
				Object value = JSONelem.get(key);
				System.out.println("value: " + value.toString());
				if(key.equals("lterm_id") || key.equals("rterm_id")){
					Integer id = Integer.parseInt(value.toString());
					if(terms.containsKey(id)){
						if(key.equals("lterm_id")){
							starting = terms.get(id);
						}
						else{
							finishing = terms.get(id);
						}
					}
				}
			}
			
			if(starting != null){
				nStarting = (g.containsParticularNode(starting) ? g.getParticularNode(starting) : g.addNode(starting));
			}
			if(finishing != null){
				nFinishing = (g.containsParticularNode(finishing) ? g.getParticularNode(finishing) : g.addNode(finishing));
			}
			
			if(nStarting != null && nFinishing != null){
				g.addEdges(nStarting, nFinishing);
			}
			
		}

		System.out.println("</CREATE ALL HIERARCHY RELATIONS>");
		
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
		
		System.out.println("<SET ALL TERMS>");
		System.out.println(data.toString());
		for(String id : result.keySet()){
			System.out.println("id: " + id);
			JSONObject JSONterm = (JSONObject)result.get(id);
			TematresTerm newTerm = new TematresTerm();
			
			setTematresTerm(JSONterm, newTerm);			
			
			terms.put(newTerm.getId(), newTerm);
		}

		System.out.println("</SET ALL TERMS>");
		
		return terms;
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
		
		html += "<ul class=\"controlledvocabulary\">";

		for(TematresTerm value : terms.values()){
			html += "<li>";
			html += "<img class=\"dummyclass\" src=\"" + FINAL_IMAGE + "\" alt=\"search term\"/>";
			html += "<a id=\"" + value.getId() + "\" href=\"javascript:void(null);\" onclick=\"javascript: i(this);\" class=\"value\">" + value.getName() + "</a>";
			html += "</li>";
		}
		
		html += "</ul>";

		System.out.println("<HTML>");
		System.out.println(html);
		System.out.println("</HTML>");
		
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
			System.out.println("IS TEMATRES SEARCH: " + JSONterm.isNull("term_id"));
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
		System.out.println("id: " + id);
		JSONObject JSONterm = (JSONObject)result.get(id);
		setTematresTerm(JSONterm, term);

		args = "task=fetchRelated&arg=" + term.getId();
		data = getResponseData(args);
		result = data.getJSONObject("result");

		for(String relatedId : result.keySet()){
			System.out.println("id: " + relatedId);
			JSONterm = (JSONObject)result.get(relatedId);
			TematresTerm newTerm = new TematresTerm();
			
			setTematresTerm(JSONterm, newTerm);			
			
			names.add(newTerm.getName());
		}
		
		return names;
	}

}
