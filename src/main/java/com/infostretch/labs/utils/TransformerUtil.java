/*******************************************************************************
 * Copyright 2018 Michael DK Fowler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR  * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT  * OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE
 *
 * You should have received a copy of the GNU General Public License along with 
 * this program in the name of LICENSE.txt in the root folder of the
 * distribution. If not, see https://opensource.org/licenses/gpl-3.0.html
 *
 ******************************************************************************/
package com.infostretch.labs.utils;

import static com.infostretch.labs.utils.PluginIgnoredClass.searchByValue;
import hudson.scm.SCM;
import hudson.tasks.CommandInterpreter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import jenkins.tasks.SimpleBuildStep;
import jenkins.tasks.SimpleBuildWrapper;
import org.jenkinsci.convertpipeline.plugins.Plugins;
import static org.jenkinsci.convertpipeline.plugins.Plugins.getPluginClass;
import org.jenkinsci.convertpipeline.transformers.Transformer;
import static org.jenkinsci.plugins.workflow.cps.Snippetizer.object2Groovy;
import org.jenkinsci.plugins.workflow.steps.CoreStep;
import org.jenkinsci.plugins.workflow.steps.CoreWrapperStep;
import org.jenkinsci.plugins.workflow.steps.durable_task.DurableTaskStep;
import org.jenkinsci.plugins.workflow.steps.durable_task.PowershellScriptStep;
import org.jenkinsci.plugins.workflow.steps.scm.GenericSCMStep;
import org.jenkinsci.plugins.workflow.steps.scm.SCMStep;

/**
 *
 * @author Michael DK Fowler
 */
public class TransformerUtil {

    public static String doIt(Object o, Transformer transformer) {
        String result;
        String nodeName = o.getClass().getSimpleName();
        PluginIgnoredClass ignoredPlugin = searchByValue(nodeName);
        if (ignoredPlugin != null) {
            result = "\n// Ignoring plugin " + nodeName;
        } else {
            try {
                Class pluginClass = getPluginClass(nodeName);
                if (pluginClass != null) {
                    Constructor<Plugins> pluginConstructor = pluginClass.getConstructor(Transformer.class, o.getClass());
                    Plugins plugin = pluginConstructor.newInstance(transformer, o);
                    result = plugin.transform();
                } else {
                    result = makeSnippet(o);
                }
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InstantiationException | SecurityException | InvocationTargetException | ArrayIndexOutOfBoundsException e) {
                return "\n// Unable to convert a build step referring to \"" + o.getClass().getName() + "\". Please verify and convert manually if required.\n";
            }
        }
        return result;
    }

    public static String listToSteps(Iterable<?> elements, Transformer transformer) {
        StringBuilder sb = new StringBuilder();
        for (Object o : elements) {
            String result = doIt(o, transformer);
            if (result != null) {
                sb.append("\t\t\t\t"+result);
            }
        }
        return sb.toString();
    }

    public static String callSnippetizer(Object o) {
        String toReturn = "\n// Unable to convert a build step referring to \"" + o.getClass().getName() + "\". Please convert this step manually.\n";
        try {
            String snippet = object2Groovy(o);
            if (snippet != null && snippet.length() > 0) {
                toReturn = (snippet + "\n");
            } else {
                toReturn = ("\n// empty snippet returned by " + o.toString() + "\n");
            }
        } catch (UnsupportedOperationException ex) {
            getLogger(TransformerUtil.class.getName()).log(WARNING, ex.getMessage());
        }
        return toReturn;
    }

    private static String makeSnippet(Object o) {
        if (o instanceof SimpleBuildStep) {
            CoreStep step = new CoreStep((SimpleBuildStep) o);
            return callSnippetizer(step);
        } else if (o instanceof hudson.scm.SCM) {
            SCMStep step = new GenericSCMStep((SCM) o);
            return callSnippetizer(step);
        } else if (o instanceof SimpleBuildWrapper) {
            CoreWrapperStep step = new CoreWrapperStep((SimpleBuildWrapper) o);
            return callSnippetizer(step);
        } else if (o instanceof hudson.plugins.powershell.PowerShell) {
            PowershellScriptStep step = new PowershellScriptStep(((CommandInterpreter) o).getCommand());
            return callSnippetizer(step);
        } else if (o instanceof org.jenkinsci.plugins.managedscripts.WinBatchBuildStep) {
            DurableTaskStep step = new org.jenkinsci.plugins.managedscripts.ManagedBatchScriptStep((org.jenkinsci.plugins.managedscripts.WinBatchBuildStep) o);
            return callSnippetizer(step);
        } else if (o instanceof org.jenkinsci.plugins.managedscripts.PowerShellBuildStep) {
            DurableTaskStep step = new org.jenkinsci.plugins.managedscripts.ManagedPowerShellScriptStep((org.jenkinsci.plugins.managedscripts.PowerShellBuildStep) o);
            return callSnippetizer(step);
        }
        return ("\n// Snippitizer is unable to convert a build step referring to \"" + o.getClass().getName() + "\". Please convert this step manually.\n");
    }
}
