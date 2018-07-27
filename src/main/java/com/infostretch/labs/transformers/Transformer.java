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

import com.infostretch.labs.utils.SCMUtil;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.JobProperty;
import hudson.scm.SCM;
import hudson.triggers.Trigger;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jenkins.model.Jenkins;
//import javax.xml.parsers.ParserConfigurationException;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
/**
 * Transformer class is the main class that handles the conversion of
 * FreeStyle job configuration to pipeline Job configuration.
 *
 * @author Mohit Dharamshi
 */

public class Transformer {
    private static final Logger logger = getLogger(Transformer.class.getName());

    //private InputStream is;
    //private Element flowDefinition;
    private String scmURL ="", scmCredentialsId = "", scmType = "", scmBranch = "";
    //protected Document doc;
   // protected Document dest;
    private org.jenkinsci.plugins.workflow.job.WorkflowJob destJob;
    //public Element jdk;
    //protected NodeList buildersList;
    public boolean firstJob = true;
    public StringBuffer script=new StringBuffer(), buildSteps=new StringBuffer(), publishSteps=new StringBuffer();
    public String currentJobName = "", previousUrl = "", previousLabel = "";
    private final Map<String, Object> requestParams;
    //private List<String> copyConfigs = new ArrayList<>(Arrays.asList("description", "properties", "triggers"));
    //private List<String> transformConfigs = new ArrayList<>(Arrays.asList("label", "scm", "builders", "publishers"));
    private boolean onlyBuildTrigger = true;
    protected FreeStyleProject job;
    private String newJobName;
    private SCM scm;

    /**
     * Constructor to initialise variables required to process transformation.
     *
     * @param requestParams Map that contains request parameters.
     */
    public Transformer(Map requestParams) {
        script = new StringBuffer();
        this.requestParams = requestParams;
    }

    /**
     * Initialises transformation process of Freestyle project to Pipeline.
     */
    public void performFreeStyleTransformation(String name) {
        newJobName=name;
        initializeConversion();
        transformJob((FreeStyleProject) requestParams.get("initialProject"), (boolean)requestParams.get("downStream"));
        finalizeConversion((boolean) requestParams.get("commitJenkinsfile"), requestParams.get("commitMessage").toString());
        logger.info("Completed conversion of all jobs");
    }

    /** For testing pureposes
     * @param jobName
     * @param doc */
    /* public String transformXml(String jobName) throws ParserConfigurationException {
        initializeConversion();
        this.currentJobName = jobName;
        transformDocument();
        finalizeConversion(false, "");
        return script.toString();
    } */

    private void initializeConversion() {
        appendToScript("timestamps {\n");
    }
    private void finalizeConversion(boolean commitJenkinsfile, String commitMessage) {
        appendToScript("\n}\n}");
        appendScriptToXML(commitJenkinsfile, commitMessage);
        saveJob();
    }

    /**
     * Invokes conversion of given FreeStyle Job.
     * This method is recursively called if downstream jobs are to be converted also.
     *
     * @param item FreeStyle job to convert.
     * @param downStream Boolean to decide if item's downstream jobs are to be converted.
     */
    private void transformJob(FreeStyleProject item, boolean downStream) {

            currentJobName = item.getFullName();
            this.job=item;
            //doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(item.getConfigFile().getFile());
            transformDocument();
            if (downStream) {
                for (Item downstreamJob : item.getDownstreamProjects()) {
                    if (downstreamJob instanceof FreeStyleProject) {
                        firstJob = false;
                        transformJob((FreeStyleProject) downstreamJob, true);
                    }
                }
            }
     /*   } catch (Exception e) {
            logger.severe("Exception occurred in Transformer constructor: " + e.getMessage());
        } */
    }

    protected void transformDocument() {
        try {
            destJob=Jenkins.getInstance().createProject(org.jenkinsci.plugins.workflow.job.WorkflowJob.class, newJobName);
            transformFreestyleJob();
        } catch (IOException ex) {
            logger.severe("Error Creating job: "+ex.getMessage());
        }
    }

    /**
     * Calls 'copy' or 'transformers' methods of various configurations of a FreeStyle job.
     */
    private void transformFreestyleJob() {
        copyConfigurations();
        logger.info("Transforming Label");
        transformLabel();
        logger.info("Transforming SCM");
        SCMTransformer scmTransformer = new SCMTransformer(this);
        scmTransformer.convertSCM();
        this.scm=scmTransformer.getScm();
        logger.info("Transforming Builders");
        BuilderTransformer builderTransformer = new BuilderTransformer(this);
        builderTransformer.convertBuilders();
        logger.info("Transforming Publishers");
        PublisherTransformer publisherTransformer = new PublisherTransformer(this);
        publisherTransformer.convertPublishers();
        this.appendToScript(this.buildSteps.toString());
        if(!this.getOnlyBuildTrigger()) {
            this.appendToScript(this.publishSteps.toString());
        }
    }


    /**
     * Copies known configurations that are identical in pipeline jobs.
     * Plugins / configurations that cause job creation / build to fail are explicitely to removed.
     *
     * @param configurations List of configurations to copy.
     */
    private void copyConfigurations() {
        try {
            getDestJob().setDescription(job.getDescription());
        } catch (IOException ex) {
           logger.severe("Error setting description");
           logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        try {
            getDestJob().setTriggers(new ArrayList<>(job.getTriggers().values()));
        } catch (IOException ex) {
            logger.severe("Error adding triggers");
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        List<String> propertiesToRemove=new ArrayList<>();
        propertiesToRemove.add("DiskUsageProperty");
        propertiesToRemove.add("hudson-plugins-promoted_builds-JobPropertyImpl");
        List<JobProperty<? super FreeStyleProject>> sourceProperties=job.getAllProperties();
        for(JobProperty<? super FreeStyleProject> property: sourceProperties){
            try {
                if(!propertiesToRemove.contains(property.getDescriptor().getJsonSafeClassName())){
                    getDestJob().addProperty(property);
                }
                else{
                    appendToScript("\n\\\\ Discarded: "+property.getDescriptor().getDisplayName());
                }
            } catch (IOException ex) {
                logger.severe("Error adding property "+property);
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
        if (label!=null) {
            if(firstJob) {
                appendToScript("\nnode ('" + label + "') { \n");
            } else {
                if(!label.equalsIgnoreCase(previousLabel)) {
                    appendToScript("\n}\nnode ('" + label + "') { \n");
                }
            }
        } else {
            if(firstJob) {
                appendToScript("\nnode () {\n");
            } else {
                if(!label.equalsIgnoreCase(previousLabel)) {
                    appendToScript("\n}\nnode () { \n");
                }
            }
        }
        previousLabel = label;
    }

    /**
     * General method to append script block with a stage wrap.
     * @param stage Name of stage to create.
     * @param block Script block to add under stage.
     */
    public void appendToScript(String stage, String block) {
        script.append("\n\tstage ('" + stage + "') {\n \t " + block + " \n\t}");
    }

    /**
     * General method to append script block to main script.
     * @param block Script block to add to main script.
     */
    public void appendToScript(String block) {
        script.append(block);
    }

    /**
     * Add Groovy Script to XML strucuture.
     * If commit to SCM is selected, script is written to Jenkinsfile and committed to SCM.
     * @param commitJenkinsfile Boolean to decide if script is to be kept inline or committed as Jenkinsfile to SCM.
     * @param commitMessage Commit message if Jenkinsfile is to be committed to SCM.
     */
    private void appendScriptToXML(boolean commitJenkinsfile, String commitMessage) {
        FlowDefinition fd;
        if(commitJenkinsfile) {
            fd= new CpsScmFlowDefinition(scm,"Jenkinsfile");
            new SCMUtil().pushJenkinsfile(script.toString(), scmURL, scmBranch, scmCredentialsId, commitMessage, scmType);
        }
        else{
            fd=new CpsFlowDefinition(script.toString(),true);
        }
        destJob.setDefinition(fd);
    }

    /**
     * Gets Element object from Node based on tag name.
     * @param node Node object from which element is to be extracted.
     * @param tag Name of tag to extract.
     * @return Element derived from node by given tag name.
     */
/*    public Element getElementByTag(Node node, String tag) {
        return (Element) ((Element) node).getElementsByTagName(tag).item(0);
    }
*/
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

    /**
     * Setter method to set SCM Branch. Useful when committing Jenkinsfile to SCM.
     * @param scmBranch SCM Branch to set for committing Jenkinsfile to SCM.
     */
    public void setScmBranch(String scmBranch) {
        this.scmBranch = scmBranch;
    }

    /**
     * Setter method to set SCM URL. Useful when committing Jenkinsfile to SCM.
     * @param scmURL SCM URL to set for committing Jenkinsfile to SCM.
     */
    public void setScmURL(String scmURL) {
        this.scmURL = scmURL;
    }

    /**
     * Setter method to set SCM Type. Useful when committing Jenkinsfile to SCM.
     * @param scmType SCM Type to set for committing Jenkinsfile to SCM.
     */
    public void setScmType(String scmType) {
        this.scmType = scmType;
    }

    /**
     * Setter method to set SCM Credentials Id. Useful when committing Jenkinsfile to SCM.
     * @param scmCredentialsId SCM Credentials Id to set for committing Jenkinsfile to SCM.
     */
    public void setScmCredentialsId(String scmCredentialsId) {
        this.scmCredentialsId = scmCredentialsId;
    }
    
    public void setOnlyBuildTrigger(boolean value) {
        onlyBuildTrigger = value;
    }

    public boolean getOnlyBuildTrigger() {
        return onlyBuildTrigger;
    }

    /**
     * @return the destJob
     */
    public org.jenkinsci.plugins.workflow.job.WorkflowJob getDestJob() {
        return destJob;
    }

}
