/*******************************************************************************
 * Copyright 2018 Michael DK Fowler
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
 *******************************************************************************/

package com.infostretch.labs.transformers;

import com.infostretch.labs.utils.TransformerUtil;
import static com.infostretch.labs.utils.TransformerUtil.doIt;
import hudson.model.JDK;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import java.util.List;

/**
 * BuilderTransformer handles the conversion of build wrappers and build steps
 * in FreeStyle job configuration to pipeline Job configuration.
 *
 * @author MDKF
 */

public class BuilderTransformer {
    private final Transformer transformer;

    /**
     * Initialize local Transformer variable. This constructor is called in main Transformer class.
     *
     * @param transformer Transformer object to be assigned to local Transformer variable.
     */
    protected BuilderTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Convert the FreeStyle job.
     */
    public void convertBuilders() {
        List<Builder> builders=transformer.job.getBuilders();
        List<BuildWrapper> buildWrappers=transformer.job.getBuildWrappersList();
        JDK jdk=transformer.job.getJDK();
        transformer.buildSteps = new StringBuffer();
        if (builders.size()> 0) {
            transformer.buildSteps.append("\n\tstage ('"+transformer.currentJobName+" - Build') {\n \t");
        }
        if (jdk!=null){
            transformer.buildSteps.append("\nwithEnv([\"JAVA_HOME=\"${ tool '"+jdk.getName()+"'}\"\n"+", \"PATH=${env.JAVA_HOME}/bin\"]) { \n");
        }
        for(BuildWrapper bw:buildWrappers){
            String result=doIt(bw, transformer);
            if (result!=null){
                transformer.buildSteps.append(result);
            }
        }
        for(Builder b:builders){
           String result=doIt(b, transformer);
            if (result!=null){
                transformer.buildSteps.append(result);
            }
        }
        if (jdk!=null){
            transformer.buildSteps.append(" \n\t}\n}");
        } else {
            transformer.buildSteps.append(" \n\t}");
        }
    }
}
