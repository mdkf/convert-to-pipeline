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

package com.infostretch.labs.utils;

/**
 * @author Mohit Dharamshi
 */
public enum PluginIgnoredClass {

    BuildTrigger("hudson.tasks.BuildTrigger"),
    NullSCM("hudson.scm.NullSCM");

    private final String className;

    PluginIgnoredClass(String className) {
        this.className = className;
    }

    public static PluginIgnoredClass searchByValue(String value) {
        for (PluginIgnoredClass plugin : PluginIgnoredClass.values()) {
            if (plugin.className.equals(value)) {
                return plugin;
            }
        }
        return null;
    }

}
