package com.morpheusdata.system.example.wizards

import com.morpheusdata.core.Plugin
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.providers.WizardProvider
import com.morpheusdata.model.Wizard
import com.morpheusdata.model.WizardStep
import com.morpheusdata.model.OptionType
import com.morpheusdata.response.ServiceResponse
import com.morpheusdata.model.FormErrors

class SingleStepWizardProvider extends com.morpheusdata.system.example.BaseProvider implements WizardProvider {

    SingleStepWizardProvider(Plugin plugin, MorpheusContext morpheus) {
        super(plugin, morpheus)
    }

    @Override
    String getCode() {
        return 'single-step-form'
    }

    @Override
    String getWizardName() {
        return 'Single Step Form'
    }

    @Override
    String getName() {
        return getWizardName()
    }

    @Override
    List<WizardStep> getWizardSteps() {

        WizardStep step = new WizardStep(
            code: 'single-step-form',
            name: 'Single Step Form',
            description: 'Enter single step form details'
        )

        step.optionTypes = [
            new OptionType(
                code: 'name',
                name: 'Name',
                fieldName: 'name',
                fieldLabel: 'Configuration Name',
                fieldCode: 'gomorpheus.label.name', //Added diffrent locale to show diff between fieldLabel and fieldCode(translation key)
                fieldContext: 'config',
                inputType: OptionType.InputType.TEXT,
                helpBlock: 'Enter the name for this configuration',
                helpBlockFieldCode: 'gomorpheus.label.configuration',//Added diffrent locale to show diff between helpBlock and helpBlockFieldCode(translation key)
                required: true,
                displayOrder: 0
            ),
            new OptionType(
                code: 'enable',
                name: 'Enable',
                fieldName: 'enable',
                fieldCode: 'gomorpheus.label.enable',
                fieldLabel: 'Enable Feature',
                fieldContext: 'config',
                inputType: OptionType.InputType.CHECKBOX,
                required: false,
                displayOrder: 1
            )
        ]

        return [step]
    }

    @Override
    ServiceResponse validateWizard(Map wizardData, Map opts) {
        def errors = new FormErrors()

        def step = wizardData['quick-config-step']
        if(!step?.name) {
            errors.addError('name', 'Name is required')
        }

        if(errors.hasErrors()) {
            return ServiceResponse.error('Validation failed', null, errors.getErrors())
        }

        return ServiceResponse.success()
    }

    @Override
    ServiceResponse submitWizard(Map wizardData, Map opts) {
        return ServiceResponse.success(wizardData['quick-config-step'])
    }
}
