package org.ceskaexpedice.akubra.processingindex;

import org.apache.commons.lang3.tuple.Pair;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

// helper utility used for extracting structure information 
public final class StructureInfoDom4jUtils {
    
    public static final Logger LOGGER = Logger.getLogger(StructureInfoDom4jUtils.class.getName());
    
    private StructureInfoDom4jUtils() {}

    private static JSONObject pidAndRelationToJson(String pid, String relation) {
        JSONObject json = new JSONObject();
        json.put("pid", pid);
        json.put("relation", relation);
        return json;
    }

    public static JSONObject extractStructureInfo(AkubraRepository akubraRepository, String pid) {
        JSONObject structure = new JSONObject();
        //parents
        JSONObject parents = new JSONObject();

        Pair<ProcessingIndexRelation, List<ProcessingIndexRelation>> parentsTpls = ProcessingIndexUtils.getParents(pid, akubraRepository);
        if (parentsTpls.getLeft() != null) {
            parents.put("own", pidAndRelationToJson(parentsTpls.getLeft().getSource(), parentsTpls.getLeft().getRelation()));
        }
        JSONArray fosterParents = new JSONArray();
        for (ProcessingIndexRelation fosterParentTpl : parentsTpls.getRight()) {
            fosterParents.put(pidAndRelationToJson(fosterParentTpl.getSource(), fosterParentTpl.getRelation()));
        }
        parents.put("foster", fosterParents);
        structure.put("parents", parents);
        
        Document relsExt = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT).asDom4j(false);

        JSONObject children = new JSONObject();
        Pair<List<ProcessingIndexRelation>, List<ProcessingIndexRelation>> childrenTpls = ProcessingIndexUtils.getChildren(pid, akubraRepository);
        JSONArray ownChildren = new JSONArray();
        Map<String, JSONObject> mapping = new HashMap<>();
        
        for (ProcessingIndexRelation ownChildTpl : childrenTpls.getLeft()) {
            mapping.put(ownChildTpl.getTarget(), pidAndRelationToJson(ownChildTpl.getTarget(), ownChildTpl.getRelation()));
        }        

        exploreRelsExt(relsExt, (child)-> {
            Element ch = child;
            Namespace namespace = new Namespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            org.dom4j.QName qname = new org.dom4j.QName("resource", namespace);
            Attribute attribute = ch.attribute(qname);
            if (attribute != null) {
                String value = attribute.getValue();
                if (value.startsWith("info:fedora/uuid")) {
                    String extractedPid = value.substring("info:fedora/".length());
                    JSONObject jsonObject = mapping.get(extractedPid);
                    if (jsonObject != null) {
                        ownChildren.put(jsonObject);
                    }
                }
            }
        });

        List<String> devList = new ArrayList<>();
        for (int i = 0; i < ownChildren.length(); i++) { devList.add(ownChildren.get(i).toString()); }
        LOGGER.fine(String.format("Pids sorted by RELS-EXT %s %s", pid, devList));

        
        children.put("own", ownChildren);
        JSONArray fosterChildren = new JSONArray();
        for (ProcessingIndexRelation fosterChildTpl : childrenTpls.getRight()) {
            fosterChildren.put(pidAndRelationToJson(fosterChildTpl.getTarget(), fosterChildTpl.getRelation()));
        }
        
        structure.put("children", children);
        children.put("foster", fosterChildren);
        
        //model
        String model = ProcessingIndexUtils.getModel(pid, akubraRepository);
        structure.put("model", model);
    
        return structure;
    }

    private static void exploreRelsExt(Document relsExt, Consumer<Element> consumer) {
        Element rootElement = relsExt.getRootElement();
        Stack<Element> stack = new Stack<>();
        stack.push(rootElement);
        while(!stack.isEmpty()) {
            Element pop = stack.pop();
            List<Element> children = pop.elements();
            for (Element child : children) {
                consumer.accept(child);
                stack.push(child);
            }
        }
    }

    private static void sortRelations(Document relsExt, JSONObject ownRelations) {
        relsExt.selectNodes("");
    }
}

