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
import hudson.scm.SCM;

/**
 * SCMTransformer handles the conversion of SCMs in
 * FreeStyle job configuration to pipeline Job configuration.
 *
 * @author Michael DK Fowler
 */

class SCMTransformer {
    
    private final Transformer transformer;
    //private Node scm;
    private SCM scm;

    protected SCMTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Calls respective convert SCM methods to convert FreeStyle Job SCM Configurations.
     */
    protected void convertSCM() {
        scm=transformer.job.getScm();
        if (getScm() != null){
            String result=doIt(scm, transformer);
            transformer.appendToScript("stage('Checkout'){\n\t\t\tsteps {\n");
            if (result!=null){
                transformer.appendToScript("\t\t\t\t"+result);
            }
            else{
                transformer.appendToScript("// Unable to convert SCM referring to \"" + getScm().getType() + "\". Please verify and convert manually if required.");
            }
            transformer.appendToScript("\n\t\t\t}\n\t\t}");
        }
    }

    /**
     * @return the scm
     */
    protected SCM getScm() {
        return scm;
    }
}
