package com.morpheusdata.system.example.workflow

import com.morpheusdata.core.providers.ConfigurationWorkflowProvider
import com.morpheusdata.model.ConfigurationWorkflowStep
import com.morpheusdata.response.ServiceResponse
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * Configuration workflow provider for Arcus system setup
 * Manages the complete configuration workflow from system setup through prechecks
 */
class ArcusSystemConfigurationWorkflowProvider extends com.morpheusdata.system.example.BaseProvider implements ConfigurationWorkflowProvider {

    ArcusSystemConfigurationWorkflowProvider(com.morpheusdata.core.Plugin plugin, com.morpheusdata.core.MorpheusContext morpheusContext) {
        super(plugin, morpheusContext)
    }

    @Override
    String getCode() {
        return 'arcus-system-configuration-workflow'
    }

    @Override
    String getName() {
        return 'Arcus System Configuration Workflow'
    }

    @Override
    String getWorkflowName() {
        return 'Arcus System Configuration'
    }

    @Override
    String getWorkflowDescription() {
        return 'Complete configuration workflow for configuring an Arcus infrastructure system'
    }

    @Override
    List<ConfigurationWorkflowStep> getWorkflowSteps() {
        // Note: Wizard providers are referenced using direct object references
        // In a real implementation, you would retrieve these wizard objects from MorpheusContext
        // For example: def systemWizard = morpheusContext.getWizard().find(new DataQuery().withFilter('code', 'arcus-system-config-wizard')).blockingGet()
        
        // For this example, we're demonstrating the structure
        // In practice, retrieve the actual Wizard objects from your plugin's wizard providers
        def systemWizard = getWizardByCode('arcus-system-config-wizard')
        def switchWizard = getWizardByCode('arcus-switch-config-wizard')
        def hostWizard = getWizardByCode('arcus-host-config-wizard')
        def storageWizard = getWizardByCode('arcus-storage-config-wizard')
        def dataNetworkWizard = getWizardByCode('arcus-datanetwork-config-wizard')
        def clusterWizard = getWizardByCode('arcus-cluster-config-wizard')
        def prechecksWizard = getWizardByCode('arcus-prechecks-wizard')
        def singleStepWizard = getWizardByCode('single-step-form')
        def formValidationWizard = getWizardByCode('form-validation-example')

        return [
            new ConfigurationWorkflowStep(
                code: 'system',
                name: 'System',
                description: 'Configure basic system settings',
                 dependsOn: [], // This step depends on no other steps
                wizard: systemWizard
            ),
            new ConfigurationWorkflowStep(
                code: 'switches',
                name: 'Switches',
                description: 'Configure network switches',
                dependsOn: ['system'], // This step depends on the 'system' step being completed
                wizard: switchWizard
            ),
            new ConfigurationWorkflowStep(
                code: 'hosts',
                name: 'Hosts',
                description: 'Configure host servers',
                dependsOn: ['system', 'switches'], // This step depends on the 'system' and 'switches' steps being completed
                wizard: hostWizard
            ),
            new ConfigurationWorkflowStep(
                code: 'storage',
                name: 'Storage',
                description: 'Configure storage arrays',
                dependsOn: ['system', 'switches', 'hosts'], // This step depends on the 'system', 'switches', and 'hosts' steps being completed
                wizard: storageWizard
            ),
            new ConfigurationWorkflowStep(
                code: 'data-network',
                name: 'Data Network',
                description: 'Configure data network settings',
                dependsOn: ['system', 'switches', 'hosts', 'storage'], // This step depends on the 'system', 'switches', and 'hosts' steps being completed
                wizard: dataNetworkWizard
            ),
            new ConfigurationWorkflowStep(
                code: 'cluster',
                name: 'Cluster',
                description: 'Configure cluster settings',
                dependsOn: ['system', 'switches', 'hosts', 'storage', 'data-network'], // This step depends on the 'system', 'switches', and 'hosts' steps being completed
                wizard: clusterWizard
            ),
            new ConfigurationWorkflowStep(
                code: 'prechecks',
                name: 'Prechecks',
                description: 'Run system validation prechecks',
                dependsOn: ['system', 'switches', 'hosts', 'storage', 'data-network', 'cluster'], // This step depends on all previous steps being completed
                wizard: prechecksWizard
            ),
            new ConfigurationWorkflowStep(
                code: 'single-step-form',
                name: 'Single Step Form',
                description: 'Single step configuration step for users',
                dependsOn: [], // This step has no dependencies and can be completed at any time
                wizard: singleStepWizard
            ),
            new ConfigurationWorkflowStep(
                code: 'form-validation',
                name: 'Form Field Groups',
                description: 'Demonstrates field group titles and group-level descriptions',
                dependsOn: [], // This step has no dependencies and can be completed at any time
                wizard: formValidationWizard
            )
        ]
    }

    @Override
    ServiceResponse saveStepConfiguration(String stepCode, Map stepData, Map currentState, Map opts) {
        // Merge the step data into the current state

        def updatedState = currentState ? [:] + currentState : [:]

        updatedState[stepCode] = stepData
        updatedState.lastCompletedStep = stepCode
        updatedState.lastUpdated = new Date()
        updatedState.status = 'pending'
        updatedState.completed = false

        return ServiceResponse.success([
            workflowState: updatedState
        ])
    }

    @Override
    ServiceResponse updateParentState(Object parentObject, Map configurationWorkflowState, Map opts) {
        if (!parentObject) {
            return ServiceResponse.error('Missing parent object')
        }

        if (!parentObject.metaClass.respondsTo(parentObject, 'setConfigurationWorkflowState', String)) {
            return ServiceResponse.error("Invalid parent object type: ${parentObject.getClass().name}")
        }

        configurationWorkflowState.status = configurationWorkflowState.status ?: 'incomplete'
        configurationWorkflowState.completed = configurationWorkflowState.completed ?: false

        def jsonState = JsonOutput.toJson(configurationWorkflowState)
        parentObject.setConfigurationWorkflowState(jsonState)

        parentObject.status = 'uninitialized'
        parentObject.statusMessage = 'Configuration workflow in progress'
        parentObject.save(flush: true, failOnError: true)
    }

    @Override
    ServiceResponse validateConfigurationWorkflow(Map configurationWorkflowState, Object parentObject, Map opts) {
        def errors = []
        
        // Validate that all required steps are completed
        def requiredSteps = ['system', 'switches', 'hosts', 'storage', 'data-network', 'cluster']
        requiredSteps.each { step ->
            if (!configurationWorkflowState[step]) {
                errors << "Step '${step}' must be completed before submission"
            }
        }
        
        // Validate system configuration
        if (configurationWorkflowState['system']) {
            def systemConfig = configurationWorkflowState['system']
            if (!systemConfig['systemName']) {
                errors << 'System name is required'
            }
        }
        
        if (errors) {
            return ServiceResponse.error(errors.join(', '))
        }
        
        return ServiceResponse.success()
    }

    @Override
    ServiceResponse submitConfigurationWorkflow(Map configurationWorkflowState, Object parentObject, Map opts) {
        // This would typically call another long-running method to execute the setup
        // For now, we'll just mark it as submitted
        if (!parentObject) {
            return ServiceResponse.error('Missing parent object')
        }

        if (!parentObject.metaClass.respondsTo(parentObject, 'setConfigurationWorkflowState', String)) {
            return ServiceResponse.error("Invalid parent object type: ${parentObject.getClass().name}")
        }
        
        // Update configurationWorkflowState with submission info
        configurationWorkflowState.status = 'completed'
        configurationWorkflowState.submittedDate = new Date()
        configurationWorkflowState.completed = true
        configurationWorkflowState.lastUpdated = new Date()

        parentObject.status = 'ok'
        parentObject.statusMessage = null

        // Update the system's configurationWorkflowState
        def jsonState = JsonOutput.toJson(configurationWorkflowState)
        parentObject.setConfigurationWorkflowState(jsonState)
        parentObject.save(flush: true, failOnError: true)

        // In a real implementation, this would trigger the actual setup process
        // For example: executeSystemSetup(system, configurationWorkflowState)
        return ServiceResponse.success([
            message: 'Arcus system configuration submitted successfully',
            systemId: parentObject.hasProperty('id') ? parentObject.id : null,
            workflowState: configurationWorkflowState
        ])
    }

    @Override
    Map getConfigurationWorkflowState(Object parentObject, Map opts) {
        if (!parentObject) {
            return [:]
        }

        if (!parentObject.metaClass.respondsTo(parentObject, 'getConfigurationWorkflowState')) {
            return [:]
        }

        def stateJson = parentObject.getConfigurationWorkflowState()
        if (stateJson) {
            return new JsonSlurper().parseText(stateJson) as Map
        }
        return [:]
    }

    @Override
    boolean shouldShowStep(ConfigurationWorkflowStep step, Map configurationWorkflowState, Map opts) {
        // All steps are always shown for this configurationWorkflow
        // Could add conditional logic here, for example:
        // - Skip cluster step if less than 2 hosts configured
        // - Skip prechecks until all other steps are completed
        return true
    }

    /**
     * Helper method to retrieve a Wizard from the provider
     * This retrieves the provider by code and calls getWizard()
     */
    private def getWizardByCode(String wizardCode) {
        // Get the WizardProvider from the plugin
        def wizardProvider = plugin.getProviderByCode(wizardCode)
        
        if (wizardProvider instanceof com.morpheusdata.core.providers.WizardProvider) {
            // Call getWizard() to get a fresh Wizard object
            return wizardProvider.getWizard()
        }
        
        // Fallback - return null if provider not found
        return null
    }
}
