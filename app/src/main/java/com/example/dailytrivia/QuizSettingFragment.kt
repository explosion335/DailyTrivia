package com.example.dailytrivia

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dailytrivia.data.AppDatabase
import com.example.dailytrivia.data.UserPreferences
import com.example.dailytrivia.databinding.FragmentQuizSettingsBinding
import com.example.dailytrivia.endpoints.opentdb.OpenTDBApi
import com.example.dailytrivia.endpoints.opentdb.RetrofitInstance
import kotlinx.coroutines.launch
import java.util.Locale
import com.google.android.material.snackbar.Snackbar
import androidx.core.text.HtmlCompat
import androidx.lifecycle.MutableLiveData
import com.example.dailytrivia.ui.login.afterTextChanged

class QuizSettingsFragment : Fragment() {

    private var _binding: FragmentQuizSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var userPreferences: UserPreferences
    private lateinit var appDatabase: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        hideFab()
        _binding = FragmentQuizSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val _formState = MutableLiveData<FormState>()
    private fun showFab() {
        (activity as? QuizActivity)?.binding?.fabAddProfile?.visibility = View.VISIBLE
    }

    private fun hideFab() {
        (activity as? QuizActivity)?.binding?.fabAddProfile?.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        appDatabase = AppDatabase.getDatabase(requireContext())

        setupDropdownMenus()
        loadSettings()
        setupSaveButton()
        setupPlayButton()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        )

        _formState.observe(viewLifecycleOwner) { formState ->
            if (formState.numberError != null) {
                binding.numberOfQuestionsEditTextLayout.error =
                    getString(formState.numberError)
            } else {
                binding.numberOfQuestionsEditTextLayout.error = null
            }
        }
// Layout vs EditText
// https://stackoverflow.com/questions/30953449/design-android-edittext-to-show-error-message-as-described-by-google
        binding.numberOfQuestionsEditText.afterTextChanged {
            val inputText = binding.numberOfQuestionsEditText.text.toString()
            val number = inputText.toIntOrNull()
            val editText = binding.numberOfQuestionsEditText

            when {
                inputText.isEmpty() -> {
                    editText.error = getString(R.string.number_of_questions_empty)
                    binding.saveSettingsButton.isEnabled = false
                    binding.playQuizButton.isEnabled = false
                }

                number == null -> {
                    editText.error = getString(R.string.number_of_questions_invalid)
                    binding.saveSettingsButton.isEnabled = false
                    binding.playQuizButton.isEnabled = false
                }

                number < 1 -> {
                    editText.error = getString(R.string.number_of_questions_too_small)
                    binding.saveSettingsButton.isEnabled = false
                    binding.playQuizButton.isEnabled = false
                }

                number > 50 -> {
                    editText.error = getString(R.string.number_of_questions_too_large)
                    binding.saveSettingsButton.isEnabled = false
                    binding.playQuizButton.isEnabled = false
                }

                else -> {
                    editText.error = null
                    binding.saveSettingsButton.isEnabled = true
                    binding.playQuizButton.isEnabled = true
                }
            }
        }

    }


    private fun setupDropdownMenus() {

        val difficultyAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.difficulty_levels,
            android.R.layout.simple_dropdown_item_1line
        )
        binding.difficultyMenu.setAdapter(difficultyAdapter)

        val typeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.question_types,
            android.R.layout.simple_dropdown_item_1line
        )
        binding.typeMenu.setAdapter(typeAdapter)

        val encodingAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.encoding_types,
            android.R.layout.simple_dropdown_item_1line
        )
        binding.encodingMenu.setAdapter(encodingAdapter)
    }

    private fun setupPlayButton() {
        binding.playQuizButton.setOnClickListener {
            fetchTriviaQuestion()
        }
    }

    private fun fetchTriviaQuestion() {
        lifecycleScope.launch {
            try {
                val numQuestions =
                    binding.numberOfQuestionsEditText.text.toString().toIntOrNull() ?: 1
                val selectedCategory = binding.categoriesMenu.text.toString()
                val categoryId = if (selectedCategory == "Any") {
                    0
                } else {
                    val categoryDao = appDatabase.categoryDao()
                    categoryDao.getCategoryByName(selectedCategory)?.id ?: 0
                }
                val difficulty =
                    binding.difficultyMenu.text.toString().lowercase(Locale.getDefault())
                val type = when (binding.typeMenu.text.toString().lowercase(Locale.getDefault())) {
                    getString(R.string.type_boolean).lowercase(Locale.getDefault()) -> "boolean"
                    getString(R.string.type_multiple).lowercase(Locale.getDefault()) -> "multiple"
                    else -> "any"
                }

                // Create API instance
                val userAPI =
                    RetrofitInstance.RetrofitClient.getClient().create(OpenTDBApi::class.java)

                val queryParams = mutableMapOf<String, String>()
                queryParams["amount"] = numQuestions.toString()

                if (difficulty != "any") queryParams["difficulty"] = difficulty
                if (type != "any") queryParams["type"] = type
                if (categoryId != 0) queryParams["category"] = categoryId.toString()

                val triviaResponse = userAPI.getQuestions(queryParams)

                if (triviaResponse.responseCode == 0 && triviaResponse.results.isNotEmpty()) {
                    // Decode questions and answers
                    val decodedQuestions = triviaResponse.results.map { question ->
                        question.copy(
                            question = HtmlCompat.fromHtml(
                                question.question,
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            ).toString(),
                            correctAnswer = HtmlCompat.fromHtml(
                                question.correctAnswer,
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            ).toString(),
                            incorrectAnswers = question.incorrectAnswers.map {
                                HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                            }
                        )
                    }

                    val intent = Intent(requireContext(), PlayQuizActivity::class.java)
                    intent.putExtra("QUESTIONS", ArrayList(decodedQuestions))
                    startActivity(intent)
                } else {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_no_questions),
                        Snackbar.LENGTH_SHORT
                    ).setAction(R.string.dismiss) {}.show()
                }
            } catch (e: Exception) {
                if (e.message == getString(R.string.http_error_429)) {
                    view?.let {
                        Snackbar.make(
                            it.findViewById(R.id.fragment_container),
                            getString(R.string.error_rate_limit_exceeded),
                            Snackbar.LENGTH_SHORT
                        ).setAction(R.string.dismiss) {}.show()
                    }
                } else {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_fetching_questions) + " ${e.message}",
                        Snackbar.LENGTH_SHORT
                    ).setAction(R.string.dismiss) {}.show()
                }
            }
        }
    }


    private fun loadSettings() {
        lifecycleScope.launch {
            val profileId = arguments?.getString("profileId")?.toIntOrNull()
            if (profileId == null) {
                return@launch
            }

            try {
                val profileDao = appDatabase.profileDao()
                val profile = profileDao.getProfileByProfileID(profileId)

                if (profile != null) {
                    binding.numberOfQuestionsEditText.setText(
                        String.format(Locale.getDefault(), "%d", profile.numberOfQuestions)
                    )

                    binding.difficultyMenu.setText(
                        getString(
                            when (profile.difficulty) {
                                "easy" -> R.string.difficulty_easy
                                "medium" -> R.string.difficulty_medium
                                "hard" -> R.string.difficulty_hard
                                else -> R.string.any
                            }
                        ),
                        false
                    )

                    binding.typeMenu.setText(
                        getString(
                            when (profile.type) {
                                "boolean" -> R.string.type_boolean
                                "multiple" -> R.string.type_multiple
                                else -> R.string.any
                            }
                        ),
                        false
                    )

                    binding.encodingMenu.setText(
                        getString(
                            when (profile.encoding) {
                                "default" -> R.string.encoding_defaultUrl
                                "legacy" -> R.string.encoding_legacyUrl
                                "url3986" -> R.string.encoding_url3986
                                "base64" -> R.string.encoding_base64
                                else -> R.string.any
                            }
                        ),
                        false
                    )

                    val categoryDao = appDatabase.categoryDao()
                    val category = categoryDao.getAllCategories()

                    if (category.isNotEmpty()) {
                        val categoryNames = listOf("Any") + category.map { it.name }
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            categoryNames
                        )
                        binding.categoriesMenu.setAdapter(adapter)

                        val currentCategory = category.find { it.id == profile.categoryId }
                        binding.categoriesMenu.setText(currentCategory?.name ?: "Any", false)
                    } else {
                        binding.categoriesMenu.setText(getString(R.string.category_not_found))
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.profile_not_found), Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error loading profile: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun setupSaveButton() {
        binding.saveSettingsButton.setOnClickListener {
            lifecycleScope.launch {
                val profileId = arguments?.getString("profileId")?.toIntOrNull()
                if (profileId == null) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.invalid_profile_id), Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val numQuestionsInput = binding.numberOfQuestionsEditText.text.toString()
                val numQuestions = numQuestionsInput.toIntOrNull()?.coerceIn(1, 50) ?: 10

                val difficulty =
                    binding.difficultyMenu.text.toString().lowercase(Locale.getDefault())
                val type = when (binding.typeMenu.text.toString().lowercase(Locale.getDefault())) {
                    getString(R.string.type_boolean).lowercase(Locale.getDefault()) -> "boolean"
                    getString(R.string.type_multiple).lowercase(Locale.getDefault()) -> "multiple"
                    else -> "any"
                }
                val encoding = binding.encodingMenu.text.toString().lowercase(Locale.getDefault())
                val categoryName = binding.categoriesMenu.text.toString()

                try {
                    val categoryDao = appDatabase.categoryDao()
                    val categoryEntity = categoryDao.getCategoryByName(categoryName)

                    if (categoryEntity == null) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_category_not_found),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    val categoryId = categoryEntity.id

                    val profileDao = appDatabase.profileDao()
                    profileDao.updateProfileByUserId(
                        profileId,
                        numQuestions,
                        difficulty,
                        type,
                        encoding,
                        categoryId = categoryId
                    )

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.profile_saved), Toast.LENGTH_SHORT
                    ).show()

                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        buildString {
                            append(getString(R.string.error_saving_profiles))
                            append(e.message)
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        showFab()
    }
}

