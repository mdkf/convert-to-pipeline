/** *****************************************************************************
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
 ****************************************************************************** */
package org.jenkinsci.convertpipeline.transformers;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.FreeStyleProject;
import hudson.model.JobProperty;
import hudson.model.ParametersDefinitionProperty;
import java.io.IOException;
import java.util.*;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jenkins.model.Jenkins.getInstance;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;

/**
 * Transformer class is the main class that handles the conversion of FreeStyle
 * job configuration to pipeline Job configuration.
 *
 * @author Michael DK Fowler
 */
public class Transformer {

    protected StringBuilder buildSteps = new StringBuilder();
    protected StringBuilder publishSteps = new StringBuilder();
    protected FreeStyleProject job;
    private static final Logger logger = getLogger(Transformer.class.getName());
    private org.jenkinsci.plugins.workflow.job.WorkflowJob destJob;
    private StringBuilder script = new StringBuilder();
    private String currentJobName = "";
    private final Map<String, Object> requestParams;
    private String newJobName;
    private final List<String> parameters = new ArrayList<>();
    private Folder folder;
    private int numConditionals = 1;

    /**
     * Constructor to initialise variables required to process transformation.
     *
     * @param requestParams Map that contains request parameters.
     */
    public Transformer(Map requestParams) {
        script = new StringBuilder();
        this.requestParams = requestParams;
    }

    /**
     * Initialises transformation process of Freestyle project to Pipeline.
     *
     * @param name
     */
    public void performFreeStyleTransformation(String name) {
        newJobName = name;
        initializeConversion();
        transformJob((FreeStyleProject) requestParams.get("initialProject"));
        // (boolean) requestParams.get("commitJenkinsfile"), requestParams.get("commitMessage").toString()
        finalizeConversion();
        logger.info("Completed conversion of all jobs");
    }

    /**
     * @return the destJob
     */
    public org.jenkinsci.plugins.workflow.job.WorkflowJob getDestJob() {
        return destJob;
    }

    public StringBuilder replaceParameters(StringBuilder script) {
        for (String s : parameters) {
            String find = "${" + s + "}";
            String replace = "${params." + s + "}";
            replaceAll(script, find, replace);
        }
        return script;
    }

    public void performFreeStyleTransformation(String newName, Folder folder) {
        this.folder = folder;
        this.performFreeStyleTransformation(newName);
    }

    public int getNumConditionals() {
        return numConditionals;
    }

    public void incrementNumConditionals() {
        numConditionals++;
    }

    /**
     * General method to append script block to main script.
     *
     * @param block Script block to add to main script.
     */
    protected void appendToScript(String block) {
        script.append(block);
    }

    /**
     * Setter method to set SCM Type. Useful when committing Jenkinsfile to SCM.
     *
     * @param scmType SCM Type to set for committing Jenkinsfile to SCM.
     */
    /*
    protected void setScmType(String scmType) {
        this.scmType = scmType;
    }

    protected void setOnlyBuildTrigger(boolean value) {
        onlyBuildTrigger = value;
    }

    protected boolean getOnlyBuildTrigger() {
        return onlyBuildTrigger;
    }
    */
    protected void transformDocument() {
        try {
            if (folder != null) {
                destJob = folder.createProject(org.jenkinsci.plugins.workflow.job.WorkflowJob.class, newJobName);
            } else {
                destJob = getInstance().createProject(org.jenkinsci.plugins.workflow.job.WorkflowJob.class, newJobName);
            }
            transformFreestyleJob();
        } catch (IOException ex) {
            logger.severe("Error Creating job: " + ex.getMessage());
        }
    }

    private void initializeConversion() {
        appendToScript("pipeline {\n");
    }

    private void finalizeConversion() {
        appendToScript("\n}\n}");
        script = replaceParameters(script);
        appendScriptToXML();
        saveJob();
    }

    /**
     * Invokes conversion of given FreeStyle Job. This method is recursively
     * called if downstream jobs are to be converted also.
     *
     * @param item FreeStyle job to convert.
     * @param downStream Boolean to decide if item's downstream jobs are to be
     * converted.
     */
    private void transformJob(FreeStyleProject item) {
        currentJobName = item.getFullName();
        this.job = item;
        transformDocument();
    }

    /**
     * Calls 'copy' or 'transformers' methods of various configurations of a
     * FreeStyle job.
     */
    private void transformFreestyleJob() {
        copyConfigurations();
        logger.info("Transforming Label");
        transformLabel();
        this.appendToScript("\tstages {\n\t\t");
        logger.info("Transforming SCM");
        SCMTransformer scmTransformer = new SCMTransformer(this);
        scmTransformer.convertSCM();
        logger.info("Transforming Builders");
        BuilderTransformer builderTransformer = new BuilderTransformer(this);
        builderTransformer.convertBuilders();
        logger.info("Transforming Publishers");
        PublisherTransformer publisherTransformer = new PublisherTransformer(this);
        publisherTransformer.convertPublishers();
        this.appendToScript(this.buildSteps.toString());
        if (this.publishSteps.length()>0) {
            this.appendToScript(this.publishSteps.toString());
        }
    }

    /**
     * Copies known configurations that are identical in pipeline jobs. Plugins
     * / configurations that cause job creation / build to fail are explicitely
     * to removed.
     *
     * @param configurations List of configurations to copy.
     */
    private void copyConfigurations() {
        try {
            getDestJob().setDescription(job.getDescription());
        } catch (IOException ex) {
            logger.severe("Error setting description");
            logger.log(SEVERE, ex.getMessage(), ex);
        }
        try {
            getDestJob().setTriggers(new ArrayList<>(job.getTriggers().values()));
        } catch (IOException ex) {
            logger.severe("Error adding triggers");
            logger.log(SEVERE, ex.getMessage(), ex);
        }
        List<String> propertiesToRemove = new ArrayList<>();
        propertiesToRemove.add("DiskUsageProperty");
        propertiesToRemove.add("hudson-plugins-promoted_builds-JobPropertyImpl");
        List<JobProperty<? super FreeStyleProject>> sourceProperties = job.getAllProperties();
        for (JobProperty<? super FreeStyleProject> property : sourceProperties) {
            try {
                if (!propertiesToRemove.contains(property.getDescriptor().getJsonSafeClassName())) {
                    getDestJob().addProperty(property);
                    if (property instanceof ParametersDefinitionProperty) {
                        parameters.addAll(((ParametersDefinitionProperty) property).getParameterDefinitionNames());
                    }
                } else {
                    /*                    if (property instanceof hudson.plugins.promoted_builds.JobPropertyImpl){
                        List<PromotionProcess> activePromotions = ((hudson.plugins.promoted_builds.JobPropertyImpl)property).getActiveItems();
                        for (PromotionProcess p : activePromotions){
                            p.
                        }
                    } */
                    appendToScript("\n// Discarded: " + property.getDescriptor().getDisplayName() + "\n");
                }
            } catch (IOException ex) {
                appendToScript("\n// Error addring: " + property.getDescriptor().getDisplayName());
                logger.severe("Error adding property " + property);
                logger.log(SEVERE, ex.getMessage(), ex);
            }
        }
        // flowDefinition.appendChild(destConfigNode); FIXME
    }

    /**
     * Transforms label to node block.
     */
    private void transformLabel() {
        String label = job.getAssignedLabelString();
        if (label != null) {
            appendToScript("\n\tagent {\n\t\tlabel\"" + label + "\"\n\t}");
        } else {
            appendToScript("\n\tagent any\n");
        }
    }

    /**
     * Add Groovy Script to XML strucuture. If commit to SCM is selected, script
     * is written to Jenkinsfile and committed to SCM.
     *
     * @param commitJenkinsfile Boolean to decide if script is to be kept inline
     * or committed as Jenkinsfile to SCM.
     * @param commitMessage Commit message if Jenkinsfile is to be committed to
     * SCM.
     */
    /* private void appendScriptToXML(boolean commitJenkinsfile, String commitMessage) {
        FlowDefinition fd;
        if (commitJenkinsfile) {
            fd = new CpsScmFlowDefinition(scm, "Jenkinsfile");
            new SCMUtil().pushJenkinsfile(script.toString(), scmURL, scmBranch, scmCredentialsId, commitMessage, scmType);
        } else {
            fd = new CpsFlowDefinition(script.toString(), true);
        }
        destJob.setDefinition(fd);
    } */
    
    private void appendScriptToXML(){
        FlowDefinition fd = new CpsFlowDefinition(script.toString(), true);
        destJob.setDefinition(fd);
    }

    /**
     * Write complete transformed configuration to input stream object.
     */
    private void saveJob() {
        try {
            getDestJob().save();
            logger.info("Transformation for job " + currentJobName + " completed successfully");
        } catch (IOException e) {
            logger.severe("Exception occurred: " + e.getMessage());
        }
    }

    private static void replaceAll(StringBuilder builder, String from, String to) {
        int index = builder.indexOf(from);
        while (index != -1) {
            builder.replace(index, index + from.length(), to);
            index += to.length(); // Move to the end of the replacement
            index = builder.indexOf(from, index);
        }
    }

    /**
     * General method to append script block with a stage wrap.
     *
     * @param stage Name of stage to create.
     * @param block Script block to add under stage.
     */
    /* private void appendToScript(String stage, String block) {
        script.append("\n\tstage ('" + stage + "') {\n \t " + block + " \n\t}");
    } */
}
