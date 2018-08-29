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

import hudson.tasks.BuildStep;
import java.util.ArrayList;
import java.util.List;
import org.jenkinsci.convertpipeline.transformers.Transformer;
import org.jenkinsci.plugins.conditionalbuildstep.ConditionalBuilder;

/**
 *
 * @author Michael DK Fowler
 */
public class SingleConditionalBuilder extends Plugins {
    private final org.jenkinsci.convertpipeline.plugins.ConditionalBuilder plugin;

    public SingleConditionalBuilder(Transformer transformer, org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder builder) {
        List<BuildStep> steps=new ArrayList<>();
        steps.add(builder.getBuildStep());
        ConditionalBuilder cb= new ConditionalBuilder(builder.getCondition(),builder.getRunner(),steps);
        plugin = new org.jenkinsci.convertpipeline.plugins.ConditionalBuilder(transformer, cb);
    }
    @Override
    public String transform() {
        return plugin.transform();
    }
}