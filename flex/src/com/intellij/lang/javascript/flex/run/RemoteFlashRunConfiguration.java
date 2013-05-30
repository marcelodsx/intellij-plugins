package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class RemoteFlashRunConfiguration extends RunConfigurationBase implements LocatableConfiguration {

  private static final String DEFAULT_NAME = "Remote debug";

  private RemoteFlashRunnerParameters myRunnerParameters = new RemoteFlashRunnerParameters();

  public RemoteFlashRunConfiguration(final Project project, final ConfigurationFactory factory, final String name) {
    super(project, factory, name);
  }

  @Override
  public RemoteFlashRunConfiguration clone() {
    final RemoteFlashRunConfiguration clone = (RemoteFlashRunConfiguration)super.clone();
    clone.myRunnerParameters = myRunnerParameters.clone();
    return clone;
  }

  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new RemoteFlashRunConfigurationForm(getProject());
  }

  @Override
  public void readExternal(final Element element) throws InvalidDataException {
    super.readExternal(element);
    myRunnerParameters = new RemoteFlashRunnerParameters();
    XmlSerializer.deserializeInto(myRunnerParameters, element);
  }

  @Override
  public void writeExternal(final Element element) throws WriteExternalException {
    super.writeExternal(element);
    XmlSerializer.serializeInto(myRunnerParameters, element);
  }

  @Override
  public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    try {
      myRunnerParameters.checkAndGetModuleAndBC(getProject());
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e.getMessage());
    }

    return FlexBaseRunner.EMPTY_RUN_STATE;
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    myRunnerParameters.checkAndGetModuleAndBC(getProject());
  }

  @NotNull
  public RemoteFlashRunnerParameters getRunnerParameters() {
    return myRunnerParameters;
  }

  @NotNull
  public Module[] getModules() {
    final Module module = ModuleManager.getInstance(getProject()).findModuleByName(myRunnerParameters.getModuleName());
    if (module != null && ModuleType.get(module) instanceof FlexModuleType) {
      return new Module[]{module};
    }
    else {
      return Module.EMPTY_ARRAY;
    }
  }

  @Override
  public boolean isGeneratedName() {
    return getName().startsWith(ExecutionBundle.message("run.configuration.unnamed.name.prefix")) || getName().equals(suggestedName());
  }

  @Override
  public String suggestedName() {
    final String bcName = myRunnerParameters.getBCName();
    return StringUtil.isEmptyOrSpaces(bcName) ? DEFAULT_NAME : (DEFAULT_NAME + " (" + bcName + ")");
  }
}
