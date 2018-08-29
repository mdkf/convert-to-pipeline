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

package org.jenkinsci.convertpipeline.transformers;

import hudson.model.Descriptor;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import static com.infostretch.labs.utils.TransformerUtil.listToSteps;


/**
 * PublisherTransformer handles the conversion of publishers (post build actions)
 * in a FreeStyle job to a pipeline Job.
 *
 * @author Michael DK Fowler
 */

class PublisherTransformer {

    private final Transformer transformer;

    protected PublisherTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    protected void convertPublishers() {
        DescribableList<Publisher,Descriptor<Publisher>> publishers=transformer.job.getPublishersList();
        if (publishers.size()>0) {
            transformer.publishSteps.append("\n\t\tstage ('Post build actions') {");
            transformer.publishSteps.append("\n\t\t\tsteps {\n");
            transformer.publishSteps.append(listToSteps(publishers, transformer));
            transformer.publishSteps.append("\n\t\t\t}"); //close steps
            transformer.publishSteps.append("\n\t\t}"); //close stage
        }
    }
}
