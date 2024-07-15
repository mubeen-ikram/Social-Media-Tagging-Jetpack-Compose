TaggingViewModel Readme
=======================

Overview
--------

TaggingViewModel is a ViewModel designed to handle tagging functionality within a text input field
in an Android application using Jetpack Compose. This ViewModel manages the state related to
tagging, including the list of tagged items, popup UI state for suggestions, and text editing
states.

Table of Contents
-----------------

* [TaggingViewModel Readme](#taggingviewmodel-readme)

    * [Overview](#overview)

    * [Initialization](#initialization)

    * [State Variables](#state-variables)

    * [Functions](#functions)

        * [Initialization Functions](#initialization-functions)

        * [Text Handling Functions](#text-handling-functions)

        * [Tag Popup Functions](#tag-popup-functions)

        * [Tagged Items Functions](#tagged-items-functions)

        * [Utility Functions](#utility-functions)

    * [Usage Example](#usage-example)

Initialization
--------------

To use TaggingViewModel, you need to initialize it with the tagging list, enabling flag, and
optional tag character and filter function.

Plain

```kotlin
viewModel.initViewModel(
  taggingList = mutableListOf(),
  // List of TaggedItem    
  // enabled = true,                 
  // Boolean to enable or disable tagging   
  // tagCharInput = '@',             
  // Character used for tagging    
  // onFilter = { search, item ->    // Filter function for suggestions          item.name?.contains(search, ignoreCase = true) ?: true      }  
   )
``` 

State Variables
---------------

* commentUiState: A MutableStateFlow managing the UI state of the text field, including the text,
  tagged items, loading state, error message, and enable flag.

* tagPopUpUiState: A MutableStateFlow managing the UI state of the tag popup, including whether it
  is shown and the list of tagging items.

* currentSearch: A MutableStateFlow storing the current search query for filtering tags.

* filteredTagsUiState: A StateFlow combining tagPopUpUiState and currentSearch to filter and show
  the list of tagging items.

* tagChar: A MutableStateFlow storing the character used for tagging.

* tempTagEndIndex: A MutableStateFlow storing the temporary end index of the current tag.

* tempTagStartIndex: A MutableStateFlow storing the temporary start index of the current tag.

Functions
---------

### Initialization Functions

* initViewModel(taggingList: MutableList, enabled: Boolean, tagCharInput: Char = '@', onFilter: (
  String, TaggedItem) -> Boolean): Initializes the ViewModel with the tagging list, enable flag, tag
  character, and filter function.

### Text Handling Functions

* onValueChange(newValue: TextFieldValue): Handles changes in the text field value, updating the UI
  state and managing tag states.

* checkStringStatus(newMessage: String, previousMessage: String): TextEditState: Checks the state of
  the text (added, edited, or removed).

* handleTaggedItemAfterEdit(newValue: TextFieldValue, characterChangeIndex: Int, textState:
  TextEditState): Handles updates to tagged items after an edit in the text field.

* updateTextSelection(newValue: TextFieldValue): Updates the text selection range in the UI state
  based on the provided new value.

### Tag Popup Functions

* showTagForName(name: String): Shows the tag popup for a given name.

* hideTagPopUp(): Hides the tag popup.

### Tagged Items Functions

* onItemTagged(item: TaggedItem): Handles the event when an item is tapped, updating the text field
  and tagged items.

* updateCommentUiState(updatedMessage: String, start: Int, newestTag: TaggedItem, taggingItemsList:
  MutableList): Updates the UI state with the provided updated message, selection range, newest tag,
  and tagging item state.

* updatedTagListWithLatestItem(item: TaggedItem, start: Int): Pair, TaggedItem>: Creates a new list
  of tagged items with the latest tagged item included.

* itemsAfterRemoving(itemsToRemove: List, taggingItemsList: MutableList): MutableList: Removes
  specified tagged items from the tagging items list.

### Utility Functions

* getResultantString(it: AnnotatedString, taggingState: MutableList): AnnotatedString: Applies
  styles to an AnnotatedString based on the tagging state.

* removeSymbolBeforeTaggingName(inputText: String, taggedUsers: List): String: Replaces tagged user
  names in the input text with a formatted string containing their category and ID.

Usage Example
-------------

Below is an example of how to use TaggingViewModel within a Composable function.

```kotlin   
@Composable
fun TaggingTextField(viewModel: TaggingViewModel = viewModel()) 
{ 
    val uiState by viewModel.commentUiState.collectAsState()
  val popupState by viewModel.filteredTagsUiState.collectAsState()
  Column { 
      TextField(       
        value = uiState.textField,
        onValueChange = { viewModel.onValueChange(it) },
        modifier = Modifier.fillMaxWidth()   
      ) 
    if (popupState.show) { DropdownMenu( 
      expanded = popupState.show, onDismissRequest = { viewModel.hideTagPopUp() } 
    ) { popupState.taggingList.forEach { item ->
        DropdownMenuItem(onClick = { viewModel.onItemTagged(item) }) { Text(text = item.name ?: "")
        }
    }
    }
    }
  }
}   
```

This example demonstrates a simple text field with tagging functionality. The TaggingViewModel
handles the state and logic for tagging, including showing a dropdown menu for tag suggestions.