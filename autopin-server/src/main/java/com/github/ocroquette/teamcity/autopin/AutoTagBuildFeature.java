package com.github.ocroquette.teamcity.autopin;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.github.ocroquette.teamcity.autopin.StringUtils.isSet;

public class AutoTagBuildFeature extends BuildFeature {

    public static final String TYPE = "auto-tag";

    public static final String PARAM_BRANCH_PATTERN = "branch_pattern";
    public static final String PARAM_TAG = "tag";
    public static final String PARAM_TAG_DEPENDENCIES = "tag_dependencies";

    private final Logger LOG = Logger.getLogger(Loggers.SERVER_CATEGORY);

    private final String myEditUrl;

    public AutoTagBuildFeature(@NotNull final PluginDescriptor descriptor) {
        myEditUrl = descriptor.getPluginResourcesPath("autotagBuildFeatureSettings.jsp");
    }


    @NotNull
    @Override
    public String getType() {
        return TYPE;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Tag the build";
    }

    @Nullable
    @Override
    public String getEditParametersUrl() {
        return myEditUrl;
    }

    @Override
    public boolean isMultipleFeaturesPerBuildTypeAllowed() {
        return true;
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> params) {
        StringBuilder sb = new StringBuilder();

        sb.append("Tag build");

        if (StringUtils.isTrue(getParameterWithDefaults(params, PARAM_TAG_DEPENDENCIES)))
            sb.append(" and all its dependencies");

        sb.append(" with \"" + getParameterWithDefaults(params, PARAM_TAG) + "\"");

        if (!getParameterWithDefaults(params, PARAM_BRANCH_PATTERN).isEmpty())
            sb.append(" if branch matches \"" + getParameterWithDefaults(params, PARAM_BRANCH_PATTERN) + "\"");

        return sb.toString();
    }

    public String getParameterWithDefaults(Map<String, String> parameters, String name) {
        if (parameters.containsKey(name)) {
            return parameters.get(name);
        }

        Map<String, String> defaultParameters = getDefaultParameters();
        if (defaultParameters.containsKey(name)) {
            return defaultParameters.get(name);
        }

        return "UNDEFINED";
    }

    @Nullable
    @Override
    public PropertiesProcessor getParametersProcessor() {
        return new PropertiesProcessor() {
            public Collection<InvalidProperty> process(Map<String, String> params) {
                List<InvalidProperty> errors = new ArrayList<InvalidProperty>();
                if (!isSet(params.get(PARAM_TAG)))
                    errors.add(new InvalidProperty(PARAM_TAG, "Tag must be provided"));
                return errors;
            }
        };
    }

    @Override
    public Map<String, String> getDefaultParameters() {
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put(PARAM_BRANCH_PATTERN, "");
        map.put(PARAM_TAG, "");
        map.put(PARAM_TAG_DEPENDENCIES, "true");
        return map;
    }
}
