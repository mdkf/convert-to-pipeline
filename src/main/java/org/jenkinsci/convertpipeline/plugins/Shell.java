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

import static com.infostretch.labs.utils.TransformerUtil.callSnippetizer;
import org.jenkinsci.convertpipeline.transformers.Transformer;
import org.jenkinsci.plugins.workflow.steps.durable_task.ShellStep;

/**
 *
 * @author Michael DK Fowler
 */
public class Shell extends Plugins {

    private final hudson.tasks.Shell builder;

    public Shell(Transformer transformer, hudson.tasks.Shell builder) {
        super(transformer, builder);
        this.builder = builder;
    }

    @Override
    public String transform() {
        ShellStep step = new ShellStep(builder.getCommand());
        Integer unstableReturnValue = builder.getUnstableReturn();
        if (unstableReturnValue != null && !unstableReturnValue.equals(0)) {
            step.setReturnStatus(true);
            return "def returnValue = " + callSnippetizer(step) + "\n if(returnValue==" + unstableReturnValue + ") {\n\t currentBuild.result='UNSTABLE'\n}";
        } else {
            return callSnippetizer(step);
        }
    }
}
