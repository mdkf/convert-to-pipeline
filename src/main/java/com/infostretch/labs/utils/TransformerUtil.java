/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.infostretch.labs.utils;

import com.infostretch.labs.transformers.Transformer;
import com.infostretch.labs.plugins.Plugins;
import com.infostretch.labs.transformers.BuilderTransformer;
import hudson.model.Items;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.workflow.steps.CoreStep;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author MFowler
 */
public class TransformerUtil {
    public static String doIt(NodeList list,Transformer transformer) {
        String result=null;
        for (int i = 1; i < list.getLength(); i = i + 2) {
            Node node = list.item(i);
            String nodeName=node.getNodeName();
            PluginIgnoredClass ignoredPlugin = PluginIgnoredClass.searchByValue(nodeName);
            if (ignoredPlugin != null){
                result="\n// Ignoring plugin "+nodeName;
            }
            else{
                try {
                    Class pluginClass = Plugins.getPluginClass(nodeName);
                    if (pluginClass != null) {
                        Constructor<Plugins> pluginConstructor = pluginClass.getConstructor(Transformer.class, Node.class);
                        Plugins plugin = pluginConstructor.newInstance(transformer, node);
                        plugin.transform();
                    } else {
                        result=makeSnippet(node);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static String makeSnippet(Node node) {
        String toReturn = "\n// Unable to convert a build step referring to \"" + node.getNodeName() + "\". Please verify and convert manually if required.";
        try {
            Object o = Items.XSTREAM2.fromXML(XMLUtil.nodeToString(node));
            if (o instanceof SimpleBuildStep) {
                CoreStep step = new CoreStep((SimpleBuildStep) o);
                String snippet = SnippetizerUtil.object2Groovy(new StringBuilder(), step, false);
                if (snippet != null) {
                    toReturn=("\n" + snippet + "\n");
                }
                else{
                    toReturn=("\n// empty snippet returned by "+o.toString()+"\n");
                }
            }
        } catch (UnsupportedOperationException ex) {
            Logger.getLogger(BuilderTransformer.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return toReturn;
    }
}
