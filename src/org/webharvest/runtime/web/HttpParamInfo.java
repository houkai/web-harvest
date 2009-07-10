package org.webharvest.runtime.web;

import org.webharvest.runtime.variables.*;

/**
 * Information about http request parameter.
 */
public class HttpParamInfo {

    // name of parameter
    private String name;

    // type of a part in multipart encoded requests - applies only if http processor is multiparted
    // valid values are file and string
    private String partType;

    // filename of upload file - applies only for multipart requests where partType = file
    private String fileName;

    // content type of upload file - applies only for multipart requests where partType = file
    private String contentType;

    // parameter value
    private Variable value;

    public HttpParamInfo(String name, String partType, String fileName, String contentType, Variable value) {
        this.name = name;
        this.partType = partType;
        this.fileName = fileName;
        this.contentType = contentType;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getPartType() {
        return partType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Variable getValue() {
        return value;
    }
    
}