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
            code: 'step 1',
            name: 'Single Step Form Step 1',
            description: 'Enter single step form details'
        )

         def StepInfoPanel = new OptionType(
            code: 'InfoPanel',
            name: 'Step Information',
            fieldName: 'StepInfoPanel',
            fieldLabel: 'Step Overview',
            fieldContext: 'config',
            inputType: new OptionType.InputType('infoPanel'), //as per core model
            required: false,
            displayOrder: 0
        )

        StepInfoPanel.setConfigMap([
            isFullWidth: true,
            variant: 'info',
            title: 'Before you run this step',
            description: 'Fill in the required fields.' //sample text, can be customized as needed
        ])

        step.optionTypes = [
            new OptionType(
                code: 'name',
                name: 'Name',
                fieldName: 'name',
                fieldLabel: 'Configuration Name',
                fieldCode: 'gomorpheus.label.name', //Added diffrent locale to show diff between fieldLabel and fieldCode(translation key)
                fieldContext: 'config',
                inputType: OptionType.InputType.TEXT,
                required: true,
                displayOrder: 1
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
                displayOrder: 2
            ),
            StepInfoPanel,
            new OptionType(
                code: 'email',
                name: 'User Email',
                fieldLabel: 'Email Address',
                fieldName: 'email',
                fieldContext: 'config',
                fieldGroup: 'User Information',
                inputType: OptionType.InputType.TEXT,
                helpText: 'Please enter a valid email address.',
                required: true,
                displayOrder: 3
            ),
            new OptionType(
                code: 'phone',
                name: 'User Phone Number',
                fieldLabel: 'Phone Number',
                fieldName: 'phone',
                fieldContext: 'config',
                fieldGroup: 'User Information',
                inputType: OptionType.InputType.TEXT,
                required: false,
                displayOrder: 4
            )
        ]
        return [step]
    }

    @Override
    ServiceResponse validateWizard(Map wizardData, Map opts) {
        def errors = new FormErrors()

        def step = wizardData['step 1']
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
        return ServiceResponse.success(wizardData['step 1'])
    }
}
