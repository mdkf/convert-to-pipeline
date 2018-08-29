/*******************************************************************************
 * Copyright 2017 Infostretch Corporation
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 * OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE
 *
 * You should have received a copy of the GNU General Public License along with this program in the name of LICENSE.txt in the root folder of the distribution. If not, see https://opensource.org/licenses/gpl-3.0.html
 *
 *
 * For any inquiry or need additional information, please contact labs_support@infostretch.com
 *******************************************************************************/

package org.jenkinsci.convertpipeline.plugins;

import com.infostretch.labs.utils.PluginClass;
import static com.infostretch.labs.utils.PluginClass.searchByTag;
import com.infostretch.labs.utils.PluginIgnoredClass;
import static com.infostretch.labs.utils.PluginIgnoredClass.searchByValue;
import hudson.tasks.Builder;
import java.io.File;
import static java.lang.Class.forName;
import static org.apache.commons.text.WordUtils.capitalize;
import org.jenkinsci.convertpipeline.transformers.Transformer;


/**
 * Super class for plugins. This class is extended by every plugin whose transformation support is to be added.
 * Common functionalities that can be used by all plugins for transformation are also included here.
 *
 * @author Mohit Dharamshi
 */
public abstract class Plugins {

    /**
     * Transformer instance whose variables need to be read and written to.
     */
    protected Transformer transformer;

    //protected Builder builder;

    /**
     * Parameterized constructor. This will be invoked by super() in each plugin class.
     * This constructor is implemented in plugin classes that will be performing transformations.
     *
     * @param transformer Transformer instance whose variables need to be read and written to.
     * @param builder
     */

    public Plugins(Transformer transformer, Builder builder) {
        this.transformer = transformer;
        //this.builder = builder;
    }

    /**
     * Default constructor. This is implemented in plugin classes that do not need transformer and node variables for particular methods.
     * Such methods will handle non-transformation actions such as pushJenkinsfile.
     */
    public Plugins() {
    }

    /**
     * Method to be overridden by subclass when transforming build steps.
     * @return 
     */
    public String transform() {
        return null;
    }

    /**
     * Method to be overridden by subclass when pushing Jenkinsfile.
     * Clone the repo, add Jenkinsfile and push to SCM.
     *
     * @param workSpace The temporary workspace where the repo will be cloned.
     * @param script Groovy script to be written in Jenkinsfile.
     * @param url URL of SCM repository where Jenkinsfile is to be pushed.
     * @param branchName Branch of SCM where Jenkinsfile is to be pushed.
     * @param commitMessage Commit message to be included in commit when pushing Jenkinsfile.
     * @param credentialsProvider Credentials to use for clone and push operations.
     */
    //public void pushJenkinsfile(File workSpace, String script, String url, String branchName, String commitMessage, CredentialsProvider credentialsProvider){}

    /**
     * Helper method to append string to buildSteps in transformer.
     * @param string String to be appended to build steps.
     */
    /*
    protected final void appendBuildSteps(String string) {
        transformer.buildSteps.append(string);
    } */

    /**
     * Helper method to append string to publishSteps in transformer.
     * @param string String to be appended to publish steps.
     */
    /*
    protected final void appendPublishSteps(String string) {
        transformer.publishSteps.append(string);
    } */

    /**
     * Returns Class that matches nodeName.
     * The method checks if any node nodeNames need to be ignored.
     * For valid nodeNames; PluginClass enum is checked for known values. If found within PluginClass enum, the corresponding Class is derived.
     * If PluginClass enum does not have a valid entry; then the assumption is that the last word of the fully qualified class name in the nodeName matches a class that exists in com.infostretch.labs.plugins package.
     * If none of the above 2 scenarios suffice; then this plugin does not support the plugin that corresponds to the nodeName.
     *
     * @param nodeName The node name whose plugin class needs to be found.
     * @return Returns Class that matches nodeName.
     */
    public static final Class getPluginClass(String nodeName) {
        try {
            PluginIgnoredClass ignoredPlugin = searchByValue(nodeName);
            if(ignoredPlugin == null) {
                PluginClass pluginClass = searchByTag(nodeName);
                String pluginName = "";
                if(pluginClass == null) {
                    String[] splitNodeName = nodeName.split("\\.");
                    pluginName = "org.jenkinsci.convertpipeline.plugins." + capitalize(splitNodeName[splitNodeName.length-1]);
                } else {
                    pluginName = "org.jenkinsci.convertpipeline.plugins." + pluginClass.toString();
                }
                return forName(pluginName);
            }
        }
        catch (ClassNotFoundException e)
        {
        }
        return null;
    }

}
