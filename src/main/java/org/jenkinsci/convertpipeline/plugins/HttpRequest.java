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
import jenkins.plugins.http_request.HttpRequestStep;
import org.jenkinsci.convertpipeline.transformers.Transformer;

/**
 *
 * @author Michael DK Fowler
 */
public class HttpRequest extends Plugins {

    private final jenkins.plugins.http_request.HttpRequest builder;

    public HttpRequest(Transformer transformer, jenkins.plugins.http_request.HttpRequest builder) {
        super(transformer, builder);
        this.builder = builder;
    }

    @Override
    public String transform() {
        HttpRequestStep s = new jenkins.plugins.http_request.HttpRequestStep(builder.getUrl());
        s.setAcceptType(builder.getAcceptType());
        s.setAuthentication(builder.getAuthentication());
        s.setConsoleLogResponseBody(builder.getConsoleLogResponseBody());
        s.setContentType(builder.getContentType());
        s.setCustomHeaders(builder.getCustomHeaders());
        s.setHttpMode(builder.getHttpMode());
        s.setHttpProxy(builder.getHttpProxy());
        s.setIgnoreSslErrors(builder.getIgnoreSslErrors());
        s.setOutputFile(builder.getOutputFile());
        s.setQuiet(builder.getQuiet());
        s.setRequestBody(builder.getRequestBody());
        s.setTimeout(builder.getTimeout());
        s.setValidResponseCodes(builder.getValidResponseCodes());
        s.setValidResponseContent(builder.getValidResponseContent());
        return callSnippetizer(s);
    }
}
