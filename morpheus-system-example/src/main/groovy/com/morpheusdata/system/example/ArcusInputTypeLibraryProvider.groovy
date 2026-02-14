package com.morpheusdata.system.example

import com.morpheusdata.core.Plugin
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.providers.AbstractInputTypeLibraryProvider
import com.morpheusdata.views.Renderer

/**
 * Provides custom input type JavaScript library for Arcus system configuration.
 * Registers multiple custom input type components (slider, toggle, rating, etc.)
 * that are built using Vite and React with Focus-UI components.
 */
class ArcusInputTypeLibraryProvider extends AbstractInputTypeLibraryProvider {

    Plugin plugin
    MorpheusContext morpheusContext

    ArcusInputTypeLibraryProvider(Plugin plugin, MorpheusContext morpheusContext) {
        this.plugin = plugin
        this.morpheusContext = morpheusContext
    }

    @Override
    Plugin getPlugin() {
        return plugin
    }

    @Override
    MorpheusContext getMorpheus() {
        return morpheusContext
    }

    @Override
    Renderer<?> getRenderer() {
        return null
    }

    @Override
    String getCode() {
        return 'arcus-input-types'
    }

    @Override
    String getName() {
        return 'Arcus Custom Input Types'
    }

    @Override
    String getLibraryScriptPath(Map<String, Object> opts) {
        return '/arcus-input-types.js'
    }
    
    /**
     * Optional load priority for loading order control.
     * Lower numbers load first.
     */
    Integer getLoadPriority() {
        return 0
    }
}
