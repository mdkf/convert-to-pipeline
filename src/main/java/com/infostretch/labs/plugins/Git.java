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

package com.infostretch.labs.plugins;

import com.infostretch.labs.transformers.Transformer;
import com.infostretch.labs.utils.SCMUtil;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import hudson.plugins.git.GitSCM;

import java.io.File;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Converts Git SCM configurations and generates checkout script.
 * @author Mohit Dharamshi
 */
public class Git extends Plugins {
    private static final Logger logger = Logger.getLogger(Git.class.getName());

    @Override
    public void pushJenkinsfile(File workSpace, String script, String url, String branchName, String commitMessage, CredentialsProvider credentialsProvider) {
        try {
            if(branchName.startsWith("*/")) {
                branchName = branchName.replaceFirst("\\*\\/*", "");
            }
            CloneCommand cloneCommand = new CloneCommand().setCredentialsProvider(credentialsProvider).setDirectory(workSpace).setURI(url).setBranch(branchName);
            org.eclipse.jgit.api.Git git = cloneCommand.call();
            logger.info("Cloned repo");
            SCMUtil.writeJenkinsfile(workSpace, script);
            AddCommand addCommand = git.add();
            addCommand.addFilepattern(".");
            logger.info("Added Jenkinsfile");
            addCommand.call();
            CommitCommand commitCommand = git.commit();
            commitCommand.setMessage(commitMessage);
            commitCommand.call();
            PushCommand pushCommand = git.push();
            pushCommand.setCredentialsProvider(credentialsProvider).setForce(true).setPushAll();
            logger.info("Pushing Jenkinsfile");
            Iterator<PushResult> it = pushCommand.call().iterator();
            if(it.hasNext()){
                logger.info(it.next().toString());
            }
            workSpace.deleteOnExit();
        } catch (Exception e) {
            logger.severe("Exception : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
