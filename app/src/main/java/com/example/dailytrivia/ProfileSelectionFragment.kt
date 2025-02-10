package com.example.dailytrivia

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailytrivia.data.AppDatabase
import com.example.dailytrivia.database.ProfileEntity
import com.example.dailytrivia.databinding.FragmentProfileSelectionBinding
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileSelectionFragment : Fragment() {

    private var _binding: FragmentProfileSelectionBinding? = null
    private val binding get() = _binding!!

    private var listener: OnProfileSelectedListener? = null

    private var userId: Int? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as OnProfileSelectedListener
        } catch (_: ClassCastException) {
            throw RuntimeException(
                getString(
                    R.string.must_implement_onprofileselectedlistener,
                    context
                )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        userId = arguments?.getInt("userId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId?.let { id ->
            loadProfiles(id)
        } ?: println("User ID is missing!")
    }

    private fun loadProfiles(userId: Int) {
        lifecycleScope.launch {
            val profiles = withContext(Dispatchers.IO) {
                val database = AppDatabase.getDatabase(requireContext())
                database.profileDao().getProfilesByUserId(userId)
                // Fetch profiles from the database
            }

            if (profiles.isNotEmpty()) {
                setupRecyclerView(profiles)
            } else {
                println("No profiles found for the user!")
            }
        }
    }

    private fun setupRecyclerView(profiles: List<ProfileEntity>) {
        val adapter = ProfileAdapter(
            profiles.map {
                Profile(
                    it.id.toString(),
                    "Profile ${it.id}"
                )
            } // Map entities to displayable data
        ) { profileId ->
            listener?.onProfileSelected(profileId)
        }
        binding.profileRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.profileRecyclerView.adapter = adapter
    }

//    private fun showError(message: String) {
//        binding.errorTextView.visibility = View.VISIBLE
//        binding.errorTextView.text = message
//    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface OnProfileSelectedListener {
        fun onProfileSelected(profileId: String)
    }

    data class Profile(val id: String, val name: String)
}
