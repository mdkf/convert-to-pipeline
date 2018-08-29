/*******************************************************************************
 * Copyright 2018 Michael DK Fowler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR  * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT  * OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE
 *
 * You should have received a copy of the GNU General Public License along with 
 * this program in the name of LICENSE.txt in the root folder of the distribution.
 * If not, see https://opensource.org/licenses/gpl-3.0.html
 *
 *******************************************************************************/
package org.jenkinsci.convertpipeline.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;
import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.jenkins_ci.plugins.run_condition.common.BaseDirectory;
import org.jenkins_ci.plugins.run_condition.logic.*;
import org.jenkinsci.convertpipeline.transformers.Transformer;
import static com.infostretch.labs.utils.TransformerUtil.listToSteps;


/**
 *
 * @author Michael DK Fowler
 */
public class ConditionalBuilder extends Plugins {

    private final StringBuilder statement = new StringBuilder();
    protected org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder builder;

    public ConditionalBuilder(Transformer transformer, org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder builder) {
        super(transformer, builder);
        this.builder=builder;
    }

    @Override
    public String transform() {
        statement.append("\t\t\t}\n"); //close out previous steps
        statement.append("\t\t}"); //close out previous stage
        int c= transformer.getNumConditionals();
        statement.append("\n\t\tstage('ConditionaBuildStep"+c+"') {\n\t\t\t");
        transformer.incrementNumConditionals();
        List<hudson.tasks.BuildStep> builders = builder.getConditionalbuilders();
        RunCondition condition = builder.getRunCondition();
        
        statement.append("when {\n\t\t\t\t");
        appendCondition(condition);
        statement.append("\t\t\t}");
        statement.append("\n\t\t\tsteps {\n");

        String steps = listToSteps(builders, transformer);
        if (steps.length() > 0) {
            statement.append(steps + "\n");
        }
        return statement.toString();
    }
    private void appendCondition(RunCondition condition) {
        if (condition instanceof org.jenkins_ci.plugins.run_condition.logic.Not) {
            appendNotCondition((org.jenkins_ci.plugins.run_condition.logic.Not) condition);
        }
        if (condition instanceof org.jenkins_ci.plugins.run_condition.logic.And) {
            appendAndCondition((org.jenkins_ci.plugins.run_condition.logic.And) condition);
        }
        if (condition instanceof org.jenkins_ci.plugins.run_condition.logic.Or) {
            appendOrCondition((org.jenkins_ci.plugins.run_condition.logic.Or) condition);
        }
        if (condition instanceof org.jenkins_ci.plugins.run_condition.core.AlwaysRun) {
            appendAlwaysCondition((org.jenkins_ci.plugins.run_condition.core.AlwaysRun) condition);
        }
        if (condition instanceof org.jenkins_ci.plugins.run_condition.core.BooleanCondition) {
            appendBooleanCondition((org.jenkins_ci.plugins.run_condition.core.BooleanCondition) condition);
        }
        if (condition instanceof org.jenkins_ci.plugins.run_condition.core.CauseCondition) {
            appendCauseCondition((org.jenkins_ci.plugins.run_condition.core.CauseCondition) condition);
        }
        if (condition instanceof org.jenkins_ci.plugins.run_condition.core.DayCondition) {
            appendDayCondition((org.jenkins_ci.plugins.run_condition.core.DayCondition) condition);
        }
        if (condition instanceof org.jenkins_ci.plugins.run_condition.core.ExpressionCondition) {
            appendExpressionCondition((org.jenkins_ci.plugins.run_condition.core.ExpressionCondition) condition);
        }
        if (condition instanceof org.jenkins_ci.plugins.run_condition.core.FileExistsCondition) {
            appendFileExistsCondition((org.jenkins_ci.plugins.run_condition.core.FileExistsCondition) condition);
        }
        if (condition instanceof org.jenkins_ci.plugins.run_condition.core.FilesMatchCondition) {
            appendFilesMatchCondition((org.jenkins_ci.plugins.run_condition.core.FilesMatchCondition) condition);
        }
        if (condition instanceof org.jenkins_ci.plugins.run_condition.core.NeverRun) {
            appendNeverCondition((org.jenkins_ci.plugins.run_condition.core.NeverRun) condition);
        }
        if (condition instanceof org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition) {
            appendStringsCondition((org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition) condition);
        }
        if (condition instanceof org.jenkins_ci.plugins.run_condition.core.NodeCondition) {
            appendNodeCondition((org.jenkins_ci.plugins.run_condition.core.NodeCondition) condition);
        }
        if (condition instanceof org.jenkins_ci.plugins.run_condition.core.NumericalComparisonCondition) {
            appendNumericalCondition((org.jenkins_ci.plugins.run_condition.core.NumericalComparisonCondition) condition);
        }
        if (condition instanceof org.jenkins_ci.plugins.run_condition.core.StatusCondition) {
            appendStatusCondition((org.jenkins_ci.plugins.run_condition.core.StatusCondition) condition);
        }
        if (condition instanceof org.jenkins_ci.plugins.run_condition.core.TimeCondition) {
            appendTimeCondition((org.jenkins_ci.plugins.run_condition.core.TimeCondition) condition);
        }
    }

    private void appendNotCondition(Not condition) {
        statement.append("not {\n\t\t\t\t\t");
        //get the nested condition
        RunCondition innerCondition = condition.getCondition();
        appendCondition(innerCondition);
        statement.append("\t\t\t\t}\n");
    }

    private void appendAndCondition(And condition) {
        ArrayList<ConditionContainer> ConditionContainers = condition.getConditions();
        statement.append("allOf {\n\t\t\t\t\t");
        appendCondition(ConditionContainers.get(0).getCondition());
        for (int c = 1; c < ConditionContainers.size(); c++) {
            statement.append("\n\t");
            appendCondition(ConditionContainers.get(c).getCondition());
        }
        statement.append("\t\t\t\t}\n");
    }

    private void appendOrCondition(Or condition) {
        ArrayList<ConditionContainer> ConditionContainers = condition.getConditions();
        statement.append("anyOf {\n\t\t\t\t\t");
        appendCondition(ConditionContainers.get(0).getCondition());
        for (int c = 1; c < ConditionContainers.size(); c++) {
            statement.append("\n\t");
            appendCondition(ConditionContainers.get(c).getCondition());
        }
        statement.append("\t\t\t\t}\n");
    }

    private void appendAlwaysCondition(org.jenkins_ci.plugins.run_condition.core.AlwaysRun condition) {
        statement.append("expression {\n\t\t\t\t\treturn true\n\t\t\t\t}\n");
    }

    private void appendBooleanCondition(org.jenkins_ci.plugins.run_condition.core.BooleanCondition condition) {
        statement.append("expression{\n\t\t\t\t\t");
        statement.append(fixVariableNotation(condition.getToken()));
        statement.append(" ==~ /(?i)(1|Y|YES|T|TRUE|ON|RUN)/\n\t\t\t\t}\n");
    }

    private void appendExpressionCondition(org.jenkins_ci.plugins.run_condition.core.ExpressionCondition condition) {
        statement.append("expression{\n\t\t\t\t\t");
        statement.append(condition.getLabel());
        statement.append("' =~ '");
        statement.append(condition.getExpression());
        statement.append("\n\t\t\t\t}\n");
    }
    
    private void appendFileExistsCondition(org.jenkins_ci.plugins.run_condition.core.FileExistsCondition condition) {
        if (condition.getBaseDir().getClass().equals(BaseDirectory.Workspace.class)) {
            statement.append("fileExists(\"");
            statement.append(condition.getFile());
            statement.append("\"\n");
        } else {
            statement.append("// cannot handle file exists outside of workspace condition");
        }
    }

    private void appendNeverCondition(org.jenkins_ci.plugins.run_condition.core.NeverRun condition) {
        statement.append("expression {\n\t\t\t\t\treturn false\n\\t\t\tt}");
    }
    
    private void appendNodeCondition(org.jenkins_ci.plugins.run_condition.core.NodeCondition condition) {
        statement.append("expression {\t\t\t\t${NODE_NAME} ==~ /(");
        List<String> allowedNodes = condition.getAllowedNodes();
        statement.append(allowedNodes.get(0));
        for (int c = 1; c < allowedNodes.size(); c++) {
            statement.append("|");
            statement.append(allowedNodes.get(c));
        }
        statement.append(")/\n\t\t\t}");
    }
    
    private void appendNumericalCondition(org.jenkins_ci.plugins.run_condition.core.NumericalComparisonCondition condition) {
        org.jenkins_ci.plugins.run_condition.core.NumericalComparisonCondition.Comparator c = condition.getComparator();
        String compartorType = c.getClass().getSimpleName();
        statement.append("expression {\n\t\t\t\t\t" + condition.getLhs());
        switch (compartorType) {
            case "LessThan":
                statement.append(" < ");
                break;
            case "GreaterThan":
                statement.append(" > ");
                break;
            case "EqualTo":
                statement.append(" == ");
                break;
            case "NotEqualTo":
                statement.append(" != ");
                break;
            case "LessThanOrEqualTo":
                statement.append(" <= ");
                break;
            case "GreaterThanOrEqualTo":
                statement.append(" >= ");
                break;
            default:
                statement.append("unknown comparator");
                break;
        }
        statement.append(condition.getRhs() + "\n\t\t\t\t}");
    }

    private void appendStatusCondition(org.jenkins_ci.plugins.run_condition.core.StatusCondition condition){
        statement.append("expression {\n\t\t\t\t\t currentbuild.resultIsBettterOrEqualTo(");
        statement.append(condition.getWorstResult().toString());
        statement.append(")\n\t\t\t\t}");
        statement.append("expression {\n\t\t\t\t\t currentbuild.resultIsWorseOrEqualTo(");
        statement.append(condition.getBestResult().toString());
        statement.append(")\n\t\t\t\t}");
    }
    
    private void appendStringsCondition(org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition condition) {
        
        statement.append("equals expected: ");
        statement.append(fixVariableNotation(condition.getArg1()));
        statement.append(", actual: ");
        statement.append(fixVariableNotation(condition.getArg2())+"\n");
    }

    private String fixVariableNotation(String s) {
        Pattern pattern=compile("^\\$\\{.+\\}$");
        Matcher matcher = pattern.matcher(s);
        if(matcher.matches()){
            StringBuilder sb=this.transformer.replaceParameters(new StringBuilder(s));
            return sb.toString().replace("${", "").replace("}", "");
        }
        else{
            return ("\""+s+"\"");
        }
    }

    private void appendCauseCondition(org.jenkins_ci.plugins.run_condition.core.CauseCondition condition) {
        statement.append("// type " + condition.getClass().getName() + " is not handled by the converter! \n");
    }

    private void appendDayCondition(org.jenkins_ci.plugins.run_condition.core.DayCondition condition) {
        statement.append("// type " + condition.getClass().getName() + " is not handled by the converter! \n");
    }

    private void appendFilesMatchCondition(org.jenkins_ci.plugins.run_condition.core.FilesMatchCondition condition) {
        statement.append("// type " + condition.getClass().getName() + " is not handled by the converter! \n");
    }

    private void appendTimeCondition(org.jenkins_ci.plugins.run_condition.core.TimeCondition condition){
        statement.append("// type " + condition.getClass().getName() + " is not handled by the converter! \n");
    }
}
