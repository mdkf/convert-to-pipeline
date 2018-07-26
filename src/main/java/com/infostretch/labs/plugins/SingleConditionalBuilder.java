/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.infostretch.labs.plugins;

import com.infostretch.labs.transformers.Transformer;
import com.infostretch.labs.utils.TransformerUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Michael DK Fowler
 */
public class SingleConditionalBuilder extends Plugins {

    public SingleConditionalBuilder(Transformer transformer, Node node) {
        super(transformer, node);
    }

    @Override
    public void transform() {
        appendBuildSteps("\t\t// SingleConditionalBuilder build step\n");
        Element condition = getElementByTag("condition");
        appendBuildSteps("if(");
        appendBuildSteps(replaceCondition(condition));

        NodeList buildStep = ((Element) node).getElementsByTagName("buildStep");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < buildStep.getLength(); i = i + 1) {
            Node n = buildStep.item(i);
            n = n.getOwnerDocument().renameNode(n, null, ((Element) n).getAttribute("class"));
            ((Element) n).removeAttribute("class");

            String s = TransformerUtil.doIt(n, transformer);
            if (s != null) {   //s is set when the doIt method uses snippet generator instead of a plugin
                sb.append(s);
            }
        }
        if (sb.length() > 0) {
            String result = sb.toString().trim();
            appendBuildSteps("\t" + result + "\n");
        }
        appendBuildSteps("}");
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
                default:
                    statement.append("// type " + conditionType + " is not handled by the converter! \n");
                    break;
            }
        } else {
            String conditionType = fullConditionType.replace("org.jenkins_ci.plugins.run_condition.core.", "");

            switch (conditionType) {
                case "AlwaysRun":
                    statement.append("true) {\n");
                    break;
                case "BooleanCondition":
                    statement.append(" type " + conditionType + "\n");
                    break;
                case "StringsMatchCondition":
                    statement.append(" type " + conditionType + "\n");
                    break;
                case "NeverRun":
                    statement.append("false) {\n");
                    break;
                default:
                    statement.append("// type " + conditionType + " is not handled by the converter! \n");
                    break;
            }
        }
        return (statement.toString());
    }
}
