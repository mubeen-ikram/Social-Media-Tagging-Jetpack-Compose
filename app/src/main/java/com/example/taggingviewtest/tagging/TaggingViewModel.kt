package com.example.taggingviewtest.Tagging

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.example.taggingviewtest.Tagging.customValues.TEXT_CHARACTER_LIMIT
import com.example.taggingviewtest.Tagging.customValues.UPDATE_TAG_NAME_FOR_NAME_UPDATE
import com.example.taggingviewtest.Tagging.customValues.defaultTaggedTextColor
import com.example.taggingviewtest.Tagging.data.TagPopUpUiState
import com.example.taggingviewtest.Tagging.data.TaggedItem
import com.example.taggingviewtest.Tagging.data.TaggingViewState
import com.example.taggingviewtest.Tagging.data.TextEditState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update


open class TaggingViewModel : ViewModel() {
    /**
     * for tag character and space at the end
     */
    private val TAGGING_OFFSET = 2
    val commentUiState = MutableStateFlow(
        TaggingViewState(
            textField = TextFieldValue(""),
            taggedItems = mutableListOf(),
            loading = false,
            errorString = "",
            enable = false,
        )
    )
    private val tagPopUpUiState = MutableStateFlow(
        TagPopUpUiState(
            show = false,
            mutableListOf()
        )
    )
    private val currentSearch = MutableStateFlow("")
    private var funcOnFilter: (String, TaggedItem) -> Boolean = { search, item ->
        item.name?.contains(search, ignoreCase = true) ?: true
    }

    val filteredTagsUiState = tagPopUpUiState.combine(currentSearch) { state, search ->
        TagPopUpUiState(
            show = state.show,
            taggingList = if (search.isBlank()) state.taggingList else state.taggingList.filter {
                funcOnFilter(search, it)
            }
        )
    }
    private val tagChar = MutableStateFlow('@')
    private val tempTagEndIndex = MutableStateFlow(-1)
    private val tempTagStartIndex = MutableStateFlow(-1)

    fun initTaggingViews(
        taggingList: MutableList<TaggedItem>,
        enabled: Boolean,
        tagCharInput: Char = '@',
        onFilter: (String, TaggedItem) -> Boolean = { search, item ->
            item.name?.contains(search, ignoreCase = true) ?: true
        }
    ) {
        commentUiState.update { it.copy(enable = enabled) }
        tagPopUpUiState.update { it.copy(taggingList = taggingList) }
        tagChar.update { tagCharInput }
        funcOnFilter = onFilter
    }


    fun onRepositoryCalled() {
        commentUiState.update { it.copy(loading = true, enable = false) }
        tagPopUpUiState.update { it.copy(show = false) }
    }

    fun onFailure() {
        commentUiState.update { it.copy(loading = false, enable = true) }
        tagPopUpUiState.update { it.copy(show = false) }
    }

    fun onSuccess() {
        commentUiState.update {
            it.copy(
                loading = false,
                enable = true,
                textField = TextFieldValue(""),
                taggedItems = mutableListOf()
            )
        }
        tagPopUpUiState.update { it.copy(show = false) }
        currentSearch.update { "" }
    }

    /**
     * Handles the changes in the text field value, updating the UI state and managing tag states.
     *
     * @param newValue The new value of the text field.
     */
    fun onInputValueChanged(newValue: TextFieldValue) {
        val newText = newValue.text
        val previousValue = commentUiState.value.textField.text
        when (val textState = checkStringStatus(newText, previousValue)) {
            TextEditState.Add -> {
                tempTagEndIndex.update { newText.length - 1 }
                when {
                    isAddedCharacterTagChar(newText) -> {
                        showTagForName("")
                        tempTagStartIndex.update { newText.length - 1 }
                    }

                    isCharactersAfterTagCharAdd(newText) -> {
                        tempTagStartIndex.update {
                            updateTempTagStartIndex(
                                newText,
                                newText.length - 1
                            )
                        }
                        updateAndSetTagName(newText, newText.length - 1)
                    }

                    else -> {
                        hideTagPopUp()
                    }
                }
                commentUiState.update { it.copy(textField = newValue) }
            }

            is TextEditState.Edit -> {
                val characterChangeIndex = textState.indexChanged
                tempTagEndIndex.update { characterChangeIndex }
                if (isCharactersAfterTagCharEdit(newText, characterChangeIndex)) {
                    tempTagStartIndex.update {
                        updateTempTagStartIndex(
                            newText,
                            characterChangeIndex
                        )
                    }
                    updateAndSetTagName(newText, characterChangeIndex)
                } else {
                    hideTagPopUp()
                }
                handleTaggedItemAfterEdit(newValue, characterChangeIndex, textState)
            }

            is TextEditState.Removed -> {
                val indexChanged = textState.indexChanged
                val changeSize = textState.changedLength

                tempTagEndIndex.update { indexChanged - changeSize }
                handleIndicesOnTextRemove(previousValue, indexChanged, changeSize)

                val taggingItemsList =
                    mutableListOf(*commentUiState.value.taggedItems.toTypedArray()).sortedBy { it.indexStart }
                        .toMutableList()

                val itemsToRemove = getItemsToRemove(taggingItemsList, indexChanged, changeSize)
                val updatedTaggingItemsList = itemsAfterRemoving(itemsToRemove, taggingItemsList)
                for (item in updatedTaggingItemsList) {
                    if (indexChanged < item.indexStart) {
                        item.indexStart -= changeSize
                        item.indexEnd -= changeSize
                    }
                }
                commentUiState.update {
                    it.copy(
                        textField = newValue,
                        taggedItems = taggingItemsList
                    )
                }
            }

            else -> {
                updateTextSelection(newValue)
            }
        }
    }

    private fun checkStringStatus(newMessage: String, previousMessage: String): TextEditState {
        return when {
            newMessage.length == previousMessage.length -> TextEditState.Default
            newMessage.length > previousMessage.length -> {
                if (previousMessage.isEmpty() || newMessage.last() != previousMessage.last()) {
                    TextEditState.Add
                } else {
                    val editIndex = newMessage.asSequence()
                        .withIndex()
                        .firstOrNull { (i, char) ->
                            i >= previousMessage.length || char != previousMessage[i]
                        }?.index ?: -1
                    val changedLength = newMessage.length - previousMessage.length
                    TextEditState.Edit(editIndex, changedLength)
                }
            }

            else -> {
                val changeSize = previousMessage.length - newMessage.length
                for (i in newMessage.indices) {
                    if (newMessage[i] != previousMessage[i]) {
                        return TextEditState.Removed(i, changeSize)
                    }
                }
                TextEditState.Removed(newMessage.length, changeSize)
            }
        }
    }

    private fun hideTagPopUp() {
        tagPopUpUiState.update { it.copy(show = false) }
        currentSearch.update { "" }
        tempTagStartIndex.update { -1 }
    }

    private fun showTagForName(name: String) {
        tagPopUpUiState.update { it.copy(show = true) }
        currentSearch.update { name }
    }

    /**
     * Applies styles to an AnnotatedString based on the tagging state.
     *
     * @param it The input AnnotatedString.
     * @param taggingState The list of tagged items indicating the ranges to style.
     * @return The resultant AnnotatedString with styles applied.
     */
    fun getResultantString(
        it: AnnotatedString,
        taggingState: MutableList<TaggedItem>
    ): AnnotatedString {
        val sortedTaggedItems = taggingState.sortedBy { item -> item.indexStart }
        val builder = AnnotatedString.Builder(it)
        for (taggedItem in sortedTaggedItems) {
            builder.addStyle(
                SpanStyle(color = defaultTaggedTextColor),
                taggedItem.indexStart,
                taggedItem.indexEnd
            )
        }
        return builder.toAnnotatedString()
    }


    /**
     * Handles the event when a item is tapped. It checks if the updated message would be within
     * limit and if not, update the comment String with The newestTag and update the commentUiState
     * with the newTag and its starting and ending indices
     *
     * @param item The tapped TaggedItem.
     */
    fun onItemTagged(item: TaggedItem) {
        if (tempTagStartIndex.value == -1)
            return

        val (start, updatedMessage) = updateCommentString(item)
        if (checkOutOfLimit(updatedMessage)) return

        val (taggingItemsList, newestTag) = updatedTagListWithLatestItem(item, start)
        updateCommentUiState(updatedMessage, start, newestTag, taggingItemsList)
        hideTagPopUp()
    }


    /**
     * Checks if edited character is  after tagChar with no space in between them.
     *
     * @param currentString The string to check.
     * @param indexChanged The index of the character that has been changed.
     * @return `true` if the changed character is after the specified Tag character with no
     * space between them, `false` otherwise.
     */
    private fun isCharactersAfterTagCharEdit(currentString: String, indexChanged: Int): Boolean {
        if (currentString[indexChanged] == tagChar.value) {
            return true
        }

        val commentBeforeChangeIndex = currentString.subSequence(0, indexChanged).toString()
        val numberOfAtBeforeEdit = commentBeforeChangeIndex.count {
            it == tagChar.value
        }
        if (numberOfAtBeforeEdit == 0)
            return false

        val subStringFromLastAt = commentBeforeChangeIndex.substringAfterLast(tagChar.value)
        return ' ' !in subStringFromLastAt
    }

    /**
     * Get the current Search UserName String based on the input String.
     *@param input The input string from which to update the current name string.
     *@param startingIndex The starting index of the current name string.
     *@param endingIndex The ending index of the current name string.
     *@return The currentTagged string based on the input string.
     */
    private fun currentTaggedString(
        input: String,
        startingIndex: Int,
        endingIndex: Int
    ): String {
        if (startingIndex == endingIndex || startingIndex > endingIndex || input[endingIndex] == tagChar.value) {
            return ""
        }
        return input.substring(startingIndex + 1, endingIndex + 1)
    }

    /**

     *Updates the temporary tag start index based on the input string and the index change for
     *  updating the temporary tag start index.

     *@param input The input string in which to update the temporary tag start index.

     *@param indexChanged The index change for updating the temporary tag start index.

     *@return The updated temporary tag start index showing the start of the tag item.
     */
    private fun updateTempTagStartIndex(input: String, indexChanged: Int): Int {
        if (input[indexChanged] == tagChar.value) {
            return indexChanged
        }

        val textBeforeIndex = input.substring(0, indexChanged)
        val lastTagIndex = textBeforeIndex.lastIndexOf(tagChar.value)
        if (lastTagIndex == -1 ||
            textBeforeIndex.substring(lastTagIndex + 1).contains(' ')
        ) {
            return -1
        }

        return lastTagIndex
    }

    /**
     * Checks if there are characters after the specified tag character in the input string.
     *
     * @param input The input string to check for characters after the tag character.
     * @return True if there are no spaces characters after the tag character, false otherwise.
     */
    private fun isCharactersAfterTagCharAdd(input: String): Boolean {
        val commentData = input.split(tagChar.value)
        if ((input.isNotEmpty() && !input.startsWith(tagChar.value) && commentData.size == 1)) {
            return false
        }
        return ' ' !in commentData.last()
    }

    /**
     * Checks if the given index falls within any tagged item's range.
     *
     * @param indexChanged The index to check.
     * @return True if the index falls within any tagged item's range, false otherwise.
     */
    private fun isIndexInTaggedName(indexChanged: Int): Boolean {
        return commentUiState.value.taggedItems.any { tagged ->
            indexChanged in (tagged.indexStart) until tagged.indexEnd
        }
    }

    /**
     * Calculates the updated text range based on the default range and the presence of tagged items.
     *
     * @param default The default text range.
     * @return The updated text range.
     */
    private fun getUpdateTextRange(default: TextRange): TextRange {
        val updateStartingIndex = findStartIndexInTaggedItems(default) ?: default.start
        val updatedEndIndex = endIndexIfContainTagged(default) ?: default.end
        return TextRange(start = updateStartingIndex, end = updatedEndIndex)
    }

    /**
     * Finds the start index within tagged items that overlaps with the specified text range.
     *
     * @param default The default text range to check.
     * @return The start index of the overlapping tagged item, or null if no overlap is found.
     */
    private fun findStartIndexInTaggedItems(default: TextRange): Int? {
        return commentUiState.value.taggedItems.find {
            default.start in it.indexStart..it.indexEnd
        }?.indexStart
    }

    /**
     * Finds the end index within tagged items that overlaps with the specified text range.
     *
     * @param default The default text range to check.
     * @return The end index of the overlapping tagged item, or null if no overlap is found.
     */
    private fun endIndexIfContainTagged(default: TextRange): Int? {
        return commentUiState.value.taggedItems.find {
            default.end in it.indexStart until it.indexEnd
        }?.indexEnd
    }


    /**
     * Updates the comment string by replacing the specified range with a tag for the given item.
     *
     * @param item The item whose tag will be inserted into the comment string.
     * @return A pair containing the start index and the updated comment string.
     * @throws IllegalArgumentException If the start or end index is out of bounds.
     */
    private fun updateCommentString(item: TaggedItem): Pair<Int, String> {
        val start = tempTagStartIndex.value
        val end = tempTagEndIndex.value
        val replacement = "${tagChar.value}${item.name}  "
        val updatedMessage =
            commentUiState.value.textField.text.replaceRange(start, end + 1, replacement)
        return Pair(start, updatedMessage)
    }

    /**
     * Checks if the updated message exceeds the character limit and updates the UI state if necessary.
     *
     * @param updatedMessage The updated message string to check.
     * @return `true` if the updated message exceeds the character limit, `false` otherwise.
     */
    private fun checkOutOfLimit(updatedMessage: String): Boolean {
        return if (updatedMessage.length > TEXT_CHARACTER_LIMIT) {
            hideTagPopUp()
            commentUiState.update {
                it.copy(
                    textField = it.textField.copy(selection = TextRange(TEXT_CHARACTER_LIMIT)),
                )
            }
            true
        } else {
            false
        }
    }

    /**
     * Updates the comment UI state with the provided updated message, selection range, newest tag, and tagging item state.
     *
     * @param updatedMessage The updated comment message.
     * @param start The start index for the text selection.
     * @param newestTag The newest tag item to be inserted.
     * @param taggingItemsList The current state of the list of tagged items.
     */
    private fun updateCommentUiState(
        updatedMessage: String,
        start: Int,
        newestTag: TaggedItem,
        taggingItemsList: MutableList<TaggedItem>
    ) {
        commentUiState.update {
            it.copy(
                textField = it.textField.copy(
                    text = updatedMessage,
                    selection = TextRange(start + newestTag.name!!.length + TAGGING_OFFSET + 1) // +1 for end space
                ),
                taggedItems = taggingItemsList,
            )
        }
    }

    /**
     * Creates a new list of tagged items with the latest tagged item included.
     *
     * @param item The item to be tagged.
     * @param start The start index for the new tag in the text.
     * @return A pair containing the updated list of tagged items and the newest tagged item.
     */
    private fun updatedTagListWithLatestItem(
        item: TaggedItem,
        start: Int
    ): Pair<MutableList<TaggedItem>, TaggedItem> {
        val taggingItemList = commentUiState.value.taggedItems.toMutableList()
        val newestTag = item.copy(
            indexStart = start,
            indexEnd = start + item.name!!.length + TAGGING_OFFSET //For last space
        )
        taggingItemList.add(newestTag)
        return Pair(taggingItemList, newestTag)
    }

    /**
     * Handles the updates to tagged items after an edit in the text field.
     *
     * @param newValue The new value of the text field.
     * @param characterChangeIndex The index of the character that changed.
     * @param textState The state of the text edit operation.
     */
    private fun handleTaggedItemAfterEdit(
        newValue: TextFieldValue,
        characterChangeIndex: Int,
        textState: TextEditState
    ) {
        if (textState !is TextEditState.Edit) return

        if (commentUiState.value.taggedItems.isEmpty())
            commentUiState.update { it.copy(textField = newValue) }
        else {
            if (isIndexInTaggedName(characterChangeIndex)) return

            updateTagIndexesInTagItems(textState, characterChangeIndex, newValue)
        }
    }

    private fun isAddedCharacterTagChar(newText: String) = newText.last() == tagChar.value

    /**
     * Updates the text selection range in the UI state based on the provided new value.
     *
     * @param newValue The new value of the text field.
     */
    private fun updateTextSelection(newValue: TextFieldValue) {
        val range = getUpdateTextRange(default = newValue.selection)
        commentUiState.update {
            it.copy(textField = newValue.copy(selection = range))
        }
    }

    /**
     * Removes specified tagged items from the tagging items list.
     *
     * @param itemsToRemove The list of tagged items to be removed.
     * @param taggingItemsList The mutable list of tagged items.
     * @return The updated list of tagged items after removal.
     */
    private fun itemsAfterRemoving(
        itemsToRemove: List<TaggedItem>,
        taggingItemsList: MutableList<TaggedItem>
    ): MutableList<TaggedItem> {
        if (commentUiState.value.taggedItems.isNotEmpty() && itemsToRemove.isNotEmpty())
            itemsToRemove.forEach { item ->
                taggingItemsList.remove(item)
            }
        return taggingItemsList
    }

    /**
     * Retrieves tagged items that need to be removed based on the change in text.
     *
     * @param taggingItemList The list of tagged items.
     * @param indexChanged The index at which the change occurred in the text.
     * @param changeSize The size of the change in text.
     * @return The list of tagged items that need to be removed.
     */
    private fun getItemsToRemove(
        taggingItemList: List<TaggedItem>,
        indexChanged: Int,
        changeSize: Int
    ): List<TaggedItem> {
        return taggingItemList.filter { item ->
            Integer.max(item.indexStart + 1, indexChanged) < Integer.min(
                item.indexEnd,
                indexChanged + changeSize
            )
        }
    }

    /**
     * Handles the adjustments needed in various indices when text is removed from the comment.
     *
     * @param previousValue The previous value of the comment text.
     * @param indexChanged The index at which the change occurred.
     * @param changeSize The size of the change (number of characters removed).
     */
    private fun handleIndicesOnTextRemove(
        previousValue: String,
        indexChanged: Int,
        changeSize: Int
    ) {
        when {
            previousValue.subSequence(
                indexChanged, indexChanged + changeSize
            ).contains(tagChar.value) -> hideTagPopUp()

            isCharactersAfterTagCharEdit(previousValue, indexChanged) -> {
                tempTagStartIndex.update {
                    updateTempTagStartIndex(
                        previousValue,
                        indexChanged
                    )
                }
                updateAndSetTagName(previousValue, tempTagEndIndex.value)
            }

            else -> hideTagPopUp()

        }
    }

    /**
     * Updates the tag indexes in the list of tagged items after a text edit. and add the indices of item tags after the index where the change is made
     *
     * @param textState The state of the text edit operation.
     * @param characterChangeIndex The index of the character that changed.
     * @param newValue The new value of the text field.
     */
    private fun updateTagIndexesInTagItems(
        textState: TextEditState.Edit,
        characterChangeIndex: Int,
        newValue: TextFieldValue
    ) {
        val changeSize = textState.changedLength
        val taggedItems = mutableListOf(*commentUiState.value.taggedItems.toTypedArray())
        taggedItems.forEach { item ->
            if (item.indexStart > characterChangeIndex) {
                item.indexStart += changeSize
                item.indexEnd += changeSize
            }
        }

        commentUiState.update {
            it.copy(
                textField = newValue,
                taggedItems = taggedItems
            )
        }
    }

    /**
     * Updates the tag name based on the current text and shows the tag popup.
     *
     * @param newText The new text after the change.
     * @param characterChangeIndex The index of the character that changed.
     */
    private fun updateAndSetTagName(newText: String, characterChangeIndex: Int) {
        val currentNameString = currentTaggedString(
            newText,
            tempTagStartIndex.value,
            characterChangeIndex
        )
        showTagForName(currentNameString)
    }

    /**
     * Replaces tagged user names in the input text with a formatted string containing their category and ID.
     * The tagged user names are expected to be prefixed by a specific tag character and followed by a space.
     *
     * @param inputText The input text containing tagged user names.
     * @param taggedUsers The list of tagged items representing users.
     * @return The updated text with tagged user names replaced by their formatted string representation.
     */
    private fun removeSymbolBeforeTaggingName(
        inputText: String,
        taggedUsers: List<TaggedItem>
    ): String {
        var commentCopy = inputText
        taggedUsers.sortedBy { it.indexEnd }.reversed().forEach { item ->
            if (item.name != null && commentCopy.contains(item.name)) {
                val tagString = "${tagChar.value}${item.name} "
                val replaceString =
                    if (UPDATE_TAG_NAME_FOR_NAME_UPDATE) "{${item.id}_${item.id}}" else item.name
                commentCopy = commentCopy.replace(tagString, replaceString)
            }
        }
        return commentCopy
    }
}