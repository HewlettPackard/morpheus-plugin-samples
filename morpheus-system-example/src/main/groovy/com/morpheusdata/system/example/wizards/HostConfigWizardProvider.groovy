package com.morpheusdata.system.example.wizards

import com.morpheusdata.core.providers.WizardProvider
import com.morpheusdata.model.FormErrors
import com.morpheusdata.model.WizardStep
import com.morpheusdata.model.OptionType
import com.morpheusdata.response.ServiceResponse

class HostConfigWizardProvider extends com.morpheusdata.system.example.BaseProvider implements WizardProvider {

    HostConfigWizardProvider(com.morpheusdata.core.Plugin plugin, com.morpheusdata.core.MorpheusContext morpheusContext) {
        super(plugin, morpheusContext)
    }

    @Override
    String getCode() {
        return 'arcus-host-config-wizard'
    }

    @Override
    String getName() {
        return 'Host Configuration Wizard'
    }

    @Override
    String getWizardName() {
        return 'Host Configuration'
    }

    @Override
    List<WizardStep> getWizardSteps() {
        def CUSTOM_HOST_TABLE = new OptionType.InputType("custom-host-table")

        def hostStep = new WizardStep(
            code: 'host-list',
            name: 'Hosts',
            description: 'Review and configure the ESXi hosts listed below from your Private Cloud Installer setup. Verify the information is correct and update networking, credentials, or other settings as needed before proceeding.'
        )

        def hostTableOptionType =  new OptionType(
                code: 'hostsTable',
                name: 'Cluster Hosts',
                fieldName: 'hostsTable',
                fieldLabel: 'Cluster Hosts',
                fieldContext: 'config',
                inputType: CUSTOM_HOST_TABLE,
                required: true,
                displayOrder: 0,
                helpText: 'Review and configure host management IPs, FQDN, and iLO addresses before proceeding.'
            );
        hostTableOptionType.setConfigMap([
            isFullWidth: true
        ])
        
        hostStep.optionTypes = [
            hostTableOptionType
           
        ]
        
        def credentialsStep = new WizardStep(
            code: 'host-credentials',
            name: 'Host Credentials',
            description: 'IPMI/iLO credentials for host management'
        )
        
        credentialsStep.optionTypes = [
            new OptionType(
                code: 'ipmiUsername',
                name: 'IPMI Username',
                fieldName: 'ipmiUsername',
                fieldLabel: 'IPMI Username',
                fieldCode: 'gomorpheus.label.username', // Example of using an i18n code for the label, this key can come from plugin locales
                helpText: 'Username for IPMI or iLO management interface',
                fieldContext: 'config',
                inputType: OptionType.InputType.TEXT,
                required: true,
                displayOrder: 0
            ),
            new OptionType(
                code: 'ipmiPassword',
                name: 'IPMI Password',
                fieldName: 'ipmiPassword',
                fieldLabel: 'IPMI Password',
                fieldCode: 'gomorpheus.label.password', // Example of using an i18n code for the label, this key can come from plugin locales
                helpText: 'Password for IPMI or iLO management interface',
                fieldContext: 'config',
                inputType: OptionType.InputType.PASSWORD,
                required: true,
                displayOrder: 1
            )
        ]
        
        def networkStep = new WizardStep(
            code: 'host-network',
            name: 'Network Configuration',
            description: 'Host network settings'
        )
        
        networkStep.optionTypes = [
            new OptionType(
                code: 'networkSubnet',
                name: 'Management Subnet',
                fieldName: 'networkSubnet',
                fieldLabel: 'Management Subnet (CIDR)',
                fieldContext: 'config',
                inputType: OptionType.InputType.TEXT,
                required: true,
                displayOrder: 0
            ),
            new OptionType(
                code: 'gateway',
                name: 'Gateway',
                fieldName: 'gateway',
                fieldLabel: 'Default Gateway',
                fieldContext: 'config',
                inputType: OptionType.InputType.TEXT,
                required: true,
                displayOrder: 1
            )
        ]
        
        return [hostStep, credentialsStep, networkStep]
    }

    @Override
    ServiceResponse validateWizard(Map wizardData, Map opts) {
        FormErrors formErrors = new FormErrors()
        
        def hostList = wizardData['host-list']
        if (!hostList || !hostList['hostsTable']) {
            formErrors.addError('hostsTable', 'At least one host row is required')
        }
        
        def credentials = wizardData['host-credentials']
        if (!credentials) {
            formErrors.addError('host-credentials', 'Host credentials are required')
        }
        
        def network = wizardData['host-network']
        if (!network) {
            formErrors.addError('host-network', 'Network configuration is required')
        }
        
        if (formErrors.hasErrors()) {
            return ServiceResponse.error('Validation failed', null, formErrors.getErrors())
        }
        
        return ServiceResponse.success()
    }

    @Override
    ServiceResponse submitWizard(Map wizardData, Map opts) {
        def result = [:]
        result.putAll(wizardData['host-list'] ?: [:])
        result.putAll(wizardData['host-credentials'] ?: [:])
        result.putAll(wizardData['host-network'] ?: [:])
        return ServiceResponse.success(result)
    }
}
