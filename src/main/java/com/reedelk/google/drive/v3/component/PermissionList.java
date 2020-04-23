package com.reedelk.google.drive.v3.component;

import com.google.api.services.drive.Drive;
import com.reedelk.google.drive.v3.internal.DriveApi;
import com.reedelk.google.drive.v3.internal.DriveApiFactory;
import com.reedelk.google.drive.v3.internal.attribute.PermissionListAttribute;
import com.reedelk.google.drive.v3.internal.command.PermissionListCommand;
import com.reedelk.google.drive.v3.internal.commons.Mappers;
import com.reedelk.google.drive.v3.internal.exception.PermissionListException;
import com.reedelk.runtime.api.annotation.Description;
import com.reedelk.runtime.api.annotation.ModuleComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("Drive Permissions List")
@Component(service = PermissionList.class, scope = PROTOTYPE)
public class PermissionList implements ProcessorSync {

    @Property("Configuration")
    @Description("The Google Service Account Configuration to be used to connect to Google Drive." +
            "This component requires the configuration of a Service Account to make authorized API calls " +
            "on behalf of the user. More info about Service Accounts and how they can be configured can " +
            "be found at the following <a href=\"https://cloud.google.com/iam/docs/service-accounts\">link</a>.")
    private DriveConfiguration configuration;

    @Property("File ID")
    @Description("The ID of the file we want to list the permission. " +
            "If empty, the file ID is taken from the message payload.")
    private DynamicString fileId;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    private DriveApi driveApi;

    @Override
    public void initialize() {
        driveApi = DriveApiFactory.create(PermissionList.class, configuration);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Message apply(FlowContext flowContext, Message message) {

        String realFileId = scriptEngine.evaluate(fileId, flowContext, message)
                .orElseThrow(() -> new PermissionListException("File ID must not be null."));

        PermissionListCommand command = new PermissionListCommand(realFileId);

        List<Map> permissions = driveApi.execute(command);

        PermissionListAttribute attribute = new PermissionListAttribute(realFileId);

        return MessageBuilder.get(FileDelete.class)
                .withList(permissions, Map.class)
                .attributes(attribute)
                .build();
    }

    public void setConfiguration(DriveConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setFileId(DynamicString fileId) {
        this.fileId = fileId;
    }
}
