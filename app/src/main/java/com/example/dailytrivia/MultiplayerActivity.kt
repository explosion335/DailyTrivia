package com.example.dailytrivia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.dailytrivia.databinding.ActivityMultiplayerBinding
import com.google.android.material.transition.MaterialSharedAxis

class MultiplayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMultiplayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMultiplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.categoryMenu.setText(R.string.any)
        binding.difficultyMenu.setText(R.string.any)
        binding.typeMenu.setText(R.string.any)
        val fragment = JoinFragment()
        fragment.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        fragment.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.hostButton.setOnClickListener {
            val categoryName = binding.categoryMenu.text.toString()
            val difficultyName = binding.difficultyMenu.text.toString()
            val typeName = binding.typeMenu.text.toString()
            hostBundle(categoryName, difficultyName, typeName)
//            navController.navigate(R.id.hostFragment)
        }

        binding.joinButton.setOnClickListener {
            navController.navigate(R.id.joinFragment)
        }
    }

    private fun hostBundle(
        categoryName: String, difficultyName: String, typeName: String
    ) {
        val fragment = HostFragment().apply {
            arguments = Bundle().apply {
                putString("categoryName", categoryName)
                putString("difficultyName", difficultyName)
                putString("typeName", typeName)
            }
        }
        supportFragmentManager.beginTransaction().setReorderingAllowed(true)
            .replace(R.id.nav_host_fragment, fragment).addToBackStack(null).commit()
    }

}
