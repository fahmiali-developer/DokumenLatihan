package com.supercode.controller;


import com.supercode.service.PosAggregatorService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PosAggregatorController {

    @Inject
    PosAggregatorService posAggregatorService;

    /*
    * upload header for pos
    * */
    @POST
    @Path("/upload/file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadHeaderPos(@MultipartForm MultipartFormDataInput input, @QueryParam("pmId") String pmId,
                                    @QueryParam("branchId") String branchId) {
        return posAggregatorService.uploadFile(input, pmId, branchId);
    }

}
