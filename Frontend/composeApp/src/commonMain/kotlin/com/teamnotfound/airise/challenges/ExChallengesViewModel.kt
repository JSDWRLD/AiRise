package com.teamnotfound.airise.challenges

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

//example of viewmodel with example data
class ExChallengesViewModel :
    ViewModel(),
    ChallengesViewModel {

        //example data for list of challenges
    private val _ui = MutableStateFlow(
        ChallengesUiState(
            items = listOf(
                ChallengeUI(
                    id = "1",
                    name = "Challenge 1",
                    description = "Daily steps to 10k",
                    imageUrl = "https://picsum.photos/seed/steps/800/400"
                ),
                ChallengeUI(
                    id = "2",
                    name = "Challenge 2",
                    description = "Drink 8 cups of water",
                    imageUrl = "https://picsum.photos/seed/water/800/400"
                ),
                ChallengeUI(
                    id = "3",
                    name = "Challenge 3",
                    description = "Finish a 3-mile run",
                    imageUrl = "https://picsum.photos/seed/run/800/400"
                )
            )
        )
    )

    override val uiState: StateFlow<ChallengesUiState> = _ui.asStateFlow()

    //track current selected challenge for the details screen
    private val _selected = MutableStateFlow<ChallengeUI?>(null)
    val selected: StateFlow<ChallengeUI?> = _selected.asStateFlow()

    //just for testing
    override fun refresh() {
        _ui.value = _ui.value.copy(isLoading = false)
    }

    //for card clicking of each challenge
    override fun onChallengeClick(id: String) {
        select(id)
    }

    //select challenge by id
    fun select(id: String) {
        _selected.value = _ui.value.items.firstOrNull { it.id == id }
    }

    //clear current selection
    fun clearSelection() {
        _selected.value = null
    }

    //update description of challenge
    fun updateSelectedDescription(newDesc: String) {
        val current = _selected.value ?: return
        val updated = current.copy(description = newDesc)
        _selected.value = updated
        _ui.value = _ui.value.copy(
            items = _ui.value.items.map { if (it.id == current.id) updated else it }
        )
    }

    //add challenge to list
    fun addChallenge(name: String, description: String) {
        val newId = (_ui.value.items.size + 1).toString()
        val newChallenge = ChallengeUI(newId, name, description, "")
        _ui.value = _ui.value.copy(items = _ui.value.items + newChallenge)
    }

}


