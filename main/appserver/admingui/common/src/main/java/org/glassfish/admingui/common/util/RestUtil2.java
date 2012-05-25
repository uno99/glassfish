/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.common.util;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.faces.context.FacesContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;

/**
 * This class is intended to replace, eventually, RestUtil. Whether or not that happens depends in large part on how the
 * new console is built. In the meantime, though, there is a need for REST-related utilities that behave in a manner not
 * supported by the current methods in RestUtil, so these need to be written. Rather than place these new methods beside
 * the existing ones, we will, for now, separate the two to maintain ease in identifying old vs new methods, as well as
 * helping to ensure that these methods remain purely REST-focused, free from the encroachment of view-related libraries
 * (see the pervasiveness of JSFTemplating types in RestUtil).
 *
 * @author jdlee
 */
public class RestUtil2 {

    @Handler(id = "rest.list",
            input = {
                    @HandlerInput(name="endpoint", type=String.class, required=true),
                    @HandlerInput(name="attrs", type=Map.class, required=false)},
            output = {
                    @HandlerOutput(name="result", type=List.class)})
    public static void sendRequestToCollectionResource(HandlerContext handlerCtx) {
        Map<String, Object> attrs = (Map<String, Object>) handlerCtx.getInputValue("attrs");
        String endpoint = fixEndpoint((String) handlerCtx.getInputValue("endpoint"));

        ClientResponse resp = RestUtil.getJerseyClient().resource(endpoint)
                .cookie(new Cookie(RestUtil.REST_TOKEN_COOKIE, RestUtil.getRestToken()))
                .accept(RestUtil.RESPONSE_TYPE).get(ClientResponse.class);
        if (!isSuccess(resp.getStatus())) {
            throw new RuntimeException(resp.getEntity(String.class));
        }

        List list = resp.getEntity(List.class);
        handlerCtx.setOutputValue("result", list);
    }

    @Handler(id = "rest.get",
            input = {
                    @HandlerInput(name="endpoint", type=String.class, required=true),
                    @HandlerInput(name="attrs", type=Map.class, required=false)},
            output = {
                    @HandlerOutput(name="result", type=Map.class)})
    public static void sendGetRequestToItemResource(HandlerContext handlerCtx) {
        Map<String, Object> attrs = (Map<String, Object>) handlerCtx.getInputValue("attrs");
        String endpoint = fixEndpoint((String) handlerCtx.getInputValue("endpoint"));

        ClientResponse resp = makeRequest(endpoint, attrs).get(ClientResponse.class);

        Map map = resp.getEntity(Map.class);
        handlerCtx.setOutputValue("result", map);
    }

    @Handler(id = "rest.post",
            input = {
                    @HandlerInput(name="endpoint", type=String.class, required=true),
                    @HandlerInput(name="attrs", type=Map.class, required=false)},
            output = {
                    @HandlerOutput(name="result", type=String.class)})
    public static void sendPostRequest(HandlerContext handlerCtx) {
        Map<String, Object> attrs = (Map<String, Object>) handlerCtx.getInputValue("attrs");
        String endpoint = fixEndpoint((String) handlerCtx.getInputValue("endpoint"));

        ClientResponse resp = makeRequest(endpoint, null).post(ClientResponse.class, attrs);
        if (!isSuccess(resp.getStatus())) {
            GuiUtil.getLogger().log(
                    Level.SEVERE,
                    GuiUtil.getCommonMessage("LOG_UPDATE_ENTITY_FAILED", new Object[]{endpoint, attrs}));
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.error.checkLog"));
            return;
        }

        handlerCtx.setOutputValue("result", endpoint);
    }

    private static String fixEndpoint(String endpoint) {
        if (endpoint.startsWith("/")) {
            endpoint = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("REST_URL") + endpoint;
        }
        return endpoint;
    }
    
    private static Builder makeRequest(String endpoint, Map<String, Object> queryParams) {
        WebResource resource = RestUtil.getJerseyClient().resource(endpoint);
        if (queryParams != null) {
            resource =  resource.queryParams(RestUtil.buildMultivalueMap(queryParams));
        }
        return resource
                .type(MediaType.APPLICATION_JSON)
                .cookie(new Cookie(RestUtil.REST_TOKEN_COOKIE, RestUtil.getRestToken()))
                .accept(RestUtil.RESPONSE_TYPE);
    }

    protected static boolean isSuccess(int status) {
        return (status >= 200) && (status <= 299);
    }
}
