package com.morpheusdata.system.example

import com.morpheusdata.core.providers.AbstractSystemTabProvider
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.model.Account
import com.morpheusdata.model.User
import com.morpheusdata.views.HTMLResponse
import com.morpheusdata.views.ViewModel

import com.morpheusdata.model.system.System

class SystemExampleCustomSummaryTabProvider extends AbstractSystemTabProvider {

	Plugin plugin
    MorpheusContext morpheusContext

    SystemExampleCustomSummaryTabProvider(Plugin plugin, MorpheusContext morpheusContext) {
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

	String code = "arcus-system-example-tab"
	String name = "Summary Tab"

	@Override
	String getCode() { code }

	@Override
	String getName() { name }

	@Override
	Boolean show(System system, User user, Account account) {
		return true
	}

	@Override
	HTMLResponse renderTemplate(System system) {
		ViewModel<System> model = new ViewModel<>()
		model.object = system
		return getRenderer().renderTemplate("hbs/tabs/summaryTab", model)
	}
}