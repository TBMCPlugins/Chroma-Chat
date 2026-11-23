package buttondevteam.chat.components.formatter.formatting

import buttondevteam.lib.architecture.IHaveConfig
import buttondevteam.lib.architecture.config.IConfigData

abstract class MatchProviderBase(override val name: String) : MatchProvider {
    override var isFinished: Boolean = false
        protected set

    abstract override fun getNextSection(
        message: String,
        ignoredAreas: MutableList<IntArray>,
        removedCharacters: MutableList<IntArray>
    ): FormattedSection?

    override fun toString(): String {
        return name
    }

    protected abstract fun resetSubclass()

    override fun reset() {
        isFinished = false
        resetSubclass()
    }

    fun enabled(config: IHaveConfig): IConfigData<Boolean> {
        return config.getData("$name.enabled", true)
    }
}
