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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * BuilderTransformer handles the conversion of build steps in
 * FreeStyle job configuration to pipeline Job configuration.
 *
 * @author Mohit Dharamshi
 */

public class BuilderTransformer {
    private Transformer transformer;

    /**
     * Initialise local Transformer variable. This constructor is called in main Transformer class.
     *
     * @param transformer Transformer object to be assigned to local Transformer variable.
     */
    protected BuilderTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Converts all build steps in FreeStyle job.
     */
    public void convertBuilders() {

        if (transformer.doc.getElementsByTagName("builders").getLength() > 0) {
            Element builders = (Element) transformer.doc.getElementsByTagName("builders").item(0);
            transformer.buildersList = builders.getChildNodes();
            transformer.buildSteps = new StringBuffer();
            if (transformer.buildersList.getLength() > 0) {
                transformer.buildSteps.append("\n\tstage ('"+transformer.currentJobName+" - Build') {\n \t");
            }
            transformer.jdk = (Element) transformer.doc.getElementsByTagName("jdk").item(0);
            if (transformer.jdk != null && !transformer.jdk.getTextContent().equals("(System)")) {
                transformer.buildSteps.append("\nwithEnv([\"JAVA_HOME=${ tool '\"+JDK+\"' }\", \"PATH=${env.JAVA_HOME}/bin\"]) { \n");
            }
            Element buildWrappers = (Element) transformer.doc.getElementsByTagName("buildWrappers").item(0);
            if (buildWrappers != null) {
                NodeList buildWrappersList = buildWrappers.getChildNodes();
                String result=TransformerUtil.doIt(buildWrappersList, transformer);
                if (result!=null){
                    transformer.buildSteps.append(result);
                }
            }
            String result=TransformerUtil.doIt(transformer.buildersList, transformer);
            if (result!=null){
                transformer.buildSteps.append(result);
            }
        }
    }
}
