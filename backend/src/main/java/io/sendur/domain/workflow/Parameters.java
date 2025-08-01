package io.sendur.domain.workflow;

import com.fasterxml.jackson.annotation.*;

public class Parameters {
    private Options options;
    private Rule rule;
    private String promptType;
    private String text;
    private String url;
    private String authentication;
    private String genericAuthType;
    private String jsCode;
    private String method;
    private Boolean sendBody;
    private String specifyBody;
    private String jsonBody;
    private Model model;

    @JsonProperty("options")
    public Options getOptions() { return options; }

    @JsonProperty("options")
    public void setOptions(Options value) { this.options = value; }

    @JsonProperty("rule")
    public Rule getRule() { return rule; }

    @JsonProperty("rule")
    public void setRule(Rule value) { this.rule = value; }

    @JsonProperty("promptType")
    public String getPromptType() { return promptType; }

    @JsonProperty("promptType")
    public void setPromptType(String value) { this.promptType = value; }

    @JsonProperty("text")
    public String getText() { return text; }

    @JsonProperty("text")
    public void setText(String value) { this.text = value; }

    @JsonProperty("url")
    public String getURL() { return url; }

    @JsonProperty("url")
    public void setURL(String value) { this.url = value; }

    @JsonProperty("authentication")
    public String getAuthentication() { return authentication; }

    @JsonProperty("authentication")
    public void setAuthentication(String value) { this.authentication = value; }

    @JsonProperty("genericAuthType")
    public String getGenericAuthType() { return genericAuthType; }

    @JsonProperty("genericAuthType")
    public void setGenericAuthType(String value) { this.genericAuthType = value; }

    @JsonProperty("jsCode")
    public String getJSCode() { return jsCode; }

    @JsonProperty("jsCode")
    public void setJSCode(String value) { this.jsCode = value; }

    @JsonProperty("method")
    public String getMethod() { return method; }

    @JsonProperty("method")
    public void setMethod(String value) { this.method = value; }

    @JsonProperty("sendBody")
    public Boolean getSendBody() { return sendBody; }

    @JsonProperty("sendBody")
    public void setSendBody(Boolean value) { this.sendBody = value; }

    @JsonProperty("specifyBody")
    public String getSpecifyBody() { return specifyBody; }

    @JsonProperty("specifyBody")
    public void setSpecifyBody(String value) { this.specifyBody = value; }

    @JsonProperty("jsonBody")
    public String getJSONBody() { return jsonBody; }

    @JsonProperty("jsonBody")
    public void setJSONBody(String value) { this.jsonBody = value; }

    @JsonProperty("model")
    public Model getModel() { return model; }

    @JsonProperty("model")
    public void setModel(Model model) { this.model = model; }
}
