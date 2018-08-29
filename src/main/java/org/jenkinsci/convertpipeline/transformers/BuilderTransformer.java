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

import static com.infostretch.labs.utils.TransformerUtil.doIt;
import hudson.model.JDK;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import java.util.List;
import static com.infostretch.labs.utils.TransformerUtil.listToSteps;

/**
 * BuilderTransformer handles the conversion of build wrappers and build steps
 * in a FreeStyle job to a pipeline Job .
 *
 * @author Michael DK Fowler
 */

class BuilderTransformer {
    private final Transformer transformer;

    protected BuilderTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    protected void convertBuilders() {
        List<Builder> builders=transformer.job.getBuilders();
        List<BuildWrapper> buildWrappers=transformer.job.getBuildWrappersList();
        JDK jdk=transformer.job.getJDK();
        transformer.buildSteps = new StringBuilder();
        if (builders.size()> 0) {
            transformer.buildSteps.append("\n\t\tstage ('Build') {\n\t\t\t");
        }
        if (jdk!=null){
            transformer.buildSteps.append("//Check JDK Path"); //FIXME
        }
        for(BuildWrapper bw:buildWrappers){
            String result=doIt(bw, transformer);
            if (result!=null){
                transformer.buildSteps.append(result);
            }
        }
        transformer.buildSteps.append("\n\t\t\tsteps {\n");
        transformer.buildSteps.append(listToSteps(builders, transformer));
        transformer.buildSteps.append("\n\t\t\t}");
        if (jdk!=null){
            transformer.buildSteps.append(" \n\t}\n}");
        } else {
            transformer.buildSteps.append(" \n\t}");
        }
    }
}
