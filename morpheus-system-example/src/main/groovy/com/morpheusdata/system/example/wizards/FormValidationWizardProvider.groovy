package com.morpheusdata.system.example.wizards

import com.morpheusdata.core.Plugin
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.providers.WizardProvider
import com.morpheusdata.model.FormErrors
import com.morpheusdata.model.OptionType
import com.morpheusdata.model.OptionTypeFieldGroup
import com.morpheusdata.model.WizardStep
import com.morpheusdata.response.ServiceResponse

/**
 * Demonstrates two capabilities for plugin form authors and the Test team:
 *
 * 1. Field groups with a group <b>title</b> ({@code name}) and a group-level
 *    <b>description</b> that renders beneath the title.
 * 2. Form field validations: {@code required}, {@code minLength}/{@code maxLength},
 *    {@code minVal}/{@code maxVal}, regex validation via {@code verifyPattern},
 *    conditional requirement via {@code requireOnCode}, plus server-side
 *    validation in {@link #validateWizard(Map, Map)} using {@link FormErrors}
 *    that surfaces field-level errors in the UI when expected values are missing
 *    or invalid.
 */
class FormValidationWizardProvider extends com.morpheusdata.system.example.BaseProvider implements WizardProvider {

    FormValidationWizardProvider(Plugin plugin, MorpheusContext morpheus) {
        super(plugin, morpheus)
    }

    @Override
    String getCode() {
        return 'form-validation-example'
    }

    @Override
    String getWizardName() {
        return 'Form Field Groups & Validation'
    }

    @Override
    String getName() {
        return getWizardName()
    }

    @Override
    List<WizardStep> getWizardSteps() {
        return [buildFieldGroupStep(), buildValidationStep()]
    }

    /**
     * Step 1 - field groups that each render a title ({@code name}) and a
     * group-level description.
     */
    private WizardStep buildFieldGroupStep() {
        WizardStep step = new WizardStep(
            code: 'field-groups',
            name: 'Field Groups',
            description: 'Demonstrates group titles and group-level descriptions'
        )

        // Group 1: a described group whose fields are always shown.
        OptionTypeFieldGroup identityGroup = new OptionTypeFieldGroup(
            code: 'network-identity',
            name: 'Network Identity',
            description: 'Basic identity settings for this network. The text you are reading is the group description.',
            collapsible: true,
            defaultCollapsed: false,
            displayOrder: 0,
            options: [
                new OptionType(
                    code: 'fv-networkName',
                    name: 'Network Name',
                    fieldName: 'networkName',
                    fieldLabel: 'Network Name',
                    fieldContext: 'config',
                    inputType: OptionType.InputType.TEXT,
                    required: true,
                    displayOrder: 0
                ),
                new OptionType(
                    code: 'fv-networkZone',
                    name: 'Network Zone',
                    fieldName: 'networkZone',
                    fieldLabel: 'Network Zone',
                    fieldContext: 'config',
                    inputType: OptionType.InputType.TEXT,
                    displayOrder: 1
                )
            ]
        )

        // Group 2: a second described group demonstrating numeric range validation.
        OptionTypeFieldGroup advancedGroup = new OptionTypeFieldGroup(
            code: 'advanced-settings',
            name: 'Advanced Settings',
            description: 'Optional tuning parameters. Each group can carry its own descriptive text like this.',
            collapsible: true,
            defaultCollapsed: false,
            displayOrder: 1,
            options: [
                new OptionType(
                    code: 'fv-mtu',
                    name: 'MTU',
                    fieldName: 'mtu',
                    fieldLabel: 'MTU',
                    fieldContext: 'config',
                    inputType: OptionType.InputType.NUMBER,
                    minVal: 576L,
                    maxVal: 9000L,
                    defaultValue: '1500',
                    displayOrder: 0
                ),
                new OptionType(
                    code: 'fv-vlanId',
                    name: 'VLAN ID',
                    fieldName: 'vlanId',
                    fieldLabel: 'VLAN ID',
                    fieldContext: 'config',
                    inputType: OptionType.InputType.NUMBER,
                    minVal: 1L,
                    maxVal: 4094L,
                    displayOrder: 1
                )
            ]
        )

        step.optionTypeFieldGroups = [identityGroup, advancedGroup]
        return step
    }

    /**
     * Step 2 - client-side validation attributes on individual option types plus
     * server-side validation in {@link #validateWizard(Map, Map)}.
     */
    private WizardStep buildValidationStep() {
        WizardStep step = new WizardStep(
            code: 'field-validations',
            name: 'Field Validations',
            description: 'Demonstrates required, length, range, regex and conditional validations'
        )

        step.optionTypes = [
            // required + length + regex (lowercase letters, digits and hyphens only)
            new OptionType(
                code: 'fv-hostname',
                name: 'Hostname',
                fieldName: 'hostname',
                fieldLabel: 'Hostname',
                fieldContext: 'config',
                inputType: OptionType.InputType.TEXT,
                required: true,
                minLength: 3L,
                maxLength: 15L,
                verifyPattern: '^[a-z0-9-]+$',
                helpText: 'Lowercase letters, numbers and hyphens only (3-15 characters)',
                displayOrder: 0
            ),
            // numeric range validation
            new OptionType(
                code: 'fv-port',
                name: 'Service Port',
                fieldName: 'port',
                fieldLabel: 'Service Port',
                fieldContext: 'config',
                inputType: OptionType.InputType.NUMBER,
                required: true,
                minVal: 1L,
                maxVal: 65535L,
                helpText: 'Valid TCP port between 1 and 65535',
                displayOrder: 1
            ),
            // regex validation for an email address
            new OptionType(
                code: 'fv-adminEmail',
                name: 'Admin Email',
                fieldName: 'adminEmail',
                fieldLabel: 'Administrator Email',
                fieldContext: 'config',
                inputType: OptionType.InputType.TEXT,
                verifyPattern: '^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$',
                helpText: 'Must be a valid email address',
                displayOrder: 2
            ),
            // toggle that conditionally requires the notification address below
            new OptionType(
                code: 'fv-enableNotifications',
                name: 'Enable Notifications',
                fieldName: 'enableNotifications',
                fieldLabel: 'Enable Notifications',
                fieldContext: 'config',
                inputType: OptionType.InputType.CHECKBOX,
                defaultValue: 'off',
                displayOrder: 3
            ),
            // required only when notifications are enabled (conditional requirement)
            new OptionType(
                code: 'fv-notificationAddress',
                name: 'Notification Address',
                fieldName: 'notificationAddress',
                fieldLabel: 'Notification Address',
                fieldContext: 'config',
                inputType: OptionType.InputType.TEXT,
                requireOnCode: 'config.enableNotifications:on',
                verifyPattern: '^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$',
                helpText: 'Required when notifications are enabled',
                displayOrder: 4
            )
        ]

        return step
    }

    @Override
    ServiceResponse validateWizard(Map wizardData, Map opts) {
        FormErrors formErrors = new FormErrors()

        Map identity = (wizardData['field-groups'] ?: [:]) as Map
        if (!fieldValue(identity, 'networkName')) {
            formErrors.addError('networkName', 'Network name is required')
        }

        Map validations = (wizardData['field-validations'] ?: [:]) as Map

        String hostname = fieldValue(validations, 'hostname')
        if (!hostname) {
            formErrors.addError('hostname', 'Hostname is required')
        } else if (hostname.length() < 3 || hostname.length() > 15) {
            formErrors.addError('hostname', 'Hostname must be between 3 and 15 characters')
        } else if (!(hostname ==~ /^[a-z0-9-]+$/)) {
            formErrors.addError('hostname', 'Hostname may only contain lowercase letters, numbers and hyphens')
        }

        String portValue = fieldValue(validations, 'port')
        if (!portValue) {
            formErrors.addError('port', 'Service port is required')
        } else if (!portValue.isInteger() || portValue.toInteger() < 1 || portValue.toInteger() > 65535) {
            formErrors.addError('port', 'Service port must be a number between 1 and 65535')
        }

        String adminEmail = fieldValue(validations, 'adminEmail')
        if (adminEmail && !(adminEmail ==~ /^[^@\s]+@[^@\s]+\.[^@\s]+$/)) {
            formErrors.addError('adminEmail', 'Administrator email must be a valid email address')
        }

        boolean notificationsEnabled = isChecked(fieldValue(validations, 'enableNotifications'))
        String notificationAddress = fieldValue(validations, 'notificationAddress')
        if (notificationsEnabled && !notificationAddress) {
            formErrors.addError('notificationAddress', 'Notification address is required when notifications are enabled')
        }

        if (formErrors.hasErrors()) {
            return ServiceResponse.error('Validation failed', null, formErrors.getErrors())
        }

        return ServiceResponse.success()
    }

    @Override
    ServiceResponse submitWizard(Map wizardData, Map opts) {
        Map result = [:]
        result.putAll((wizardData['field-groups'] ?: [:]) as Map)
        result.putAll((wizardData['field-validations'] ?: [:]) as Map)
        return ServiceResponse.success(result)
    }

    /** Reads a field value whether the step data is flat or nested under {@code config}. */
    private static String fieldValue(Map stepData, String fieldName) {
        def value = stepData[fieldName]
        if (value == null && stepData['config'] instanceof Map) {
            value = ((Map) stepData['config'])[fieldName]
        }
        return value != null ? value.toString().trim() : null
    }

    private static boolean isChecked(String value) {
        return value != null && (value == 'on' || value == 'true')
    }
}
