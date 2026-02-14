package com.morpheusdata.system.example.wizards

import com.morpheusdata.core.providers.WizardProvider
import com.morpheusdata.model.FormErrors
import com.morpheusdata.model.WizardStep
import com.morpheusdata.model.OptionType
import com.morpheusdata.response.ServiceResponse

class ClusterConfigWizardProvider extends com.morpheusdata.system.example.BaseProvider implements WizardProvider {

    ClusterConfigWizardProvider(com.morpheusdata.core.Plugin plugin, com.morpheusdata.core.MorpheusContext morpheusContext) {
        super(plugin, morpheusContext)
    }

    @Override
    String getCode() {
        return 'arcus-cluster-config-wizard'
    }

    @Override
    String getName() {
        return 'Cluster Configuration Wizard'
    }

    @Override
    String getWizardName() {
        return 'Cluster Configuration'
    }

    @Override
    List<WizardStep> getWizardSteps() {
        def CUSTOM_SLIDER = new OptionType.InputType("custom-slider")
        def CUSTOM_TOGGLE = new OptionType.InputType("custom-toggle")
        def CUSTOM_RATING = new OptionType.InputType("custom-rating")
        
        def clusterStep = new WizardStep(
            code: 'cluster-settings',
            name: 'Cluster Settings',
            description: 'Basic cluster configuration'
        )
        
        clusterStep.optionTypes = [
            new OptionType(
                code: 'clusterName',
                name: 'Cluster Name',
                fieldName: 'clusterName',
                fieldLabel: 'Cluster Name',
                fieldContext: 'config',
                inputType: OptionType.InputType.TEXT,
                required: true,
                displayOrder: 0
            ),
            new OptionType(
                code: 'highAvailability',
                name: 'High Availability',
                fieldName: 'highAvailability',
                fieldLabel: 'High Availability Mode',
                fieldContext: 'config',
                inputType: CUSTOM_TOGGLE,
                defaultValue: true,
                helpText: 'Enable high availability clustering',
                config: groovy.json.JsonOutput.toJson([
                    onLabel: 'Enabled',
                    offLabel: 'Disabled',
                    description: 'Provides automatic failover and redundancy'
                ]),
                displayOrder: 1
            ),
            new OptionType(
                code: 'clusterPriority',
                name: 'Cluster Priority',
                fieldName: 'clusterPriority',
                fieldLabel: 'Cluster Priority Level',
                fieldContext: 'config',
                inputType: CUSTOM_RATING,
                defaultValue: '3',
                helpText: 'Set the priority level for this cluster',
                config: groovy.json.JsonOutput.toJson([
                    maxRating: 5,
                    showLabels: true,
                    labels: ['Low', 'Normal', 'High', 'Critical', 'Mission Critical'],
                    icon: '★',
                    emptyIcon: '☆'
                ]),
                required: true,
                displayOrder: 2
            )
        ]
        
        def resourcesStep = new WizardStep(
            code: 'cluster-resources',
            name: 'Resource Allocation',
            description: 'Configure cluster resources'
        )
        
        resourcesStep.optionTypes = [
            new OptionType(
                code: 'replicaCount',
                name: 'Replica Count',
                fieldName: 'replicaCount',
                fieldLabel: 'Number of Replicas',
                fieldContext: 'config',
                inputType: CUSTOM_SLIDER,
                defaultValue: '3',
                helpText: 'Number of cluster replicas for high availability',
                config: groovy.json.JsonOutput.toJson([
                    min: 1,
                    max: 9,
                    step: 1,
                    defaultValue: 3,
                    showValue: true,
                    unit: 'replicas'
                ]),
                required: true,
                displayOrder: 0
            ),
            new OptionType(
                code: 'cpuAllocation',
                name: 'CPU Allocation',
                fieldName: 'cpuAllocation',
                fieldLabel: 'CPU Cores Per Node',
                fieldContext: 'config',
                inputType: CUSTOM_SLIDER,
                defaultValue: '8',
                helpText: 'Number of CPU cores allocated to each cluster node',
                config: groovy.json.JsonOutput.toJson([
                    min: 2,
                    max: 64,
                    step: 2,
                    defaultValue: 8,
                    showValue: true,
                    unit: 'cores'
                ]),
                required: true,
                displayOrder: 1
            ),
            new OptionType(
                code: 'memoryAllocation',
                name: 'Memory Allocation',
                fieldName: 'memoryAllocation',
                fieldLabel: 'Memory Per Node',
                fieldContext: 'config',
                inputType: CUSTOM_SLIDER,
                defaultValue: '32',
                helpText: 'Memory allocated to each cluster node',
                config: groovy.json.JsonOutput.toJson([
                    min: 8,
                    max: 512,
                    step: 8,
                    defaultValue: 32,
                    showValue: true,
                    unit: 'GB'
                ]),
                required: true,
                displayOrder: 2
            )
        ]
        
        return [clusterStep, resourcesStep]
    }

    @Override
    ServiceResponse validateWizard(Map wizardData, Map opts) {
        FormErrors formErrors = new FormErrors()
        
        def cluster = wizardData['cluster-settings']
        if (!cluster || !cluster['clusterName']) {
            formErrors.addError('clusterName', 'Cluster name is required')
        }
        
        def resources = wizardData['cluster-resources']
        if (!resources) {
            formErrors.addError('cluster-resources', 'Resource allocation is required')
        }
        
        if (formErrors.hasErrors()) {
            return ServiceResponse.error('Validation failed', null, formErrors.getErrors())
        }
        
        return ServiceResponse.success()
    }

    @Override
    ServiceResponse submitWizard(Map wizardData, Map opts) {
        def result = [:]
        result.putAll(wizardData['cluster-settings'] ?: [:])
        result.putAll(wizardData['cluster-resources'] ?: [:])
        return ServiceResponse.success(result)
    }
}
