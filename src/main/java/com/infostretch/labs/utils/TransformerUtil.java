/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.infostretch.labs.utils;

import com.infostretch.labs.plugins.Plugins;
import static com.infostretch.labs.plugins.Plugins.getPluginClass;
import com.infostretch.labs.transformers.BuilderTransformer;
import com.infostretch.labs.transformers.Transformer;
import static com.infostretch.labs.utils.PluginIgnoredClass.searchByValue;
import static com.infostretch.labs.utils.XMLUtil.nodeToString;
import hudson.model.Items;
import static hudson.model.Items.XSTREAM2;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.workflow.cps.Snippetizer;
import org.jenkinsci.plugins.workflow.steps.CoreStep;
import org.jenkinsci.plugins.workflow.steps.durable_task.BatchScriptStep;
import org.jenkinsci.plugins.workflow.steps.durable_task.DurableTaskStep;
import org.jenkinsci.plugins.workflow.steps.durable_task.ShellStep;
import org.w3c.dom.Node;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

/**
 *
 * @author Michael DK Fowler
 */
public class TransformerUtil {
    public static String doIt(Object o,Transformer transformer) {
        String result=null;
        String nodeName=o.getClass().getSimpleName();
        PluginIgnoredClass ignoredPlugin = PluginIgnoredClass.searchByValue(nodeName);
        if (ignoredPlugin != null){
            result="\n// Ignoring plugin "+nodeName;
        }
        else{
            try {
                Class pluginClass = Plugins.getPluginClass(nodeName);
                if (pluginClass != null) {
                    Constructor<Plugins> pluginConstructor = pluginClass.getConstructor(Transformer.class, Node.class);
                    Plugins plugin = pluginConstructor.newInstance(transformer, o);
                    plugin.transform();
                } else {
                    result=makeSnippet(o);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException | ArrayIndexOutOfBoundsException e) {
                int c=1;
            }
        }
        return result;
    }
    public static String transformList(Iterable<?> elements, Transformer transformer){
        StringBuilder sb=new StringBuilder();
        for (Object o:elements){
            String result = TransformerUtil.doIt(o, transformer);
            if (result!=null){
                sb.append(result);
            }
        }
        return sb.toString();
    }

    private static String makeSnippet(Node node) {
        try {
            Object o = Items.XSTREAM2.fromXML(XMLUtil.nodeToString(node));
            return makeSnippet(o);
        }
        catch (Exception e){
            return "\n// Error in xml: "+node.toString()+"\n";
        }
    }

    private static String callSnippetizer(org.jenkinsci.plugins.workflow.steps.Step o) {
        String toReturn="\n// Unable to convert a build step referring to \"" + o.getClass().getName() + "\". Please verify and convert manually if required.\n";
        try {
            String snippet = Snippetizer.object2Groovy(o);
            if (snippet != null && snippet.length()>0) {
                toReturn=(snippet + "\n");
            }
            else{
                toReturn=("\n// empty snippet returned by "+o.toString()+"\n");
            }
        }
        catch (UnsupportedOperationException ex) {
            getLogger(BuilderTransformer.class.getName()).log(WARNING, ex.getMessage());
        }
        return toReturn;
    }
    private static String makeSnippet(Object o){
         if (o instanceof SimpleBuildStep){
            CoreStep step = new CoreStep((SimpleBuildStep)o);
            return callSnippetizer(step);
        }
        else if (o instanceof hudson.tasks.BatchFile){
            hudson.tasks.BatchFile bf= (hudson.tasks.BatchFile)o;
            BatchScriptStep step=new BatchScriptStep(bf.getCommand());
            Integer unstableReturnValue=bf.getUnstableReturn();
            if (unstableReturnValue != null && !unstableReturnValue.equals(0)){
                step.setReturnStatus(true);
                return "def returnValue = "+callSnippetizer(step)+"\n if(returnValue=="+unstableReturnValue+") {\n\t currentBuild.result='UNSTABLE'\n}";
            }
            else{
                return callSnippetizer(step);
            }
        }
        else if (o instanceof hudson.tasks.Shell){
            hudson.tasks.Shell sh=(hudson.tasks.Shell)o;
            ShellStep step=new ShellStep(sh.getCommand());
            Integer unstableReturnValue=sh.getUnstableReturn();
            if (unstableReturnValue != null && !unstableReturnValue.equals(0)){
                step.setReturnStatus(true);
                return "def returnValue = "+callSnippetizer(step)+"\n if(returnValue=="+unstableReturnValue+") {\n\t currentBuild.result='UNSTABLE'\n}";
            }
            else{
                return callSnippetizer(step);
            }
        }
         return ("\n// Unable to convert a build step referring to \"" + o.getClass().getName() + "\". Please verify and convert manually if required.\n");
    }
}
