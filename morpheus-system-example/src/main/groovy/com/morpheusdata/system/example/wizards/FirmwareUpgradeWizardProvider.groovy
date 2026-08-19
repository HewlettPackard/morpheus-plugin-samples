package com.morpheusdata.system.example.wizards

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.core.providers.WizardProvider
import com.morpheusdata.model.FormErrors
import com.morpheusdata.model.OptionType
import com.morpheusdata.model.WizardStep
import com.morpheusdata.response.ServiceResponse
import com.morpheusdata.system.example.BaseProvider

/**
 * Wizard used to collect input for the firmware upgrade action.
 *
 * <p>Referenced by {@code SystemFirmwareUpgradeActionProvider} via the {@code wizard}
 * property of its {@code ActionType}. When an action type has a wizard, the UI runs the
 * wizard first and hands the collected data to the action provider.</p>
 */
class FirmwareUpgradeWizardProvider extends BaseProvider implements WizardProvider {

    static final String STEP_CODE = 'firmware-upgrade-options'

    FirmwareUpgradeWizardProvider(Plugin plugin, MorpheusContext morpheus) {
        super(plugin, morpheus)
    }

    @Override
    String getCode() {
        return 'arcus-firmware-upgrade-wizard'
    }

    @Override
    String getWizardName() {
        return 'Firmware Upgrade'
    }

    @Override
    String getWizardDescription() {
        return 'Collects the options used when upgrading firmware across the Arcus system'
    }

    @Override
    String getName() {
        return getWizardName()
    }

    @Override
    List<WizardStep> getWizardSteps() {
        WizardStep step = new WizardStep(
            code: STEP_CODE,
            name: 'Upgrade Options',
            description: 'Choose the firmware bundle and upgrade strategy'
        )

        step.optionTypes = [
            new OptionType(
                code: 'targetVersion',
                name: 'Target Version',
                fieldName: 'targetVersion',
                fieldLabel: 'Target Firmware Version',
                fieldContext: 'config',
                inputType: OptionType.InputType.TEXT,
                required: true,
                displayOrder: 1
            ),
            new OptionType(
                code: 'rollingUpgrade',
                name: 'Rolling Upgrade',
                fieldName: 'rollingUpgrade',
                fieldLabel: 'Perform Rolling Upgrade',
                fieldContext: 'config',
                inputType: OptionType.InputType.CHECKBOX,
                required: false,
                displayOrder: 2
            ),
            new OptionType(
                code: 'maintenanceWindowMinutes',
                name: 'Maintenance Window',
                fieldName: 'maintenanceWindowMinutes',
                fieldLabel: 'Maintenance Window (minutes)',
                fieldContext: 'config',
                inputType: OptionType.InputType.NUMBER,
                required: false,
                defaultValue: '60',
                displayOrder: 3
            )
        ]

        return [step]
    }

    @Override
    ServiceResponse validateWizard(Map wizardData, Map opts) {
        def errors = new FormErrors()
        def step = wizardData[STEP_CODE]

        if(!step?.targetVersion) {
            errors.addError('targetVersion', 'Target firmware version is required')
        }

        def windowMinutes = step?.maintenanceWindowMinutes
        if(windowMinutes && !(windowMinutes.toString().isInteger() && windowMinutes.toString().toInteger() > 0)) {
            errors.addError('maintenanceWindowMinutes', 'Maintenance window must be a positive number of minutes')
        }

        if(errors.hasErrors()) {
            return ServiceResponse.error('Validation failed', null, errors.getErrors())
        }

        return ServiceResponse.success()
    }

    @Override
    ServiceResponse submitWizard(Map wizardData, Map opts) {
        // The action provider does the actual work, so the wizard simply hands back the data
        return ServiceResponse.success(wizardData[STEP_CODE])
    }
}
