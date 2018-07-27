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

package com.infostretch.labs.transformers;

import com.infostretch.labs.utils.TransformerUtil;
import static com.infostretch.labs.utils.TransformerUtil.doIt;
import hudson.model.Descriptor;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * PublisherTransformer handles the conversion of publishers (post build actions) in
 * FreeStyle job configuration to pipeline Job configuration.
 *
 * @author Mohit Dharamshi
 */

public class PublisherTransformer {

    private final Transformer transformer;

    /**
     * This boolean value defines if any steps are added to publish steps.
     * Some publishers are added to build steps to avoid creating unnecessary stages. e.g. TestNG steps are added to buildSteps instead of publishers.
     * Mailer plugin on the other hand for example, will set this to false as it adds its publish actions to publishSteps.
     * To ensure a new plugin whose transformations needed to be added to publish steps are added to the script;
     * set onlyBuildTrigger to false in that plugin's transformPublisher method.
     */


    /**
     * Initialise local Transformer variable. This constructor is called in main Transformer class.
     *
     * @param transformer Transformer object to be assigned to local Transformer variable.
     */
    protected PublisherTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Converts all node steps in FreeStyle job.
     *
     * Some cases below may be added to transformer.buildSteps instead of transformer.publishSteps.
     * This is to reduce unnecessary stages.
     * Such cases will have onlyBuildTrigger NOT being set to false.
     */
    protected void convertPublishers() {
        DescribableList<Publisher,Descriptor<Publisher>> publishers=transformer.job.getPublishersList();
        if (publishers.size()>0) {
            transformer.setOnlyBuildTrigger(true);
            transformer.publishSteps.append("\n\tstage ('"+transformer.currentJobName+" - Post build actions') {");
            transformer.publishSteps.append("\n/*\nPlease note this is a direct conversion of post-build actions. \nIt may not necessarily work/behave in the same way as post-build actions work.\nA logic review is suggested.\n*/");
            for (Publisher p:publishers){
                String result = doIt(p, transformer);
                if (result!=null){
                    transformer.publishSteps.append(result);
                }
            }
            transformer.publishSteps.append(" \n\t}");
        }
    }
}
