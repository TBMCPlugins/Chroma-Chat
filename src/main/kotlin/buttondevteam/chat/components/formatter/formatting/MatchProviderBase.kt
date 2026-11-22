package buttondevteam.chat.components.formatter.formatting

import lombok.Getter

@RequiredArgsConstructor
abstract class MatchProviderBase : MatchProvider {
    @Getter
    protected var finished: kotlin.Boolean = false

    @Getter
    private val name: kotlin.String? = null

    @Nullable
    abstract override fun getNextSection(
        message: kotlin.String?,
        ignoredAreas: java.util.ArrayList<kotlin.IntArray?>?,
        removedCharacters: java.util.ArrayList<kotlin.IntArray?>?
    ): FormattedSection?

    override fun toString(): kotlin.String {
        return name!!
    }

    protected abstract fun resetSubclass()

    override fun reset() {
        finished = false
        resetSubclass()
    }

    fun enabled(config: IHaveConfig): IConfigData<kotlin.Boolean?> {
        return config.getData<kotlin.Boolean?>(name + ".enabled", true)
    }
}
