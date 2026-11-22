package buttondevteam.chat.components.formatter.formatting

/**
 * Attempts to find a match for the provided message, returning null if none was found.
 */
interface MatchProvider {
    @Nullable
    fun getNextSection(
        message: String?,
        ignoredAreas: ArrayList<IntArray?>?,
        removedCharacters: ArrayList<IntArray?>?
    ): FormattedSection?

    val isFinished: Boolean

    val name: String?

    override fun toString(): String

    fun reset()
}
