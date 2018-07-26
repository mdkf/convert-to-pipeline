/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.infostretch.labs.plugins;

import com.infostretch.labs.transformers.Transformer;
import com.infostretch.labs.utils.TransformerUtil;
import static com.infostretch.labs.utils.TransformerUtil.doIt;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Michael DK Fowler
 */
public class ConditionalBuilder extends Plugins {

    public ConditionalBuilder(Transformer transformer, Node node) {
        super(transformer, node);
    }

    @Override
    public void transform() {
        appendBuildSteps("\t\t// ConditionalBuilder build step\n");
        Element condition = getElementByTag("runCondition");
        appendBuildSteps("if(");
        appendBuildSteps(replaceCondition(condition));
        appendBuildSteps("){\n\t");
        
        Node conditionalbuilders = ((Element) node).getElementsByTagName("conditionalbuilders").item(0);
        NodeList builders=conditionalbuilders.getChildNodes();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < builders.getLength(); i = i + 1) {
            Node n = builders.item(i);
            String s = doIt(n, transformer);
            if (s != null) {   //s is set when the doIt method uses snippet generator instead of a plugin
                sb.append(s);
            }
        }
        if (sb.length() > 0) {
            String result = sb.toString().trim();
            appendBuildSteps("\t" + result + "\n");
        }
        appendBuildSteps("}\n");
    }

    private String replaceCondition(Element condition) {
        String fullConditionType = condition.getAttribute("class");
        StringBuilder statement = new StringBuilder();
        if (fullConditionType.startsWith("org.jenkins_ci.plugins.run_condition.logic.")) {
            String conditionType = fullConditionType.replace("org.jenkins_ci.plugins.run_condition.logic.", "");
            switch (conditionType) {
                case "Not":
                    statement.append("!");
                    //get the nested condition
                    condition = (Element) condition.getElementsByTagName("condition").item(0);
                    statement.append(replaceCondition(condition));
                    break;
                case "And":
                    Element conditionRoot = (Element) condition.getElementsByTagName("conditions").item(0);
                    NodeList ConditionContainers=conditionRoot.getElementsByTagName("org.jenkins__ci.plugins.run__condition.logic.ConditionContainer");
                    Element nestedCondition = (Element) ((Element)ConditionContainers.item(0)).getElementsByTagName("condition").item(0);
                    statement.append(replaceCondition(nestedCondition));
                    for (int c =1; c<ConditionContainers.getLength(); c++){
                        statement.append(" && ");
                        nestedCondition = (Element) ((Element)ConditionContainers.item(c)).getElementsByTagName("condition").item(0);
                        statement.append(replaceCondition(nestedCondition));
                    }
                    //statement.append("){\n\t");
                    break;
                default:
                    statement.append("// type " + conditionType + " is not handled by the converter! \n");
                    break;
            }
        } else {
            String conditionType = fullConditionType.replace("org.jenkins_ci.plugins.run_condition.core.", "");

            switch (conditionType) {
                case "AlwaysRun":
                    statement.append("true");
                    break;
                case "BooleanCondition":
                    statement.append(condition.getElementsByTagName("token").item(0).getTextContent());
                    break;
                case "StringsMatchCondition":
                    statement.append("\"");
                    statement.append(condition.getElementsByTagName("arg1").item(0).getTextContent());
                    statement.append("\"==\"");
                    statement.append(condition.getElementsByTagName("arg2").item(0).getTextContent());
                    statement.append("\"");
                    break;
                case "NeverRun":
                    statement.append("false");
                    break;
                default:
                    statement.append("// type " + conditionType + " is not handled by the converter! \n");
                    break;
            }
        }
        return (statement.toString());
    }
}
