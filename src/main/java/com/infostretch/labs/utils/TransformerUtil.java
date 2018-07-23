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
import hudson.tasks.CommandInterpreter;
import org.jenkinsci.plugins.workflow.steps.CoreStep;
import org.w3c.dom.Node;


import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jenkinsci.plugins.workflow.steps.durable_task.BatchScriptStep;
import org.jenkinsci.plugins.workflow.steps.durable_task.DurableTaskStep;
import org.jenkinsci.plugins.workflow.steps.durable_task.ShellStep;


/**
 *
 * @author Michael DK Fowler
 */
public class TransformerUtil {
    public static String doIt(Node node,Transformer transformer) {
        String result=null;
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
                e.printStackTrace(System.err);
            }
        }
        return result;
    }

    private static String makeSnippet(Node node) {
        Object o = Items.XSTREAM2.fromXML(XMLUtil.nodeToString(node));
        if (o instanceof SimpleBuildStep){
            CoreStep step = new CoreStep((SimpleBuildStep)o);
            return callSnippetizer(node, step);
        }
        else if (o instanceof hudson.tasks.BatchFile){
            hudson.tasks.BatchFile bf= (hudson.tasks.BatchFile)o;
            BatchScriptStep step=new BatchScriptStep(bf.getCommand());
            Integer unstableReturnValue=bf.getUnstableReturn();
            if (unstableReturnValue != null && !unstableReturnValue.equals(0)){
                step.setReturnStatus(true);
                return "def returnValue = "+callSnippetizer(node, step)+"\n if(returnValue=="+unstableReturnValue+") {\n\t currentBuild.result='UNSTABLE'\n}";
            }
            else{
                return callSnippetizer(node, step);
            }
        }
        else if (o instanceof hudson.tasks.Shell){
            hudson.tasks.Shell sh=(hudson.tasks.Shell)o;
            DurableTaskStep step=new ShellStep(sh.getCommand());
            Integer unstableReturnValue=sh.getUnstableReturn();
            if (unstableReturnValue != null && !unstableReturnValue.equals(0)){
                step.setReturnStatus(true);
                return "def returnValue = "+callSnippetizer(node, step)+"\n if(returnValue=="+unstableReturnValue+") {\n\t currentBuild.result='UNSTABLE'\n}";
            }
        }

        return "//"+ node.getNodeName()+" is of type "+o.getClass().getSuperclass()+" and will be skipped\n"; 
    }

    private static String callSnippetizer(Node node, org.jenkinsci.plugins.workflow.steps.Step o) {
        String toReturn="\n// Unable to convert a build step referring to \"" + node.getNodeName() + "\". Please verify and convert manually if required.";
        try {
            String snippet = SnippetizerUtil.object2Groovy(new StringBuilder(), o, false);
            if (snippet != null) {
                toReturn=(snippet + "\n");
            }
            else{
                toReturn=("\n// empty snippet returned by "+o.toString()+"\n");
            }
        }
        catch (UnsupportedOperationException ex) {
            Logger.getLogger(BuilderTransformer.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return toReturn;
    }
}
