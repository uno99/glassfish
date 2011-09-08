/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.console.beans;

import javax.faces.bean.*;
import javax.faces.context.FacesContextFactory;
import javax.faces.*;
import javax.faces.context.FacesContext;
import java.util.*;
import org.glassfish.admingui.console.util.DeployUtil;
import org.glassfish.admingui.console.util.CommandUtil;

@ManagedBean(name="applicationBean")
@RequestScoped
public class ApplicationBean {

    private String appName;
    //public List<String> URLs;
    public ApplicationBean() {
        FacesContextFactory factory = (FacesContextFactory)
            FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        Map requestMap =
                FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        appName = (String)requestMap.get("appName");
        List l = getInstances();
    }

    public String getDescription() {
        return appName;
    }

    public String getAppName() {
        return appName;
    }
    
    public List<Map<String, String>> getUrls() {
        return DeployUtil.getTargetURLList(appName, appName);
    }

    public List<Map> getServices() {
        return CommandUtil.listServices(appName, null, "application");
    }

    public List getInstances() {
        return DeployUtil.getApplicationTarget(appName, "application-ref");
    }


}
